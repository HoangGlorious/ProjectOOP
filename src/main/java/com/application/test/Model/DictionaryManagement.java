package com.application.test.Model;

import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.Trie;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class DictionaryManagement {
    private final Trie dictionaryTrie;
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String DATA_FILE_NAME = "dictionary_data.txt";
    public static final Path DATA_FILE_PATH = Paths.get(USER_HOME, DATA_FILE_NAME);
    public static final String DEFAULT_RESOURCE_PATH = "/dictionaries.txt";

    // Pattern để tách headword và pronunciation từ dòng @
    // Ví dụ: @ a b c - book /ˈeɪbiːˈsiːbʊk/
    // Group 1: headword (a b c - book)
    // Group 2: pronunciation (/ˈeɪbiːˈsiːbʊk/)
    private static final Pattern HEADWORD_PATTERN = Pattern.compile("^@\\s*(.*?)\\s*(/.*?/)?$");


    public DictionaryManagement() {
        this.dictionaryTrie = new Trie();
    }

    /**
     * Nạp dữ liệu từ điển từ file có định dạng phức tạp.
     * File được đọc theo encoding UTF-8.
     */
    public void loadDataFromFile() {
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
                        if (currentEntry != null && !currentEntry.getHeadword().isEmpty()) {
                            dictionaryTrie.insert(currentEntry);
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
            }

            // Lưu mục từ cuối cùng
            if (currentEntry != null && !currentEntry.getHeadword().isEmpty()) {
                dictionaryTrie.insert(currentEntry);
                loadedEntries++;
            }

            System.out.println("-> Đã nạp thành công " + loadedEntries + " mục từ từ file dữ liệu.");
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
     * Sử dụng Trie để tìm kiếm hiệu quả.
     * Kết quả trả về được sắp xếp theo alphabet.
     *
     * @param prefix Tiền tố cần tìm.
     * @return Danh sách các DictionaryEntry khớp. Trả về danh sách rỗng nếu không tìm thấy hoặc prefix rỗng.
     */
    public List<DictionaryEntry> searchEntriesByPrefix(String prefix) {
        List<DictionaryEntry> results = dictionaryTrie.searchByPrefix(prefix);

        // Sắp xếp kết quả tìm kiếm theo alphabet của headword
        Collections.sort(results, Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER));

        return results;
    }

    /**
     * Tra cứu một mục từ chính xác theo headword (không phân biệt hoa thường).
     * Sử dụng Trie để tìm kiếm.
     *
     * @param headword Từ cần tra cứu.
     * @return Optional chứa DictionaryEntry nếu tìm thấy (entry đầu tiên nếu có nhiều), Optional rỗng nếu không.
     */
    public Optional<DictionaryEntry> lookupEntry(String headword) {
        if (headword == null || headword.trim().isEmpty()) {
            return Optional.empty();
        }
        // *** THAY ĐỔI: Gọi hàm findExact của Trie ***
        List<DictionaryEntry> foundEntries = dictionaryTrie.findExact(headword.trim());

        // Trả về Optional của entry đầu tiên nếu tìm thấy
        if (!foundEntries.isEmpty()) {
            return Optional.of(foundEntries.get(0)); // Giả sử chỉ cần entry đầu tiên nếu trùng headword
        }
        return Optional.empty();
    }

    /**
     * Thêm một mục từ mới vào Trie.
     * Kiểm tra trùng lặp headword (không phân biệt hoa thường) trước khi thêm.
     *
     * @param newEntry Mục từ mới cần thêm.
     * @return true nếu thêm thành công, false nếu từ đã tồn tại hoặc đầu vào không hợp lệ.
     */
    public boolean addEntry(DictionaryEntry newEntry) {
        if (newEntry == null || newEntry.getHeadword() == null || newEntry.getHeadword().trim().isEmpty()) {
            System.err.println("Lỗi: Không thể thêm mục từ rỗng hoặc không có headword.");
            return false;
        }

        Optional<DictionaryEntry> existingEntry = lookupEntry(newEntry.getHeadword()); // Sử dụng hàm lookup đã có/sẽ sửa

        if (existingEntry.isPresent()) {
            System.err.println("Lỗi: Từ '" + newEntry.getHeadword() + "' đã tồn tại trong từ điển.");
            return false;
        }

        dictionaryTrie.insert(newEntry);
        return true;
    }

    /**
     * Lưu (ghi đè) dữ liệu từ điển hiện tại ra file dữ liệu chính bên ngoài.
     * Dữ liệu được sắp xếp trước khi ghi.
     */
    public void saveDataToFile() {
        System.out.println("\nĐang lưu dữ liệu từ điển vào file: " + DATA_FILE_PATH + "...");

        List<DictionaryEntry> entries = dictionaryTrie.getAllEntries();
        Collections.sort(entries, Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER));

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(DATA_FILE_PATH.toFile()), StandardCharsets.UTF_8))) { // Ghi vào DATA_FILE_PATH

            for (DictionaryEntry entry : entries) {
                // ... (Phần logic ghi các dòng @, *, -, = giữ nguyên như cũ) ...
                writer.write("@ " + entry.getHeadword() /* ... */);
                writer.newLine();
                for (WordSense sense : entry.getSenses()) {
                    writer.write("* " + sense.getPartOfSpeech());
                    writer.newLine();
                }
            }
            System.out.println("=> Đã lưu dữ liệu từ điển thành công vào file: " + DATA_FILE_PATH);

        } catch (IOException e) {
            System.err.println("Lỗi: Đã xảy ra sự cố khi lưu file dữ liệu: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi lưu file dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<DictionaryEntry> getAllDictionaryEntries() {
        List<DictionaryEntry> allEntries = dictionaryTrie.getAllEntries();
        Collections.sort(allEntries, Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER));
        return allEntries;
    }


    // ... các hàm khác ...
    // --- Các hàm khác cần được cập nhật để làm việc với DictionaryEntry ---
    // Ví dụ: dictionaryLookup, searcher,...
}
