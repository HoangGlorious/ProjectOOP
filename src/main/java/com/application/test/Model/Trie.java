package com.application.test.Model;
import com.application.test.Model.DictionaryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Lớp đại diện cho một nút trong cây Trie
class TrieNode {
    Map<Character, TrieNode> children;
    List<DictionaryEntry> entries;

    TrieNode() {
        children = new HashMap<>();
        entries = new ArrayList<>();
    }
}

public class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    /**
     * Thêm một DictionaryEntry vào Trie.
     * Headword của entry được dùng làm key để chèn vào cây.
     *
     * @param entry DictionaryEntry cần thêm.
     */
    public void insert(DictionaryEntry entry) {
        if (entry == null || entry.getHeadword() == null || entry.getHeadword().isEmpty()) {
            return;
        }

        String word = entry.getHeadword().toLowerCase(); // Chuyển về chữ thường để không phân biệt hoa thường khi tìm kiếm
        TrieNode currentNode = root;

        for (char ch : word.toCharArray()) {
            // Nếu ký tự con chưa tồn tại, tạo nút mới
            currentNode.children.putIfAbsent(ch, new TrieNode());
            // Di chuyển đến nút con
            currentNode = currentNode.children.get(ch);
        }
        // Đến cuối từ, thêm entry vào danh sách tại nút hiện tại
        boolean alreadyExists = currentNode.entries.stream().anyMatch(existing -> existing.getHeadword().equalsIgnoreCase(entry.getHeadword()));
        if (!alreadyExists) {
            currentNode.entries.add(entry);
        } else {
            System.err.println("Warning: Duplicate headword '" + entry.getHeadword() + "' encountered during insert.");
        }
    }

    /**
     * Tìm kiếm tất cả các DictionaryEntry có headword bắt đầu bằng một tiền tố cho trước.
     *
     * @param prefix Tiền tố cần tìm.
     * @return Danh sách các DictionaryEntry khớp. Trả về danh sách rỗng nếu không tìm thấy.
     */
    public List<DictionaryEntry> searchByPrefix(String prefix) {
        List<DictionaryEntry> results = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) {
            return results; // Trả về rỗng nếu tiền tố rỗng
        }

        String lowerPrefix = prefix.toLowerCase();
        TrieNode prefixNode = findNode(lowerPrefix); // Tìm nút tương ứng với tiền tố

        if (prefixNode != null) {
            // Nếu tìm thấy nút cho tiền tố, duyệt từ nút đó để thu thập tất cả các entry
            collectAllEntries(prefixNode, results);
        }

        // Lưu ý: Kết quả trả về từ đây CHƯA được sắp xếp alphabet.
        // Việc sắp xếp có thể thực hiện ở lớp gọi (ví dụ: DictionaryManagement hoặc Controller).
        return results;
    }

    /**
     * Tìm kiếm chính xác một DictionaryEntry dựa trên headword.
     *
     * @param word Headword cần tìm.
     * @return Danh sách các DictionaryEntry khớp chính xác. Trả về danh sách rỗng nếu không tìm thấy.
     */
    public List<DictionaryEntry> findExact(String word) {
        List<DictionaryEntry> results = new ArrayList<>();
        if (word == null || word.isEmpty()) {
            return results;
        }

        String lowerWord = word.toLowerCase();
        TrieNode node = findNode(lowerWord); // Tìm nút cuối cùng của từ

        if (node != null && !node.entries.isEmpty()) {
            // Nếu tìm thấy nút và nút đó chứa entries, thêm chúng vào kết quả
            results.addAll(node.entries);
        }
        return results;
    }


    // --- Các hàm helper ---

    // Tìm nút trong Trie tương ứng với cuối của prefix
    private TrieNode findNode(String prefix) {
        TrieNode currentNode = root;
        for (char ch : prefix.toCharArray()) {
            if (!currentNode.children.containsKey(ch)) {
                return null; // Không tìm thấy nút cho ký tự này
            }
            currentNode = currentNode.children.get(ch);
        }
        return currentNode; // Trả về nút cuối cùng của prefix
    }

    // Duyệt đệ quy từ một nút để thu thập tất cả các entries con
    private void collectAllEntries(TrieNode node, List<DictionaryEntry> results) {
        // Thêm các entries tại nút hiện tại vào kết quả
        results.addAll(node.entries);

        // Duyệt qua tất cả các nút con
        for (TrieNode childNode : node.children.values()) {
            collectAllEntries(childNode, results); // Gọi đệ quy cho nút con
        }
    }

    // (Tùy chọn) Hàm xóa một entry khỏi Trie
    // Việc xóa trong Trie phức tạp hơn, cần kiểm tra nút có entries khác hoặc có nút con không
    // public boolean remove(DictionaryEntry entry) { ... }

    // (Tùy chọn) Hàm lấy tất cả entries trong Trie (cho chức năng showAllWords)
    public List<DictionaryEntry> getAllEntries() {
        List<DictionaryEntry> allEntries = new ArrayList<>();
        collectAllEntries(root, allEntries);
        // Kết quả này cần được sắp xếp alphabet sau khi lấy ra
        return allEntries;
    }

    /**
     * Xóa một DictionaryEntry với headword cụ thể khỏi Trie.
     * Nếu có nhiều entries với cùng headword, chỉ xóa những entry khớp.
     * Không prune cây.
     *
     * @param headword Headword của entry cần xóa.
     * @return true nếu tìm thấy và xóa ít nhất một entry, false nếu không tìm thấy.
     */
    public boolean remove(String headword) {
        if (headword == null || headword.isEmpty()) {
            return false;
        }
        String lowerWord = headword.toLowerCase();
        TrieNode node = findNode(lowerWord);

        boolean removed = false;
        if (node != null && !node.entries.isEmpty()) {
            // Xóa tất cả entries trong list có headword khớp (case-insensitive)
            removed = node.entries.removeIf(entry -> entry.getHeadword().equalsIgnoreCase(headword));
            // TODO: (Nâng cao) Thêm logic prune cây nếu node.entries rỗng và node không có con
        }
        return removed;
    }

    /**
     * Thay thế DictionaryEntry cho một headword cụ thể bằng một entry mới.
     * Được dùng khi headword không thay đổi. Xóa tất cả entries cũ khớp headword và thêm entry mới.
     *
     * @param headword Headword của entry cần thay thế.
     * @param updatedEntry Entry mới chứa nội dung cập nhật.
     * @return true nếu tìm thấy headword và thay thế thành công, false nếu không tìm thấy headword.
     */
    public boolean replaceEntry(String headword, DictionaryEntry updatedEntry) {
        if (headword == null || headword.isEmpty() || updatedEntry == null || !headword.equalsIgnoreCase(updatedEntry.getHeadword())) {
            // Headword phải khớp và entry mới không null/empty
            return false;
        }
        String lowerWord = headword.toLowerCase();
        TrieNode node = findNode(lowerWord);

        if (node != null && !node.entries.isEmpty()) {
            // Xóa tất cả entries cũ khớp headword tại nút này
            node.entries.removeIf(entry -> entry.getHeadword().equalsIgnoreCase(headword));
            // Thêm entry mới (đã cập nhật nội dung)
            node.entries.add(updatedEntry);
            return true;
        }
        return false; // Không tìm thấy nút cho headword
    }

}
