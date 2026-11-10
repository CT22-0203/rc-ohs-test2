import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects documentation that reads like generated boilerplate: repeated
 * "this method is used to ..." Javadoc, tutorial-style numbered steps and
 * filler phrases such as "note that" / "here is an example".
 *
 * <p>It also looks for the copy-paste documentation style common to generated
 * test suites: the exact same Javadoc sentence repeated across many members,
 * and a Javadoc block on nearly every field and method.</p>
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

    /** A single Javadoc block. Used to measure documentation coverage. */
    private static final Pattern JAVADOC_BLOCK = Pattern.compile("/\\*\\*");

    private static final Pattern CLASS_NAME = Pattern.compile(
            "\\b(?:class|record|enum|interface)\\s+(\\w+)"
    );

    /**
     * Any method declaration, including package-private test methods. Test
     * classes often leave test methods without an access modifier.
     */
    private static final Pattern METHOD_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:(?:public|protected|private|static|final|abstract"
                    + "|synchronized)\\s+)*void\\s+\\w+\\s*\\("
    );

    private static final Pattern FIELD_DECLARATION = Pattern.compile(
            "(?m)^\\s*(?:public|protected|private)\\s+"
                    + "(?:static\\s+|final\\s+|transient\\s+|volatile\\s+)*"
                    + "(?:[\\w.$<>\\[\\], ?]+\\s+)*\\w+\\s*(?:=|;)"
    );

    /** Comment lines that are too generic or legal to count as boilerplate. */
    private static final String[] REPEAT_EXCLUSIONS = {
            "copyright", "license", "licensed", "spdx", "all rights reserved",
            "http://", "https://", "www.", "@author", "@version",
            "@since", "<p>", "{@link", "{@code", "all rights reserved"
    };

    private static final Pattern COMMENT_PREFIX = Pattern.compile(
            "^(?://+|/\\*+|\\*+/?)\\s*"
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

        scoreRepeatedDocumentation(context, comments);
        scoreDocumentationCoverage(context);
    }

    /**
     * Detects the same long documentation sentence copied onto many members.
     * A one-off shared sentence is normal; the same Javadoc tag repeated five
     * or more times is a strong sign of generated boilerplate.
     */
    private void scoreRepeatedDocumentation(
            AnalysisContext context,
            String comments) {

        Map<String, Integer> seen = new HashMap<>();
        String worstLine = null;
        int worstCount = 0;

        for (String rawLine : comments.split("\\R")) {

            String line = normalizeCommentLine(rawLine);

            if (!isScorableCommentLine(line)) {
                continue;
            }

            int count = seen.merge(line, 1, Integer::sum);

            if (count > worstCount) {
                worstCount = count;
                worstLine = line;
            }
        }

        if (worstCount >= 5) {
            context.addPoints(
                    Categories.COMMENTS,
                    10,
                    "The same documentation sentence is repeated "
                            + worstCount + " times: \""
                            + AnalyzerSupport.truncate(worstLine, 60) + "\""
            );
        } else if (worstCount >= 3) {
            context.addPoints(
                    Categories.COMMENTS,
                    6,
                    "The same documentation sentence is repeated "
                            + worstCount + " times: \""
                            + AnalyzerSupport.truncate(worstLine, 60) + "\""
            );
        } else if (worstCount == 2 && wordCount(worstLine) >= 10) {
            context.addPoints(
                    Categories.COMMENTS,
                    3,
                    "A long documentation sentence is duplicated verbatim: \""
                            + AnalyzerSupport.truncate(worstLine, 60) + "\""
            );
        }
    }

    /**
     * Detects test suites where every field and test method carries its own
     * Javadoc. Generated tests tend to document even private mocks this way,
     * while hand-written suites usually annotate the tests themselves.
     */
    private void scoreDocumentationCoverage(AnalysisContext context) {

        String code = context.getCode();

        Matcher classMatcher = CLASS_NAME.matcher(code);
        String className = classMatcher.find() ? classMatcher.group(1) : "";

        boolean testClass = className.endsWith("Test")
                || className.endsWith("Tests")
                || code.contains("@Test");

        if (!testClass) {
            return;
        }

        int javadocs = AnalyzerSupport.count(JAVADOC_BLOCK, code);

        if (javadocs < 8) {
            return;
        }

        int members = AnalyzerSupport.count(METHOD_DECLARATION, code)
                + AnalyzerSupport.count(FIELD_DECLARATION, code);

        if (members < 8) {
            return;
        }

        // Javadoc blocks include the class-level comment, so cap the ratio.
        double coverage = Math.min(1.0, javadocs / (double) members);

        if (coverage >= 0.9) {
            context.addPoints(
                    Categories.COMMENTS,
                    4,
                    "Javadoc on nearly every field and test method ("
                            + javadocs + " blocks for " + members + " members)."
            );
        }
    }

    private String normalizeCommentLine(String rawLine) {

        String line = COMMENT_PREFIX.matcher(rawLine.trim()).replaceFirst("");

        return line.replaceAll("\\s+", " ").trim();
    }

    private boolean isScorableCommentLine(String line) {

        if (line.length() < 25 || wordCount(line) < 5) {
            return false;
        }

        String lower = line.toLowerCase();

        for (String exclusion : REPEAT_EXCLUSIONS) {
            if (lower.contains(exclusion)) {
                return false;
            }
        }

        return true;
    }

    private int wordCount(String line) {

        if (line == null || line.isEmpty()) {
            return 0;
        }

        return line.split(" ").length;
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
