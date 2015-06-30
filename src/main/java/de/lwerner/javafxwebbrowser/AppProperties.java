package de.lwerner.javafxwebbrowser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Lukas Werner
 * 
 * TODO Have to make properties writable
 */
public class AppProperties {
    
    private static final String FILE_NAME = "config.properties";
    private static final AppProperties INSTANCE = new AppProperties();

    private final Properties delegate;
    
    public AppProperties() {
        delegate = new Properties();
    }
    
    public synchronized void readAppProperties() throws IOException {
        try (final InputStream in = getClass().getResourceAsStream(FILE_NAME)) {
            delegate.load(in);
        }
    }
    
    public synchronized String getProperty(final PropertyName key) {
        return delegate.getProperty(key.propertyKey);
    }
    
//    public synchronized void setProperty(final PropertyName key, final String value) {
//        delegate.setProperty(key.propertyKey, value);
//    }
    
    public static final AppProperties getInstance() {
        return INSTANCE;
    }
    
}