package edu.geo4.duke.gui;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ControlsContainer extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private JSlider mySpeedControl;
    
    public ControlsContainer (final ApplicationFrame frame) {
        super();
        mySpeedControl = new JSlider(SwingConstants.HORIZONTAL, -50, 50, 0);
        mySpeedControl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                float stretchFactor = ((float) -mySpeedControl.getValue() / 100f) + 1f;
                frame.updateStretchFactor(stretchFactor);
            }
        });
        add(mySpeedControl);
        setVisible(true);
    }

}
