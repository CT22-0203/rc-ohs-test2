import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects documentation that reads like generated boilerplate: repeated
 * "this method is used to ..." Javadoc, tutorial-style numbered steps and
 * filler phrases such as "note that" / "here is an example".
 *
 * <p>Only comment text is inspected, and phrase counts are normalized by file
 * size so a long, heavily documented class is not penalized.</p>
 */
public class CommentAnalyzer implements CodeAnalyzer {

    private static final String[] TEMPLATE_PHRASES = {
            "this method is used to",
            "this test verifies that",
            "this test checks that",
            "the purpose of this method",
            "this function is responsible for",
            "this class represents",
            "this method returns",
            "this method takes",
            "is used to represent",
            "initializes the test environment",
            "sets up the test environment",
            "assert that the result",
            "helper method to",
            "utility method to",
            "here is an example",
            "here's an example",
            "the following code",
            "as shown below",
            "note that this",
            "please note that",
            "it is important to note",
            "in summary"
    };

    private static final Pattern NUMBERED_STEPS = Pattern.compile(
            "(?m)^\\s*(?://|\\*)\\s*(?:step\\s*)?\\d+\\s*[.):]\\s+\\S",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void analyze(AnalysisContext context) {

        String comments = context.getComments().toLowerCase();
        int lines = context.getLineCount();

        int phraseCount = 0;
        Set<String> distinctPhrases = new LinkedHashSet<>();

        for (String phrase : TEMPLATE_PHRASES) {
            int occurrences = AnalyzerSupport.count(comments, phrase);

            if (occurrences > 0) {
                phraseCount += occurrences;
                distinctPhrases.add(phrase);
            }
        }

        double scaledPhrases = AnalyzerSupport.scaleForSize(phraseCount, lines);

        if (distinctPhrases.size() >= 3 && scaledPhrases >= 6) {
            context.addPoints(
                    Categories.COMMENTS,
                    10,
                    phraseCount + " template-like documentation phrases across "
                            + distinctPhrases.size() + " distinct patterns."
            );
        } else if (distinctPhrases.size() >= 2 && scaledPhrases >= 3) {
            context.addPoints(
                    Categories.COMMENTS,
                    5,
                    phraseCount + " generic documentation phrase(s) detected: "
                            + summarize(distinctPhrases)
            );
        }

        int numberedSteps = AnalyzerSupport.count(NUMBERED_STEPS, comments);

        if (numberedSteps >= 8 && phraseCount >= 2) {
            context.addPoints(
                    Categories.COMMENTS,
                    5,
                    numberedSteps + " numbered step comments combined with "
                            + "generic descriptions."
            );
        }
    }

    private String summarize(Set<String> phrases) {

        StringBuilder summary = new StringBuilder();
        int shown = 0;

        for (String phrase : phrases) {
            if (shown == 3) {
                summary.append(", ...");
                break;
            }

            if (shown > 0) {
                summary.append(", ");
            }

            summary.append('"').append(phrase).append('"');
            shown++;
        }

        return summary.toString();
    }
}
