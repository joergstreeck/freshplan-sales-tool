package de.freshplan.modules.leads.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;

import de.freshplan.domain.shared.LeadSource;
import de.freshplan.modules.leads.domain.LeadStage;
import de.freshplan.modules.leads.domain.LeadStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Tests für Pre-Claim Business Logic Variante B (Handelsvertretervertrag §2(8)(a)).
 *
 * <p>BUSINESS RULE (Variante B):
 *
 * <ul>
 *   <li>MESSE/TELEFON → Erstkontakt PFLICHT → direkt REGISTRIERUNG + Vollschutz (firstContactDocumentedAt gesetzt)
 *   <li>EMPFEHLUNG/WEB/PARTNER/SONSTIGE → Pre-Claim erlaubt → VORMERKUNG (firstContactDocumentedAt = NULL, 10 Tage Frist)
 *   <li>registered_at IMMER gesetzt (Audit Trail, DB Best Practice)
 * </ul>
 *
 * <p>Referenz: VARIANTE_B_MIGRATION_GUIDE.md, LeadSource.requiresFirstContact()
 */
@QuarkusTest
public class LeadPreClaimLogicTest {

  /**
   * Test 1: MESSE Lead MIT Erstkontakt → direkt REGISTRIERUNG (Variante B)
   *
   * <p>Business Rule: MESSE erfordert Erstkontakt (contactPerson) → Lead startet in REGISTRIERUNG,
   * registeredAt UND firstContactDocumentedAt gesetzt (Vollschutz).
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("MESSE lead with contact person → direct REGISTRIERUNG (Variante B)")
  public void testMesseLeadWithContactPerson_DirectRegistrierung() {
    Map<String, Object> request =
        Map.of(
            "companyName", "Z-Catering Mitte GmbH",
            "contactPerson", "Max Mustermann", // ← PFLICHT für MESSE!
            "source", "MESSE",
            "city", "Berlin",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("MESSE"))
        .body("stage", equalTo("REGISTRIERUNG")) // Stage 1 (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", notNullValue()) // ← Vollschutz!
        .body("contactPerson", equalTo("Max Mustermann"));
  }

  /**
   * Test 2: MESSE Lead OHNE Erstkontakt → 400 Bad Request
   *
   * <p>Business Rule: MESSE ohne contactPerson wird ABGELEHNT (Erstkontakt ist Pflicht).
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("MESSE lead without contact person → 400 Bad Request")
  public void testMesseLeadWithoutContactPerson_BadRequest() {
    Map<String, Object> request =
        Map.of(
            "companyName", "Y-Hotel Berlin",
            // "contactPerson" FEHLT! ← Sollte 400 triggern
            "source", "MESSE",
            "city", "Berlin",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(400)
        .body("error", equalTo("First contact required"))
        .body("source", equalTo("MESSE"));
  }

  /**
   * Test 3: TELEFON Lead MIT Erstkontakt → direkt REGISTRIERUNG
   *
   * <p>Business Rule: TELEFON erfordert Erstkontakt (identisch zu MESSE).
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("TELEFON lead with contact person → direct REGISTRIERUNG")
  public void testTelefonLeadWithContactPerson_DirectRegistrierung() {
    Map<String, Object> request =
        Map.of(
            "companyName", "X-Restaurant München",
            "contactPerson", "Anna Schmidt", // ← PFLICHT für TELEFON!
            "source", "TELEFON",
            "city", "München",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("TELEFON"))
        .body("stage", equalTo("REGISTRIERUNG")) // Stage 1 (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", notNullValue()) // ← Vollschutz!
        .body("contactPerson", equalTo("Anna Schmidt"));
  }

  /**
   * Test 4: TELEFON Lead OHNE Erstkontakt → 400 Bad Request
   *
   * <p>Business Rule: TELEFON ohne contactPerson wird ABGELEHNT.
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("TELEFON lead without contact person → 400 Bad Request")
  public void testTelefonLeadWithoutContactPerson_BadRequest() {
    Map<String, Object> request =
        Map.of(
            "companyName", "W-Kantine Hamburg",
            "source", "TELEFON",
            "city", "Hamburg",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(400)
        .body("error", equalTo("First contact required"))
        .body("source", equalTo("TELEFON"));
  }

  /**
   * Test 5: EMPFEHLUNG Lead → VORMERKUNG (Pre-Claim Variante B)
   *
   * <p>Business Rule: EMPFEHLUNG erlaubt Pre-Claim → Lead startet in VORMERKUNG,
   * registeredAt gesetzt (Audit Trail), firstContactDocumentedAt NULL (Pre-Claim aktiv).
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("EMPFEHLUNG lead → VORMERKUNG (Pre-Claim Variante B)")
  public void testEmpfehlungLead_VormerkungWithoutProtection() {
    Map<String, Object> request =
        Map.of(
            "companyName", "V-Hotel Dresden",
            "source", "EMPFEHLUNG",
            "city", "Dresden",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("EMPFEHLUNG"))
        .body("stage", equalTo("VORMERKUNG")) // Stage 0 (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", nullValue()); // ← Pre-Claim aktiv!
  }

  /**
   * Test 6: WEB_FORMULAR Lead → VORMERKUNG (Pre-Claim erlaubt)
   *
   * <p>Business Rule: WEB_FORMULAR erlaubt Pre-Claim → VORMERKUNG, registeredAt ist NULL.
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("WEB_FORMULAR lead → VORMERKUNG (Pre-Claim allowed)")
  public void testWebFormularLead_VormerkungWithoutProtection() {
    Map<String, Object> request =
        Map.of(
            "companyName", "U-Catering Köln",
            "source", "WEB_FORMULAR",
            "city", "Köln",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("WEB_FORMULAR"))
        .body("stage", equalTo("VORMERKUNG")) // Stage 0 (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", nullValue()); // ← Pre-Claim aktiv!
  }

  /**
   * Test 7: PARTNER Lead → VORMERKUNG (Pre-Claim erlaubt)
   *
   * <p>Business Rule: PARTNER erlaubt Pre-Claim → VORMERKUNG, registeredAt ist NULL.
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("PARTNER lead → VORMERKUNG (Pre-Claim allowed)")
  public void testPartnerLead_VormerkungWithoutProtection() {
    Map<String, Object> request =
        Map.of(
            "companyName", "T-Restaurant Stuttgart",
            "source", "PARTNER",
            "city", "Stuttgart",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("PARTNER"))
        .body("stage", equalTo("VORMERKUNG")) // Stage 0 (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", nullValue()); // ← Pre-Claim aktiv!
  }

  /**
   * Test 8: SONSTIGES Lead → VORMERKUNG (Pre-Claim erlaubt)
   *
   * <p>Business Rule: SONSTIGES erlaubt Pre-Claim → VORMERKUNG, registeredAt ist NULL.
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("SONSTIGES lead → VORMERKUNG (Pre-Claim allowed)")
  public void testSonstigesLead_VormerkungWithoutProtection() {
    Map<String, Object> request =
        Map.of(
            "companyName", "S-Kantine Frankfurt",
            "source", "SONSTIGES",
            "city", "Frankfurt",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("SONSTIGES"))
        .body("stage", equalTo("VORMERKUNG")) // Stage 0 (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", nullValue()); // ← Pre-Claim aktiv!
  }

  /**
   * Test 9: LeadSource.requiresFirstContact() validiert korrekte Enum-Werte
   *
   * <p>Unit Test für Business-Logic im Enum selbst.
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("LeadSource.requiresFirstContact() returns correct values")
  public void testLeadSourceRequiresFirstContact() {
    // MESSE und TELEFON erfordern Erstkontakt
    assert LeadSource.MESSE.requiresFirstContact();
    assert LeadSource.TELEFON.requiresFirstContact();

    // Alle anderen erlauben Pre-Claim
    assert !LeadSource.EMPFEHLUNG.requiresFirstContact();
    assert !LeadSource.WEB_FORMULAR.requiresFirstContact();
    assert !LeadSource.PARTNER.requiresFirstContact();
    assert !LeadSource.SONSTIGES.requiresFirstContact();
  }

  /**
   * Test 10: EMPFEHLUNG Lead MIT contactPerson → trotzdem VORMERKUNG (contactPerson ist optional)
   *
   * <p>Business Rule: Auch wenn contactPerson angegeben wird, bleibt EMPFEHLUNG in VORMERKUNG
   * (Erstkontakt ist nicht PFLICHT).
   */
  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("EMPFEHLUNG lead with contact person → still VORMERKUNG (optional)")
  public void testEmpfehlungLeadWithContactPerson_StillVormerkung() {
    Map<String, Object> request =
        Map.of(
            "companyName", "R-Hotel Leipzig",
            "contactPerson", "Julia Becker", // ← Optional für EMPFEHLUNG
            "source", "EMPFEHLUNG",
            "city", "Leipzig",
            "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("EMPFEHLUNG"))
        .body("stage", equalTo("VORMERKUNG")) // ← Immer noch VORMERKUNG! (JSON serializes as String)
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue()) // ← IMMER gesetzt (Audit Trail)!
        .body("firstContactDocumentedAt", nullValue()) // ← Pre-Claim aktiv (10 Tage Frist)!
        .body("contactPerson", equalTo("Julia Becker")); // ← contactPerson wird gespeichert
  }
}
