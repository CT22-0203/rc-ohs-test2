import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureAnalyzer implements CodeAnalyzer {

    private static final Pattern METHOD = Pattern.compile(
            "(?:public|private|protected)\\s+"
                    + "(?:static\\s+)?[\\w<>\\[], ?]+\\s+"
                    + "\\w+\\s*\\([^)]*\\)\\s*\\{"
    );

    @Override
    public void analyze(AnalysisContext context) {

        String code = context.getCode();

        Matcher matcher = METHOD.matcher(code);
        Map<String, Integer> bodyPatterns = new HashMap<>();

        while (matcher.find()) {

            int openingBrace = code.indexOf('{', matcher.start());
            int closingBrace = findClosingBrace(code, openingBrace);

            if (closingBrace == -1) {
                continue;
            }

            String body = code.substring(openingBrace + 1, closingBrace);
            String normalized = normalize(body);

            if (normalized.length() < 20) {
                continue;
            }

            bodyPatterns.put(
                    normalized,
                    bodyPatterns.getOrDefault(normalized, 0) + 1
            );
        }

        int repeatedBodies = 0;

        for (int occurrences : bodyPatterns.values()) {
            if (occurrences >= 3) {
                repeatedBodies += occurrences;
            }
        }

        if (repeatedBodies >= 6) {
            context.addPoints(
                    "Code Structure",
                    10,
                    "Multiple methods contain highly similar normalized bodies."
            );
        } else if (repeatedBodies >= 3) {
            context.addPoints(
                    "Code Structure",
                    5,
                    "Repeated normalized method-body structures detected."
            );
        }

        int testCount = count(code, "@Test");

        if (testCount >= 10 && repeatedBodies >= 3) {
            context.addPoints(
                    "Code Structure",
                    5,
                    "Large test class with repeated method-body patterns."
            );
        }
    }

    private String normalize(String body) {

        return body
                .replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"STRING\"")
                .replaceAll("'(?:\\\\.|[^'\\\\])*'", "'CHAR'")
                .replaceAll("\\b\\d+\\b", "NUMBER")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int findClosingBrace(String code, int openingBrace) {

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = openingBrace; i < code.length(); i++) {

            char current = code.charAt(i);

            if (inString) {
                if (current == '"' && !escaped) {
                    inString = false;
                }

                if (current == '\\' && !escaped) {
                    escaped = true;
                } else {
                    escaped = false;
                }

                continue;
            }

            if (current == '"') {
                inString = true;
            } else if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;

                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private int count(String text, String target) {
        int total = 0;
        int index = 0;

        while ((index = text.indexOf(target, index)) != -1) {
            total++;
            index += target.length();
        }

        return total;
    }
}
