package com.application.test.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class DailyWordleGame extends WordleGame {

    private static final String PREF_NODE_NAME = "/com/application/test/dailywordle";
    private static final String PREF_KEY_LAST_PLAYED_DATE = "lastPlayedDate";
    private static final String PREF_KEY_GAME_WON = "gameWon";
    private static final String PREF_KEY_CURRENT_ATTEMPT = "currentAttempt";
    private static final String PREF_KEY_ATTEMPTS = "attempts";
    private static final String PREF_KEY_ATTEMPTS_STATES = "attemptsStates";

    private LocalDate lastPlayedDateLoaded;
    private String dailyTargetWord;
    private boolean wasGameWon;

    public DailyWordleGame() {
        super();
        System.out.println("Initializing DailyWordleGame...");
        loadState();
        this.dailyTargetWord = determineDailyWord();
        this.targetWord = this.dailyTargetWord;

        if (isNewDayAndCanPlay()) {
            currentAttempt = 0;
            attempts.clear();
            attemptsStates.clear();
            wasGameWon = false;
            System.out.println("Starting new Daily Wordle for today. Target: " + targetWord);
        } else {
            System.out.println("Already played today or continuing. Target: " + targetWord);
            if (!canPlayTodayGenuine()) {
                currentAttempt = MAX_ATTEMPTS;
                System.out.println("Setting currentAttempt to MAX to reflect completed state.");
            } else {
                System.out.println("State indicates playable, maintaining current state.");
            }
        }
    }

    private String determineDailyWord() {
        if (validWords == null || validWords.isEmpty()) {
            throw new IllegalStateException("Valid word list is empty in DailyWordleGame!");
        }
        LocalDate today = LocalDate.now();
        long daysSinceEpoch = today.toEpochDay();
        int wordIndex = (int) (Math.abs(daysSinceEpoch) % validWords.size());
        String determinedWord = validWords.get(wordIndex);
        System.out.println("Determined word for " + today + " (index " + wordIndex + "): " + determinedWord);
        return determinedWord;
    }

    @Override
    public List<LetterState> makeGuess(String guess) {
        System.out.println("Attempting guess: " + guess);
        if (!canPlayTodayGenuine() && currentAttempt >= MAX_ATTEMPTS) {
            System.err.println("Cannot guess: Already played today.");
            return null;
        }
        List<LetterState> result = super.makeGuess(guess);
        if (result != null) {
            System.out.println("Guess accepted: " + guess);
            if (isGameOver()) {
                System.out.println("Daily game finished. Marking as played today.");
                wasGameWon = isGameWon();
                markAsPlayedToday();
            }
        } else {
            System.err.println("Guess rejected: Invalid or game over.");
        }
        return result;
    }

    private boolean isNewDayAndCanPlay() {
        LocalDate today = LocalDate.now();
        boolean isNewDay = (lastPlayedDateLoaded == null || !lastPlayedDateLoaded.equals(today));
        boolean canActuallyPlay = canPlayTodayGenuine();
        return isNewDay && canActuallyPlay;
    }

    public boolean canPlayTodayGenuine() {
        LocalDate today = LocalDate.now();
        boolean canPlay = lastPlayedDateLoaded == null || !lastPlayedDateLoaded.equals(today);
        System.out.println("canPlayTodayGenuine: " + canPlay + " (lastPlayed: " + lastPlayedDateLoaded + ")");
        return canPlay;
    }

    public LocalDate getLastPlayedDate() {
        return lastPlayedDateLoaded;
    }

    @Override
    public boolean isGameWon() {
        if (!canPlayTodayGenuine() && currentAttempt >= MAX_ATTEMPTS) {
            return wasGameWon;
        }
        return super.isGameWon();
    }

    private void markAsPlayedToday() {
        LocalDate today = LocalDate.now();
        lastPlayedDateLoaded = today;
        saveState();
    }

    private void saveState() {
        try {
            Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
            if (lastPlayedDateLoaded != null) {
                prefs.put(PREF_KEY_LAST_PLAYED_DATE, lastPlayedDateLoaded.toString());
                System.out.println("Saved last played date: " + lastPlayedDateLoaded);
            } else {
                prefs.remove(PREF_KEY_LAST_PLAYED_DATE);
                System.out.println("Removed last played date from Preferences.");
            }
            prefs.putBoolean(PREF_KEY_GAME_WON, isGameWon());
            System.out.println("Saved game won state: " + isGameWon());
            prefs.putInt(PREF_KEY_CURRENT_ATTEMPT, currentAttempt);
            System.out.println("Saved current attempt: " + currentAttempt);
            String attemptsStr = String.join(",", attempts);
            prefs.put(PREF_KEY_ATTEMPTS, attemptsStr);
            System.out.println("Saved attempts: " + attemptsStr);
            String statesStr = attemptsStates.stream()
                    .map(states -> states.stream()
                            .map(Enum::name)
                            .collect(Collectors.joining("-")))
                    .collect(Collectors.joining(","));
            prefs.put(PREF_KEY_ATTEMPTS_STATES, statesStr);
            System.out.println("Saved attempts states: " + statesStr);
            prefs.flush();
        } catch (SecurityException e) {
            System.err.println("Security error accessing Preferences: " + e.getMessage());
        } catch (BackingStoreException e) {
            System.err.println("Error flushing Preferences: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error saving Preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadState() {
        try {
            Preferences prefs = Preferences.userRoot().node(PREF_NODE_NAME);
            String savedDateStr = prefs.get(PREF_KEY_LAST_PLAYED_DATE, null);
            if (savedDateStr != null) {
                try {
                    this.lastPlayedDateLoaded = LocalDate.parse(savedDateStr);
                    System.out.println("Loaded last played date: " + lastPlayedDateLoaded);
                    if (lastPlayedDateLoaded.equals(LocalDate.now())) {
                        wasGameWon = prefs.getBoolean(PREF_KEY_GAME_WON, false);
                        currentAttempt = prefs.getInt(PREF_KEY_CURRENT_ATTEMPT, 0);
                        String attemptsStr = prefs.get(PREF_KEY_ATTEMPTS, "");
                        if (!attemptsStr.isEmpty()) {
                            attempts.clear();
                            for (String attempt : attemptsStr.split(",")) {
                                if (!attempt.isEmpty()) {
                                    attempts.add(attempt);
                                }
                            }
                        }
                        String statesStr = prefs.get(PREF_KEY_ATTEMPTS_STATES, "");
                        if (!statesStr.isEmpty()) {
                            attemptsStates.clear();
                            for (String stateRow : statesStr.split(",")) {
                                if (!stateRow.isEmpty()) {
                                    List<LetterState> states = new ArrayList<>();
                                    for (String state : stateRow.split("-")) {
                                        try {
                                            states.add(LetterState.valueOf(state));
                                        } catch (IllegalArgumentException e) {
                                            System.err.println("Invalid LetterState: " + state + ", skipping.");
                                        }
                                    }
                                    if (states.size() == WORD_LENGTH) {
                                        attemptsStates.add(states);
                                    }
                                }
                            }
                        }
                        if (currentAttempt >= MAX_ATTEMPTS) {
                            currentAttempt = MAX_ATTEMPTS;
                        }
                        System.out.println("Loaded game state: Won=" + wasGameWon + ", Attempts=" + currentAttempt + ", Guesses=" + attempts + ", States=" + attemptsStates);
                    }
                } catch (Exception parseEx) {
                    System.err.println("Error parsing saved date: '" + savedDateStr + "'. Resetting state.");
                    this.lastPlayedDateLoaded = null;
                    prefs.remove(PREF_KEY_LAST_PLAYED_DATE);
                    prefs.flush();
                }
            } else {
                System.out.println("No last played date found in Preferences.");
                this.lastPlayedDateLoaded = null;
            }
        } catch (SecurityException e) {
            System.err.println("Security error accessing Preferences: " + e.getMessage());
            this.lastPlayedDateLoaded = null;
        } catch (Exception e) {
            System.err.println("Unexpected error loading Preferences: " + e.getMessage());
            e.printStackTrace();
            this.lastPlayedDateLoaded = null;
        }
    }

    @Override
    public void resetGame() {
        System.out.println("resetGame() called on DailyWordleGame. Reloading state for today...");
        loadState();
        this.dailyTargetWord = determineDailyWord();
        this.targetWord = this.dailyTargetWord;
        if (isNewDayAndCanPlay()) {
            currentAttempt = 0;
            attempts.clear();
            attemptsStates.clear();
            wasGameWon = false;
            System.out.println("Reset game for new day.");
        } else if (!canPlayTodayGenuine()) {
            currentAttempt = MAX_ATTEMPTS;
            System.out.println("Game already completed today, maintaining state.");
        }
    }
}