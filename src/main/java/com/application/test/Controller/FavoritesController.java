package com.application.test.Controller;

import com.application.test.Model.FavoriteManagement;
import com.application.test.Model.GeneralManagement;
import com.application.test.Model.DictionarySource;
import com.application.test.Model.DictionaryEntry;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.FXML;

public class FavoritesController implements Initializable {

    @FXML
    private ListView<String> favoritesListView;
    @FXML
    private Button viewDefinitionButton;
    @FXML
    private Button removeFavoriteButton;
    @FXML
    private Button backButton;

    private FavoriteManagement favoriteManagement;
    private GeneralManagement dictionaryManagement;

    // Callback để báo hiệu cho DictionaryApplication khi muốn quay lại màn hình trước đó
    private Runnable onGoBackToWelcome;
    // Callback để báo hiệu cho DictionaryApplication khi muốn xem nghĩa của một từ yêu thích
    // Nó sẽ truyền headword để màn hình Dictionary tự tìm kiếm
    private Consumer<String> onViewWordDefinition;


    // Setters
    public void setFavoriteManagement(FavoriteManagement favoriteManagement) {
        this.favoriteManagement = favoriteManagement;
        loadFavoriteWords();
    }

    public void setDictionaryManager(GeneralManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
    }

    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    public void setOnViewWordDefinition(Consumer<String> onViewWordDefinition) {
        this.onViewWordDefinition = onViewWordDefinition;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("FavoritesController initialized.");
        // Ban đầu vô hiệu hóa các nút thao tác
        updateButtonStates();

        // Thêm listener cho ListView để enable/disable nút khi chọn/bỏ chọn
        favoritesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonStates();
        });

        // (Tùy chọn) Xử lý double-click trên ListView để xem nghĩa
        favoritesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleViewDefinition(null); // Gọi hàm xem nghĩa
            }
        });
    }

    /**
     * Nạp và hiển thị danh sách từ yêu thích vào ListView.
     */
    private void loadFavoriteWords() {
        if (favoriteManagement != null) {
            List<String> favWords = favoriteManagement.getAllFavoriteWords();
            ObservableList<String> observableFavWords = FXCollections.observableArrayList(favWords);
            favoritesListView.setItems(observableFavWords);
            System.out.println("Displayed " + favWords.size() + " favorite words.");
        } else {
            System.err.println("FavoriteManagement is null. Cannot load favorite words.");
            favoritesListView.setItems(FXCollections.observableArrayList()); // Hiển thị list rỗng
        }
        updateButtonStates(); // Cập nhật trạng thái nút sau khi nạp
    }

    /**
     * Cập nhật trạng thái enable/disable của các nút.
     */
    private void updateButtonStates() {
        boolean isWordSelected = favoritesListView.getSelectionModel().getSelectedItem() != null;
        // if (viewDefinitionButton != null) viewDefinitionButton.setDisable(!isWordSelected);
        if (removeFavoriteButton != null) removeFavoriteButton.setDisable(!isWordSelected);
    }


    /**
     * Xử lý khi nhấn nút "Xem Nghĩa".
     * Báo hiệu cho DictionaryApplication chuyển sang màn hình Dictionary và tìm kiếm từ này.
     */
    @FXML
    protected void handleViewDefinition(ActionEvent event) {
        String selectedWord = favoritesListView.getSelectionModel().getSelectedItem();
        if (selectedWord != null && onViewWordDefinition != null) {
            System.out.println("Viewing definition for favorite: " + selectedWord);
            onViewWordDefinition.accept(selectedWord); // Gọi callback truyền từ cần xem
        }
    }

    /**
     * Xử lý khi nhấn nút "Xóa khỏi Yêu thích".
     */
    @FXML
    protected void handleRemoveFavorite(ActionEvent event) {
        String selectedWord = favoritesListView.getSelectionModel().getSelectedItem();
        if (selectedWord != null && favoriteManagement != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Xác nhận xóa");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Bạn có chắc chắn muốn xóa '" + selectedWord + "' khỏi danh sách yêu thích?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean removed = favoriteManagement.removeFavorite(selectedWord);
                if (removed) {
                    System.out.println("Đã xóa '" + selectedWord + "' khỏi yêu thích.");
                    loadFavoriteWords(); // Nạp lại danh sách để cập nhật UI
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa từ khỏi danh sách yêu thích.");
                    // favoriteManager.saveFavorites(); // Đã tự lưu trong removeFavorite()
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa từ khỏi danh sách yêu thích.");
                }
            }
        }
    }

    /**
     * Xử lý khi nhấn nút "Quay lại".
     * Báo hiệu cho DictionaryApplication quay lại màn hình trước đó.
     */
    @FXML
    protected void handleBackButtonAction(ActionEvent event) {
        System.out.println("Back button clicked. Signalling to go back to Welcome View.");
        if (onGoBackToWelcome != null) {
            try {
                onGoBackToWelcome.run();
            } catch (RuntimeException e) {
                System.err.println("Lỗi khi thực hiện callback quay lại màn hình welcome: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoBackToWelcome chưa được thiết lập!");
        }
    }

    /**
     * Hàm tiện ích để hiển thị Alert Box.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Phương thức này có thể được gọi từ DictionaryApplication khi quay lại màn hình này,
     * để đảm bảo danh sách yêu thích luôn được cập nhật.
     */
    public void refreshFavoritesList() {
        System.out.println("Refreshing favorites list.");
        loadFavoriteWords();
    }
}

