package com.application.test.Model;

public class ExamplePhrase {
    private String englishPhrase;
    private String vietnameseTranslation;

    public ExamplePhrase(String englishPhrase, String vietnameseTranslation) {
        this.englishPhrase = englishPhrase != null ? englishPhrase.trim() : "";
        this.vietnameseTranslation = vietnameseTranslation != null ? vietnameseTranslation.trim() : "";
    }

    public String getEnglish() {
        return englishPhrase;
    }

    public String getVietnamese() {
        return vietnameseTranslation;
    }

    @Override
    public String toString() {
        return "= " + englishPhrase + " : " + vietnameseTranslation;
    }
}
