package org.example.traker;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Entry {

    private static final String SETTINGS_FILE = "resources/settings.txt";

    public static void main(String[] args) throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, LineUnavailableException {
        BufferedReader reader = new BufferedReader(new FileReader(SETTINGS_FILE));
        ApplicationProperties properties = new PropertiesMapper(reader).readProperties(ApplicationProperties.class);

        MicrophoneVolumePanel panel = new MicrophoneVolumePanel(properties);
        panel.setPreferredSize(new Dimension(properties.getWindowWidth(), properties.getWindowHeight()));

        Application app = new Application("Microphone Volume Image")
                .withDefaultProperties()
                .withDefaultListeners();

        app.add(panel);
        app.pack();
        panel.startListening();
    }
}
