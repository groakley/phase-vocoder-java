package edu.geo4.duke.processing.operators;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import edu.geo4.duke.processing.players.ICaller;


public class PassThroughCallee implements ICallee {

    private volatile ICaller myCaller = null;
    private volatile int myJobID;
    private volatile boolean isRunning = true;

    private BlockingQueue<Byte> input = new LinkedBlockingQueue<Byte>();
    
    @Override
    public void run () {
        while (isRunning) {
            if (myCaller != null) {
                byte[] output = new byte[input.size()];
                try {
                    for (int i = 0; i < output.length; i++) {
                        output[i] = input.take();
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myCaller.answer(this, myJobID, output);
            }
        }

    }

    @Override
    public void answer (ICaller caller, int jobID, byte[] data) {
        myCaller = caller;
        myJobID = jobID;
        for (Byte b : data) {
            try {
                input.put(b);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop () {
        isRunning = false;
    }

    @Override
    public ICallee getNewInstance () {
        return new PassThroughCallee();
    }

}
