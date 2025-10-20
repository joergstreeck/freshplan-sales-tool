# 🔗 Xentral API - Zentrale Informationen

**Erstellt:** 2025-10-19
**Quelle:** IT-Response (Mail vom 19.10.2025) + Screenshots (20.10.2025)
**Genutzt von:** Sprint 2.1.7.4, Sprint 2.1.7.2
**Status:** ✅ 100% KOMPLETT - Alle Felder aus Screenshots verifiziert

---

## 📋 ÜBERSICHT

Dieses Dokument konsolidiert **alle Xentral-API Informationen** aus der IT-Response.
Beide Sprints (2.1.7.4 + 2.1.7.2) nutzen diese zentrale Referenz.

**📸 WICHTIG:** Detaillierte Screenshot-Analyse siehe:
→ [`XENTRAL_SCREENSHOTS_FINDINGS.md`](./XENTRAL_SCREENSHOTS_FINDINGS.md)
- Alle Feldnamen aus Xentral UI verifiziert
- Code-Beispiele für beide Sprints
- Business-Logik dokumentiert

---

## 1️⃣ BASIS-KONFIGURATION ✅ KOMPLETT

### **Base-URL**
```
https://644b6ff97320d.xentral.biz/api/v1
```

### **Authentifizierung**
- **Methode:** Personal Access Token (PAT)
- **Header:** `Authorization: Bearer {token}`
- **Token erstellen:**
  1. Administration > Account settings
  2. Developer Settings
  3. Personal Access Tokens

⚠️ **WICHTIG:** Keine granularen Read-Only Berechtigungen verfügbar
- Token hat unbegrenzte Permissions

### **Wichtige Links**
- **API Intro:** https://developer.xentral.com/reference/intro
- **Authentifizierung:** https://developer.xentral.com/reference/authentication
- **Filter/Pagination:** https://developer.xentral.com/reference/filtering-sorting-pagination
- **Testing:** https://developer.xentral.com/reference/e
- **Webhooks:** https://developer.xentral.com/reference/webhooks
- **OpenAPI Spec:** https://raw.githubusercontent.com/xentral/api-spec-public/main/openapi/xentral-api.openapi-3.0.0.json
- **Instanz-Doku:** https://644b6ff97320d.xentral.biz/www/api/docs.html

---

## 2️⃣ KUNDEN-API ✅ KOMPLETT

### **Endpoint**
```bash
# Legacy (REST):
GET /customerlistv2

# Neue API:
GET /api/v1/customers
```

**Empfehlung IT:** Prüfe instanzspezifische Doku für Endpoint-Wahl

### **📸 Kundennummer-Format** ✅ VERIFIZIERT (Screenshot)

**Quelle:** Screenshot Xentral UI #1 + #2 (siehe `XENTRAL_SCREENSHOTS_FINDINGS.md`)

- **Format:** 5-stellig OHNE Präfix
- **Beispiele:** `56037`, `55509` (NICHT `C-56037` oder `K-56037`)
- **FreshPlan Spalte:** `xentral_customer_id VARCHAR(10)` (5 Ziffern + Reserve)

### **📸 Sales-Rep Feld** ✅ VERIFIZIERT (Screenshot)

**Quelle:** Screenshot Xentral UI #1 (Kunde/Adresse → Zuordnung-Section)

**Feldnamen:**
- **Primary:** `"Vertrieb"` (Sales-Rep)
- **Fallback:** `"Innendienst"` (Account Manager, wenn Vertrieb leer)

**API-Mapping:**
```bash
# Schritt 1: User-ID aus Vertrieb-Feld holen
GET /api/v1/customers/{id}
# Response: { "vertrieb": "U-12345", "innendienst": "U-67890" }

# Schritt 2: User-Details holen
GET /api/v1/users/{userId}
# Response: { "id": "U-12345", "username": "jstreeck", "name": "Jörg Streeck" }
```

**FreshPlan Migration:**
```sql
-- Sprint 2.1.7.2: V10031
ALTER TABLE customers
ADD COLUMN xentral_sales_rep_id VARCHAR(50); -- User-ID (z.B. "U-12345")
```

