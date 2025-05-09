package com.application.test.Controller;

import com.application.test.Model.*;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.awt.geom.GeneralPath;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Consumer;

import com.application.test.Model.DictionaryEntry;

public class WelcomeController implements Initializable {

    @FXML
    private StackPane mainPane;
    @FXML
    private TextField welcomeSearchTextField;
    @FXML
    private Button welcomeSearchButton;
    @FXML
    private ListView<String> suggestionListView;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ImageView backgroundImageView;
    @FXML
    private ImageView icon1;
    @FXML
    private ComboBox<String> sourceComboBox;
    @FXML
    private Hyperlink WordOfTheDay;

    private WordOfTheDay wotd;

    // Callbacks để báo hiệu cho DictionaryApplication
    private Consumer<String> onSearchInitiated; // Callback khi người dùng tìm kiếm từ tồn tại
    private Consumer<String> onAddWordInitiated; // Callback khi người dùng muốn thêm từ (từ thông báo lỗi)
    private GeneralManagement dictionaryManagement;
    private Runnable onGoToGame;
    private Runnable onGoToThesaurus;
    private Runnable onGoToSentenceTranslation;
    private Runnable onGoToGrammar;
    private Runnable onGoToFavorites;


    public void setOnGoToGame(Runnable onGoToGame) {
        this.onGoToGame = onGoToGame;
    }

    public void setOnGoToThesaurus(Runnable onGoToThesaurus) {
        this.onGoToThesaurus = onGoToThesaurus;
    }

    public void setOnGoToSentenceTranslation(Runnable onGoToSentenceTranslation) {
        this.onGoToSentenceTranslation = onGoToSentenceTranslation;
    }

    public void setOnGoToGrammar(Runnable onGoToGrammar) {
        this.onGoToGrammar = onGoToGrammar;
    }

    public void setOnGoToFavorites(Runnable onGoToFavorites) {
        this.onGoToFavorites = onGoToFavorites;
    }

    public void setOnSearchInitiated(Consumer<String> onSearchInitiated) {
        this.onSearchInitiated = onSearchInitiated;
    }

    public void setOnAddWordInitiated(Consumer<String> onAddWordInitiated) {
        this.onAddWordInitiated = onAddWordInitiated;
    }

