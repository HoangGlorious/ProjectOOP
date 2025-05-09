package com.application.test.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

// Lớp WordOfTheDay gồm
// headwords: danh sách các từ tiếng anh trong file dictionaries.txt
// lastUpdate: lần cuối cập nhật WOTD, phục vụ cho việc quyết định cập nhật WOTD không
// todayWord: WOTD đẻ tránh lặp từ và để display
// wotdManagement: đối tượng quản lý từ điển (dùng để tạo headwords)
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
        // Cập nhật WOTD nếu null (lần đầu dùng app)
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

            // Lấy DictionarySource thông qua wotdManagement.getActiveSource()
            DictionarySource source = wotdManagement.getActiveSource();
            if (source == null) {
                throw new IllegalStateException("No active dictionary source available");
            }

            // Lọc toàn bộ các từ tiếng anh từ các entry trong file dictionaries.txt
            this.headwords = source.getAllEntries().stream()
                    .map(DictionaryEntry::getHeadword)
                    .filter(Objects::nonNull)
                    .filter(h -> !h.isEmpty())
                    .collect(Collectors.toList());

            // Xử lí trường hợp danh sách trống
            if (headwords.isEmpty()) {
                throw new IllegalStateException("No words available in the dictionary");
            }

            // Callback
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
        // Điều kiện cập nhật: Lần cuối cập nhật khác hôm nay hoặc lần đầu dùng app
        if(!today.equals(lastUpdate) || todayWord == null) {
            // Khởi tạo Random bằng seed này để không đổi từ trong cùng 1 ngày
            long seed = today.toEpochDay();
            // Tạo một đối tượng Random, dùng để sinh số ngẫu nhiên.
            // Seed được cố định theo ngày, nên random.nextInt(...) luôn cho ra cùng một kết quả nếu gọi lại trong cùng một ngày.
            Random random = new Random(seed);
            lastUpdate = today;
            // Chọn 1 chỉ số ngẫu nhiên trong danh sách để gán todayWord
            todayWord = headwords.get(random.nextInt(headwords.size()));
        }
    }
}
