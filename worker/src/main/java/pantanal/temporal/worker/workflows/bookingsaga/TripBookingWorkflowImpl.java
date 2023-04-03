/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package pantanal.temporal.worker.workflows.bookingsaga;

import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class TripBookingWorkflowImpl implements TripBookingWorkflow {

  private final ActivityOptions options = ActivityOptions.newBuilder()
      .setStartToCloseTimeout(Duration.ofHours(1))
      .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
      .setCancellationType(ActivityCancellationType.WAIT_CANCELLATION_COMPLETED)
      .build();

  private final TripBookingActivities activities = Workflow.newActivityStub(TripBookingActivities.class, options);

  private CancellationScope cancellationScope;

  @Override
  public void bookTrip(String name, long duration, String carBookingResult, String hotelBookingResult,
      String flightBookingResult) {

    // Configure SAGA to run compensation activities in parallel
    Saga.Options sagaOptions = new Saga.Options.Builder()
        .setParallelCompensation(true)
        .build();

    Saga saga = new Saga(sagaOptions);

    try {
      cancellationScope = Workflow.newCancellationScope(
          () -> {
            String carReservationID = activities.reserveCar(name, duration, carBookingResult);
            saga.addCompensation(activities::cancelCar, carReservationID, name);

            String hotelReservationID = activities.bookHotel(name, duration, hotelBookingResult);
            saga.addCompensation(activities::cancelHotel, hotelReservationID, name);

            String flightReservationID = activities.bookFlight(name, duration, flightBookingResult);
            saga.addCompensation(activities::cancelFlight, flightReservationID, name);
          });

      cancellationScope.run();

    } catch (ActivityFailure e) {
      saga.compensate();
    }
  }

  @Override
  public void cancelWorkflow() {
    cancellationScope.cancel();
  }
}
