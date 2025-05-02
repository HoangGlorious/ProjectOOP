package com.application.test;

import com.application.test.Controller.DictionaryController;
import com.application.test.Controller.WelcomeController;
import com.application.test.Model.GeneralManagement;
import com.application.test.Model.DictionarySource;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;


import java.io.IOException;
import java.net.URL;

public class DictionaryApplication extends Application {

    private Stage primaryStage;
    private GeneralManagement dictionaryManagement;
    private Scene welcomeScene;
    private Scene dictionaryScene;
    private WelcomeController welcomeControllerInstance;
    private DictionaryController dictionaryControllerInstance;
    private String pendingActionWord = null;
    private boolean pendingAddAction = false;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        stage.setTitle("Ứng dụng Từ điển");

        // *** Khởi tạo DictionaryManager và nạp dữ liệu cho TẤT CẢ các nguồn ***
        this.dictionaryManagement = new GeneralManagement();
        dictionaryManagement.loadAllSourcesData(); // Nạp dữ liệu cho tất cả sources

        // --- Load màn hình Welcome ---
        URL welcomeFxmlUrl = getClass().getResource("/com/application/test/view/welcome.fxml"); // Kiểm tra lại đường dẫn
        if (welcomeFxmlUrl == null) { System.err.println("Lỗi: Không tìm thấy file welcome.fxml trong classpath!"); System.exit(1); }
        FXMLLoader welcomeLoader = new FXMLLoader(welcomeFxmlUrl);
        Parent welcomeRoot = welcomeLoader.load();
        WelcomeController welcomeController = welcomeLoader.getController();
        welcomeController.setDictionaryManagement(this.dictionaryManagement);
        welcomeController.setOnSearchInitiated(this::handleSearchInitiated);
        welcomeController.setOnAddWordInitiated(this::handleAddWordInitiated);
        // TODO: Thiết lập callback cho các nút khác nếu chúng dẫn đến màn hình/chức năng khác


        this.welcomeScene = new Scene(welcomeRoot);
        stage.setScene(welcomeScene);
        stage.show();

        // Xử lý sự kiện khi đóng cửa sổ
        stage.setOnCloseRequest(event -> {
            System.out.println("Đang đóng ứng dụng...");
            if (dictionaryManagement != null) {
                dictionaryManagement.saveAllSourcesData(); // Lưu dữ liệu cho TẤT CẢ các nguồn
            }
            if (dictionaryControllerInstance != null) {
                dictionaryControllerInstance.resetScene(); // Reset Dictionary view
            }
            // *** Call resetView on WelcomeController instance before closing ***
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
                this.dictionaryControllerInstance = dictionaryLoader.getController();

                // Truyền instance DictionaryManagement
                dictionaryControllerInstance.setDictionaryManagement(this.dictionaryManagement);

                // Thiết lập callback quay lại
                dictionaryControllerInstance.setOnGoBackToWelcome(this::showWelcomeView);

                this.dictionaryScene = new Scene(dictionaryRoot);
            }

            if (welcomeControllerInstance != null) {
                welcomeControllerInstance.resetView();
            } else {
                System.err.println("WelcomeController instance is null. Cannot reset Welcome scene.");
            }

            // Thay thế Scene hiện tại bằng Scene từ điển
            primaryStage.setScene(this.dictionaryScene);
            primaryStage.setTitle("📚 Dictionary Lookup");
            System.out.println("Đã chuyển sang màn hình từ điển.");

            // *** Sau khi chuyển Scene, xử lý các pending actions ***
            if (this.pendingAddAction) {
                if (this.pendingActionWord != null && !this.pendingActionWord.isEmpty()) {
                    Stage currentStage = (Stage) primaryStage.getScene().getWindow();
                    dictionaryControllerInstance.initiateAddWordDialog(this.pendingActionWord, primaryStage);
                }
            } else {
                // Nếu không phải add action, thì có thể là search hoặc chỉ chuyển màn hình
                // Dù có pendingActionWord hay không, gọi setSearchText để cập nhật search field
                // và trigger logic hiển thị ban đầu hoặc search/gợi ý
                dictionaryControllerInstance.setSearchText(this.pendingActionWord != null ? this.pendingActionWord : "");
            }

            // Reset pending actions (đã xử lý trong setSearchText nếu text rỗng)
            this.pendingActionWord = null;
            this.pendingAddAction = false;

        } catch (IOException e) {
            System.err.println("Lỗi khi load màn hình từ điển: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Chuyển về màn hình Welcome View.
     */
    private void showWelcomeView() {
        if (this.welcomeScene != null) {
            primaryStage.setScene(this.welcomeScene);
            primaryStage.setTitle("Ứng dụng Từ điển"); // Đổi lại tiêu đề
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
