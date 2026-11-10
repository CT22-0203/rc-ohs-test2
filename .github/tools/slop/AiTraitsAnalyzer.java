import java.util.regex.Pattern;

public class AiTraitsAnalyzer implements CodeAnalyzer {

    @Override
    public void analyze(AnalysisContext context) {

        String code = context.getCode();

        if (Pattern.compile(
                "Generated\\s+by\\s+AI|AI-generated",
                Pattern.CASE_INSENSITIVE
        ).matcher(code).find()) {

            context.addPoints(
                    "Typical AI Traits",
                    20,
                    "Explicit AI-generation marker found in source code."
            );
        }

        if (code.contains("TODO: implement")) {
            context.addPoints(
                    "Typical AI Traits",
                    5,
                    "Implementation placeholder detected."
            );
        }

        if (count(code, "UnsupportedOperationException") >= 2
                && count(code, "TODO") >= 2) {

            context.addPoints(
                    "Typical AI Traits",
                    6,
                    "Multiple unfinished implementation indicators detected."
            );
        }

        if (Pattern.compile(
                "catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}"
        ).matcher(code).find()) {

            context.addPoints(
                    "Typical AI Traits",
                    4,
                    "Empty exception-handling block detected."
            );
        }
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