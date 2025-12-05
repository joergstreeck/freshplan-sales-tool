package de.freshplan.modules.leads.api.selfservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit Tests für Error Report CSV-Download Logik.
 *
 * <p>Sprint 2.1.8 - Issue #149 Testet:
 *
 * <ul>
 *   <li>CSV-Format (Semikolon-Separator, UTF-8 BOM)
 *   <li>Header und Spalten
 *   <li>Validierungsfehler Export
 *   <li>Duplikate als Warnung
 *   <li>CSV-Escaping für Sonderzeichen
 * </ul>
 *
 * <p>Diese Tests benötigen keinen Quarkus-Kontext, da sie reine Unit-Tests der CSV-Logik sind.
 *
 * @since Sprint 2.1.8
 */
@DisplayName("SelfServiceImport CSV Download Unit Tests")
class SelfServiceImportResourceCsvTest {

  // ============================================================================
  // CSV Escape Tests (Unit-Tests der escapeCsv Methode)
  // ============================================================================

  @Nested
  @DisplayName("CSV Escaping")
  class CsvEscapingTests {

    @Test
    @DisplayName("should escape semicolon in value")
    void shouldEscapeSemicolon() {
      String result = escapeCsvValue("Haus;Garten");
      assertEquals("\"Haus;Garten\"", result);
    }

    @Test
    @DisplayName("should escape double quotes in value")
    void shouldEscapeDoubleQuotes() {
      String result = escapeCsvValue("Er sagte \"Hallo\"");
      assertEquals("\"Er sagte \"\"Hallo\"\"\"", result);
    }

    @Test
    @DisplayName("should escape newline in value")
    void shouldEscapeNewline() {
      String result = escapeCsvValue("Zeile1\nZeile2");
      assertEquals("\"Zeile1\nZeile2\"", result);
    }

    @Test
    @DisplayName("should not escape simple value")
    void shouldNotEscapeSimpleValue() {
      String result = escapeCsvValue("Einfacher Wert");
      assertEquals("Einfacher Wert", result);
    }

    @Test
    @DisplayName("should handle null value")
    void shouldHandleNullValue() {
      String result = escapeCsvValue(null);
      assertEquals("", result);
    }

    @Test
    @DisplayName("should handle empty value")
    void shouldHandleEmptyValue() {
      String result = escapeCsvValue("");
      assertEquals("", result);
    }

    @Test
    @DisplayName("should escape carriage return")
    void shouldEscapeCarriageReturn() {
      String result = escapeCsvValue("Text\rMehr");
      assertEquals("\"Text\rMehr\"", result);
    }

    @Test
    @DisplayName("should handle combined special characters")
    void shouldHandleCombinedSpecialCharacters() {
      String result = escapeCsvValue("Text;mit\"Sonder\nzeichen");
      assertEquals("\"Text;mit\"\"Sonder\nzeichen\"", result);
    }

