package pantanal.temporal.worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import pantanal.temporal.worker.workflows.bookingsaga.TripBookingActivitiesImpl;
import pantanal.temporal.worker.workflows.bookingsaga.TripBookingWorkflowImpl;
import pantanal.temporal.worker.workflows.dsl.DslActivitiesImpl;
import pantanal.temporal.worker.workflows.dsl.DynamicDslWorkflow;
import pantanal.temporal.worker.workflows.extendtimeout.ExtendTimeoutWorkflowImpl;
import pantanal.temporal.worker.workflows.extendtimeout.LongRunningActivityImpl;
import pantanal.temporal.worker.workflows.moneytransfer.AccountActivityImpl;
import pantanal.temporal.worker.workflows.moneytransfer.MoneyTransferWorkflowImpl;

@SpringBootApplication
public class TemporalWorkerApp {

  @Value("${temporal.workflow.taskqueue}")
  private String workflowTaskQueue;

  private WorkflowClient workflowClient;

	public TemporalWorkerApp(WorkflowClient workflowClient)
	{
		this.workflowClient = workflowClient;
	}

	public static void main(String[] args) {
		SpringApplication.run(TemporalWorkerApp.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner() {
		return args -> {
        // Worker factory is used to create Workers that poll specific Task Queues.
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        Worker worker = factory.newWorker(workflowTaskQueue);

        // Register Money Transfer workflow and its activities
        worker.registerWorkflowImplementationTypes(MoneyTransferWorkflowImpl.class);
        worker.registerActivitiesImplementations(new AccountActivityImpl());

        // Register DSL Workflow and its activities
        worker.registerWorkflowImplementationTypes(DynamicDslWorkflow.class);
        worker.registerActivitiesImplementations(new DslActivitiesImpl());

        // Register Extend Timeout workflow and its activities
        worker.registerWorkflowImplementationTypes(ExtendTimeoutWorkflowImpl.class);
        worker.registerActivitiesImplementations(new LongRunningActivityImpl());

        // Register Booking Saga workflow and its activities
        worker.registerWorkflowImplementationTypes(TripBookingWorkflowImpl.class);
        worker.registerActivitiesImplementations(new TripBookingActivitiesImpl());

        // Start listening to the Task Queue.
        factory.start();
		};
	}	
}
