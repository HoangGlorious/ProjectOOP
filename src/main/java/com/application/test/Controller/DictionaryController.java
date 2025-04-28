package com.application.test.Controller;

import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.DictionaryManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Import FXMLLoader
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent; // Import Parent
import javafx.scene.Scene; // Import Scene
import javafx.scene.control.*;
import javafx.stage.Modality; // Import Modality
import javafx.stage.Stage; // Import Stage
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.function.Consumer; // Import Consumer


public class DictionaryController implements Initializable {

    @FXML
    private TextField searchTextField;
    @FXML
    private ListView<String> wordListView;
    @FXML
    private TextArea definitionTextArea;
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

    private DictionaryManagement dictionaryManagement;
    private ObservableList<String> wordListObservable;
    private Runnable onGoBackToWelcome;
    private Runnable onDictionaryDataChanged;


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
        this.searchTextField.setText(text);
    }

    public void performSearch(String searchTerm) {
        handleSearchTextChange(searchTerm);
    }

    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Không nạp dữ liệu ở đây nữa vì dictionaryManagement chưa chắc đã được set.
        // Logic hiển thị dữ liệu ban đầu được chuyển sang loadAndDisplayInitialData().

        // --- Khởi tạo ObservableList ---
        wordListObservable = FXCollections.observableArrayList();
        wordListView.setItems(wordListObservable); // Gán ObservableList vào ListView

        // --- Thêm Listener khi người dùng chọn một từ trong ListView ---
        wordListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        displayWordDefinition(newValue);
                    } else {
                        definitionTextArea.setText("");
                    }
                }
        );

        // --- Thêm Listener cho TextField tìm kiếm ---
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchTextChange(newValue);
        });

        // Ban đầu vô hiệu hóa các nút Sửa, Xóa, Phát âm
        updateButtonStates(null);
        wordListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateButtonStates(newValue);
                }
        );

        System.out.println("DictionaryController initialized.");
    }

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
    private void handleSearchTextChange(String searchText) {
        if (dictionaryManagement == null) return; // Kiểm tra null

        // *** THAY ĐỔI: Gọi hàm searchEntriesByPrefix của Management (sẽ dùng Trie) ***
        List<DictionaryEntry> searchResults = dictionaryManagement.searchEntriesByPrefix(searchText); // <-- Vẫn gọi hàm này
        List<String> resultHeadwords = searchResults.stream()
                .map(DictionaryEntry::getHeadword)
                .collect(Collectors.toList());

        wordListObservable.setAll(resultHeadwords);
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
    protected void handleSearchButtonAction(ActionEvent event) {
        String searchTerm = searchTextField.getText().trim();
        if (!searchTerm.isEmpty()) {
            performSearch(searchTerm); // Gọi hàm performSearch với text từ search field
        } else {
            // Nếu search field trống, hiển thị lại toàn bộ từ điển
            loadAndDisplayInitialData();
        }
    }

    @FXML
    protected void handleAddButtonAction(ActionEvent event) {
        // Khi nhấn nút Add trên màn hình Dictionary, mở dialog thêm từ với từ khóa trống
        initiateAddWordDialog("");
    }

    // Cập nhật handleEditButtonAction để gọi initiateEditWordDialog
    @FXML
    protected void handleEditButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null && dictionaryManagement != null) {
            Optional<DictionaryEntry> entryToEdit = dictionaryManagement.lookupEntry(selectedHeadword);
            if (entryToEdit.isPresent()) {
                initiateEditWordDialog(entryToEdit.get()); // Gọi hàm mở dialog sửa từ
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
    public void initiateAddWordDialog(String initialWord) {
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
            Stage primaryStage = (Stage) searchTextField.getScene().getWindow(); // Lấy Stage cha từ bất kỳ control nào trên màn hình này
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
    private void initiateEditWordDialog(DictionaryEntry entryToEdit) {
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
            Stage primaryStage = (Stage) searchTextField.getScene().getWindow(); // Lấy Stage cha
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
