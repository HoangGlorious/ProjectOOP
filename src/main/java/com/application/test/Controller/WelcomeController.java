package com.application.test.Controller;

import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.GeneralManagement;
import com.application.test.Model.DictionarySource;

import javafx.scene.control.ComboBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Consumer;
import com.application.test.Model.DictionaryEntry;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;

import java.io.IOException;


public class WelcomeController implements Initializable {

    @FXML private StackPane mainPane;
    @FXML private TextField welcomeSearchTextField;
    @FXML private Button welcomeSearchButton;
    @FXML private ListView<String> suggestionListView;
    @FXML private Label welcomeLabel;
    @FXML private ImageView backgroundImageView;
    @FXML private ImageView icon1;

    // Callbacks để báo hiệu cho DictionaryApplication
    private Consumer<String> onSearchInitiated; // Callback khi người dùng tìm kiếm từ tồn tại
    private Consumer<String> onAddWordInitiated; // Callback khi người dùng muốn thêm từ (từ thông báo lỗi)
    private GeneralManagement dictionaryManagement;
    private Runnable onGoToGame;

    public void setOnGoToGame(Runnable onGoToGame) {
        this.onGoToGame = onGoToGame;
    }

    public void setOnSearchInitiated(Consumer<String> onSearchInitiated) {
        this.onSearchInitiated = onSearchInitiated;
    }

    public void setOnAddWordInitiated(Consumer<String> onAddWordInitiated) {
        this.onAddWordInitiated = onAddWordInitiated;
    }

