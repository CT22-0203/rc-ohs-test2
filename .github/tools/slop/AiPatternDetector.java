import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Heuristic scanner for coding patterns that commonly appear in generated
 * ("AI slop") code.
 *
 * <p>The score is an aggregate of five categories and is intentionally
 * explainable: every point is backed by an evidence line in the report. It is
 * not a satisfiable authorship test.</p>
 *
 * <p>Exit codes:</p>
 * <ul>
 *   <li>{@code 0} - analysis completed and the score is below the threshold.</li>
 *   <li>{@code 1} - analysis completed and the score reached the threshold.</li>
 *   <li>{@code 2} - the detector could not run (bad arguments, unreadable
 *       input, unexpected failure).</li>
 * </ul>
 *
 * <p>Usage: {@code AiPatternDetector [options] <source-dir> <report-file>}</p>
 */
public class AiPatternDetector {

    private static final int MAX_SCORE = 100;
    private static final int DEFAULT_THRESHOLD = 5;

    /**
     * Weight of the worst file relative to the mean. A single generated file
     * should not disappear into the average score of a large repository, but
     * the mean still matters so one noisy file alone does not define the
     * project.
     */
    private static final double MAX_FILE_WEIGHT = 0.60;

    private static final int MAX_TABLE_FILES = 20;
    private static final int MAX_FINDING_FILES = 15;
    private static final int MAX_EVIDENCE_LINES = 40;
    private static final int MAX_LISTED_FILES = 50;

    private static final List<String> DEFAULT_EXCLUDES = Arrays.asList(
            "build", "target", ".git", "out", "node_modules", "generated"
    );

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");

    private static final List<CodeAnalyzer> ANALYZERS = Arrays.asList(
            new NamingAnalyzer(),
            new CommentAnalyzer(),
            new StructureAnalyzer(),
            new AiTraitsAnalyzer(),
            new LogicAnalyzer(),
            new DaoPatternAnalyzer()
    );

