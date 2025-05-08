package com.application.test.Controller;


import com.application.test.Model.Thesaurus;
import com.application.test.Model.ThesaurusResult;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.scene.text.Text;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
=======
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
>>>>>>> e80a7117434ad2c661fd92b830f0beab4173efd9


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThesaurusController {
    @FXML
    private Button TBackButton;
    @FXML
    private TextField thesaurusSearchBar;
    @FXML
<<<<<<< HEAD
    private VBox thesaurusResultContainer;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    private void initialize() {
        thesaurusResultContainer.getChildren().clear();
    }
=======
    private TextArea thesaurusResultArea;
    private Runnable onGoBackToWelcome;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
>>>>>>> e80a7117434ad2c661fd92b830f0beab4173efd9

    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    @FXML
<<<<<<< HEAD
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
=======
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
>>>>>>> e80a7117434ad2c661fd92b830f0beab4173efd9
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
