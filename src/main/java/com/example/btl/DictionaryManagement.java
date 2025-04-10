package com.example.btl;

import com.example.btl.DictionaryEntry;
import com.example.btl.WordSense;
import com.example.btl.ExamplePhrase;

import java.io.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;


public class DictionaryManagement {
    private final Dictionary dictionary;
    public static final String DEFAULT_RESOURCE_PATH = "/dictionaries.txt"; // Giữ lại hoặc đổi tên file

    // Pattern để tách headword và pronunciation từ dòng @
    // Ví dụ: @ a b c - book /ˈeɪbiːˈsiːbʊk/
    // Group 1: headword (a b c - book)
    // Group 2: pronunciation (/ˈeɪbiːˈsiːbʊk/)
    private static final Pattern HEADWORD_PATTERN = Pattern.compile("^@\\s*(.*?)\\s*(/.*?/)?$");


    public DictionaryManagement(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Nạp dữ liệu từ điển từ file có định dạng phức tạp.
     * File được đọc theo encoding UTF-8.
     */
    public void insertFromFile() {
        DictionaryEntry currentEntry = null;
        WordSense currentSense = null;
        int lineNum = 0;
        int loadedEntries = 0;
        int skippedLines = 0;

        System.out.println("Đang nạp dữ liệu từ resource: " + DEFAULT_RESOURCE_PATH + "...");

        // Lấy InputStream từ classpath resource
        InputStream is = DictionaryManagement.class.getResourceAsStream(DEFAULT_RESOURCE_PATH);

        if (is == null) {
            System.err.println("Lỗi: Không tìm thấy resource '" + DEFAULT_RESOURCE_PATH + "' trong classpath.");
            System.err.println("Hãy chắc chắn file tồn tại trong 'src/main/resources' và đường dẫn là chính xác.");
            return; // Thoát nếu không tìm thấy resource
        }

        // Sử dụng try-with-resources với InputStreamReader và BufferedReader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                // --- Phần logic xử lý dòng (giữ nguyên như trước) ---
                if (line.startsWith("@")) {
                    Matcher matcher = HEADWORD_PATTERN.matcher(line);
                    if (matcher.find()) {
                        if (currentEntry != null) {
                            dictionary.addEntry(currentEntry);
                            loadedEntries++;
                        }
                        String headword = matcher.group(1).trim();
                        String pronunciation = (matcher.group(2) != null) ? matcher.group(2).trim() : "";
                        currentEntry = new DictionaryEntry(headword, pronunciation);
                        currentSense = null;
                    } else {
                        System.err.println("Cảnh báo: Bỏ qua dòng " + lineNum + " do sai định dạng @: \"" + line + "\"");
                        currentEntry = null;
                        currentSense = null;
                        skippedLines++;
                    }
                } else if (line.startsWith("*") && currentEntry != null) {
                    String partOfSpeech = line.substring(1).trim();
                    currentSense = new WordSense(partOfSpeech);
                    currentEntry.addSense(currentSense);
                } else if (line.startsWith("-") && currentSense != null) {
                    String definition = line.substring(1).trim();
                    currentSense.addDefinition(definition);
                } else if (line.startsWith("=") && currentSense != null) {
                    String exampleLine = line.substring(1).trim();
                    String[] parts = exampleLine.split("\\+", 2);
                    String eng = parts[0].trim();
                    String vie = (parts.length > 1) ? parts[1].trim() : "";
                    currentSense.addExample(new ExamplePhrase(eng, vie));
                } else if (currentEntry != null) {
                    System.err.println("Thông tin: Bỏ qua dòng " + lineNum + " không xác định định dạng: \"" + line + "\"");
                    skippedLines++;
                } else {
                    System.err.println("Thông tin: Bỏ qua dòng " + lineNum + " không thuộc mục từ nào: \"" + line + "\"");
                    skippedLines++;
                }
                // --- Kết thúc phần logic xử lý dòng ---
            }

            // Lưu mục từ cuối cùng
            if (currentEntry != null) {
                dictionary.addEntry(currentEntry);
                loadedEntries++;
            }

            System.out.println("-> Đã nạp thành công " + loadedEntries + " mục từ.");
            if (skippedLines > 0) {
                System.out.println("-> Đã bỏ qua " + skippedLines + " dòng do lỗi định dạng hoặc không xác định.");
            }

        } catch (IOException e) {
            // IOException có thể xảy ra khi đọc từ BufferedReader
            System.err.println("Lỗi: Đã xảy ra sự cố khi đọc resource: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Bắt các lỗi không mong muốn khác
            System.err.println("Lỗi không xác định khi nạp resource: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tìm kiếm các mục từ có headword bắt đầu bằng một tiền tố cho trước (không phân biệt hoa thường).
     * Kết quả trả về được sắp xếp theo alphabet.
     *
     * @param prefix Tiền tố cần tìm kiếm.
     * @return Danh sách các DictionaryEntry khớp với tiền tố. Trả về danh sách rỗng nếu không tìm thấy hoặc prefix rỗng.
     */
    public List<DictionaryEntry> searchEntriesByPrefix(String prefix) {
        List<DictionaryEntry> results = new ArrayList<>();
        if (prefix == null || prefix.trim().isEmpty()) {
            return results; // Trả về rỗng nếu không có gì để tìm
        }

        // Chuẩn hóa tiền tố tìm kiếm (xóa khoảng trắng thừa, chuyển về chữ thường)
        String searchTerm = prefix.trim().toLowerCase();

        // Lọc danh sách các entry
        results = dictionary.getAllEntries() // Lấy tất cả các entry từ Dictionary
                .stream() // Chuyển sang Stream API để xử lý
                .filter(entry -> entry.getHeadword().toLowerCase().startsWith(searchTerm)) // Lọc những entry có headword bắt đầu bằng searchTerm
                .sorted(Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER)) // Sắp xếp kết quả
                .collect(Collectors.toList()); // Thu thập kết quả thành List

        return results;
    }

    /**
     * Tra cứu một mục từ chính xác theo headword (không phân biệt hoa thường).
     *
     * @param headword Từ cần tra cứu.
     * @return Optional chứa DictionaryEntry nếu tìm thấy, Optional rỗng nếu không.
     */
    public Optional<DictionaryEntry> lookupEntry(String headword) {
        return dictionary.findEntry(headword); // Gọi hàm đã có trong Dictionary
    }

    // ... các hàm khác ...
    // --- Các hàm khác cần được cập nhật để làm việc với DictionaryEntry ---
    // Ví dụ: dictionaryLookup, searcher,...
}
