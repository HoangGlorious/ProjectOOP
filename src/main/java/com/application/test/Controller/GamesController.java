package com.application.test.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;

public class GamesController {

    @FXML
    private VBox gamesPane;

    @FXML
    protected void launchWordle(MouseEvent event) {
        try {
            // Load FXML with the correct path that matches your project structure
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/wordle_view.fxml"));
            Parent wordleRoot = loader.load();

            // Create scene and add CSS if it exists
            Scene wordleScene = new Scene(wordleRoot);

            // Try to load CSS with the correct path
            String cssPath = "/com/application/test/css/wordle.css";
            if (getClass().getResource(cssPath) != null) {
                wordleScene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                System.out.println("CSS loaded successfully");
            } else {
                System.err.println("Warning: CSS file not found at: " + cssPath);
                // Try alternative path
                cssPath = "/com/application/test/wordle.css";
                if (getClass().getResource(cssPath) != null) {
                    wordleScene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                    System.out.println("CSS loaded from alternative path");
                }
            }

            // Get the current stage
            Stage primaryStage = (Stage) gamesPane.getScene().getWindow();

            // Set the new scene
            primaryStage.setTitle("Wordle Game");
            primaryStage.setScene(wordleScene);
            primaryStage.sizeToScene();

            // Debug info
            System.out.println("Wordle game launched successfully");

        } catch (IOException e) {
            System.err.println("Error loading Wordle game: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    protected void backToWelcome() {
        try {
            // Load welcome screen with the correct path
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/welcome.fxml"));
            Parent welcomeRoot = loader.load();

            Scene welcomeScene = new Scene(welcomeRoot);
            Stage primaryStage = (Stage) gamesPane.getScene().getWindow();
            primaryStage.setTitle("Ứng dụng Từ điển");
            primaryStage.setScene(welcomeScene);
            primaryStage.sizeToScene();

            System.out.println("Returned to welcome screen");
        } catch (IOException e) {
            System.err.println("Error returning to welcome screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}