    public static void main(String[] args) {

        Options options;

        try {
            options = Options.parse(args);
        } catch (RuntimeException e) {
            System.err.println("ai-detect: " + e.getMessage());
            printUsage(System.err);
            System.exit(2);
            return;
        }

        if (options.help) {
            printUsage(System.out);
            return;
        }

        try {
            run(options);
        } catch (Exception e) {
            System.err.println("ai-detect: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void run(Options options) throws IOException {

        Selection selection = selectFiles(options);

        List<AnalysisContext> results = new ArrayList<>();
        List<String> skipped = new ArrayList<>(selection.skipped);
        int unreadable = 0;

        for (Path file : selection.files) {

            String code;

            try {
                code = new String(
                        Files.readAllBytes(file),
                        StandardCharsets.UTF_8
                );
            } catch (IOException e) {
                unreadable++;
                skipped.add(display(file) + " (unreadable: " + e.getMessage() + ")");
                continue;
            }

            if (SourceText.isSuppressed(code)) {
                skipped.add(display(file)
                        + " (contains @generated or ai-detect:ignore)");
                continue;
            }

            AnalysisContext context = new AnalysisContext(
                    file,
                    code,
                    SourceText.extractComments(code)
            );

            for (CodeAnalyzer analyzer : ANALYZERS) {
                analyzer.analyze(context);
            }

            results.add(context);
        }

        // Suppressed or deleted files are a legitimate pass; files that exist
        // but cannot be read mean the tool itself failed.
        if (results.isEmpty() && unreadable > 0) {
            throw new IOException(
                    "no files could be analyzed; check the source paths and permissions"
            );
        }

        CrossFileAnalyzer.analyze(results);

        int totalScore = 0;
        int maxFileScore = 0;

        for (AnalysisContext result : results) {
            int score = result.getTotalScore();
            totalScore += score;
            maxFileScore = Math.max(maxFileScore, score);
        }

        int meanFileScore = results.isEmpty()
                ? 0
                : (int) Math.round((double) totalScore / results.size());

        int aggregateScore = (int) Math.round(
                MAX_FILE_WEIGHT * maxFileScore
                        + (1.0 - MAX_FILE_WEIGHT) * meanFileScore
        );

        aggregateScore = Math.min(MAX_SCORE, aggregateScore);

        int filesAtOrAboveThreshold = 0;

        for (AnalysisContext result : results) {
            if (result.getTotalScore() >= options.threshold) {
                filesAtOrAboveThreshold++;
            }
        }

        boolean detected = aggregateScore >= options.threshold
                || filesAtOrAboveThreshold > 0;

        generateReport(
                options,
                results,
                skipped,
                aggregateScore,
                meanFileScore,
                maxFileScore,
                filesAtOrAboveThreshold,
                detected
        );

        writeSummary(
                options,
                results.size(),
                skipped.size(),
                filesAtOrAboveThreshold,
                aggregateScore,
                meanFileScore,
                maxFileScore,
                highestFile(results),
                detected
        );

        System.out.println("AI Detection: "
                + (detected ? "FAILED (threshold reached)" : "PASSED"));
        System.out.println("Files analyzed: " + results.size());
        System.out.println("Files skipped: " + skipped.size());
        System.out.println("Aggregate score: " + aggregateScore + "/100"
                + " (highest file " + maxFileScore
                + ", mean " + meanFileScore + ")");
        System.out.println("Threshold: " + options.threshold + "/100");
        System.out.println("Report: " + options.reportFile);

        if (detected) {
            System.exit(1);
        }
    }

    /**
     * Collects the files to analyze, either from a directory walk or from a
     * list produced by CI (for example, {@code git diff --name-only}).
     */
    private static Selection selectFiles(Options options) throws IOException {

        Selection selection = new Selection();

        if (options.filesFrom != null) {

            if (!Files.isRegularFile(options.filesFrom)) {
                throw new IOException(
                        "file list not found: " + options.filesFrom
                );
            }

            Set<Path> seen = new LinkedHashSet<>();

            for (String line : Files.readAllLines(
                    options.filesFrom,
                    StandardCharsets.UTF_8
            )) {

                String entry = line.trim();

                if (entry.isEmpty() || entry.startsWith("#")) {
                    continue;
                }

                // Tolerate a UTF-8 byte-order mark at the start of the list.
                if (entry.charAt(0) == '\uFEFF') {
                    entry = entry.substring(1).trim();
                }

                if (entry.isEmpty() || entry.startsWith("#")) {
                    continue;
                }

                Path file = Path.of(entry).normalize();

                if (!entry.endsWith(".java")) {
                    continue;
                }

                if (!Files.isRegularFile(file)) {
                    selection.skipped.add(
                            entry + " (listed for analysis but not found)"
                    );
                    continue;
                }

                if (seen.add(file)) {
                    selection.files.add(file);
                }
            }

            return selection;
        }

        if (!Files.isDirectory(options.sourceDir)) {
            throw new IOException(
                    "source directory not found: " + options.sourceDir
            );
        }

        try (Stream<Path> files = Files.walk(options.sourceDir)) {
            selection.files.addAll(
                    files
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".java"))
                            .filter(path -> !isExcluded(options, path))
                            .sorted()
                            .collect(Collectors.toList())
            );
        }

        return selection;
    }

    private static boolean isExcluded(Options options, Path path) {

        String normalized = path.toString().replace('\\', '/');

        for (String directory : DEFAULT_EXCLUDES) {
            if (normalized.contains("/" + directory + "/")
                    || normalized.startsWith(directory + "/")
                    || normalized.endsWith("/" + directory)) {
                return true;
            }
        }

        for (PathMatcher matcher : options.excludeMatchers) {
            if (matcher.matches(path)) {
                return true;
            }
        }

        return false;
    }