    public void setDictionaryManagement(GeneralManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
        initializeSourceComboBox();
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

    private void initializeSourceComboBox() {
        if (dictionaryManagement == null || sourceComboBox == null) return;

        // Lấy danh sách tên hiển thị của các nguồn có sẵn
        List<String> sourceDisplayNames = dictionaryManagement.getAvailableSourceDisplayNames();
        sourceComboBox.setItems(FXCollections.observableArrayList(sourceDisplayNames));

        // Chọn nguồn đang hoạt động làm giá trị mặc định
        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        sourceComboBox.getSelectionModel().select(activeSource.getDisplayName());

        // Thêm Listener khi người dùng chọn một nguồn khác trong ComboBox
        sourceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                String newSourceId = dictionaryManagement.getSourceIdByDisplayName(newValue);
                if (newSourceId != null && dictionaryManagement.setActiveSource(newSourceId)) {
                    System.out.println("Nguồn đã chuyển sang: " + newValue);
                    welcomeSearchTextField.clear();
                    showSuggestions(""); // Ẩn gợi ý và clear listview gợi ý
                }
            }
        });

        // Apply custom cell factory for consistent styling
        sourceComboBox.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            cell.getStyleClass().add("WcomboBoxCell");
            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    cell.setText(newItem);
                } else {
                    cell.setText(null);
                }
            });
            return cell;
        });

        // Style the selected value display (button cell)
        sourceComboBox.setButtonCell(new ListCell<>() {
            {
                getStyleClass().add("WcomboBoxCell");
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
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
     *
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
        if (dictionaryManagement == null) {
            return;
        }
        if (searchTerm.isEmpty()) {
            showNotFoundAlert();
            return;
        }

        // *** Lấy nguồn từ điển đang hoạt động và lookup trên nguồn đó ***
        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        Optional<DictionaryEntry> foundEntry = activeSource.lookupEntry(searchTerm);

        if (foundEntry.isPresent()) {
            // Báo hiệu cho DictionaryApplication để chuyển sang màn hình từ điển và hiển thị từ này
            if (onSearchInitiated != null) {
                onSearchInitiated.accept(searchTerm);
            }
        } else {
            // Hiển thị thông báo không tìm thấy và hỏi thêm từ
            showNotFoundAlertWithAddOption(searchTerm); // Hàm này gọi onAddWordInitiated
        }
    }

    private void showNotFoundAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Tìm kiếm trống!");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng gõ một từ vào thanh tìm kiếm!");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2);"
                + "-fx-font-family: 'Segoe UI';"
                + "-fx-font-size: 14px;");

        alert.showAndWait();
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
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2);"
                + "-fx-font-family: 'Segoe UI';"
                + "-fx-font-size: 14px;");

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

    // Hàm set WOTD
    public void setWotd(WordOfTheDay wotd) {
        this.wotd = wotd;
        updateWOTDDisplay();
    }


    // Hàm cập nhật trưng bày WordOfTheDay
    // WordOfTheDay được trưng bày dưới dạng hyperlink thực hiện hoạt động tra cứu từ điển khi nhấn vào WOTD
    private void updateWOTDDisplay() {
        Platform.runLater(() -> {
            try {
                // Cập nhật WordOfTheDay hyperlink
                String todayWord = wotd.getTodayWord();
                if (todayWord != null && !todayWord.isEmpty()) {
                    WordOfTheDay.setText(todayWord);

                    // Set hành động đưa đến entry từ điển khi click vào hyperlink
                    WordOfTheDay.setOnAction(e -> {
                        welcomeSearchTextField.setText(todayWord);
                        handleWelcomeSearchAction(e);
                    });
                } else {
                    // Nếu không có từ để hiển thị, ẩn hyperlink
                    WordOfTheDay.setVisible(false);
                }
            } catch (Exception e) {
                System.err.println("Failed to update WOTD: " + e.getMessage());
                WordOfTheDay.setVisible(false);
            }
        });
    }


    // Các phương thức xử lý sự kiện khác cho các nút khác
    // Các nút này có thể gọi các callback khác nếu chúng dẫn đến các màn hình khác
    // Hoặc nếu chúng vẫn dẫn đến màn hình từ điển (như Vie-Eng), bạn có thể gọi onGoToDictionary.run()

    @FXML
    protected void handleSentenceTranslation(ActionEvent event) {
        System.out.println("Sentence Translation clicked.");
        if (onGoToSentenceTranslation != null) {
            try {
                onGoToSentenceTranslation.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go to SentenceTranslation callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoToSentenceTranslation chưa được thiết lập!");
        }
    }

    @FXML
    protected void handleThesaurus(ActionEvent event) {
        System.out.println("Thesaurus clicked. Signaling to go to Thesaurus screen.");
        if (onGoToThesaurus != null) {
            try {
                onGoToThesaurus.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go to Thesaurus callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoToThesaurus chưa được thiết lập!");
        }
    }

    @FXML
    protected void handleGrammar(ActionEvent event) {
        System.out.println("Grammar clicked. Signaling to go to Grammar screen.");
        if (onGoToGrammar != null) {
            try {
                onGoToGrammar.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go to Grammar callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoToGrammar chưa được thiết lập!");
        }
    }

    @FXML
    protected void handleGames(ActionEvent event) {
        System.out.println("Games clicked. Signaling to go to Game screen.");
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

    @FXML
    protected void handleFavorites(ActionEvent event) {
        System.out.println("Favorites clicked. Signaling to go to Favorites screen.");
        if (onGoToFavorites != null) {
            try {
                onGoToFavorites.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go to Favorites callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoToFavorites chưa được thiết lập!");
        }
    }
}