#!/bin/bash
### This bash script deploy the infrastruce for deploying temporal java app to an aks cluster.

(
    set -euo pipefail

    # Configure environment variables
    if [ -f .env ]
    then
        set -o allexport; source .env; set +o allexport
    else
        error "Missing .env file. Check .env.template"
    fi

    # Creating resource  group
    echo "Creating resource group"
    az group create \
    --name $RESOURCE_GROUP_NAME \
    --location $LOCATION

    # Creating AKS
    echo "Creating AKS cluster"
    az aks create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $AKS_CLUSTER_NAME \
    --location $LOCATION \
    --node-count 2 \
    --node-vm-size Standard_B2ms \
    --kubernetes-version 1.25.5 \
    --verbose

    echo "Starting AKS get credentials"
    az aks get-credentials \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $AKS_CLUSTER_NAME \
    --overwrite-existing

    # creating MySQL
    echo "Creating MySQL"
    az mysql flexible-server create \
    --resource-group $RESOURCE_GROUP_NAME \
    --name $DB_SERVER \
    --location $LOCATION \
    --admin-user $MYSQL_USER \
    --admin-password $MYSQL_PASSWORD \
    --sku-name Standard_B2ms \
    --public-access 0.0.0.0 \
    --yes

    az mysql flexible-server parameter set \
    --resource-group $RESOURCE_GROUP_NAME \
    --server-name $DB_SERVER \
    --name require_secure_transport \
    --value OFF

    kubectl create secret generic temporal-server \
        --from-literal db=mysql \
        --from-literal hostname="$DB_SERVER.mysql.database.azure.com" \
        --from-literal username=$MYSQL_USER \
        --from-literal password=$MYSQL_PASSWORD

    echo "Deploying app and services"
    cd ../deployment
    kubectl apply -f temporal-server-deployment.yaml
    kubectl apply -f temporal-server-service.yaml
    kubectl apply -f temporal-web-deployment.yaml
    kubectl apply -f temporal-web-service.yaml
)