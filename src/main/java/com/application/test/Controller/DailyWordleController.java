package com.application.test.Controller;

import com.application.test.DictionaryApplication;
import com.application.test.Model.DailyWordleGame;
import com.application.test.Model.WordleGame;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DailyWordleController extends WordleController implements Initializable {

    @FXML
    private Label dailyStatusLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Button playClassicButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        super.initialize();
        try {
            game = new DailyWordleGame();
            System.out.println("DailyWordleGame initialized with target word: " + game.getTargetWord());
            System.out.println("Can play today: " + ((DailyWordleGame) game).canPlayTodayGenuine());
        } catch (Exception e) {
            System.err.println("Error initializing DailyWordleGame: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi", "Không thể khởi tạo trò chơi Daily Wordle!");
            return;
        }

        if (dateLabel != null) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dateLabel.setText("Ngày: " + today.format(formatter));
        }

        if (guessInput != null) {
            guessInput.setOnKeyPressed(this::handleKeyPress);
        }
        if (guessButton != null) {
            guessButton.setOnAction(event -> makeGuess());
        }

        if (game instanceof DailyWordleGame && !((DailyWordleGame) game).canPlayTodayGenuine()) {
            restorePreviousGuesses();
        }
        updateDailyStatusAndControls();

        System.out.println("DailyWordleController initialized.");
    }

    private void restorePreviousGuesses() {
        List<String> attempts = game.getAttempts();
        List<List<WordleGame.LetterState>> states = game.getAttemptsStates();
        for (int i = 0; i < attempts.size(); i++) {
            updateUI(attempts.get(i), states.get(i));
        }
        checkGameEnd(); // Cập nhật thông báo và nút dựa trên trạng thái game
        System.out.println("Restored " + attempts.size() + " previous guesses.");
    }

    private void updateDailyStatusAndControls() {
        System.out.println("Updating daily status and controls...");
        if (!(game instanceof DailyWordleGame)) {
            System.err.println("Error: game is not instance of DailyWordleGame");
            return;
        }

        DailyWordleGame dailyGame = (DailyWordleGame) game;
        boolean canPlay = dailyGame.canPlayTodayGenuine();
        boolean isAlreadyOver = game.isGameOver();
        boolean isAlreadyWon = game.isGameWon();
        boolean isGameFinished = isAlreadyOver || isAlreadyWon;

        System.out.println("Can Play Today: " + canPlay);
        System.out.println("Is Game Finished: " + isGameFinished);
        System.out.println("Is Game Won: " + isAlreadyWon);
        System.out.println("Is Game Over: " + isAlreadyOver);

        if (dailyStatusLabel == null || guessInput == null || guessButton == null
                || learnButton == null || playClassicButton == null) {
            System.err.println("Lỗi: FXML controls chưa được inject đầy đủ.");
            System.out.println("dailyStatusLabel: " + dailyStatusLabel);
            System.out.println("guessInput: " + guessInput);
            System.out.println("guessButton: " + guessButton);
            System.out.println("learnButton: " + learnButton);
            System.out.println("playClassicButton: " + playClassicButton);
            return;
        }

        if (canPlay && !isGameFinished) {
            guessInput.setDisable(false);
            guessButton.setDisable(false);
            guessInput.requestFocus();
            learnButton.setVisible(false);
            learnButton.setManaged(false);
            playClassicButton.setVisible(false);
            playClassicButton.setManaged(false);
            System.out.println("Input controls ENABLED, action buttons HIDDEN.");
        } else {
            guessInput.setDisable(true);
            guessButton.setDisable(true);
            learnButton.setVisible(true);
            learnButton.setManaged(true);
            playClassicButton.setVisible(true);
            playClassicButton.setManaged(true);
            System.out.println("Input controls DISABLED, action buttons SHOWN.");
            System.out.println("learnButton visibility: " + learnButton.isVisible());
            System.out.println("playClassicButton visibility: " + playClassicButton.isVisible());
        }

        if (!canPlay) {
            if (isAlreadyWon) {
                dailyStatusLabel.setText("Bạn đã thắng Wordle hôm nay! Quay lại vào ngày mai.");
            } else {
                dailyStatusLabel.setText("Bạn đã hoàn thành Wordle hôm nay! Quay lại vào ngày mai.");
            }
        } else if (isAlreadyWon) {
            dailyStatusLabel.setText("Bạn đã thắng! Quay lại vào ngày mai.");
        } else if (game.isGameOver()) {
            dailyStatusLabel.setText("Hết lượt! Quay lại vào ngày mai.");
        } else {
            dailyStatusLabel.setText("Hãy đoán từ của ngày hôm nay!");
        }

        if (!canPlay || isGameFinished) {
            messageLabel.setText("Từ cần đoán là: " + game.getTargetWord().toUpperCase());
        } else {
            messageLabel.setText("Trò chơi mới! Hãy đoán từ có 5 chữ cái!");
        }
    }

    @Override
    protected void makeGuess() {
        System.out.println("Make guess attempt...");
        if (game.isGameOver() || game.isGameWon()) {
            System.out.println("Game already over, guess ignored.");
            return;
        }
        super.makeGuess();
        updateDailyStatusAndControls();
        System.out.println("Guess processed.");
    }

    @Override
    protected void handleKeyPress(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            System.out.println("Enter key pressed.");
            makeGuess();
            event.consume();
        }
    }

    @FXML
    private void handlePlayClassic() {
        System.out.println("Handling Play Classic button click...");
        try {
            // Tải wordle_view.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/View/wordle_view.fxml"));
            Parent root = loader.load();

            // Lấy controller của wordle_view.fxml
            WordleController controller = loader.getController();
            controller.setOnGoBackToMenu(() -> {
                try {
                    // Quay lại menu khi nhấn Back từ Wordle thường
                    if (onGoBackToMenu != null) {
                        onGoBackToMenu.run();
                    }
                } catch (Exception e) {
                    System.err.println("Error going back to menu from Wordle: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Thay đổi scene
            Stage stage = (Stage) playClassicButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 640);
            scene.getStylesheets().add(getClass().getResource("/com/application/test/CSS/wordle.css").toExternalForm());
            stage.setScene(scene);
            System.out.println("Switched to Classic Wordle mode (wordle_view.fxml).");
        } catch (Exception e) {
            System.err.println("Error loading wordle_view.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi", "Không thể chuyển sang Wordle thường!");
        }
    }
    private String getBaseForm(String word) {
        if (word == null || word.length() < 2) {
            return word;
        }
        if (word.endsWith("es")) {
            return word.substring(0, word.length() - 2);
        } else if (word.endsWith("ed")) {
            return word.substring(0, word.length() - 2);
        } else if (word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
    @FXML
    protected void learnWord(ActionEvent event) {
//        String targetWord = game.getTargetWord();
//        String baseWord = getBaseForm(targetWord);
//        System.out.println("learnWord() called with targetWord: " + targetWord + ", baseWord: " + baseWord);
//        DictionaryApplication app = (DictionaryApplication) guessInput.getScene().getWindow().getUserData();
//        if (app != null) {
//            System.out.println("DictionaryApplication found, calling handleSearchInitiated with: " + baseWord);
//            app.handleSearchInitiated(baseWord);
//            System.out.println("handleSearchInitiated called, checking if dictionary updated...");
//        } else {
//            System.err.println("DictionaryApplication instance not found in stage user data!");
//        }
    }

}