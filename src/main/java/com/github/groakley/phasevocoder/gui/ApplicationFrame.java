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

package com.github.groakley.phasevocoder.gui;

import com.github.groakley.phasevocoder.processing.operators.TimeStretchOperator;

import javax.swing.*;


public final class ApplicationFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  private final TimeStretchOperator timeStretchOperator;

  public ApplicationFrame(TimeStretchOperator timeStretchOperator) {
    super("Time Stretchhhhh");
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.timeStretchOperator = timeStretchOperator;
    add(new ControlsContainer(this));
    pack();
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  void updateStretchFactor(float stretchFactor) {
    timeStretchOperator.updateStretchFactor(stretchFactor);
  }

  void updateLocked(boolean locked) {
    timeStretchOperator.updateLockPhase(locked);
  }

}
