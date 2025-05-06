package com.application.test.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleGame implements Games {
    private static final int WORD_LENGTH = 5;
    private static final int MAX_ATTEMPTS = 6;

    private String targetWord;
    private final List<String> validWords;
    private int currentAttempt;
    private final List<String> attempts;
    private final List<List<LetterState>> attemptsStates;

    public enum LetterState {
        CORRECT, PRESENT, ABSENT
    }

    /** Constructor không tham số, tự động load từ điển từ resources */
    public WordleGame() {
        this.validWords = loadDictionaryFromResource("/five_letter_words.txt");
        this.attempts = new ArrayList<>();
        this.attemptsStates = new ArrayList<>();
        resetGame();
    }

    /** (Nếu vẫn cần) Constructor có đường dẫn, sẽ chuyển về constructor không tham số */
    public WordleGame(String unusedPath) {
        this();
    }

    private List<String> loadDictionaryFromResource(String resourcePath) {
        List<String> words = new ArrayList<>();
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Không tìm thấy resource: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.length() == WORD_LENGTH) {
                        words.add(line);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi load từ điển từ resource", e);
        }
        System.out.println("Đã tải " + words.size() + " từ từ điển.");
        return words;
    }

    public void resetGame() {
        if (!validWords.isEmpty()) {
            targetWord = validWords.get(new Random().nextInt(validWords.size()));
        } else {
            targetWord = "apple";
        }
        currentAttempt = 0;
        attempts.clear();
        attemptsStates.clear();
    }

    public boolean isValidGuess(String word) {
        return word != null
                && word.length() == WORD_LENGTH
                && validWords.contains(word.toLowerCase());
    }

    public List<LetterState> makeGuess(String guess) {
        guess = guess.toLowerCase();
        if (!isValidGuess(guess) || currentAttempt >= MAX_ATTEMPTS) {
            return null;
        }
        attempts.add(guess);
        List<LetterState> states = checkGuess(guess);
        attemptsStates.add(states);
        currentAttempt++;
        return states;
    }

    private List<LetterState> checkGuess(String guess) {
        List<LetterState> states = new ArrayList<>();
        char[] targetChars = targetWord.toCharArray();
        char[] guessChars  = guess.toCharArray();
        boolean[] marked   = new boolean[WORD_LENGTH];

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guessChars[i] == targetChars[i]) {
                states.add(LetterState.CORRECT);
                marked[i] = true;
            } else {
                states.add(null);
            }
        }
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (states.get(i) != null) continue;
            boolean found = false;
            for (int j = 0; j < WORD_LENGTH; j++) {
                if (guessChars[i] == targetChars[j] && !marked[j]) {
                    states.set(i, LetterState.PRESENT);
                    marked[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found) states.set(i, LetterState.ABSENT);
        }
        return states;
    }

    public boolean isGameWon() {
        return !attempts.isEmpty()
                && attempts.get(attempts.size() - 1).equals(targetWord);
    }

    public boolean isGameOver() {
        return isGameWon() || currentAttempt >= MAX_ATTEMPTS;
    }

    public int getCurrentAttempt()    { return currentAttempt; }
    public int getMaxAttempts()       { return MAX_ATTEMPTS; }
    public String getTargetWord()     { return targetWord; }
    public List<String> getAttempts() { return new ArrayList<>(attempts); }
    public List<List<LetterState>> getAttemptsStates() {
        return new ArrayList<>(attemptsStates);
    }

    @Override
    public void startGame() {
        resetGame();
        System.out.println("Wordle game has started!");
    }

    @Override
    public void endGame() {
        System.out.println("Wordle game has ended.");
    }
}
