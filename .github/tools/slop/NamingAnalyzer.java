import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Looks for the generic, low-information identifiers that generated code
 * tends to reuse ({@code data}, {@code result}, {@code temp1}, ...).
 *
 * <p>Only real declarations are considered: comments and string literals are
 * removed first, and counts are normalized by file size so a large,
 * well-written class does not accumulate points.</p>
 */
public class NamingAnalyzer implements CodeAnalyzer {

    private static final Pattern DECLARATION = Pattern.compile(
            "\\b(?:"
                    + "(?:byte|short|int|long|float|double|boolean|char|var)"
                    + "(?:\\s*\\[\\])*"
                    + "|[A-Z][A-Za-z0-9_$]*(?:\\s*<[^;{}()]*?>)?"
                    + "(?:\\s*\\[\\])*"
                    + ")\\s+([a-z_$][A-Za-z0-9_$]*)\\s*(?:=|;|,|:)"
    );

    private static final Pattern PLACEHOLDER = Pattern.compile(
            "\\b(?:test|method|variable|temp|object|foo|bar|baz|sample|example"
                    + "|demo|item)\\d+\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final String[] GENERIC_NAMES = {
            "data", "result", "results", "temp", "tmp", "value", "values",
            "object", "obj", "flag", "response", "list", "map", "item",
            "element", "output", "input", "info", "params", "args"
    };

    @Override
    public void analyze(AnalysisContext context) {

        String code = SourceText.stripCommentsAndLiterals(context.getCode());
        int lines = context.getLineCount();

        Matcher matcher = DECLARATION.matcher(code);
        Map<String, Integer> counts = new HashMap<>();

        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();
            counts.merge(name, 1, Integer::sum);
        }

        List<String> genericNames = new ArrayList<>();
        int genericDeclarations = 0;

        for (String name : GENERIC_NAMES) {
            int occurrences = counts.getOrDefault(name, 0);

            if (occurrences >= 2) {
                genericDeclarations += occurrences;
                genericNames.add(name + " (x" + occurrences + ")");
            }
        }

        int distinctNames = genericNames.size();
        double scaledDeclarations =
                AnalyzerSupport.scaleForSize(genericDeclarations, lines);

        if (distinctNames >= 3 && scaledDeclarations >= 9) {
            context.addPoints(
                    Categories.NAMING,
                    6,
                    "Several generic variable names are repeatedly declared: "
                            + summarize(genericNames)
            );
        } else if (distinctNames >= 2 && scaledDeclarations >= 6) {
            context.addPoints(
                    Categories.NAMING,
                    3,
                    "Repeated generic variable declarations: "
                            + summarize(genericNames)
            );
        }

        int placeholders = AnalyzerSupport.count(PLACEHOLDER, code);

        if (placeholders >= 3) {
            context.addPoints(
                    Categories.NAMING,
                    6,
                    placeholders + " numbered placeholder identifiers detected."
            );
        } else if (placeholders >= 2) {
            context.addPoints(
                    Categories.NAMING,
                    3,
                    placeholders + " numbered placeholder identifiers detected."
            );
        }
    }

    private String summarize(List<String> names) {

        List<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted);

        if (sorted.size() > 5) {
            return String.join(", ", sorted.subList(0, 5))
                    + " and " + (sorted.size() - 5) + " more";
        }

        return String.join(", ", sorted);
    }
}
