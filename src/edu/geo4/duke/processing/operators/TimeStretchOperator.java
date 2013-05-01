package edu.geo4.duke.processing.operators;

import java.util.Arrays;
import java.util.LinkedList;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import edu.geo4.duke.processing.window.WindowBuilder;
import edu.geo4.duke.util.CapacityQueue;


public class TimeStretchOperator extends PassThroughCallee {

    private static final int WLen = 2048;

    private CapacityQueue<Byte> input;
    private CapacityQueue<Byte> output;

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
        input = new CapacityQueue<Byte>(WLen);
        output = new CapacityQueue<Byte>(WLen);
        for (int i = 0; i < output.getCapacity(); i++) {
            output.offer((byte) 0);
        }
    }

    @Override
    protected byte[] process () throws InterruptedException {
        if (inputBuffer.size() < WLen) { return new byte[0]; }

        float[] grain = getNextGrain();
        fftShift(grain);
        float[] doubleGrain = Arrays.copyOf(grain, WLen * 2);
        FFT.realForwardFull(doubleGrain);
        float[] r = findReal(doubleGrain);
        float[] phi = findImag(doubleGrain);

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
        FFT.realInverseFull(doubleGrain, false);
        grain = extractReal(doubleGrain);
        fftShift(grain);
        for (int i = 0; i < grain.length; i++) {
            grain[i] = grain[i] * w1[i];
        }

        phi0 = Arrays.copyOf(phi, phi0.length);

        LinkedList<Byte> finishedBytes = new LinkedList<Byte>();
        for (int i = 0; i < n2; i++) {
            finishedBytes.add(output.poll());
            output.offer((byte) 0);
        }
        Byte[] outBytes = new Byte[output.getCapacity()];
        output.toArray(outBytes);

        for (int i = 0; i < outBytes.length; i++) {
            outBytes[i] = new Byte((byte) (mapFloatToByte(grain[i]) + outBytes[i].byteValue()));
        }
        output.addAll(Arrays.asList(outBytes));

        byte[] finalOutput = new byte[finishedBytes.size()];
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
        Byte[] inBytes = new Byte[WLen];
        input.toArray(inBytes);

        float[] grain = new float[WLen];
        for (int i = 0; i < WLen; i++) {
            grain[i] = mapByteToFloat(inBytes[i]) * w1[i];
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

    private static float[] findReal (float[] complex) {
        float[] realPart = new float[complex.length / 2];
        for (int i = 0; i < realPart.length; i++) {
            realPart[i] =
                    (float) Math
                            .sqrt(Math.pow(complex[2 * i], 2) + Math.pow(complex[2 * i + 1], 2));
        }
        return realPart;
    }

    private static float[] findImag (float[] complex) {
        float[] imagPart = new float[complex.length / 2];
        for (int i = 0; i < imagPart.length; i++) {
            imagPart[i] = (float) Math.atan((complex[2 * i] + 1) / complex[2 * i]);
        }
        return imagPart;
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

    private static float mapByteToFloat (byte inByte) {
        final float m = 2f / 255f;
        final float b = 1f - m * 127f;
        return m * (float) inByte + b;
    }

    private static byte mapFloatToByte (float inFloat) {
        final float m = 255f / 2f;
        final float b = 127f - m;
        return (byte) (m * inFloat + b);
    }

}
