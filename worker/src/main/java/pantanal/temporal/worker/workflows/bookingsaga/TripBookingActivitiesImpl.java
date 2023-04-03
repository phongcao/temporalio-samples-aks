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

import java.util.UUID;

import io.temporal.activity.Activity;

public class TripBookingActivitiesImpl implements TripBookingActivities {

  @Override
  public String reserveCar(String name, long duration, String carBookingResult) {
    System.out.println("reserving car for '" + name + "'");
    simulateLoad(duration);

    if (carBookingResult.equalsIgnoreCase("fail")) {
      throw new RuntimeException("Car booking did not work");
    }

    return UUID.randomUUID().toString();
  }

  @Override
  public String bookHotel(String name, long duration, String hotelBookingResult) {
    System.out.println("booking hotel for '" + name + "'");
    simulateLoad(duration);

    if (hotelBookingResult.equalsIgnoreCase("fail")) {
      throw new RuntimeException("Hotel booking did not work");
    }

    return UUID.randomUUID().toString();
  }

  @Override
  public String bookFlight(String name, long duration, String flightBookingResult) {
    System.out.println("booking flight for '" + name + "'");
    simulateLoad(duration);

    if (flightBookingResult.equalsIgnoreCase("fail")) {
      throw new RuntimeException("Flight booking did not work");
    }

    return UUID.randomUUID().toString();
  }

  @Override
  public String cancelFlight(String reservationID, String name) {
    System.out.println("cancelling flight reservation '" + reservationID + "' for '" + name + "'");
    simulateLoad(1000);

    return UUID.randomUUID().toString();
  }

  @Override
  public String cancelHotel(String reservationID, String name) {
    System.out.println("cancelling hotel reservation '" + reservationID + "' for '" + name + "'");
    simulateLoad(1000);

    return UUID.randomUUID().toString();
  }

  @Override
  public String cancelCar(String reservationID, String name) {
    System.out.println("cancelling car reservation '" + reservationID + "' for '" + name + "'");
    simulateLoad(1000);

    return UUID.randomUUID().toString();
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
