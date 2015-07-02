package de.lwerner.javafxwebbrowser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class MainApp extends Application {

    private static AppProperties properties;

    private WebView webView;
    private WebEngine webEngine;

    private ComboBox<String> cbUrl;

    private List<String> history;

    private StringProperty searchInput;
    
    private final File historyFile = new File(System.getProperty("user.dir") + "/" + "history.txt");

    private void loadSite(String urlAsString) {
        if (urlAsString == null || urlAsString.isEmpty()) {
            return;
        }
        if (!urlAsString.contains("://")) {
            urlAsString = "http://" + urlAsString;
        }
        if (!cbUrl.getItems().contains(urlAsString)) {
            cbUrl.getItems().add(0, urlAsString);
        }
        properties.setProperty(PropertyName.URL_LAST, urlAsString);
        webEngine.load(urlAsString);
        cbUrl.setValue(urlAsString);
    }

    private void loadCurrentInput() {
        String url = cbUrl.getValue();
        // Have to use that due to double loading sites, but it does not work yet
        // cbUrl.getSelectionModel().clearSelection(); 
        loadSite(url);
    }

    private void search(String query) throws UnsupportedEncodingException {
        String urlAsString = properties.getProperty(PropertyName.URL_SEARCH);
        urlAsString += URLEncoder.encode(query, "UTF-8");
        loadSite(urlAsString);
    }

    private void search() throws UnsupportedEncodingException {
        search(searchInput.get());
    }
    
    private void writeHistory() throws IOException {
        Files.write(historyFile.toPath(), history, Charset.defaultCharset());
    }
    
    private void loadHistory() throws IOException {
        List<String> tempList = Files.readAllLines(historyFile.toPath());
        tempList.stream().forEach((s) -> {
            cbUrl.getItems().add(s);
        });
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

        BorderPane borderPane = new BorderPane(webView);

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(5));

        BorderPane topRight = new BorderPane();

        Button btnHome = new Button("\uf015");
        btnHome.setFont(Font.font("FontAwesome", 14));
        btnHome.setOnAction(e -> {
            loadSite(properties.getProperty(PropertyName.URL_HOME));
        });

        cbUrl = new ComboBox<>();
        cbUrl.setMaxWidth(Double.MAX_VALUE);
        cbUrl.setEditable(true);
        cbUrl.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                loadCurrentInput();
            }

        });

        cbUrl.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loadCurrentInput();
            }
        });

        history = new LinkedList<>();
        Bindings.bindContent(history, cbUrl.getItems());
        try {
            loadHistory();
        } catch (IOException ex) { }

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
                    Logger.getLogger(MainApp.class.getName())
                            .log(Level.SEVERE, null, ex);
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
                Logger.getLogger(MainApp.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        });

        topRight.setLeft(btnGo);
        topRight.setCenter(tfSearch);
        topRight.setRight(btnSearch);

        topBar.setLeft(btnHome);
        topBar.setCenter(cbUrl);
        topBar.setRight(topRight);

        borderPane.setTop(topBar);

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

        stage.setTitle(properties.getProperty(PropertyName.APP_NAME) + " "
                + properties.getProperty(PropertyName.APP_VERSION));
        stage.setScene(scene);
        stage.show();

        loadSite(properties.getProperty(PropertyName.URL_HOME));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
