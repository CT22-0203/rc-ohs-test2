import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DAO-specific fingerprints of generated JDBC code.
 *
 * <p>The patterns here mirror what a reviewer notices when reading a generated
 * DAO: the same generic exception sentence repeated in every method, Javadoc
 * on every constant and private helper, a static {@code COLUMNS} constant
 * reused by several queries, a {@code toX(ResultSet)} mapper, a UUID generated
 * inside the DAO, nested try-with-resources, and the manual
 * {@code int index = 1; setX(index, ...); index++;} binding style.</p>
 *
 * <p>This analyzer only runs on classes whose name ends with {@code DaoImpl},
 * so ordinary services, models and tests are not affected.</p>
 */
public class DaoPatternAnalyzer implements CodeAnalyzer {

    private static final Pattern CLASS_NAME = Pattern.compile(
            "\\bclass\\s+(\\w+)"
    );

    private static final Pattern STRING_LITERAL = Pattern.compile(
            "\"((?:[^\"\\\\]|\\\\.){12,200})\""
    );

    private static final Pattern EXCEPTION_MESSAGE = Pattern.compile(
            "(?i)^\\s*(?:an?\\s+)?(?:exception|error|failed|failure|unable"
                    + "|could\\s+not)\\b.*"
    );

    private static final Pattern INDEX_INCREMENT = Pattern.compile(
            "\\bindex\\s*\\+\\+"
    );

    private static final Pattern INDEX_BINDING = Pattern.compile(
            "set[A-Z]\\w*\\s*\\(\\s*index\\b"
    );

    private static final Pattern SQL_CONSTANT = Pattern.compile(
            "(?m)^\\s*private\\s+static\\s+final\\s+String\\s+[A-Z_]+\\s*="
    );

    private static final Pattern INNER_RESULT_SET = Pattern.compile(
            "try\\s*\\(\\s*ResultSet\\b"
    );

    private static final Pattern ROW_MAPPER = Pattern.compile(
            "private\\s+\\w+\\s+to[A-Z]\\w*\\s*\\(\\s*ResultSet\\b"
    );

    private static final Pattern NULL_GUARD_CHAIN = Pattern.compile(
            "if\\s*\\([^)]*==\\s*null[^)]*\\|\\|[^)]*==\\s*null"
    );

    private static final Pattern JAVADOC_BLOCK = Pattern.compile("/\\*\\*");

    private static final Pattern METHOD_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:(?:public|protected|private|static|final|abstract"
                    + "|synchronized)\\s+)*(?:<[^>]+>\\s+)?"
                    + "[A-Za-z_$][\\w.$<>\\[\\], ?]*\\s+\\w+\\s*"
                    + "\\([^;{]*\\)\\s*(?:throws\\s+[^{]+)?\\{"
    );

    private static final Pattern FIELD_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:(?:public|protected|private|static|final|transient"
                    + "|volatile)\\s+)+[\\w.$<>\\[\\], ?]+\\s+\\w+\\s*(?:=|;)"
    );

    private static final String[] GENERIC_DAO_JAVADOC = {
            "implements the data access operations",
            "the sql statement that",
            "the columns selected for",
            "the current row of the result set",
            "the result set positioned at"
    };

    @Override
    public void analyze(AnalysisContext context) {

        String raw = context.getCode();
        String code = SourceText.stripCommentsAndLiterals(raw);

        Matcher classMatcher = CLASS_NAME.matcher(code);
        String className = classMatcher.find() ? classMatcher.group(1) : "";

        if (!className.endsWith("DaoImpl")) {
            return;
        }

        scoreRepeatedExceptionMessages(context, raw);
        scoreDocumentedMembers(context, raw);
        scoreManualIndexBinding(context, code);
        scoreDaoStructure(context, code);
        scoreNullGuardChain(context, code);
        scoreGenericDaoJavadoc(context, context.getComments());
    }

    /**
     * The same generated exception sentence usually appears in every method,
     * for example "An exception was thrown while finding a medical record."
     * and "An exception was thrown while adding a medical record.".
     */
    private void scoreRepeatedExceptionMessages(
            AnalysisContext context,
            String raw) {

        Map<String, Integer> templates = new HashMap<>();
        String worst = null;
        int worstCount = 0;

        Matcher matcher = STRING_LITERAL.matcher(raw);

        while (matcher.find()) {

            String message = matcher.group(1).trim();

            if (!EXCEPTION_MESSAGE.matcher(message).matches()) {
                continue;
            }

            String template = normalizeMessageTemplate(message);
            int count = templates.merge(template, 1, Integer::sum);

            if (count > worstCount) {
                worstCount = count;
                worst = template;
            }
        }

        if (worstCount >= 2) {
            context.addPoints(
                    Categories.LOGIC,
                    4,
                    "The same generic exception message is repeated "
                            + worstCount + " times: \"" + worst + " ...\""
            );

            context.addTechnicalNote(
                    "Replace the repeated console messages with real logging "
                            + "and propagate the failure to the caller."
            );
        }
    }

