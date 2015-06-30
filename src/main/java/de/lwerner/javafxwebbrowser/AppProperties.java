package de.lwerner.javafxwebbrowser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Properties;

/**
 *
 * @author Lukas Werner
 */
public class AppProperties {
    
    private static final String FILE_NAME = "config.properties";
    private static final File COPIED_FILE = new File(System.getProperty("user.dir") + "/" + FILE_NAME);
    private static final AppProperties INSTANCE = new AppProperties();

    private final Properties delegate;
    
    public AppProperties() {
        delegate = new Properties();
    }
    
    public synchronized void readAppProperties() throws IOException {
        if (!COPIED_FILE.exists()) {
            Files.copy(getClass().getResourceAsStream(FILE_NAME), COPIED_FILE.toPath());
        }
        delegate.load(new FileReader(COPIED_FILE));
        Properties temp = new Properties();
        try (InputStream in = getClass().getResourceAsStream(FILE_NAME)) {
            temp.load(in);
            for (PropertyName prop: PropertyName.values()) {
                if (!delegate.contains(prop.propertyKey)) {
                    setProperty(prop, temp.getProperty(prop.propertyKey));
                }
            }
        }
    }
    
    public synchronized String getProperty(final PropertyName key) {
        return delegate.getProperty(key.propertyKey);
    }
    
    public synchronized void setProperty(final PropertyName key, final String value) {
        delegate.setProperty(key.propertyKey, value);
    }
    
    public static final AppProperties getInstance() {
        return INSTANCE;
    }

    public void writeAppProperties() throws IOException {
        delegate.store(new PrintWriter(COPIED_FILE), getProperty(PropertyName.APP_NAME) + " Properties");
    }
    
}