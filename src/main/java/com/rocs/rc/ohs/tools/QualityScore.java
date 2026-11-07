package com.rocs.rc.ohs.tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;

public class QualityScore {

    private int errors = 0;
    private int warnings = 0;
    private int infos = 0;
    private int duplications = 0;

    public static void main(String[] args) throws Exception {
        QualityScore scorer = new QualityScore();
        int failBelow = 70;

        for (int i = 0; i < args.length; i++) {
            if ("--fail-below".equals(args[i]) && i + 1 < args.length) {
                failBelow = Integer.parseInt(args[i + 1]);
                i++;
            } else {
                File f = new File(args[i]);
                if (!f.exists()) {
                    System.out.println("WARN: report not found: " + f);
                    continue;
                }
                String name = f.getName().toLowerCase();
                if (name.startsWith("pmd")) scorer.parsePMD(f);
                else if (name.startsWith("checkstyle")) scorer.parseCheckstyle(f);
                else if (name.startsWith("spotbugs")) scorer.parseSpotBugs(f);
                else if (name.startsWith("cpd")) scorer.parseCPD(f);
            }
        }

        int score = scorer.computeScore();
        System.out.println("========================================");
        System.out.println("  Quality Score: " + score + "/100");
        System.out.println("  Errors: " + scorer.errors);
        System.out.println("  Warnings: " + scorer.warnings);
        System.out.println("  Infos: " + scorer.infos);
        System.out.println("  Duplications: " + scorer.duplications);
        System.out.println("  Threshold: " + failBelow);
        System.out.println("========================================");

        if (score < failBelow) {
            System.out.println("FAILED: score " + score + " < " + failBelow);
            System.exit(1);
        } else {
            System.out.println("PASSED");
        }
    }

    private int computeScore() {
        int penalty = (errors * 10) + (warnings * 2) + (duplications * 5);
        return Math.max(0, 100 - penalty);
    }

    private Document parse(File f) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return db.parse(f);
    }

    private void parsePMD(File f) throws Exception {
        Document doc = parse(f);
        NodeList violations = doc.getElementsByTagName("violation");
        for (int i = 0; i < violations.getLength(); i++) {
            Element v = (Element) violations.item(i);
            int priority = Integer.parseInt(v.getAttribute("priority"));
            if (priority <= 2) errors++;
            else if (priority <= 3) warnings++;
            else infos++;
        }
    }

    private void parseCheckstyle(File f) throws Exception {
        Document doc = parse(f);
        NodeList errorNodes = doc.getElementsByTagName("error");
        for (int i = 0; i < errorNodes.getLength(); i++) {
            Element e = (Element) errorNodes.item(i);
            String sev = e.getAttribute("severity");
            if ("error".equals(sev)) errors++;
            else if ("warning".equals(sev)) warnings++;
            else infos++;
        }
    }

    private void parseSpotBugs(File f) throws Exception {
        Document doc = parse(f);
        NodeList bugs = doc.getElementsByTagName("BugInstance");
        for (int i = 0; i < bugs.getLength(); i++) {
            Element b = (Element) bugs.item(i);
            String p = b.getAttribute("priority");
            if (p.isEmpty()) continue;
            int priority = Integer.parseInt(p);
            if (priority == 1) errors++;
            else if (priority == 2) warnings++;
            else infos++;
        }
    }

    private void parseCPD(File f) throws Exception {
        Document doc = parse(f);
        NodeList dupBlocks = doc.getElementsByTagName("duplication");
        duplications = dupBlocks.getLength();
    }
}