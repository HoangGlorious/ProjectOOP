package com.application.test.Controller;

import com.application.test.Model.DictionaryManagement;
import com.application.test.Model.DictionaryEntry;

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
    private DictionaryManagement dictionaryManagement;

    public void setOnSearchInitiated(Consumer<String> onSearchInitiated) {
        this.onSearchInitiated = onSearchInitiated;
    }

    public void setOnAddWordInitiated(Consumer<String> onAddWordInitiated) {
        this.onAddWordInitiated = onAddWordInitiated;
    }

    public void setDictionaryManagement(DictionaryManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
        System.out.println("DictionaryManagement set in WelcomeController.");
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
    }

    private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");
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

        // Lấy danh sách gợi ý từ DictionaryManagement (sẽ dùng Trie)
        List<DictionaryEntry> suggestions = dictionaryManagement.searchEntriesByPrefix(prefix);

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
        System.out.println("Search action triggered on Welcome screen for: '" + searchTerm + "'");

        // Ẩn gợi ý khi thực hiện tìm kiếm
        suggestionListView.setVisible(false);
        suggestionListView.setManaged(false);


        // 1. Validation: Kiểm tra thanh tìm kiếm trống
        if (searchTerm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tìm kiếm trống", "Vui lòng gõ một từ vào thanh tìm kiếm.");
            return; // Dừng xử lý
        }

        // 2. Validation: Kiểm tra ký tự đặc biệt/số
        if (INVALID_CHARACTERS_PATTERN.matcher(searchTerm).find()) {
            showAlert(Alert.AlertType.WARNING, "Input không hợp lệ", "Từ tìm kiếm không được chứa ký tự đặc biệt.");
            return; // Dừng xử lý
        }


        // 3. Thực hiện Tra cứu chính xác trong DictionaryManagement
        if (dictionaryManagement == null) {
            System.err.println("DictionaryManagement chưa được set! Không thể tìm kiếm.");
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Chức năng từ điển chưa sẵn sàng.");
            return; // Dừng xử lý
        }

        Optional<DictionaryEntry> foundEntry = dictionaryManagement.lookupEntry(searchTerm);

        // 4. Xử lý kết quả Tra cứu
        if (foundEntry.isPresent()) {
            System.out.println("Từ '" + searchTerm + "' được tìm thấy. Chuyển màn hình.");
            // Báo hiệu cho DictionaryApplication để chuyển sang màn hình từ điển và hiển thị từ này
            if (onSearchInitiated != null) {
                onSearchInitiated.accept(searchTerm); // Truyền từ khóa tìm kiếm qua callback
            } else {
                System.err.println("Callback onSearchInitiated chưa được thiết lập!");
                // Tùy chọn: Chuyển màn hình dictionary view mà không hiển thị từ cụ thể
                // if (onGoToDictionary != null) { onGoToDictionary.run(); }
            }

        } else {
            System.out.println("Từ '" + searchTerm + "' không tìm thấy.");
            // Hiển thị thông báo không tìm thấy và hỏi thêm từ
            showNotFoundAlertWithAddOption(searchTerm);
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

    protected void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // Các phương thức xử lý sự kiện khác cho các nút khác
    // Các nút này có thể gọi các callback khác nếu chúng dẫn đến các màn hình khác
    // Hoặc nếu chúng vẫn dẫn đến màn hình từ điển (như Vie-Eng), bạn có thể gọi onGoToDictionary.run()
    @FXML
    protected void handleEngVie(ActionEvent event) {
        System.out.println("Navigating to English-Vietnamese...");
        if (onSearchInitiated != null) {
            onSearchInitiated.accept("");
        } else {
            System.err.println("Callback onSearchInitiated chưa được thiết lập!");
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
        if (onSearchInitiated != null) {
            onSearchInitiated.accept("");
        } else {
            System.err.println("Callback onSearchInitiated chưa được thiết lập!");
        }
    }

    @FXML
    protected void handleGames(ActionEvent event) {
        System.out.println("Games clicked.");
        // TODO: Nếu có màn hình game riêng, gọi callback khác hoặc load Stage/Scene mới
    }

}