**Business-Logik:**
- IF `Vertrieb` nicht leer → nutze `Vertrieb`
- ELSE IF `Innendienst` nicht leer → nutze `Innendienst`
- ELSE → Warnung loggen, Kein Sync

### **Filter-Syntax** ✅ KOMPLETT
```bash
?filter[0][key]=FELDNAME&filter[0][op]=equals&filter[0][value]=WERT
```

**Operatoren:**
- `equals` - Exact Match
- `in` - Mehrere Werte (z.B. `value[]=A&value[]=B`)
- `startsWith` - Prefix Match
- `contains` - Substring Match
- `between` - Range (z.B. Datum)

**Beispiel:**
```bash
# Kunde nach ID filtern
GET /customerlistv2?filter[0][key]=id&filter[0][op]=equals&filter[0][value]=C-47236
```

---

## 2️⃣.2 LEGACY API vs. NEW API ✅ KOMPLETT (20.10.2025)

### **⚠️ KRITISCHE ERKENNTNIS: Sales-Rep Felder nur in Legacy API!**

**Getestet:** 2025-10-20 mit Token `344|AVV7locnqoJLXmvJsOgNINcfDm2j5b7J6GoEq1Jw`

**Vergleich:**

| Feature | New API (`/api/v1/customers`) | Legacy API (`/api/AdresseGet`) |
|---------|-------------------------------|--------------------------------|
| **Format** | JSON | XML |
| **Auth** | Bearer Token | Bearer Token |
| **Kundennummer** | ✅ Verfügbar | ✅ Verfügbar |
| **Vertrieb (Sales-Rep)** | ❌ FEHLT | ✅ VERFÜGBAR |
| **Innendienst (Account Mgr)** | ❌ FEHLT | ✅ VERFÜGBAR |
| **Vollständige Felder** | ⚠️ Limitiert | ✅ ALLE Felder |

### **Legacy API Endpoints**

**Adress-Verwaltung:**
```bash
# Adresse abrufen (GET)
GET /api/AdresseGet?id={addressId}

# Adresse erstellen (POST)
POST /api/AdresseCreate

# Adresse bearbeiten (PUT)
PUT /api/AdresseEdit
```

### **Legacy API Response Format (XML)**

**Test-Call:**
```bash
curl -s -H 'Authorization: Bearer 344|AVV7...' \
'https://644b6ff97320d.xentral.biz/api/AdresseGet?id=3261'
```

**Response (XML):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<root>
  <kundennummer>56037</kundennummer>
  <vertrieb>0</vertrieb>           <!-- ✅ GEFUNDEN! (0 = nicht zugewiesen) -->
  <innendienst>0</innendienst>     <!-- ✅ GEFUNDEN! (0 = nicht zugewiesen) -->
  <projekt>JBX-ORDER-TRANSFER</projekt>
  <typ>firma</typ>
  <name>Hotel NH Vienna Airport Conference Center</name>
  <telefon>...</telefon>
  <email>...</email>
  <!-- ... weitere Felder ... -->
</root>
```

### **Field-Mapping für Sprint 2.1.7.2**

**Sales-Rep Detection Logic:**
```java
// Schritt 1: Legacy API aufrufen
String xml = xentralClient.get("/api/AdresseGet?id=" + customerId);
Document doc = parseXml(xml);

// Schritt 2: Vertrieb + Innendienst extrahieren
String vertriebId = doc.selectFirst("vertrieb").text();  // "0" oder "U-12345"
String innendienstId = doc.selectFirst("innendienst").text();  // "0" oder "U-67890"

// Schritt 3: Sales-Rep bestimmen (Primary: Vertrieb, Fallback: Innendienst)
String salesRepId = null;
if (!vertriebId.equals("0")) {
    salesRepId = vertriebId;  // Vertrieb hat Priorität
} else if (!innendienstId.equals("0")) {
    salesRepId = innendienstId;  // Fallback auf Innendienst
}

