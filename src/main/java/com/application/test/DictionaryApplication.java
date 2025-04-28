package com.application.test;

import com.application.test.Controller.DictionaryController;
import com.application.test.Controller.WelcomeController;
import com.application.test.Model.DictionaryManagement;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;


import java.io.IOException;
import java.net.URL;

public class DictionaryApplication extends Application {

    private Stage primaryStage;
    private DictionaryManagement dictionaryManagement;
    private Scene welcomeScene;
    private Scene dictionaryScene;
    private DictionaryController dictionaryControllerInstance;
    private String pendingActionWord = null;
    private boolean pendingAddAction = false;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        stage.setTitle("Ứng dụng Từ điển");

        // --- Khởi tạo và nạp dữ liệu từ điển ---
        this.dictionaryManagement = new DictionaryManagement();
        dictionaryManagement.loadDataFromFile();

        // --- Load màn hình Welcome ---
        URL welcomeFxmlUrl = getClass().getResource("/com/application/test/view/welcome.fxml"); // <-- Lấy URL của resource
        if (welcomeFxmlUrl == null) {
            System.err.println("Lỗi: Không tìm thấy file welcome.fxml trong classpath!");
            System.exit(1);
        }

        FXMLLoader welcomeLoader = new FXMLLoader(welcomeFxmlUrl); // <-- Truyền URL vào constructor

        Parent welcomeRoot = welcomeLoader.load();
        WelcomeController welcomeController = welcomeLoader.getController();

        // Truyền DictionaryManagement cho WelcomeController
        welcomeController.setDictionaryManagement(this.dictionaryManagement);

        // *** Thiết lập các callbacks cho WelcomeController ***
        welcomeController.setOnSearchInitiated(this::handleSearchInitiated); // Khi WelcomeController báo search
        welcomeController.setOnAddWordInitiated(this::handleAddWordInitiated); // Khi WelcomeController báo add từ


        // Tạo Scene và lưu trữ nó
        this.welcomeScene = new Scene(welcomeRoot);

        // Gán Scene welcome và hiển thị Stage ban đầu
        stage.setScene(welcomeScene);
        stage.show();

        // Xử lý sự kiện khi đóng cửa sổ
        stage.setOnCloseRequest(event -> {
            System.out.println("Đang đóng ứng dụng...");
            if (dictionaryManagement != null) {
                dictionaryManagement.saveDataToFile();
            }
            /** if (dictionaryControllerInstance != null) {
                dictionaryControllerInstance.shutdownTTS();
            }*/
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

            // Thay thế Scene hiện tại bằng Scene từ điển
            primaryStage.setScene(this.dictionaryScene);
            primaryStage.setTitle("📚 Dictionary Lookup");
            System.out.println("Đã chuyển sang màn hình từ điển.");

            // *** Sau khi chuyển Scene, xử lý các pending actions ***
            if (this.pendingAddAction) { // Nếu là hành động thêm từ
                if (this.pendingActionWord != null && !this.pendingActionWord.isEmpty()) {
                    // Báo hiệu cho DictionaryController mở dialog thêm từ với từ khóa ban đầu
                    dictionaryControllerInstance.initiateAddWordDialog(this.pendingActionWord); // Cần tạo hàm này trong DictionaryController
                }
            } else if (this.pendingActionWord != null && !this.pendingActionWord.isEmpty()) { // Nếu là hành động tìm kiếm
                // Báo hiệu cho DictionaryController thực hiện tìm kiếm
                dictionaryControllerInstance.performSearch(this.pendingActionWord); // Hàm này đã có/sẽ sửa
            } else {
                // Nếu không có pending action (ví dụ: chuyển từ welcome bằng nút khác không phải search)
                // Có thể hiển thị toàn bộ từ điển hoặc để trống tùy ý
                dictionaryControllerInstance.loadAndDisplayInitialData(); // Hiển thị toàn bộ ban đầu
            }

            // Reset pending actions sau khi xử lý
            this.pendingActionWord = null;
            this.pendingAddAction = false;


        } catch (IOException e) {
            System.err.println("Lỗi khi load màn hình từ điển: " + e.getMessage());
            e.printStackTrace();
            // TODO: Hiển thị Alert lỗi nghiêm trọng và thoát ứng dụng
        }
    }

    /**
     * Chuyển về màn hình Welcome View.
     */
    private void showWelcomeView() {
        if (this.welcomeScene != null) {
            primaryStage.setScene(this.welcomeScene);
            primaryStage.setTitle("Ứng dụng Từ điển"); // Đổi lại tiêu đề

            // TODO: Reset trạng thái màn hình Welcome (xóa search text, ẩn gợi ý)
            // Bạn cần lấy WelcomeController instance và gọi hàm reset.
            // Cần lưu WelcomeController instance giống như DictionaryControllerInstance
            // welcomeControllerInstance.resetView(); // Cần tạo và gọi hàm này
            System.out.println("Đã quay lại màn hình welcome.");
        } else {
            System.err.println("Welcome scene chưa được tạo!");
        }
    }


    public static void main(String[] args) {
        launch();
    }
}
