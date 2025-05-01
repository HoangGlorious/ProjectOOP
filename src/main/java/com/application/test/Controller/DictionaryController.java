package com.application.test.Controller;

import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.DictionaryManagement;
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

import javax.sound.midi.Synthesizer;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.function.Consumer; // Import Consumer


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

    private DictionaryManagement dictionaryManagement;
    private ObservableList<String> wordListObservable;
    private Runnable onGoBackToWelcome;
    private Runnable onDictionaryDataChanged;
    private Consumer<String> onSearchInitiated;
    private Consumer<String> onAddWordInitiated;
    
    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }


    /**
     * Setter để nhận instance DictionaryManagement từ DictionaryApplication.
     *
     * @param dictionaryManagement Instance DictionaryManagement đã được nạp dữ liệu.
     */
    public void setDictionaryManagement(DictionaryManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
        System.out.println("DictionaryManagement set in DictionaryController.");
    }

    public void setSearchText(String text) {
        this.dictionarySearchTextField.setText(text);
    }

    public void performSearch(String searchTerm) {
        if (dictionaryManagement == null) return;

        String cleanedSearchTerm = searchTerm.trim();

        // Ẩn ListView gợi ý sau khi thực hiện tìm kiếm chính xác
        suggestionListView.setVisible(false);
        suggestionListView.setManaged(false);

        // Thực hiện lookup chính xác
        Optional<DictionaryEntry> foundEntry = dictionaryManagement.lookupEntry(cleanedSearchTerm);

        if (foundEntry.isPresent()) {
            // Nếu tìm thấy chính xác, hiển thị chỉ từ đó trong ListView chính và nghĩa chi tiết
            wordListObservable.setAll(foundEntry.get().getHeadword());
            wordListView.getSelectionModel().selectFirst(); // Tự động chọn từ đó
            displayWordDefinition(foundEntry.get().getHeadword()); // Hiển thị nghĩa
            System.out.println("Displayed exact match for: " + cleanedSearchTerm);

        } else {
            // Nếu không tìm thấy chính xác, hiển thị thông báo và có thể hiển thị gợi ý tiền tố (tùy thiết kế)
            System.out.println("Exact match not found for: " + cleanedSearchTerm);
            wordListObservable.clear(); // Xóa danh sách chính
            definitionTextArea.setText("Từ '" + cleanedSearchTerm + "' không tìm thấy trong từ điển."); // Thông báo
            wordListView.getSelectionModel().clearSelection(); // Bỏ chọn

            // Tùy chọn: Nếu bạn muốn hiển thị gợi ý tiền tố ngay cả khi search chính xác không tìm thấy:
            // List<DictionaryEntry> prefixSuggestions = dictionaryManagement.searchEntriesByPrefix(cleanedSearchTerm);
            // if (!prefixSuggestions.isEmpty()) {
            //      definitionTextArea.appendText("\nKết quả gợi ý:");
            //      // Hiển thị gợi ý ở đâu đó (ListView gợi ý đã ẩn, có thể hiển thị trong TextArea hoặc ListView chính)
            // }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("DictionaryController initialized.");

        // Khởi tạo ObservableList cho wordListView (danh sách từ chính)
        wordListObservable = FXCollections.observableArrayList();
        wordListView.setItems(wordListObservable);
        suggestionListView.setItems(FXCollections.observableArrayList());
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
                dictionarySearchTextField.setText(newValue); // Đặt gợi ý vào search field
                performSearch(newValue); // Gọi hàm tìm kiếm với gợi ý được chọn
                suggestionListView.setVisible(false);
            }
        });


        // --- Thêm Listener cho TextField tìm kiếm (trên màn hình Dictionary) ---
        // Listener này sẽ gọi hàm xử lý tìm kiếm/gợi ý khi text thay đổi
        dictionarySearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchTextChange(newValue); // Gọi hàm xử lý khi text thay đổi
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

        // *** Thêm Listener để tính toán vị trí của ListView gợi ý khi layout thay đổi ***
        // Điều này đảm bảo ListView gợi ý luôn nằm ngay dưới search field
        searchContainer.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            updateSuggestionListViewPosition();
        });
        // Tính toán vị trí lần đầu sau khi initialize hoàn tất và layout được tính
        // Sử dụng Platform.runLater để đảm bảo layout đã được tính toán
        javafx.application.Platform.runLater(this::updateSuggestionListViewPosition);

        if (centerSplitPane != null && wordListView != null) {
            // Đặt ListView bên trái là không co giãn
            SplitPane.setResizableWithParent(wordListView, false);
            // VBox bên phải (definition) sẽ tự động co giãn để lấp đầy phần còn lại
        } else {
            System.err.println("SplitPane hoặc ListView chưa sẵn sàng!");
        }
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

    private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s-]");

    public void loadAndDisplayInitialData() {
        if (dictionaryManagement != null) {
            System.out.println("Loading and displaying initial dictionary data...");
            List<String> allHeadwords = dictionaryManagement.getAllDictionaryEntries().stream()
                    .map(DictionaryEntry::getHeadword)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());

            wordListObservable.setAll(allHeadwords);
            System.out.println("Displayed " + allHeadwords.size() + " entries.");
        } else {
            System.err.println("DictionaryManagement chưa được set!");
        }
    }


    private void displayWordDefinition(String headword) {
        Optional<DictionaryEntry> entry = dictionaryManagement.lookupEntry(headword);
        if (entry.isPresent()) {
            definitionTextArea.setText(entry.get().getFormattedExplanation());
        } else {
            definitionTextArea.setText("Không tìm thấy thông tin chi tiết của từ " + headword + ".");
        }
    }

    // Hàm xử lý sự kiện khi text trong ô tìm kiếm thay đổi
    private void handleSearchTextChange(String searchTerm) {
        if (dictionaryManagement == null) return;

        String cleanedSearchTerm = searchTerm.trim();

        if (cleanedSearchTerm.isEmpty()) {
            // Nếu search field trống, hiển thị toàn bộ từ điển và ẩn gợi ý
            loadAndDisplayInitialData(); // Hiển thị toàn bộ
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
            return;
        }

        // *** Thực hiện tìm kiếm theo tiền tố để gợi ý ***
        List<DictionaryEntry> suggestions = dictionaryManagement.searchEntriesByPrefix(cleanedSearchTerm);
        List<String> suggestionHeadwords = suggestions.stream()
                .map(DictionaryEntry::getHeadword)
                .collect(Collectors.toList());

        // Cập nhật ListView gợi ý
        suggestionListView.setItems(FXCollections.observableArrayList(suggestionHeadwords));

        // Hiển thị hoặc ẩn ListView gợi ý
        if (!suggestionHeadwords.isEmpty()) {
            suggestionListView.setVisible(true);
            suggestionListView.setManaged(true);
            // Tùy chọn: Đặt chiều cao cho ListView gợi ý
            // suggestionListView.setMaxHeight(suggestionHeadwords.size() * 24);
        } else {
            suggestionListView.setVisible(false);
            suggestionListView.setManaged(false);
        }

        // *** Xóa nội dung ListView chính và TextArea định nghĩa khi đang gõ gợi ý ***
        wordListObservable.clear();
        definitionTextArea.setText("");
        wordListView.getSelectionModel().clearSelection(); // Bỏ chọn

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

        Optional<DictionaryEntry> foundEntry = dictionaryManagement.lookupEntry(searchTerm);

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
        Stage ownerStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        initiateAddWordDialog("", ownerStage);
    }

    // Cập nhật handleEditButtonAction để gọi initiateEditWordDialog
    @FXML
    protected void handleEditButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null && dictionaryManagement != null) {
            Optional<DictionaryEntry> entryToEdit = dictionaryManagement.lookupEntry(selectedHeadword);
            if (entryToEdit.isPresent()) {
                // Lấy Stage cha từ sự kiện của nút
                Stage ownerStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                initiateEditWordDialog(entryToEdit.get(), ownerStage); // <-- Truyền Stage cha
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin chi tiết cho từ đã chọn.");
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Chọn từ", "Vui lòng chọn một từ trong danh sách để sửa.");
        }
    }


    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null && dictionaryManagement != null) {
            // Hỏi xác nhận xóa
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Xác nhận xóa");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Bạn có chắc chắn muốn xóa từ '" + selectedHeadword + "' không?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Nếu người dùng xác nhận, gọi hàm xóa trong DictionaryManagement
                boolean deleted = dictionaryManagement.deleteEntry(selectedHeadword);

                if (deleted) {
                    // Nếu xóa thành công, cập nhật ListView và xóa nội dung chi tiết
                    // Cách đơn giản: load lại toàn bộ danh sách
                    onDictionaryDataChanged.run(); // Gọi callback cập nhật ListView

                    // Hoặc cách tối ưu: chỉ xóa mục đó khỏi ObservableList
                    // wordListObservable.remove(selectedHeadword);
                    // definitionTextArea.setText(""); // Xóa nội dung chi tiết
                    // wordListView.getSelectionModel().clearSelection(); // Bỏ chọn

                    System.out.println("Đã xóa: " + selectedHeadword);
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa từ '" + selectedHeadword + "'.");

                    // Tùy chọn: Lưu thay đổi ngay sau khi xóa
                    // dictionaryManagement.saveDataToFile();

                } else {
                    // Thông báo lỗi (lỗi nội bộ hoặc không tìm thấy mặc dù đã check)
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa từ '" + selectedHeadword + "'.");
                }
            } else {
                // Người dùng hủy xóa
                System.out.println("Hủy bỏ xóa từ '" + selectedHeadword + "'.");
            }

        } else {
            showAlert(Alert.AlertType.INFORMATION, "Chọn từ", "Vui lòng chọn một từ trong danh sách để xóa.");
        }
    }


    @FXML
    protected void handleSpeakButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null) {
            System.out.println("Speak button clicked for: " + selectedHeadword);
            // TODO: Tích hợp thư viện TTS (Text-to-Speech) và gọi API của nó để phát âm selectedHeadword
            // Ví dụ với FreeTTS: Call API để nói selectedHeadword
        } else {
            System.out.println("No word selected to speak.");
        }
    }

    // Hàm helper để cập nhật trạng thái enable/disable của các nút
    private void updateButtonStates(String selectedHeadword) {
        boolean isWordSelected = (selectedHeadword != null && !selectedHeadword.isEmpty());
        editButton.setDisable(!isWordSelected);
        deleteButton.setDisable(!isWordSelected);
        speakButton.setDisable(!isWordSelected);
    }

    // TODO: Implement functions for Add/Edit/Delete Dialogs, TTS integration, Game integration

    public DictionaryManagement getDictionaryManagement() {
        return dictionaryManagement;
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