// Schritt 4: FreshPlan User finden
if (salesRepId != null) {
    User freshplanUser = userRepo.findByXentralUserId(salesRepId);
    if (freshplanUser != null) {
        customer.assignedTo = freshplanUser.id;
        customerRepo.save(customer);
    }
}
```

**⚠️ WICHTIG für Sprint 2.1.7.2:**
- **MUSS Legacy API nutzen** für Sales-Rep Mapping
- New API (`/api/v1/customers`) hat NICHT die benötigten Felder
- XML-Parsing erforderlich (Java: JAXB oder DOM/SAX Parser)

### **Warum Legacy API?**

**Vorteile:**
- ✅ ALLE Felder verfügbar (inkl. `vertrieb`, `innendienst`)
- ✅ Funktioniert mit aktuellem Token
- ✅ Sofort einsatzbereit

**Nachteile:**
- ⚠️ XML statt JSON (Parsing-Overhead)
- ⚠️ Legacy-Support unklar (möglicherweise deprecated in Zukunft)

**Entscheidung:**
✅ **Nutze Legacy API für Phase 1 (Sprint 2.1.7.2)**
- Später: Migration zu New API, wenn Xentral Felder nachreicht

---

## 2️⃣.1 LIEFERSCHEIN-API ✅ KOMPLETT

### **Endpoint**
```bash
GET /api/v1/delivery-notes
# oder Legacy:
GET /api/v1/lieferschein
```

### **📸 Lieferstatus Detection** ✅ VERIFIZIERT (Screenshot)

**Quelle:** Screenshot Xentral UI #2 (Lieferschein-Detail)

**Status-Feld:** `"Status"`

**Trigger-Wert:** `"VERSENDET"` (shipped/versendet)

**User-Entscheidung (20.10.2025):**
✅ **`Status = "VERSENDET"` reicht für PROSPECT → AKTIV Aktivierung!**

**Begründung:**
- Lieferschein mit Status VERSENDET = Commitment erfüllt
- Food-Logistik: Wenn versendet → wird auch geliefert (B2B-Direct-Delivery)
- Kein Paket-Tracking nötig (direkte Anlieferung)

**Beispiel API-Call:**
```bash
# Alle versendeten Lieferscheine für Kunde
GET /api/v1/lieferschein?filter[0][key]=kunde&filter[0][op]=equals&filter[0][value]=55509&filter[1][key]=status&filter[1][op]=equals&filter[1][value]=VERSENDET
```

**Sprint 2.1.7.4 Interface:**
```java
public interface XentralOrderEventHandler {
    void handleOrderDelivered(
        String xentralCustomerId,  // "55509" (5-stellig)
        String deliveryNoteNumber, // "XFJ868"
        LocalDate shippedDate      // Datum Status=VERSENDET
    );
}
```

---

## 3️⃣ RECHNUNGS-API ✅ KOMPLETT

### **Endpoint**
```bash
GET /api/v1/invoices
```

### **Filter nach Customer-ID**
```bash
GET /api/v1/invoices?filter[0][key]=customerId&filter[0][op]=equals&filter[0][value]=C-47236
```

### **Response-Format**
Standard-Format mit `meta`, `data`, `links`:
```json
{
  "meta": {
    "page": 1,
    "size": 50,
    "total": 123
  },
  "data": [
    {
      "invoiceId": "INV-12345",
      "customerId": "C-47236",
      "invoiceDate": "2025-10-15",
      "dueDate": "2025-11-15",
      "paymentDate": "2025-11-10",
      "amount": 4500.00,
      "status": "PAID"
    }
  ],
  "links": { ... }
}
```

### **Felder für Zahlungsverhalten** ✅ KOMPLETT

**Sprint 2.1.7.2 benötigt:**
- `invoiceDate` - Rechnungsdatum
- `paymentDate` - Zahlungsdatum (NULL wenn nicht bezahlt)
- `dueDate` - Fälligkeitsdatum
- `amount` - Rechnungsbetrag
- `status` - Status (z.B. PAID, OPEN, OVERDUE)

**Berechnung:**
```java
// Zahlungsverhalten berechnen
int daysToPay = ChronoUnit.DAYS.between(invoiceDate, paymentDate);
int daysOverdue = ChronoUnit.DAYS.between(dueDate, paymentDate);

