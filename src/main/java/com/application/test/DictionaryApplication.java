package com.application.test;

import com.application.test.Controller.*;
import com.application.test.Model.GeneralManagement;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Parent;


import java.io.IOException;
import java.net.URL;

public class DictionaryApplication extends Application {

    private Stage primaryStage;
    private GeneralManagement dictionaryManagement;
    private Scene welcomeScene;
    private Scene dictionaryScene;
    private Scene gameMenuScene;
    private Scene wordleScene;
    private Scene thesaurusScene;
    private WelcomeController welcomeControllerInstance;
    private DictionaryController dictionaryControllerInstance;
    private ThesaurusController thesaurusControllerInstance;
    private GamesController gamesControllerInstance;
    private WordleController wordleControllerInstance;
    private String pendingActionWord = null;
    private boolean pendingAddAction = false;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        stage.setTitle("Ứng dụng Từ điển");

        // *** Khởi tạo DictionaryManager và nạp dữ liệu cho TẤT CẢ các nguồn ***
        this.dictionaryManagement = new GeneralManagement();
        dictionaryManagement.loadAllSourcesData();

        // --- Load màn hình Welcome ---
        URL welcomeFxmlUrl = getClass().getResource("/com/application/test/view/welcome.fxml"); // Kiểm tra lại đường dẫn
        if (welcomeFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy file welcome.fxml trong classpath!"); System.exit(1); }
        FXMLLoader welcomeLoader = new FXMLLoader(welcomeFxmlUrl);
        Parent welcomeRoot = welcomeLoader.load();

        this.welcomeControllerInstance = welcomeLoader.getController();

        WelcomeController welcomeController = welcomeLoader.getController();
        welcomeController.setDictionaryManagement(this.dictionaryManagement);
        welcomeController.setOnSearchInitiated(this::handleSearchInitiated);
        welcomeController.setOnAddWordInitiated(this::handleAddWordInitiated);
        welcomeController.setOnGoToGame(this::showGameMenu);
        welcomeController.setOnGoToThesaurus(this::showThesaurusView);


        this.welcomeScene = new Scene(welcomeRoot);
        stage.setScene(welcomeScene);
        stage.getIcons().add(new Image(getClass().getResource("/com/application/test/images/app_icon.png").toExternalForm()));
        stage.show();
        stage.setResizable(false);

        // Xử lý sự kiện khi đóng cửa sổ
        stage.setOnCloseRequest(event -> {
            System.out.println("Đang đóng ứng dụng...");
            if (dictionaryManagement != null) {
                dictionaryManagement.saveAllSourcesData(); // Lưu dữ liệu cho TẤT CẢ các nguồn
            }
            if (dictionaryControllerInstance != null) {
                dictionaryControllerInstance.resetScene(); // Reset Dictionary view
            }
            if (welcomeControllerInstance != null) {
                welcomeControllerInstance.resetView(); // Reset Welcome view
            }
            System.out.println("Ứng dụng đã đóng.");
        });
    }

    // *** Hàm xử lý khi WelcomeController báo hiệu tìm kiếm ***
    private void handleSearchInitiated(String searchTerm) {
        this.pendingActionWord = searchTerm; // Lưu từ khóa tìm kiếm
        this.pendingAddAction = false; // Không phải hành động thêm
        showDictionaryView(); // Chuyển sang màn hình từ điển
    }

    // *** Hàm xử lý khi WelcomeController báo hiệu thêm từ ***
    private void handleAddWordInitiated(String wordToAdd) {
        this.pendingActionWord = wordToAdd; // Lưu từ cần thêm
        this.pendingAddAction = true; // Là hành động thêm
        showDictionaryView(); // Chuyển sang màn hình từ điển
    }

    /**
     * Load màn hình Dictionary View (nếu chưa), thiết lập callback quay lại,
     * truyền DictionaryManagement, LƯU instance Controller.
     * Sau đó, kiểm tra pending actions (tìm kiếm hoặc thêm) và xử lý.
     */
    private void showDictionaryView() {
        try {
            if (this.dictionaryScene == null) {
                FXMLLoader dictionaryLoader = new FXMLLoader(getClass().getResource("/com/application/test/view/dictionary_view.fxml"));
                Parent dictionaryRoot = dictionaryLoader.load();
                if (this.pendingActionWord != null && !this.pendingActionWord.isEmpty()) {
                    this.dictionaryControllerInstance = dictionaryLoader.getController();
                    dictionaryControllerInstance.setDictionaryManagement(this.dictionaryManagement);
                    dictionaryControllerInstance.setOnGoBackToWelcome(this::showWelcomeView);

                    // *** Store the pending search term in the controller instance itself ***
                    // This transfers the data to the controller
                    dictionaryControllerInstance.setInitialSearchTerm(this.pendingActionWord);

                    this.dictionaryScene = new Scene(dictionaryRoot);
                }

                this.pendingActionWord = null;
                this.pendingAddAction = false;


                primaryStage.setScene(this.dictionaryScene); // Set the Dictionary scene
                primaryStage.setTitle("📚 Dictionary Lookup");
                System.out.println("Đã chuyển sang màn hình từ điển.");


            } else {
                this.pendingActionWord = null;
                this.pendingAddAction = false;

                primaryStage.setScene(this.dictionaryScene); // Set the Dictionary scene
                primaryStage.setTitle("📚 Dictionary Lookup");
                System.out.println("Đã chuyển sang màn hình từ điển.");
            }


        } catch (IOException e) { System.err.println("Lỗi khi load màn hình từ điển: " + e.getMessage()); e.printStackTrace(); /* ... */ }
    }

