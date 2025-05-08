package com.application.test.Controller;

import com.application.test.DictionaryApplication;
import com.application.test.Model.Games;
import com.application.test.Model.WordleGame;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class WordleController{
    protected static final int WORD_LENGTH = 5;
    protected static final int MAX_ATTEMPTS = 6;
    protected Runnable onGoBackToMenu;
    @FXML
    protected VBox rootPane;
    @FXML
    protected GridPane wordleGrid;
    @FXML
    protected TextField guessInput;
    @FXML
    protected Button guessButton;
    @FXML
    protected Button newGameButton;
    @FXML
    protected Label messageLabel;
    @FXML
    protected Button learnButton; // Nút "Tìm hiểu từ"

    protected WordleGame game;
    protected Label[][] letterLabels;

    public void initialize() {
        game = new WordleGame(); // Giả sử WordleGame đã được khởi tạo và có từ điển
        letterLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
        setupWordleGrid();
        guessInput.setOnKeyPressed(this::handleKeyPress);
        guessButton.setOnAction(event -> makeGuess());
        newGameButton.setOnAction(event -> resetGame());
        messageLabel.setText("Hãy đoán từ có 5 chữ cái!");

        if (learnButton != null) {
            learnButton.setVisible(false); // Ẩn nút ban đầu
            learnButton.setManaged(false); // Không quản lý layout của nút khi ẩn
            learnButton.setOnAction(this::learnWord); // Gán hành động cho nút
        }
    }

    public void setOnGoBackToMenu(Runnable onGoBackToMenu) {
        this.onGoBackToMenu = onGoBackToMenu;
    }

    protected void setupWordleGrid() {
        wordleGrid.getChildren().clear();
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                Label label = new Label();
                label.setPrefSize(50, 50);
                label.setAlignment(Pos.CENTER);
                label.getStyleClass().add("letter-box");
                letterLabels[row][col] = label;
                wordleGrid.add(label, col, row);
            }
        }
    }

    protected void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            makeGuess();
        }
    }

    protected void makeGuess() {
        if (game.isGameOver()) {
            showAlert("Trò chơi đã kết thúc", "Hãy bắt đầu trò chơi mới!");
            return;
        }
        String guess = guessInput.getText().trim().toLowerCase();
        if (guess.length() != WORD_LENGTH) {
            showAlert("Lỗi", "Vui lòng nhập từ có đúng 5 chữ cái!");
            return;
        }
        if (!guess.matches("[a-z]+")) {
            showAlert("Lỗi", "Từ đoán chỉ được chứa các chữ cái a-z.");
            return;
        }
        if (!game.isValidGuess(guess)) { // Kiểm tra xem từ đoán có trong từ điển của WordleGame không
            showAlert("Lỗi", "Từ '" + guess.toUpperCase() + "' không có trong từ điển cho phép!");
            return;
        }

        List<WordleGame.LetterState> states = game.makeGuess(guess);
        // Giả sử game.makeGuess() không bao giờ trả về null nếu guess hợp lệ
        // Nếu có thể trả về null, cần kiểm tra
        // if (states == null) {
        //     showAlert("Lỗi", "Không thể xử lý lượt đoán này!");
        //     return;
        // }

        updateUI(guess, states);
        guessInput.clear();
        checkGameEnd();
    }

    protected void updateUI(String guess, List<WordleGame.LetterState> states) {
        int rowIndex = game.getCurrentAttempt() - 1; // game.getCurrentAttempt() là lần đoán TIẾP THEO
        // nên lần đoán vừa thực hiện là currentAttempt - 1
        if (rowIndex < 0 || rowIndex >= MAX_ATTEMPTS) {
            System.err.println("Chỉ số hàng không hợp lệ khi cập nhật UI: " + rowIndex);
            return;
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            Label label = letterLabels[rowIndex][i];
            label.setText(String.valueOf(guess.charAt(i)).toUpperCase());
            label.getStyleClass().removeAll("correct", "present", "absent", "letter-box-filled");
            label.getStyleClass().add("letter-box"); // Luôn giữ lại style cơ bản
            label.getStyleClass().add("letter-box-filled"); // Thêm style cho ô đã điền

            switch (states.get(i)) {
                case CORRECT:
                    label.getStyleClass().add("correct");
                    break;
                case PRESENT:
                    label.getStyleClass().add("present");
                    break;
                case ABSENT:
                    label.getStyleClass().add("absent");
                    break;
            }
        }
    }

    protected void checkGameEnd() {
        if (game.isGameWon()) {
            messageLabel.setText("Chúc mừng! Bạn đã đoán đúng: " + game.getTargetWord().toUpperCase());
            guessInput.setDisable(true);
            guessButton.setDisable(true);
            if (learnButton != null) {
                learnButton.setVisible(true);
                learnButton.setManaged(true);
            }
        } else if (game.isGameOver()) {
            messageLabel.setText("Hết lượt! Từ cần đoán là: " + game.getTargetWord().toUpperCase());
            guessInput.setDisable(true);
            guessButton.setDisable(true);
            if (learnButton != null) {
                learnButton.setVisible(true);
                learnButton.setManaged(true);
            }
        } else {
            messageLabel.setText("Lượt đoán " + (game.getCurrentAttempt()) + "/" + MAX_ATTEMPTS);
        }
    }

    public void resetGame() {
        game.resetGame(); // Đặt lại trạng thái game trong WordleGame
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                Label label = letterLabels[row][col];
                label.setText("");
                label.getStyleClass().removeAll("correct", "present", "absent", "letter-box-filled");
                label.getStyleClass().add("letter-box");
            }
        }
        guessInput.setDisable(false);
        guessButton.setDisable(false);
        guessInput.clear();
        messageLabel.setText("Trò chơi mới! Hãy đoán từ có 5 chữ cái!");
        if (learnButton != null) {
            learnButton.setVisible(false);
            learnButton.setManaged(false);
        }
        guessInput.requestFocus();
    }

    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void backToMenu(ActionEvent event) {
        System.out.println("Back button clicked in WordleController.");
        // resetGame(); // Có thể reset game trước khi quay lại menu
        if (onGoBackToMenu != null) {
            try {
                onGoBackToMenu.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go back to Games callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoBackToMenu is not set in WordleController!");
            // Cân nhắc không đóng stage ở đây, để lớp gọi quản lý stage
            // Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // stage.close();
        }
    }

    /**
     * Cố gắng lấy dạng cơ bản (lemma) của một từ.
     * Lưu ý: Hàm này đơn giản hóa và không thể bao quát hết các trường hợp bất quy tắc
     * hoặc phức tạp của tiếng Anh.
     * @param word Từ cần lấy dạng cơ bản.
     * @return Dạng cơ bản ước tính của từ.
     */
    private String getBaseForm(String word) {
        if (word == null || word.length() < 2) {
            return word;
        }

        String lowerWord = word.toLowerCase();

        // Ưu tiên các hậu tố đặc biệt và dài hơn trước
        // Ví dụ: cries -> cry (cho từ 5 chữ)
        if (lowerWord.length() == 5 && lowerWord.endsWith("ies")) {

            char charBeforeIes = lowerWord.charAt(lowerWord.length() - 4);
            if (!isVowel(charBeforeIes)) {
                return lowerWord.substring(0, lowerWord.length() - 3) + "y";
            }
        }


        if (lowerWord.length() == 5 && lowerWord.endsWith("ied")) {

            char charBeforeIed = lowerWord.charAt(lowerWord.length() - 4);
            if (!isVowel(charBeforeIed)) {
                return lowerWord.substring(0, lowerWord.length() - 3) + "y";
            }
        }


        if (lowerWord.endsWith("ed")) {
            String stem = lowerWord.substring(0, lowerWord.length() - 2);
            if (stem.length() == 0) return lowerWord;


            if (stem.endsWith("e")) {
                return stem;
            }

            if (stem.length() >= 2 && stem.charAt(stem.length() - 1) == stem.charAt(stem.length() - 2) &&
                    !isVowel(stem.charAt(stem.length() - 1)) && // là phụ âm
                    !(stem.endsWith("ll") || stem.endsWith("ss") || stem.endsWith("ff") || stem.endsWith("zz")) // Trừ các đuôi ll, ss, ff, zz thường giữ nguyên
            ) {

                return stem.substring(0, stem.length() - 1);
            }
            // Từ gốc kết thúc bằng "ed": "need", "feed", "bed"
            if (lowerWord.equals("need") || lowerWord.equals("feed") || lowerWord.equals("bed") ||
                    lowerWord.equals("bleed") || lowerWord.equals("speed") || lowerWord.equals("breed")) {
                return lowerWord;
            }
            return stem;
        }


        if (lowerWord.endsWith("es")) {
            String stem = lowerWord.substring(0, lowerWord.length() - 2);
            if (stem.length() == 0) return lowerWord;


            if (stem.endsWith("s") || stem.endsWith("x") || stem.endsWith("z") ||
                    (stem.length() >= 2 && (stem.substring(stem.length()-2).equals("ch") || stem.substring(stem.length()-2).equals("sh")))) {
                return stem;
            }

            if (lowerWord.equals("goes")) return "go";
            if (lowerWord.equals("does")) return "do";

            if (stem.endsWith("e")) {
                return stem;
            }
            return lowerWord;
        }


        if (lowerWord.endsWith("s")) {

            if (lowerWord.endsWith("ss")) {
                return lowerWord;
            }
            String stem = lowerWord.substring(0, lowerWord.length() - 1);
            if (stem.length() == 0) return lowerWord;


            if (lowerWord.equals("bus") || lowerWord.equals("lens") || lowerWord.equals("always") ||
                    lowerWord.equals("is") || lowerWord.equals("as") || lowerWord.equals("this") ||
                    lowerWord.equals("its") || lowerWord.equals("his") || lowerWord.equals("us") ||
                    lowerWord.equals("plus")) {
                return lowerWord;
            }

            return stem;
        }

        return lowerWord;
    }

    private boolean isVowel(char c) {
        return "aeiou".indexOf(Character.toLowerCase(c)) != -1;
    }

    @FXML
    protected void learnWord(ActionEvent event) {
        String targetWord = game.getTargetWord();
        if (targetWord == null || targetWord.isEmpty()) {
            showAlert("Thông báo", "Không có từ mục tiêu để tìm hiểu.");
            return;
        }

        String baseWord = getBaseForm(targetWord);
        System.out.println("learnWord() called with targetWord: " + targetWord + ", baseWord: " + baseWord);

        DictionaryApplication app = null;
        if (guessInput.getScene() != null && guessInput.getScene().getWindow() != null) {
            Object userData = guessInput.getScene().getWindow().getUserData();
            if (userData instanceof DictionaryApplication) {
                app = (DictionaryApplication) userData;
            }
        }

//        if (app != null) {
//            System.out.println("DictionaryApplication found, calling handleSearchInitiated with: " + baseWord);
//            app.handleSearchInitiated(baseWord); // Gọi phương thức để chuyển sang tab tra từ và tìm kiếm
//            System.out.println("handleSearchInitiated called.");
//        } else {
//            System.err.println("DictionaryApplication instance not found in stage user data!");
//            showAlert("Lỗi", "Không thể khởi chạy chức năng tìm hiểu từ điển.");
//        }
    }
}