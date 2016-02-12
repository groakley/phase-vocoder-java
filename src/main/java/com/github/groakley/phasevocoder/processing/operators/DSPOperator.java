/*
 * Copyright (c) 2016 Grant Oakley
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. The ASF licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.groakley.phasevocoder.processing.operators;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.github.groakley.phasevocoder.processing.players.ICaller;


public class DSPOperator implements ICallee {

  private volatile ICaller myCaller = null;
  private volatile int myJobID;
  private volatile boolean isRunning = true;

  // protected BlockingQueue<Float> inputBuffer = new LinkedBlockingQueue<Float>();
  protected BlockingQueue<Float> inputBuffer = new LinkedBlockingQueue<Float>(8192);

  @Override
  public final void run() {
    while (isRunning) {
      if (myCaller != null) {
        float[] output;
        try {
          output = process();
          myCaller.answer(this, myJobID, output);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected float[] process() throws InterruptedException {
    float[] output = new float[inputBuffer.size()];
    for (int i = 0; i < output.length; i++) {
      output[i] = inputBuffer.take();
    }
    return output;
  }

  @Override
  public void call(ICaller caller, int jobID, float[] data) throws InterruptedException {
    myCaller = caller;
    myJobID = jobID;
    for (Float f : data) {
      inputBuffer.put(f);
    }
  }

  @Override
  public void stop() {
    isRunning = false;
  }

  @Override
  public ICallee getNewInstance() {
    return new DSPOperator();
  }

  @Override
  public int remainingCapacity() {
    return inputBuffer.remainingCapacity();
  }

}
