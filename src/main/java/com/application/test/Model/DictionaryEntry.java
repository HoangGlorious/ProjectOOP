package com.application.test.Model;
import java.util.ArrayList;
import java.util.List;

public class DictionaryEntry {
    private String headword; // Từ gốc (sau @, trước /)
    private String pronunciation; // Phiên âm (trong /.../)
    private List<WordSense> senses; // Danh sách các nghĩa/cách dùng

    public DictionaryEntry(String headword, String pronunciation) {
        this.headword = headword != null ? headword.trim() : "";
        this.pronunciation = pronunciation != null ? pronunciation.trim() : "";
        this.senses = new ArrayList<>();
    }

    public String getHeadword() {
        return headword;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public List<WordSense> getSenses() {
        return senses;
    }

    public void addSense(WordSense sense) {
        if (sense != null) {
            this.senses.add(sense);
        }
    }

    // Cần thiết để ListView/ComboBox hiển thị headword
    @Override
    public String toString() {
        return headword;
    }

    // Phương thức để lấy toàn bộ thông tin dưới dạng chuỗi định dạng đẹp
    public String getFormattedExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("@ ").append(headword);
        if (pronunciation != null && !pronunciation.isEmpty()) {
            sb.append(" ").append(pronunciation);
        }
        sb.append("\n");

        for (WordSense sense : senses) {
            // Thêm thụt lề cho các sense
            String[] senseLines = sense.toString().split("\n");
            for(String line : senseLines) {
                sb.append("  ").append(line).append("\n");
            }
        }
        return sb.toString().trim(); // Xóa dòng trống cuối cùng nếu có
    }
}
