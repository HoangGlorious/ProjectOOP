package com.application.test.Controller;

import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.DictionaryManagement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
        // Sau khi nhận được dictionaryManagement, mới nạp dữ liệu vào ListView
        loadAndDisplayInitialData();
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

    private void loadAndDisplayInitialData() {
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
        // Khi người dùng nhấn nút "Back", gọi callback để báo hiệu cho DictionaryApplication
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
        // Nếu muốn Search chỉ khi nhấn nút, có thể gọi handleSearchTextChange(searchTextField.getText()); ở đây
        // Hoặc nếu đã dùng listener on text change thì nút search có thể không cần thiết hoặc có chức năng khác
        System.out.println("Search button clicked (logic handled by text change listener)");
        // Có thể buộc focus vào ListView sau khi search
        wordListView.requestFocus();
        if (!wordListView.getItems().isEmpty()) {
            wordListView.getSelectionModel().selectFirst();
        }
    }

    @FXML
    protected void handleAddButtonAction(ActionEvent event) {
        System.out.println("Add button clicked");
        // TODO: Mở một cửa sổ (Dialog/Stage mới) để người dùng nhập thông tin từ mới
        // Sau khi nhập xong, tạo DictionaryEntry và gọi dictionaryManagement.addEntry()
        // Nếu thêm thành công, cập nhật ListView:
        // wordListObservable.add(newEntry.getHeadword());
        // Collections.sort(wordListObservable, String.CASE_INSENSITIVE_ORDER); // Sắp xếp lại
        // dictionaryManagement.saveDataToFile(); // Tùy chọn lưu ngay
    }

    @FXML
    protected void handleEditButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null) {
            System.out.println("Edit button clicked for: " + selectedHeadword);
            // TODO: Mở cửa sổ sửa từ, nạp thông tin của entry hiện tại vào form
            // Sau khi sửa xong, cập nhật đối tượng DictionaryEntry trong bộ nhớ
            // dictionaryManagement.saveDataToFile(); // Tùy chọn lưu ngay
            // Cập nhật lại hiển thị chi tiết
            // displayWordDefinition(selectedHeadword);
            // Có thể cần cập nhật ListView nếu headword bị sửa
        } else {
            System.out.println("No word selected for editing.");
        }
    }

    @FXML
    protected void handleDeleteButtonAction(ActionEvent event) {
        String selectedHeadword = wordListView.getSelectionModel().getSelectedItem();
        if (selectedHeadword != null) {
            System.out.println("Delete button clicked for: " + selectedHeadword);
            // TODO: Hỏi xác nhận từ người dùng (Alert Dialog)
            // Nếu xác nhận, gọi hàm xóa từ trong DictionaryManagement (bạn cần tạo hàm này)
            // boolean deleted = dictionaryManagement.deleteEntry(selectedHeadword);
            // if (deleted) {
            // Cập nhật ListView và xóa nội dung chi tiết
            //     wordListObservable.remove(selectedHeadword);
            //     definitionTextArea.setText("");
            //     dictionaryManagement.saveDataToFile(); // Tùy chọn lưu ngay
            //     System.out.println("Deleted: " + selectedHeadword);
            // } else {
            //      System.out.println("Failed to delete: " + selectedHeadword);
            // }
        } else {
            System.out.println("No word selected for deletion.");
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

}
