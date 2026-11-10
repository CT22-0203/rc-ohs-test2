import java.util.regex.Pattern;

/**
 * Small, dependency-free helpers for separating Java code from its comments
 * and string literals.
 *
 * <p>Naive regular expressions over raw source produce two kinds of mistakes:
 * rules match inside comments or string literals (inflating scores), and brace
 * counting breaks when a string or char literal contains {@code '{'} or
 * {@code '}'}. Both problems disappear once literals and comments are removed
 * before the code checks run.</p>
 */
public final class SourceText {

    /** Files containing this marker are skipped by the detector. */
    private static final Pattern SUPPRESSION = Pattern.compile(
            "@generated\\b|ai-detect\\s*:\\s*ignore|slop-ignore",
            Pattern.CASE_INSENSITIVE
    );

    private SourceText() {
    }

    /**
     * Removes comments but keeps string and char literals intact. Use this
     * when the rule needs to inspect literal content (SQL text, messages).
     */
    public static String stripComments(String source) {
        return transform(source, false);
    }

    /**
     * Removes comments and blanks out string/char literals while preserving
     * line breaks, so line-based heuristics still see the original layout.
     */
    public static String stripCommentsAndLiterals(String source) {
        return transform(source, true);
    }

    private static String transform(String source, boolean stripLiterals) {

        StringBuilder code = new StringBuilder(source.length());
        int index = 0;

        while (index < source.length()) {

            char current = source.charAt(index);
            char next = index + 1 < source.length()
                    ? source.charAt(index + 1)
                    : '\0';

            if (current == '/' && next == '/') {
                index = skipLineComment(source, index);
            } else if (current == '/' && next == '*') {
                index = skipBlockComment(source, index, code);
            } else if (isTextBlock(source, index)) {
                int end = skipTextBlock(source, index);

                if (stripLiterals) {
                    code.append("\"\"");
                } else {
                    code.append(source, index, end);
                }

                index = end;
            } else if (current == '"') {
                int end = skipQuoted(source, index + 1, '"');

                if (stripLiterals) {
                    code.append("\"\"");
                } else {
                    code.append(source, index, end);
                }

                index = end;
            } else if (current == '\'') {
                int end = skipQuoted(source, index + 1, '\'');

                if (stripLiterals) {
                    code.append("' '");
                } else {
                    code.append(source, index, end);
                }

                index = end;
            } else {
                code.append(current);
                index++;
            }
        }

        return code.toString();
    }

    /**
     * Collects only the comment text of a source file.
     */
    public static String extractComments(String source) {

        StringBuilder comments = new StringBuilder();
        int index = 0;

        while (index < source.length()) {

            char current = source.charAt(index);
            char next = index + 1 < source.length()
                    ? source.charAt(index + 1)
                    : '\0';

            if (isTextBlock(source, index)) {
                index = skipTextBlock(source, index);
            } else if (current == '"') {
                index = skipQuoted(source, index + 1, '"');
            } else if (current == '\'') {
                index = skipQuoted(source, index + 1, '\'');
            } else if (current == '/' && next == '/') {
                int end = source.indexOf('\n', index);
                end = end < 0 ? source.length() : end;
                comments.append(source, index, end).append('\n');
                index = end;
            } else if (current == '/' && next == '*') {
                int end = source.indexOf("*/", index + 2);
                end = end < 0 ? source.length() : end + 2;
                comments.append(source, index, end).append('\n');
                index = end;
            } else {
                index++;
            }
        }

        return comments.toString();
    }

    /** Number of lines, with a sensible minimum of one for empty input. */
    public static int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }

        int lines = 1;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        return lines;
    }

    /**
     * True when the file opts out of detection or is generated code.
     *
     * <p>Only comments and annotations are inspected, so a marker that merely
     * appears inside a string literal (for example a test fixture or a
     * message) does not silently skip the whole file.</p>
     */
    public static boolean isSuppressed(String source) {
        return SUPPRESSION.matcher(extractComments(source)).find()
                || SUPPRESSION.matcher(stripCommentsAndLiterals(source)).find();
    }

    private static int skipLineComment(String source, int start) {
        int end = source.indexOf('\n', start);
        return end < 0 ? source.length() : end;
    }

    private static int skipBlockComment(
            String source,
            int start,
            StringBuilder code) {

        int end = source.indexOf("*/", start + 2);
        end = end < 0 ? source.length() : end + 2;

        for (int i = start; i < end; i++) {
            if (source.charAt(i) == '\n') {
                code.append('\n');
            }
        }

        if (end > start && source.charAt(end - 1) != '\n') {
            code.append(' ');
        }

        return end;
    }

    private static boolean isTextBlock(String source, int start) {
        return start + 2 < source.length()
                && source.charAt(start) == '"'
                && source.charAt(start + 1) == '"'
                && source.charAt(start + 2) == '"';
    }

    private static int skipTextBlock(String source, int start) {
        int end = source.indexOf("\"\"\"", start + 3);
        return end < 0 ? source.length() : end + 3;
    }

    private static int skipQuoted(String source, int start, char quote) {

        int index = start;

        while (index < source.length()) {

            char current = source.charAt(index);

            if (current == '\\') {
                index += 2;
                continue;
            }

            if (current == quote) {
                return index + 1;
            }

            index++;
        }

        return source.length();
    }
}
