package org.example.traker;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class MicrophoneVolumePanel extends JPanel {

    private final ApplicationProperties properties;
    private static final String IMAGES_FOLDER = "resources/images";
    private BufferedImage[] images;
    private int currentImageIndex = 0;

    public MicrophoneVolumePanel(ApplicationProperties properties) throws IOException {
        this.properties = properties;
        loadImages();
    }

    private void loadImages() throws IOException {
        File folder = new File(IMAGES_FOLDER);
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if(imageFiles == null)
            throw new NoSuchFileException("no image files found");

        images = new BufferedImage[imageFiles.length];

        for (int i = 0; i < imageFiles.length; i++) {
            images[i] = ImageIO.read(imageFiles[i]);
        }
    }

    public void startListening() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        byte[] buffer = new byte[4096];
        while (true) {
            int bytesRead = line.read(buffer, 0, buffer.length);
            double volume = calculateVolume(buffer, bytesRead);
            updateImage(volume);
        }
    }

    private double calculateVolume(byte[] audioData, int bytesRead) {
        double sum = 0;
        for (int i = 0; i < bytesRead; i += 2) {
            short sample = (short) ((audioData[i + 1] << 8) | (audioData[i] & 0xFF));
            sum += Math.abs(sample);
        }
        return sum / (bytesRead / 2);
    }

    private void updateImage(double volume) {
        int imageIndex;

        if (volume <= properties.getVolumeThreshold()[0]) {
            imageIndex = 0;
        } else if (volume <= properties.getVolumeThreshold()[1]) {
            imageIndex = 1;
        } else if (volume <= properties.getVolumeThreshold()[2]) {
            imageIndex = 2;
        } else {
            imageIndex = 0;
        }

        if (imageIndex != currentImageIndex) {
            currentImageIndex = imageIndex;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(images[currentImageIndex], 0, 0, getWidth(), getHeight(), null);
    }
}
