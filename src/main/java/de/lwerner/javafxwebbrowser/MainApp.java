package de.lwerner.javafxwebbrowser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

public class MainApp extends Application {

    private static AppProperties properties;
    
    private Stage mainWindow;

    private WebView webView;
    private WebEngine webEngine;
    
    private Toolbar toolbar;
    
    private final File historyFile = new File(System.getProperty("user.dir") + "/" + "history.txt");
    
    public boolean hasNext() {
        return webEngine.getHistory().getCurrentIndex() < webEngine.getHistory().getEntries().size() - 1;
    }
    
    public boolean hasPrevious() {
        // Little bug: First site change later recognized
        return webEngine.getHistory().getCurrentIndex() > 0;
    }
        
    public void next() {
        try {
            webEngine.getHistory().go(1);
        } catch (Exception e) {
            // DO NOTHING
        }
    }
    
    public void previous() {
        try {
            webEngine.getHistory().go(-1);
        } catch (Exception e) {
            // DO NOTHING
        }
    }
    
    public Window getWindow() {
        return mainWindow;
    }

    public void loadHome() {
        loadSite(properties.getProperty(PropertyName.URL_HOME));
    }
    
    public void updateHome() {
        properties.setProperty(PropertyName.URL_HOME, webEngine.getLocation());
    }
    
    public void loadSite(String urlAsString) {
        if (urlAsString == null || urlAsString.isEmpty()) {
            return;
        }
        if (!urlAsString.contains("://")) {
            urlAsString = "http://" + urlAsString;
        }
        webEngine.load(urlAsString);
    }

    public void search(String query) throws UnsupportedEncodingException {
        if (query != null && !query.isEmpty()) {
            String urlAsString = properties.getProperty(PropertyName.URL_SEARCH);
            urlAsString += URLEncoder.encode(query, "UTF-8");
            loadSite(urlAsString);
        }
    }

    private void writeHistory() throws IOException {
        Files.write(historyFile.toPath(), toolbar.getHistory(), Charset.defaultCharset());
    }

    private void loadHistory() throws IOException {
        if (historyFile.exists()) {
            List<String> tempList = Files.readAllLines(historyFile.toPath());
            toolbar.setHistory(tempList);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        properties = AppProperties.getInstance();
        try {
            properties.readAppProperties();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                properties.writeAppProperties();
                writeHistory();
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }));

        Font.loadFont(getClass().getResource("fontawesome-webfont.ttf")
                .toExternalForm(), 12);

        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                toolbar.setCurrentUrl(newValue);
            }
        });

        BorderPane borderPane = new BorderPane(webView);
        
        mainWindow = stage;

        toolbar = new Toolbar(this);
        borderPane.setTop(toolbar);
        
        loadHistory();

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(webEngine.getLoadWorker()
                .progressProperty());

        webEngine.getLoadWorker().stateProperty().addListener((e, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                progressBar.setVisible(false);
            } else if (n == Worker.State.FAILED) {
                webEngine.load(MainApp.class.getResource("error.html")
                        .toExternalForm());
                progressBar.setVisible(false);
            } else if (n == Worker.State.RUNNING) {
                progressBar.setVisible(true);
            }
        });

        StackPane root = new StackPane(borderPane, progressBar);

        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle(properties.getProperty(PropertyName.APP_NAME) + " "
                + properties.getProperty(PropertyName.APP_VERSION));
        stage.setScene(scene);
        stage.show();

        loadSite(properties.getProperty(PropertyName.URL_HOME));
    }
    
    public static AppProperties getProperties() {
        return properties;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
