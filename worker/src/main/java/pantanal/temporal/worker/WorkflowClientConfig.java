package pantanal.temporal.worker;

import io.temporal.authorization.AuthorizationGrpcMetadataProvider;
import io.temporal.authorization.AuthorizationTokenSupplier;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowClientConfig {

  @Value("${temporal.server.url}")
  private String temporalServerUrl;

  @Bean
  WorkflowClient workflowClient(){

    // Un-comment to enable authorization
    // AuthorizationTokenSupplier tokenSupplier =
    //   () -> "Bearer ...";

    WorkflowServiceStubs service =
      WorkflowServiceStubs.newServiceStubs(
        WorkflowServiceStubsOptions.newBuilder()
          // .addGrpcMetadataProvider(new AuthorizationGrpcMetadataProvider(tokenSupplier))
          .setTarget(temporalServerUrl).build());

    WorkflowClient client = WorkflowClient.newInstance(service);

    return client;
  }
}
