package de.freshplan.modules.leads.service;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests für FileParserService - Sprint 2.1.8
 *
 * <p>Testet das Fuzzy Auto-Mapping für Spalten-Erkennung:
 *
 * <ul>
 *   <li>Exakter Match (Dictionary)
 *   <li>Token-basiertes Match
 *   <li>Levenshtein Fuzzy-Match
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@QuarkusTest
class FileParserServiceTest {

  @Inject FileParserService fileParserService;

  // ============================================================================
  // Exakter Match Tests
  // ============================================================================

  @Nested
  @DisplayName("Exakter Match (Dictionary)")
  class ExactMatchTests {

    @Test
    @DisplayName("Erkennt standard deutsche Spaltennamen")
    void shouldMatchStandardGermanColumns() {
      List<String> columns = List.of("Firma", "E-Mail", "Telefon", "PLZ", "Stadt");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("companyName", mapping.get("Firma"));
      assertEquals("email", mapping.get("E-Mail"));
      assertEquals("phone", mapping.get("Telefon"));
      assertEquals("postalCode", mapping.get("PLZ"));
      assertEquals("city", mapping.get("Stadt"));
    }

    @Test
    @DisplayName("Erkennt englische Spaltennamen")
    void shouldMatchEnglishColumns() {
      List<String> columns = List.of("Company", "Email", "Phone", "City", "Street");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("companyName", mapping.get("Company"));
      assertEquals("email", mapping.get("Email"));
      assertEquals("phone", mapping.get("Phone"));
      assertEquals("city", mapping.get("City"));
      assertEquals("street", mapping.get("Street"));
    }

    @Test
    @DisplayName("Case-insensitive Matching")
    void shouldMatchCaseInsensitive() {
      List<String> columns = List.of("FIRMA", "firma", "Firma", "FiRmA");

      // Nur erstes Match (danach ist companyName "used")
      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals(1, mapping.size());
      assertTrue(mapping.containsValue("companyName"));
    }
  }

  // ============================================================================
  // Token-Match Tests
  // ============================================================================

  @Nested
  @DisplayName("Token-basiertes Match")
  class TokenMatchTests {

    @Test
    @DisplayName("Erkennt 'E-Mail-Adresse' durch Token 'mail'")
    void shouldMatchEmailAdresse() {
      List<String> columns = List.of("E-Mail-Adresse");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("email", mapping.get("E-Mail-Adresse"));
    }

    @Test
    @DisplayName("Erkennt 'Unternehmensname' durch Token 'unternehmen'")
    void shouldMatchUnternehmensname() {
      List<String> columns = List.of("Unternehmensname");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("companyName", mapping.get("Unternehmensname"));
    }

    @Test
    @DisplayName("Erkennt 'Telefonnummer_privat' durch Token 'telefon'")
    void shouldMatchTelefonnummerPrivat() {
      List<String> columns = List.of("Telefonnummer_privat");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("phone", mapping.get("Telefonnummer_privat"));
    }

    @Test
    @DisplayName("Erkennt zusammengesetzte Spaltennamen")
    void shouldMatchCompoundColumnNames() {
      // "Kontaktperson" wird über Token-Match zu contactPerson (enthält "kontakt")
      List<String> columns =
          List.of("Straße und Hausnummer", "Homepage URL", "Ansprechpartner Info");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("street", mapping.get("Straße und Hausnummer"));
      assertEquals("website", mapping.get("Homepage URL"));
      assertEquals("contactPerson", mapping.get("Ansprechpartner Info"));
    }
  }

  // ============================================================================
  // Fuzzy-Match Tests (Levenshtein)
  // ============================================================================

  @Nested
  @DisplayName("Fuzzy-Match (Levenshtein)")
  class FuzzyMatchTests {

    @Test
    @DisplayName("Erkennt 'Teleofn' als Tippfehler für 'Telefon'")
    void shouldMatchTypoTelefon() {
      List<String> columns = List.of("Teleofn");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("phone", mapping.get("Teleofn"));
    }

    @Test
    @DisplayName("Erkennt 'Mailadresse' durch Token-Match auf 'mail'")
    void shouldMatchMailadresse() {
      // Wird über Token-Match gefunden (enthält "mail")
      List<String> columns = List.of("Mailadresse");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("email", mapping.get("Mailadresse"));
    }

    @Test
    @DisplayName("Erkennt 'Strase' als Tippfehler für 'Strasse'")
    void shouldMatchTypoStrasse() {
      List<String> columns = List.of("Strase");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("street", mapping.get("Strase"));
    }

    @Test
    @DisplayName("Lehnt zu unterschiedliche Strings ab (unter Threshold)")
    void shouldRejectTooDistantStrings() {
      List<String> columns = List.of("XYZ123", "Foobar", "Blub");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertTrue(mapping.isEmpty());
    }
  }

  // ============================================================================
  // Edge Cases
  // ============================================================================

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Verhindert doppelte Zuordnungen")
    void shouldPreventDuplicateMappings() {
      // Beide könnten auf 'email' matchen, aber nur erste wird zugeordnet
      List<String> columns = List.of("E-Mail", "Mail-Adresse");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      long emailCount = mapping.values().stream().filter(v -> v.equals("email")).count();
      assertEquals(1, emailCount);
    }

    @Test
    @DisplayName("Leere Spaltenliste gibt leeres Mapping zurück")
    void shouldHandleEmptyColumns() {
      List<String> columns = List.of();

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertTrue(mapping.isEmpty());
    }

    @Test
    @DisplayName("Whitespace in Spaltennamen wird getrimmt")
    void shouldTrimWhitespace() {
      List<String> columns = List.of("  Firma  ", " E-Mail ");

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("companyName", mapping.get("  Firma  "));
      assertEquals("email", mapping.get(" E-Mail "));
    }

    @Test
    @DisplayName("Mixed Real-World CSV Header")
    void shouldHandleRealWorldCsvHeader() {
      // Simuliert typische Excel/CSV-Export Header
      List<String> columns =
          List.of(
              "Firmenname", // Exakt
              "E-Mail-Adresse", // Token
              "Teleofn", // Fuzzy (Tippfehler)
              "Postleitzahl", // Exakt
              "Ort", // Exakt
              "Straße", // Exakt
              "Anmerkungen", // Token (notes)
              "ID", // Ignoriert
              "Erstelldatum" // Ignoriert
              );

      Map<String, String> mapping = fileParserService.autoDetectMapping(columns);

      assertEquals("companyName", mapping.get("Firmenname"));
      assertEquals("email", mapping.get("E-Mail-Adresse"));
      assertEquals("phone", mapping.get("Teleofn"));
      assertEquals("postalCode", mapping.get("Postleitzahl"));
      assertEquals("city", mapping.get("Ort"));
      assertEquals("street", mapping.get("Straße"));
      assertEquals("notes", mapping.get("Anmerkungen"));
      assertFalse(mapping.containsKey("ID"));
      assertFalse(mapping.containsKey("Erstelldatum"));
    }
  }
}
