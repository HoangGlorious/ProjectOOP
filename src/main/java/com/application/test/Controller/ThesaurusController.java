package com.application.test.Controller;


import com.application.test.Model.Thesaurus;
import com.application.test.Model.ThesaurusResult;
import javafx.fxml.FXML;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

import javafx.scene.control.*;


import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThesaurusController {
    @FXML
    private HBox thesaurusTopHBox;
    @FXML
    private Button thesaurusSearchButton;
    @FXML
    private Button TBackButton;
    @FXML
    private TextField thesaurusSearchBar;
    @FXML

    private VBox thesaurusResultContainer;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    private void initialize() {
        // Hàm initialize sẽ xóa text trong phần kết quả của những lần tra trước
        thesaurusResultContainer.getChildren().clear();
    }


    private Runnable onGoBackToWelcome;


    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    @FXML
    // Hàm xử lý việc tìm thesaurus
    private void handleThesaurusSearch() {
        // Lấy từ khóa từ phần thanh tìm kiếm và thông báo nếu thanh tìm kiếm trống
        String word = thesaurusSearchBar.getText().trim();
        if (word.isEmpty()) {
            showResultText("Please enter a word");
            return;
        }

        // Cập nhật trạng thái đang tìm thesaurus cho từ
        showResultText("Searching for thesaurus of word: " + word + "...");

        // Tìm thesaurus trong nền bằng executor
        executor.execute(() -> {
            ThesaurusResult result = null;
            try {
                // Gọi hàm lookup thesaurus của từ
                result = Thesaurus.lookup(word);
            } catch (Exception e) {
                e.printStackTrace();
                ThesaurusResult errorResult = new ThesaurusResult("", List.of(), List.of(), e.getMessage());
                ThesaurusResult finalResult = errorResult;
                javafx.application.Platform.runLater(() -> {
                    showResultText("Error: " + finalResult.getError());
                });
            }

            // Khai báo kết quả tìm thesaurus
            ThesaurusResult finalResult = result;

            // Trả về lỗi nếu có hoặc trưng bày kết quả nếu không có lỗi
            javafx.application.Platform.runLater(() -> {
                if (finalResult.hasError()) {
                    showResultText("Error: " + finalResult.getError());
                    return;
                }

                // Gọi hàm trưng bày kết quả
                displayThesaurusResult(finalResult);
            });
        });
    }


    // Hàm trưng bày kết quả
    // Mỗi từ đông nghĩa và trái nghĩa đều là các hyperlink dẫn đến thesaurus của chúng
    private void displayThesaurusResult(ThesaurusResult result) {
        thesaurusResultContainer.getChildren().clear();

        // Thêm title
        Text title = new Text("Thesaurus for: " + result.getWord() + "\n\n");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        thesaurusResultContainer.getChildren().add(title);

        // Thêm synonym và trưng bày dưới dạng TextFlow
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

        // Thêm antonym và trưng bày dưới dạng TextFlow
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

        // Xử lý trường hợp không có dữ liệu thesaurus
        if (result.getSynonyms().isEmpty() && result.getAntonyms().isEmpty()) {
            showResultText("No thesaurus data found");
        }
    }

    // Hàm phụ để cập nhật vùng kết quả thesaurus
    private void showResultText(String message) {
        thesaurusResultContainer.getChildren().clear();
        thesaurusResultContainer.getChildren().add(new Text(message));
    }


    public void resetScene() {
        System.out.println("Resetting Thesaurus scene...");
        if (thesaurusSearchBar != null) {
            thesaurusSearchBar.clear();
        }
        if (thesaurusResultContainer != null) {
            thesaurusResultContainer.getChildren().clear();
        }
    }

    @FXML
    protected void thesaurusBackToWelcome() {
        System.out.println("Back to Welcome button clicked in Thesaurus." +
                " Signaling DictionaryApplication.");
        if (onGoBackToWelcome != null) {
            try {
                onGoBackToWelcome.run();
            } catch (RuntimeException e) {
                System.err.println("Error executing go back to Welcome callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Callback onGoBackToWelcome is not set in Thesaurus!");
        }
    }
}
