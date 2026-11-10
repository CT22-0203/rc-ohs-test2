import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds signals that only appear when several files are compared.
 *
 * <p>The strongest cross-file tell is a generic exception sentence that is
 * reused verbatim across different DAOs, for example
 * {@code "An exception was thrown while ..."} with only the noun changed. A
 * single file with that message is weak evidence; the same sentence family in
 * four different DAOs is a strong shared-template signal.</p>
 */
public final class CrossFileAnalyzer {

    private static final Pattern STRING_LITERAL = Pattern.compile(
            "\"((?:[^\"\\\\]|\\\\.){12,200})\""
    );

    private static final Pattern EXCEPTION_MESSAGE = Pattern.compile(
            "(?i)^\\s*(?:an?\\s+)?(?:exception|error|failed|failure|unable"
                    + "|could\\s+not)\\b.*"
    );

    private CrossFileAnalyzer() {
    }

    /**
     * Adds points to every DAO that shares a generic exception message with at
     * least one other DAO.
     */
    public static void analyze(List<AnalysisContext> results) {

        Map<String, Set<AnalysisContext>> byTemplate = new LinkedHashMap<>();

        for (AnalysisContext context : results) {

            if (!isDao(context)) {
                continue;
            }

            Matcher matcher = STRING_LITERAL.matcher(context.getCode());

            while (matcher.find()) {

                String message = matcher.group(1).trim();

                if (!EXCEPTION_MESSAGE.matcher(message).matches()) {
                    continue;
                }

                byTemplate
                        .computeIfAbsent(
                                normalizeMessageTemplate(message),
                                key -> new LinkedHashSet<>()
                        )
                        .add(context);
            }
        }

        for (Map.Entry<String, Set<AnalysisContext>> entry
                : byTemplate.entrySet()) {

            Set<AnalysisContext> files = entry.getValue();

            if (files.size() < 2) {
                continue;
            }

            for (AnalysisContext context : files) {
                context.addPoints(
                        Categories.LOGIC,
                        4,
                        "Cross-file consistency: the same generic exception "
                                + "message appears in " + files.size()
                                + " DAO files: \"" + entry.getKey() + " ...\""
                );

                context.addTechnicalNote(
                        "The same exception sentence is reused across "
                                + files.size() + " DAOs; check whether it was "
                                + "copied from one template."
                );
            }
        }
    }

    private static boolean isDao(AnalysisContext context) {
        return context.getFile().getFileName().toString().endsWith("DaoImpl.java");
    }

    private static String normalizeMessageTemplate(String message) {

        String[] words = message.trim().split("\\s+");
        StringBuilder template = new StringBuilder();

        for (int index = 0; index < words.length && index < 5; index++) {

            if (index > 0) {
                template.append(' ');
            }

            template.append(words[index]);
        }

        return template.toString()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", "")
                .trim();
    }
}
