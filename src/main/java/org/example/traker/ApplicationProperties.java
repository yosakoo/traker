package org.example.traker;

import java.util.Arrays;

public class ApplicationProperties {

    private int[] volumeThreshold;

    private int windowWidth = 400;

    private int windowHeight = 300;

    public int[] getVolumeThreshold() {
        return volumeThreshold;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "volumeThreshold=" + Arrays.toString(volumeThreshold) +
                ", windowWidth=" + windowWidth +
                ", windowHeight=" + windowHeight +
                '}';
    }
}
