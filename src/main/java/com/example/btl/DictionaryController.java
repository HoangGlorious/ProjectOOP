package com.example.btl;

import com.example.btl.Dictionary;
import com.example.btl.DictionaryManagement;
import com.example.btl.DictionaryEntry;

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

    // Liên kết với các thành phần UI trong FXML bằng fx:id
    @FXML private TextField searchTextField;
    @FXML private ListView<String> wordListView; // ListView hiển thị headword (String)
    @FXML private TextArea definitionTextArea; // Hoặc VBox/Label để hiển thị định nghĩa chi tiết
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button speakButton; // Nút phát âm

    private Dictionary dictionary;
    private DictionaryManagement dictionaryManagement;

    private ObservableList<String> wordListObservable;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.dictionary = new Dictionary();
        this.dictionaryManagement = new DictionaryManagement(this.dictionary);

        dictionaryManagement.insertFromFile();

        // --- Khởi tạo ObservableList và hiển thị lên ListView ---
        // Lấy tất cả headword từ các entry đã nạp
        List<String> allHeadwords = dictionary.getAllEntries().stream()
                .map(DictionaryEntry::getHeadword)
                .sorted(String.CASE_INSENSITIVE_ORDER) // Sắp xếp ban đầu
                .collect(Collectors.toList());


        wordListObservable = FXCollections.observableArrayList(allHeadwords);
        wordListView.setItems(wordListObservable);

        // --- Thêm Listener khi người dùng chọn một từ trong ListView ---
        wordListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // newValue là headword của từ được chọn
                    if (newValue != null) {
                        displayWordDefinition(newValue);
                    } else {
                        definitionTextArea.setText(""); // Xóa nội dung nếu không có gì được chọn
                    }
                }
        );

        // --- Thêm Listener cho TextField tìm kiếm (tìm kiếm ngay khi gõ) ---
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearchTextChange(newValue);
        });

        // Ban đầu có thể vô hiệu hóa các nút Sửa, Xóa, Phát âm nếu chưa có từ nào được chọn
        updateButtonStates(null);
        wordListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateButtonStates(newValue);
                }
        );
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
        // Gọi hàm tìm kiếm từ DictionaryManagement (cần sửa hàm searchEntriesByPrefix để trả về List<DictionaryEntry>)
        // Hiện tại searchEntriesByPrefix trả về List<DictionaryEntry> rồi, nên chỉ cần map sang headword
        List<DictionaryEntry> searchResults = dictionaryManagement.searchEntriesByPrefix(searchText);
        List<String> resultHeadwords = searchResults.stream()
                .map(DictionaryEntry::getHeadword)
                .collect(Collectors.toList());

        // Cập nhật ListView
        wordListObservable.setAll(resultHeadwords); // Xóa toàn bộ và thêm kết quả mới
        // Sau khi cập nhật ListView, nếu chỉ có 1 kết quả, có thể tự động chọn nó
        if (resultHeadwords.size() == 1) {
            wordListView.getSelectionModel().selectFirst();
        } else if (resultHeadwords.isEmpty()) {
            definitionTextArea.setText("Không tìm thấy từ nào khớp.");
        } else {
            definitionTextArea.setText("Kết quả tìm kiếm..."); // Xóa nội dung chi tiết cũ
        }
    }

    // --- Các phương thức xử lý sự kiện từ Buttons (@FXML) ---

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

}
