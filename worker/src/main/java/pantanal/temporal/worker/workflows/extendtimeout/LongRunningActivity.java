package pantanal.temporal.worker.workflows.extendtimeout;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface LongRunningActivity {

  @ActivityMethod
  void activityMethodA(long duration);

  @ActivityMethod
  void activityMethodB(long duration);

  @ActivityMethod
  void activityMethodC(long duration);
}