    private static void generateReport(
            Options options,
            List<AnalysisContext> results,
            List<String> skipped,
            int score,
            int meanFileScore,
            int maxFileScore,
            int filesAtOrAboveThreshold,
            boolean detected) throws IOException {

        Path report = options.reportFile;

        if (report.getParent() != null) {
            Files.createDirectories(report.getParent());
        }

        List<AnalysisContext> ranked = new ArrayList<>(results);
        ranked.sort(
                Comparator.comparingInt(AnalysisContext::getTotalScore)
                        .reversed()
                        .thenComparing(result -> result.getFile().toString())
        );

        List<String> output = new ArrayList<>();

        output.add("# AI Code Pattern Detection Report");
        output.add("");
        output.add("- Generated: "
                + LocalDateTime.now(ZoneOffset.UTC).format(TIMESTAMP));
        output.add("- Scope: " + describeScope(options, results.size()));
        output.add("- Threshold: " + options.threshold + "/100");
        output.add("");
        output.add("## Result");
        output.add("");
        output.add("**" + (detected ? "FAILED" : "PASSED") + "** — "
                + (detected
                        ? "the aggregate score or at least one file reached the "
                                + "configured threshold."
                        : "neither the aggregate score nor any file reached the "
                                + "configured threshold."));
        output.add("");
        output.add("| Metric | Value |");
        output.add("| --- | --- |");
        output.add("| Aggregate score | **" + score + "/100** |");
        output.add("| Highest file score | " + maxFileScore + "/100"
                + (highestFile(results) == null
                        ? ""
                        : " (`" + highestFile(results) + "`)")
                + " |");
        output.add("| Mean file score | " + meanFileScore + "/100 |");
        output.add("| Files analyzed | " + results.size() + " |");
        output.add("| Files at/above threshold | "
                + filesAtOrAboveThreshold + " |");
        output.add("| Files skipped | " + skipped.size() + " |");
        output.add("");
        output.add("Aggregate score = 60% of the highest file score + 40% of the "
                + "mean file score. Detection also fails when any single file "
                + "reaches the threshold, so one generated file is not diluted "
                + "by a large, clean codebase.");
        output.add("");

        addFileTable(output, ranked);
        addFileFindings(output, ranked);
        addCategoryAnalysis(output, ranked);
        addTechnicalNotes(output, ranked);
        addSkippedFiles(output, skipped);
        addAnalyzedFiles(output, ranked);

        output.add("## How the score works");
        output.add("");
        output.add("- Five categories are scored 0-20 each; a file can score at "
                + "most 100.");
        output.add("- Explicit AI markers (`Generated by ChatGPT`, "
                + "`Generated with Claude Code`, `Co-authored-by: Copilot`, "
                + "`@author Gemini`, ...) score 20 on their own.");
        output.add("- Generated test style is scored too: the same Javadoc "
                + "sentence repeated across many methods, Javadoc on every "
                + "field and test method, and sentence-length test names.");
        output.add("- Exception handlers that only print to the console also "
                + "score, as does dynamic SQL in DAO code.");
        output.add("- The check fails when the aggregate score or any single "
                + "file reaches the threshold.");
        output.add("- Comments and string literals are stripped before "
                + "declaration and structure rules run; getters/setters are "
                + "excluded from duplication checks.");
        output.add("- To exclude a generated file, add `@generated` or "
                + "`// ai-detect:ignore` to it.");
        output.add("");
        output.add("---");
        output.add("*Heuristic analysis only. This report identifies configured "
                + "patterns and does not prove AI authorship. Common coding "
                + "practices, templates, and project conventions can produce "
                + "similar patterns.*");

        Files.write(report, output, StandardCharsets.UTF_8);
    }

    private static void addFileTable(
            List<String> output,
            List<AnalysisContext> ranked) {

        output.add("## Files With Detected Patterns");
        output.add("");

        List<AnalysisContext> scored = new ArrayList<>();

        for (AnalysisContext result : ranked) {
            if (result.getTotalScore() > 0) {
                scored.add(result);
            }
        }

        if (scored.isEmpty()) {
            output.add("No configured indicators were found in any file.");
            output.add("");
            return;
        }

        output.add("| File | Score | Strongest category |");
        output.add("| --- | --- | --- |");

        int shown = 0;

        for (AnalysisContext result : scored) {

            if (shown == MAX_TABLE_FILES) {
                output.add("");
                output.add("_" + (scored.size() - MAX_TABLE_FILES)
                        + " more file(s) omitted; see the artifact for details._");
                break;
            }

            output.add("| `" + display(result.getFile()) + "` | "
                    + result.getTotalScore() + "/100 | "
                    + result.getDominantCategory() + " |");
            shown++;
        }

        output.add("");
    }

    /**
     * Reviewer-style findings for each scored file: score, confidence band and
     * every reason behind the points. This makes the report explainable per
     * file instead of only showing a project-wide number.
     */
    private static void addFileFindings(
            List<String> output,
            List<AnalysisContext> ranked) {

        output.add("## File-by-File Findings");
        output.add("");

        int shown = 0;

        for (AnalysisContext result : ranked) {

            if (result.getTotalScore() <= 0) {
                continue;
            }

            if (shown == MAX_FINDING_FILES) {
                output.add("_" + (countScored(ranked) - MAX_FINDING_FILES)
                        + " more scored file(s) omitted; see the file table._");
                output.add("");
                break;
            }

            output.add("### `" + display(result.getFile()) + "`");
            output.add("");
            output.add("- Score: **" + result.getTotalScore() + "/100** - "
                    + verdict(result.getTotalScore()));
            output.add("- Strongest category: " + result.getDominantCategory());

            for (String category : Categories.ALL) {
                for (String reason : result.getEvidence().get(category)) {
                    output.add("- " + reason);
                }
            }

            output.add("");
            shown++;
        }

        if (shown == 0) {
            output.add("No file scored above zero.");
            output.add("");
        }

        output.add("_Scores are heuristic evidence, not proof of authorship._");
        output.add("");
    }

