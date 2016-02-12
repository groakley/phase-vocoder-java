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

import com.github.groakley.phasevocoder.processing.players.ICaller;


/**
 * This interface describes a thread that operate on streams of bytes. Its intended application is
 * in DSP, where it is intended to provide a general interface for "black-box" signal processing
 * units.
 * 
 * @author Grant Oakley
 * 
 */
public interface ICallee extends Runnable {

  /**
   * Inputs data to be processed. If the operator's buffer is full, this command blocks, and will
   * not execute into there is room in the buffer. Will not work if the thread has not been started.
   * 
   * @param caller process to report back to
   * @param jobID integer identifying the called process
   * @param data data to be processed as an array of bytes
   */
  public void call(ICaller caller, int jobID, float[] data) throws InterruptedException;

  /**
   * Stops the thread and releases any of its resources.
   */
  public void stop();

  /**
   * Initializes and returns a copy an instance of this class with the same input parameters it was
   * given in its constructor. This is used to get copies of the initial instance for multiple audio
   * channels.
   * 
   * @return copy of this instance as it was originally created
   */
  public ICallee getNewInstance();

  /**
   * Finds how many more elements could be added without blocking.
   * 
   * @return the capacity remaining before the call operation will block
   */
  public int remainingCapacity();

}
