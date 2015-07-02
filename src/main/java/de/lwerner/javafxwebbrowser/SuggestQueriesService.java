package de.lwerner.javafxwebbrowser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Lukas Werner
 */
public class SuggestQueriesService extends Service<List<String>> {

    private final String suggestApiUrl;

    private final String query;

    public SuggestQueriesService(String query) {
        this.query = query;
        suggestApiUrl = MainApp.getProperties().getProperty(PropertyName.URL_GOOGLESUGGEST);
    }

    @Override
    protected Task<List<String>> createTask() {
        return new SuggestQueriesTask();
    }

    class SuggestQueriesTask extends Task<List<String>> {

        @Override
        protected List<String> call() throws Exception {
            URL url = new URL(String.format(suggestApiUrl, URLEncoder.encode(query, "utf-8")));
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(new BufferedReader(new InputStreamReader(url.openStream())));
            JSONArray suggestionsArray = (JSONArray) jsonArray.get(1);
            List<String> result = new LinkedList<>();
            suggestionsArray.stream().forEach((obj) -> {
                result.add(obj.toString());
            });
            return result;
        }

    }

}
