package com.application.test.Model;


import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;



import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Thesaurus {

    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public static ThesaurusResult lookup(String word) throws Exception {
        String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);
        String finalURL = API_URL + encodedWord;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(finalURL))
                .GET().build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() != 200) {
            throw new RuntimeException("API request failed with status code " + response.statusCode());
        }

        return parseResponse(response.body());
    }

    private static ThesaurusResult parseResponse(String json) {
        ThesaurusResult result = new ThesaurusResult();
        JsonArray entries = new JsonArray();
        entries.add(json);

        if(entries.isEmpty()) {
            return result;
        }

        JsonObject firstEntry = entries.get(0).getAsJsonObject();
        result.setWord(firstEntry.get("word").getAsString());

        JsonArray meanings = firstEntry.getAsJsonArray("meanings");

        for(int i = 0; i<meanings.size(); i++) {
            JsonObject meaning = meanings.get(i).getAsJsonObject();
            JsonArray definitions = meaning.getAsJsonArray("definitions");

            for(int j=0; j<definitions.size(); j++) {
                JsonObject definition = definitions.get(j).getAsJsonObject();
            }

            for(int j=0; j< definitions.size(); j++) {
                JsonObject definition = definitions.get(j).getAsJsonObject();
                if(definition.has("synonyms")) {
                    JsonArray synonyms = definition.getAsJsonArray("synonyms");
                    result.addSynonyms(synonyms.get(0).getAsString());
                }
                if(definition.has("antonyms")) {
                    JsonArray antonyms = definition.getAsJsonArray("antonyms");
                    result.addAntonyms(antonyms.get(0).getAsString());
                }
            }
        }
        return result;
    }
}
