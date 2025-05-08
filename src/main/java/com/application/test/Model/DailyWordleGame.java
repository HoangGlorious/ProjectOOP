package com.application.test.Model;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DailyWordleGame extends WordleGame {

    private static final String STATE_DIR = ".dailywordle"; // Thư mục trong home directory
    private static final String STATE_FILE = "dailywordle_state.txt"; // File lưu trạng thái
    private static final String STATE_PATH = System.getProperty("user.home") + File.separator + STATE_DIR + File.separator + STATE_FILE;

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
            resetForNewDay();
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

    private void resetForNewDay() {
        currentAttempt = 0;
        attempts.clear();
        attemptsStates.clear();
        wasGameWon = false;
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
            System.err.println("Cannot guess: Already played today or game completed.");
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
            saveState(); // Lưu trạng thái sau mỗi lượt đoán
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
        File dir = new File(System.getProperty("user.home") + File.separator + STATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(STATE_PATH))) {
            writer.println(lastPlayedDateLoaded != null ? lastPlayedDateLoaded.toString() : "null");
            writer.println(wasGameWon);
            writer.println(currentAttempt);
            writer.println(String.join(",", attempts));
            for (List<LetterState> states : attemptsStates) {
                writer.println(states.stream().map(Enum::name).reduce((a, b) -> a + "-" + b).orElse(""));
            }
            System.out.println("Saved state to " + STATE_PATH + ": lastPlayed=" + lastPlayedDateLoaded + ", won=" + wasGameWon + ", attempts=" + currentAttempt);
        } catch (IOException e) {
            System.err.println("Error saving state to " + STATE_PATH + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadState() {
        File dir = new File(System.getProperty("user.home") + File.separator + STATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(STATE_PATH);
        if (!file.exists()) {
            System.out.println("No state file found at " + STATE_PATH + ", starting new game.");
            resetForNewDay();
            lastPlayedDateLoaded = null;
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(STATE_PATH))) {
            String dateStr = reader.readLine();
            if (dateStr == null || dateStr.trim().isEmpty()) {
                throw new IOException("Invalid state file: Missing last played date.");
            }
            if (!"null".equals(dateStr)) {
                lastPlayedDateLoaded = LocalDate.parse(dateStr);
            } else {
                lastPlayedDateLoaded = null;
            }

            String gameWonStr = reader.readLine();
            if (gameWonStr == null || gameWonStr.trim().isEmpty()) {
                throw new IOException("Invalid state file: Missing game won state.");
            }
            wasGameWon = Boolean.parseBoolean(gameWonStr);

            String attemptStr = reader.readLine();
            if (attemptStr == null || attemptStr.trim().isEmpty()) {
                throw new IOException("Invalid state file: Missing current attempt.");
            }
            currentAttempt = Integer.parseInt(attemptStr);

            String attemptsLine = reader.readLine();
            if (attemptsLine == null) {
                throw new IOException("Invalid state file: Missing attempts data.");
            }
            attempts.clear();
            if (!attemptsLine.trim().isEmpty()) {
                String[] attemptArray = attemptsLine.split(",");
                for (String attempt : attemptArray) {
                    if (!attempt.trim().isEmpty()) {
                        attempts.add(attempt.trim());
                    }
                }
            }

            attemptsStates.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    List<LetterState> states = new ArrayList<>();
                    String[] stateArray = line.split("-");
                    if (stateArray.length != WORD_LENGTH) {
                        System.err.println("Invalid state line: " + line + ". Skipping...");
                        continue;
                    }
                    for (String state : stateArray) {
                        states.add(LetterState.valueOf(state));
                    }
                    attemptsStates.add(states);
                }
            }
            System.out.println("Loaded state from " + STATE_PATH + ": lastPlayed=" + lastPlayedDateLoaded + ", won=" + wasGameWon + ", attempts=" + currentAttempt);
        } catch (IOException e) {
            System.err.println("Error loading state from " + STATE_PATH + ": " + e.getMessage());
            e.printStackTrace();
            resetForNewDay();
            lastPlayedDateLoaded = null;
        } catch (Exception e) {
            System.err.println("Unexpected error loading state from " + STATE_PATH + ": " + e.getMessage());
            e.printStackTrace();
            resetForNewDay();
            lastPlayedDateLoaded = null;
        }
    }

    @Override
    public void resetGame() {
        System.out.println("resetGame() called on DailyWordleGame. Reloading state for today...");
        loadState();
        this.dailyTargetWord = determineDailyWord();
        this.targetWord = this.dailyTargetWord;
        if (isNewDayAndCanPlay()) {
            resetForNewDay();
            System.out.println("Reset game for new day.");
        } else if (!canPlayTodayGenuine()) {
            currentAttempt = MAX_ATTEMPTS;
            System.out.println("Game already completed today, maintaining state.");
        }
    }
}