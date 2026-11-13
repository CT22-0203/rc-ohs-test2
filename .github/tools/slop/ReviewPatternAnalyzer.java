import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reviewer-style fingerprints that a professor or code reviewer usually points
 * out when reading generated JDBC and test code.
 *
 * <p>Each rule records the visible fragment that triggered it, so the report
 * can show a "Probable AI-origin Markers" table with a file/fragment, a reason
 * and a confidence band instead of only a numeric score.</p>
 *
 * <p>The rules cover hardcoded JDBC binding loops, generic
 * {@code RuntimeException} wrappers, inline SQL with aliased columns,
 * null-safe ternary mapping, and generic class or return Javadoc.</p>
 */
public class ReviewPatternAnalyzer implements CodeAnalyzer {

    private static final Pattern BINDING_LOOP = Pattern.compile(
            "for\\s*\\(\\s*int\\s+(\\w+)\\s*=\\s*\\d+\\s*;\\s*\\1\\s*<=?\\s*"
                    + "([A-Za-z_][\\w.]*|\\d+)\\s*;\\s*\\1\\s*\\+\\+\\s*\\)"
    );

    private static final Pattern INT_CONSTANT = Pattern.compile(
            "(?m)^\\s*private\\s+static\\s+final\\s+int\\s+"
                    + "([A-Z][A-Z0-9_]*)\\s*=\\s*\\d+\\s*;"
    );

    private static final Pattern RUNTIME_EXCEPTION_WITH_CAUSE = Pattern.compile(
            "throw\\s+new\\s+RuntimeException\\s*\\(\\s*"
                    + "\"([^\"]{0,160})\"\\s*(?:\\+[^,]*)?,\\s*\\w+\\s*\\)"
    );

    private static final Pattern RUNTIME_EXCEPTION_GENERIC = Pattern.compile(
            "throw\\s+new\\s+RuntimeException\\s*\\(\\s*"
                    + "\"((?:Failed|Failure|Error|Unable|Could\\s+not|Exception)"
                    + "[^\"]{0,160})\"\\s*\\)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_LITERAL = Pattern.compile(
            "\"((?:[^\"\\\\]|\\\\.){20,500})\""
    );

    private static final Pattern LOWER_LIKE = Pattern.compile(
            "LOWER\\s*\\([^)]*\\)\\s*LIKE\\s*LOWER\\s*\\(\\s*\\?\\s*\\)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NULL_SAFE_TERNARY = Pattern.compile(
            "==\\s*null\\s*\\?\\s*null\\s*:\\s*\\w+\\.(?:to\\w+|get\\w+)"
                    + "\\s*\\([^)]*\\)"
    );

    private static final Pattern JAVADOC_BLOCK = Pattern.compile(
            "/\\*\\*(.*?)\\*/",
            Pattern.DOTALL
    );

    private static final Pattern GENERIC_CLASS_SENTENCE = Pattern.compile(
            "^\\s*(?:Implements|Provides|Handles|Manages|Represents|Defines"
                    + "|Contains)\\b[^.]{10,160}\\.",
            Pattern.CASE_INSENSITIVE
    );

    private static final String[] GENERIC_CLASS_WORDS = {
            "database", "operations", "retrieval", "searching", "functionality",
            "data access", "information", "records", "management", "processing"
    };

    private static final Pattern GENERIC_RETURN = Pattern.compile(
            "@return\\s+(?:a|an|the)\\s+(?:new\\s+)?"
                    + "(?:list|collection|map|set|array)\\s+of\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void analyze(AnalysisContext context) {

        String raw = context.getCode();
        String structure = SourceText.stripCommentsAndLiterals(raw);

        scoreBindingLoop(context, raw, structure);
        scoreRuntimeExceptionWrapper(context, raw, structure);
        scoreInlineSql(context, raw, structure);
        scoreNullSafeMapping(context, raw, structure);
        scoreGenericClassJavadoc(context);
        scoreGenericReturnJavadoc(context);
    }

