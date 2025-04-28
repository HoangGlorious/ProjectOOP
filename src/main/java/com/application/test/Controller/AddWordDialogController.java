package com.application.test.Controller;

import com.application.test.Model.WordSenseInputGroup;
import com.application.test.Model.DictionaryManagement;
import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.ExamplePhrase;
import com.application.test.Model.WordSense;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox; // Import VBox
import javafx.stage.Stage; // Import Stage

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer; // Import Consumer
import java.util.Optional; // Import Optional
import java.util.regex.Pattern;



public class AddWordDialogController implements Initializable, SenseContainerController {
    private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");
    @FXML private TextField headwordField;
    @FXML private TextField pronunciationField;
    @FXML private VBox sensesContainer; // Container để chứa các UI cho từng Sense
    @FXML private Button addSenseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private DictionaryManagement dictionaryManagement;
    // Callback để báo hiệu cho DictionaryController khi thêm thành công
    private Runnable onWordAdded;
    // Từ khóa ban đầu được truyền từ màn hình Welcome (nếu có)
    private String initialWord;

    // List tạm để lưu trữ dữ liệu của các senses đang nhập
    private List<WordSenseInputGroup> senseInputGroups; // Sẽ tạo lớp helper WordSenseInputGroup


    // Setter cho DictionaryManagement
    public void setDictionaryManagement(DictionaryManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
    }

    // Setter cho callback
    public void setOnWordAdded(Runnable onWordAdded) {
        this.onWordAdded = onWordAdded;
    }

    // Setter cho từ khóa ban đầu
    public void setInitialWord(String initialWord) {
        this.initialWord = initialWord;
        if (headwordField != null && initialWord != null) {
            headwordField.setText(initialWord);
            headwordField.setDisable(true); // Không cho sửa headword nếu được truyền từ Welcome
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        senseInputGroups = new ArrayList<>();
        // Thêm một sense trống ban đầu khi dialog mở
        handleAddSense(null); // Gọi hàm thêm sense

        // Xử lý sự kiện khi nhấn Cancel
        cancelButton.setOnAction(event -> {
            closeDialog();
        });

        // Xử lý sự kiện khi nhấn Save
        saveButton.setOnAction(event -> {
            handleSave();
        });

        // Nếu initialWord đã được set trước khi initialize (ví dụ: trong setInitialWord)
        // thì headwordField đã có text và bị disable.
        // Nếu setInitialWord được gọi SAU initialize, cần set text ở đó.
    }

    @FXML
    protected void handleAddSense(ActionEvent event) {
        // Tạo nhóm input mới, truyền 'this' (là AddWordDialogController)
        WordSenseInputGroup newSenseGroup = new WordSenseInputGroup(this); // <-- OK vì this implement SenseContainerController
        senseInputGroups.add(newSenseGroup);
        sensesContainer.getChildren().add(newSenseGroup.getUI());
        newSenseGroup.handleAddDefinition(null);
    }

    /**
     * Hàm được gọi từ WordSenseInputGroup khi một sense bị xóa.
     * Implement phương thức từ interface SenseContainerController.
     * @param senseGroup Nhóm input của sense bị xóa.
     */
    @Override // Đánh dấu override để kiểm tra đúng interface
    public void removeSenseGroup(WordSenseInputGroup senseGroup) {
        senseInputGroups.remove(senseGroup);
        sensesContainer.getChildren().remove(senseGroup.getUI());
        // ... logic kiểm tra số lượng sense ...
    }


    /**
     * Xử lý khi nhấn nút "Lưu".
     * Thu thập dữ liệu từ các input fields và lưu vào từ điển.
     */
    @FXML
    protected void handleSave() {
        // 1. Thu thập dữ liệu từ các input fields
        String headword = headwordField.getText().trim();
        String pronunciation = pronunciationField.getText().trim();

        // 2. Validation (kiểm tra trống, ký tự đặc biệt, trùng lặp headword)
        if (headword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input trống", "Headword không được để trống.");
            return;
        }
        // Kiểm tra ký tự đặc biệt (có thể dùng regex như trong WelcomeController)
        if (INVALID_CHARACTERS_PATTERN.matcher(headword).find()) {
            showAlert(Alert.AlertType.WARNING, "Input không hợp lệ", "Headword không được chứa ký tự đặc biệt hoặc số.");
            return;
        }

        if (dictionaryManagement == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Dictionary Management chưa sẵn sàng.");
            return;
        }

        // Nếu không phải là từ được truyền từ Welcome (initialWord), kiểm tra trùng lặp headword
        if (initialWord == null || !initialWord.equalsIgnoreCase(headword)) {
            if (dictionaryManagement.lookupEntry(headword).isPresent()) {
                showAlert(Alert.AlertType.WARNING, "Từ đã tồn tại", "Từ '" + headword + "' đã tồn tại trong từ điển.");
                return;
            }
        }


        // Thu thập dữ liệu Senses, Definitions, Examples từ các nhóm input
        List<WordSense> senses = new ArrayList<>();
        boolean hasAtLeastOneSense = false;
        for (WordSenseInputGroup senseGroup : senseInputGroups) {
            Optional<WordSense> senseOptional = senseGroup.getData();
            if (senseOptional.isPresent()) {
                senses.add(senseOptional.get());
                hasAtLeastOneSense = true;
            }
            // Nếu senseGroup.getData() trả về Optional.empty(), có thể hiển thị lỗi cho sense đó
        }

        if (!hasAtLeastOneSense) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Phải thêm ít nhất một loại từ/cách dùng (Sense).");
            return;
        }

        // 3. Tạo DictionaryEntry
        DictionaryEntry newEntry = new DictionaryEntry(headword, pronunciation);
        senses.forEach(newEntry::addSense); // Thêm tất cả senses vào entry

        // 4. Lưu vào từ điển (sử dụng DictionaryManagement)
        boolean added = dictionaryManagement.addEntry(newEntry); // Hàm addEntry đã kiểm tra trùng lặp

        if (added) {
            System.out.println("Thêm từ thành công: " + headword);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm từ '" + headword + "' vào từ điển.");
            // Gọi callback để báo hiệu cho DictionaryController cập nhật ListView
            if (onWordAdded != null) {
                onWordAdded.run();
            }
            closeDialog(); // Đóng dialog sau khi lưu thành công
        } else {
            // Lỗi đã được in trong DictionaryManagement.addEntry()
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm từ '" + headword + "'.");
        }
    }

    /**
     * Đóng cửa sổ dialog.
     */
    @FXML
    protected void handleCancel() {
        System.out.println("Hủy thêm từ.");
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow(); // Lấy Stage từ bất kỳ control nào trong dialog
        stage.close();
    }

    /**
     * Hàm tiện ích để hiển thị Alert Box.
     * @param type Loại Alert.
     * @param title Tiêu đề.
     * @param message Nội dung.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // *** Cần tạo lớp helper WordSenseInputGroup ***
    // Lớp này quản lý UI cho một Sense cụ thể (Label PoS, VBox Definitions, VBox Examples, buttons + / -)
    // và cung cấp phương thức getData() để thu thập dữ liệu WordSense từ các input fields con.
}
