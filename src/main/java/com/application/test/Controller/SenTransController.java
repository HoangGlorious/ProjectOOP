package com.application.test.Controller;

import com.application.test.Model.SentenceTranslation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SenTransController {
    @FXML
    private ComboBox<String> langSource;
    @FXML
    private TextField sentenceInput;
    @FXML
    private Button translateButton;
    @FXML
    private TextArea translationArea;
    @FXML
    private Label status;

    private Runnable onGoBackToWelcome;
    private SentenceTranslation senTrans = new SentenceTranslation();

    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    @FXML
    private void initialize() {
        langSource.getItems().addAll("Eng-Vie", "Vie-Eng");
        langSource.setValue("Eng-Vie");
        sentenceInput.clear();
        translationArea.clear();
        translateButton.setOnAction(event -> handleSenTranslation());
    }

    @FXML
    private void handleSenTranslation() {
        // Lấy câu cần dịch từ phần nhập câu, trả về thông báo nếu chưa nhập câu
        String sentence = sentenceInput.getText().trim();
        if (sentence.isEmpty()) {
            translationArea.setText("Please enter a sentence");
            return;
        }

        try {
            // Lấy thông tin từ ComboBox để quyết định dịch từ ngôn ngữ nào sang ngôn ngữ nào
            String languageSource = langSource.getValue();
            String sourceLang = languageSource.equals("Eng-Vie") ? "en" : "vi";
            String targetLang = languageSource.equals("Eng-Vie") ? "vi" : "en";

            // Dịch câu và in kết quả ra transtationArea, cập nhật status Label
            String translatedSen = senTrans.senTranslate(sentence, sourceLang, targetLang);
            translationArea.setText(translatedSen);
            status.setText("Translation successful!");
        } catch (Exception e) {
            e.printStackTrace();
            translationArea.setText("Error: " + e.getMessage());
            status.setText("Translation failed!");
        }
    }

    public void resetScene() {
        System.out.println("Resetting SenTran scene... ");
        if (sentenceInput != null) {
            sentenceInput.clear();
        }
        if (translationArea != null) {
            translationArea.clear();
        }
    }

    @FXML
    protected void senBackToWelcome() {
        System.out.println("Back to Welcome button clicked in SenTran." +
                " Signaling DictionaryApplication.");
        if (onGoBackToWelcome != null) {
            try {
                onGoBackToWelcome.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go back to Welcome callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoBackToWelcome is not set in SenTran!");
        }
    }
}