    public void setDictionaryManagement(GeneralManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("WelcomeController initialized.");
        backgroundImageView.setImage(new Image(getClass().getResource("/com/application/test/images/testing.png").toExternalForm()));
        icon1.setImage(new Image(getClass().getResource("/com/application/test/images/icon1.png").toExternalForm()));
        welcomeSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            showSuggestions(newValue);
        });

        suggestionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Khi click vào gợi ý, đặt text vào search field và thực hiện tìm kiếm
                welcomeSearchTextField.setText(newValue);
                suggestionListView.setVisible(false);
                suggestionListView.setManaged(false);
                handleWelcomeSearchAction(null);
            }
        });
        suggestionListView.setFocusTraversable(false);
        // TODO: Khởi tạo UI chọn nguồn (ví dụ ComboBox) với danh sách từ dictionaryManager.getAvailableSourceDisplayNames()
        // TODO: Đặt listener cho ComboBox để gọi dictionaryManager.setActiveSource()
    }

    public void resetView() {
        System.out.println("Resetting Welcome scene UI...");
        // Clear the search bar text
        if (welcomeSearchTextField != null) {
            welcomeSearchTextField.clear();
        }

        // Hide and clear the suggestion list
        if (suggestionListView != null) {
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
            suggestionListView.getItems().clear(); // Clear items in the suggestion list
        }
    }


    /**
     * Hiển thị danh sách gợi ý dựa trên tiền tố trong search text field.
     * @param prefix Tiền tố để tìm gợi ý.
     */
    private void showSuggestions(String prefix) {
        if (dictionaryManagement == null || prefix == null || prefix.trim().isEmpty()) {
            // Ẩn gợi ý nếu không có tiền tố hoặc dictionaryManagement chưa sẵn sàng
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
            return;
        }

        DictionarySource activeSource = this.dictionaryManagement.getActiveSource();
        List<DictionaryEntry> suggestions = activeSource.searchEntriesByPrefix(prefix);

        // Chuyển List<DictionaryEntry> thành List<String> (chỉ headword)
        List<String> suggestionHeadwords = suggestions.stream()
                .map(DictionaryEntry::getHeadword)
                .collect(Collectors.toList());

        // Cập nhật ListView gợi ý
        ObservableList<String> suggestionObservableList = FXCollections.observableArrayList(suggestionHeadwords);
        suggestionListView.setItems(suggestionObservableList);

        // Hiển thị hoặc ẩn ListView tùy thuộc vào số lượng gợi ý
        if (!suggestionHeadwords.isEmpty()) {
            suggestionListView.setVisible(true);
            suggestionListView.setManaged(true);
            suggestionListView.setMaxHeight(suggestionHeadwords.size() * 8);
        } else {
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
        }
    }


    // *** Phương thức xử lý sự kiện khi nhấn Enter trong TextField hoặc click nút Search ***
    @FXML
    protected void handleWelcomeSearchAction(ActionEvent event) {
        String searchTerm = welcomeSearchTextField.getText().trim();
        if (dictionaryManagement == null) { /* ... lỗi ... */ return; }

        // *** Lấy nguồn từ điển đang hoạt động và lookup trên nguồn đó ***
        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        Optional<DictionaryEntry> foundEntry = activeSource.lookupEntry(searchTerm);

        if (foundEntry.isPresent()) {
            // Báo hiệu cho DictionaryApplication để chuyển sang màn hình từ điển và hiển thị từ này
            if (onSearchInitiated != null) { onSearchInitiated.accept(searchTerm); } else { /* ... lỗi ... */ }
        } else {
            // Hiển thị thông báo không tìm thấy và hỏi thêm từ
            showNotFoundAlertWithAddOption(searchTerm); // Hàm này gọi onAddWordInitiated
        }
    }

    private void showNotFoundAlertWithAddOption(String notFoundWord) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Từ không tồn tại");
        alert.setHeaderText(null); // Bỏ HeaderText mặc định
        alert.setContentText("Từ '" + notFoundWord + "' không có trong từ điển.");

        // Thêm các ButtonType tùy chỉnh
        ButtonType addButtonType = new ButtonType("Thêm từ này");
        ButtonType cancelButtonType = new ButtonType("Đóng");

        alert.getButtonTypes().setAll(addButtonType, cancelButtonType);

        // Hiển thị alert và chờ phản hồi của người dùng
        Optional<ButtonType> result = alert.showAndWait();

        // Xử lý phản hồi
        if (result.isPresent() && result.get() == addButtonType) {
            System.out.println("Người dùng muốn thêm từ '" + notFoundWord + "'. Báo hiệu cho DictionaryApplication.");
            // Báo hiệu cho DictionaryApplication để chuyển sang màn hình từ điển và mở dialog thêm từ
            if (onAddWordInitiated != null) {
                onAddWordInitiated.accept(notFoundWord); // Truyền từ cần thêm qua callback
            } else {
                System.err.println("Callback onAddWordInitiated chưa được thiết lập!");
                // Tùy chọn: Chuyển màn hình dictionary view mà không làm gì khác
                // if (onGoToDictionary != null) { onGoToDictionary.run(); }
            }
        } else {
            // Người dùng chọn "Đóng" hoặc đóng alert, không làm gì thêm
            System.out.println("Người dùng không muốn thêm từ.");
        }
    }


    // Các phương thức xử lý sự kiện khác cho các nút khác
    // Các nút này có thể gọi các callback khác nếu chúng dẫn đến các màn hình khác
    // Hoặc nếu chúng vẫn dẫn đến màn hình từ điển (như Vie-Eng), bạn có thể gọi onGoToDictionary.run()
    @FXML
    protected void handleEngVie(ActionEvent event) {
        System.out.println("Chuyển sang nguồn Anh-Việt.");
        if (dictionaryManagement != null && dictionaryManagement.setActiveSource("en-vi")) {
            if (onSearchInitiated != null) { onSearchInitiated.accept(""); } else { /* ... lỗi ... */ }
        } else { System.err.println("Không thể chuyển sang nguồn Anh-Việt."); }
    }

    @FXML
    protected void handleVieEng(ActionEvent event) {
        System.out.println("Chuyển sang nguồn Việt-Anh.");
        if (dictionaryManagement != null && dictionaryManagement.setActiveSource("vi-en")) {
            if (onSearchInitiated != null) { onSearchInitiated.accept(""); } else { /* ... lỗi ... */ }
        } else { System.err.println("Không thể chuyển sang nguồn Việt-Anh."); }
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
        // Chuyển sang nguồn đang hoạt động trước khi chuyển màn hình Dictionary
        // (vì việc sửa/xóa sẽ xảy ra trên nguồn đang hoạt động)
        if (onSearchInitiated != null) {
            onSearchInitiated.accept(""); // Chuyển đến màn hình dictionary (không search gì)
            // TODO: Sau khi chuyển, cần báo hiệu DictionaryController mở Dialog sửa từ
            // Cần một callback riêng hoặc cờ hiệu trong pending actions
        } else {
            System.out.println("Không thể mở màn hình từ điển.");
        }
    }


    @FXML
    protected void handleGames(ActionEvent event) {
        System.out.println("Games clicked. Signaling to go to Game screen.");
        // *** Gọi callback onGoToGame ***
        if (onGoToGame != null) {
            try {
                onGoToGame.run(); // Kích hoạt hành động chuyển màn hình game
            } catch (RuntimeException e) {
                System.err.println("Error executing go to Game callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoToGame chưa được thiết lập!");
        }
    }
}