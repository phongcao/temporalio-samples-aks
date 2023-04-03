package pantanal.temporal.worker.workflows.extendtimeout;

import io.temporal.activity.Activity;

public class LongRunningActivityImpl implements LongRunningActivity {

  @Override
  public void activityMethodA(long duration) {
    simulateLoad(duration);
  }

  @Override
  public void activityMethodB(long duration) {
    simulateLoad(duration);
  }

  @Override
  public void activityMethodC(long duration) {
    simulateLoad(duration);
  }

  private void simulateLoad(long duration) {

    // Simulate a long-running operation
    final long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < startTime + duration) {
      Activity.getExecutionContext().heartbeat(System.currentTimeMillis() - startTime);
      Thread.yield();
    }
  }
}
