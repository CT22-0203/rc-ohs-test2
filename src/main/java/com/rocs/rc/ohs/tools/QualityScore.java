/**
 * Parses PMD, Checkstyle, and CPD XML reports.
 * Computes a 0-100 quality score and exits 1 if below threshold.
 */
package com.rocs.rc.ohs.tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;

/**
 * Computes a quality score from static analysis reports.
 */
public class QualityScore {

    /** Count of error-severity findings. */
    private int errors = 0;

    /** Count of warning-severity findings. */
    private int warnings = 0;

    /** Count of info-severity findings. */
    private int infos = 0;

    /** Count of duplication blocks. */
    private int duplications = 0;

    /**
     * Entry point.
     *
     * @param args report file paths and --fail-below threshold
     * @throws Exception on parse failure
     */
    public static void main(String[] args) throws Exception {
        QualityScore scorer = new QualityScore();
        int failBelow = 70;

        for (int i = 0; i < args.length; i++) {
            if ("--fail-below".equals(args[i])
                    && i + 1 < args.length) {
                failBelow = Integer.parseInt(args[i + 1]);
                i++;
            } else {
                File f = new File(args[i]);
                if (!f.exists()) {
                    System.out.println(
                            "WARN: report not found: " + f);
                    continue;
                }
                String name = f.getName().toLowerCase();
                if (name.startsWith("pmd")) {
                    scorer.parsePMD(f);
                } else if (name.startsWith("checkstyle")) {
                    scorer.parseCheckstyle(f);
                } else if (name.startsWith("spotbugs")) {
                    scorer.parseSpotBugs(f);
                } else if (name.startsWith("cpd")) {
                    scorer.parseCPD(f);
                }
            }
        }

        int score = scorer.computeScore();
        System.out.println("========================================");
        System.out.println("  Quality Score: " + score + "/100");
        System.out.println("  Errors: " + scorer.errors);
        System.out.println("  Warnings: " + scorer.warnings);
        System.out.println("  Infos: " + scorer.infos);
        System.out.println(
                "  Duplications: " + scorer.duplications);
        System.out.println("  Threshold: " + failBelow);
        System.out.println("========================================");

        if (score < failBelow) {
            System.out.println(
                    "FAILED: score " + score + " < " + failBelow);
            System.exit(1);
        } else {
            System.out.println("PASSED");
        }
    }

    /**
     * Computes the score from accumulated counts.
     *
     * @return score between 0 and 100
     */
    private int computeScore() {
        int penalty = (errors * 10) + (warnings * 2)
                + (duplications * 5);
        return Math.max(0, 100 - penalty);
    }

    /**
     * Parses an XML file into a DOM Document.
     *
     * @param f the XML file to parse
     * @return the parsed Document
     * @throws Exception on parse error
     */
    private Document parse(File f) throws Exception {
        DocumentBuilder db =
                DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
        return db.parse(f);
    }

    /**
     * Parses PMD XML report.
     *
     * @param f the PMD report file
     * @throws Exception on parse error
     */
    private void parsePMD(File f) throws Exception {
        Document doc = parse(f);
        NodeList violations =
                doc.getElementsByTagName("violation");
        for (int i = 0; i < violations.getLength(); i++) {
            Element v = (Element) violations.item(i);
            int priority =
                    Integer.parseInt(v.getAttribute("priority"));
            if (priority <= 2) {
                errors++;
            } else if (priority <= 3) {
                warnings++;
            } else {
                infos++;
            }
        }
    }

    /**
     * Parses Checkstyle XML report.
     *
     * @param f the Checkstyle report file
     * @throws Exception on parse error
     */
    private void parseCheckstyle(File f) throws Exception {
        Document doc = parse(f);
        NodeList errorNodes =
                doc.getElementsByTagName("error");
        for (int i = 0; i < errorNodes.getLength(); i++) {
            Element e = (Element) errorNodes.item(i);
            String sev = e.getAttribute("severity");
            if ("error".equals(sev)) {
                errors++;
            } else if ("warning".equals(sev)) {
                warnings++;
            } else {
                infos++;
            }
        }
    }

    /**
     * Parses SpotBugs XML report.
     *
     * @param f the SpotBugs report file
     * @throws Exception on parse error
     */
    private void parseSpotBugs(File f) throws Exception {
        Document doc = parse(f);
        NodeList bugs =
                doc.getElementsByTagName("BugInstance");
        for (int i = 0; i < bugs.getLength(); i++) {
            Element b = (Element) bugs.item(i);
            String p = b.getAttribute("priority");
            if (p.isEmpty()) {
                continue;
            }
            int priority = Integer.parseInt(p);
            if (priority == 1) {
                errors++;
            } else if (priority == 2) {
                warnings++;
            } else {
                infos++;
            }
        }
    }

    /**
     * Parses CPD XML report.
     *
     * @param f the CPD report file
     * @throws Exception on parse error
     */
    private void parseCPD(File f) throws Exception {
        Document doc = parse(f);
        NodeList dupBlocks =
                doc.getElementsByTagName("duplication");
        duplications = dupBlocks.getLength();
    }
}