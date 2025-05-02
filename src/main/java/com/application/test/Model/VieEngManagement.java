package com.application.test.Model;
import com.application.test.Model.Trie;
import com.application.test.Model.WordSense;
import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.ExamplePhrase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VieEngManagement implements DictionarySource {
    private Trie dictionaryTrie;
    // Thông tin nguồn
    private static final String SOURCE_ID = "vi-en";
    private static final String DISPLAY_NAME = "Việt - Anh";

    // Đường dẫn file text gốc
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String DATA_FILE_NAME = "vietanh_data.txt"; // Tên file data cho Việt-Anh
    private static final Path DATA_FILE_PATH = Paths.get(USER_HOME, DATA_FILE_NAME);
    // Resource mặc định
    private static final String DEFAULT_RESOURCE_PATH = "/vietanh.txt"; // Tên file resource Việt-Anh

    private static final Pattern HEADWORD_PATTERN = Pattern.compile("^@\\s*(.*?)\\s*(/.*?/)?$");

    public VieEngManagement() {
        this.dictionaryTrie = new Trie();
    }

    @Override
    public String getSourceId() {
        return SOURCE_ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /** Helper function for the main data import function. */
    private void loadDataFromStream(InputStream is, String sourceName) {
        DictionaryEntry currentEntry = null;
        WordSense currentSense = null;
        int lineNum = 0;
        int loadedEntries = 0;
        int skippedLines = 0;

        System.out.println("Đang nạp dữ liệu từ: " + sourceName + "...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                if (line.isEmpty()) continue;

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

            if (currentEntry != null && !currentEntry.getHeadword().isEmpty()) {
                dictionaryTrie.insert(currentEntry);
                loadedEntries++;
            }

            System.out.println("-> Đã nạp thành công " + loadedEntries + " mục từ từ " + sourceName + ".");
            if (skippedLines > 0) {
                System.out.println("-> Đã bỏ qua " + skippedLines + " dòng do lỗi định dạng hoặc không xác định.");
            }

        } catch (IOException e) {
            System.err.println("Lỗi: Đã xảy ra sự cố khi đọc " + sourceName + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi nạp " + sourceName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Nạp dữ liệu từ điển từ file có định dạng phức tạp.
     * File được đọc theo encoding UTF-8.
     */
    public void loadData() {
        InputStream resourceStream = EngVieManagement.class.getResourceAsStream(DEFAULT_RESOURCE_PATH);
        if (resourceStream != null) {
            loadDataFromStream(resourceStream, "resource: " + DEFAULT_RESOURCE_PATH);
        } else {
            System.err.println("Lỗi: Không tìm thấy resource '" + DEFAULT_RESOURCE_PATH + "' trong classpath.");
        }

        // Load from user data file if exists
        if (Files.exists(DATA_FILE_PATH)) {
            try (InputStream userFileStream = Files.newInputStream(DATA_FILE_PATH)) {
                loadDataFromStream(userFileStream, "user data file: " + DATA_FILE_PATH);
            } catch (IOException e) {
                System.err.println("Lỗi: Không thể mở file dữ liệu người dùng: " + e.getMessage());
                e.printStackTrace();
            }
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
     * Cập nhật một mục từ trong từ điển.
     * Xử lý hai trường hợp: thay đổi headword và chỉ thay đổi nội dung.
     *
     * @param oldHeadword Headword gốc của từ cần sửa.
     * @param updatedEntry DictionaryEntry mới chứa nội dung cập nhật.
     * @return true nếu cập nhật thành công, false nếu không tìm thấy từ gốc hoặc từ mới đã tồn tại (khi thay đổi headword).
     */
    public boolean updateEntry(String oldHeadword, DictionaryEntry updatedEntry) {
        if (oldHeadword == null || oldHeadword.trim().isEmpty() || updatedEntry == null || updatedEntry.getHeadword().trim().isEmpty()) {
            System.err.println("Lỗi: Thông tin cập nhật không hợp lệ.");
            return false;
        }
        String oldWord = oldHeadword.trim();
        String newWord = updatedEntry.getHeadword().trim();

        // 1. Tìm entry cũ để đảm bảo nó tồn tại
        Optional<DictionaryEntry> existingOldEntry = lookupEntry(oldWord);
        if (!existingOldEntry.isPresent()) {
            System.err.println("Lỗi: Không tìm thấy từ '" + oldWord + "' để cập nhật.");
            return false;
        }

        // 2. Kiểm tra xem headword có thay đổi không
        if (!oldWord.equalsIgnoreCase(newWord)) {
            // Case 1: Headword ĐANG thay đổi
            System.out.println("Cập nhật: Thay đổi headword từ '" + oldWord + "' sang '" + newWord + "'.");

            // Kiểm tra xem headword MỚI đã tồn tại chưa
            Optional<DictionaryEntry> existingNewEntry = lookupEntry(newWord);
            if (existingNewEntry.isPresent()) {
                System.err.println("Lỗi: Từ mới '" + newWord + "' đã tồn tại. Không thể cập nhật thành từ này.");
                return false;
            }

            // Xóa entry cũ khỏi Trie dựa trên headword cũ
            boolean removed = dictionaryTrie.remove(oldWord);
            if (!removed) {
                // Trường hợp này hiếm xảy ra nếu lookupEntry đã tìm thấy từ đó
                System.err.println("Lỗi nội bộ: Không thể xóa entry cũ '" + oldWord + "' từ Trie.");
                return false;
            }

            // Chèn entry mới (với headword mới) vào Trie
            dictionaryTrie.insert(updatedEntry);
            System.out.println("=> Đã cập nhật headword và nội dung thành công.");
            return true;

        } else {
            // Case 2: Headword KHÔNG thay đổi, chỉ sửa nội dung
            System.out.println("Cập nhật: Chỉ sửa nội dung cho từ '" + oldWord + "'.");

            // Thay thế nội dung entry trong Trie
            boolean replaced = dictionaryTrie.replaceEntry(oldWord, updatedEntry); // Sử dụng phương thức replaceEntry của Trie
            if (!replaced) {
                // Trường hợp này cũng hiếm xảy ra nếu lookupEntry đã tìm thấy từ đó
                System.err.println("Lỗi nội bộ: Không thể thay thế entry cho từ '" + oldWord + "'.");
                return false;
            }
            System.out.println("=> Đã cập nhật nội dung thành công.");
            return true;
        }
    }

    /**
     * Xóa một mục từ khỏi từ điển dựa trên headword.
     *
     * @param headword Headword của từ cần xóa.
     * @return true nếu xóa thành công, false nếu không tìm thấy từ.
     */
    public boolean deleteEntry(String headword) {
        if (headword == null || headword.trim().isEmpty()) {
            System.err.println("Lỗi: Headword xóa không hợp lệ.");
            return false;
        }
        String wordToDelete = headword.trim();

        // Kiểm tra xem từ có tồn tại không trước khi xóa
        Optional<DictionaryEntry> existingEntry = lookupEntry(wordToDelete);
        if (!existingEntry.isPresent()) {
            System.err.println("Lỗi: Không tìm thấy từ '" + wordToDelete + "' để xóa.");
            return false;
        }

        // Gọi hàm xóa của Trie
        boolean removed = dictionaryTrie.remove(wordToDelete);

        if (removed) {
            System.out.println("Đã xóa thành công từ '" + wordToDelete + "'.");
            return true;
        } else {
            // Trường hợp này hiếm xảy ra nếu lookupEntry đã tìm thấy
            System.err.println("Lỗi nội bộ: Không thể xóa từ '" + wordToDelete + "' từ Trie.");
            return false;
        }
    }


    /**
     * Lưu (ghi đè) dữ liệu từ điển hiện tại ra file dữ liệu chính bên ngoài.
     * Dữ liệu được sắp xếp trước khi ghi.
     */
    public void saveData() {
        System.out.println("Đang lưu dữ liệu từ điển vào file: " + DATA_FILE_PATH + "...");

        List<DictionaryEntry> entries = dictionaryTrie.getAllEntries();
        Collections.sort(entries, Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER));

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(DATA_FILE_PATH.toFile()), StandardCharsets.UTF_8))) {

            for (DictionaryEntry entry : entries) {
                // Write headword and pronunciation (if available)
                String headwordLine = "@ " + entry.getHeadword();
                if (entry.getPronunciation() != null && !entry.getPronunciation().isEmpty()) {
                    headwordLine += " /" + entry.getPronunciation() + "/";
                }
                writer.write(headwordLine);
                writer.newLine();

                for (WordSense sense : entry.getSenses()) {
                    // Write part of speech
                    writer.write("* " + sense.getPartOfSpeech());
                    writer.newLine();

                    // Write definitions
                    for (String definition : sense.getDefinitions()) {
                        writer.write("- " + definition);
                        writer.newLine();
                    }

                    // Write example phrases
                    for (ExamplePhrase example : sense.getExamples()) {
                        String exampleLine = "= " + example.getEnglish();
                        if (example.getVietnamese() != null && !example.getVietnamese().isEmpty()) {
                            exampleLine += " + " + example.getVietnamese();
                        }
                        writer.write(exampleLine);
                        writer.newLine();
                    }
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

    public List<DictionaryEntry> getAllEntries() {
        List<DictionaryEntry> allEntries = dictionaryTrie.getAllEntries();
        Collections.sort(allEntries, Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER));
        return allEntries;
    }
}
