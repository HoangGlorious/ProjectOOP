package com.application.test.Model;
import com.application.test.Model.DictionaryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Lớp đại diện cho một nút trong cây Trie
class TrieNode {
    Map<Character, TrieNode> children;
    List<DictionaryEntry> entries;

    TrieNode() {
        children = new HashMap<>();
        entries = new ArrayList<>();
    }
}

// Lớp cài đặt cấu trúc Trie
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
        currentNode.entries.add(entry);
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
}
