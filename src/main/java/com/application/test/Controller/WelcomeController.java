package com.application.test.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Giữ lại nếu bạn muốn dùng initialize
import javafx.event.ActionEvent;
// import các import không cần thiết cho việc load FXML nếu bạn xóa logic đó
// import javafx.scene.Parent;
// import javafx.scene.Scene;
// import javafx.scene.Node;
// import javafx.stage.Stage;

import java.net.URL;       // Cần cho Initializable
import java.util.ResourceBundle; // Cần cho Initializable


public class WelcomeController implements Initializable {

    private Runnable onGoToDictionary; // Callback khi muốn chuyển sang màn hình từ điển

    // Setter cho callback
    public void setOnGoToDictionary(Runnable onGoToDictionary) {
        this.onGoToDictionary = onGoToDictionary;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: Khởi tạo ban đầu cho màn hình Welcome nếu cần (ví dụ: hiệu ứng)
        System.out.println("WelcomeController initialized.");
    }


    @FXML
    protected void handleSearchAction(ActionEvent event) {
        System.out.println("Search button clicked (from Welcome). Signalling to go to Dictionary View.");
        // Khi người dùng nhấn nút "Search", gọi callback để báo hiệu cho DictionaryApplication
        if (onGoToDictionary != null) {
            // Callback có thể ném Exception, nên cần bọc trong try-catch nếu Runnable.run() ném Exception
            try {
                onGoToDictionary.run();
            } catch (RuntimeException e) { // Runnable.run() chỉ ném RuntimeException
                System.err.println("Lỗi khi thực hiện callback chuyển màn hình: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoToDictionary chưa được thiết lập!");
        }
    }

    // Các phương thức xử lý sự kiện khác cho các nút khác
    // Các nút này có thể gọi các callback khác nếu chúng dẫn đến các màn hình khác
    // Hoặc nếu chúng vẫn dẫn đến màn hình từ điển (như Vie-Eng), bạn có thể gọi onGoToDictionary.run()
    @FXML
    protected void handleEngVie(ActionEvent event) {
        System.out.println("Navigating to English-Vietnamese...");
        // Nếu Eng-Vie cũng là màn hình Dictionary chính
        if (onGoToDictionary != null) {
            try { onGoToDictionary.run(); } catch (RuntimeException e) { e.printStackTrace(); }
        }
    }

    @FXML
    protected void handleVieEng(ActionEvent event) {
        System.out.println("Navigating to Vietnamese-English...");

    }

    @FXML
    protected void handleSentenceTranslation(ActionEvent event) {
        System.out.println("Sentence Translation clicked.");
        // TODO: Nếu có màn hình dịch câu riêng, gọi callback khác hoặc load Stage/Scene mới
    }

    @FXML
    protected void handleThesaurus(ActionEvent event) {
        System.out.println("Thesaurus clicked.");
        // TODO: Nếu có màn hình từ đồng nghĩa riêng, gọi callback khác hoặc load Stage/Scene mới
    }

    @FXML
    protected void handleGrammar(ActionEvent event) {
        System.out.println("Grammar clicked.");
        // TODO: Nếu có màn hình ngữ pháp riêng, gọi callback khác hoặc load Stage/Scene mới
    }

    @FXML
    protected void handleEditWords(ActionEvent event) {
        System.out.println("Edit Words clicked.");
        // TODO: Nếu Sửa từ dẫn đến màn hình khác hoặc Dialog, gọi callback khác hoặc mở Stage/Dialog mới
        // Nếu bạn tích hợp sửa từ trong màn hình Dictionary chính, thì logic này có thể gọi onGoToDictionary
        if (onGoToDictionary != null) {
            try { onGoToDictionary.run(); } catch (RuntimeException e) { e.printStackTrace(); }
        }
    }

    @FXML
    protected void handleGames(ActionEvent event) {
        System.out.println("Games clicked.");
        // TODO: Nếu có màn hình game riêng, gọi callback khác hoặc load Stage/Scene mới
    }

}