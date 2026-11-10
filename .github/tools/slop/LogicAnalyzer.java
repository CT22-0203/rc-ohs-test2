import java.util.regex.Pattern;

/**
 * Business-logic anti-patterns that show up frequently in generated DAO and
 * service code: swallowing database errors into empty results, returning
 * {@code null} from a {@code catch} block, and building SQL by concatenation.
 *
 * <p>Patterns are scoped to the {@code catch} block or to SQL construction.
 * The previous version treated any {@code new ArrayList<>()} in a file as an
 * "empty collection fallback", which flagged almost every Java class.</p>
 */
public class LogicAnalyzer implements CodeAnalyzer {

    private static final Pattern CATCH_RETURNS_EMPTY_COLLECTION = Pattern.compile(
            "catch\\s*\\([^)]*\\)\\s*\\{\\s*return\\s+"
                    + "(?:Collections\\.empty(?:List|Map|Set)\\(\\)"
                    + "|new\\s+(?:ArrayList|LinkedList|HashMap|HashSet"
                    + "|TreeMap|TreeSet)\\s*(?:<[^>]*>)?\\s*\\(\\s*\\))\\s*;"
    );

    private static final Pattern CATCH_RETURNS_NULL = Pattern.compile(
            "catch\\s*\\([^)]*\\)\\s*\\{\\s*return\\s+null\\s*;"
    );

    private static final Pattern DYNAMIC_SQL = Pattern.compile(
            "where\\s+1\\s*=\\s*1",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_CONCATENATION = Pattern.compile(
            "\"(?:select|insert|update|delete)\\b[^\"]*\"\\s*\\+\\s*[A-Za-z_$]",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRINT_STACK_TRACE = Pattern.compile(
            "\\.printStackTrace\\s*\\("
    );

    /**
     * A catch block whose only reaction is printing to the console. Generated
     * DAO/service code very often replaces real error handling with
     * {@code System.out.println(e.getMessage())}.
     */
    private static final Pattern CATCH_PRINTS_TO_CONSOLE = Pattern.compile(
            "catch\\s*\\([^)]*\\)\\s*\\{[^}]*System\\.(?:out|err)\\."
                    + "(?:print|printf|println)\\s*\\("
    );

    @Override
    public void analyze(AnalysisContext context) {

        String raw = context.getCode();

        // SQL rules need the literal text, so only comments are removed.
        String code = SourceText.stripComments(raw);

        // Control-flow rules must not match text that only appears inside a
        // string, so literals are blanked for those checks.
        String structure = SourceText.stripCommentsAndLiterals(raw);

        if (CATCH_RETURNS_EMPTY_COLLECTION.matcher(structure).find()) {
            context.addPoints(
                    Categories.LOGIC,
                    6,
                    "Database or service exception is swallowed by returning an "
                            + "empty collection."
            );

            context.addTechnicalNote(
                    "Review whether database failures should be logged or propagated "
                            + "instead of returning an empty collection."
            );
        }

        if (CATCH_RETURNS_NULL.matcher(structure).find()) {
            context.addPoints(
                    Categories.LOGIC,
                    5,
                    "Exception handler returns null directly."
            );

            context.addTechnicalNote(
                    "Review exception handling and null-return behavior."
            );
        }

        boolean hasBuilder = structure.contains("StringBuilder")
                || structure.contains("StringBuffer");

        if (hasBuilder && DYNAMIC_SQL.matcher(code).find()) {
            context.addPoints(
                    Categories.LOGIC,
                    4,
                    "Dynamic SQL built with an always-true WHERE clause; "
                            + "review query construction."
            );

            context.addTechnicalNote(
                    "Review dynamic SQL for injection risk and parameterization."
            );
        }

        if (SQL_CONCATENATION.matcher(code).find()) {
            context.addPoints(
                    Categories.LOGIC,
                    4,
                    "SQL string built by concatenating a variable into the query."
            );

            context.addTechnicalNote(
                    "Prefer PreparedStatement parameters over SQL concatenation."
            );
        }

        if (PRINT_STACK_TRACE.matcher(structure).find()) {
            context.addTechnicalNote(
                    "Consider using a logging framework instead of printStackTrace()."
            );
        }

        if (CATCH_PRINTS_TO_CONSOLE.matcher(structure).find()) {
            context.addPoints(
                    Categories.LOGIC,
                    4,
                    "Exception handler only prints the error to the console."
            );

            context.addTechnicalNote(
                    "Consider logging or propagating the exception instead of "
                            + "printing it to the console."
            );
        }

        if (structure.contains("System.out.println") && structure.contains("catch")) {
            context.addTechnicalNote(
                    "Consider using a logging framework for exception reporting."
            );
        }
    }
}
