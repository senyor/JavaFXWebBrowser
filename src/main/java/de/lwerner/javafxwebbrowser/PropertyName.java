package de.lwerner.javafxwebbrowser;

/**
 *
 * @author Lukas Werner
 */
public enum PropertyName {
    
    // Basic configuration
    DEBUG("debug"),

    // App properties
    APP_NAME("app.name"),
    APP_VERSION("app.version"),
    APP_HISTORYSIZE("app.historysize"),
    
    // URL properties
    URL_HOME("url.home"),
    URL_SEARCH("url.search"),
    URL_GOOGLESUGGEST("url.googlesuggest");
    
    final String propertyKey;
    
    PropertyName(final String propertyKey) {
        this.propertyKey = propertyKey;
    }
    
}