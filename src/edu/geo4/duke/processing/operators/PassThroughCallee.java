package edu.geo4.duke.processing.operators;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import edu.geo4.duke.processing.players.ICaller;


public class PassThroughCallee implements ICallee {

    private volatile ICaller myCaller = null;
    private volatile int myJobID;
    private volatile boolean isRunning = true;

    protected BlockingQueue<Byte> inputBuffer = new LinkedBlockingQueue<Byte>();

    @Override
    public final void run () {
        while (isRunning) {
            if (myCaller != null) {
                byte[] output;
                try {
                    output = process();
                    myCaller.answer(this, myJobID, output);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected byte[] process () throws InterruptedException {
        byte[] output = new byte[inputBuffer.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = inputBuffer.take();
        }
        return output;
    }

    @Override
    public void answer (ICaller caller, int jobID, byte[] data) throws InterruptedException {
        myCaller = caller;
        myJobID = jobID;
        for (Byte b : data) {
            inputBuffer.put(b);
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
