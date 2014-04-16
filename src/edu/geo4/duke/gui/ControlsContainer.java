package edu.geo4.duke.gui;

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

public class ControlsContainer extends JPanel {

  private static final long serialVersionUID = 1L;

  private JSlider mySpeedControl;

  public ControlsContainer(final ApplicationFrame frame) {
    super();

    final int SLIDER_MIN = -50;
    final int SLIDER_MAX = 50;
    mySpeedControl = new JSlider(SwingConstants.HORIZONTAL, SLIDER_MIN, SLIDER_MAX, 0);
    mySpeedControl.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        float stretchFactor = ((float) -mySpeedControl.getValue() / 100f) + 1f;
        frame.updateStretchFactor(stretchFactor);
      }
    });
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    labelTable.put(new Integer(SLIDER_MIN), new JLabel("Slower"));
    labelTable.put(new Integer(SLIDER_MAX), new JLabel("Faster"));
    mySpeedControl.setLabelTable(labelTable);
    mySpeedControl.setPaintLabels(true);
    add(mySpeedControl);

    add(new JLabel("      "));

    final JCheckBox mySetLock = new JCheckBox("Lock Phase");
    mySetLock.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        frame.updateLocked(mySetLock.isSelected());
      }
    });
    add(mySetLock);
    setVisible(true);
  }

}
