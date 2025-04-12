package com.example.btl;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class DictionaryCommandline {
    private final DictionaryManagement dictionaryManagement;
    private final Dictionary dictionary;
    private final Scanner scanner;

    public DictionaryCommandline(Dictionary dictionary, DictionaryManagement dictionaryManagement) {
        this.dictionary = dictionary;
        this.dictionaryManagement = dictionaryManagement;
        this.scanner = new Scanner(System.in);
    }

    // Hiển thị tất cả các mục từ theo thứ tự alphabet của headword
    public void showAllWords() {
        // Lấy danh sách các DictionaryEntry
        List<DictionaryEntry> entries = dictionary.getAllEntries();

        if (entries.isEmpty()) {
            System.out.println("Từ điển hiện đang trống.");
            return;
        }

        // Sắp xếp danh sách theo headword
        Collections.sort(entries, Comparator.comparing(DictionaryEntry::getHeadword, String.CASE_INSENSITIVE_ORDER));


        System.out.println("-------------------- TỪ ĐIỂN --------------------");
        for (int i = 0; i < entries.size(); i++) {
            DictionaryEntry entry = entries.get(i);
            System.out.println("\nMục từ " + (i + 1) + ":");
            // Sử dụng hàm getFormattedExplanation đã tạo trong DictionaryEntry
            System.out.println(entry.getFormattedExplanation());
            System.out.println("--------------------------------------------------");
        }
    }

    // --- Cập nhật dictionaryBasic và các hàm khác nếu cần ---
    // dictionaryBasic bây giờ không còn phù hợp vì không có nhập liệu theo định dạng mới từ console
    // Bạn có thể tạo hàm mới để chỉ nạp file và hiển thị
    public void loadAndShow() {
        System.out.println("--- Chế độ Từ điển Nâng cao ---");
        // Nạp từ file (đã gọi từ Main hoặc ở đây)
        // dictionaryManagement.insertFromFile(DictionaryManagement.DEFAULT_FILE_PATH);
        System.out.println("\n--- Danh sách từ trong từ điển ---");
        showAllWords();
    }

    /**
     * Hàm tương tác để người dùng nhập từ và tìm kiếm theo tiền tố.
     * Hiển thị kết quả tìm được.
     */
    public void dictionarySearcherInteractive() {
        System.out.print("\nNhập từ cần tìm kiếm (tìm theo tiền tố): ");
        String searchTerm = scanner.nextLine(); // Đọc input từ Scanner đã khởi tạo

        // Gọi hàm tìm kiếm từ DictionaryManagement
        List<DictionaryEntry> results = dictionaryManagement.searchEntriesByPrefix(searchTerm);

        // Hiển thị kết quả
        if (results.isEmpty()) {
            System.out.println("Không tìm thấy kết quả nào khớp với '" + searchTerm + "'.");
        } else {
            System.out.println("\n--- Kết quả tìm kiếm cho '" + searchTerm + "' (" + results.size() + " mục) ---");
            for (int i = 0; i < results.size(); i++) {
                DictionaryEntry entry = results.get(i);
                System.out.println("\nKết quả " + (i + 1) + ":");
                System.out.println(entry.getFormattedExplanation()); // In thông tin chi tiết
                System.out.println("--------------------------------------------------");
            }
        }
    }

    /**
     * Hàm tương tác để người dùng nhập từ và tra cứu chính xác.
     * Hiển thị kết quả tìm được.
     */
    public void dictionaryLookupInteractive() {
        System.out.print("\nNhập từ cần tra cứu chính xác: ");
        String searchTerm = scanner.nextLine();

        Optional<DictionaryEntry> result = dictionaryManagement.lookupEntry(searchTerm);

        if (result.isPresent()) {
            System.out.println("\n--- Thông tin từ '" + searchTerm + "' ---");
            System.out.println(result.get().getFormattedExplanation());
            System.out.println("--------------------------------------------------");
        } else {
            System.out.println("Không tìm thấy từ '" + searchTerm + "' trong từ điển.");
        }
    }

    /**
     * Hàm tương tác để người dùng thêm một mục từ mới vào từ điển.
     */
    public void addWordInteractive() {
        System.out.println("\n--- Thêm từ mới vào từ điển ---");

        // 1. Nhập Headword (bắt buộc)
        String headword;
        while (true) {
            System.out.print("Nhập từ tiếng Anh (Headword - bắt buộc): ");
            headword = scanner.nextLine().trim();
            if (!headword.isEmpty()) {
                // Kiểm tra trùng lặp ngay lập tức
                if (dictionaryManagement.lookupEntry(headword).isPresent()) {
                    System.out.println("Lỗi: Từ '" + headword + "' đã tồn tại. Vui lòng nhập từ khác.");
                } else {
                    break; // Từ hợp lệ và không trùng
                }
            } else {
                System.out.println("Headword không được để trống.");
            }
        }

        // 2. Nhập Phiên âm (tùy chọn)
        System.out.print("Nhập phiên âm (ví dụ: /heˈloʊ/) (bỏ trống nếu không có): ");
        String pronunciation = scanner.nextLine().trim();
        if (!pronunciation.isEmpty() && (!pronunciation.startsWith("/") || !pronunciation.endsWith("/"))) {
            System.out.println("Cảnh báo: Phiên âm nên được đặt trong dấu /.../");
        }

        // Tạo đối tượng DictionaryEntry ban đầu
        DictionaryEntry newEntry = new DictionaryEntry(headword, pronunciation);

        // 3. Nhập các Word Sense (ít nhất 1)
        boolean addedAtLeastOneSense = false;
        while (true) {
            System.out.print("\nThêm một loại từ/cách dùng (Word Sense)? (y/n): ");
            String addSenseChoice = scanner.nextLine().trim().toLowerCase();
            if (!addSenseChoice.equals("y")) {
                if (!addedAtLeastOneSense) {
                    System.out.println("Lỗi: Phải thêm ít nhất một loại từ/cách dùng.");
                    continue; // Bắt buộc nhập lại lựa chọn thêm sense
                }
                break; // Kết thúc việc thêm sense
            }

            // 3.1. Nhập Loại từ (Part of Speech - bắt buộc)
            String partOfSpeech;
            while (true) {
                System.out.print("  * Nhập loại từ (ví dụ: danh từ, động từ - bắt buộc): ");
                partOfSpeech = scanner.nextLine().trim();
                if (!partOfSpeech.isEmpty()) {
                    break;
                } else {
                    System.out.println("  Loại từ không được để trống.");
                }
            }
            WordSense currentSense = new WordSense(partOfSpeech);

            // 3.2. Nhập Định nghĩa (Definitions - ít nhất 1)
            System.out.println("  Nhập các định nghĩa (ít nhất 1). Nhập dòng trống để kết thúc:");
            boolean addedAtLeastOneDefinition = false;
            while (true) {
                System.out.print("    - Định nghĩa: ");
                String definition = scanner.nextLine().trim();
                if (definition.isEmpty()) {
                    if (!addedAtLeastOneDefinition) {
                        System.out.println("    Lỗi: Phải nhập ít nhất một định nghĩa cho loại từ này.");
                        continue; // Yêu cầu nhập lại định nghĩa
                    }
                    break; // Kết thúc nhập định nghĩa
                }
                currentSense.addDefinition(definition);
                addedAtLeastOneDefinition = true;
            }

            // 3.3. Nhập Ví dụ (Examples - tùy chọn)
            System.out.println("  Nhập các ví dụ (tùy chọn). Nhập dòng trống ở phần tiếng Anh để kết thúc:");
            while (true) {
                System.out.print("    = Ví dụ tiếng Anh (bỏ trống để kết thúc): ");
                String engExample = scanner.nextLine().trim();
                if (engExample.isEmpty()) {
                    break; // Kết thúc nhập ví dụ
                }
                System.out.print("      + Nghĩa tiếng Việt của ví dụ: ");
                String vieExample = scanner.nextLine().trim();
                currentSense.addExample(new ExamplePhrase(engExample, vieExample));
            }

            // Thêm sense vừa tạo vào entry
            newEntry.addSense(currentSense);
            addedAtLeastOneSense = true; // Đánh dấu đã thêm thành công ít nhất 1 sense
        } // Kết thúc vòng lặp thêm sense

        // 4. Lưu vào từ điển
        if (dictionaryManagement.addEntry(newEntry)) {
            System.out.println("\n=> Đã thêm thành công từ '" + headword + "' vào từ điển!");
        } else {
            // Thông báo lỗi đã được in trong dictionaryManagement.addEntry()
            System.out.println("\n=> Thêm từ không thành công.");
        }
    }

    // Bạn có thể thêm hàm để đóng Scanner khi chương trình kết thúc
    public void closeScanner() {
        System.out.println("Đóng scanner.");
        scanner.close();
    }

    public void dictionaryAdvanced() {
        boolean running = true;
        while(running) {
            // Hiển thị menu (0: Thoát, 1: Tìm kiếm tiền tố, 2: Tra cứu chính xác, 3: Show All, ...)
            System.out.println("\n----- MENU -----");
            System.out.println("[1] Tìm kiếm (tiền tố)");
            System.out.println("[2] Tra cứu (chính xác)");
            System.out.println("[3] Hiển thị toàn bộ từ điển");
            System.out.println("[0] Thoát");
            System.out.print("Nhập lựa chọn của bạn: ");
            String choice = scanner.nextLine();

            switch(choice) {
                case "1":
                    dictionarySearcherInteractive();
                    break;
                case "2":
                    dictionaryLookupInteractive();
                    break;
                case "3":
                    showAllWords();
                    break;
                case "4":
                    addWordInteractive();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("\nnhap lai di bozo");
            }
        }
        closeScanner(); // Đóng scanner khi thoát vòng lặp
    }

    // --- Các hàm như dictionaryAdvanced, dictionarySearcher cần cập nhật ---
    // Ví dụ: dictionarySearcher sẽ tìm và trả về List<DictionaryEntry>
    // dictionaryLookup sẽ tìm và trả về Optional<DictionaryEntry>
}
