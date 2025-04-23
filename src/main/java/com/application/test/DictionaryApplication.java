package com.application.test;

import com.application.test.Controller.DictionaryController;
import com.application.test.Controller.WelcomeController;
import com.application.test.Model.Dictionary;
import com.application.test.Model.DictionaryManagement;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;


import java.io.IOException;

public class DictionaryApplication extends Application {

    private Stage primaryStage;
    private DictionaryManagement dictionaryManagement;
    private Scene welcomeScene;
    private Scene dictionaryScene;

    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        stage.setTitle("Ứng dụng Từ điển"); // Tiêu đề chung ban đầu

        this.dictionary = new Dictionary(); // Giả sử Dictionary là public class
        this.dictionaryManagement = new DictionaryManagement(this.dictionary);
        dictionaryManagement.insertFromFile(); // Nạp dữ liệu từ file


        // --- Load màn hình Welcome ---
        FXMLLoader welcomeLoader = new FXMLLoader(getClass().getResource("/com/application/test/welcome.fxml"));
        Parent welcomeRoot = welcomeLoader.load();
        WelcomeController welcomeController = welcomeLoader.getController();

        // Tạo Scene cho màn hình welcome và hiển thị
        this.welcomeScene = new Scene(welcomeRoot);
        welcomeController.setOnGoToDictionary(this::showDictionaryView);
        stage.setScene(welcomeScene);
        stage.show();

        // Tùy chọn: Xử lý sự kiện khi đóng cửa sổ chính để lưu dữ liệu
        stage.setOnCloseRequest(event -> {
            System.out.println("Đang đóng ứng dụng. Lưu dữ liệu...");
            if (dictionaryManagement != null) {
                dictionaryManagement.saveDataToFile();
            }
        });
    }

    /**
     * Load màn hình Dictionary View và thay thế Scene hiện tại của Stage chính.
     */
    private void showDictionaryView() {
        try {
            if (this.dictionaryScene == null) {
                // Load màn hình Dictionary View
                FXMLLoader dictionaryLoader = new FXMLLoader(getClass().getResource("/com/application/test/dictionary_view.fxml"));
                Parent dictionaryRoot = dictionaryLoader.load();

                DictionaryController dictionaryController = dictionaryLoader.getController();

                // *** Truyền instance DictionaryManagement đã nạp dữ liệu cho DictionaryController ***
                dictionaryController.setDictionaryManagement(this.dictionaryManagement);

                dictionaryController.setOnGoBackToWelcome(this::showWelcomeView);

                this.dictionaryScene = new Scene(dictionaryRoot);
            }

            primaryStage.setScene(this.dictionaryScene);
            primaryStage.setTitle("📚 Tìm kiếm từ điển"); // Đổi tiêu đề cửa sổ

            // Stage đã được hiển thị, không cần gọi primaryStage.show() nữa
            System.out.println("Đã chuyển sang màn hình từ điển.");

        } catch (IOException e) {
            System.err.println("Lỗi khi load màn hình từ điển: " + e.getMessage());
            e.printStackTrace();
            // TODO: Hiển thị Alert lỗi nghiêm trọng và thoát ứng dụng
        }
    }

    private void showWelcomeView() {
        if (this.welcomeScene != null) {
            primaryStage.setScene(this.welcomeScene);
            primaryStage.setTitle("Ứng dụng Từ điển");
            System.out.println("Đã quay lại welcome.");
        }
    }


    public static void main(String[] args) {
        launch();
    }

    private Dictionary dictionary;
}
