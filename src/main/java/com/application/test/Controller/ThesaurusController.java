package com.application.test.Controller;


import com.application.test.Model.Thesaurus;
import com.application.test.Model.ThesaurusResult;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThesaurusController {
    @FXML
    private TextField thesaurusSearchBar;
    @FXML
    private VBox thesaurusResultContainer;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    private void initialize() {
        thesaurusResultContainer.getChildren().clear();
    }

    @FXML
    private void handleThesaurusSearch() {
        String word = thesaurusSearchBar.getText().trim();
        if (word.isEmpty()) {
            showResultText("Please enter a word");
            return;
        }

        showResultText("Searching for: " + word + "...");

        executor.execute(() -> {
            ThesaurusResult result = null;
            try {
                result = Thesaurus.lookup(word);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ThesaurusResult finalResult = result;
            javafx.application.Platform.runLater(() -> {
                if (finalResult.hasError()) {
                    showResultText("Error: " + finalResult.getError());
                    return;
                }

                displayThesaurusResult(finalResult);
            });
        });
    }

    private void displayThesaurusResult(ThesaurusResult result) {
        thesaurusResultContainer.getChildren().clear();

        // Thêm title
        Text title = new Text("Results for: " + result.getWord() + "\n\n");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        thesaurusResultContainer.getChildren().add(title);

        // Thêm antonym
        if (!result.getSynonyms().isEmpty()) {
            Text synTitle = new Text("Synonyms:\n");
            synTitle.setStyle("-fx-font-weight: bold;");
            thesaurusResultContainer.getChildren().add(synTitle);

            TextFlow synonymsFlow = new TextFlow();
            synonymsFlow.setStyle("-fx-padding: 0 0 10 0;");

            for (String synonym : result.getSynonyms()) {
                Hyperlink link = new Hyperlink(synonym + " ");
                link.setOnAction(e -> {
                    thesaurusSearchBar.setText(synonym);
                    handleThesaurusSearch();
                });
                synonymsFlow.getChildren().add(link);
            }

            thesaurusResultContainer.getChildren().add(synonymsFlow);
        }

        // Thêm synonym
        if (!result.getAntonyms().isEmpty()) {
            Text antTitle = new Text("Antonyms:\n");
            antTitle.setStyle("-fx-font-weight: bold;");
            thesaurusResultContainer.getChildren().add(antTitle);

            TextFlow antonymsFlow = new TextFlow();

            for (String antonym : result.getAntonyms()) {
                Hyperlink link = new Hyperlink(antonym + " ");
                link.setOnAction(e -> {
                    thesaurusSearchBar.setText(antonym);
                    handleThesaurusSearch();
                });
                antonymsFlow.getChildren().add(link);
            }

            thesaurusResultContainer.getChildren().add(antonymsFlow);
        }

        if (result.getSynonyms().isEmpty() && result.getAntonyms().isEmpty()) {
            showResultText("No thesaurus data found");
        }
    }

    private void showResultText(String message) {
        thesaurusResultContainer.getChildren().clear();
        thesaurusResultContainer.getChildren().add(new Text(message));
    }
}