    private void showGameMenu() {
        try {
            if (this.gameMenuScene == null) { // Sử dụng gameMenuScene
                URL gameMenuFxmlUrl = getClass().getResource("/com/application/test/view/games.fxml"); // <-- File FXML Game Menu
                if (gameMenuFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy file games.fxml trong classpath!"); System.exit(1); }
                FXMLLoader gameMenuLoader = new FXMLLoader(gameMenuFxmlUrl);
                Parent gameMenuRoot = gameMenuLoader.load();
                this.gamesControllerInstance = gameMenuLoader.getController(); // Lưu instance GamesController

                gamesControllerInstance.setOnGoBackToWelcome(this::showWelcomeView);

                // *** Thiết lập callback launch game cụ thể cho GamesController ***
                gamesControllerInstance.setOnLaunchSpecificGame(this::showSpecificGame); // <-- Cần tạo hàm showSpecificGame

                this.gameMenuScene = new Scene(gameMenuRoot); // Sử dụng gameMenuScene
            }

            if (welcomeControllerInstance != null) { welcomeControllerInstance.resetView(); } // Reset Welcome view
            if (dictionaryControllerInstance != null) { dictionaryControllerInstance.resetScene(); } // Reset Dictionary view


            primaryStage.setScene(this.gameMenuScene); // Sử dụng gameMenuScene
            primaryStage.setTitle("Games"); // Tiêu đề cho màn hình Game Menu
            System.out.println("Đã chuyển sang màn hình game (Menu).");

        } catch (IOException e) { System.err.println("Lỗi khi load màn hình game menu: " + e.getMessage()); e.printStackTrace(); /* ... */ }
    }

    private void showSpecificGame(String gameId) {
        try {
            URL specificGameFxmlUrl;
            if ("wordle".equals(gameId)) {
                specificGameFxmlUrl = getClass().getResource("/com/application/test/view/wordle_view.fxml");
                if (specificGameFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy file wordle_view.fxml!"); System.exit(1); }
            } else {
                System.err.println("Game ID không hợp lệ: " + gameId);
                return;
            }


            if (this.wordleScene == null) { // Chỉ load Wordle FXML lần đầu
                FXMLLoader specificGameLoader = new FXMLLoader(specificGameFxmlUrl);
                Parent specificGameRoot = specificGameLoader.load();
                this.wordleControllerInstance = specificGameLoader.getController();

                // *** Thiết lập callback quay lại Game Menu cho WordleController ***
                wordleControllerInstance.setOnGoBackToGames(this::showGameMenu);
                System.out.println("setOnGoBackToGames called on WordleController instance.");

                this.wordleScene = new Scene(specificGameRoot); // Sử dụng wordleScene
            } else {
                if (wordleControllerInstance != null) {
                    wordleControllerInstance.resetGame();
                }
            }

            // *** Reset trạng thái màn hình trước khi chuyển ***
            if (welcomeControllerInstance != null) { welcomeControllerInstance.resetView(); }
            if (dictionaryControllerInstance != null) { dictionaryControllerInstance.resetScene(); }

            // Set the specific game Scene (Wordle) on the primary stage
            primaryStage.setScene(this.wordleScene); // Sử dụng wordleScene
            primaryStage.setTitle("Wordle Game"); // Tiêu đề cho màn hình Wordle
            System.out.println("Đã chuyển sang màn hình game: " + gameId);

        } catch (IOException e) { System.err.println("Lỗi khi load màn hình game cụ thể: " + e.getMessage()); e.printStackTrace(); /* ... */ }
    }

    private void showThesaurusView() {
        try {
            if (this.thesaurusScene == null) {
                URL thesaurusFxmlUrl = getClass().getResource("/com/application/test/view/thesaurus.fxml"); // <-- File FXML Game Menu
                if (thesaurusFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy file thesaurus.fxml trong classpath!"); System.exit(1); }
                FXMLLoader thesaurusLoader = new FXMLLoader(thesaurusFxmlUrl);
                Parent thesaurusRoot = thesaurusLoader.load();
                this.thesaurusControllerInstance = thesaurusLoader.getController();

                thesaurusControllerInstance.setOnGoBackToWelcome(this::showWelcomeView);
                this.thesaurusScene = new Scene(thesaurusRoot);
            } else {
                thesaurusControllerInstance.resetScene();
            }

            if (welcomeControllerInstance != null) { welcomeControllerInstance.resetView(); } // Reset Welcome view
            if (dictionaryControllerInstance != null) { dictionaryControllerInstance.resetScene(); } // Reset Dictionary view


            primaryStage.setScene(this.thesaurusScene);
            primaryStage.setTitle("Thesaurus");
            System.out.println("Đã chuyển sang màn hình Thesaurus (Menu).");

        } catch (IOException e) { System.err.println("Lỗi khi load màn hình thesaurus menu: " + e.getMessage()); e.printStackTrace(); /* ... */ }
    }

    /**
     * Chuyển về màn hình Welcome View.
     */
    private void showWelcomeView() {
        if (this.welcomeScene != null) {
            primaryStage.setScene(this.welcomeScene);
            primaryStage.setTitle("Ứng dụng Từ điển");
            if (welcomeControllerInstance != null) {
                welcomeControllerInstance.resetView();
            } else {
                System.err.println("WelcomeController instance is null. Cannot reset Welcome scene.");
            }
        } else {
            System.err.println("Welcome scene chưa được tạo!");
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