    /**
     * Generated search code often binds parameters through a loop driven by a
     * hardcoded count, sometimes hidden behind a constant such as
     * {@code SEARCH_PARAMETER_COUNT}. The loop and the SQL text can then drift
     * apart without a compile error.
     */
    private void scoreBindingLoop(
            AnalysisContext context,
            String raw,
            String structure) {

        String parameterCountConstant = findParameterCountConstant(structure);

        Matcher loop = BINDING_LOOP.matcher(structure);

        if (!loop.find()) {

            if (parameterCountConstant != null) {
                context.addFinding(
                        Categories.STRUCTURE,
                        3,
                        "'Abstracted but brittle' " + parameterCountConstant
                                + " constant is coupled to the SQL text; if the "
                                + "query changes, the binding silently breaks.",
                        declarationLine(raw, parameterCountConstant)
                );
            }

            return;
        }

        String variable = loop.group(1);
        String bound = loop.group(2);

        Pattern binding = Pattern.compile(
                "set[A-Z]\\w*\\s*\\(\\s*" + Pattern.quote(variable) + "\\s*,"
        );

        if (!binding.matcher(structure).find()) {
            return;
        }

        String fragment = AnalyzerSupport.matchedLine(raw, BINDING_LOOP);

        if (bound.equals(parameterCountConstant)) {
            context.addFinding(
                    Categories.STRUCTURE,
                    5,
                    "JDBC binding loop driven by the 'abstracted but brittle' "
                            + bound + " constant; if the SQL changes, the bind "
                            + "count silently drifts.",
                    fragment
            );
        } else {
            context.addFinding(
                    Categories.STRUCTURE,
                    4,
                    "JDBC binding loop with a hardcoded parameter count is a "
                            + "common LLM shortcut; the loop and the query can "
                            + "silently fall out of sync.",
                    fragment
            );
        }
    }

    /**
     * A catch block that replaces the original failure with
     * {@code throw new RuntimeException("Failed to ...", e)} loses the domain
     * meaning of the error, which is a common generated-code reflex.
     */
    private void scoreRuntimeExceptionWrapper(
            AnalysisContext context,
            String raw,
            String structure) {

        Matcher withCause = RUNTIME_EXCEPTION_WITH_CAUSE.matcher(raw);
        String message = null;
        String fragment = "";

        if (withCause.find()) {
            message = withCause.group(1);
            fragment = AnalyzerSupport.matchedLine(
                    raw,
                    RUNTIME_EXCEPTION_WITH_CAUSE
            );
        } else {
            Matcher generic = RUNTIME_EXCEPTION_GENERIC.matcher(raw);

            if (generic.find() && structure.contains("catch")) {
                message = generic.group(1);
                fragment = AnalyzerSupport.matchedLine(
                        raw,
                        RUNTIME_EXCEPTION_GENERIC
                );
            }
        }

        if (message == null) {
            return;
        }

        context.addFinding(
                Categories.LOGIC,
                4,
                "Generic RuntimeException wrapper with a message (\""
                        + AnalyzerSupport.truncate(message, 45)
                        + "\") is a common reflex; prefer a domain-specific "
                        + "exception and log the context.",
                fragment
        );

        context.addTechnicalNote(
                "Use a domain-specific exception and log the original cause "
                        + "instead of wrapping it in a generic RuntimeException."
        );
    }

    /**
     * Generated search DAOs tend to inline the whole SQL string and alias
     * columns ({@code p.id AS p_id}) instead of keeping query constants, often
     * with the same {@code LOWER(...) LIKE LOWER(?)} predicate repeated.
     */
    private void scoreInlineSql(
            AnalysisContext context,
            String raw,
            String structure) {

        if (!isDaoImpl(context) || !structure.contains("prepareStatement")) {
            return;
        }

        int lowerLikes = AnalyzerSupport.count(LOWER_LIKE, raw);
        boolean alias = false;
        String fragment = "";

        Matcher literal = SQL_LITERAL.matcher(raw);

        while (literal.find()) {

            String sql = literal.group(1).trim();

            if (!sql.toLowerCase(Locale.ROOT).startsWith("select ")) {
                continue;
            }

            String lower = sql.toLowerCase(Locale.ROOT);

            if (fragment.isEmpty()) {
                fragment = AnalyzerSupport.truncate(sql, 90);
            }

            if (lower.contains(" as ")
                    && lower.matches("(?s).*\\bas\\s+[a-z]\\w*_[a-z]\\w*.*")) {
                alias = true;
                fragment = AnalyzerSupport.truncate(sql, 90);
                break;
            }
        }

        if (lowerLikes >= 2) {
            fragment = AnalyzerSupport.matchedLine(raw, LOWER_LIKE);
        }

        if (!alias && lowerLikes < 2) {
            return;
        }

        String reason = "Inline SQL in the DAO instead of query constants, "
                + "with aliased columns (for example p.id AS p_id) and/or "
                + "repeated LOWER(...) LIKE LOWER(?) predicates. A very common "
                + "AI-generated search pattern; the repeated LOWER() calls are "
                + "also index-unfriendly.";

        context.addFinding(
                Categories.STRUCTURE,
                5,
                reason,
                fragment
        );
    }

