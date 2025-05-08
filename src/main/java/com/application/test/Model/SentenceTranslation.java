package com.application.test.Model;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SentenceTranslation {
    private final String GAPI_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t";

    public String senTranslate(String sentence, String sourceLang, String targetLang) throws Exception {
        if (sentence == null || sentence.trim().isEmpty()) {
            return "";
        }

        String encodedSentence = java.net.URLEncoder.encode(sentence, "UTF-8");
        String urlString = String.format("%s&sl=%s&tl=%s&q=%s",
                GAPI_URL, sourceLang, targetLang, encodedSentence);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + connection.getResponseCode());
        }

        try (BufferedReader br = new BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }

            JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
            // Navigate through nested arrays to get the translated text
            String translatedText = jsonArray
                    .get(0).getAsJsonArray()
                    .get(0).getAsJsonArray()
                    .get(0).getAsString();

            return translatedText;
        }
    }
}
