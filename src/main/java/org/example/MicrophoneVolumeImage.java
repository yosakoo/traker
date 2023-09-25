package org.example;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MicrophoneVolumeImage {

    private static Point mouseDownCompCoords;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Microphone Volume Image");
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));

        MicrophoneVolumePanel panel = new MicrophoneVolumePanel();
        frame.add(panel);

        frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                mouseDownCompCoords = null;
            }
        });

        frame.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.startListening();
    }
}

class MicrophoneVolumePanel extends JPanel {

    private BufferedImage[] images;
    private int currentImageIndex = 0;
    private double previousVolume = 0.0;
    private Map<String, Consumer<Double>> settingHandlers = new HashMap<>();
    private double minVolume1;
    private double maxVolume1;
    private double minVolume2;
    private double maxVolume2;
    private double minVolume3;
    private double maxVolume3;
    private static final int DEFAULT_WINDOW_WIDTH = 400;
    private static final int DEFAULT_WINDOW_HEIGHT = 300;
    private static final String SETTINGS_FILE = "resources/settings.txt";
    private static final String IMAGES_FOLDER = "resources/images";

    private int windowWidth = DEFAULT_WINDOW_WIDTH;
    private int windowHeight = DEFAULT_WINDOW_HEIGHT;

    public MicrophoneVolumePanel() {
        loadImages();
        initSettingHandlers();
        loadSettingsFromFile(SETTINGS_FILE);
        setPreferredSize(new Dimension(windowWidth, windowHeight));
    }
    private void initSettingHandlers() {
        settingHandlers.put("minVolume1", value -> minVolume1 = value);
        settingHandlers.put("maxVolume1", value -> maxVolume1 = value);
        settingHandlers.put("minVolume2", value -> minVolume2 = value);
        settingHandlers.put("maxVolume2", value -> maxVolume2 = value);
        settingHandlers.put("minVolume3", value -> minVolume3 = value);
        settingHandlers.put("maxVolume3", value -> maxVolume3 = value);
        settingHandlers.put("windowWidth", value -> windowWidth = value.intValue());
        settingHandlers.put("windowHeight", value -> windowHeight = value.intValue());
    }

    private void loadImages() {
        File folder = new File(IMAGES_FOLDER);
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (imageFiles != null) {
            images = new BufferedImage[imageFiles.length];
            for (int i = 0; i < imageFiles.length; i++) {
                try {
                    images[i] = ImageIO.read(imageFiles[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadSettingsFromFile(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    double value = Double.parseDouble(parts[1].trim());
                    processSetting(key, value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processSetting(String key, double value) {
        settingHandlers.getOrDefault(key, v -> {}).accept(value);
    }

    public void startListening() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
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
        if (images != null && images.length > 0) {
            int imageIndex;
            System.out.println(volume);

            if (volume >= minVolume1 && volume < maxVolume1) {
                imageIndex = 0;
            } else if (volume >= minVolume2 && volume < maxVolume2) {
                imageIndex = 1;
            } else if (volume >= minVolume3 && volume <= maxVolume3) {
                imageIndex = 2;
            } else {
                imageIndex = 0;
            }

            if (imageIndex != currentImageIndex) {
                currentImageIndex = imageIndex;
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (images != null && images.length > 0) {
            g.drawImage(images[currentImageIndex], 0, 0, getWidth(), getHeight(), null);
        }
    }
}
