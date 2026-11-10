import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamingAnalyzer implements CodeAnalyzer {

    private static final Pattern DECLARATION = Pattern.compile(
            "\\b(?:int|long|double|float|boolean|String|Object|"
                    + "List\\s*<[^>]+>|Map\\s*<[^>]+>)\\s+"
                    + "([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*(?:=|;|,)"
    );

    private static final Pattern PLACEHOLDER = Pattern.compile(
            "\\b(?:test|method|variable|temp|object)[0-9]+\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final String[] GENERIC_NAMES = {
            "data", "result", "temp", "tmp",
            "value", "object", "flag", "response"
    };

    @Override
    public void analyze(AnalysisContext context) {

        String code = context.getCode();
        Matcher matcher = DECLARATION.matcher(code);

        Map<String, Integer> counts = new HashMap<>();

        while (matcher.find()) {
            String name = matcher.group(1).toLowerCase();

            counts.put(name, counts.getOrDefault(name, 0) + 1);
        }

        int genericDeclarations = 0;
        int repeatedNames = 0;

        for (String name : GENERIC_NAMES) {
            int occurrences = counts.getOrDefault(name, 0);

            if (occurrences >= 3) {
                genericDeclarations += occurrences;
                repeatedNames++;
            }
        }

        if (repeatedNames >= 3 && genericDeclarations >= 12) {
            context.addPoints(
                    "Naming Style",
                    8,
                    "Several generic variable names are repeatedly declared."
            );
        } else if (repeatedNames >= 2 && genericDeclarations >= 8) {
            context.addPoints(
                    "Naming Style",
                    4,
                    "Repeated generic variable declarations detected."
            );
        }

        Matcher placeholderMatcher = PLACEHOLDER.matcher(code);
        int placeholders = 0;

        while (placeholderMatcher.find()) {
            placeholders++;
        }

        if (placeholders >= 4) {
            context.addPoints(
                    "Naming Style",
                    6,
                    "Multiple numbered placeholder identifiers detected."
            );
        } else if (placeholders >= 2) {
            context.addPoints(
                    "Naming Style",
                    3,
                    "Numbered placeholder identifiers detected."
            );
        }
    }
}
