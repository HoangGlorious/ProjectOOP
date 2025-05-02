package com.application.test.Controller;

import com.application.test.Model.*;

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
import java.util.Optional;
import java.util.regex.Pattern;



public class AddWordDialogController implements Initializable, SenseContainerController {
    private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s-]");
    @FXML private TextField headwordField;
    @FXML private TextField pronunciationField;
    @FXML private VBox sensesContainer; // Container để chứa các UI cho từng Sense
    @FXML private Button addSenseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private GeneralManagement dictionaryManagement;
    // Callback để báo hiệu cho DictionaryController khi thêm thành công
    private Runnable onWordAdded;
    // Từ khóa ban đầu được truyền từ màn hình Welcome (nếu có)
    private String initialWord;

    // List tạm để lưu trữ dữ liệu của các senses đang nhập
    private List<WordSenseInputGroup> senseInputGroups; // Sẽ tạo lớp helper WordSenseInputGroup


    // Setter cho DictionaryManagement
    public void setDictionaryManagement(GeneralManagement dictionaryManagement) {
        this.dictionaryManagement = dictionaryManagement;
    }

    // Setter cho callback
    public void setOnWordAdded(Runnable onWordAdded) {
        this.onWordAdded = onWordAdded;
    }

    // Setter cho từ khóa ban đầu
    public void setInitialWord(String initialWord) {
        this.initialWord = initialWord;
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

        if (dictionaryManagement == null) { showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Dictionary Manager chưa sẵn sàng."); return; }
        // activeSource = dictionaryManager.getActiveSource(); // Lấy lại nguồn active trước khi lưu (đảm bảo dùng nguồn hiện tại)
        DictionarySource activeSource = dictionaryManagement.getActiveSource();


        // Nếu không phải là từ được truyền từ Welcome (initialWord), kiểm tra trùng lặp headword TRÊN NGUỒN ACTIVE
        if (initialWord == null || !initialWord.equalsIgnoreCase(headword)) {
            if (activeSource.lookupEntry(headword).isPresent()) { // Lookup trên nguồn active
                showAlert(Alert.AlertType.WARNING, "Từ đã tồn tại", "Từ '" + headword + "' đã tồn tại trong từ điển đang hoạt động.");
                return;
            }
        }


        // Thu thập dữ liệu Senses, Definitions, Examples (giữ nguyên)
        List<WordSense> senses = new ArrayList<>();
        boolean hasAtLeastOneSense = false; /* ... logic thu thập senses ... */
        for (WordSenseInputGroup senseGroup : senseInputGroups) {
            Optional<WordSense> senseOptional = senseGroup.getData();
            if (senseOptional.isPresent()) {
                senses.add(senseOptional.get());
                hasAtLeastOneSense = true;
            }
        }


        if (!hasAtLeastOneSense) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Phải thêm ít nhất một loại từ/cách dùng (Sense)."); return; }

        // Tạo DictionaryEntry (giữ nguyên)
        DictionaryEntry newEntry = new DictionaryEntry(headword, pronunciation);
        senses.forEach(newEntry::addSense);

        // *** Lưu vào từ điển (sử dụng nguồn đang hoạt động) ***
        boolean added = activeSource.addEntry(newEntry); // Gọi addEntry trên nguồn active

        if (added) {
            System.out.println("Thêm từ thành công: " + headword + " vào nguồn " + activeSource.getDisplayName());
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm từ '" + headword + "' vào từ điển.");
            if (onWordAdded != null) { onWordAdded.run(); }

            // *** GỌI HÀM LƯU FILE TRÊN NGUỒN ĐANG HOẠT ĐỘNG (HOẶC TẤT CẢ CÁC NGUỒN) ***
            activeSource.saveData(); // Lưu chỉ nguồn active
            // dictionaryManager.saveAllSourcesData(); // Hoặc lưu tất cả nguồn

            System.out.println("Đã lưu thay đổi sau khi thêm từ.");

            closeDialog();
        } else { showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm từ '" + headword + "'."); }
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
