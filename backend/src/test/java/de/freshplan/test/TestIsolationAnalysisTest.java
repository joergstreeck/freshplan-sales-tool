package de.freshplan.test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Analysiert ALLE Test-Dateien auf Test-Isolation-Probleme
 *
 * <p>Devise: Sicherheit geht vor Schnelligkeit!
 */
@QuarkusTest
@Tag("quarantine")
public class TestIsolationAnalysisTest {

  @Inject EntityManager em;

  @Test
  public void analyzeAllTestsForIsolationProblems() throws IOException {
    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== TEST ISOLATION ANALYSIS - SICHERHEIT GEHT VOR SCHNELLIGKEIT ===");
    System.out.println("=".repeat(100));

    Path testDir = Paths.get("src/test/java/de/freshplan");

    // Sammle alle Test-Dateien
    List<TestFileAnalysis> analyses = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(testDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().endsWith("Test.java"))
          .filter(p -> !p.toString().contains("TestIsolationAnalysis"))
          .forEach(
              path -> {
                try {
                  TestFileAnalysis analysis = analyzeTestFile(path);
                  if (analysis.hasProblems()) {
                    analyses.add(analysis);
                  }
                } catch (IOException e) {
                  System.err.println("Error analyzing: " + path);
                }
              });
    }

    // Sortiere nach Schweregrad
    analyses.sort((a, b) -> Integer.compare(b.getSeverity(), a.getSeverity()));

    // Report
    System.out.println("\n=== GEFUNDENE PROBLEME ===");
    System.out.println("Anzahl problematischer Tests: " + analyses.size());

    for (TestFileAnalysis analysis : analyses) {
      System.out.println("\n" + "-".repeat(80));
      System.out.println("FILE: " + analysis.fileName);
      System.out.println(
          "SEVERITY: "
              + getSeverityLabel(analysis.getSeverity())
              + " ("
              + analysis.getSeverity()
              + ")");
      System.out.println("PROBLEMS:");

      if (analysis.hasBeforeEachWithPersist) {
        System.out.println("  ❌ @BeforeEach mit persist() - fügt bei JEDEM Test Daten hinzu!");
      }
      if (analysis.hasTransactionalWithoutRollback) {
        System.out.println(
            "  ❌ @Transactional ohne @TestTransaction - kein automatischer Rollback!");
      }
      if (analysis.hasPersistWithoutCleanup) {
        System.out.println("  ❌ persist() ohne @AfterEach Cleanup!");
      }
      if (analysis.persistCallsCount > 0) {
        System.out.println("  ⚠️ " + analysis.persistCallsCount + " persist() Aufrufe gefunden");
      }
      if (!analysis.hasTestTransaction) {
        System.out.println("  ⚠️ Kein @TestTransaction gefunden");
      }
      if (!analysis.hasAfterEach) {
        System.out.println("  ⚠️ Kein @AfterEach Cleanup gefunden");
      }

      if (!analysis.persistPatterns.isEmpty()) {
        System.out.println("  PERSIST PATTERNS:");
        for (String pattern : analysis.persistPatterns) {
          System.out.println("    - " + pattern);
        }
      }
    }

    // Zusammenfassung
    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== ZUSAMMENFASSUNG ===");

    int critical = (int) analyses.stream().filter(a -> a.getSeverity() >= 8).count();
    int high =
        (int) analyses.stream().filter(a -> a.getSeverity() >= 5 && a.getSeverity() < 8).count();
    int medium =
        (int) analyses.stream().filter(a -> a.getSeverity() >= 3 && a.getSeverity() < 5).count();
    int low = (int) analyses.stream().filter(a -> a.getSeverity() < 3).count();

    System.out.println("KRITISCH (8-10): " + critical + " Tests");
    System.out.println("HOCH (5-7): " + high + " Tests");
    System.out.println("MITTEL (3-4): " + medium + " Tests");
    System.out.println("NIEDRIG (0-2): " + low + " Tests");

    // Empfehlungen
    System.out.println("\n=== EMPFOHLENE MASSNAHMEN ===");
    System.out.println("1. SOFORT: Alle Tests mit Severity >= 8 fixen");
    System.out.println("2. @TestTransaction statt @Transactional verwenden");
    System.out.println("3. @AfterEach mit Cleanup implementieren");
    System.out.println("4. TestDataBuilder Pattern mit automatischem Cleanup");
    System.out.println("5. CI/CD Check für Datenbank-Wachstum implementieren");

