package com.application.test.Model;
import java.util.ArrayList;
import java.util.List;

public class WordSense {
    private String partOfSpeech; // Loại từ (e.g., "danh từ", "động từ")
    private List<String> definitions; // Danh sách các định nghĩa (bắt đầu bằng '-')
    private List<ExamplePhrase> examples; // Danh sách các ví dụ (bắt đầu bằng '=')

    public WordSense(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech != null ? partOfSpeech.trim() : "Không xác định";
        this.definitions = new ArrayList<>();
        this.examples = new ArrayList<>();
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public List<String> getDefinitions() {
        return definitions;
    }

    public List<ExamplePhrase> getExamples() {
        return examples;
    }

    public void addDefinition(String definition) {
        if (definition != null && !definition.trim().isEmpty()) {
            this.definitions.add(definition.trim());
        }
    }

    public void addExample(ExamplePhrase example) {
        if (example != null) {
            this.examples.add(example);
        }
    }

    @Override
    public String toString() {
        // Định dạng hiển thị cơ bản cho một sense
        StringBuilder sb = new StringBuilder();
        sb.append("* ").append(partOfSpeech).append("\n");
        for (String def : definitions) {
            sb.append("  - ").append(def).append("\n");
        }
        for (ExamplePhrase ex : examples) {
            sb.append("  ").append(ex.toString()).append("\n"); // Đã có dấu = ở đầu
        }
        return sb.toString();
    }
}
