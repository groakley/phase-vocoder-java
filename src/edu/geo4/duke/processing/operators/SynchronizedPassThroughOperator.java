package edu.geo4.duke.processing.operators;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class SynchronizedPassThroughOperator implements SynchronizedByteOperator {

    private BlockingQueue<Byte> inputQueue;
    private BlockingQueue<Byte> outputQueue;
    private volatile boolean isActive;

    public SynchronizedPassThroughOperator () {
        inputQueue = new LinkedBlockingQueue<Byte>();
        outputQueue = new LinkedBlockingQueue<Byte>();
    }

    @Override
    public void run () {
        isActive = true;
        while (isActive) {
            try {
                Byte b = inputQueue.poll(50L, TimeUnit.MILLISECONDS);
                if (b != null) {
                    outputQueue.put(b);
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close () {
        isActive = false;
    }

    @Override
    public void put (byte[] data) throws InterruptedException {
        for (Byte b : data) {
            inputQueue.put(b);
        }

    }

    @Override
    public byte[] take (int size) throws InterruptedException {
        byte[] output = new byte[size];
        int i = 0;
        while (i < size) {
            Byte b = outputQueue.poll(100L, TimeUnit.MILLISECONDS);
            if (b == null) {
                output[i] = 0;
            }
            else {
                output[i] = b;
            }
            i++;
        }
        return output;
    }

    @Override
    public int bytesRemaining () {
        return outputQueue.size();
    }

}
