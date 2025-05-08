package com.application.test.Controller;


import com.application.test.Model.Thesaurus;
import com.application.test.Model.ThesaurusResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThesaurusController {
    @FXML
    private TextField thesaurusSearchBar;
    @FXML
    private TextArea thesaurusResultArea;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private void handleThesaurusSearch() {
        String word = thesaurusSearchBar.getText().trim().toLowerCase();
        if (!word.isEmpty()) {
            thesaurusResultArea.setText("Thesaurus result for " + word);
            loadThesaurus(word);
        } else {
            thesaurusResultArea.setText("Please enter a word to search!");
        }
    }

    private void loadThesaurus(String word) {
        thesaurusResultArea.clear();
        thesaurusResultArea.appendText("Thesaurus result for " + word + "\n");

        executorService.execute(() -> {
            try {
                ThesaurusResult result = Thesaurus.lookup(word);
                Platform.runLater(() -> {
                    thesaurusResultArea.clear();

                    if (result.hasError()) {
                        thesaurusResultArea.appendText("Error: " + result.getError());
                        return;
                    }

                    Label thesaurusTitle = new Label("Thesaurus for " + word);
                    thesaurusResultArea.appendText(thesaurusTitle.getText() + "\n");

                    if (!result.getSynonyms().isEmpty()) {
                        Label synonymsLabel = new Label("Synonyms:");
                        thesaurusResultArea.appendText(synonymsLabel.getText() + "\n");

                        FlowPane synonymsPane = new FlowPane();
                        synonymsPane.setHgap(10);
                        synonymsPane.setVgap(10);

                        for (String synonym : result.getSynonyms()) {
                            Hyperlink synLink = new Hyperlink(synonym);
                            synLink.setOnAction(event -> {
                                thesaurusSearchBar.setText(synonym);
                                handleThesaurusSearch();
                            });
                            synonymsPane.getChildren().add(synLink);
                        }

                        thesaurusResultArea.appendText(String.valueOf(synonymsPane));
                    }

                    if (!result.getAntonyms().isEmpty()) {
                        Label antonymsLabel = new Label("Antonyms:");
                        thesaurusResultArea.appendText(antonymsLabel.getText() + "\n");

                        FlowPane antonymsPane = new FlowPane();
                        antonymsPane.setHgap(10);
                        antonymsPane.setVgap(10);

                        for (String antonym : result.getAntonyms()) {
                            Hyperlink synLink = new Hyperlink(antonym);
                            synLink.setOnAction(event -> {
                                thesaurusSearchBar.setText(antonym);
                                handleThesaurusSearch();
                            });
                            antonymsPane.getChildren().add(synLink);
                        }

                        thesaurusResultArea.appendText(String.valueOf(antonymsPane));
                    }

                    if (result.getSynonyms().isEmpty() && result.getAntonyms().isEmpty()) {
                        thesaurusResultArea.appendText(String.valueOf(new Label("No thesaurus data available for this word.")));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    thesaurusResultArea.clear();
                    thesaurusResultArea.appendText("Error: " + e.getMessage());
                });
            }
        });
    }
}
