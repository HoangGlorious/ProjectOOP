package com.application.test.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ThesaurusResult {
    private String word;
    private List<String> synonyms = new ArrayList<>();
    private List<String> antonyms = new ArrayList<>();
    private String error;

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

    public void addSynonym(String synonym) {
        if (synonym != null && !synonym.isEmpty()) {
            this.synonyms.add(synonym.toLowerCase());
        }
    }

    public List<String> getAntonyms() {
        return antonyms.stream().distinct().collect(Collectors.toList());
    }

    public void addAntonym(String antonym) {
        if (antonym != null && !antonym.isEmpty()) {
            this.antonyms.add(antonym.toLowerCase());
        }
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