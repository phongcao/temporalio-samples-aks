package pantanal.temporal.worker.workflows.extendtimeout;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ExtendTimeoutWorkflow {

  @WorkflowMethod
  void start(long duration);

  @SignalMethod
  void updateTimeout(long timeout);
}
