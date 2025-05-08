package com.application.test.Controller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent; // Sửa import

public class WordleMenuController {
    @FXML
    private AnchorPane anchorPane;

    private Runnable onGoBackToGames;
    private Runnable onLaunchWordle;
    private Runnable onLaunchDailyWordle;

    public void setOnGoBackToGames(Runnable callback) {
        this.onGoBackToGames = callback;
    }

    public void setOnLaunchWordle(Runnable callback) {
        this.onLaunchWordle = callback;
    }

    public void setOnLaunchDailyWordle(Runnable callback) {
        this.onLaunchDailyWordle = callback;
    }

    @FXML
    protected void handleBackToGames(ActionEvent event) {
        if (onGoBackToGames != null) {
            onGoBackToGames.run();
        }
    }

    @FXML
    protected void handlePlay(ActionEvent event) {
        if (onLaunchWordle != null) {
            onLaunchWordle.run();
        }
    }

    @FXML
    protected void handlePlayDaily(ActionEvent event) {
        if (onLaunchDailyWordle != null) {
            onLaunchDailyWordle.run();
        }
    }
}