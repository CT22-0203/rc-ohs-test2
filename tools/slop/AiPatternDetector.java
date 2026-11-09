
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AiPatternDetector {

    private static final int MAX_CATEGORY_SCORE = 20;
    private static final int MAX_SCORE = 100;
    private static final int DETECTION_THRESHOLD = 10;

    private static final Pattern IDENTIFIER =
            Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b");

    private static final Pattern TEST_METHOD =
            Pattern.compile("@Test\\s*(?:\\([^)]*\\))?\\s*(?:public\\s+)?"
                    + "(?:void|[\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(");

    private static final List<String> GENERIC_NAMES = Arrays.asList(
            "data", "result", "temp", "tmp", "object",
            "value", "flag", "helper", "manager", "handler",
            "process", "execute", "response", "request"
    );

    private static final List<String> TEMPLATE_PHRASES = Arrays.asList(
            "this method is used to",
            "this test verifies that",
            "this test checks that",
            "should return the expected",
            "initializes the test environment",
            "sets up the test environment",
            "the purpose of this method",
            "assert that the result"
    );

    private static final Map<String, Category> categories =
            new LinkedHashMap<>();

    private static final List<String> analyzedFiles = new ArrayList<>();
    private static final List<String> technicalNotes = new ArrayList<>();

    private static int totalJavaFiles;

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
            System.err.println("Source directory not found: " + sourceDir);
            System.exit(1);
            return;
        }

        initializeCategories();

        try {
            List<Path> files = scanDirectory(sourceDir);

            totalJavaFiles = files.size();

            for (Path file : files) {
                analyzeFile(file);
            }

            int percentage = calculateTotalScore();
            int scoreOutOfTen = (int) Math.round(percentage / 10.0);

            boolean detected = percentage >= DETECTION_THRESHOLD;

            generateReport(
                    reportFile,
                    percentage,
                    scoreOutOfTen,
                    detected
            );

            System.out.println("Java files analyzed: " + totalJavaFiles);
            System.out.println(
                    "AI-like Pattern Score: " + percentage + "/100"
            );
            System.out.println(
                    "Converted Score: " + scoreOutOfTen + "/10"
            );

            System.out.println(
                    "Detection Threshold: "
                            + DETECTION_THRESHOLD + "/100"
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

    private static void initializeCategories() {

        categories.put("Naming Style", new Category());
        categories.put("Comment Style", new Category());
        categories.put("Code Structure", new Category());
        categories.put("Typical AI Traits", new Category());
        categories.put("Business Logic Footprints", new Category());
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

    private static void analyzeFile(Path file) throws IOException {

        String code = new String(
                Files.readAllBytes(file),
                StandardCharsets.UTF_8
        );

        String normalizedPath = file.toString().replace("\\", "/");

        analyzedFiles.add(normalizedPath);

        analyzeNaming(code, file);
        analyzeComments(code, file);
        analyzeStructure(code, file);
        analyzeAiTraits(code, file);
        analyzeBusinessLogic(code, file);
        analyzeTechnicalIssues(code, file);
    }


    private static void analyzeNaming(String code, Path file) {

        Matcher matcher = IDENTIFIER.matcher(code);

        Map<String, Integer> identifierCounts = new LinkedHashMap<>();

        int totalIdentifiers = 0;

        while (matcher.find()) {

            String identifier = matcher.group();

            if (identifier.length() < 3) {
                continue;
            }

            String normalized = identifier.toLowerCase();

            identifierCounts.put(
                    normalized,
                    identifierCounts.getOrDefault(normalized, 0) + 1
            );

            totalIdentifiers++;
        }

        int genericOccurrences = 0;
        int repeatedGenericNames = 0;

        for (String genericName : GENERIC_NAMES) {

            int occurrences = identifierCounts.getOrDefault(
                    genericName,
                    0
            );

            if (occurrences >= 3) {
                genericOccurrences += occurrences;
                repeatedGenericNames++;
            }
        }

        // Score only when multiple generic names are repeatedly used.
        if (repeatedGenericNames >= 4 && genericOccurrences >= 16) {

            addPoints(
                    "Naming Style",
                    10,
                    file + ": Multiple generic identifiers are repeatedly used."
            );

        } else if (repeatedGenericNames >= 3
                && genericOccurrences >= 12) {

            addPoints(
                    "Naming Style",
                    7,
                    file + ": Repeated use of several generic identifiers."
            );
        }

        // Detect placeholder-style names.
        Matcher placeholderMatcher = Pattern.compile(
                "\\b(test|method|variable|temp|object)[0-9]+\\b",
                Pattern.CASE_INSENSITIVE
        ).matcher(code);

        int placeholderCount = 0;

        while (placeholderMatcher.find()) {
            placeholderCount++;
        }

        if (placeholderCount >= 4) {

            addPoints(
                    "Naming Style",
                    6,
                    file + ": Multiple numbered placeholder identifiers detected."
            );

        } else if (placeholderCount >= 2) {

            addPoints(
                    "Naming Style",
                    3,
                    file + ": Numbered placeholder identifiers detected."
            );
        }

        // Detect excessive one-letter variable-like identifiers.
        Matcher shortNameMatcher = Pattern.compile(
                "\\b(?:int|String|double|float|boolean|long|char)"
                        + "\\s+([a-zA-Z])\\b"
        ).matcher(code);

        int shortNameCount = 0;

        while (shortNameMatcher.find()) {
            shortNameCount++;
        }

        if (shortNameCount >= 6) {

            addPoints(
                    "Naming Style",
                    4,
                    file + ": Frequent one-letter variable declarations detected."
            );
        }
    }


    private static void analyzeComments(String code, Path file) {

        String lower = code.toLowerCase();

        int javadocCount = count(code, "/**");
        int templateCount = 0;

        for (String phrase : TEMPLATE_PHRASES) {
            templateCount += count(lower, phrase);
        }

        if (templateCount >= 4) {
            addPoints(
                    "Comment Style",
                    10,
                    file + ": Multiple generic documentation phrases detected."
            );

        } else if (templateCount >= 2) {
            addPoints(
                    "Comment Style",
                    6,
                    file + ": Repeated template-like comment phrases detected."
            );
        }

        int testCount = count(code, "@Test");

        if (testCount >= 8 && javadocCount >= 8) {
            addPoints(
                    "Comment Style",
                    6,
                    file + ": Extensive repeated Javadoc documentation in test methods."
            );
        }

        if (count(lower, "this method") >= 4) {
            addPoints(
                    "Comment Style",
                    4,
                    file + ": Repeated generic method descriptions."
            );
        }
    }

    private static void analyzeStructure(String code, Path file) {

        int testCount = count(code, "@Test");

        int assertionCount =
                count(code, "assertEquals")
                        + count(code, "assertTrue")
                        + count(code, "assertFalse")
                        + count(code, "assertNotNull")
                        + count(code, "assertNull");

        int mockCount =
                count(code, "when(")
                        + count(code, "mock(")
                        + count(code, "mockStatic(");

        int verifyCount = count(code, "verify(");

        if (testCount >= 8) {
            addPoints(
                    "Code Structure",
                    5,
                    file + ": Large number of test methods in one class."
            );
        }

        if (testCount >= 8 && assertionCount >= 12) {
            addPoints(
                    "Code Structure",
                    5,
                    file + ": Repeated test and assertion structure."
            );
        }

        if (testCount >= 8 && mockCount >= 5 && verifyCount >= 5) {
            addPoints(
                    "Code Structure",
                    7,
                    file + ": Repetitive mocking and verification structure."
            );
        }

        if (count(code, "@BeforeEach") > 1
                || count(code, "@AfterEach") > 1) {

            addPoints(
                    "Code Structure",
                    3,
                    file + ": Repeated test lifecycle annotations."
            );
        }

        Matcher testMatcher = TEST_METHOD.matcher(code);

        List<String> methodNames = new ArrayList<>();

        while (testMatcher.find()) {
            methodNames.add(testMatcher.group(1));
        }

        if (methodNames.size() >= 6) {

            long filterMethods = methodNames.stream()
                    .filter(name ->
                            name.toLowerCase().startsWith("filter"))
                    .count();

            if (filterMethods >= 6) {
                addPoints(
                        "Code Structure",
                        5,
                        file + ": Repeated test method naming and structure."
                );
            }
        }
    }

    private static void analyzeAiTraits(String code, Path file) {

        if (code.contains("Generated by AI")
                || code.contains("AI-generated")) {

            addPoints(
                    "Typical AI Traits",
                    12,
                    file + ": Explicit AI-generation marker found."
            );
        }

        if (code.contains("TODO: implement")) {
            addPoints(
                    "Typical AI Traits",
                    8,
                    file + ": Implementation placeholder detected."
            );
        }

        if (code.contains("@ExtendWith(MockitoExtension.class)")
                && code.contains("@MockitoSettings")
                && code.contains("Strictness.LENIENT")
                && code.contains("ArgumentCaptor")
                && code.contains("mockStatic(")) {

            addPoints(
                    "Typical AI Traits",
                    6,
                    file + ": Multiple advanced Mockito patterns combined."
            );
        }

        if (count(code, "UnsupportedOperationException") >= 2) {
            addPoints(
                    "Typical AI Traits",
                    5,
                    file + ": Multiple unsupported-operation placeholders."
            );
        }

        if (Pattern.compile(
                "catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}"
        ).matcher(code).find()) {

            addPoints(
                    "Typical AI Traits",
                    5,
                    file + ": Empty exception-handling block."
            );
        }
    }

    private static void analyzeBusinessLogic(
            String code, Path file) {

        if (code.contains("catch (SQLException")
                && (code.contains("return new ArrayList")
                || code.contains("return Collections.emptyList"))) {

            addPoints(
                    "Business Logic Footprints",
                    8,
                    file + ": SQL errors may be hidden by returning an empty collection."
            );

            technicalNotes.add(
                    file + ": Review whether database failures should be propagated or logged."
            );
        }

        if (code.contains("UnsupportedOperationException")
                || code.contains("TODO: implement")) {

            addPoints(
                    "Business Logic Footprints",
                    8,
                    file + ": Placeholder implementation may indicate incomplete business logic."
            );
        }

        if (Pattern.compile("return\\s+null\\s*;")
                .matcher(code).find()) {

            addPoints(
                    "Business Logic Footprints",
                    4,
                    file + ": Method returns null; review its business behavior."
            );
        }

        if (code.contains("where 1 = 1")
                && code.contains("StringBuilder")
                && code.contains(".append(")) {

            addPoints(
                    "Business Logic Footprints",
                    4,
                    file + ": Generic dynamic SQL construction pattern detected."
            );
        }
    }

    private static void analyzeTechnicalIssues(
            String code, Path file) {

        if (code.contains("System.out.println")
                && code.contains("catch")) {

            technicalNotes.add(
                    file + ": Consider a logging framework instead of console output."
            );
        }

        if (code.contains(".toLowerCase()")
                && !code.contains("Locale.ROOT")) {

            technicalNotes.add(
                    file + ": Consider Locale.ROOT for consistent text normalization."
            );
        }
    }

    private static void addPoints(
            String categoryName,
            int points,
            String evidence) {

        Category category = categories.get(categoryName);

        int added = Math.min(
                points,
                MAX_CATEGORY_SCORE - category.score
        );

        if (added <= 0) {
            return;
        }

        category.score += added;
        category.evidence.add(evidence);
    }

    private static int calculateTotalScore() {

        int total = 0;

        for (Category category : categories.values()) {
            total += category.score;
        }

        return Math.min(total, MAX_SCORE);
    }

    private static int count(String text, String target) {

        int total = 0;
        int index = 0;

        while ((index = text.indexOf(target, index)) != -1) {
            total++;
            index += target.length();
        }

        return total;
    }

    private static void generateReport(
            Path report,
            int percentage,
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

        output.add(
                "**AI-like Pattern Score: " + percentage + "%**"
        );

        output.add("");

        output.add(
                "**Converted Score: " + scoreOutOfTen + "/10**"
        );

        output.add("");

        output.add(
                "**Likelihood of AI Generation: Not established**"
        );

        output.add("");

        output.add(
                "This is a heuristic assessment of coding patterns. "
                        + "It does not establish whether AI generated the code."
        );

        output.add("");

        output.add("## Overall Assessment");
        output.add("");

        if (percentage >= 70) {

            output.add(
                    "Several configured AI-associated coding patterns "
                            + "were detected. Manual review is recommended."
            );

        } else if (percentage >= 40) {

            output.add(
                    "Some configured coding patterns were detected. "
                            + "Review the findings and source code."
            );

        } else {

            output.add(
                    "Few configured AI-associated coding patterns "
                            + "were detected."
            );
        }

        output.add("");
        output.add("## Category Analysis");
        output.add("");

        for (Map.Entry<String, Category> entry : categories.entrySet()) {

            Category category = entry.getValue();

            output.add("### " + entry.getKey());
            output.add("");

            output.add(
                    "**Score: " + category.score + "/20**"
            );

            output.add("");

            if (category.evidence.isEmpty()) {

                output.add("- No configured indicators detected.");

            } else {

                for (String evidence : category.evidence) {
                    output.add("- " + evidence);
                }
            }

            output.add("");
        }

        output.add("## Technical Review Notes");
        output.add("");

        if (technicalNotes.isEmpty()) {

            output.add("- No configured technical review notes.");

        } else {

            for (String note : technicalNotes) {
                output.add("- " + note);
            }
        }

        output.add("");
        output.add("## Analyzed Files");
        output.add("");

        output.add(
                "**Total Java files analyzed:** " + totalJavaFiles
        );

        output.add("");

        for (String file : analyzedFiles) {
            output.add("- `" + file + "`");
        }

        output.add("");
        output.add("## Build Result");
        output.add("");

        output.add(
                "**Detection Threshold:** "
                        + DETECTION_THRESHOLD + "/100"
        );

        output.add("");

        if (detected) {

            output.add(
                    "**DETECTED — AI-like patterns found.**"
            );

            output.add("");
            output.add("**Build Status: FAILED**");

        } else {

            output.add(
                    "**NOT DETECTED — Score is below 10/100.**"
            );

            output.add("");
            output.add("**Build Status: PASSED**");
        }

        output.add("");
        output.add("---");

        output.add(
                "*A human review is recommended. Common coding practices, "
                        + "shared templates, project conventions, and manually "
                        + "written code can produce similar patterns.*"
        );

        Files.write(
                report,
                output,
                StandardCharsets.UTF_8
        );
    }

    private static class Category {

        private int score;

        private final List<String> evidence =
                new ArrayList<>();
    }
}
