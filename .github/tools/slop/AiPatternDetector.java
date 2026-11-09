import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AiPatternDetector {

    private static final int MAX_SCORE = 100;
    private static final int DETECTION_THRESHOLD = 10;

    private static final List<CodeAnalyzer> ANALYZERS =
            Arrays.asList(
                    new NamingAnalyzer(),
                    new CommentAnalyzer(),
                    new StructureAnalyzer(),
                    new AiTraitsAnalyzer(),
                    new LogicAnalyzer()
            );

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println(
                    "Usage: AiPatternDetector <source-dir> <report-file>"
            );
            System.exit(1);
            return;
        }

        Path sourceDir = Path.of(args[0]);
        Path reportFile = Path.of(args[1]);

        if (!Files.isDirectory(sourceDir)) {
            System.err.println(
                    "Source directory not found: " + sourceDir
            );
            System.exit(1);
            return;
        }

        try {

            List<Path> files = scanDirectory(sourceDir);
            List<AnalysisContext> results = new ArrayList<>();

            for (Path file : files) {

                String code = new String(
                        Files.readAllBytes(file),
                        StandardCharsets.UTF_8
                );

                AnalysisContext context = new AnalysisContext(
                        file,
                        code,
                        extractComments(code)
                );

                for (CodeAnalyzer analyzer : ANALYZERS) {
                    analyzer.analyze(context);
                }

                results.add(context);
            }

            int totalScore = calculateTotalScore(results);
            int scoreOutOfTen = (int) Math.round(totalScore / 10.0);

            boolean detected = totalScore >= DETECTION_THRESHOLD;

            generateReport(
                    reportFile,
                    results,
                    totalScore,
                    scoreOutOfTen,
                    detected
            );

            System.out.println(
                    "Java files analyzed: " + files.size()
            );

            System.out.println(
                    "AI-like Pattern Score: " + totalScore + "/100"
            );

            System.out.println(
                    "Converted Score: " + scoreOutOfTen + "/10"
            );

            System.out.println(
                    "Detection Threshold: "
                            + DETECTION_THRESHOLD + "/100"
            );

            System.out.println(
                    "Report generated: " + reportFile
            );

            if (detected) {
                System.err.println(
                        "AI-like patterns detected. Build failed."
                );
                System.exit(1);
                return;
            }

            System.out.println(
                    "AI-like patterns not detected. Build passed."
            );

        } catch (IOException e) {

            System.err.println(
                    "AI Detection error: " + e.getMessage()
            );

            System.exit(1);
        }
    }

    private static List<Path> scanDirectory(Path directory)
            throws IOException {

        try (Stream<Path> files = Files.walk(directory)) {

            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private static int calculateTotalScore(
            List<AnalysisContext> results) {

        int total = 0;

        for (AnalysisContext result : results) {
            total += result.getTotalScore();
        }

        // Average the per-file scores so larger projects
        // are not automatically assigned higher scores.
        if (results.isEmpty()) {
            return 0;
        }

        return Math.min(
                MAX_SCORE,
                (int) Math.round((double) total / results.size())
        );
    }

    private static String extractComments(String code) {

        StringBuilder comments = new StringBuilder();

        Matcher matcher = Pattern.compile(
                "/\\*[\\s\\S]*?\\*/|//[^\\r\\n]*"
        ).matcher(code);

        while (matcher.find()) {
            comments.append(matcher.group()).append('\n');
        }

        return comments.toString();
    }

    private static void generateReport(
            Path report,
            List<AnalysisContext> results,
            int score,
            int scoreOutOfTen,
            boolean detected) throws IOException {

        if (report.getParent() != null) {
            Files.createDirectories(report.getParent());
        }

        List<String> output = new ArrayList<>();

        output.add("# AI Code Pattern Detection Report");
        output.add("");
        output.add("## Detection Result");
        output.add("");
        output.add("**AI-like Pattern Score: " + score + "/100**");
        output.add("");
        output.add("**Converted Score: " + scoreOutOfTen + "/10**");
        output.add("");
        output.add("**Detection Threshold: "
                + DETECTION_THRESHOLD + "/100**");
        output.add("");
        output.add("**Likelihood of AI Generation: Not established**");
        output.add("");
        output.add("This report identifies configured coding patterns only. "
                + "It does not prove AI authorship.");
        output.add("");

        output.add("## Category Analysis");
        output.add("");

        String[] categories = {
                "Naming Style",
                "Comment Style",
                "Code Structure",
                "Typical AI Traits",
                "Business Logic Footprints"
        };

        for (String category : categories) {

            int categoryScore = 0;
            List<String> evidence = new ArrayList<>();

            for (AnalysisContext result : results) {

                categoryScore += result.getScores().get(category);

                for (String item : result.getEvidence().get(category)) {
                    evidence.add(
                            result.getFile() + ": " + item
                    );
                }
            }

            if (!results.isEmpty()) {
                categoryScore = (int) Math.round(
                        (double) categoryScore / results.size()
                );
            }

            output.add("### " + category);
            output.add("");
            output.add("**Average Score: " + categoryScore + "/20**");
            output.add("");

            if (evidence.isEmpty()) {
                output.add("- No configured indicators detected.");
            } else {
                for (String item : evidence) {
                    output.add("- " + item);
                }
            }

            output.add("");
        }

        output.add("## Technical Review Notes");
        output.add("");

        boolean hasNotes = false;

        for (AnalysisContext result : results) {
            for (String note : result.getTechnicalNotes()) {
                output.add("- " + result.getFile() + ": " + note);
                hasNotes = true;
            }
        }

        if (!hasNotes) {
            output.add("- No technical review notes.");
        }

        output.add("");
        output.add("## Analyzed Files");
        output.add("");
        output.add("**Total Java files analyzed: "
                + results.size() + "**");
        output.add("");

        for (AnalysisContext result : results) {
            output.add("- `" + result.getFile() + "`");
        }

        output.add("");
        output.add("## Build Result");
        output.add("");

        if (detected) {
            output.add("**DETECTED — Configured pattern threshold reached.**");
            output.add("");
            output.add("**Build Status: FAILED**");
        } else {
            output.add("**NOT DETECTED — Score is below threshold.**");
            output.add("");
            output.add("**Build Status: PASSED**");
        }

        output.add("");
        output.add("---");
        output.add("*Heuristic analysis only. Human review is recommended. "
                + "Common coding practices, templates, and project conventions "
                + "can produce similar patterns.*");

        Files.write(
                report,
                output,
                StandardCharsets.UTF_8
        );
    }
}
