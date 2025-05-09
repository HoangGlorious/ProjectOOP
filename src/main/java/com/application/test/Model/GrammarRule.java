package com.application.test.Model;

public class GrammarRule {
    private String category;
    private String title;
    private String formulaAffirmative;
    private String formulaNegative;
    private String formulaQuestion;
    private String usage;
    private String examples;

    public GrammarRule(String category, String title, String formulaAffirmative, String formulaNegative,
                       String formulaQuestion, String usage, String examples) {
        this.category = category;
        this.title = title;
        this.formulaAffirmative = formulaAffirmative;
        this.formulaNegative = formulaNegative;
        this.formulaQuestion = formulaQuestion;
        this.usage = usage;
        this.examples = examples;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getFormulaAffirmative() {
        return formulaAffirmative;
    }

    public String getFormulaNegative() {
        return formulaNegative;
    }

    public String getFormulaQuestion() {
        return formulaQuestion;
    }

    public String getUsage() {
        return usage;
    }

    public String getExamples() {
        return examples;
    }

    @Override
    public String toString() {
        return "GrammarRule{" +
                "category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", formulaAffirmative='" + formulaAffirmative + '\'' +
                ", formulaNegative='" + formulaNegative + '\'' +
                ", formulaQuestion='" + formulaQuestion + '\'' +
                ", usage='" + usage + '\'' +
                ", examples='" + examples + '\'' +
                '}';
    }
}