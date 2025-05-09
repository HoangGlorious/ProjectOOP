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
    private Scene senTranScene;
    private WelcomeController welcomeControllerInstance;
    private DictionaryController dictionaryControllerInstance;
    private ThesaurusController thesaurusControllerInstance;
    private SenTransController senTransControllerInstance;
    private GamesController gamesControllerInstance;
    private WordleController wordleControllerInstance;
    private String pendingActionWord = null;
    private boolean pendingAddAction = false;
    private Scene wordleMenuScene;
    private Scene dailyWordleScene;
    private WordleMenuController wordleMenuControllerInstance;
    private DailyWordleController dailyWordleControllerInstance;
    private Scene grammarScene;
    private GrammarController grammarControllerInstance;
    public GeneralManagement getDictionaryManagement() {
        return dictionaryManagement;
    }
    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        stage.setUserData(this);
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
        welcomeController.setOnGoToSentenceTranslation(this::showSenTranView);
        welcomeController.setOnGoToGrammar(this::showGrammarView);

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
            if (this.gameMenuScene == null) {
                URL gameMenuFxmlUrl = getClass().getResource("/com/application/test/view/games.fxml");
                if (gameMenuFxmlUrl == null) {
                    System.err.println("Lỗi: Không tìm thấy file games.fxml trong classpath!");
                    // Có thể hiển thị Alert cho người dùng hoặc quay lại welcome
                    showWelcomeView();
                    return;
                }
                FXMLLoader gameMenuLoader = new FXMLLoader(gameMenuFxmlUrl);
                Parent gameMenuRoot = gameMenuLoader.load();
                this.gamesControllerInstance = gameMenuLoader.getController();

                if (this.gamesControllerInstance != null) {
                    gamesControllerInstance.setOnGoBackToWelcome(this::showWelcomeView);
                    // Khi chọn một game từ menu chính, sẽ đi đến menu con của game đó (nếu có)
                    // Ở đây, giả sử "wordle" là game duy nhất có menu con
                    gamesControllerInstance.setOnLaunchSpecificGame(gameId -> {
                        if ("wordle".equalsIgnoreCase(gameId)) {
                            showSpecificGameMenu("wordle"); // Gọi menu con của Wordle
                        } else {
                            // Xử lý các game khác không có menu con (nếu có)
                            // showSpecificGame(gameId); // Hoặc đi thẳng vào game
                            System.err.println("Game ID không được hỗ trợ cho menu con: " + gameId);
                        }
                    });
                } else {
                    System.err.println("Lỗi: gamesControllerInstance is null!");
                }
                this.gameMenuScene = new Scene(gameMenuRoot);
            }
            // Không gọi resetOtherViews ở đây theo yêu cầu của bạn
            primaryStage.setScene(this.gameMenuScene);
            primaryStage.setTitle("🎮 Games Menu");
            System.out.println("Đã chuyển sang Games Menu.");

        } catch (IOException e) {
            System.err.println("Lỗi khi load Games Menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSpecificGameMenu(String gameType) { // gameType ví dụ: "wordle"
        if (!"wordle".equalsIgnoreCase(gameType)) {
            System.err.println("Loại game menu không được hỗ trợ: " + gameType + ". Quay lại Game Menu.");
            showGameMenu();
            return;
        }
        try {
            if (this.wordleMenuScene == null) {
                URL menuFxmlUrl = getClass().getResource("/com/application/test/view/wordlemenu.fxml");
                if (menuFxmlUrl == null) {
                    System.err.println("Lỗi: Không tìm thấy file wordlemenu.fxml!");
                    showGameMenu(); // Quay lại menu trước đó nếu lỗi
                    return;
                }
                FXMLLoader menuLoader = new FXMLLoader(menuFxmlUrl);
                Parent menuRoot = menuLoader.load();
                this.wordleMenuControllerInstance = menuLoader.getController();

                if (this.wordleMenuControllerInstance != null) {
                    wordleMenuControllerInstance.setOnGoBackToGames(this::showGameMenu); // Quay lại Games Menu
                    wordleMenuControllerInstance.setOnLaunchWordle(() -> showSpecificGame("wordle"));       // Chơi Wordle Thường
                    wordleMenuControllerInstance.setOnLaunchDailyWordle(() -> showSpecificGame("daily_wordle")); // Chơi Daily Wordle
                } else {
                    System.err.println("Lỗi: wordleMenuControllerInstance is null!");
                }
                this.wordleMenuScene = new Scene(menuRoot);
            }
            // Không gọi resetOtherViews
            primaryStage.setScene(this.wordleMenuScene);
            primaryStage.setTitle("Wordle Menu");
            System.out.println("Đã chuyển sang Wordle Menu.");

        } catch (IOException e) {
            System.err.println("Lỗi khi load Wordle Menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSpecificGame(String gameId) {
        try {
            String fxmlFile = null;
            String title = "Game";
            Scene targetScene = null;
            Object controllerInstance = null; // Dùng Object để linh hoạt

            if ("wordle".equalsIgnoreCase(gameId)) {
                fxmlFile = "/com/application/test/view/wordle_view.fxml";
                title = "Wordle Game";
                if (this.wordleScene == null) {
                    URL gameFxmlUrl = getClass().getResource(fxmlFile);
                    if (gameFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy " + fxmlFile + "!"); return; }
                    FXMLLoader loader = new FXMLLoader(gameFxmlUrl);
                    Parent root = loader.load();
                    this.wordleControllerInstance = loader.getController();
                    this.wordleScene = new Scene(root);
                }
                targetScene = this.wordleScene;
                controllerInstance = this.wordleControllerInstance;

            } else if ("daily_wordle".equalsIgnoreCase(gameId)) {
                fxmlFile = "/com/application/test/view/daily_wordle_view.fxml";
                title = "Daily Wordle Game";
                if (this.dailyWordleScene == null) {
                    URL gameFxmlUrl = getClass().getResource(fxmlFile);
                    if (gameFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy " + fxmlFile + "!"); return; }
                    FXMLLoader loader = new FXMLLoader(gameFxmlUrl);
                    Parent root = loader.load();
                    this.dailyWordleControllerInstance = loader.getController();
                    this.dailyWordleScene = new Scene(root);
                }
                targetScene = this.dailyWordleScene;
                controllerInstance = this.dailyWordleControllerInstance;

            } else {
                System.err.println("Game ID không hợp lệ: " + gameId);
                showGameMenu(); // Quay lại menu game nếu ID không đúng
                return;
            }

            // Thiết lập callback "Back" và reset game
            if (controllerInstance instanceof WordleController) { // Áp dụng cho WordleController và DailyWordleController
                WordleController wc = (WordleController) controllerInstance;
                // Nút back trong game sẽ quay lại Wordle Menu
                wc.setOnGoBackToMenu(() -> showSpecificGameMenu("wordle"));
                wc.resetGame(); // Reset game mỗi khi vào màn hình
                System.out.println("Đã reset và thiết lập callback back cho " + gameId);
            } else {
                System.err.println("Lỗi: Controller instance không phải là WordleController hoặc null cho gameId: " + gameId);
                if (controllerInstance == null) System.err.println("Controller instance thực sự là NULL.");
                else System.err.println("Actual controller type: " + controllerInstance.getClass().getName());
                // Có thể quay lại menu trước nếu controller không đúng
                // showSpecificGameMenu("wordle");
                // return;
            }


            if (targetScene == null) {
                System.err.println("Lỗi: targetScene là null, không thể chuyển màn hình cho " + gameId);
                return;
            }
            // Không gọi resetOtherViews
            primaryStage.setScene(targetScene);
            primaryStage.setTitle(title);
            System.out.println("Đã chuyển sang màn chơi " + title + ".");

        } catch (IOException e) {
            System.err.println("IOException when loading Game (" + gameId + "): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) { // Bắt cả các lỗi khác
            System.err.println("Unexpected error when loading Game (" + gameId + "): " + e.getMessage());
            e.printStackTrace();
        }
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
    private void showGrammarView() {
        try {
            if (this.grammarScene == null) {
                URL grammarFxmlUrl = getClass().getResource("/com/application/test/view/grammar_view.fxml");
                if (grammarFxmlUrl == null) {
                    System.err.println("Lỗi: Không tìm thấy file grammar_view.fxml trong classpath!");
                    showWelcomeView();
                    return;
                }
                FXMLLoader grammarLoader = new FXMLLoader(grammarFxmlUrl);
                Parent grammarRoot = grammarLoader.load();
                this.grammarControllerInstance = grammarLoader.getController();
                grammarControllerInstance.setOnGoBackToWelcome(unused -> showWelcomeView());
                this.grammarScene = new Scene(grammarRoot);
                URL cssUrl = getClass().getResource("/com/application/test/CSS/grammar.css");
                if (cssUrl != null) {
                    this.grammarScene.getStylesheets().add(cssUrl.toExternalForm());
                } else {
                    System.err.println("Lỗi: Không tìm thấy file grammar.css tại /com/application/test/CSS/grammar.css");
                }
            }
            if (welcomeControllerInstance != null) {
                welcomeControllerInstance.resetView();
            }
            if (dictionaryControllerInstance != null) {
                dictionaryControllerInstance.resetScene();
            }
            primaryStage.setScene(this.grammarScene);
            primaryStage.setTitle("📘 Grammar");
            System.out.println("Đã chuyển sang màn hình Grammar.");
        } catch (IOException e) {
            System.err.println("Lỗi khi load màn hình Grammar: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showSenTranView() {
        try {
            if (this.senTranScene == null) {
                URL senTranFxmlUrl = getClass().getResource("/com/application/test/view/sentence_translate.fxml");
                if (senTranFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy file sentence_translate.fxml trong classpath!"); System.exit(1); }
                FXMLLoader senLoader = new FXMLLoader(senTranFxmlUrl);
                Parent senRoot = senLoader.load();
                this.senTransControllerInstance = senLoader.getController();

                senTransControllerInstance.setOnGoBackToWelcome(this::showWelcomeView);
                this.senTranScene = new Scene(senRoot);
            } else {
                senTransControllerInstance.resetScene();
            }

            if (welcomeControllerInstance != null) { welcomeControllerInstance.resetView(); } // Reset Welcome view
            if (dictionaryControllerInstance != null) { dictionaryControllerInstance.resetScene(); } // Reset Dictionary view


            primaryStage.setScene(this.senTranScene);
            primaryStage.setTitle("Sentence Translator");
            System.out.println("Đã chuyển sang màn hình SenTran.");

        } catch (IOException e) { System.err.println("Lỗi khi load màn hình SenTran menu: " + e.getMessage()); e.printStackTrace(); /* ... */ }
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
