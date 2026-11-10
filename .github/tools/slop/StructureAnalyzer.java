import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects groups of methods that share an identical normalized body, which is
 * a common fingerprint of generated CRUD/boilerplate code.
 *
 * <p>Getters, setters, {@code toString}, {@code equals}, {@code hashCode} and
 * other trivial accessors are excluded: they are legitimately repetitive in
 * hand-written code and were the main source of false positives.</p>
 */
public class StructureAnalyzer implements CodeAnalyzer {

    private static final Pattern METHOD = Pattern.compile(
            "(?m)(?:^|[;{}])\\s*"
                    + "(?:@\\w+(?:\\([^)]*\\))?\\s*)*"
                    + "(?:public|private|protected)\\s+"
                    + "(?:static\\s+)?(?:final\\s+)?(?:synchronized\\s+)?"
                    + "(?:<[^>]+>\\s+)?"
                    + "[\\w.$]+(?:\\s*<[^;{}()]*?>)?(?:\\s*\\[\\])*\\s+"
                    + "(\\w+)\\s*\\([^)]*\\)\\s*"
                    + "(?:throws\\s+[\\w.,\\s]+)?\\{"
    );

    private static final Pattern TEST_ANNOTATION = Pattern.compile("@Test\\b");

    @Override
    public void analyze(AnalysisContext context) {

        String code = SourceText.stripCommentsAndLiterals(context.getCode());

        Matcher matcher = METHOD.matcher(code);
        Map<String, List<String>> bodies = new HashMap<>();
        int candidateMethods = 0;

        while (matcher.find()) {

            String methodName = matcher.group(1);
            int openingBrace = matcher.end() - 1;
            int closingBrace = findClosingBrace(code, openingBrace);

            if (closingBrace == -1) {
                continue;
            }

            String body = code.substring(openingBrace + 1, closingBrace);

            if (isTrivial(methodName, body)) {
                continue;
            }

            String normalized = normalize(body);

            if (normalized.length() < 40) {
                continue;
            }

            candidateMethods++;
            bodies.computeIfAbsent(normalized, key -> new ArrayList<>())
                    .add(methodName);
        }

        int duplicatedMethods = 0;
        List<String> groups = new ArrayList<>();

        for (List<String> methods : bodies.values()) {
            if (methods.size() < 3) {
                continue;
            }

            duplicatedMethods += methods.size();

            List<String> names = new ArrayList<>(methods);
            Collections.sort(names);

            if (groups.size() < 3) {
                groups.add(methods.size() + " methods share a body ("
                        + summarize(names) + ")");
            }
        }

        boolean meaningfulShare = candidateMethods > 0
                && duplicatedMethods * 10 >= candidateMethods;

        if (duplicatedMethods >= 6 && meaningfulShare) {
            context.addPoints(
                    Categories.STRUCTURE,
                    10,
                    "Repeated normalized method bodies: "
                            + String.join("; ", groups)
            );
        } else if (duplicatedMethods >= 3 && meaningfulShare) {
            context.addPoints(
                    Categories.STRUCTURE,
                    5,
                    "Several methods share a normalized body: "
                            + String.join("; ", groups)
            );
        }

        int testCount = AnalyzerSupport.count(TEST_ANNOTATION, code);

        if (testCount >= 10 && duplicatedMethods >= 3) {
            context.addPoints(
                    Categories.STRUCTURE,
                    5,
                    "Large test class (" + testCount
                            + " tests) with repeated method-body patterns."
            );
        }
    }

    /**
     * Accessors and object plumbing are regularly duplicated in hand-written
     * code, so they are not evidence of generated code.
     */
    private boolean isTrivial(String methodName, String body) {

        String name = methodName.toLowerCase();

        if (name.startsWith("get") || name.startsWith("set")
                || name.startsWith("is") || name.equals("tostring")
                || name.equals("equals") || name.equals("hashcode")
                || name.equals("canequal") || name.equals("compareto")) {
            return true;
        }

        String compact = normalize(body);

        if (compact.matches("return\\s+(?:this\\.)?\\w+\\s*;")) {
            return true;
        }

        if (compact.matches("(?:this\\.)?\\w+\\s*=\\s*\\w+\\s*;")) {
            return true;
        }

        if (compact.matches("super\\([^;]*\\)\\s*;")) {
            return true;
        }

        return AnalyzerSupport.count(compact, ";") <= 2;
    }

    private String normalize(String body) {
        return body
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String summarize(List<String> names) {

        if (names.size() <= 4) {
            return String.join(", ", names);
        }

        return String.join(", ", names.subList(0, 4))
                + ", ... (" + names.size() + " total)";
    }

    private int findClosingBrace(String code, int openingBrace) {

        int depth = 0;

        for (int i = openingBrace; i < code.length(); i++) {

            char current = code.charAt(i);

            if (current == '{') {
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
}