    private static int countScored(List<AnalysisContext> ranked) {

        int scored = 0;

        for (AnalysisContext result : ranked) {
            if (result.getTotalScore() > 0) {
                scored++;
            }
        }

        return scored;
    }

    private static String verdict(int score) {

        if (score >= 20) {
            return "**very likely AI-generated or AI-assisted "
                    + "(high confidence)**";
        }

        if (score >= 10) {
            return "**likely AI-generated or AI-assisted "
                    + "(medium confidence)**";
        }

        if (score >= 5) {
            return "**possible AI-generated or AI-assisted content "
                    + "(low-to-medium confidence)**";
        }

        return "**weak AI-style signals only (low confidence)**";
    }

    private static void addCategoryAnalysis(
            List<String> output,
            List<AnalysisContext> ranked) {

        output.add("## Category Analysis");
        output.add("");

        for (String category : Categories.ALL) {

            int total = 0;
            int highest = 0;
            List<String> evidence = new ArrayList<>();

            for (AnalysisContext result : ranked) {
                int categoryScore = result.getScore(category);
                total += categoryScore;
                highest = Math.max(highest, categoryScore);

                for (String item : result.getEvidence().get(category)) {
                    evidence.add("`" + display(result.getFile()) + "`: " + item);
                }
            }

            int mean = ranked.isEmpty()
                    ? 0
                    : (int) Math.round((double) total / ranked.size());

            output.add("### " + category);
            output.add("");
            output.add("Mean: " + mean + "/20 | Highest file: "
                    + highest + "/20");
            output.add("");

            if (evidence.isEmpty()) {
                output.add("- No configured indicators detected.");
            } else {

                int limit = Math.min(evidence.size(), MAX_EVIDENCE_LINES);

                for (int i = 0; i < limit; i++) {
                    output.add("- " + evidence.get(i));
                }

                if (evidence.size() > limit) {
                    output.add("- _" + (evidence.size() - limit)
                            + " more indicator(s) omitted._");
                }
            }

            output.add("");
        }
    }

    private static void addTechnicalNotes(
            List<String> output,
            List<AnalysisContext> ranked) {

        output.add("## Technical Review Notes");
        output.add("");

        boolean hasNotes = false;

        for (AnalysisContext result : ranked) {
            for (String note : result.getTechnicalNotes()) {
                output.add("- `" + display(result.getFile()) + "`: " + note);
                hasNotes = true;
            }
        }

        if (!hasNotes) {
            output.add("- No technical review notes.");
        }

        output.add("");
    }

    private static void addSkippedFiles(
            List<String> output,
            List<String> skipped) {

        if (skipped.isEmpty()) {
            return;
        }

        output.add("## Skipped Files");
        output.add("");
        output.add("These files were not scored:");
        output.add("");

        for (String item : skipped) {
            output.add("- " + item);
        }

        output.add("");
    }

    private static void addAnalyzedFiles(
            List<String> output,
            List<AnalysisContext> ranked) {

        output.add("## Analyzed Files");
        output.add("");
        output.add("**Total Java files analyzed: " + ranked.size() + "**");
        output.add("");

        int limit = Math.min(ranked.size(), MAX_LISTED_FILES);

        for (int i = 0; i < limit; i++) {
            output.add("- `" + display(ranked.get(i).getFile()) + "`");
        }

        if (ranked.size() > limit) {
            output.add("- _" + (ranked.size() - limit)
                    + " more file(s) omitted._");
        }

        output.add("");
    }

    private static void writeSummary(
            Options options,
            int filesAnalyzed,
            int filesSkipped,
            int filesAtOrAboveThreshold,
            int score,
            int meanFileScore,
            int maxFileScore,
            String highestFile,
            boolean detected) throws IOException {

        Path summary = options.summaryFile();

        if (summary.getParent() != null) {
            Files.createDirectories(summary.getParent());
        }

        List<String> lines = new ArrayList<>();
        lines.add("score=" + score);
        lines.add("threshold=" + options.threshold);
        lines.add("detected=" + detected);
        lines.add("files_analyzed=" + filesAnalyzed);
        lines.add("files_skipped=" + filesSkipped);
        lines.add("files_at_or_above_threshold=" + filesAtOrAboveThreshold);
        lines.add("max_file_score=" + maxFileScore);
        lines.add("mean_file_score=" + meanFileScore);
        lines.add("highest_file=" + (highestFile == null ? "" : highestFile));
        lines.add("scope=" + (options.filesFrom != null ? "changed" : "all"));

        Files.write(summary, lines, StandardCharsets.UTF_8);
    }

