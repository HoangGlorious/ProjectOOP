
package com.application.test.Model;


import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SentenceTranslation {
    // Setup Google Translate API URL
    private final String GAPI_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t";

    // Hàm dịch câu dùng Google Translate API
    public String senTranslate(String sentence, String sourceLang, String targetLang) throws Exception {
        if (sentence == null || sentence.trim().isEmpty()) {
            return "";
        }

        // Encode câu cần dịch và dựng URL
        String encodedSentence = java.net.URLEncoder.encode(sentence, "UTF-8");
        String urlString = String.format("%s&sl=%s&tl=%s&q=%s",
                GAPI_URL, sourceLang, targetLang, encodedSentence);

        // Tạo HTTP request
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Xử lý trường hợp dịch không thành công
        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + connection.getResponseCode());
        }

        // Đọc và xử lý phản hồi của Google Translate API
        // Dùng BufferedReader để đọc connnection.getInputStream() chứa phản hồi của Google Translate API
        // Dùng StringBuilder để phân tích phản hồi trong khi đang đọc từng dòng 1 cách hệu quả
        // Dùng vòng lặp while để đọc từng dòng và thêm dữ liệu cần thiết vào StringBuilder cho đến khi mọi dữ liệu của phản hồi đã được xử lý
        try (BufferedReader br = new BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            // Phân tích phản hồi thành JsonArray bằng JsonParser
            JsonArray jsonArray = JsonParser.parseString(response.toString()).getAsJsonArray();
            // Đi qua JsonArray và xuất ra câu đã dịch
            String translatedText = jsonArray
                    .get(0).getAsJsonArray()
                    .get(0).getAsJsonArray()
                    .get(0).getAsString();

            return translatedText;
        }
    }
}
