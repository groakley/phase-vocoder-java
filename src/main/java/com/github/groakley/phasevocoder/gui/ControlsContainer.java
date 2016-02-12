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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class ControlsContainer extends JPanel {
  private static final long serialVersionUID = 1L;

  ControlsContainer(final ApplicationFrame frame) {
    super();

    JSlider timeStretchSlider = createTimeStretchSlider(frame);
    JCheckBox phaseLockCheckbox = createPhaseLockCheckbox(frame);

    add(timeStretchSlider);
    // Add a horizontal spacer. At this point I think I'll pass on actually learning Swing.
    add(new JLabel("      "));
    add(phaseLockCheckbox);

    setVisible(true);
  }

  private JSlider createTimeStretchSlider(final ApplicationFrame frame) {
    final int SLIDER_MIN = -50;
    final int SLIDER_MAX = 50;

    JSlider speedControlSlider = new JSlider(SwingConstants.HORIZONTAL, SLIDER_MIN, SLIDER_MAX, 0);
    speedControlSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        float stretchFactor = ((float) -speedControlSlider.getValue() / 100f) + 1f;
        frame.updateStretchFactor(stretchFactor);
      }
    });
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(SLIDER_MIN, new JLabel("Slower"));
    labelTable.put(SLIDER_MAX, new JLabel("Faster"));
    speedControlSlider.setLabelTable(labelTable);
    speedControlSlider.setPaintLabels(true);
    return speedControlSlider;
  }

  private JCheckBox createPhaseLockCheckbox(final ApplicationFrame frame) {
    final JCheckBox mySetLock = new JCheckBox("Lock Phase");
    mySetLock.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        frame.updateLocked(mySetLock.isSelected());
      }
    });
    return mySetLock;
  }
}