    private static String describeScope(Options options, int fileCount) {

        String label = options.filesFrom != null
                ? "Java files listed in `" + display(options.filesFrom) + "`"
                : "Java files under `" + display(options.sourceDir) + "`";

        return label + " (" + fileCount + " analyzed)";
    }

    private static String highestFile(List<AnalysisContext> results) {

        AnalysisContext highest = null;

        for (AnalysisContext result : results) {
            if (highest == null
                    || result.getTotalScore() > highest.getTotalScore()) {
                highest = result;
            }
        }

        if (highest == null || highest.getTotalScore() == 0) {
            return null;
        }

        return display(highest.getFile());
    }

    private static String display(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static void printUsage(PrintStream out) {
        out.println("Usage: AiPatternDetector [options] <source-dir> <report-file>");
        out.println();
        out.println("Options:");
        out.println("  --threshold <0-100>  Score at which the check fails"
                + " (default: " + DEFAULT_THRESHOLD + ")");
        out.println("  --files-from <file>   Analyze only paths listed in <file>,");
        out.println("                        one per line (used by CI diffs)");
        out.println("  --exclude <glob>      Skip matching paths (repeatable)");
        out.println("  -h, --help           Show this help");
        out.println();
        out.println("A machine-readable summary is written next to the report as");
        out.println("ai-pattern-summary.properties.");
        out.println();
        out.println("Exit codes: 0 = passed, 1 = threshold reached, 2 = tool error.");
    }

    /**
     * Parsed command-line configuration.
     */
    private static final class Options {

        private int threshold = defaultThreshold();
        private Path sourceDir;
        private Path reportFile;
        private Path filesFrom;
        private boolean help;
        private final List<String> excludes = new ArrayList<>();
        private final List<PathMatcher> excludeMatchers = new ArrayList<>();

        private static Options parse(String[] args) {

            Options options = new Options();
            List<String> positional = new ArrayList<>();

            for (int i = 0; i < args.length; i++) {

                String argument = args[i];

                switch (argument) {
                    case "--threshold":
                        options.threshold = parseThreshold(requireValue(args, ++i, argument));
                        break;
                    case "--files-from":
                        options.filesFrom = Path.of(requireValue(args, ++i, argument));
                        break;
                    case "--exclude":
                        options.excludes.add(requireValue(args, ++i, argument));
                        break;
                    case "-h":
                    case "--help":
                        options.help = true;
                        break;
                    default:
                        if (argument.startsWith("--")) {
                            throw new IllegalArgumentException(
                                    "unknown option: " + argument
                            );
                        }
                        positional.add(argument);
                }
            }

            if (options.help) {
                return options;
            }

            if (positional.size() != 2) {
                throw new IllegalArgumentException(
                        "expected <source-dir> and <report-file>"
                );
            }

            options.sourceDir = Path.of(positional.get(0));
            options.reportFile = Path.of(positional.get(1));

            for (String glob : options.excludes) {
                options.excludeMatchers.add(
                        FileSystems.getDefault().getPathMatcher("glob:" + glob)
                );
            }

            return options;
        }

        private Path summaryFile() {

            Path parent = reportFile.getParent();

            if (parent == null) {
                return Path.of("ai-pattern-summary.properties");
            }

            return parent.resolve("ai-pattern-summary.properties");
        }

        private static String requireValue(
                String[] args,
                int index,
                String option) {

            if (index >= args.length) {
                throw new IllegalArgumentException(
                        "missing value for " + option
                );
            }

            return args[index];
        }

        private static int parseThreshold(String value) {

            int threshold;

            try {
                threshold = Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "--threshold must be a number between 0 and 100"
                );
            }

            if (threshold < 0 || threshold > 100) {
                throw new IllegalArgumentException(
                        "--threshold must be between 0 and 100"
                );
            }

            return threshold;
        }

        private static int defaultThreshold() {

            String configured = System.getProperty("slop.threshold");

            if (configured == null || configured.trim().isEmpty()) {
                return DEFAULT_THRESHOLD;
            }

            return parseThreshold(configured);
        }
    }

    /**
     * Files chosen for analysis plus anything that was intentionally skipped.
     */
    private static final class Selection {

        private final List<Path> files = new ArrayList<>();
        private final List<String> skipped = new ArrayList<>();
    }
}