    /**
     * Compact null-safe ternaries in row mappers are good practice, but the
     * exact {@code value == null ? null : value.toX()} shape is a frequent
     * generated-code fingerprint, so it carries a small weight only.
     */
    private void scoreNullSafeMapping(
            AnalysisContext context,
            String raw,
            String structure) {

        if (!isDaoImpl(context)) {
            return;
        }

        if (NULL_SAFE_TERNARY.matcher(structure).find()) {
            context.addFinding(
                    Categories.LOGIC,
                    1,
                    "Null-safe mapping is good practice, but the compact "
                            + "ternary style is common in AI-generated row "
                            + "mappers.",
                    AnalyzerSupport.matchedLine(raw, NULL_SAFE_TERNARY)
            );
        }
    }

    /**
     * A polished class Javadoc that only restates the obvious ("Implements
     * database patient searching and medical record retrieval.") is typical of
     * generated documentation.
     */
    private void scoreGenericClassJavadoc(AnalysisContext context) {

        if (!isDaoOrTest(context)) {
            return;
        }

        String text = firstJavadocText(context.getComments());

        if (text.isEmpty()) {
            return;
        }

        Matcher sentence = GENERIC_CLASS_SENTENCE.matcher(text);

        if (!sentence.find()) {
            return;
        }

        String value = sentence.group().trim();
        String lower = value.toLowerCase(Locale.ROOT);
        boolean generic = false;

        for (String word : GENERIC_CLASS_WORDS) {
            if (lower.contains(word)) {
                generic = true;
                break;
            }
        }

        if (!generic) {
            return;
        }

        context.addFinding(
                Categories.COMMENTS,
                3,
                "Polished, generic class Javadoc that restates the obvious "
                        + "(\"" + AnalyzerSupport.truncate(value, 70)
                        + "\"). Very common LLM phrasing.",
                AnalyzerSupport.truncate(value, 90)
        );
    }

    /**
     * "&#64;return a list of ..." restates the signature without adding
     * information, which is typical of generated documentation.
     */
    private void scoreGenericReturnJavadoc(AnalysisContext context) {

        if (!isDaoOrTest(context)) {
            return;
        }

        Matcher matcher = GENERIC_RETURN.matcher(context.getComments());

        if (matcher.find()) {
            String value = AnalyzerSupport.truncate(matcher.group(), 60);

            context.addFinding(
                    Categories.COMMENTS,
                    3,
                    "Generic Javadoc restating the return type (\"" + value
                            + "\") is typical AI documentation.",
                    value
            );
        }
    }

    private String findParameterCountConstant(String structure) {

        Matcher matcher = INT_CONSTANT.matcher(structure);

        while (matcher.find()) {

            String name = matcher.group(1);

            if (name.contains("PARAM") && name.contains("COUNT")) {
                return name;
            }
        }

        return null;
    }

    private String declarationLine(String raw, String name) {

        for (String line : raw.split("\\R")) {
            if (line.contains(name)
                    && line.contains("static")
                    && line.contains("final")) {
                return line.trim();
            }
        }

        return "";
    }

    private boolean isDaoImpl(AnalysisContext context) {
        return context.getFile().getFileName().toString()
                .endsWith("DaoImpl.java");
    }

    private boolean isDaoOrTest(AnalysisContext context) {

        String name = context.getFile().getFileName().toString();

        return name.endsWith("DaoImpl.java")
                || name.endsWith("ServiceImpl.java")
                || name.endsWith("Test.java")
                || name.endsWith("Tests.java");
    }

    /**
     * Returns the text of the first Javadoc block with its leading asterisks
     * removed, so a sentence rule can run against the class description.
     */
    private String firstJavadocText(String comments) {

        Matcher matcher = JAVADOC_BLOCK.matcher(comments);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1)
                .replaceAll("(?m)^\\s*\\*+\\s?", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
