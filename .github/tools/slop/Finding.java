/**
 * One explainable piece of evidence behind a file's score.
 *
 * <p>A finding carries the visible code fragment that triggered the rule, the
 * reviewer-style reason, and a confidence band derived from the rule's weight.
 * The report uses these to render the "Probable AI-origin Markers" table.</p>
 */
public final class Finding {

    private final String category;
    private final int points;
    private final String reason;
    private final String fragment;
    private final String confidence;

    Finding(String category, int points, String reason, String fragment) {
        this.category = category;
        this.points = points;
        this.reason = reason;
        this.fragment = fragment == null ? "" : fragment;
        this.confidence = confidenceFor(points);
    }

    /**
     * Maps a rule's weight to a reviewer-friendly confidence band.
     *
     * @param points The points the rule contributed.
     * @return Low, Medium, Medium-high or Strong.
     */
    public static String confidenceFor(int points) {
        if (points >= 8) {
            return "Strong";
        }

        if (points >= 5) {
            return "Medium-high";
        }

        if (points >= 3) {
            return "Medium";
        }

        return "Low";
    }

    public String getCategory() {
        return category;
    }

    public int getPoints() {
        return points;
    }

    public String getReason() {
        return reason;
    }

    public String getFragment() {
        return fragment;
    }

    public String getConfidence() {
        return confidence;
    }
}
