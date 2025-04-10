package com.example.btl;

public class ExamplePhrase {
    private String englishPhrase;
    private String vietnameseTranslation;

    public ExamplePhrase(String englishPhrase, String vietnameseTranslation) {
        this.englishPhrase = englishPhrase != null ? englishPhrase.trim() : "";
        this.vietnameseTranslation = vietnameseTranslation != null ? vietnameseTranslation.trim() : "";
    }

    public String getEnglishPhrase() {
        return englishPhrase;
    }

    public String getVietnameseTranslation() {
        return vietnameseTranslation;
    }

    @Override
    public String toString() {
        // Định dạng hiển thị cơ bản
        return "= " + englishPhrase + " : " + vietnameseTranslation;
    }
}
