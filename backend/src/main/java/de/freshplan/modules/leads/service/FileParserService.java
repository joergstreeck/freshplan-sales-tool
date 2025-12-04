package de.freshplan.modules.leads.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jboss.logging.Logger;

/**
 * File Parser Service - Sprint 2.1.8 Phase 2
 *
 * <p>Parst CSV und Excel-Dateien für Self-Service Lead-Import:
 *
 * <ul>
 *   <li>CSV (UTF-8, Windows-1252, ISO-8859-1)
 *   <li>XLSX (Excel 2007+)
 *   <li>Auto-Detection von Spalten-Mapping
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class FileParserService {

  private static final Logger LOG = Logger.getLogger(FileParserService.class);

  /** Maximale Dateigröße: 5 MB */
  public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

  /** Maximale Zeilen (Admin: 1000) */
  public static final int MAX_ROWS = 1000;

  /** Unterstützte Datei-Endungen */
  public static final Set<String> SUPPORTED_EXTENSIONS = Set.of("csv", "xlsx");

  // ============================================================================
  // Auto-Detection Mapping (DE/EN → Lead-Feld)
  // ============================================================================

  private static final Map<String, String> COLUMN_MAPPING = new LinkedHashMap<>();

  static {
    // Firmenname
    COLUMN_MAPPING.put("firma", "companyName");
    COLUMN_MAPPING.put("firmenname", "companyName");
    COLUMN_MAPPING.put("unternehmen", "companyName");
    COLUMN_MAPPING.put("company", "companyName");
    COLUMN_MAPPING.put("companyname", "companyName");
    COLUMN_MAPPING.put("company name", "companyName");

    // E-Mail
    COLUMN_MAPPING.put("e-mail", "email");
    COLUMN_MAPPING.put("email", "email");
    COLUMN_MAPPING.put("mail", "email");
    COLUMN_MAPPING.put("e mail", "email");

    // Telefon
    COLUMN_MAPPING.put("telefon", "phone");
    COLUMN_MAPPING.put("tel", "phone");
    COLUMN_MAPPING.put("fon", "phone");
    COLUMN_MAPPING.put("phone", "phone");
    COLUMN_MAPPING.put("telefonnummer", "phone");

    // PLZ
    COLUMN_MAPPING.put("plz", "postalCode");
    COLUMN_MAPPING.put("postleitzahl", "postalCode");
    COLUMN_MAPPING.put("postalcode", "postalCode");
    COLUMN_MAPPING.put("postal code", "postalCode");
    COLUMN_MAPPING.put("zip", "postalCode");
    COLUMN_MAPPING.put("zipcode", "postalCode");

    // Stadt
    COLUMN_MAPPING.put("ort", "city");
    COLUMN_MAPPING.put("stadt", "city");
    COLUMN_MAPPING.put("city", "city");
    COLUMN_MAPPING.put("town", "city");

    // Straße
    COLUMN_MAPPING.put("straße", "street");
    COLUMN_MAPPING.put("strasse", "street");
    COLUMN_MAPPING.put("adresse", "street");
    COLUMN_MAPPING.put("street", "street");
    COLUMN_MAPPING.put("address", "street");

    // Branche
    COLUMN_MAPPING.put("branche", "businessType");
    COLUMN_MAPPING.put("industry", "businessType");
    COLUMN_MAPPING.put("geschäftsart", "businessType");

    // Quelle
    COLUMN_MAPPING.put("quelle", "source");
    COLUMN_MAPPING.put("herkunft", "source");
    COLUMN_MAPPING.put("source", "source");
    COLUMN_MAPPING.put("origin", "source");

    // Ansprechpartner
    COLUMN_MAPPING.put("ansprechpartner", "contactPerson");
    COLUMN_MAPPING.put("kontakt", "contactPerson");
    COLUMN_MAPPING.put("contact", "contactPerson");
    COLUMN_MAPPING.put("contactperson", "contactPerson");
    COLUMN_MAPPING.put("contact person", "contactPerson");

    // Position
    COLUMN_MAPPING.put("position", "contactPosition");
    COLUMN_MAPPING.put("funktion", "contactPosition");
    COLUMN_MAPPING.put("title", "contactPosition");
    COLUMN_MAPPING.put("jobtitle", "contactPosition");

    // Notizen
    COLUMN_MAPPING.put("notiz", "notes");
    COLUMN_MAPPING.put("notizen", "notes");
    COLUMN_MAPPING.put("bemerkung", "notes");
    COLUMN_MAPPING.put("notes", "notes");
    COLUMN_MAPPING.put("comments", "notes");
    COLUMN_MAPPING.put("kommentar", "notes");

    // Website
    COLUMN_MAPPING.put("website", "website");
    COLUMN_MAPPING.put("webseite", "website");
    COLUMN_MAPPING.put("homepage", "website");
    COLUMN_MAPPING.put("url", "website");
  }

  // ============================================================================
  // Public API
  // ============================================================================

  /**
   * Parst eine Datei und gibt die Spalten-Header zurück.
   *
   * @param inputStream Datei-InputStream
   * @param fileName Original-Dateiname
   * @return ParseResult mit Spalten und Vorschau-Daten
   */
  public ParseResult parseFile(InputStream inputStream, String fileName, long fileSize)
      throws FileParseException {
    LOG.infof("Parsing file: %s (size: %d bytes)", fileName, fileSize);

    // 1. Validierung
    validateFile(fileName, fileSize);

    // 2. Format erkennen und parsen
    String extension = getFileExtension(fileName).toLowerCase();

    try {
      return switch (extension) {
        case "csv" -> parseCsv(inputStream);
        case "xlsx" -> parseExcel(inputStream);
        default -> throw new FileParseException("Nicht unterstütztes Format: " + extension);
      };
    } catch (FileParseException e) {
      throw e;
    } catch (Exception e) {
      LOG.errorf(e, "Fehler beim Parsen der Datei: %s", fileName);
      throw new FileParseException("Datei konnte nicht gelesen werden: " + e.getMessage());
    }
  }

  /**
   * Auto-Erkennung von Spalten-Mapping.
   *
   * @param columns Spalten-Header aus der Datei
   * @return Map von Datei-Spalte → Lead-Feld
   */
  public Map<String, String> autoDetectMapping(List<String> columns) {
    Map<String, String> mapping = new LinkedHashMap<>();

    for (String column : columns) {
      String normalized = column.toLowerCase().trim();
      String leadField = COLUMN_MAPPING.get(normalized);
      if (leadField != null) {
        mapping.put(column, leadField);
      }
    }

    LOG.infof("Auto-detected %d of %d columns", mapping.size(), columns.size());
    return mapping;
  }

  /**
   * Gibt die Liste der verfügbaren Lead-Felder zurück.
   *
   * @return Liste der Lead-Felder mit Beschreibung
   */
  public List<LeadFieldInfo> getLeadFields() {
    return List.of(
        new LeadFieldInfo("companyName", "Firmenname", true),
        new LeadFieldInfo("email", "E-Mail", false),
        new LeadFieldInfo("phone", "Telefon", false),
        new LeadFieldInfo("city", "Stadt", false),
        new LeadFieldInfo("postalCode", "PLZ", false),
        new LeadFieldInfo("street", "Straße", false),
        new LeadFieldInfo("businessType", "Branche", false),
        new LeadFieldInfo("source", "Quelle", false),
        new LeadFieldInfo("contactPerson", "Ansprechpartner", false),
        new LeadFieldInfo("contactPosition", "Position", false),
        new LeadFieldInfo("notes", "Notizen", false),
        new LeadFieldInfo("website", "Website", false));
  }

  // ============================================================================
  // CSV Parser
  // ============================================================================

  private ParseResult parseCsv(InputStream inputStream) throws Exception {
    // Charset-Detection: Versuche UTF-8, dann Windows-1252
    byte[] bytes = inputStream.readAllBytes();
    Charset charset = detectCharset(bytes);

    LOG.infof("CSV charset detected: %s", charset.name());

    // CSV parsen
    CSVParser parser =
        new CSVParserBuilder().withSeparator(detectSeparator(bytes, charset)).build();

    try (CSVReader reader =
        new CSVReaderBuilder(new InputStreamReader(new ByteArrayInputStream(bytes), charset))
            .withCSVParser(parser)
            .build()) {

      List<String[]> allRows = reader.readAll();

      if (allRows.isEmpty()) {
        throw new FileParseException("CSV-Datei ist leer");
      }

      // Header = erste Zeile
      String[] headerRow = allRows.get(0);
      List<String> columns = Arrays.asList(headerRow);

      // Daten = Rest (max MAX_ROWS)
      List<Map<String, String>> rows = new ArrayList<>();
      int maxRows = Math.min(allRows.size() - 1, MAX_ROWS);

      for (int i = 1; i <= maxRows; i++) {
        String[] dataRow = allRows.get(i);
        Map<String, String> row = new LinkedHashMap<>();
        for (int j = 0; j < columns.size() && j < dataRow.length; j++) {
          row.put(columns.get(j), dataRow[j] != null ? dataRow[j].trim() : "");
        }
        rows.add(row);
      }

      // Auto-Mapping
      Map<String, String> suggestedMapping = autoDetectMapping(columns);

      return new ParseResult(
          columns, rows, suggestedMapping, allRows.size() - 1, "CSV", charset.name());
    }
  }

  private Charset detectCharset(byte[] bytes) {
    // Einfache Heuristik: UTF-8 wenn gültig, sonst Windows-1252
    try {
      String test = new String(bytes, StandardCharsets.UTF_8);
      // Prüfe auf ungültige Zeichen
      if (!test.contains("\uFFFD")) {
        return StandardCharsets.UTF_8;
      }
    } catch (Exception ignored) {
      // UTF-8 fehlgeschlagen
    }
    return Charset.forName("Windows-1252");
  }

  private char detectSeparator(byte[] bytes, Charset charset) {
    String firstLines = new String(bytes, 0, Math.min(bytes.length, 1000), charset);
    String firstLine = firstLines.split("\n")[0];

    int semicolonCount = countChar(firstLine, ';');
    int commaCount = countChar(firstLine, ',');
    int tabCount = countChar(firstLine, '\t');

    if (semicolonCount >= commaCount && semicolonCount >= tabCount) {
      return ';';
    } else if (tabCount >= commaCount) {
      return '\t';
    }
    return ',';
  }

  private int countChar(String s, char c) {
    return (int) s.chars().filter(ch -> ch == c).count();
  }

  // ============================================================================
  // Excel Parser
  // ============================================================================

  private ParseResult parseExcel(InputStream inputStream) throws Exception {
    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);

      if (sheet.getPhysicalNumberOfRows() == 0) {
        throw new FileParseException("Excel-Datei ist leer");
      }

      // Header = erste Zeile
      Row headerRow = sheet.getRow(0);
      if (headerRow == null) {
        throw new FileParseException("Keine Header-Zeile gefunden");
      }

      List<String> columns = new ArrayList<>();
      for (Cell cell : headerRow) {
        columns.add(getCellValueAsString(cell));
      }

      // Daten = Rest (max MAX_ROWS)
      List<Map<String, String>> rows = new ArrayList<>();
      int maxRows = Math.min(sheet.getPhysicalNumberOfRows() - 1, MAX_ROWS);

      for (int i = 1; i <= maxRows; i++) {
        Row dataRow = sheet.getRow(i);
        if (dataRow == null) continue;

        Map<String, String> row = new LinkedHashMap<>();
        for (int j = 0; j < columns.size(); j++) {
          Cell cell = dataRow.getCell(j);
          row.put(columns.get(j), cell != null ? getCellValueAsString(cell) : "");
        }
        rows.add(row);
      }

      // Auto-Mapping
      Map<String, String> suggestedMapping = autoDetectMapping(columns);

      return new ParseResult(
          columns, rows, suggestedMapping, sheet.getPhysicalNumberOfRows() - 1, "XLSX", "UTF-8");
    }
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) return "";

    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> {
        if (DateUtil.isCellDateFormatted(cell)) {
          yield cell.getLocalDateTimeCellValue().toString();
        }
        // Dezimalzahlen ohne unnötige Nachkommastellen
        double value = cell.getNumericCellValue();
        if (value == Math.floor(value)) {
          yield String.valueOf((long) value);
        }
        yield String.valueOf(value);
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> {
        try {
          yield cell.getStringCellValue();
        } catch (Exception e) {
          yield String.valueOf(cell.getNumericCellValue());
        }
      }
      default -> "";
    };
  }

  // ============================================================================
  // Validierung
  // ============================================================================

  private void validateFile(String fileName, long fileSize) throws FileParseException {
    // Dateigröße
    if (fileSize > MAX_FILE_SIZE) {
      throw new FileParseException(
          String.format(
              "Datei zu groß: %.1f MB (max. %.1f MB)",
              fileSize / 1024.0 / 1024.0, MAX_FILE_SIZE / 1024.0 / 1024.0));
    }

    // Datei-Endung
    String extension = getFileExtension(fileName).toLowerCase();
    if (!SUPPORTED_EXTENSIONS.contains(extension)) {
      throw new FileParseException(
          "Nicht unterstütztes Format: " + extension + ". Erlaubt: CSV, XLSX");
    }
  }

  private String getFileExtension(String fileName) {
    int lastDot = fileName.lastIndexOf('.');
    if (lastDot < 0 || lastDot == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(lastDot + 1);
  }

  // ============================================================================
  // Inner Classes
  // ============================================================================

  /** Ergebnis des File-Parsings */
  public record ParseResult(
      List<String> columns,
      List<Map<String, String>> rows,
      Map<String, String> suggestedMapping,
      int totalRows,
      String fileType,
      String charset) {}

  /** Lead-Feld Information für Frontend */
  public record LeadFieldInfo(String key, String label, boolean required) {}

  /** Exception für Parse-Fehler */
  public static class FileParseException extends Exception {
    public FileParseException(String message) {
      super(message);
    }
  }
}
