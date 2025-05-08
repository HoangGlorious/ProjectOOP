package com.application.test.Controller;


import com.application.test.Model.Thesaurus;
import com.application.test.Model.ThesaurusResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThesaurusController {
    @FXML
    private Button TBackButton;
    @FXML
    private TextField thesaurusSearchBar;
    @FXML
    private TextArea thesaurusResultArea;
    private Runnable onGoBackToWelcome;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    @FXML
    protected void handleThesaurusSearch() {
        String word = thesaurusSearchBar.getText().trim().toLowerCase();
        if (!word.isEmpty()) {
            thesaurusResultArea.setText("Thesaurus result for " + word);
            loadThesaurus(word);
        } else {
            thesaurusResultArea.setText("Please enter a word to search!");
        }
    }

    protected void loadThesaurus(String word) {
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

    public void resetScene() {
        System.out.println("Resetting Thesaurus scene...");
        if (thesaurusSearchBar != null) {
            thesaurusSearchBar.clear();
        }
        if (thesaurusResultArea != null) {
            thesaurusResultArea.clear();
        }
    }

    @FXML
    protected void thesaurusBackToWelcome() {
        System.out.println("Back to Welcome button clicked in GamesController. Signaling DictionaryApplication.");
        if (onGoBackToWelcome != null) {
            try {
                onGoBackToWelcome.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go back to Welcome callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoBackToWelcome is not set in GamesController!");
        }
    }
}
