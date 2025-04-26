package com.application.test.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dictionary {
    // Thay đổi kiểu dữ liệu của danh sách
    private final List<DictionaryEntry> entries;

    public Dictionary() {
        this.entries = new ArrayList<>();
    }

    // Thay đổi kiểu tham số và tên phương thức cho rõ ràng
    public void addEntry(DictionaryEntry entry) {
        if (entry != null) {
            this.entries.add(entry);
        }
    }

    // Trả về danh sách các DictionaryEntry
    public List<DictionaryEntry> getAllEntries() {
        return this.entries;
    }

    // Có thể thêm hàm tìm kiếm entry theo headword ở đây
    public Optional<DictionaryEntry> findEntry(String headword) {
        if (headword == null || headword.trim().isEmpty()) {
            return Optional.empty();
        }
        String searchTerm = headword.trim();
        return entries.stream()
                .filter(entry -> entry.getHeadword().equalsIgnoreCase(searchTerm))
                .findFirst();
    }

    public int getNumberOfEntries() {
        return this.entries.size();
    }
}