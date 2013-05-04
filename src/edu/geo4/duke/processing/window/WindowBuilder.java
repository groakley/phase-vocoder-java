package edu.geo4.duke.processing.window;

public class WindowBuilder {

    public static float[] buildHanning (int length, boolean periodic) {
        float[] window = new float[length];
        int N;
        if (periodic) {
            N = length + 1;
            for (int i = 1; i < length; i++) {
                window[i] = (float) (0.5 * (1.0 - Math.cos((2.0 * Math.PI * (i)) / (N - 1))));
            }
        }
        else {
            N = length + 2;
            for (int i = 0; i < length; i++) {
                window[i] = (float) (0.5 * (1.0 - Math.cos((2.0 * Math.PI * (i + 1)) / (N - 1))));
            }
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