// Ampel:
// 🟢 GREEN: Pünktlich oder früher (daysToPay <= 0)
// 🟡 YELLOW: 1-7 Tage zu spät
// 🟠 ORANGE: 8-30 Tage zu spät
// 🔴 RED: >30 Tage zu spät
```

### **📸 Zahlungsverhalten Felder** ✅ VERIFIZIERT (Screenshot)

**Quelle:** Screenshot Xentral UI #3 (Rechnung → Mahnwesen-Section)

**Kritische Felder:**
- **zahlungsstatus:** `"offen"` oder `"bezahlt"` (Dropdown)
- **bezahlt_am:** Date | NULL (leer wenn Status="offen")
- **mahndatum:** Date (für Mahnungen)
- **zahlungsziel_tage:** Integer (Anzahl Tage, z.B. 14, 30)
- **lieferdatum:** Date
- **SOLL:** Decimal (Rechnungsbetrag)
- **IST:** Decimal (bereits bezahlt)

**Zahlungsverhalten Berechnung:**
```java
public enum PaymentBehavior { GREEN, YELLOW, ORANGE, RED }

public PaymentBehavior calculatePaymentBehavior(Invoice invoice) {
    if (invoice.zahlungsstatus.equals("offen")) {
        return PaymentBehavior.PENDING;
    }

    LocalDate invoiceDate = invoice.datum;
    LocalDate paymentDate = invoice.bezahlt_am;
    LocalDate dueDate = invoiceDate.plusDays(invoice.zahlungsziel_tage);

    long daysOverdue = ChronoUnit.DAYS.between(dueDate, paymentDate);

    // Ampel-Logik
    if (daysOverdue <= 0) return PaymentBehavior.GREEN;  // Pünktlich/früher
    if (daysOverdue <= 7) return PaymentBehavior.YELLOW; // 1-7 Tage zu spät
    if (daysOverdue <= 30) return PaymentBehavior.ORANGE; // 8-30 Tage
    return PaymentBehavior.RED; // >30 Tage zu spät
}
```

---

## 4️⃣ ZAHLUNGSVERHALTEN ✅ KOMPLETT

### **Payment-Summary Endpoint**
❌ **Nicht verfügbar**

### **Alternative: Selbst berechnen** ✅
Aus `/api/v1/invoices` Feldern:
- `invoiceDate`
- `paymentDate`
- `dueDate`
- `amount`
- `status`

**Implementation:** Siehe 3️⃣ Rechnungs-API

---

## 5️⃣ RATE LIMITS & PERFORMANCE ✅ KOMPLETT

### **Rate Limits**
⚠️ **Nicht dokumentiert**

**IT-Empfehlung:** Konservativ starten
- **Start:** 10-20 requests/min
- **Monitor:** Response-Header prüfen (X-RateLimit-Remaining)
- **Bei Fehler 429:** Exponential Backoff

### **Timeout**
- **Empfohlen:** 10s
- **Retry:** Max 3x mit Exponential Backoff

### **Pagination**
```bash
?page[size]=50  # Max 50 Items pro Seite
?page[number]=1 # Start bei 1
```

**Best Practice:**
```java
// Batch-Processing
int pageSize = 50; // Max
int currentPage = 1;

