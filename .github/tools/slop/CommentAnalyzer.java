import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentAnalyzer implements CodeAnalyzer {

    private static final String[] TEMPLATE_PHRASES = {
            "this method is used to",
            "this test verifies that",
            "this test checks that",
            "the purpose of this method",
            "initializes the test environment",
            "sets up the test environment",
            "assert that the result",
            "this function is responsible for"
    };

    @Override
    public void analyze(AnalysisContext context) {

        String comments = context.getComments().toLowerCase();

        int templateCount = 0;

        for (String phrase : TEMPLATE_PHRASES) {
            templateCount += count(comments, phrase);
        }

        if (templateCount >= 5) {
            context.addPoints(
                    "Comment Style",
                    10,
                    "Multiple repeated template-like documentation phrases."
            );
        } else if (templateCount >= 3) {
            context.addPoints(
                    "Comment Style",
                    6,
                    "Several generic documentation phrases detected."
            );
        }

        Matcher matcher = Pattern.compile(
                "(?m)^\\s*//\\s*(?:step|note|important|purpose)\\s*:",
                Pattern.CASE_INSENSITIVE
        ).matcher(comments);

        int structuredComments = 0;

        while (matcher.find()) {
            structuredComments++;
        }

        if (structuredComments >= 8 && templateCount >= 2) {
            context.addPoints(
                    "Comment Style",
                    5,
                    "Repeated structured comment templates combined with generic descriptions."
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
