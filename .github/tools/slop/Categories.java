import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Canonical category names used by every analyzer.
 *
 * <p>Keeping these in one place stops a typo in an analyzer from silently
 * dropping points: {@link AnalysisContext#addPoints} rejects unknown
 * categories instead of ignoring them.</p>
 */
public final class Categories {

    public static final String NAMING = "Naming Style";
    public static final String COMMENTS = "Comment Style";
    public static final String STRUCTURE = "Code Structure";
    public static final String AI_TRAITS = "Typical AI Traits";
    public static final String LOGIC = "Business Logic Footprints";

    public static final List<String> ALL = Collections.unmodifiableList(
            Arrays.asList(NAMING, COMMENTS, STRUCTURE, AI_TRAITS, LOGIC)
    );

    private Categories() {
    }
}
