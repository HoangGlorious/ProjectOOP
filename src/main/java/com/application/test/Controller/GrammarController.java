package com.application.test.Controller;

import com.application.test.Model.GrammarManagement;
import com.application.test.Model.GrammarRule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class GrammarController implements Initializable {

    @FXML private ComboBox<String> categoryBox;
    @FXML private ListView<String> tenseListView; // Giữ tên gốc từ FXML
    @FXML private TextField searchTenseField;      // Giữ tên gốc từ FXML
    @FXML private ListView<String> tenseSuggestionListView; // Giữ tên gốc từ FXML
    @FXML private TextArea formulaArea;
    @FXML private TextArea usageArea;
    @FXML private TextArea exampleArea;

    private GrammarManagement grammarManagement;
    private Consumer<Void> onGoBackToWelcome;

    public void setOnGoBackToWelcome(Consumer<Void> onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        grammarManagement = new GrammarManagement();
        grammarManagement.loadGrammarRules();

        // 1. Điền danh mục vào ComboBox một cách động
        List<String> distinctCategories = grammarManagement.getGrammarRules().stream()
                .map(GrammarRule::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        if (distinctCategories.isEmpty()) {
            distinctCategories.add("Không có danh mục");
        }
        categoryBox.setItems(FXCollections.observableArrayList(distinctCategories));
        if (!distinctCategories.contains("Không có danh mục") && !distinctCategories.isEmpty()) {
            categoryBox.getSelectionModel().selectFirst();
        }

        // 2. Listener cho ComboBox danh mục
        categoryBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newCategory) -> {
            if (newCategory != null && !"Không có danh mục".equals(newCategory)) {
                List<String> ruleTitles = grammarManagement.getGrammarRules().stream()
                        .filter(rule -> newCategory.equals(rule.getCategory()))
                        .map(GrammarRule::getTitle)
                        .sorted()
                        .collect(Collectors.toList());
                tenseListView.setItems(FXCollections.observableArrayList(ruleTitles));
                tenseListView.setVisible(true);
                tenseListView.setManaged(true);
                tenseListView.setPrefHeight(Math.min(ruleTitles.size() * 30, 400));
                clearContentAreas();
            } else {
                tenseListView.getItems().clear();
                tenseListView.setVisible(false);
                tenseListView.setManaged(false);
                clearContentAreas();
            }
        });

        // Tự động trigger listener của categoryBox để load mục đầu tiên (nếu có)
        // Điều này sẽ xảy ra tự động nếu selectFirst() ở trên trigger listener.
        // Nếu không, bạn có thể cần gọi một hàm riêng để load rule list cho category đầu tiên.
        // Cách đơn giản là gọi lại logic cập nhật ListView sau khi set item cho ComboBox
        if (categoryBox.getSelectionModel().getSelectedItem() != null &&
                !"Không có danh mục".equals(categoryBox.getSelectionModel().getSelectedItem())) {
            String selectedCategoryInitial = categoryBox.getSelectionModel().getSelectedItem();
            List<String> initialRuleTitles = grammarManagement.getGrammarRules().stream()
                    .filter(rule -> selectedCategoryInitial.equals(rule.getCategory()))
                    .map(GrammarRule::getTitle)
                    .sorted()
                    .collect(Collectors.toList());
            tenseListView.setItems(FXCollections.observableArrayList(initialRuleTitles));
            tenseListView.setVisible(true);
            tenseListView.setManaged(true);
            tenseListView.setPrefHeight(Math.min(initialRuleTitles.size() * 30, 400));
            clearContentAreas();
        }


        // 3. Listener cho tenseListView: Khi chọn một mục, hiển thị nội dung
        tenseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedCategory = categoryBox.getSelectionModel().getSelectedItem();
                if (selectedCategory != null && !"Không có danh mục".equals(selectedCategory)) {
                    displayRuleContent(selectedCategory, newValue);
                }
            }
        });

        // 4. Listener cho TextField tìm kiếm
        searchTenseField.textProperty().addListener((observable, oldValue, newValue) -> {
            showRuleSuggestions(newValue); // Đổi tên phương thức này nếu muốn, nhưng giữ tên biến là searchTenseField
        });

        // 5. Listener cho tenseSuggestionListView: Khi chọn gợi ý, hiển thị nội dung
        tenseSuggestionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) { // newValue ở đây là title của rule
                // Cần tìm rule hoàn chỉnh để biết category của nó
                GrammarRule selectedRuleFromSuggestion = grammarManagement.getGrammarRules().stream()
                        .filter(r -> r.getTitle().equals(newValue)) // Giả sử title là duy nhất hoặc bạn có cơ chế khác
                        // Nếu title không duy nhất qua các category, bạn cần thêm logic để chọn đúng rule
                        // Ví dụ, nếu category đang được chọn, ưu tiên rule trong category đó
                        .findFirst()
                        .orElse(null);

                if (selectedRuleFromSuggestion != null) {
                    searchTenseField.setText(selectedRuleFromSuggestion.getTitle());
                    tenseSuggestionListView.setVisible(false);
                    tenseSuggestionListView.setManaged(false);

                    // Hiển thị nội dung rule
                    displayRuleContent(selectedRuleFromSuggestion.getCategory(), selectedRuleFromSuggestion.getTitle());

                    // Cập nhật lựa chọn trên categoryBox và tenseListView
                    categoryBox.getSelectionModel().select(selectedRuleFromSuggestion.getCategory());
                    // Đảm bảo tenseListView đã được cập nhật items cho category này trước khi select
                    // Listener của categoryBox nên tự động làm điều này. Nếu không, bạn cần cập nhật items ở đây.
                    tenseListView.getSelectionModel().select(selectedRuleFromSuggestion.getTitle());
                }
            }
        });
    }

    // Đổi tên phương thức cho rõ ràng hơn, nhưng nó vẫn được gọi từ listener của searchTenseField
    private void showRuleSuggestions(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            tenseSuggestionListView.setVisible(false);
            tenseSuggestionListView.setManaged(false);
            return;
        }

        String selectedCategory = categoryBox.getSelectionModel().getSelectedItem();

        List<String> matchingRuleTitles = grammarManagement.getGrammarRules().stream()
                .filter(rule ->
                        (selectedCategory == null || "Không có danh mục".equals(selectedCategory) || selectedCategory.equals(rule.getCategory())) &&
                                rule.getTitle().toLowerCase().contains(keyword.toLowerCase())
                )
                .map(GrammarRule::getTitle)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        tenseSuggestionListView.setItems(FXCollections.observableArrayList(matchingRuleTitles));

        if (!matchingRuleTitles.isEmpty()) {
            tenseSuggestionListView.setVisible(true);
            tenseSuggestionListView.setManaged(true);
            tenseSuggestionListView.setPrefHeight(Math.min(matchingRuleTitles.size() * 30, 150));
        } else {
            tenseSuggestionListView.setVisible(false);
            tenseSuggestionListView.setManaged(false);
        }
    }

    // Tên phương thức đã được điều chỉnh, nhận category và ruleTitle
    private void displayRuleContent(String category, String ruleTitle) {
        GrammarRule rule = grammarManagement.getGrammarRules().stream()
                .filter(r -> category.equals(r.getCategory()) && ruleTitle.equals(r.getTitle()))
                .findFirst()
                .orElse(null);

        if (rule != null) {
            StringBuilder formulaText = new StringBuilder();

            boolean hasAffirmative = rule.getFormulaAffirmative() != null && !rule.getFormulaAffirmative().isEmpty();
            boolean hasNegative = rule.getFormulaNegative() != null && !rule.getFormulaNegative().isEmpty();
            boolean hasQuestion = rule.getFormulaQuestion() != null && !rule.getFormulaQuestion().isEmpty();

            // Kịch bản 1: Chỉ có phần "khẳng định" (coi là công thức/quy tắc chung)
            if (hasAffirmative && !hasNegative && !hasQuestion) {
                formulaText.append(rule.getFormulaAffirmative()); // Không thêm tiền tố
            }
            // Kịch bản 2: Có nhiều phần (khẳng định, phủ định, nghi vấn) hoặc có phủ định/nghi vấn
            else {
                if (hasAffirmative) {
                    formulaText.append("**Khẳng định:**\n").append(rule.getFormulaAffirmative()).append("\n\n");
                }
                if (hasNegative) {
                    formulaText.append("**Phủ định:**\n").append(rule.getFormulaNegative()).append("\n\n");
                }
                if (hasQuestion) {
                    formulaText.append("**Nghi vấn:**\n").append(rule.getFormulaQuestion()).append("\n\n");
                }
            }

            formulaArea.setText(formulaText.toString().trim());
            usageArea.setText(rule.getUsage() != null ? rule.getUsage() : "Không có thông tin.");
            exampleArea.setText(rule.getExamples() != null ? rule.getExamples() : "Không có ví dụ.");

        } else {
            clearContentAreas();
            formulaArea.setText("Không tìm thấy nội dung cho '" + ruleTitle + "' trong danh mục '" + category + "'.");
        }
    }
    private void clearContentAreas() {
        formulaArea.clear();
        usageArea.clear();
        exampleArea.clear();
    }

    @FXML
    private void handleBack() {
        if (onGoBackToWelcome != null) {
            onGoBackToWelcome.accept(null);
        }
    }

    // Giữ tên gốc từ FXML onAction
    @FXML
    private void handleSearchTenseAction() {
        String searchText = searchTenseField.getText().trim();
        String selectedCategory = categoryBox.getSelectionModel().getSelectedItem();

        if (!searchText.isEmpty()) {
            GrammarRule foundRule = null;
            if (selectedCategory != null && !"Không có danh mục".equals(selectedCategory)) {
                foundRule = grammarManagement.getGrammarRules().stream()
                        .filter(r -> selectedCategory.equals(r.getCategory()) && r.getTitle().equalsIgnoreCase(searchText))
                        .findFirst().orElse(null);
            }

            if (foundRule == null) { // Nếu không tìm thấy trong category hiện tại hoặc không có category nào được chọn
                foundRule = grammarManagement.getGrammarRules().stream()
                        .filter(r -> r.getTitle().equalsIgnoreCase(searchText)) // Tìm trong tất cả các title
                        .findFirst().orElse(null);
            }

            if (foundRule != null) {
                displayRuleContent(foundRule.getCategory(), foundRule.getTitle());
                tenseSuggestionListView.setVisible(false);
                tenseSuggestionListView.setManaged(false);

                categoryBox.getSelectionModel().select(foundRule.getCategory());
                // Listener của categoryBox nên tự động cập nhật tenseListView, sau đó chúng ta select item
                tenseListView.getSelectionModel().select(foundRule.getTitle());
            } else {
                formulaArea.setText("Không tìm thấy quy tắc nào khớp với: '" + searchText + "'");
                usageArea.clear();
                exampleArea.clear();
            }
        }
    }
}