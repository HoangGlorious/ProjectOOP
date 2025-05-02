package com.application.test.Model;

import com.application.test.Controller.AddWordDialogController;

import com.application.test.Controller.SenseContainerController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WordSenseInputGroup {
    private final SenseContainerController parentController;
    private final VBox senseUI; // Container chính cho UI của sense này

    private TextField partOfSpeechField;
    private VBox definitionsContainer; // Container cho Definition inputs
    private VBox examplesContainer; // Container cho Example inputs

    private List<TextField> definitionFields; // List các input field cho definitions
    private List<HBox> exampleInputGroups; // List các HBox chứa input fields cho examples (Anh + Việt)


    public WordSenseInputGroup(SenseContainerController parentController) {
        this.parentController = parentController;
        this.senseUI = new VBox(5); // Container chính cho sense này, spacing 5
        senseUI.setPadding(new Insets(10)); // Padding cho mỗi sense
        senseUI.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;"); // Border cho dễ nhìn

        definitionFields = new ArrayList<>();
        exampleInputGroups = new ArrayList<>();

        // Xây dựng UI cho sense
        buildUI();
    }

    // Xây dựng cấu trúc UI cho một Sense
    private void buildUI() {
        // Header cho sense (Loại từ và nút xóa sense)
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        partOfSpeechField = new TextField();
        partOfSpeechField.setPromptText("Loại từ (ví dụ: danh từ)");
        HBox.setHgrow(partOfSpeechField, Priority.ALWAYS); // Cho field giãn hết cỡ

        Button removeSenseButton = new Button("Xóa Sense");
        removeSenseButton.setOnAction(event -> parentController.removeSenseGroup(this)); // Gọi hàm xóa trong Controller cha

        headerBox.getChildren().addAll(partOfSpeechField, removeSenseButton);


        // Container cho Definitions
        definitionsContainer = new VBox(5); // spacing 5
        Label defLabel = new Label("Định nghĩa:");
        Button addDefinitionButton = new Button("+ Định nghĩa");
        addDefinitionButton.setOnAction(event -> handleAddDefinition(null)); // Thêm definition

        HBox defHeader = new HBox(5, defLabel, addDefinitionButton);
        defHeader.setAlignment(Pos.CENTER_LEFT);


        // Container cho Examples
        examplesContainer = new VBox(5); // spacing 5
        Label exLabel = new Label("Ví dụ:");
        Button addExampleButton = new Button("+ Ví dụ");
        addExampleButton.setOnAction(event -> handleAddExample()); // Thêm example

        HBox exHeader = new HBox(5, exLabel, addExampleButton);
        exHeader.setAlignment(Pos.CENTER_LEFT);


        // Thêm tất cả vào container chính của sense
        senseUI.getChildren().addAll(headerBox, defHeader, definitionsContainer, exHeader, examplesContainer);
    }


    /**
     * Xử lý khi nhấn nút "+ Định nghĩa".
     * Thêm một input field mới cho Definition.
     * @param initialText Văn bản ban đầu (dùng khi sửa từ).
     */
    public void handleAddDefinition(String initialText) {
        TextField definitionField = new TextField();
        definitionField.setPromptText("Enter definition");
        if (initialText != null) {
            definitionField.setText(initialText);
        }
        // Thêm nút xóa cho từng definition
        Button removeDefButton = new Button("-");
        removeDefButton.setOnAction(event -> removeDefinitionField(definitionField));

        HBox defInputBox = new HBox(5, definitionField, removeDefButton);
        HBox.setHgrow(definitionField, Priority.ALWAYS);

        definitionFields.add(definitionField); // Thêm vào list quản lý
        definitionsContainer.getChildren().add(defInputBox); // Thêm UI vào container
    }

    // Xóa một input field Definition
    private void removeDefinitionField(TextField field) {
        definitionFields.remove(field);
        // Tìm và xóa HBox chứa field này từ UI
        definitionsContainer.getChildren().removeIf(node -> node instanceof HBox && ((HBox)node).getChildren().contains(field));
    }


    /**
     * Xử lý khi nhấn nút "+ Ví dụ".
     * Thêm một cặp input field mới cho Example (Anh + Việt).
     */
    public void handleAddExample() {
        TextField engField = new TextField();
        engField.setPromptText("Ví dụ tiếng Anh");
        TextField vieField = new TextField();
        vieField.setPromptText("Nghĩa tiếng Việt");

        // Thêm nút xóa cho từng cặp ví dụ
        Button removeExButton = new Button("-");
        removeExButton.setOnAction(event -> removeExampleInputGroup(engField)); // Sử dụng engField để định danh nhóm

        HBox exampleBox = new HBox(5, engField, new Label("+"), vieField, removeExButton); // HBox cho 1 cặp ví dụ
        HBox.setHgrow(engField, Priority.ALWAYS);
        HBox.setHgrow(vieField, Priority.ALWAYS);
        exampleBox.setAlignment(Pos.CENTER_LEFT);

        exampleInputGroups.add(exampleBox); // Thêm vào list quản lý
        examplesContainer.getChildren().add(exampleBox); // Thêm UI vào container
    }

    // Xóa một cặp input field Example
    private void removeExampleInputGroup(TextField engField) {
        // Tìm và xóa HBox chứa engField này từ list quản lý
        // Dòng này có thể đúng vì exampleInputGroups là List<HBox>
        exampleInputGroups.removeIf(box -> box.getChildren().contains(engField));

        // Tìm và xóa HBox chứa engField này từ UI container
        // Cần ép kiểu Node thành HBox
        examplesContainer.getChildren().removeIf(node -> {
            return node instanceof HBox && ((HBox) node).getChildren().contains(engField);
        });
    }


    /**
     * Thu thập dữ liệu WordSense từ các input fields trong nhóm này.
     * @return Optional chứa WordSense nếu dữ liệu hợp lệ (ít nhất có loại từ và 1 definition).
     *         Optional rỗng nếu dữ liệu không hợp lệ.
     */
    public Optional<WordSense> getData() {
        String partOfSpeech = partOfSpeechField.getText().trim();

        // Yêu cầu phải có loại từ
        if (partOfSpeech.isEmpty()) {
            System.err.println("Sense trống loại từ."); // Có thể hiển thị lỗi trên UI dialog
            return Optional.empty();
        }

        WordSense sense = new WordSense(partOfSpeech);

        // Thu thập Definitions
        boolean hasDefinition = false;
        for (TextField defField : definitionFields) {
            String definition = defField.getText().trim();
            if (!definition.isEmpty()) {
                sense.addDefinition(definition);
                hasDefinition = true;
            }
        }

        // Yêu cầu phải có ít nhất 1 definition
        if (!hasDefinition) {
            System.err.println("Sense '" + partOfSpeech + "' thiếu định nghĩa."); // Có thể hiển thị lỗi trên UI dialog
            // Có thể xóa sense này nếu nó không có definition
            // parentController.removeSenseGroup(this);
            return Optional.empty();
        }


        // Thu thập Examples
        for (HBox exampleBox : exampleInputGroups) {
            TextField engField = (TextField) exampleBox.getChildren().get(0); // TextField tiếng Anh
            TextField vieField = (TextField) exampleBox.getChildren().get(2); // TextField tiếng Việt (sau Label "+")

            String eng = engField.getText().trim();
            String vie = vieField.getText().trim();

            if (!eng.isEmpty()) { // Chỉ thêm ví dụ nếu có phần tiếng Anh
                sense.addExample(new ExamplePhrase(eng, vie));
            }
        }

        return Optional.of(sense);
    }

    /**
     * Trả về container UI của nhóm input này.
     */
    public VBox getUI() {
        return senseUI;
    }

    // (Tùy chọn) Phương thức để nạp dữ liệu WordSense vào các input fields (dùng khi SỬA từ)
    public void loadData(WordSense sense) {
        if (sense == null) return;

        partOfSpeechField.setText(sense.getPartOfSpeech());

        // Xóa các input definitions/examples mặc định hoặc cũ
        definitionsContainer.getChildren().clear();
        definitionFields.clear();
        examplesContainer.getChildren().clear();
        exampleInputGroups.clear();

        // Nạp definitions
        for (String def : sense.getDefinitions()) {
            handleAddDefinition(def); // Sử dụng hàm thêm definition có initial text
        }

        // Nạp examples
        for (ExamplePhrase example : sense.getExamples()) {
            TextField engField = new TextField(); engField.setText(example.getEnglish()); engField.setPromptText("Ví dụ tiếng Anh");
            TextField vieField = new TextField(); vieField.setText(example.getVietnamese()); vieField.setPromptText("Nghĩa tiếng Việt");
            Button removeExButton = new Button("-");
            removeExButton.setOnAction(event -> removeExampleInputGroup(engField));
            HBox exampleBox = new HBox(5, engField, new Label("+"), vieField, removeExButton);
            HBox.setHgrow(engField, Priority.ALWAYS); HBox.setHgrow(vieField, Priority.ALWAYS); exampleBox.setAlignment(Pos.CENTER_LEFT);
            exampleInputGroups.add(exampleBox); examplesContainer.getChildren().add(exampleBox);
        }
        // Cần đảm bảo ít nhất 1 definition field nếu danh sách definition rỗng (dùng hàm addDefinition sau khi load)
        if (sense.getDefinitions().isEmpty()) {
            handleAddDefinition(null);
        }
    }

}
