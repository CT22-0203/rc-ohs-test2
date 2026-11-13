import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared helpers for counting and for normalizing counts by file size.
 */
final class AnalyzerSupport {

    private AnalyzerSupport() {
    }

    static int count(String text, String needle) {

        if (text == null || needle == null || needle.isEmpty()) {
            return 0;
        }

        int total = 0;
        int index = 0;

        while ((index = text.indexOf(needle, index)) != -1) {
            total++;
            index += needle.length();
        }

        return total;
    }

    static int count(Pattern pattern, String text) {

        if (text == null) {
            return 0;
        }

        Matcher matcher = pattern.matcher(text);
        int total = 0;

        while (matcher.find()) {
            total++;
        }

        return total;
    }

    /**
     * Normalizes a raw count so large files are not punished for their size.
     * Files up to 300 lines are compared as-is; bigger files need to show a
     * proportionally larger number of indicators.
     */
    static double scaleForSize(int count, int lines) {
        double factor = Math.max(1.0, lines / 300.0);
        return count / factor;
    }

    static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        String compact = text.trim().replaceAll("\\s+", " ");

        if (compact.length() <= maxLength) {
            return compact;
        }

        return compact.substring(0, maxLength).trim() + "...";
    }

    /**
     * Returns the first line that the pattern matches, trimmed and shortened
     * so it can be shown in the report as a visible code fragment.
     *
     * @param source The text to search.
     * @param pattern The rule's pattern.
     * @return The matched line, or an empty string when nothing matches.
     */
    static String matchedLine(String source, Pattern pattern) {

        if (source == null || pattern == null) {
            return "";
        }

        Matcher matcher = pattern.matcher(source);

        if (!matcher.find()) {
            return "";
        }

        int start = source.lastIndexOf('\n', matcher.start()) + 1;
        int end = source.indexOf('\n', matcher.end());

        if (end < 0) {
            end = source.length();
        }

        return truncate(source.substring(start, end), 90);
    }
}
