import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-file analysis state: the source, its comments, the score accumulated in
 * each category, and the reasons behind those points.
 */
public class AnalysisContext {

    public static final int MAX_CATEGORY_SCORE = 20;

    private final Path file;
    private final String code;
    private final String comments;
    private final int lineCount;

    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private final Map<String, List<String>> evidence = new LinkedHashMap<>();
    private final List<String> technicalNotes = new ArrayList<>();
    private final List<Finding> findings = new ArrayList<>();

    public AnalysisContext(Path file, String code, String comments) {
        this.file = file;
        this.code = code;
        this.comments = comments;
        this.lineCount = SourceText.countLines(code);

        for (String category : Categories.ALL) {
            scores.put(category, 0);
            evidence.put(category, new ArrayList<>());
        }
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

    public int getLineCount() {
        return lineCount;
    }

    public void addPoints(String category, int points, String reason) {
        addFinding(category, points, reason, "");
    }

    /**
     * Adds points and records the visible fragment that triggered the rule.
     *
     * @param category The scoring category.
     * @param points The rule weight.
     * @param reason The reviewer-style explanation.
     * @param fragment The matched code or documentation text, or an empty
     *                 string when the rule has no single visible fragment.
     */
    public void addFinding(
            String category,
            int points,
            String reason,
            String fragment) {

        if (!scores.containsKey(category)) {
            throw new IllegalArgumentException(
                    "Unknown category: " + category
            );
        }

        if (points <= 0) {
            return;
        }

        int current = scores.get(category);
        int added = Math.min(points, MAX_CATEGORY_SCORE - current);

        if (added > 0) {
            scores.put(category, current + added);

            List<String> reasons = evidence.get(category);

            if (!reasons.contains(reason)) {
                reasons.add(reason);
                findings.add(new Finding(
                        category,
                        added,
                        reason,
                        fragment
                ));
            }
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

    /**
     * Returns the explainable findings behind this file's score.
     *
     * @return Findings in the order the rules fired.
     */
    public List<Finding> getFindings() {
        return findings;
    }

    public int getScore(String category) {
        return scores.getOrDefault(category, 0);
    }

    public int getTotalScore() {

        int total = 0;

        for (int score : scores.values()) {
            total += score;
        }

        return Math.min(total, 100);
    }

    /** Category with the most points, or {@code null} when the file is clean. */
    public String getDominantCategory() {

        String dominant = null;
        int highest = 0;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                dominant = entry.getKey();
            }
        }

        return dominant;
    }
}
