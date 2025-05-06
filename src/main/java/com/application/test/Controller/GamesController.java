package com.application.test.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.function.Consumer;

public class GamesController {
    private Scene wordleMenuScene; // Added scene for WordleMenu
    private WordleMenuController wordleMenuControllerInstance; // Added controller for WordleMenu
    @FXML
    private VBox gamesPane;
    private Runnable onGoBackToWelcome;
    private Consumer<String> onLaunchSpecificGame;


    public void setOnGoBackToWelcome(Runnable onGoBackToWelcome) {
        this.onGoBackToWelcome = onGoBackToWelcome;
    }

    public void setOnLaunchSpecificGame(Consumer<String> onLaunchSpecificGame) {
        this.onLaunchSpecificGame = onLaunchSpecificGame;
    }

    @FXML
    protected void launchWordle(MouseEvent event) {
        System.out.println("Launching Wordle game from Game Menu.");
        if (onLaunchSpecificGame != null) {
            try {
                onLaunchSpecificGame.accept("wordle"); // Signal to launch Wordle menu
            } catch (RuntimeException e) {
                System.err.println("Error executing launch Wordle callback: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Direct navigation as fallback if callback is not set
            System.err.println("Callback onLaunchSpecificGame is not set in GamesController! Trying direct navigation...");
            try {
                // Load the WordleMenu directly as a fallback
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/application/test/view/wordlemenu.fxml"));
                Parent wordleMenuRoot = loader.load();

                Scene wordleMenuScene = new Scene(wordleMenuRoot);


                Stage stage = (Stage) gamesPane.getScene().getWindow();
                stage.setTitle("Wordle Menu");
                stage.setScene(wordleMenuScene);
                stage.sizeToScene();

                System.out.println("Loaded Wordle Menu screen using direct navigation");
            } catch (IOException e) {
                System.err.println("Error during direct navigation to Wordle Menu: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void backToWelcome() {
        System.out.println("Back to Welcome button clicked in GamesController. Signaling DictionaryApplication.");

        // Signal DictionaryApplication to switch scene back to Welcome
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