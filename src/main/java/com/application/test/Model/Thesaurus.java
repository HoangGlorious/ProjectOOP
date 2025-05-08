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
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static ThesaurusResult lookup(String word) throws Exception {

        try {
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);
            String finalURL = API_URL + encodedWord;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(finalURL))
                    .GET().build();

            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return new ThesaurusResult("", List.of(), List.of(),
                        "API request failed with status code " + response.statusCode());
            }
            return parseResponse(response.body());
        } catch (Exception e) {
            return new ThesaurusResult("", List.of(), List.of(), "Error: " + e.getMessage());
        }
    }

    private static ThesaurusResult parseResponse(String json) {
        try {
            JsonArray entries = new JsonArray();
            entries.add(json);

            if (entries.isEmpty()) {
                return new ThesaurusResult("", List.of(), List.of(), "No results found");
            }

            JsonObject firstEntry = entries.getAsJsonObject();
            String word = firstEntry.getAsJsonPrimitive("word").getAsString();
            List<String> synonym = new ArrayList<>();
            List<String> antonym = new ArrayList<>();

            JsonArray meanings = firstEntry.getAsJsonArray("meanings");

            for (int i = 0; i < meanings.size(); i++) {
                JsonObject meaning = meanings.get(i).getAsJsonObject();
                JsonArray definitions = meaning.getAsJsonArray("definitions");


                for (int j = 0; j < definitions.size(); j++) {
                    JsonObject definition = definitions.get(j).getAsJsonObject();
                    addAllStrings(definition, "synonyms", synonym);
                    addAllStrings(definition, "antonyms", antonym);
                }

                addAllStrings(meaning, "synonyms", synonym);
                addAllStrings(meaning, "antonyms", antonym);
            }
            return new ThesaurusResult(word, synonym, antonym, "");
        } catch (Exception e) {
            return new ThesaurusResult("", List.of(), List.of(), "Parsing response failed: " + e.getMessage());
        }
    }

    private static void addAllStrings(JsonObject obj, String key, List<String> list) {
        if (obj.has(key)) {
            JsonArray items = obj.getAsJsonArray(key);
            for (int i = 0; i < items.size(); i++) {
                String item = items.get(i).getAsString();
                if (item != null && !item.trim().isEmpty()) {
                    list.add(item.trim());
                }
            }
        }
    }
}
