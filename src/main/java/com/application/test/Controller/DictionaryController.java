package com.application.test.Controller;

import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.DictionarySource;
import com.application.test.Model.GeneralManagement;



import com.application.test.Model.TextToSpeech;
import javafx.application.Platform;

import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Import FXMLLoader
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent; // Import Parent
import javafx.scene.Scene; // Import Scene
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality; // Import Modality
import javafx.stage.Stage; // Import Stage
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Consumer; // Import Consumer

import static com.application.test.Model.TextToSpeech.speak;


public class DictionaryController implements Initializable {

    @FXML
    private SplitPane centerSplitPane;
    @FXML
    private HBox topBarHBox;
    @FXML
    private AnchorPane topAnchorPane;
    @FXML
    private TextField dictionarySearchTextField;
    @FXML
    private ListView<String> wordListView;
    @FXML
    private TextArea definitionTextArea;
    @FXML
    private Button dictSearchButton;
    @FXML
    private VBox searchContainer;
    @FXML
    private Button backButton1;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button speakButton;
    @FXML
    private ListView<String> suggestionListView;
    @FXML
    private ComboBox<String> sourceComboBox;

    private GeneralManagement dictionaryManagement;
    private ObservableList<String> wordListObservable;
    private Runnable onGoBackToWelcome;
    private Runnable onDictionaryDataChanged;
    private Consumer<String> onSearchInitiated;
    private Consumer<String> onAddWordInitiated;
    private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s-]");
    private String initialSearchTerm;

    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    public void setInitialSearchTerm(String initialSearchTerm) {
        this.initialSearchTerm = initialSearchTerm;
        System.out.println("Initial search term set to: '" + initialSearchTerm + "' in DictionaryController.");
    }

    public void setDictionaryManagement(GeneralManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
        initializeSourceComboBox();
        System.out.println("DictionaryManagement set in DictionaryController.");
    }

    public void performSearch(String searchTerm) {
        if (dictionaryManagement == null) return;

        String cleanedSearchTerm = searchTerm.trim();
        suggestionListView.setVisible(false);
        suggestionListView.setManaged(false);

        if (cleanedSearchTerm.isEmpty()) { loadAndDisplayInitialData(); return; }

        // *** Lấy nguồn từ điển đang hoạt động và lookup trên nguồn đó ***
        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        Optional<DictionaryEntry> foundEntry = activeSource.lookupEntry(cleanedSearchTerm);

        if (foundEntry.isPresent()) {
            wordListObservable.setAll(foundEntry.get().getHeadword());
            wordListView.getSelectionModel().selectFirst();
            displayWordDefinition(foundEntry.get().getHeadword());
            System.out.println("Displayed exact match for: " + cleanedSearchTerm);
        } else {
            System.out.println("Exact match not found for: " + cleanedSearchTerm);
            wordListObservable.clear();
            definitionTextArea.setText("Từ '" + cleanedSearchTerm + "' không tìm thấy trong từ điển.");
            wordListView.getSelectionModel().clearSelection();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("DictionaryController initialized.");

        // Khởi tạo ObservableList cho wordListView (danh sách từ chính)
        wordListObservable = FXCollections.observableArrayList();
        wordListView.setItems(wordListObservable);

        // Khởi tạo ObservableList cho suggestionListView (gợi ý)
        suggestionListView.setItems(FXCollections.observableArrayList()); // Initialize suggestion list items too
        suggestionListView.setFocusTraversable(false);
        suggestionListView.setVisible(false);
        suggestionListView.setManaged(false);

        // --- Thêm Listener khi người dùng chọn một từ trong wordListView (danh sách chính) ---
        wordListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayWordDefinition(newValue);
                    } else {
                        definitionTextArea.setText("");
                    }
                }
        );

        // --- Thêm Listener khi người dùng chọn một gợi ý trong suggestionListView ---
        suggestionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {

                dictionarySearchTextField.setText(newValue);
                // Ẩn gợi ý sau khi chọn
                suggestionListView.setVisible(false);
                suggestionListView.setManaged(false);
            }
        });

        // --- Thêm Listener cho TextField tìm kiếm (trên màn hình Dictionary) ---
        // Listener này sẽ gọi hàm xử lý tìm kiếm/gợi ý khi text thay đổi
        dictionarySearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchTextChange(newValue); // Gọi hàm xử lý khi text thay đổi (cho gợi ý)
        });

        // Xử lý nhấn Enter trên dictionarySearchTextField màn hình Dictionary
        dictionarySearchTextField.setOnAction(event -> {
            // Khi nhấn Enter, lấy text hiện tại và thực hiện tìm kiếm chính xác
            performSearch(dictionarySearchTextField.getText().trim());
        });

        // Xử lý click nút Search trên màn hình Dictionary
        if (dictSearchButton != null) {
            dictSearchButton.setOnAction(event -> {
                // Khi click nút, lấy text hiện tại và thực hiện tìm kiếm chính xác
                performSearch(dictionarySearchTextField.getText().trim());
            });
        }


        // Ban đầu vô hiệu hóa các nút (trừ Add)
        updateButtonStates(null);
        wordListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateButtonStates(newValue);
                }
        );

        // Thiết lập callback khi dữ liệu từ điển thay đổi
        this.onDictionaryDataChanged = this::loadAndDisplayInitialData;

        // Listener để tính toán vị trí của ListView gợi ý
        /* ... */
        Platform.runLater(this::updateSuggestionListViewPosition);


        if (centerSplitPane != null && wordListView != null) {
            SplitPane.setResizableWithParent(wordListView, false);
        } else {
            System.err.println("SplitPane hoặc ListView chưa sẵn sàng!");
        }

        Platform.runLater(() -> {
            if (initialSearchTerm != null && dictionarySearchTextField != null) { // Kiểm tra initialSearchTerm và TextField
                System.out.println("Processing initial search term: '" + initialSearchTerm + "' in Platform.runLater.");
                dictionarySearchTextField.setText(initialSearchTerm); // <-- Đặt text field (sẽ trigger listener)

                performSearch(initialSearchTerm);
            } else {
                // Nếu initialSearchTerm là null hoặc trống, hiển thị toàn bộ từ điển ban đầu
                System.out.println("No initial search term or it's empty. Loading all data.");
                loadAndDisplayInitialData();
            }
        });
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
                    // *** Nạp lại dữ liệu vào ListView chính khi chuyển nguồn ***
                    // Xóa nội dung search field và hiển thị toàn bộ từ điển của nguồn mới
                    setInitialSearchTerm(""); // Đặt text rỗng, hàm này sẽ gọi loadAndDisplayInitialData()
                    // TODO: Cập nhật trạng thái các nút (ví dụ: nút Speak có thể bị disable nếu nguồn mới không hỗ trợ phát âm)
                }
            }
        });
    }

    public void resetScene() {
        System.out.println("Resetting Dictionary scene UI...");
        // Check if FXML elements have been injected and are not null
        // These checks are still good practice, although the timing should now be correct
        if (dictionarySearchTextField != null) {
            dictionarySearchTextField.clear();
        }
        if (suggestionListView != null) {
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
            suggestionListView.getItems().clear(); // Clear items in the suggestion list
        }
        // Optional: Clear the main word list and definition area
        // (The setSearchText("") call from Welcome will reload the main list anyway when you return)
        if (wordListView != null) {
            wordListView.getItems().clear();
            wordListView.getSelectionModel().clearSelection();
        }
        if (definitionTextArea != null) {
            definitionTextArea.clear();
        }

        // Ensure buttons are updated based on no selection
        // This will likely be handled by the wordListView listener firing when items are cleared
        // updateButtonStates(null); // Can call explicitly if needed
    }


    /**
     * Cập nhật vị trí của ListView gợi ý để nó nằm ngay dưới searchContainer.
     */
    private void updateSuggestionListViewPosition() {
        if (searchContainer != null && suggestionListView != null) {
            // Lấy bounds của searchContainer trong hệ tọa độ của AnchorPane cha (topAnchorPane)
            Bounds boundsInAnchorPane = searchContainer.getBoundsInParent();

            // Thiết lập neo TOP của ListView gợi ý ngay dưới searchContainer
            AnchorPane.setTopAnchor(suggestionListView, boundsInAnchorPane.getMaxY()); // MaxY là đáy của searchContainer

            // Thiết lập neo LEFT và RIGHT của ListView gợi ý khớp với searchContainer
            AnchorPane.setLeftAnchor(suggestionListView, boundsInAnchorPane.getMinX());
            // Chiều rộng của ListView gợi ý bằng chiều rộng của searchContainer
            suggestionListView.setPrefWidth(boundsInAnchorPane.getWidth());
            // Hủy neo RIGHT để PrefWidth có tác dụng
            AnchorPane.setRightAnchor(suggestionListView, null);

            // Tùy chọn: Đặt chiều rộng tối đa cho ListView gợi ý nếu không muốn nó quá rộng
            // suggestionListView.setMaxWidth(boundsInAnchorPane.getWidth());
        }
    }

    public void loadAndDisplayInitialData() {
        if (dictionaryManagement == null) return;

        // *** Lấy nguồn từ điển đang hoạt động và lấy tất cả entries ***
        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        List<DictionaryEntry> allEntries = activeSource.getAllEntries();

        List<String> allHeadwords = allEntries.stream()
                .map(DictionaryEntry::getHeadword)
                .collect(Collectors.toList());

        wordListObservable.setAll(allHeadwords);
        System.out.println("Displayed " + allHeadwords.size() + " entries from source: " + activeSource.getDisplayName());
        wordListView.getSelectionModel().clearSelection();
        definitionTextArea.setText("");
        // TODO: Cập nhật các nút (Speak) dựa trên khả năng của nguồn active
    }


    private void displayWordDefinition(String headword) {
        if (dictionaryManagement == null) return;
        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        Optional<DictionaryEntry> entry = activeSource.lookupEntry(headword);
        if (entry.isPresent()) {
            definitionTextArea.setText(entry.get().getFormattedExplanation());
        } else {
            definitionTextArea.setText("Không tìm thấy thông tin chi tiết cho '" + headword + "'.");
        }
    }

    // Hàm xử lý sự kiện khi text trong ô tìm kiếm thay đổi
    private void handleSearchTextChange(String searchTerm) {
        if (dictionaryManagement == null) return;
        String cleanedSearchTerm = searchTerm.trim();

        if (cleanedSearchTerm.isEmpty()) {
            loadAndDisplayInitialData();
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
            return;
        }

        DictionarySource activeSource = dictionaryManagement.getActiveSource();
        List<DictionaryEntry> suggestions = activeSource.searchEntriesByPrefix(cleanedSearchTerm);

        List<String> suggestionHeadwords = suggestions.stream().map(DictionaryEntry::getHeadword).collect(Collectors.toList());

        suggestionListView.setItems(FXCollections.observableArrayList(suggestionHeadwords));

        if (!suggestionHeadwords.isEmpty()) { /* ... hiển thị gợi ý ... */ suggestionListView.setVisible(true); suggestionListView.setManaged(true); /* ... set prefHeight ... */ } else { /* ... ẩn gợi ý ... */ suggestionListView.setVisible(false); suggestionListView.setManaged(false); }

        wordListObservable.clear();
        definitionTextArea.setText("");
        wordListView.getSelectionModel().clearSelection();
    }


    // --- Các phương thức xử lý sự kiện từ Buttons (@FXML) ---

    // *** Phương thức xử lý sự kiện khi nhấn nút Back ***
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

    @FXML
    protected void handleDictionarySearchAction(ActionEvent event) {
        String searchTerm = dictionarySearchTextField.getText().trim();
        System.out.println("Search action triggered on dictionary screen for: '" + searchTerm + "'");

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

        // *** Lấy nguồn từ điển đang hoạt động ***
        DictionarySource activeSource = dictionaryManagement.getActiveSource();

        // *** Gọi lookupEntry trên nguồn đang hoạt động ***
        Optional<DictionaryEntry> foundEntry = activeSource.lookupEntry(searchTerm);

        // 4. Xử lý kết quả Tra cứu
        if (foundEntry.isPresent()) {
            System.out.println("Từ '" + searchTerm + "' được tìm thấy. Chuyển màn hình.");
            // Báo hiệu cho DictionaryApplication để chuyển sang màn hình từ điển và hiển thị từ này
            if (onSearchInitiated != null) {
                onSearchInitiated.accept(searchTerm); // Truyền từ khóa tìm kiếm qua callback
            } else {
                System.err.println("Callback onSearchInitiated chưa được thiết lập!");
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

    @FXML
    protected void handleSearchButtonAction(ActionEvent event) {
        performSearch(dictionarySearchTextField.getText().trim());
    }

    @FXML
    protected void handleAddButtonAction(ActionEvent event) {
        // TODO: Mở dialog thêm từ, truyền dictionaryManager và active source info nếu cần
        // initiateAddWordDialog("", (Stage)((Node)event.getSource()).getScene().getWindow()); // Cần sửa initiateAddWordDialog để nhận DictionaryManager
        // Hoặc dialogController nhận DictionaryManager và tự lấy active source
        Stage ownerStage = (Stage)((Node)event.getSource()).getScene().getWindow();
        initiateAddWordDialog("", ownerStage); // Hàm này cần truyền dictionaryManager cho dialogController
    }

    @FXML
    protected void handleEditButtonAction(ActionEvent event) {
        if (dictionaryManagement == null) return;
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null) {
            DictionarySource activeSource = dictionaryManagement.getActiveSource();
            Optional<DictionaryEntry> entryToEdit = activeSource.lookupEntry(selectedHeadword); // Lookup trên nguồn active
            if (entryToEdit.isPresent()) {
                // TODO: Mở dialog sửa từ, truyền dictionaryManager, entryToEdit, và active source info nếu cần
                // initiateEditWordDialog(entryToEdit.get(), (Stage)((Node)event.getSource()).getScene().getWindow()); // Cần sửa initiateEditWordDialog
                Stage ownerStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                initiateEditWordDialog(entryToEdit.get(), ownerStage); // Hàm này cần truyền dictionaryManager cho dialogController
            } else { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin chi tiết cho từ đã chọn trong nguồn đang hoạt động."); }
        } else { showAlert(Alert.AlertType.INFORMATION, "Chọn từ", "Vui lòng chọn một từ trong danh sách để sửa."); }
    }


    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) {
        if (dictionaryManagement == null) return;
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null) {
            // Hỏi xác nhận xóa
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION); /* ... */
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // *** Gọi deleteEntry trên nguồn đang hoạt động ***
                DictionarySource activeSource = dictionaryManagement.getActiveSource();
                boolean deleted = activeSource.deleteEntry(selectedHeadword);

                if (deleted) {
                    onDictionaryDataChanged.run(); // Cập nhật ListView
                    System.out.println("Đã xóa: " + selectedHeadword + " từ nguồn " + activeSource.getDisplayName());
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa từ '" + selectedHeadword + "'.");
                    // Tùy chọn: Lưu thay đổi ngay sau khi xóa
                    // activeSource.saveData(); // Lưu chỉ nguồn active
                    // dictionaryManager.saveAllSourcesData(); // Lưu tất cả nguồn
                } else { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa từ '" + selectedHeadword + "'."); }
            } else { System.out.println("Hủy bỏ xóa."); }
        } else { showAlert(Alert.AlertType.INFORMATION, "Chọn từ", "Vui lòng chọn một từ trong danh sách để xóa."); }
    }


    @FXML
    protected void handleSpeakButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null) {
            System.out.println("Speak button clicked for: " + selectedHeadword);

            //Gọi hàm speak của TextToSpeech để phát âm từ.
            try {
                speak(selectedHeadword);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            System.out.println("No word selected to speak.");
        }
    }

    // Hàm helper để cập nhật trạng thái enable/disable của các nút
    private void updateButtonStates(String selectedHeadword) {
        boolean isWordSelected = (selectedHeadword != null && !selectedHeadword.isEmpty());
        editButton.setDisable(!isWordSelected);
        deleteButton.setDisable(!isWordSelected);

    }

    /**
     * Phương thức để mở dialog Thêm từ.
     * Được gọi từ WelcomeController hoặc khi nhấn nút Add trên màn hình Dictionary.
     * @param initialWord Từ khóa ban đầu để điền vào field Headword (hoặc null/empty).
     */
    public void initiateAddWordDialog(String initialWord, Stage ownerStage) {
        System.out.println("Initiating Add Word dialog for word: " + initialWord);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/add_word_dialog.fxml"));
            Parent root = loader.load();

            AddWordDialogController dialogController = loader.getController();

            // Truyền các đối tượng/callback cần thiết cho dialog
            dialogController.setDictionaryManagement(this.dictionaryManagement);
            dialogController.setOnWordAdded(this.onDictionaryDataChanged); // Khi thêm xong, gọi hàm cập nhật ListView
            dialogController.setInitialWord(initialWord); // Truyền từ khóa ban đầu

            // Tạo Stage mới cho dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Thêm Từ Mới");
            dialogStage.setScene(new Scene(root));

            // Thiết lập modality
            Stage primaryStage = (Stage) dictionarySearchTextField.getScene().getWindow(); // Lấy Stage cha từ bất kỳ control nào trên màn hình này
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            dialogStage.showAndWait(); // Hiển thị và chờ

        } catch (IOException e) {
            System.err.println("Lỗi khi mở cửa sổ thêm từ: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ thêm từ.");
        }
    }

    /**
     * Phương thức để mở dialog Sửa từ.
     * Được gọi khi nhấn nút Edit.
     * @param entryToEdit Entry cần sửa.
     */
    private void initiateEditWordDialog(DictionaryEntry entryToEdit, Stage ownerStage) {
        if (entryToEdit == null) {
            System.err.println("Cannot initiate Edit Word dialog: entry is null.");
            return;
        }
        System.out.println("Initiating Edit Word dialog for word: " + entryToEdit.getHeadword());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/edit_word_dialog.fxml")); // <-- Cần tạo file FXML này
            Parent root = loader.load();

            EditWordDialogController dialogController = loader.getController(); // <-- Cần tạo Controller này

            // Truyền các đối tượng/callback cần thiết cho dialog
            dialogController.setDictionaryManagement(this.dictionaryManagement);
            dialogController.setOnWordUpdated(this.onDictionaryDataChanged); // Khi sửa xong, gọi hàm cập nhật ListView
            dialogController.setEntryToEdit(entryToEdit); // Nạp dữ liệu của entry cần sửa

            // Tạo Stage mới cho dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Chỉnh Sửa Từ: " + entryToEdit.getHeadword());
            dialogStage.setScene(new Scene(root));

            // Thiết lập modality
            Stage primaryStage = (Stage) dictionarySearchTextField.getScene().getWindow(); // Lấy Stage cha
            dialogStage.initOwner(primaryStage);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            dialogStage.showAndWait(); // Hiển thị và chờ

        } catch (IOException e) {
            System.err.println("Lỗi khi mở cửa sổ sửa từ: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở cửa sổ sửa từ.");
        }
    }


    // Hàm tiện ích hiển thị Alert (có thể di chuyển đến lớp Utility chung)
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
