package de.lwerner.javafxwebbrowser;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

/**
 *
 * @author Lukas Werner
 */
public class Toolbar extends GridPane {
    
    private static final AppProperties properties = MainApp.getProperties();
    
    private final MainApp context;

    private Button btPrevious;
    private Button btNext;
    
    private ComboBox<String> cbAddress;
    
    private Button btGo;
    
    private ComboBox<String> cbSearch;
    
    private Button btSearch;
    
    private Button btHome;
    private Button btOptions;
    
    private ContextMenu cmOptions;
    
    public Toolbar(MainApp context) {
        this.context = context;
        
        setHgap(5);
        setPadding(new Insets(6, 8, 6, 8));
        
        initialize();
        eventHandling();
        build();
    }
    
    private void initialize() {
        btPrevious = new Button("\uf060");
        btPrevious.setFont(Font.font("FontAwesome", 14));
        btPrevious.setDisable(true);
        btNext = new Button("\uf061");
        btNext.setFont(Font.font("FontAwesome", 14));
        btNext.setDisable(true);
        
        cbAddress = new ComboBox<>();
        
        btGo = new Button("\uf061");
        btGo.setFont(Font.font("FontAwesome", 14));
        
        cbSearch = new ComboBox<>();
        
        btSearch = new Button("\uf002");
        btSearch.setFont(Font.font("FontAwesome", 14));
        
        btHome = new Button("\uf015");
        btHome.setFont(Font.font("FontAwesome", 14));
        btOptions = new Button("\uf0c9");
        btOptions.setFont(Font.font("FontAwesome", 14));
        
        cmOptions = new ContextMenu();
        MenuItem itemUpdateHome = new MenuItem("Update home");
        itemUpdateHome.setOnAction(e -> {
            context.updateHome();
        });
        cmOptions.getItems().add(itemUpdateHome);
    }
    
    private void eventHandling() {
        btNext.setOnAction(e -> {
            context.next();
        });
        
        btPrevious.setOnAction(e -> {
            context.previous();
        });
        
        cbAddress = new ComboBox<>();
        cbAddress.setMaxWidth(Double.MAX_VALUE);
        cbAddress.setEditable(true);
        
        cbAddress.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                context.loadSite(cbAddress.getValue());
            }
        });
        
        btGo.setOnAction(e -> {
            context.loadSite(cbAddress.getValue());
        });
        
        cbSearch.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                try {
                    context.search(cbSearch.getValue());
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        cbSearch.getEditor().addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    context.search(cbSearch.getValue());
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (cbSearch.getEditor().getText() != null && !cbSearch.getEditor().getText().isEmpty()) {
                    cbSearch.getItems().clear();
                    SuggestQueriesService suggestQueriesService = new SuggestQueriesService(cbSearch.getEditor().getText());
                    suggestQueriesService.setOnSucceeded((WorkerStateEvent event1) -> {
                        List<String> querySuggestions = suggestQueriesService.getValue();
                        querySuggestions.stream().forEach((s) -> {
                            cbSearch.getItems().add(s);
                        });
                    });
                    suggestQueriesService.start();
                    cbSearch.show();
                } else {
                    cbSearch.hide();
                }
            }
        });
        
        btSearch.setOnAction(e -> {
            try {
                context.search(cbSearch.getValue());
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Toolbar.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        btHome.setOnAction(e -> {
            context.loadHome();
        });

        btOptions.setOnAction(e -> {
            // TODO: Style this a bit better
            cmOptions.show(context.getWindow());
        });
    }
    
    private void build() {
        btPrevious.setShape(new Circle(1));
        add(btPrevious, 0, 0);
        
        btNext.setShape(new Circle(1));
        add(btNext, 1, 0);
        
        cbAddress.setMaxWidth(Double.MAX_VALUE);
        cbAddress.setEditable(true);
        add(cbAddress, 2, 0);
        
        btGo.getStyleClass().add("transparent");
        add(btGo, 3, 0);
        
        cbSearch.setEditable(true);
        add(cbSearch, 4, 0);
        
        btSearch.getStyleClass().add("transparent");
        add(btSearch, 5, 0);
        
        btHome.getStyleClass().add("transparent");
        add(btHome, 6, 0);
        
        btOptions.getStyleClass().add("transparent");
        add(btOptions, 7, 0);
        
        GridPane.setHgrow(cbAddress, Priority.ALWAYS);
        GridPane.setHgrow(cbSearch, Priority.SOMETIMES);
    }

    public synchronized void setCurrentUrl(String location) {
        // TODO: Check for possibilities to autoload site on click in history
        if (!cbAddress.getItems().contains(location)) {
            cbAddress.getItems().add(0, location);
            if (cbAddress.getItems().size() > 
                    Integer.valueOf(properties.getProperty(PropertyName.APP_HISTORYSIZE))) {
                cbAddress.getItems().remove(cbAddress.getItems().size() - 1);
            }
        }
        cbAddress.getEditor().setText(location);
        btNext.setDisable(!context.hasNext());
        btPrevious.setDisable(!context.hasPrevious());
    }
    
    public List<String> getHistory() {
        return cbAddress.getItems();
    }
    
    public void setHistory(List<String> history) {
        cbAddress.getItems().clear();
        cbAddress.getItems().addAll(history);
    }
    
}