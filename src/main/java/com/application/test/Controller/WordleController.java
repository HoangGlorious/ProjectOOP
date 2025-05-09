package com.application.test.Controller;

import com.application.test.DictionaryApplication;
import com.application.test.Model.WordleGame;
import com.application.test.Model.GeneralManagement;
import com.application.test.Model.DictionarySource;
import com.application.test.Model.DictionaryEntry;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class WordleController {
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
    protected Button learnButton;

    protected WordleGame game;
    protected Label[][] letterLabels;

    public void initialize() {
        game = new WordleGame();
        letterLabels = new Label[MAX_ATTEMPTS][WORD_LENGTH];
        setupWordleGrid();
        guessInput.setOnKeyPressed(this::handleKeyPress);
        guessButton.setOnAction(event -> makeGuess());
        newGameButton.setOnAction(event -> resetGame());
        messageLabel.setText("Hãy đoán từ có 5 chữ cái!");

        if (learnButton != null) {
            learnButton.setVisible(false);
            learnButton.setManaged(false);
            learnButton.setOnAction(this::learnWord);
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
        if (!game.isValidGuess(guess)) {
            showAlert("Lỗi", "Từ '" + guess.toUpperCase() + "' không có trong từ điển cho phép!");
            return;
        }

        List<WordleGame.LetterState> states = game.makeGuess(guess);
        updateUI(guess, states);
        guessInput.clear();
        checkGameEnd();
    }

    protected void updateUI(String guess, List<WordleGame.LetterState> states) {
        int rowIndex = game.getCurrentAttempt() - 1;
        if (rowIndex < 0 || rowIndex >= MAX_ATTEMPTS) {
            System.err.println("Chỉ số hàng không hợp lệ khi cập nhật UI: " + rowIndex);
            return;
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            Label label = letterLabels[rowIndex][i];
            label.setText(String.valueOf(guess.charAt(i)).toUpperCase());
            label.getStyleClass().removeAll("correct", "present", "absent", "letter-box-filled");
            label.getStyleClass().add("letter-box");
            label.getStyleClass().add("letter-box-filled");

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
            playCutscene(true); // Phát cutscene thắng
        } else if (game.isGameOver()) {
            messageLabel.setText("Hết lượt! Từ cần đoán là: " + game.getTargetWord().toUpperCase());
            guessInput.setDisable(true);
            guessButton.setDisable(true);
            if (learnButton != null) {
                learnButton.setVisible(true);
                learnButton.setManaged(true);
            }
            playCutscene(false); // Phát cutscene thua
        } else {
            messageLabel.setText("Lượt đoán " + (game.getCurrentAttempt()) + "/" + MAX_ATTEMPTS);
        }
    }

    public void resetGame() {
        game.resetGame();
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
        if (onGoBackToMenu != null) {
            try {
                onGoBackToMenu.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go back to Games callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoBackToMenu is not set in WordleController!");
        }
    }

    @FXML
    protected void learnWord(ActionEvent event) {
        System.out.println("learnWord() called in WordleController");
        String targetWord = game.getTargetWord();

        // Kiểm tra targetWord có null hoặc rỗng
        if (targetWord == null || targetWord.isEmpty()) {
            System.err.println("Target word is null or empty!");
            showAlert("Lỗi", "Không thể học từ: Từ mục tiêu không hợp lệ!");
            return;
        }

        try {
            // Tải dictionary_view.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/dictionary_view.fxml"));
            Parent root = loader.load();
            DictionaryController dictionaryController = loader.getController();

            // Lấy Stage từ guessInput
            Stage stage = (Stage) guessInput.getScene().getWindow();
            if (stage == null) {
                System.err.println("Stage is null in learnWord!");
                showAlert("Lỗi", "Không thể truy cập cửa sổ chính!");
                return;
            }
            System.out.println("Using stage: " + stage);

            // Lấy DictionaryApplication từ userData
            DictionaryApplication app = (DictionaryApplication) stage.getUserData();
            if (app == null) {
                System.err.println("DictionaryApplication not found in stage userData! Stage: " + stage);
                showAlert("Lỗi", "Không thể truy cập ứng dụng từ điển!");
                return;
            }
            System.out.println("DictionaryApplication found: " + app);

            // Lấy GeneralManagement
            GeneralManagement dictionaryManagement = app.getDictionaryManagement();
            if (dictionaryManagement == null) {
                System.err.println("DictionaryManagement is null!");
                showAlert("Lỗi", "Không thể truy cập từ điển!");
                return;
            }

            // Lấy nguồn từ điển đang hoạt động
            DictionarySource activeSource = dictionaryManagement.getActiveSource();
            if (activeSource == null) {
                System.err.println("Active dictionary source is null!");
                showAlert("Lỗi", "Không có nguồn từ điển nào đang hoạt động!");
                return;
            }

            // Kiểm tra targetWord có trong từ điển hay không
            String searchWord;
            Optional<DictionaryEntry> foundEntry = activeSource.lookupEntry(targetWord);
            if (foundEntry.isPresent()) {
                searchWord = targetWord; // Sử dụng targetWord nếu có trong từ điển
                System.out.println("Target word found in dictionary: " + targetWord);
            } else {
                searchWord = game.getBaseForm(targetWord); // Sử dụng base form nếu không có
                System.out.println("Target word not found in dictionary, using base form: " + searchWord);
            }

            // Thiết lập DictionaryManagement và callback
            dictionaryController.setDictionaryManagement(dictionaryManagement);
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
            dictionaryController.triggerInitialState(searchWord);

            // Chuyển sang màn hình từ điển
            Scene dictionaryScene = new Scene(root, 1200, 640);
            dictionaryScene.getStylesheets().add(getClass().getResource("/com/application/test/CSS/style.css").toExternalForm());
            stage.setScene(dictionaryScene);
            stage.setTitle("📚 Dictionary Lookup");
            System.out.println("Switched to dictionary view with search term: " + searchWord);
        } catch (IOException e) {
            System.err.println("Error loading dictionary_view.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở màn hình từ điển!");
        }
    }

    protected void playCutscene(boolean isWin) {
        try {
            // Chọn video dựa trên trạng thái thắng/thua
            String videoPath = isWin ? "/com/application/test/videos/win_cutscene.mp4" : "/com/application/test/videos/lose_cutscene.mp4";
            String videoTitle = isWin ? "Cutscene Thắng" : "Cutscene Thua";
            System.out.println("Attempting to load cutscene: " + videoPath);

            // Kiểm tra tài nguyên video
            URL videoUrl = getClass().getResource(videoPath);
            if (videoUrl == null) {
                System.err.println("Video resource not found: " + videoPath);
                showAlert("Lỗi", "Không thể tìm thấy file video: " + videoPath);
                return;
            }
            System.out.println("Video resource found at: " + videoUrl.toExternalForm());

            // Tải video
            Media media = new Media(videoUrl.toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(800);
            mediaView.setFitHeight(600);

            // Tạo label hiển thị kết quả
            Label resultLabel = new Label(isWin ? "🎉 YOU WIN! 🎉" : "💀 YOU LOSE! 💀");
            resultLabel.setFont(new Font("Arial Black", 48));
            resultLabel.setTextFill(isWin ? Color.LIMEGREEN : Color.CRIMSON);
            resultLabel.setStyle("-fx-effect: dropshadow(gaussian, black, 5, 0, 0, 0);");

            // Tạo animation cho label (fade in / fade out)
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.8), resultLabel);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.3);
            fadeTransition.setCycleCount(Animation.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();

            // Tạo StackPane chứa video và label (label overlay lên video)
            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(mediaView, resultLabel);
            StackPane.setAlignment(resultLabel, Pos.TOP_CENTER); // canh label lên top center

            // Tạo nút Bỏ qua
            Button skipButton = new Button("Bỏ qua");

            // Lấy Stage hiện tại
            Stage stage = (Stage) guessInput.getScene().getWindow();
            if (stage == null) {
                System.err.println("Stage is null in playCutscene!");
                showAlert("Lỗi", "Không thể truy cập cửa sổ chính!");
                return;
            }
            Scene originalScene = guessInput.getScene();
            if (originalScene == null) {
                System.err.println("Original scene is null in playCutscene!");
                showAlert("Lỗi", "Không thể lưu scene trò chơi!");
                return;
            }

            // Hành động cho nút Bỏ qua
            skipButton.setOnAction(e -> {
                mediaPlayer.stop();
                restoreGameScene(stage, originalScene);
            });

            // Tạo VBox chứa StackPane và nút Bỏ qua
            VBox cutsceneLayout = new VBox(10);
            cutsceneLayout.setAlignment(Pos.CENTER);
            cutsceneLayout.getChildren().addAll(stackPane, skipButton);

            // Tạo scene cho cutscene
            Scene cutsceneScene = new Scene(cutsceneLayout, 1200, 640);
            stage.setScene(cutsceneScene);
            stage.setTitle(videoTitle);

            // Tự động phát video
            mediaPlayer.setAutoPlay(true);

            // Khi video kết thúc, khôi phục lại scene trò chơi
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.stop();
                restoreGameScene(stage, originalScene);
            });

            // Xử lý lỗi phát media
            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer error: " + mediaPlayer.getError().getMessage());
                showAlert("Lỗi", "Không thể phát cutscene: " + mediaPlayer.getError().getMessage());
                restoreGameScene(stage, originalScene);
            });

            System.out.println("Cutscene started: " + videoPath);

        } catch (Exception e) {
            showAlert("Thông báo", "Bạn đã hoàn thành wordle hôm nay rồi ");
        }
    }

    private void restoreGameScene(Stage stage, Scene originalScene) {
        if (stage == null || originalScene == null) {
            System.err.println("Cannot restore game scene: stage or originalScene is null");
            showAlert("Lỗi", "Không thể khôi phục giao diện trò chơi!");
            return;
        }
        stage.setScene(originalScene);
        stage.setTitle("Wordle");
        System.out.println("Restored game scene.");
    }
}