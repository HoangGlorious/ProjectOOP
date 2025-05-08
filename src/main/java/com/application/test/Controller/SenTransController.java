package com.application.test.Controller;

import com.application.test.Model.SentenceTranslation;
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

        translateButton.setOnAction(event -> handleSenTranslation() );
    }

    @FXML
    private void handleSenTranslation() {
        String sentence = sentenceInput.getText().trim();
        if (sentence.isEmpty()) {
            translationArea.setText("Please enter a sentence");
            return;
        }

        try {
            String languageSource = langSource.getValue();
            String sourceLang = languageSource.equals("Eng-Vie") ? "en" : "vi";
            String targetLang = languageSource.equals("Eng-Vie") ? "vi" : "en";

            String translatedSen = senTrans.senTranslate(sentence, sourceLang, targetLang);
            translationArea.setText(translatedSen);
            status.setText("Translation successful!");
        } catch (Exception e) {
            e.printStackTrace();
            translationArea.setText("Error: " + e.getMessage());
            status.setText("Translation failed!");
        }
    }
}
