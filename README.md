# Deploying Temporal on Azure

There are two main components that need to be deployed:
  - `Temporal Server`: This service keeps track of workflows, activities, and tasks and
  coordinates workers' execution.
  - `Workers`: These are compute nodes that run your Temporal application code.
  You compile your workflows and activities into a worker executable. Next you can run the
  worker executable locally or deploy it on Azure and it will listen for new tasks to process.

In this sample project, you will deploy a Temporal server on Azure Kubernetes Service (AKS)
and a worker application on Azure Container Instances (ACI).

## Deploying Temporal Server

![Temporal Server Only](/docs/img/temporal-server-only.png)

A Temporal server only required dependency for basic operation is a database. This sample
project uses [Azure Database for MySQL](https://azure.microsoft.com/en-us/products/mysql).

### Setting up Azure Database for MySQL

1. In Azure portal, create an `Azure Database for MySQL Flexible Server` resource.
Take note of `Admin username` and `Password` for later use.
2. Under `Networking` tab, select
`Allow public access from any Azure service within Azure to this server` so that temporal
server deployed on AKS can access the database.
3. Go to the database, select `Server parameters`, search for `require_secure_transport`
and set it to `OFF`.

    ![MySQL allow insecure transport](/docs/img/mysql-enable-insecure.png)

    > **_NOTE:_** This is still safe since the network policy only allows internal traffic
    on Azure. However, in a production environment, you should consider using the secure
    transport by adding an SSL certificate.

### Deploying Temporal Server on AKS

1. Create an AKS cluster:

    ```bash
    az aks create \
      --resource-group [Resource Group] \
      --name [AKS Name] \
      --location [Region] \
      --node-count 2 \
      --node-vm-size Standard_B2ms \
      --kubernetes-version 1.25.5 \
      --verbose
    ```

2. Enable `kubectl` to access the cluster:

    ```bash
    az aks get-credentials \
      --resource-group [Resource Group] \
      --name [AKS Name]
    ```

3. Create secrets required for deployment:

    ```bash
    kubectl create secret generic temporal-server \
      --from-literal db=mysql \
      --from-literal hostname=[MySQL API server address] \
      --from-literal username=[MySQL admin username] \
      --from-literal password=[MySQL admin password]
    ```

4. Deploy a Temporal server using kubectl:

    ```bash
    cd service/deployment
    kubectl apply -f temporal-server-deployment.yaml
    kubectl apply -f temporal-server-service.yaml
    kubectl apply -f temporal-web-deployment.yaml
    kubectl apply -f temporal-web-service.yaml
    ```

5. Access the Temporal Web UI via its IP address:

    ![Temporal Web UI's IP Address](/docs/img/temporal-web-ip.png)

## Deploying Worker Application

![Temporal Server and Worker](/docs/img/temporal-server-and-worker.png)

The sample worker app is a Java Spring Boot application that you are going to deploy on ACI.

### Building Docker Image

1. Build a local Docker image:

    ```bash
    cd worker
    docker build . -t temporal-worker-app
    ```

2. Get the temporal server's public IP:

    ![Temporal server's IP Address](/docs/img/temporal-server-ip.png)

3. Run the worker app locally:

    ```bash
    docker run -e TEMPORAL_SERVER_HOSTNAME=[Temporal Server IP] temporal-worker-app
    ```

### Publish Image on Azure Container Registry (ACR)

1. Create an Azure Container Registry resource:

    ```bash
    az acr create \
      --name [ACR Name] \
      --resource-group [Resource Group] \
      --sku Standard \
      --admin-enabled true
    ```

2. Login to ACR:

    ```bash
    az acr login \
      --name [ACR Name]
    ```

3. Push the local Docker image to ACR:

    ```bash
    docker tag temporal-worker-app:latest [ACR Name].azurecr.io/temporal-worker-app:latest
    docker push [ACR Name].azurecr.io/temporal-worker-app:latest
    ```

### Deploying Worker App on ACI

```bash
az container create \
  --resource-group [Resource Group] \
  --name [Container Name] \
  --registry-username [ACR Login Username] \
  --registry-password [ACR Login Password] \
  --image [ACR Name].azurecr.io/temporal-worker-app:latest \
  --environment-variables TEMPORAL_SERVER_HOSTNAME=[Temporal Server IP] \
  --dns-name-label temporal-worker \
  --restart-policy always
```

## Invoking Workflow using Temporal CLI (tctl)

![Temporal Architecture](/docs/img/temporal-architecture.png)

There are many ways to invoke a temporal workflow. In this sample project, you will use
the [Temporal CLI](https://docs.temporal.io/tctl-v1).

### MoneyTransferWorkflow

```bash
tctl \
  --address [Temporal Server IP]:7233 \
  workflow run \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type MoneyTransferWorkflow \
  --input '"AccountA"' \
  --input '"AccountB"' \
  --input '"REF00001"' \
  --input 10000
```

The money transfer workflow will run and display the following result:

![The Money Transfer Workflow Result](/docs/img/money-transfer-workflow-result.png)

### DynamicDslWorkflow

A dynamic DSL workflow requires three input parameters: 
  - `id`: defined in a [YAML file](worker/src/main/resources/dsl/customerapplication/workflow.yml#L1).
  - `version`: defined in a [YAML file](worker/src/main/resources/dsl/customerapplication/workflow.yml#L3).
  - `input data`: workflow input data.

```bash
tctl \
  --address [Temporal Server IP]:7233 \
  workflow run \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type DynamicDslWorkflow \
  --input '"customerapplication"' \
  --input '"1.0"' \
  --input '{"customer":{"name":"John","age":22},"results":[]}'
```

### ExtendTimeoutWorkflow

This workflow demonstrates how to use
[signals](https://docs.temporal.io/application-development/features?lang=java#signals) to
extend timeout values of activities.

#### Test Case 1 - Timeout issue

```bash
tctl \
  --address [Temporal Server IP]:7233 \
  workflow run \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type ExtendTimeoutWorkflow \
  --input 10000
```

After invoking the above workflow, there is a timeout issue:

![Activity Timeout](/docs/img/activity-timeout.png)

The reason being the default activity timeout is `15 seconds` but the second and third
activity methods take `20 seconds`:

```java
  private final ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
      // Timeout options specify when to automatically timeout Activities if the
      // process is taking too long.
      .setStartToCloseTimeout(Duration.ofSeconds(15))
      .setHeartbeatTimeout(Duration.ofSeconds(5))
      .setRetryOptions(retryoptions)
      .build();

  @Override
  public void start(long duration) {

    // The first activity method duration should be lower than the default
    // timeout value and always complete successfully.
    longRunningActivity.activityMethodA(duration);

    // The increased duration value in the second and third activity method
    // to ensure that they'll encounter a timeout issue if there is no change.
    longRunningActivity.activityMethodB(duration * 2);
    longRunningActivity.activityMethodC(duration * 2);
  }
```

#### Test Case 2 - Successful after increasing timeout

```bash
cd worker/scripts/tctl
./signal_extend_timeout.sh
```

In the script above, after invoking the workflow, tctl sends a signal to the workflow
to increase the timeout values of the activity's methods.

Although it has no effect on the first activity since it's already running, it helps
the second and third ones complete successfully.

### TripBookingWorkflow

![The Trip Booking Workflow](https://raw.githubusercontent.com/berndruecker/trip-booking-saga-java/master/docs/example-use-case.png)

#### Test Case 1 - Smooth Run

```bash
tctl \
  --address [Temporal Server IP]:7233 \
  workflow run \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type TripBookingWorkflow \
  --input '"mytrip1"' \
  --input 1000 \
  --input '"success"' \
  --input '"success"' \
  --input '"success"'
```

The workflow just completes successfully.

#### Test Case 2 - Failure and Rollback

```bash
tctl \
  --address [Temporal Server IP]:7233 \
  workflow run \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type TripBookingWorkflow \
  --input '"mytrip2"' \
  --input 1000 \
  --input '"success"' \
  --input '"success"' \
  --input '"fail"'
```

In this case, the flight booking has failed so the Saga implemention helps invoke
the compensation methods for hotel and car reservations:

![Saga invokes compensation methods](/docs/img/temporal-saga-rollback.png)

#### Test Case 3 - Cancel and Rollback

```bash
cd worker/scripts/tctl
./cancel_workflow.sh
```

In this case, an external signal is sent to cancel all running activities gracefully and
the compensation methods are invoked.

![Saga invokes compensation methods](/docs/img/temporal-cancel-rollback.png)

## Troubleshooting

1. `./gradlew not found` issue when building Docker images on Windows:
- *Solution*: converting EOL to UNIX (LF) of the gradlew file.
