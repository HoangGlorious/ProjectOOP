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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
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

    @FXML
    private TextField guessInput;

    @FXML
    private Button guessButton;

    @FXML
    private Button learnButton;

    private Stage primaryStage;

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

        // Lấy primaryStage từ guessInput
        if (guessInput != null && guessInput.getScene() != null && guessInput.getScene().getWindow() != null) {
            primaryStage = (Stage) guessInput.getScene().getWindow();
            System.out.println("PrimaryStage set in DailyWordleController: " + primaryStage);
        } else {
            System.err.println("Cannot set primaryStage: guessInput, scene, or window is null");
        }

        System.out.println("DailyWordleController initialized.");
    }

    private void restorePreviousGuesses() {
        List<String> attempts = game.getAttempts();
        List<List<WordleGame.LetterState>> states = game.getAttemptsStates();
        for (int i = 0; i < attempts.size(); i++) {
            updateUI(attempts.get(i), states.get(i));
        }
        checkGameEnd();
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
        } else {
            guessInput.setDisable(true);
            guessButton.setDisable(true);
            learnButton.setVisible(true);
            learnButton.setManaged(true);
            playClassicButton.setVisible(true);
            playClassicButton.setManaged(true);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/wordle_view.fxml"));
            Parent root = loader.load();
            WordleController controller = loader.getController();
            controller.setOnGoBackToMenu(() -> {
                try {
                    if (onGoBackToMenu != null) {
                        onGoBackToMenu.run();
                    }
                } catch (Exception e) {
                    System.err.println("Error going back to menu from Wordle: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            Scene scene = new Scene(root, 1200, 640);
            scene.getStylesheets().add(getClass().getResource("/com/application/test/CSS/wordle.css").toExternalForm());
            primaryStage.setScene(scene);
            System.out.println("Switched to Classic Wordle mode (wordle_view.fxml).");
        } catch (Exception e) {
            System.err.println("Error loading wordle_view.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi", "Không thể chuyển sang Wordle thường!");
        }
    }

    @FXML
    protected void learnWord(ActionEvent event) {
        System.out.println("learnWord() called in DailyWordleController");
        if (!(game instanceof DailyWordleGame)) {
            System.err.println("Error: game is not instance of DailyWordleGame");
            showAlert("Lỗi", "Không thể lấy thông tin từ vì trò chơi không hợp lệ!");
            return;
        }

        String targetWord = game.getTargetWord();
        String baseWord = game.getBaseForm(targetWord);
        System.out.println("Target word: " + targetWord + ", Base word: " + baseWord);

        try {
            // Tải dictionary_view.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/dictionary_view.fxml"));
            Parent root = loader.load();
            DictionaryController dictionaryController = loader.getController();

            // Kiểm tra primaryStage
            if (primaryStage == null) {
                System.err.println("primaryStage is null in learnWord!");
                showAlert("Lỗi", "Không thể truy cập cửa sổ chính!");
                return;
            }
            System.out.println("Using primaryStage: " + primaryStage);

            // Lấy DictionaryApplication từ userData
            DictionaryApplication app = (DictionaryApplication) primaryStage.getUserData();
            if (app == null) {
                System.err.println("DictionaryApplication not found in stage userData! Stage: " + primaryStage);
                showAlert("Lỗi", "Không thể truy cập ứng dụng từ điển!");
                return;
            }
            System.out.println("DictionaryApplication found: " + app);

            // Thiết lập DictionaryManagement và callback
            dictionaryController.setDictionaryManagement(app.getDictionaryManagement());
            dictionaryController.setOnGoBackToWelcome(() -> {
                try {
                    if (onGoBackToMenu != null) {
                        onGoBackToMenu.run(); // Quay lại menu Wordle
                    }
                } catch (Exception e) {
                    System.err.println("Error going back to Wordle menu: " + e.getMessage());
                }
            });

            // Thiết lập từ cần tìm kiếm
            dictionaryController.setInitialSearchTerm(baseWord);

            // Chuyển sang màn hình từ điển
            Scene dictionaryScene = new Scene(root, 1200, 640);
            dictionaryScene.getStylesheets().add(getClass().getResource("/com/application/test/CSS/style.css").toExternalForm());
            primaryStage.setScene(dictionaryScene);
            primaryStage.setTitle("📚 Dictionary Lookup");
            System.out.println("Switched to dictionary view with search term: " + baseWord);
        } catch (IOException e) {
            System.err.println("Error loading dictionary_view.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình từ điển!");
        }
    }

    @Override
    protected void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}