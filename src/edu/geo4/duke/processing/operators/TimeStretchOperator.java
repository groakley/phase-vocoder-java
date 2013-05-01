package edu.geo4.duke.processing.operators;

import java.util.Arrays;
import java.util.LinkedList;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import edu.geo4.duke.processing.window.WindowBuilder;
import edu.geo4.duke.util.CapacityQueue;


public class TimeStretchOperator extends PassThroughCallee {

    private static final int WLen = 2048;

    private CapacityQueue<Float> input;
    private CapacityQueue<Float> output;

    private final float[] w1;
    private final int n1 = 512;
    private volatile int n2;

    private FloatFFT_1D FFT;
    private float[] omega;
    private float[] phi0;
    private float[] psi;

    public TimeStretchOperator (float stretchFactor) {
        FFT = new FloatFFT_1D(WLen);
        n2 = (int) (n1 * stretchFactor);
        w1 = WindowBuilder.buildHamming(WLen);
        omega = WindowBuilder.buildOmega(WLen, n1);
        phi0 = new float[WLen];
        psi = new float[WLen];
        input = new CapacityQueue<Float>(WLen);
        output = new CapacityQueue<Float>(WLen);
        for (int i = 0; i < output.getCapacity(); i++) {
            output.offer(0f);
        }
    }

    @Override
    protected float[] process () throws InterruptedException {
        if (inputBuffer.size() < WLen) { return new float[0]; }

        float[] grain = getNextGrain();
        fftShift(grain);

        float[] doubleGrain = new float[grain.length * 2];
        for (int i = 0; i < grain.length; i++) {
            doubleGrain[i] = grain[i];
        }

        FFT.realForwardFull(doubleGrain);
        float[] r = findMag(doubleGrain);
        float[] phi = findAngle(doubleGrain);

        float[] delta_phi = new float[WLen];
        for (int i = 0; i < delta_phi.length; i++) {
            float diff = phi[i] - phi0[i] - omega[i];
            delta_phi[i] = omega[i] + princarg(diff);
            psi[i] = princarg(psi[i] + delta_phi[i] * getStretchFactor());
        }
        for (int i = 0; i < doubleGrain.length - 1; i = i + 2) {
            float psiTmp = psi[i / 2];
            float rTmp = r[i / 2];
            doubleGrain[i] = (float) Math.cos(psiTmp) * rTmp;
            doubleGrain[i + 1] = (float) Math.sin(psiTmp) * rTmp;
        }
        FFT.complexInverse(doubleGrain, true);
        grain = extractReal(doubleGrain);
        fftShift(grain);
        for (int i = 0; i < grain.length; i++) {
            grain[i] = grain[i] * w1[i];
        }

        phi0 = Arrays.copyOf(phi, phi0.length);

        LinkedList<Float> finishedBytes = new LinkedList<Float>();
        for (int i = 0; i < n2; i++) {
            finishedBytes.add(output.poll());
            output.offer(0f);
        }
        Float[] outBytes = new Float[output.getCapacity()];
        output.toArray(outBytes);

        for (int i = 0; i < outBytes.length; i++) {
            outBytes[i] = new Float(grain[i] + outBytes[i]);
        }
        output.addAll(Arrays.asList(outBytes));

        float[] finalOutput = new float[finishedBytes.size()];
        for (int i = 0; i < finalOutput.length; i++) {
            finalOutput[i] = finishedBytes.poll();
        }
        return finalOutput;
    }

    private float[] getNextGrain () throws InterruptedException {
        if (input.size() == 0) {
            while (input.size() < input.getCapacity()) {
                input.offer(inputBuffer.take());
            }
        }
        else {
            for (int i = 0; i < n1; i++) {
                input.offer(inputBuffer.take());
            }
        }
        Float[] inBytes = input.toArray(new Float[0]);

        float[] grain = new float[WLen];
        for (int i = 0; i < WLen; i++) {
            grain[i] = inBytes[i] * w1[i];
        }
        return grain;
    }

    private static float[] extractReal (float[] complex) {
        float[] realPart = new float[complex.length / 2];
        for (int i = 0; i < realPart.length; i++) {
            realPart[i] = complex[2 * i];
        }
        return realPart;
    }

    private static float[] findMag (float[] complex) {
        float[] mag = new float[complex.length / 2];
        for (int i = 0; i < mag.length; i++) {
            mag[i] =
                    (float) Math
                            .sqrt(Math.pow(complex[2 * i], 2) + Math.pow(complex[2 * i + 1], 2));
        }
        return mag;
    }

    private static float[] findAngle (float[] complex) {
        float[] angle = new float[complex.length / 2];
        for (int i = 0; i < angle.length; i++) {
            angle[i] = (float) Math.atan2(complex[2 * i + 1], complex[2 * i]);
        }
        return angle;
    }

    private static void fftShift (float[] data) {
        if (data.length % 2 == 0) {
            fftShiftEven(data);
        }
        else {
            fftShiftOdd(data);
        }
    }

    private static void fftShiftOdd (float[] data) {
        int shiftAmt = data.length / 2;
        int remaining = data.length;
        int curr = 0;
        float save = data[curr];
        while (remaining >= 0) {
            float next = data[(curr + shiftAmt) % data.length];
            data[(curr + shiftAmt) % data.length] = save;
            save = next;
            curr = (curr + shiftAmt) % data.length;
            remaining--;
        }
    }

    private static void fftShiftEven (float[] data) {
        for (int i = 0; i < data.length / 2; i++) {
            float tmp = data[i];
            data[i] = data[i + data.length / 2];
            data[i + data.length / 2] = tmp;
        }
    }

    private static float princarg (float phase) {
        return (float) ((((double) phase + Math.PI) % (-2 * Math.PI)) + Math.PI);
    }

    public void updateStretchFactor (int stretchFactor) {
        n2 = (int) (n1 * stretchFactor);
        output.setCapacity(n2);
    }

    public float getStretchFactor () {
        return (float) n2 / (float) n1;
    }

    @Override
    public ICallee getNewInstance () {
        return new TimeStretchOperator(getStretchFactor());
    }

}