    System.out.println("\n" + "=".repeat(100));
    System.out.println("=== ANALYSE ABGESCHLOSSEN ===");
    System.out.println("=".repeat(100));
  }

  private TestFileAnalysis analyzeTestFile(Path path) throws IOException {
    TestFileAnalysis analysis = new TestFileAnalysis();
    analysis.fileName = path.getFileName().toString();
    analysis.fullPath = path.toString();

    String content = Files.readString(path);
    String[] lines = content.split("\n");

    // Patterns für Probleme
    Pattern persistPattern = Pattern.compile("\\.(persist|persistAndFlush)\\s*\\(");
    Pattern beforeEachPattern = Pattern.compile("@BeforeEach");
    Pattern afterEachPattern = Pattern.compile("@AfterEach");
    Pattern transactionalPattern = Pattern.compile("@Transactional");
    Pattern testTransactionPattern = Pattern.compile("@TestTransaction");
    Pattern customerPattern = Pattern.compile("new\\s+Customer\\s*\\(\\)");
    Pattern setCompanyNamePattern =
        Pattern.compile("\\.setCompanyName\\s*\\([\"']([^\"']+)[\"']\\)");

    boolean inBeforeEach = false;
    boolean inAfterEach = false;
    int bracketDepth = 0;

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];

      // Check for annotations
      if (beforeEachPattern.matcher(line).find()) {
        analysis.hasBeforeEach = true;
        inBeforeEach = true;
        bracketDepth = 0;
      }

      if (afterEachPattern.matcher(line).find()) {
        analysis.hasAfterEach = true;
        inAfterEach = true;
        inBeforeEach = false;
        bracketDepth = 0;
      }

      if (transactionalPattern.matcher(line).find()) {
        analysis.hasTransactional = true;
      }

      if (testTransactionPattern.matcher(line).find()) {
        analysis.hasTestTransaction = true;
      }

      // Track bracket depth
      for (char c : line.toCharArray()) {
        if (c == '{') bracketDepth++;
        if (c == '}') {
          bracketDepth--;
          if (bracketDepth == 0) {
            inBeforeEach = false;
            inAfterEach = false;
          }
        }
      }

      // Check for persist calls
      Matcher persistMatcher = persistPattern.matcher(line);
      if (persistMatcher.find()) {
        analysis.persistCallsCount++;

        if (inBeforeEach) {
          analysis.hasBeforeEachWithPersist = true;
          analysis.persistPatterns.add("Line " + (i + 1) + " in @BeforeEach: " + line.trim());
        } else if (!inAfterEach) {
          analysis.hasPersistWithoutCleanup = true;
          analysis.persistPatterns.add("Line " + (i + 1) + ": " + line.trim());
        }
      }

      // Check for test data patterns
      Matcher companyNameMatcher = setCompanyNamePattern.matcher(line);
      if (companyNameMatcher.find()) {
        String companyName = companyNameMatcher.group(1);
        if (companyName.contains("Test")
            || companyName.contains("Search")
            || companyName.contains("Export")
            || companyName.contains("Performance")) {
          analysis.testDataPatterns.add(companyName);
        }
      }
    }

    // Bewerte Probleme
    if (analysis.hasTransactional && !analysis.hasTestTransaction) {
      analysis.hasTransactionalWithoutRollback = true;
    }

    return analysis;
  }

  private String getSeverityLabel(int severity) {
    if (severity >= 8) return "🔴 KRITISCH";
    if (severity >= 5) return "🟠 HOCH";
    if (severity >= 3) return "🟡 MITTEL";
    return "🟢 NIEDRIG";
  }

  private static class TestFileAnalysis {
    String fileName;
    String fullPath;
    boolean hasBeforeEach = false;
    boolean hasAfterEach = false;
    boolean hasTransactional = false;
    boolean hasTestTransaction = false;
    boolean hasBeforeEachWithPersist = false;
    boolean hasTransactionalWithoutRollback = false;
    boolean hasPersistWithoutCleanup = false;
    int persistCallsCount = 0;
    List<String> persistPatterns = new ArrayList<>();
    List<String> testDataPatterns = new ArrayList<>();

    boolean hasProblems() {
      return hasBeforeEachWithPersist
          || hasTransactionalWithoutRollback
          || hasPersistWithoutCleanup
          || persistCallsCount > 0;
    }

    int getSeverity() {
      int severity = 0;
      if (hasBeforeEachWithPersist) severity += 5; // Sehr schlecht - jeder Test fügt Daten hinzu
      if (hasTransactionalWithoutRollback) severity += 3; // Schlecht - kein Rollback
      if (hasPersistWithoutCleanup) severity += 2; // Problematisch
      if (persistCallsCount > 5) severity += 2; // Viele Daten
      if (!hasTestTransaction && hasTransactional) severity += 2;
      return severity;
    }
  }
}
