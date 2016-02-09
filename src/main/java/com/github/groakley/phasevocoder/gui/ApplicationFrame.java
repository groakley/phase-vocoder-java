package com.github.groakley.phasevocoder.gui;

import com.github.groakley.phasevocoder.processing.operators.TimeStretchOperator;
import javax.swing.JFrame;


public class ApplicationFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  private TimeStretchOperator myOp;

  public ApplicationFrame(TimeStretchOperator tso) {
    super("Time Stretchhhhh");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    myOp = tso;
    add(new ControlsContainer(this));
    pack();
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  protected void updateStretchFactor(float stretchFactor) {
    myOp.updateStretchFactor(stretchFactor);
  }

  protected void updateLocked(boolean locked) {
    myOp.updateLockPhase(locked);
  }

}
