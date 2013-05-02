package edu.geo4.duke.gui;

import javax.swing.JFrame;
import edu.geo4.duke.processing.operators.TimeStretchOperator;


public class ApplicationFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private TimeStretchOperator myOp;

    public ApplicationFrame (TimeStretchOperator tso) {
        super("Time Stretchhhhh");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myOp = tso;
        add(new ControlsContainer(this));
        pack();
        setResizable(false);
        setVisible(true);
    }

    protected void updateStretchFactor (float stretchFactor) {
        myOp.updateStretchFactor(stretchFactor);
    }

}