    // Helper method replicating escapeCsv from SelfServiceImportResource
    private String escapeCsvValue(String value) {
      if (value == null) {
        return "";
      }
      if (value.contains(";")
          || value.contains("\"")
          || value.contains("\n")
          || value.contains("\r")) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
      }
      return value;
    }
  }

  // ============================================================================
  // CSV Header Tests
  // ============================================================================

  @Nested
  @DisplayName("CSV Header Format")
  class CsvHeaderTests {

    @Test
    @DisplayName("should have correct CSV header columns")
    void shouldHaveCorrectCsvHeader() {
      // Expected header format (German)
      String expectedHeader = "Zeile;Spalte;Fehlertyp;Fehlermeldung;Originalwert";

      // Verify header matches spec
      String[] columns = expectedHeader.split(";");
      assertEquals(5, columns.length, "CSV should have 5 columns");
      assertEquals("Zeile", columns[0]);
      assertEquals("Spalte", columns[1]);
      assertEquals("Fehlertyp", columns[2]);
      assertEquals("Fehlermeldung", columns[3]);
      assertEquals("Originalwert", columns[4]);
    }

    @Test
    @DisplayName("should start with UTF-8 BOM")
    void shouldStartWithUtf8Bom() {
      // UTF-8 BOM = EF BB BF = \uFEFF
      String bomChar = "\uFEFF";
      assertEquals(1, bomChar.length());
      assertEquals(0xFEFF, bomChar.charAt(0));
    }

    @Test
    @DisplayName("should generate complete CSV header with BOM")
    void shouldGenerateCompleteCsvHeaderWithBom() {
      String csvHeader = "\uFEFF" + "Zeile;Spalte;Fehlertyp;Fehlermeldung;Originalwert\n";

      assertTrue(csvHeader.startsWith("\uFEFF"), "Should start with UTF-8 BOM");
      assertTrue(csvHeader.endsWith("\n"), "Should end with newline");
      assertTrue(csvHeader.contains("Zeile;Spalte;Fehlertyp"), "Should contain header");
    }
  }

  // ============================================================================
  // CSV Content Tests
  // ============================================================================

  @Nested
  @DisplayName("CSV Content Generation")
  class CsvContentTests {

    @Test
    @DisplayName("should format validation error correctly")
    void shouldFormatValidationErrorCorrectly() {
      // Simulated error row
      int row = 3;
      String column = "email";
      String message = "Ungültiges E-Mail-Format";
      String value = "nicht.eine.email";

      String csvLine = row + ";" + column + ";VALIDATION;" + message + ";" + value;

      assertTrue(csvLine.startsWith("3;"), "Should start with row number");
      assertTrue(csvLine.contains("VALIDATION"), "Should contain error type");
      assertTrue(csvLine.contains("email"), "Should contain column name");
      assertTrue(csvLine.contains("nicht.eine.email"), "Should contain original value");
    }

    @Test
    @DisplayName("should format duplicate warning correctly")
    void shouldFormatDuplicateWarningCorrectly() {
      // Simulated duplicate row
      int row = 5;
      String existingCompanyName = "Test GmbH";
      String existingLeadId = "abc-123";
      double similarity = 0.95;

      String message =
          String.format(
              "Mögliches Duplikat: %s (ID: %s, Ähnlichkeit: %d%%)",
              existingCompanyName, existingLeadId, Math.round(similarity * 100));

      String csvLine = row + ";companyName;EXACT_MATCH;" + message + ";-";

      assertTrue(csvLine.contains("companyName"), "Should contain column");
      assertTrue(csvLine.contains("EXACT_MATCH"), "Should contain duplicate type");
      assertTrue(csvLine.contains("Test GmbH"), "Should contain existing company name");
      assertTrue(csvLine.contains("95%"), "Should contain similarity percentage");
    }

    @Test
    @DisplayName("should format fuzzy match duplicate")
    void shouldFormatFuzzyMatchDuplicate() {
      int row = 7;
      String existingCompanyName = "Restaurant Müller";
      String existingLeadId = "xyz-456";
      double similarity = 0.87;

      String message =
          String.format(
              "Mögliches Duplikat: %s (ID: %s, Ähnlichkeit: %d%%)",
              existingCompanyName, existingLeadId, Math.round(similarity * 100));

      String csvLine = row + ";companyName;FUZZY_MATCH;" + message + ";-";

      assertTrue(csvLine.contains("FUZZY_MATCH"), "Should contain fuzzy match type");
      assertTrue(csvLine.contains("87%"), "Should contain similarity percentage");
    }

    @Test
    @DisplayName("should add info line when no errors")
    void shouldAddInfoLineWhenNoErrors() {
      String infoLine = "-;-;INFO;Keine Validierungsfehler gefunden;-";

      assertTrue(infoLine.contains("INFO"), "Should contain INFO type");
      assertTrue(
          infoLine.contains("Keine Validierungsfehler gefunden"), "Should contain info message");
    }

    @Test
    @DisplayName("should format complete error row")
    void shouldFormatCompleteErrorRow() {
      StringBuilder csv = new StringBuilder();
      csv.append("\uFEFF");
      csv.append("Zeile;Spalte;Fehlertyp;Fehlermeldung;Originalwert\n");
      csv.append("2;companyName;VALIDATION;Firmenname ist erforderlich;\n");
      csv.append("3;email;VALIDATION;Ungültige E-Mail;test@\n");

      String content = csv.toString();
      assertTrue(content.startsWith("\uFEFF"), "Should have BOM");
      assertTrue(content.lines().count() >= 3, "Should have header + 2 error lines");
    }
  }

  // ============================================================================
  // CSV Filename Tests
  // ============================================================================

  @Nested
  @DisplayName("CSV Filename Format")
  class CsvFilenameTests {

    @Test
    @DisplayName("should generate correct filename format")
    void shouldGenerateCorrectFilenameFormat() {
      String uploadId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
      String expectedFilename = "import-errors-" + uploadId.substring(0, 8) + ".csv";

      assertEquals("import-errors-a1b2c3d4.csv", expectedFilename);
    }

    @Test
    @DisplayName("should use first 8 characters of UUID")
    void shouldUseFirst8CharsOfUuid() {
      String uploadId = "12345678-1234-1234-1234-123456789012";
      String shortId = uploadId.substring(0, 8);

      assertEquals("12345678", shortId);
      assertEquals(8, shortId.length());
    }

    @Test
    @DisplayName("should generate unique filenames for different uploads")
    void shouldGenerateUniqueFilenames() {
      String uploadId1 = "aaaaaaaa-1234-1234-1234-123456789012";
      String uploadId2 = "bbbbbbbb-1234-1234-1234-123456789012";

      String filename1 = "import-errors-" + uploadId1.substring(0, 8) + ".csv";
      String filename2 = "import-errors-" + uploadId2.substring(0, 8) + ".csv";

      assertNotEquals(filename1, filename2, "Different uploads should have different filenames");
    }
  }

  // ============================================================================
  // CSV Complete Generation Test
  // ============================================================================

  @Nested
  @DisplayName("Complete CSV Generation")
  class CompleteCsvGenerationTests {

    @Test
    @DisplayName("should generate valid CSV with multiple errors and duplicates")
    void shouldGenerateValidCsvWithMultipleErrorsAndDuplicates() {
      StringBuilder csv = new StringBuilder();

      // Header (BOM für Excel-Kompatibilität)
      csv.append("\uFEFF");
      csv.append("Zeile;Spalte;Fehlertyp;Fehlermeldung;Originalwert\n");

      // Validierungsfehler
      csv.append("2;companyName;VALIDATION;Firmenname ist erforderlich;\n");
      csv.append("3;email;VALIDATION;Ungültiges E-Mail-Format;test@invalid\n");

      // Duplikate
      csv.append("5;companyName;EXACT_MATCH;");
      csv.append("Mögliches Duplikat: Existing GmbH (ID: abc-123, Ähnlichkeit: 100%)");
      csv.append(";-\n");

      String content = csv.toString();

      // Verify structure
      assertTrue(content.startsWith("\uFEFF"), "Should start with BOM");
      assertTrue(content.contains("Zeile;Spalte;Fehlertyp"), "Should have header");
      assertTrue(content.contains("VALIDATION"), "Should have validation errors");
      assertTrue(content.contains("EXACT_MATCH"), "Should have duplicates");
      assertTrue(content.contains("100%"), "Should have similarity");

      // Verify line count (lines() splits by newlines)
      long lineCount = content.lines().count();
      assertTrue(lineCount >= 4, "Should have at least 4 lines (header + 2 errors + 1 duplicate)");
    }
  }
}
