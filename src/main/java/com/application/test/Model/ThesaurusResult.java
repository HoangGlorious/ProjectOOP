package com.application.test.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ThesaurusResult {
    private String word;
    private List<String> synonyms = new ArrayList<>();
    private List<String> antonyms = new ArrayList<>();
    private String error;

    public ThesaurusResult(String word, List<String> synonyms, List<String> antonyms, String error) {
        this.word = word;
        this.synonyms = List.copyOf(synonyms);
        this.antonyms = List.copyOf(antonyms);
        this.error = error;
    }

    // Getters and setters
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getSynonyms() {
        return synonyms.stream().distinct().collect(Collectors.toList());
    }

    public List<String> getAntonyms() {
        return antonyms.stream().distinct().collect(Collectors.toList());
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }
}