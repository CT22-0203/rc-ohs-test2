import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalysisContext {

    public static final int MAX_CATEGORY_SCORE = 20;

    private final Path file;
    private final String code;
    private final String comments;

    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private final Map<String, List<String>> evidence = new LinkedHashMap<>();
    private final List<String> technicalNotes = new ArrayList<>();

    public AnalysisContext(Path file, String code, String comments) {
        this.file = file;
        this.code = code;
        this.comments = comments;

        initialize("Naming Style");
        initialize("Comment Style");
        initialize("Code Structure");
        initialize("Typical AI Traits");
        initialize("Business Logic Footprints");
    }

    private void initialize(String category) {
        scores.put(category, 0);
        evidence.put(category, new ArrayList<>());
    }

    public Path getFile() {
        return file;
    }

    public String getCode() {
        return code;
    }

    public String getComments() {
        return comments;
    }

    public void addPoints(String category, int points, String reason) {
        if (!scores.containsKey(category) || points <= 0) {
            return;
        }

        int current = scores.get(category);
        int added = Math.min(points, MAX_CATEGORY_SCORE - current);

        if (added > 0) {
            scores.put(category, current + added);
            evidence.get(category).add(reason);
        }
    }

    public void addTechnicalNote(String note) {
        if (!technicalNotes.contains(note)) {
            technicalNotes.add(note);
        }
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public Map<String, List<String>> getEvidence() {
        return evidence;
    }

    public List<String> getTechnicalNotes() {
        return technicalNotes;
    }

    public int getTotalScore() {
        int total = 0;

        for (int score : scores.values()) {
            total += score;
        }

        return Math.min(total, 100);
    }
}
