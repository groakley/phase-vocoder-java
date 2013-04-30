package edu.geo4.duke.processing.operators;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import edu.geo4.duke.processing.players.ICaller;
import edu.geo4.duke.processing.window.WindowBuilder;


public class TimeStretchOperator implements ICallee {

    private static final int WLen = 2048;

    private final float[] w1;
    private final int n1 = 512;
    private int n2;

    private float[] omega;
    private float[] phi0;
    private float[] psi;

    public TimeStretchOperator (float stretchFactor) {
        n2 = (int) (n1 * stretchFactor);
        w1 = WindowBuilder.buildHamming(WLen);
        omega = WindowBuilder.buildOmega(WLen, n1);
    }

    @Override
    public void run () {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void answer (ICaller caller, int jobID, byte[] data) {
        // TODO Auto-generated method stub
        
    }
    
    private static void fftShift (byte[] data) {
        if (data.length % 2 == 0) {
            fftShiftEven(data);
        }
        else {
            fftShiftOdd(data);
        }
    }

    private static void fftShiftOdd (byte[] data) {
        int shiftAmt = data.length / 2;
        int remaining = data.length;
        int curr = 0;
        byte save = data[curr];
        while (remaining >= 0) {
            byte next = data[(curr + shiftAmt) % data.length];
            data[(curr + shiftAmt) % data.length] = save;
            save = next;
            curr = (curr + shiftAmt) % data.length;
            remaining--;
        }
    }

    private static void fftShiftEven (byte[] data) {
        for (int i = 0; i < data.length / 2; i++) {
            byte tmp = data[i];
            data[i] = data[i + data.length / 2];
            data[i + data.length / 2] = tmp;
        }
    }
    
    public void updateStretchFactor (int stretchFactor) {
        n2 = (int) (n1 * stretchFactor);
    }
    
    public float getStretchFactor () {
        return (float) n2 / (float) n1;
    }


    @Override
    public void stop () {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ICallee getNewInstance () {
        // TODO Auto-generated method stub
        return null;
    }

}
