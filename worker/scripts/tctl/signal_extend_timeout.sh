# Specify workflow id for easy testing
workflowId='extend-timeout-workflow-id'

# Asynchronously invoke workflow
activityDurationMs=10000
tctl \
  --address localhost:7233 \
  workflow start \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type ExtendTimeoutWorkflow \
  --workflow_id $workflowId \
  --input $activityDurationMs

# Sleep 1 second to make sure that the workflow has run
sleep 1

# Increase timeout value to 30 seconds for activities
timeoutMs=30000
tctl \
  --address localhost:7233 \
  workflow signal \
  --workflow_id $workflowId \
  --name updateTimeout \
  --input $timeoutMs \
  --yes

# Check progress
tctl \
  --address localhost:7233 \
  workflow observe \
  --workflow_id $workflowId
