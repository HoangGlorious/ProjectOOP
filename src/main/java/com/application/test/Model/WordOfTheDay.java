package com.application.test.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class WordOfTheDay {
    private List<String> headwords;
    private LocalDate lastUpdate;
    private String todayWord;
    private final GeneralManagement wotdManagement;

    // Constructor
    public WordOfTheDay(GeneralManagement wotdManagement) {
        // wotdManagement được yêu cầu là phải khác null để load WOTD
        this.wotdManagement = Objects.requireNonNull(wotdManagement, "Management cannot be null");
        loadWords();
        updateWOTD();
    }

    // Hàm getTodayWord
    public String getTodayWord() {
        // Cập nhật WOTD nếu null
        if (todayWord == null) {
            updateWOTD();
        }
        return todayWord;
    }

    public GeneralManagement getWotdManagement() {
        return wotdManagement;
    }

    public List<String> getHeadwords() {
        return new ArrayList<>(headwords);
    }


    // Hàm loadWords
    public void loadWords() {
        try {
            // Trả về lỗi nếu wotdManagement null
            if (wotdManagement == null) {
                throw new IllegalStateException("Management is not initialized");
            }


            DictionarySource source = wotdManagement.getActiveSource();
            if (source == null) {
                throw new IllegalStateException("No active dictionary source available");
            }

            // Load từ tiếng anh từ các entry trong file .txt
            this.headwords = source.getAllEntries().stream()
                    .map(DictionaryEntry::getHeadword)
                    .filter(Objects::nonNull)
                    .filter(h -> !h.isEmpty())
                    .collect(Collectors.toList());

            if (headwords.isEmpty()) {
                throw new IllegalStateException("No words available in the dictionary");
            }

            System.out.println("Successfully loaded " + headwords.size() + " words");
        } catch (Exception e) {
            // Thông báo nếu lỗi khi load
            throw new IllegalStateException("Failed to load words: " + e.getMessage(), e);
        }
    }

    // Hàm cập nhật WOTD
    public void updateWOTD() {

        // Trả về null nếu danh sách từ trống
        if (headwords == null || headwords.isEmpty()) {
            this.todayWord = null;
            return;
        }

        // Cập nhật từ theo ngày
        LocalDate today = LocalDate.now();
        if(!today.equals(lastUpdate) || todayWord == null) {
            long seed = today.toEpochDay();
            Random random = new Random(seed);
            lastUpdate = today;
            todayWord = headwords.get(random.nextInt(headwords.size()));
        }
    }
}
