package com.application.test.Model;


import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WordOfTheDay {
    private List<String> headwords;
    private LocalDate lastUpdate;
    private String todayWord;

    private GeneralManagement wotdManagement;

    public void setWotdManagement(GeneralManagement wotdManagement) {
        this.wotdManagement = wotdManagement;
    }


    public WordOfTheDay() {
    }

    public String getTodayWord() {
        return todayWord;
    }

    public void loadWords() {
        DictionarySource activeSource = wotdManagement.getActiveSource();
        List<DictionaryEntry> allEntries = activeSource.getAllEntries();

        headwords = allEntries.stream()
                .map(DictionaryEntry::getHeadword)
                .collect(Collectors.toList());
    }

    public void updateWOTD() {
        LocalDate today = LocalDate.now();
        if(!today.equals(lastUpdate) || todayWord == null) {
            long seed = today.toEpochDay();
            Random random = new Random(seed);
            lastUpdate = today;
            todayWord = headwords.get(random.nextInt(headwords.size()));
        }
    }
}
