package com.application.test.Controller;

import com.application.test.Model.WordleGame;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class WordleController {
    private static final int WORD_LENGTH = 5;
    private static final int MAX_ATTEMPTS = 6;

    @FXML
    private VBox rootPane;

    @FXML
    private GridPane wordleGrid;

    @FXML
    private TextField guessInput;

    @FXML
    private Button guessButton;

    @FXML
    private Button newGameButton;

    @FXML
    private Label messageLabel;

    private WordleGame game;
    private Label[][] letterLabels;

    public void initialize() {
        // Tạo game với đường dẫn đến file từ điển 5 chữ cái
        game = new WordleGame();

        // Khởi tạo grid của Wordle (6 hàng, 5 cột)
        letterLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
        setupWordleGrid();

        // Xử lý sự kiện nhập từ bàn phím
        guessInput.setOnKeyPressed(this::handleKeyPress);

        // Xử lý nút đoán
        guessButton.setOnAction(event -> makeGuess());

        // Xử lý nút trò chơi mới
        newGameButton.setOnAction(event -> resetGame());

        // Hiển thị thông báo ban đầu
        messageLabel.setText("Hãy đoán từ có 5 chữ cái!");
    }

    private void setupWordleGrid() {
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

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            makeGuess();
        }
    }

    private void makeGuess() {
        if (game.isGameOver()) {
            showAlert("Trò chơi đã kết thúc", "Hãy bắt đầu trò chơi mới!");
            return;
        }

        String guess = guessInput.getText().trim().toLowerCase();

        if (guess.length() != WORD_LENGTH) {
            showAlert("Lỗi", "Vui lòng nhập từ có đúng 5 chữ cái!");
            return;
        }

        if (!game.isValidGuess(guess)) {
            showAlert("Lỗi", "Từ không có trong từ điển!");
            return;
        }

        List<WordleGame.LetterState> states = game.makeGuess(guess);
        if (states == null) {
            showAlert("Lỗi", "Không thể đoán từ này!");
            return;
        }

        // Cập nhật giao diện với kết quả đoán
        updateUI(guess, states);

        // Xóa ô nhập
        guessInput.clear();

        // Kiểm tra kết thúc trò chơi
        checkGameEnd();
    }

    private void updateUI(String guess, List<WordleGame.LetterState> states) {
        int rowIndex = game.getCurrentAttempt() - 1;

        for (int i = 0; i < WORD_LENGTH; i++) {
            Label label = letterLabels[rowIndex][i];
            label.setText(String.valueOf(guess.charAt(i)).toUpperCase());

            // Xóa tất cả style class trước đó và thêm style mới
            label.getStyleClass().removeAll("correct", "present", "absent");
            label.getStyleClass().add("letter-box"); // Đảm bảo luôn có class cơ bản

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

    private void checkGameEnd() {
        if (game.isGameWon()) {
            messageLabel.setText("Chúc mừng! Bạn đã đoán đúng: " + game.getTargetWord().toUpperCase());
            guessInput.setDisable(true);
            guessButton.setDisable(true);
        } else if (game.isGameOver()) {
            messageLabel.setText("Game over! Từ cần đoán là: " + game.getTargetWord().toUpperCase());
            guessInput.setDisable(true);
            guessButton.setDisable(true);
        } else {
            messageLabel.setText("Lượt đoán " + game.getCurrentAttempt() + "/" + game.getMaxAttempts());
        }
    }

    private void resetGame() {
        game.resetGame();

        // Đặt lại giao diện
        for (int row = 0; row < MAX_ATTEMPTS; row++) {
            for (int col = 0; col < WORD_LENGTH; col++) {
                Label label = letterLabels[row][col];
                label.setText("");
                label.getStyleClass().removeAll("correct", "present", "absent");
                label.getStyleClass().add("letter-box");
            }
        }

        // Đặt lại trạng thái
        guessInput.setDisable(false);
        guessButton.setDisable(false);
        guessInput.clear();
        messageLabel.setText("Trò chơi mới! Hãy đoán từ có 5 chữ cái!");

        // Focus vào ô nhập
        guessInput.requestFocus();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Method to handle the "Back" button to return to the Games menu
     */
    @FXML
    protected void backToGames() {
        try {
            // Load the games.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/games.fxml"));
            Parent gamesRoot = loader.load();

            // Get the current stage
            Stage primaryStage = (Stage) rootPane.getScene().getWindow();

            // Set the new scene
            Scene gamesScene = new Scene(gamesRoot);
            primaryStage.setTitle("Games");
            primaryStage.setScene(gamesScene);
            primaryStage.sizeToScene();

            System.out.println("Returned to games menu");
        } catch (IOException e) {
            System.err.println("Error returning to games menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}