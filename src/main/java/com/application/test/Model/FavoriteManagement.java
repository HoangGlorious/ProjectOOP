package com.application.test.Model; // Hoặc một package mới như com.application.test.favorites

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteManagement {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String FAVORITES_FILE_NAME = "favorite_words.txt";
    private static final Path FAVORITES_FILE_PATH = Paths.get(USER_HOME, FAVORITES_FILE_NAME);

    private Set<String> favoriteHeadwords; // Dùng Set để đảm bảo không có trùng lặp và tìm kiếm nhanh

    public FavoriteManagement() {
        this.favoriteHeadwords = new HashSet<>();
        loadFavorites(); // Nạp từ yêu thích khi khởi tạo
    }

    /**
     * Nạp danh sách từ yêu thích từ file.
     */
    private void loadFavorites() {
        if (Files.exists(FAVORITES_FILE_PATH)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(FAVORITES_FILE_PATH.toFile(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String headword = line.trim();
                    if (!headword.isEmpty()) {
                        favoriteHeadwords.add(headword.toLowerCase()); // Lưu dưới dạng chữ thường để không phân biệt hoa thường
                    }
                }
                System.out.println("Đã nạp " + favoriteHeadwords.size() + " từ yêu thích.");
            } catch (IOException e) {
                System.err.println("Lỗi khi nạp từ yêu thích: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("File từ yêu thích không tồn tại. Sẽ được tạo khi lưu.");
        }
    }

    /**
     * Lưu danh sách từ yêu thích hiện tại ra file.
     */
    public void saveFavorites() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(FAVORITES_FILE_PATH.toFile()), StandardCharsets.UTF_8))) {

            for (String headword : favoriteHeadwords) {
                writer.write(headword);
                writer.newLine();
            }
            System.out.println("Đã lưu " + favoriteHeadwords.size() + " từ yêu thích vào file.");
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu từ yêu thích: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thêm một từ vào danh sách yêu thích.
     * @param headword Từ gốc cần thêm.
     * @return true nếu thêm thành công, false nếu từ đã tồn tại hoặc không hợp lệ.
     */
    public boolean addFavorite(String headword) {
        if (headword == null || headword.trim().isEmpty()) {
            return false;
        }
        boolean added = favoriteHeadwords.add(headword.trim().toLowerCase());
        if (added) {
            System.out.println("Đã thêm '" + headword + "' vào yêu thích.");
            saveFavorites(); // Tùy chọn: Lưu ngay sau khi thêm
        }
        return added;
    }

    /**
     * Xóa một từ khỏi danh sách yêu thích.
     * @param headword Từ gốc cần xóa.
     * @return true nếu xóa thành công, false nếu từ không tồn tại hoặc không hợp lệ.
     */
    public boolean removeFavorite(String headword) {
        if (headword == null || headword.trim().isEmpty()) {
            return false;
        }
        boolean removed = favoriteHeadwords.remove(headword.trim().toLowerCase());
        if (removed) {
            System.out.println("Đã xóa '" + headword + "' khỏi yêu thích.");
            saveFavorites(); // Tùy chọn: Lưu ngay sau khi xóa
        }
        return removed;
    }

    /**
     * Kiểm tra xem một từ có nằm trong danh sách yêu thích không.
     * @param headword Từ gốc cần kiểm tra.
     * @return true nếu là từ yêu thích, false nếu không.
     */
    public boolean isFavorite(String headword) {
        if (headword == null || headword.trim().isEmpty()) {
            return false;
        }
        return favoriteHeadwords.contains(headword.trim().toLowerCase());
    }

    /**
     * Lấy danh sách tất cả các từ yêu thích.
     * @return List các headword yêu thích (đã sắp xếp).
     */
    public List<String> getAllFavoriteWords() {
        List<String> sortedFavorites = new ArrayList<>(favoriteHeadwords);
        Collections.sort(sortedFavorites); // Sắp xếp để hiển thị
        return sortedFavorites;
    }
}