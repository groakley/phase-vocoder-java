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

package com.github.groakley.phasevocoder.processing.window;

public class WindowBuilder {

  public static float[] buildHanning(int length, boolean periodic) {
    float[] window = new float[length];
    int N;
    if (periodic) {
      N = length + 1;
      for (int i = 1; i < length; i++) {
        window[i] = (float) (0.5 * (1.0 - Math.cos((2.0 * Math.PI * (i)) / (N - 1))));
      }
    } else {
      N = length + 2;
      for (int i = 0; i < length; i++) {
        window[i] = (float) (0.5 * (1.0 - Math.cos((2.0 * Math.PI * (i + 1)) / (N - 1))));
      }
    }
    return window;
  }

  public static float[] buildOmega(int length, int analysisHop) {
    float[] window = new float[length];
    for (int i = 0; i < length; i++) {
      window[i] = (float) (2 * Math.PI * analysisHop * i / length);
    }
    return window;
  }
}