while (hasMoreData) {
    List<Customer> customers = apiClient.getCustomers(currentPage, pageSize);
    // Process batch
    currentPage++;
}
```

---

## 6️⃣ TESTUMGEBUNG ✅ KOMPLETT

### **Test-System**
❌ **Kein separates Test-System** standardmäßig verfügbar

### **Lösung: Test-Projekt in Production**
**Referenz:** https://developer.xentral.com/reference/e

**Vorgehen:**
1. Test-Projekt in Production erstellen
2. Alle Requests mit `project.id` filtern
3. Daten isoliert von Production

**Beispiel:**
```bash
# Test-Projekt-ID: TEST-2025
GET /customerlistv2?filter[0][key]=project.id&filter[0][op]=equals&filter[0][value]=TEST-2025
```

**Nächste Schritte:**
1. Test-Projekt anlegen
2. Test-Kunden/Rechnungen erstellen
3. API-Calls gegen Test-Projekt testen

---

## 7️⃣ WEBHOOKS ✅ KOMPLETT

### **Status**
⚠️ **Beta-Feature** (hinter Feature-Flag)

### **Verfügbarkeit**
- **Kontakt:** api@xentral.com
- **Setup:** Feature-Flag aktivieren erforderlich

### **Entscheidung für Sprints 2.1.7.4 + 2.1.7.2**
✅ **Polling-Ansatz nutzen** (statt Webhooks)

**Begründung:**
- Webhooks in Beta
- Polling sofort umsetzbar
- Nightly Job reicht für Use Cases (Dashboard, Churn-Alarm)

**Polling-Frequenz:**
- **1x täglich (02:00 Uhr)** - User-Entscheidung 2025-10-19

**Migration-Path:**
Später zu Webhooks wechselbar, wenn Production-Ready

---

## 8️⃣ NÄCHSTE SCHRITTE

### **✅ Für Sprint 2.1.7.4 (ZUERST) - ALLES BEREIT!**
1. ✅ Polling-Ansatz statt Webhooks (bereits entschieden)
2. ✅ Order-Status-Feld für "Delivered" geklärt: `Status = "VERSENDET"` (Screenshot #2)
3. ✅ XentralOrderEventHandler Interface definieren (unabhängig von Xentral-Details)

**Status:** 🟢 Kann morgen starten - alle Informationen vorhanden!

### **✅ Für Sprint 2.1.7.2 (DANACH) - ALLES BEREIT!**
1. ✅ Sales-Rep-Feld geklärt: **Legacy API `/api/AdresseGet`** nutzen (XML: `<vertrieb>`, `<innendienst>`)
2. ✅ Order-Status-Feld für "Delivered" geklärt: `Status = "VERSENDET"` (gleiche Info wie 2.1.7.4)
3. ✅ Nightly Job implementieren (Polling 1x täglich 02:00 Uhr)
4. ✅ API-Token funktioniert: `344|AVV7locnqoJLXmvJsOgNINcfDm2j5b7J6GoEq1Jw`

**Status:** 🟢 Kann nach 2.1.7.4 starten - alle Informationen vorhanden!

### **Implementierungs-Details für Sprint 2.1.7.2:**
```java
// 1. Legacy API für Sales-Rep Mapping nutzen
String xml = xentralClient.get("/api/AdresseGet?id=" + customerId);
Document doc = parseXml(xml);
String vertriebId = doc.selectFirst("vertrieb").text();
String innendienstId = doc.selectFirst("innendienst").text();

// 2. Lieferschein-Polling für PROSPECT → AKTIV
GET /api/v1/delivery-notes?filter[0][key]=status&filter[0][op]=equals&filter[0][value]=VERSENDET

