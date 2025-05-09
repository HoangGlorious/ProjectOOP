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
    // Setup URL cho Free Dictionary API và setup HttpClient
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // Hàm tìm Thesaurus cho 1 từ
    public static ThesaurusResult lookup(String word) throws Exception {

        try {
            // Encode từ và tạo URL
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);
            String finalURL = API_URL + encodedWord;

            // Tạo HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(finalURL))
                    .GET().build();

            // Lấy phản hồi cho request gửi bằng HttpClient
            HttpResponse<String> response = httpClient
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Xử lý trường hợp request không thành công
            if (response.statusCode() != 200) {
                return new ThesaurusResult("", List.of(), List.of(),
                        "API request failed with status code " + response.statusCode());
            }

            // Trả về phản hồi đã được phân tích
            return parseResponse(response.body());
        } catch (Exception e) {
            return new ThesaurusResult("", List.of(), List.of(), "Error: " + e.getMessage());
        }
    }

    // Hàm phân tích phản hồi từ Free Dictionary API thành từ gốc, từ đồng nghĩa và từ trái nghĩa
    private static ThesaurusResult parseResponse(String json) {
        try {
            // Đọc dữ liệu Json, trả về JsonArray
            JsonArray entries = com.google.gson.JsonParser.parseString(json).getAsJsonArray();

            // Trả về thông báo nếu không có từ đồng nghĩa hay trái nghĩa
            if (entries.isEmpty()) {
                return new ThesaurusResult("", List.of(), List.of(), "No results found");
            }

            // Đọc từ gốc và khởi tạo danh sách từ đồng nghĩa và trái nghĩa
            JsonObject firstEntry = entries.get(0).getAsJsonObject();
            String word = firstEntry.getAsJsonPrimitive("word").getAsString();
            List<String> synonym = new ArrayList<>();
            List<String> antonym = new ArrayList<>();

            // Skim qua các nghĩa của từ
            JsonArray meanings = firstEntry.getAsJsonArray("meanings");

            // Mỗi nghĩa bao gồm định nghĩa, mảng từ đồng nghĩa và mảng từ trái nghĩa
            for (int i = 0; i < meanings.size(); i++) {
                JsonObject meaning = meanings.get(i).getAsJsonObject();
                JsonArray definitions = meaning.getAsJsonArray("definitions");

                // Scan các nghĩa để tìm danh sách từ đồng nghĩa và danh sách từ trái nghĩa
                for (int j = 0; j < definitions.size(); j++) {
                    JsonObject definition = definitions.get(j).getAsJsonObject();
                    addAllStrings(definition, "synonyms", synonym);
                    addAllStrings(definition, "antonyms", antonym);
                }

                // Thêm các từ đồng nghĩa và trái nghĩa của phần meaning (Do format của Free Dictionary API)
                addAllStrings(meaning, "synonyms", synonym);
                addAllStrings(meaning, "antonyms", antonym);
            }
            return new ThesaurusResult(word, synonym, antonym, "");
        } catch (Exception e) {
            return new ThesaurusResult("", List.of(), List.of(), "Parsing response failed: " + e.getMessage());
        }
    }

    // Hàm phụ để đọc danh sách từ Json, loại bỏ rỗng và null
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
