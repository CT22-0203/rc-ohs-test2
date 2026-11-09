import java.util.regex.Pattern;

public class LogicAnalyzer implements CodeAnalyzer {

    @Override
    public void analyze(AnalysisContext context) {

        String code = context.getCode();

        boolean sqlException = code.contains("SQLException")
                || code.contains("SQLSyntaxErrorException");

        boolean emptyCollectionFallback =
                code.contains("Collections.emptyList()")
                        || code.contains("new ArrayList<>()");

        if (sqlException && emptyCollectionFallback
                && code.contains("catch")) {

            context.addPoints(
                    "Business Logic Footprints",
                    6,
                    "Database exception handling combined with empty collection fallback."
            );

            context.addTechnicalNote(
                    "Review whether database failures should be logged or propagated."
            );
        }

        boolean dynamicSql = code.contains("StringBuilder")
                && code.contains(".append(")
                && code.toLowerCase().contains("where 1 = 1");

        if (dynamicSql) {
            context.addPoints(
                    "Business Logic Footprints",
                    4,
                    "Dynamic SQL construction pattern detected; review query handling."
            );
        }

        if (Pattern.compile(
                "catch\\s*\\([^)]*\\)\\s*\\{\\s*return\\s+null\\s*;"
        ).matcher(code).find()) {

            context.addPoints(
                    "Business Logic Footprints",
                    5,
                    "Exception handling directly returns null."
            );

            context.addTechnicalNote(
                    "Review exception handling and null-return behavior."
            );
        }

        if (code.contains("System.out.println")
                && code.contains("catch")) {

            context.addTechnicalNote(
                    "Consider using a logging framework for exception reporting."
            );
        }
    }
}
