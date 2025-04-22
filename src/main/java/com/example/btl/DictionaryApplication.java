package com.example.btl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class DictionaryApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load file FXML
        // Sử dụng getResource() để load từ classpath (thư mục resources)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/btl/dictionary_view.fxml")); // Đường dẫn tương đối trong package
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Ứng dụng Từ điển Anh-Việt");
        stage.setScene(scene);
        stage.show();

        // Tùy chọn: Xử lý sự kiện khi đóng cửa sổ để lưu dữ liệu
        stage.setOnCloseRequest(event -> {
            System.out.println("Đang đóng ứng dụng. Lưu dữ liệu...");
            // Lấy Controller instance để gọi hàm lưu
            // DictionaryController controller = fxmlLoader.getController();
            // if (controller != null && controller.getDictionaryManagement() != null) {
            // controller.getDictionaryManagement().saveDataToFile(); // Cần thêm getter cho DictionaryManagement
            // }
            // hoặc gọi trực tiếp logic lưu file nếu không phụ thuộc vào controller state
            // DictionaryManagement tempMgmt = new DictionaryManagement(new Dictionary()); // Tạo instance tạm
            // tempMgmt.saveDataToFile(); // Lưu ý: cách này có thể không lưu được thay đổi nếu logic phức tạp
            // Cách tốt nhất là controller quản lý việc lưu khi thoát.
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
