
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

    private static final Pattern IDENTIFIER =
            Pattern.compile("\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b");

    private static final Pattern TEST_METHOD =
            Pattern.compile("@Test\\s*(?:\\([^)]*\\))?\\s*(?:public\\s+)?"
                    + "(?:void|[\\w<>\\[\\]]+)\\s+(\\w+)\\s*\\(");

    private static final Pattern METHOD =
            Pattern.compile("(?:public|protected|private)\\s+"
                    + "(?:static\\s+)?(?:final\\s+)?[\\w<>\\[\\], ?]+"
                    + "\\s+\\w+\\s*\\([^;]*\\)\\s*\\{");

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

        if (args.length != 3) {
            System.err.println(
                    "Usage: AiPatternDetector <source-dir> <report-file> <threshold>"
            );
            System.exit(1);
            return;
        }

        Path sourceDir = Path.of(args[0]);
        Path reportFile = Path.of(args[1]);

        int threshold;

        try {
            threshold = Integer.parseInt(args[2]);

            if (threshold < 0 || threshold > 10) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be between 0 and 10.");
            System.exit(1);
            return;
        }

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
            int scoreOutOfTen = percentage / 10;

            generateReport(reportFile, percentage, scoreOutOfTen, threshold);

            System.out.println("Java files analyzed: " + totalJavaFiles);
            System.out.println("AI-like Pattern Score: "
                    + percentage + "/100");
            System.out.println("Converted Score: "
                    + scoreOutOfTen + "/10");

            if (scoreOutOfTen >= threshold) {
                System.err.println(
                        "Configured detection threshold reached. Build failed."
                );
                System.exit(1);
            }

            System.out.println("AI Pattern Detection passed.");

        } catch (IOException e) {
            System.err.println("AI Detection error: " + e.getMessage());
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

        int totalIdentifiers = 0;
        int genericIdentifiers = 0;

        while (matcher.find()) {
            String identifier = matcher.group();

            if (identifier.length() < 3) {
                continue;
            }

            totalIdentifiers++;

            if (GENERIC_NAMES.contains(identifier.toLowerCase())) {
                genericIdentifiers++;
            }
        }

        if (genericIdentifiers >= 8) {
            addPoints(
                    "Naming Style", 8,
                    file + ": Frequent use of generic identifiers."
            );
        } else if (genericIdentifiers >= 4) {
            addPoints(
                    "Naming Style", 5,
                    file + ": Several generic identifiers were found."
            );
        }

        if (totalIdentifiers > 0) {
            double ratio = (double) genericIdentifiers / totalIdentifiers;

            if (ratio >= 0.15 && genericIdentifiers >= 3) {
                addPoints(
                        "Naming Style", 7,
                        file + ": Generic identifiers form a noticeable portion of the names."
                );
            }
        }

        if (Pattern.compile("\\b(test1|test2|method1|method2|temp1|temp2)\\b",
                Pattern.CASE_INSENSITIVE).matcher(code).find()) {

            addPoints(
                    "Naming Style", 5,
                    file + ": Numbered or placeholder-style identifiers detected."
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
                    "Comment Style", 10,
                    file + ": Multiple generic documentation phrases detected."
            );
        } else if (templateCount >= 2) {
            addPoints(
                    "Comment Style", 6,
                    file + ": Repeated template-like comment phrases detected."
            );
        }

        int testCount = count(code, "@Test");

        if (testCount >= 8 && javadocCount >= 8) {
            addPoints(
                    "Comment Style", 6,
                    file + ": Extensive repeated Javadoc documentation in test methods."
            );
        }

        if (count(lower, "this method") >= 4) {
            addPoints(
                    "Comment Style", 4,
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
                    "Code Structure", 5,
                    file + ": Large number of test methods in one class."
            );
        }

        if (testCount >= 8 && assertionCount >= 12) {
            addPoints(
                    "Code Structure", 5,
                    file + ": Repeated test and assertion structure."
            );
        }

        if (testCount >= 8 && mockCount >= 5 && verifyCount >= 5) {
            addPoints(
                    "Code Structure", 7,
                    file + ": Repetitive mocking and verification structure."
            );
        }

        if (count(code, "@BeforeEach") > 1
                || count(code, "@AfterEach") > 1) {

            addPoints(
                    "Code Structure", 3,
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
                    .filter(name -> name.toLowerCase().startsWith("filter"))
                    .count();

            if (filterMethods >= 6) {
                addPoints(
                        "Code Structure", 5,
                        file + ": Repeated test method naming and structure."
                );
            }
        }
    }

    private static void analyzeAiTraits(String code, Path file) {

        if (code.contains("Generated by AI")
                || code.contains("AI-generated")) {

            addPoints(
                    "Typical AI Traits", 12,
                    file + ": Explicit AI-generation marker found."
            );
        }

        if (code.contains("TODO: implement")) {
            addPoints(
                    "Typical AI Traits", 8,
                    file + ": Generated-looking implementation placeholder."
            );
        }

        if (code.contains("@ExtendWith(MockitoExtension.class)")
                && code.contains("@MockitoSettings")
                && code.contains("Strictness.LENIENT")
                && code.contains("ArgumentCaptor")
                && code.contains("mockStatic(")) {

            addPoints(
                    "Typical AI Traits", 6,
                    file + ": Multiple advanced Mockito patterns combined."
            );
        }

        if (count(code, "UnsupportedOperationException") >= 2) {
            addPoints(
                    "Typical AI Traits", 5,
                    file + ": Multiple unsupported-operation placeholders."
            );
        }

        if (Pattern.compile("catch\\s*\\([^)]*\\)\\s*\\{\\s*\\}")
                .matcher(code).find()) {

            addPoints(
                    "Typical AI Traits", 5,
                    file + ": Empty exception-handling block."
            );
        }
    }

    private static void analyzeBusinessLogic(String code, Path file) {

        if (code.contains("catch (SQLException")
                && (code.contains("return new ArrayList")
                || code.contains("return Collections.emptyList"))) {

            addPoints(
                    "Business Logic Footprints", 8,
                    file + ": SQL errors may be hidden by returning an empty collection."
            );

            technicalNotes.add(
                    file + ": Review whether database failures should be propagated or logged."
            );
        }

        if (code.contains("UnsupportedOperationException")
                || code.contains("TODO: implement")) {

            addPoints(
                    "Business Logic Footprints", 8,
                    file + ": Placeholder implementation may indicate incomplete business logic."
            );
        }

        if (Pattern.compile("return\\s+null\\s*;").matcher(code).find()) {
            addPoints(
                    "Business Logic Footprints", 4,
                    file + ": Method returns null; review its business behavior."
            );
        }

        if (code.contains("where 1 = 1")
                && code.contains("StringBuilder")
                && code.contains(".append(")) {

            addPoints(
                    "Business Logic Footprints", 4,
                    file + ": Generic dynamic SQL construction pattern detected."
            );
        }
    }

    private static void analyzeTechnicalIssues(String code, Path file) {

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
            String categoryName, int points, String evidence) {

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
            int threshold) throws IOException {

        if (report.getParent() != null) {
            Files.createDirectories(report.getParent());
        }

        List<String> output = new ArrayList<>();

        output.add("# AI Code Pattern Detection Report");
        output.add("");
        output.add("## Detection Result");
        output.add("");
        output.add("**AI-like Pattern Score: " + percentage + "%**");
        output.add("");
        output.add("**Converted Score: " + scoreOutOfTen + "/10**");
        output.add("");
        output.add("**Likelihood of AI Generation: Not established**");
        output.add("");
        output.add("This is a heuristic assessment of coding patterns. "
                + "It does not establish whether AI generated the code.");
        output.add("");

        output.add("## Overall Assessment");
        output.add("");

        if (percentage >= 70) {
            output.add("Several configured AI-associated coding patterns "
                    + "were detected. Manual review is recommended.");
        } else if (percentage >= 40) {
            output.add("Some configured coding patterns were detected. "
                    + "Review the findings and source code.");
        } else {
            output.add("Few configured AI-associated coding patterns "
                    + "were detected.");
        }

        output.add("");
        output.add("## Category Analysis");
        output.add("");

        for (Map.Entry<String, Category> entry : categories.entrySet()) {

            Category category = entry.getValue();

            output.add("### " + entry.getKey());
            output.add("");
            output.add("**Score: " + category.score + "/20**");
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
        output.add("**Total Java files analyzed:** " + totalJavaFiles);
        output.add("");

        for (String file : analyzedFiles) {
            output.add("- `" + file + "`");
        }

        output.add("");
        output.add("## Build Result");
        output.add("");
        output.add("**Configured failure threshold:** " + threshold + "/10");
        output.add("");

        if (scoreOutOfTen >= threshold) {
            output.add("**FAILED — Configured threshold reached.**");
        } else {
            output.add("**PASSED — Score is below the configured threshold.**");
        }

        output.add("");
        output.add("---");
        output.add("*A human review is recommended. Common coding practices, "
                + "shared templates, project conventions, and manually written "
                + "code can produce similar patterns.*");

        Files.write(report, output, StandardCharsets.UTF_8);
    }

    private static class Category {

        private int score;
        private final List<String> evidence = new ArrayList<>();
    }
}
