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

package pantanal.temporal.worker.workflows.dsl;

import io.temporal.activity.Activity;
import pantanal.temporal.worker.workflows.dsl.model.ActResult;
import pantanal.temporal.worker.workflows.dsl.model.Customer;

public class DslActivitiesImpl implements DslActivities {
  @Override
  public ActResult checkCustomerInfo(Customer customer) {
    try {
      return new ActResult(
          Activity.getExecutionContext().getInfo().getActivityType(), "checkCustomerInfo invoked");
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public ActResult updateApplicationInfo(Customer customer) {
    try {
      return new ActResult(
          Activity.getExecutionContext().getInfo().getActivityType(),
          "updateApplicationInfo invoked");
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public ActResult approveApplication(Customer customer) {
    try {
      return new ActResult("decision", "Application APPROVED");
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public ActResult rejectApplication(Customer customer) {
    try {
      return new ActResult("decision-" + customer.getName(), "Application DENIED");
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public ActResult invokeBankingService(Customer customer) {
    try {
      return new ActResult(
          Activity.getExecutionContext().getInfo().getActivityType(),
          "invokeBankingService invoked");
    } catch (Exception e) {
      return null;
    }
  }
}