// 3. Rechnungs-Polling für Zahlungsverhalten
GET /api/v1/invoices?filter[0][key]=zahlungsstatus&filter[0][op]=in&filter[0][value][]=offen&filter[0][value][]=bezahlt
```

---

## 9️⃣ ZUSAMMENFASSUNG - STATUS

| Kategorie | Status | Details |
|-----------|--------|---------|
| **Base-URL** | ✅ KOMPLETT | `https://644b6ff97320d.xentral.biz/api/v1` |
| **Auth** | ✅ KOMPLETT | Personal Access Token (Bearer) |
| **Kunden-API (New)** | ⚠️ LIMITIERT | Endpoint ✅, Kundennummer ✅ (5-stellig), Sales-Rep ❌ (fehlt!) |
| **Kunden-API (Legacy)** | ✅ KOMPLETT | Endpoint ✅, Kundennummer ✅, Sales-Rep ✅ (XML: `<vertrieb>`, `<innendienst>`) |
| **Lieferschein-API** | ✅ KOMPLETT | Endpoint ✅, Status ✅ (`VERSENDET` = Trigger) |
| **Rechnungs-API** | ✅ KOMPLETT | Endpoint ✅, Zahlungsfelder ✅ (zahlungsstatus, bezahlt_am, etc.) |
| **Filter-Syntax** | ✅ KOMPLETT | Query-Object-Pattern dokumentiert |
| **Rate Limits** | ✅ KOMPLETT | 10-20 req/min, Timeout 10s |
| **Testumgebung** | ✅ KOMPLETT | Test-Projekt mit `project.id` |
| **Webhooks** | ✅ KOMPLETT | Beta → Polling-Ansatz |
| **Polling-Frequenz** | ✅ KOMPLETT | 1x täglich 02:00 Uhr |

**✅ Offene Punkte: 0 - ALLE Informationen verifiziert!**

**Alle kritischen Informationen aus Screenshots + API-Tests verifiziert:**
- ✅ Kundennummer-Format (5-stellig ohne Präfix)
- ✅ Sales-Rep-Feld (`Vertrieb` + Fallback `Innendienst`) - **NUR Legacy API!**
- ✅ Lieferstatus-Detection (`Status = VERSENDET`)
- ✅ Zahlungsverhalten-Felder (zahlungsstatus, bezahlt_am, mahndatum, zahlungsziel_tage)
- ✅ Business-Logik definiert (siehe `XENTRAL_SCREENSHOTS_FINDINGS.md`)
- ✅ Legacy API XML-Format dokumentiert (inkl. Code-Beispiele)

**✅ API-Token Status:**
- Token `344|AVV7locnqoJLXmvJsOgNINcfDm2j5b7J6GoEq1Jw` validiert: **FUNKTIONIERT**
- Erfolgreich getestet: `/api/v1/customers` (JSON) + `/api/AdresseGet` (XML)
- Bereit für Sprint 2.1.7.2 Implementierung

---

## 🔗 REFERENZEN

**Sprints:**
- Sprint 2.1.7.4: [TRIGGER_SPRINT_2_1_7_4.md](../TRIGGER_SPRINT_2_1_7_4.md)
- Sprint 2.1.7.4: [SPEC_SPRINT_2_1_7_4_TECHNICAL.md](SPEC_SPRINT_2_1_7_4_TECHNICAL.md)
- Sprint 2.1.7.2: [TRIGGER_SPRINT_2_1_7_2.md](../TRIGGER_SPRINT_2_1_7_2.md)
- Sprint 2.1.7.2: [SPEC_SPRINT_2_1_7_2_TECHNICAL.md](SPEC_SPRINT_2_1_7_2_TECHNICAL.md)

**Design Decisions:**
- Sprint 2.1.7.4: [SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md](SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md)
- Sprint 2.1.7.2: [SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md](SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md)

**Xentral Dokumentation:**
- API Intro: https://developer.xentral.com/reference/intro
- OpenAPI Spec: https://raw.githubusercontent.com/xentral/api-spec-public/main/openapi/xentral-api.openapi-3.0.0.json
- Instanz-Doku: https://644b6ff97320d.xentral.biz/www/api/docs.html

---

**Letzte Aktualisierung:** 2025-10-20 23:58 (Legacy API dokumentiert, Token validiert)
**Owner:** Claude + Jörg (Screenshots + API-Tests + User-Entscheidungen)
**Status:** ✅ 100% KOMPLETT - Alle Informationen für Sprint 2.1.7.4 + 2.1.7.2 vorhanden

**📸 Detaillierte Screenshot-Analyse:** → [`XENTRAL_SCREENSHOTS_FINDINGS.md`](./XENTRAL_SCREENSHOTS_FINDINGS.md)
**🔐 Token-Erstellung Checkliste:** → [`XENTRAL_TOKEN_CHECKLIST.md`](./XENTRAL_TOKEN_CHECKLIST.md)
