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

package com.github.groakley.phasevocoder.processing.players;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.github.groakley.phasevocoder.processing.operators.ICallee;
import com.github.groakley.phasevocoder.processing.operators.TimeStretchOperator;

public class Mp3Player implements ICaller {

  private WavPlayer myWavPlayer;

  public Mp3Player(File mp3file, ICallee operator) {
    testPlay(mp3file);
  }

  public void testPlay(File file) {
    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(file);
      AudioInputStream din = null;
      AudioFormat baseFormat = in.getFormat();
      AudioFormat decodedFormat =
          new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
              baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
              false);
      din = AudioSystem.getAudioInputStream(decodedFormat, in);

      myWavPlayer = new WavPlayer(din, new TimeStretchOperator(1.0f, false));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void answer(ICallee callee, int jobID, float[] reply) {
    // Assumes the WavPlayer successfully initialized.
    myWavPlayer.answer(callee, jobID, reply);
  }

  @Override
  public void run() {
    myWavPlayer.run();
  }

}
