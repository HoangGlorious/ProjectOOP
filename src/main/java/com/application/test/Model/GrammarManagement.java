package com.application.test.Model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GrammarManagement {
    private List<GrammarRule> grammarRules;

    public GrammarManagement() {
        this.grammarRules = new ArrayList<>();
    }

    public List<GrammarRule> getGrammarRules() {
        return grammarRules;
    }

    public void loadGrammarRules() {
        grammarRules.clear();
        String resourcePath = "/grammar.txt";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(resourcePath)))) {
            if (reader == null) {
                throw new IllegalStateException("Không tìm thấy file tại đường dẫn: " + resourcePath);
            }
            String line;
            String category = null;
            String title = null;
            StringBuilder formulaAffirmative = new StringBuilder();
            StringBuilder formulaNegative = new StringBuilder();
            StringBuilder formulaQuestion = new StringBuilder();
            StringBuilder usage = new StringBuilder();
            StringBuilder examples = new StringBuilder();
            boolean inUsage = false;
            boolean inExamples = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("@")) {
                    // Nếu đã có rule trước đó, lưu lại
                    if (category != null && title != null) {
                        grammarRules.add(new GrammarRule(category, title,
                                formulaAffirmative.toString().trim(),
                                formulaNegative.toString().trim(),
                                formulaQuestion.toString().trim(),
                                usage.toString().trim(),
                                examples.toString().trim()));
                    }
                    // Bắt đầu rule mới
                    String[] parts = line.split(" ", 2);
                    if (parts.length < 2) continue;
                    category = parts[0].substring(1).trim(); // Bỏ @
                    title = parts[1].trim();
                    formulaAffirmative = new StringBuilder();
                    formulaNegative = new StringBuilder();
                    formulaQuestion = new StringBuilder();
                    usage = new StringBuilder();
                    examples = new StringBuilder();
                    inUsage = false;
                    inExamples = false;
                } else if (line.startsWith("=")) {
                    usage.append(line.substring(1).trim()).append("\n");
                    inUsage = true;
                    inExamples = false;
                } else if (line.startsWith(">>")) {
                    formulaAffirmative.append(line.substring(2).trim()).append("\n");
                    inUsage = false;
                    inExamples = false;
                } else if (line.startsWith("><")) {
                    formulaNegative.append(line.substring(2).trim()).append("\n");
                    inUsage = false;
                    inExamples = false;
                } else if (line.startsWith(">?")) {
                    formulaQuestion.append(line.substring(2).trim()).append("\n");
                    inUsage = false;
                    inExamples = false;
                } else if (line.startsWith("?>")) {
                    examples.append(line.substring(2).trim()).append("\n");
                    inUsage = false;
                    inExamples = true;
                } else if (inUsage) {
                    usage.append(line).append("\n");
                } else if (inExamples) {
                    examples.append(line).append("\n");
                }
            }
            // Lưu rule cuối cùng
            if (category != null && title != null) {
                grammarRules.add(new GrammarRule(category, title,
                        formulaAffirmative.toString().trim(),
                        formulaNegative.toString().trim(),
                        formulaQuestion.toString().trim(),
                        usage.toString().trim(),
                        examples.toString().trim()));
            }
            System.out.println("Loaded " + grammarRules.size() + " grammar rules.");
        } catch (Exception e) {
            System.err.println("Error loading grammar rules: " + e.getMessage());
            e.printStackTrace();
        }
    }
}