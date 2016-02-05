package edu.geo4.duke.processing.operators;

import java.util.Arrays;
import java.util.LinkedList;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import edu.geo4.duke.processing.window.WindowBuilder;
import edu.geo4.duke.util.CapacityQueue;


public class TimeStretchOperator extends DSPOperator {

  private static final int WIN_LEN = 2048;

  private CapacityQueue<Float> input;
  private CapacityQueue<Float> output;

  private final float[] w1;
  private final int n1 = 512;
  private static volatile int n2;
  private static volatile boolean myLockPhase;

  private FloatFFT_1D FFT;
  private float[] omega;
  private float[] phi0;
  private float[] psi;

  public TimeStretchOperator(float stretchFactor, boolean lockPhase) {
    FFT = new FloatFFT_1D(WIN_LEN);
    n2 = (int) (n1 * stretchFactor);
    myLockPhase = lockPhase;
    w1 = WindowBuilder.buildHanning(WIN_LEN, true);
    omega = WindowBuilder.buildOmega(WIN_LEN, n1);
    phi0 = new float[WIN_LEN];
    psi = new float[WIN_LEN];
    input = new CapacityQueue<Float>(WIN_LEN);
    output = new CapacityQueue<Float>(WIN_LEN);
    for (int i = 0; i < output.getCapacity(); i++) {
      output.offer(0f);
    }
  }

  @Override
  protected float[] process() throws InterruptedException {

    if (inputBuffer.size() < WIN_LEN) {
      return new float[0];
    }

    int grainN2 = n2;
    float grainStretchFactor = (float) grainN2 / (float) n1;

    float[] grain = getNextGrain();
    fftShift(grain);

    grain = timeStretchGrain(grainStretchFactor, grain);

    LinkedList<Float> finishedBytes = overlapAddAndSlide(grainN2, grain);

    float[] finalOutput = new float[finishedBytes.size()];
    float overlapScaling = (float) WIN_LEN / ((float) grainN2 * 2.0f);
    for (int i = 0; i < finalOutput.length; i++) {
      finalOutput[i] = finishedBytes.poll() / overlapScaling;
    }
    return finalOutput;
  }

  private LinkedList<Float> overlapAddAndSlide(int grainN2, float[] grain) {
    LinkedList<Float> finishedBytes = new LinkedList<Float>();
    for (int i = 0; i < grainN2; i++) {
      finishedBytes.add(output.poll());
      output.offer(0f);
    }
    Float[] outBytes = output.toArray(new Float[0]);

    for (int i = 0; i < outBytes.length; i++) {
      outBytes[i] = new Float(grain[i] + outBytes[i]);
    }
    output.addAll(Arrays.asList(outBytes));
    return finishedBytes;
  }

  private float[] timeStretchGrain(float grainStretchFactor, float[] grain) {
    float[] doubleGrain = new float[grain.length * 2];
    for (int i = 0; i < grain.length; i++) {
      doubleGrain[i] = grain[i];
    }

    FFT.realForwardFull(doubleGrain);
    float[] r = findMag(doubleGrain);
    float[] phi = findAngle(doubleGrain);

    float[] delta_phi = new float[WIN_LEN];
    for (int i = 0; i < delta_phi.length; i++) {
      float diff = phi[i] - phi0[i] - omega[i];
      delta_phi[i] = omega[i] + princarg(diff);
      psi[i] = princarg(psi[i] + delta_phi[i] * grainStretchFactor);
    }

    for (int i = 0; i < doubleGrain.length - 1; i = i + 2) {
      float psiTmp = psi[i / 2];
      float rTmp = r[i / 2];
      doubleGrain[i] = (float) Math.cos(psiTmp) * rTmp;
      doubleGrain[i + 1] = (float) Math.sin(psiTmp) * rTmp;
    }

    if (myLockPhase) {
      float[] phaseLocked = new float[doubleGrain.length];
      for (int i = 2; i < phaseLocked.length - 2; i++) {
        phaseLocked[i] = doubleGrain[i - 2] + doubleGrain[i] + doubleGrain[i + 2];
      }
      float[] phaseLockAngles = findAngle(phaseLocked);
      setAngles(doubleGrain, phaseLockAngles);
    }

    FFT.complexInverse(doubleGrain, true);
    grain = extractReal(doubleGrain);
    fftShift(grain);
    for (int i = 0; i < grain.length; i++) {
      grain[i] = grain[i] * w1[i];
    }

    phi0 = Arrays.copyOf(phi, phi0.length);
    return grain;
  }

  private float[] getNextGrain() throws InterruptedException {
    if (input.size() == 0) {
      while (input.size() < input.getCapacity()) {
        input.offer(inputBuffer.take());
      }
    } else {
      for (int i = 0; i < n1; i++) {
        input.offer(inputBuffer.take());
      }
    }
    Float[] inBytes = input.toArray(new Float[0]);

    float[] grain = new float[WIN_LEN];
    for (int i = 0; i < WIN_LEN; i++) {
      grain[i] = inBytes[i] * w1[i];
    }
    return grain;
  }

  private static float[] extractReal(float[] complex) {
    float[] realPart = new float[complex.length / 2];
    for (int i = 0; i < realPart.length; i++) {
      realPart[i] = complex[2 * i];
    }
    return realPart;
  }

  private static float[] findMag(float[] complex) {
    float[] mag = new float[complex.length / 2];
    for (int i = 0; i < mag.length; i++) {
      mag[i] = (float) Math.sqrt(Math.pow(complex[2 * i], 2) + Math.pow(complex[2 * i + 1], 2));
    }
    return mag;
  }

  private static void setAngles(float[] complex, float[] newAngles) {
    float[] mags = findMag(complex);
    for (int i = 0; i < newAngles.length; i++) {
      complex[2 * i] = (float) (mags[i] * Math.cos(newAngles[i]));
      complex[2 * i + 1] = (float) (mags[i] * Math.sin(newAngles[i]));
    }
  }

  private static float[] findAngle(float[] complex) {
    float[] angle = new float[complex.length / 2];
    for (int i = 0; i < angle.length; i++) {
      angle[i] = (float) Math.atan2(complex[2 * i + 1], complex[2 * i]);
    }
    return angle;
  }

  private static void fftShift(float[] data) {
    if (data.length % 2 == 0) {
      fftShiftEven(data);
    } else {
      fftShiftOdd(data);
    }
  }

  private static void fftShiftOdd(float[] data) {
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

  private static void fftShiftEven(float[] data) {
    for (int i = 0; i < data.length / 2; i++) {
      float tmp = data[i];
      data[i] = data[i + data.length / 2];
      data[i + data.length / 2] = tmp;
    }
  }

  private static float princarg(float phase) {
    return (float) ((((double) phase + Math.PI) % (-2 * Math.PI)) + Math.PI);
  }

  public synchronized void updateStretchFactor(float stretchFactor) {
    n2 = (int) ((float) n1 * stretchFactor);
  }

  public synchronized void updateLockPhase(boolean lockPhase) {
    myLockPhase = lockPhase;
  }

  public synchronized float getStretchFactor() {
    return (float) n2 / (float) n1;
  }

  @Override
  public ICallee getNewInstance() {
    return new TimeStretchOperator(getStretchFactor(), myLockPhase);
  }

}
