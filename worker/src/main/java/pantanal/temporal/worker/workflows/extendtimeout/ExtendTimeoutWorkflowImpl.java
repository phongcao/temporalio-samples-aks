package pantanal.temporal.worker.workflows.extendtimeout;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import io.temporal.common.RetryOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ExtendTimeoutWorkflowImpl implements ExtendTimeoutWorkflow {

  private static final String ACTIVITY_METHOD_A = "ActivityMethodA";
  private static final String ACTIVITY_METHOD_B = "ActivityMethodB";
  private static final String ACTIVITY_METHOD_C = "ActivityMethodC";

  // RetryOptions specify how to automatically handle retries when Activities
  // fail.
  private final RetryOptions retryoptions = RetryOptions.newBuilder()
      .setInitialInterval(Duration.ofSeconds(1))
      .setMaximumInterval(Duration.ofSeconds(100))
      .setBackoffCoefficient(2)
      .setMaximumAttempts(1)
      .build();

  private final ActivityOptions defaultActivityOptions = ActivityOptions.newBuilder()
      // Timeout options specify when to automatically timeout Activities if the
      // process is taking too long.
      .setStartToCloseTimeout(Duration.ofSeconds(15))
      .setHeartbeatTimeout(Duration.ofSeconds(5))
      // Optionally provide customized RetryOptions.
      // Temporal retries failures by default, this is simply an example.
      .setRetryOptions(retryoptions)
      .build();

  private final LongRunningActivity longRunningActivity = Workflow.newActivityStub(
      LongRunningActivity.class,
      defaultActivityOptions,
      null);

  // The start method is the entry point to the Workflow.
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

  @Override
  public void updateTimeout(long timeout) {

    final Map<String, ActivityOptions> newOptions = new HashMap<String, ActivityOptions>() {
      {
        // Change timeout value of the `activityMethodA` method
        put(ACTIVITY_METHOD_A, ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMillis(timeout)).build());

        // Change timeout value of the `activityMethodB` method
        put(ACTIVITY_METHOD_B, ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMillis(timeout)).build());

        // Change timeout value of the `activityMethodC` method
        put(ACTIVITY_METHOD_C, ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMillis(timeout)).build());
      }
    };

    Workflow.applyActivityOptions(newOptions);
  }
}
