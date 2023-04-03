# Specify workflow id for easy testing
workflowId='cancel-saga-workflow'

# Asynchronously invoke workflow
activityDurationMs=5000
tctl \
  --address localhost:7233 \
  workflow start \
  --taskqueue MAIN_TASK_QUEUE \
  --workflow_type TripBookingWorkflow \
  --workflow_id $workflowId \
  --input '"mytrip3"' \
  --input $activityDurationMs \
  --input '"success"' \
  --input '"success"' \
  --input '"success"'

# Sleep 8 seconds
sleep 8

# Cancel workflow gracefully by sending a signal
tctl \
  --address localhost:7233 \
  workflow signal \
  --workflow_id $workflowId \
  --name cancelWorkflow \
  --yes

# Check progress
tctl \
  --address localhost:7233 \
  workflow observe \
  --workflow_id $workflowId
