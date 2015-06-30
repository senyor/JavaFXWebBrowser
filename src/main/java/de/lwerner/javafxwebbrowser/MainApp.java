package de.lwerner.javafxwebbrowser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MainApp extends Application {
    
    private static AppProperties properties;
    
    private WebView webView;
    private WebEngine webEngine;
    
    private StringProperty urlInput;
    private StringProperty searchInput;
    
    private void loadSite(String urlAsString) {
        if (!urlAsString.contains("://")) {
            urlAsString = "http://" + urlAsString;
        }
        webEngine.load(urlAsString);
    }
    
    private void loadCurrentInput() {
        loadSite(urlInput.get());
    }
    
    private void search(String query) throws UnsupportedEncodingException {
        String urlAsString = properties.getProperty(PropertyName.URL_SEARCH);
        urlAsString += URLEncoder.encode(query, "UTF-8");
        loadSite(urlAsString);
    }
    
    private void search() throws UnsupportedEncodingException {
        search(searchInput.get());
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(getClass().getResource("fontawesome-webfont.ttf").toExternalForm(), 12);
        
        webView = new WebView();
        webEngine = webView.getEngine();
        
        loadSite(properties.getProperty(PropertyName.URL_HOME));
        
        BorderPane borderPane = new BorderPane(webView);
        
        HBox topBar = new HBox(5);
        topBar.setPadding(new Insets(5));
        
        Button btnHome = new Button("\uf015");
        btnHome.setFont(Font.font("FontAwesome", 14));
        btnHome.setOnAction(e -> {
            loadSite(properties.getProperty(PropertyName.URL_HOME));
        });
        
        TextField tfUrl = new TextField();
        tfUrl.setOnKeyReleased((KeyEvent e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                loadCurrentInput();
            }
        });
        tfUrl.setMinWidth(300);
        tfUrl.setMaxWidth(500);
        
        urlInput = new SimpleStringProperty();
        urlInput.bind(tfUrl.textProperty());
        
        Button btnGo = new Button("\uf061");
        btnGo.setFont(Font.font("FontAwesome", 14));
        btnGo.setOnAction(e -> {
            loadCurrentInput();
        });
        
        Label lbSearch = new Label("Search:");
        
        TextField tfSearch = new TextField();
        tfSearch.setOnKeyReleased((KeyEvent e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    search();
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        searchInput = new SimpleStringProperty();
        searchInput.bind(tfSearch.textProperty());
        
        Button btnSearch = new Button("\uf002");
        btnSearch.setFont(Font.font("FontAwesome", 14));
        btnSearch.setOnAction(e -> {
            try {
                search();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        topBar.getChildren().addAll(btnHome, tfUrl, btnGo, lbSearch, tfSearch, btnSearch);
             
        borderPane.setTop(topBar);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
        
        webEngine.getLoadWorker().stateProperty().addListener((e, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                progressBar.setVisible(false);
            } else if (n == Worker.State.FAILED) {
                webEngine.load(MainApp.class.getResource("error.html").toExternalForm());
                progressBar.setVisible(false);
            } else if (n == Worker.State.RUNNING) {
                progressBar.setVisible(true);
            }
        });
        
        StackPane root = new StackPane(borderPane, progressBar);
        
        Scene scene = new Scene(root, 800, 500);
        
        stage.setTitle(properties.getProperty(PropertyName.APP_NAME));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        properties = AppProperties.getInstance();
        try {
            properties.readAppProperties();
            launch(args);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
