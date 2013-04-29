package edu.geo4.duke.processing.window;

public class WindowBuilder {

    public static float[] buildHamming (int length) {
        float[] window = new float[length];
        for (int i = 0; i < length; i++) {
            window[i] = 0.54f - (float) (0.46 * Math.cos((2 * Math.PI * i) / (length - 1)));
        }
        return window;
    }

    public static float[] buildOmega (int length, int analysisHop) {
        float[] window = new float[length];
        for (int i = 0; i < length; i++) {
            window[i] = (float) (2 * Math.PI * analysisHop * i / length);
        }
        return window;
    }
}
