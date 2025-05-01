package com.application.test.Controller;

import com.application.test.Model.DictionaryManagement;
import com.application.test.Model.DictionaryEntry;
import com.application.test.Model.WordSense;
import com.application.test.Model.WordSenseInputGroup;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;
import java.util.function.Consumer; // Import Consumer

public class EditWordDialogController implements Initializable, SenseContainerController {
    @FXML private TextField headwordField;
    @FXML private TextField pronunciationField;
    @FXML private VBox sensesContainer;
    @FXML private Button addSenseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private DictionaryManagement dictionaryManagement;
    private Runnable onWordUpdated; // Callback khi sửa thành công
    private DictionaryEntry originalEntry; // Lưu trữ entry gốc đang được sửa

    // List tạm để lưu trữ dữ liệu của các senses đang chỉnh sửa
    private List<WordSenseInputGroup> senseInputGroups; // Sử dụng lại lớp helper


    // Setter cho DictionaryManagement
    public void setDictionaryManagement(DictionaryManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
    }

    // Setter cho callback
    public void setOnWordUpdated(Runnable onWordUpdated) {
        this.onWordUpdated = onWordUpdated;
    }

    /**
     * Setter để nạp dữ liệu của entry cần sửa vào dialog.
     * @param entry Entry cần sửa.
     */
    public void setEntryToEdit(DictionaryEntry entry) {
        this.originalEntry = entry;
        if (entry != null) {
            headwordField.setText(entry.getHeadword());
            pronunciationField.setText(entry.getPronunciation());

            // Xóa các input definitions/examples mặc định hoặc cũ
            sensesContainer.getChildren().clear(); // Clear UI
            senseInputGroups.clear(); // Clear list quản lý

            // Nạp dữ liệu cho các senses hiện có
            for (WordSense sense : entry.getSenses()) {
                // Tạo nhóm input mới, truyền 'this' (là EditWordDialogController)
                WordSenseInputGroup senseGroup = new WordSenseInputGroup(this); // <-- OK vì this implement SenseContainerController
                senseGroup.loadData(sense); // Nạp dữ liệu sense vào nhóm input
                senseInputGroups.add(senseGroup);
                sensesContainer.getChildren().add(senseGroup.getUI());
            }
            // Nếu entry không có sense nào, thêm một sense trống ban đầu
            if (entry.getSenses().isEmpty()) {
                handleAddSense(null);
            }
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        senseInputGroups = new ArrayList<>();

        // Xử lý sự kiện khi nhấn Cancel
        cancelButton.setOnAction(event -> {
            closeDialog();
        });

        // Xử lý sự kiện khi nhấn Save
        saveButton.setOnAction(event -> {
            handleSave();
        });

        // Lưu ý: setEntryToEdit sẽ được gọi SAU initialize bởi DictionaryController
    }


    /**
     * Xử lý khi nhấn nút "+ Thêm Loại từ/Nghĩa".
     * Thêm một nhóm input mới cho một Word Sense.
     */
    @FXML
    protected void handleAddSense(ActionEvent event) {
        // Tương tự như trong AddWordDialogController
        // Tạo nhóm input mới, truyền 'this' (là EditWordDialogController)
        WordSenseInputGroup newSenseGroup = new WordSenseInputGroup(this); // <-- OK
        senseInputGroups.add(newSenseGroup);
        sensesContainer.getChildren().add(newSenseGroup.getUI());
        newSenseGroup.handleAddDefinition(null);
    }

    /**
     * Hàm được gọi từ WordSenseInputGroup khi một sense bị xóa.
     * Implement phương thức từ interface SenseContainerController.
     * @param senseGroup Nhóm input của sense bị xóa.
     */
    @Override // Đánh dấu override
    public void removeSenseGroup(WordSenseInputGroup senseGroup) {
        senseInputGroups.remove(senseGroup);
        sensesContainer.getChildren().remove(senseGroup.getUI());
        // ... logic kiểm tra số lượng sense ...
    }


    /**
     * Xử lý khi nhấn nút "Cập nhật".
     * Thu thập dữ liệu, xóa entry cũ và thêm entry mới (hoặc cập nhật entry hiện có).
     */
    @FXML
    protected void handleSave() {
        // 1. Thu thập dữ liệu từ các input fields (tương tự Add)
        String headword = headwordField.getText().trim();
        String pronunciation = pronunciationField.getText().trim();

        // 2. Validation
        if (headword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input trống", "Headword không được để trống.");
            return;
        }
        // Kiểm tra ký tự đặc biệt
        // if (INVALID_CHARACTERS_PATTERN.matcher(headword).find()) { /* ... */ return; }


        if (dictionaryManagement == null) { /* ... */ return; }
        if (originalEntry == null) { /* ... */ return; } // Đảm bảo có entry gốc


        // Kiểm tra trùng lặp headword MỚI (chỉ khi headword thay đổi)
        if (!originalEntry.getHeadword().equalsIgnoreCase(headword)) {
            if (dictionaryManagement.lookupEntry(headword).isPresent()) {
                showAlert(Alert.AlertType.WARNING, "Từ đã tồn tại", "Từ '" + headword + "' đã tồn tại trong từ điển.");
                return;
            }
        }

        // Thu thập dữ liệu Senses, Definitions, Examples (tương tự Add)
        List<WordSense> senses = new ArrayList<>();
        boolean hasAtLeastOneSense = false;
        for (WordSenseInputGroup senseGroup : senseInputGroups) {
            Optional<WordSense> senseOptional = senseGroup.getData();
            if (senseOptional.isPresent()) {
                senses.add(senseOptional.get());
                hasAtLeastOneSense = true;
            }
        }

        if (!hasAtLeastOneSense) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Phải có ít nhất một loại từ/cách dùng (Sense).");
            return;
        }

        // 3. Tạo DictionaryEntry MỚI từ dữ liệu đã sửa
        DictionaryEntry updatedEntry = new DictionaryEntry(headword, pronunciation);
        senses.forEach(updatedEntry::addSense);

        // 4. Thực hiện cập nhật trong DictionaryManagement
        boolean updated = dictionaryManagement.updateEntry(originalEntry.getHeadword(), updatedEntry); // <-- Cần cài đặt hàm này

        if (updated) {
            System.out.println("Cập nhật từ thành công: " + headword);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật từ '" + headword + "'.");
            // Gọi callback để báo hiệu cho DictionaryController cập nhật ListView
            if (onWordUpdated != null) {
                onWordUpdated.run();
            }
            closeDialog();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật từ '" + headword + "'.");
        }
    }

    /**
     * Đóng cửa sổ dialog.
     */
    @FXML
    protected void handleCancel() {
        System.out.println("Hủy sửa từ.");
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
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

    // TODO: Implement loadData(WordSense sense) in WordSenseInputGroup (already added a basic version)
}