    /**
     * Generated DAOs document constants and private helpers that hand-written
     * code usually leaves undocumented.
     */
    private void scoreDocumentedMembers(AnalysisContext context, String raw) {

        int javadocs = AnalyzerSupport.count(JAVADOC_BLOCK, raw);
        int members = AnalyzerSupport.count(METHOD_DECLARATION, raw)
                + AnalyzerSupport.count(FIELD_DECLARATION, raw);

        if (members < 6) {
            return;
        }

        double coverage = Math.min(1.0, javadocs / (double) members);

        if (coverage >= 0.9) {
            context.addPoints(
                    Categories.COMMENTS,
                    4,
                    "Javadoc on nearly every constant, field and private "
                            + "helper (" + javadocs + " blocks for "
                            + members + " members)."
            );
        }
    }

    /**
     * {@code int index = 1; setX(index, ...); index++;} is readable, but the
     * verbose increment style is very common in generated JDBC code.
     */
    private void scoreManualIndexBinding(AnalysisContext context, String code) {

        int increments = AnalyzerSupport.count(INDEX_INCREMENT, code);
        int bindings = AnalyzerSupport.count(INDEX_BINDING, code);

        if (increments >= 2 && bindings >= 2) {
            context.addPoints(
                    Categories.STRUCTURE,
                    4,
                    "Manual parameter index incremented " + increments
                            + " times while binding " + bindings
                            + " statement values (int index = 1; ... index++)."
            );
        }
    }

    /**
     * Counts the structural features of the common generated DAO template:
     * static SQL constants, a shared {@code COLUMNS} constant, a
     * {@code toX(ResultSet)} mapper, UUID generation inside the DAO, nested
     * try-with-resources and {@code setNull} handling.
     */
    private void scoreDaoStructure(AnalysisContext context, String code) {

        int features = 0;
        Set<String> found = new LinkedHashSet<>();

        if (SQL_CONSTANT.matcher(code).find()) {
            features++;
            found.add("static SQL constants");
        }

        if (AnalyzerSupport.count(Pattern.compile("\\bCOLUMNS\\b"), code) >= 2) {
            features++;
            found.add("shared COLUMNS constant");
        }

        if (ROW_MAPPER.matcher(code).find()) {
            features++;
            found.add("toX(ResultSet) mapper");
        }

        if (code.contains("UUID.randomUUID")) {
            features++;
            found.add("UUID generated inside the DAO");
        }

        if (INNER_RESULT_SET.matcher(code).find()) {
            features++;
            found.add("nested try-with-resources");
        }

        if (code.contains(".setNull(")) {
            features++;
            found.add("setNull handling");
        }

        if (features >= 4) {
            context.addPoints(
                    Categories.STRUCTURE,
                    6,
                    "Standard AI DAO template: " + String.join(", ", found) + "."
            );
        } else if (features == 3) {
            context.addPoints(
                    Categories.STRUCTURE,
                    4,
                    "Several generated-DAO structure features: "
                            + String.join(", ", found) + "."
            );
        }
    }

    private void scoreNullGuardChain(AnalysisContext context, String code) {

        if (NULL_GUARD_CHAIN.matcher(code).find()) {
            context.addPoints(
                    Categories.LOGIC,
                    2,
                    "Defensive null-guard chain on several object levels."
            );
        }
    }

    private void scoreGenericDaoJavadoc(AnalysisContext context, String comments) {

        String lower = comments.toLowerCase(Locale.ROOT);
        int distinct = 0;

        for (String phrase : GENERIC_DAO_JAVADOC) {
            if (lower.contains(phrase)) {
                distinct++;
            }
        }

        if (distinct >= 3) {
            context.addPoints(
                    Categories.COMMENTS,
                    4,
                    "Polished, generic DAO Javadoc using " + distinct
                            + " recurring description templates."
            );
        } else if (distinct == 2) {
            context.addPoints(
                    Categories.COMMENTS,
                    2,
                    "Generic DAO Javadoc description templates detected."
            );
        }
    }

    private String normalizeMessageTemplate(String message) {

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
