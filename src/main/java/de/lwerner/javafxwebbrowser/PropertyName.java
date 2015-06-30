package de.lwerner.javafxwebbrowser;

/**
 *
 * @author Lukas Werner
 */
public enum PropertyName {

    // App properties
    APP_NAME("app.name"),
    
    // URL properties
    URL_HOME("url.home"),
    URL_SEARCH("url.search");
    
    final String propertyKey;
    
    PropertyName(final String propertyKey) {
        this.propertyKey = propertyKey;
    }
    
}