package edu.geo4.duke.processing.operators;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import edu.geo4.duke.processing.players.ICaller;


public class DSPOperator implements ICallee {

    private volatile ICaller myCaller = null;
    private volatile int myJobID;
    private volatile boolean isRunning = true;

//    protected BlockingQueue<Float> inputBuffer = new LinkedBlockingQueue<Float>();
    protected BlockingQueue<Float> inputBuffer = new LinkedBlockingQueue<Float>(8192);

    @Override
    public final void run () {
        while (isRunning) {
            if (myCaller != null) {
                float[] output;
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

    protected float[] process () throws InterruptedException {
        float[] output = new float[inputBuffer.size()];
        for (int i = 0; i < output.length; i++) {
            output[i] = inputBuffer.take();
        }
        return output;
    }

    @Override
    public void call (ICaller caller, int jobID, float[] data) throws InterruptedException {
        myCaller = caller;
        myJobID = jobID;
        for (Float f : data) {
            inputBuffer.put(f);
        }
    }

    @Override
    public void stop () {
        isRunning = false;
    }

    @Override
    public ICallee getNewInstance () {
        return new DSPOperator();
    }

    @Override
    public int remainingCapacity () {
        return inputBuffer.remainingCapacity();
    }

}
