# 🔗 Xentral API - Zentrale Informationen

**Erstellt:** 2025-10-19
**Quelle:** IT-Response (Mail vom 19.10.2025)
**Genutzt von:** Sprint 2.1.7.4, Sprint 2.1.7.2
**Status:** ⚠️ TEILWEISE KOMPLETT - Sales-Rep-Feld noch offen

---

## 📋 ÜBERSICHT

Dieses Dokument konsolidiert **alle Xentral-API Informationen** aus der IT-Response.
Beide Sprints (2.1.7.4 + 2.1.7.2) nutzen diese zentrale Referenz.

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

## 2️⃣ KUNDEN-API ⚠️ TEILWEISE OFFEN

### **Endpoint**
```bash
# Legacy (REST):
GET /customerlistv2

# Neue API (prüfen):
GET /api/v1/customers
```

**Empfehlung IT:** Prüfe instanzspezifische Doku für Endpoint-Wahl

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

### **❓ OFFEN: Sales-Rep Feld** ⚠️ KRITISCH

**Problem:** Wie filtern wir Kunden nach Verkäufer?

**Benötigt für:**
- Sprint 2.1.7.4: XentralOrderEventHandler Interface (Event-Mapping)
- Sprint 2.1.7.2: ConvertToCustomerDialog (Dropdown-Filter)

**Fragen:**
1. **Feldname:** Wie heißt das Feld? (z.B. `salesRepId`, `assignedUser`, `verkaufer`, `bearbeiter`)
2. **Wert-Format:** User-ID? Name? Email?
3. **Filter-Beispiel:** Wie sieht der API-Call aus?

**Test-Call benötigt:**
```bash
# Beispiel: 1 Kunde holen, um Felder zu sehen
GET /customerlistv2?page[size]=1

# Erwartete Response:
{
  "data": [{
    "id": "C-47236",
    "name": "Burger Brothers GmbH",
    "???": "???" ← SALES-REP FELD?
  }]
}
```

**Migration betroffen:**
```sql
-- Sprint 2.1.7.2: V10031
ALTER TABLE customers
ADD COLUMN xentral_sales_rep_id VARCHAR(???); ← Datentyp/Länge?
```

### **Feldnamen & Formate** ⚠️ INSTANZSPEZIFISCH

**IT-Hinweis:** Feldnamen sind instanzspezifisch
- Test-Call durchführen ODER
- Instanz-Doku prüfen: https://644b6ff97320d.xentral.biz/www/api/docs.html

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

### **❓ OFFEN: Order-Status für "Delivered"**

**Problem:** Wie erkennen wir "gelieferte Bestellungen"?

**Benötigt für:**
- Sprint 2.1.7.4: PROSPECT → AKTIV Transition (bei erster Lieferung)
- Sprint 2.1.7.2: Nightly Job (Check delivered orders)

**Fragen:**
1. **Status-Feld:** Heißt es `status`, `deliveryStatus`, `orderStatus`?
2. **Wert:** "DELIVERED", "GELIEFERT", "COMPLETED"?
3. **Alternativ:** Gibt es ein `deliveryDate` Feld?

**Test-Call benötigt:**
```bash
# Beispiel: 1 Rechnung holen, um Felder zu sehen
GET /api/v1/invoices?page[size]=1

# Erwartete Response:
{
  "data": [{
    "status": "DELIVERED" ← DIESES FELD?
    "deliveryDate": "2025-10-18" ← ODER DIESES?
  }]
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

### **Für Sprint 2.1.7.4 (ZUERST):**
1. ✅ Polling-Ansatz statt Webhooks (bereits entschieden)
2. ❓ **OFFEN:** Order-Status-Feld für "Delivered" klären
3. ✅ XentralOrderEventHandler Interface definieren (unabhängig von Xentral-Details)

### **Für Sprint 2.1.7.2 (DANACH):**
1. ❓ **OFFEN:** Sales-Rep-Feld klären (Feldname + Format)
2. ❓ **OFFEN:** Order-Status-Feld für "Delivered" klären (gleiche Info wie 2.1.7.4)
3. ✅ Nightly Job implementieren (Polling 1x täglich 02:00 Uhr)

### **Test-Calls benötigt:**
```bash
# 1. Customer-Felder inspizieren (Sales-Rep-Feld)
GET /customerlistv2?page[size]=1

# 2. Invoice-Felder inspizieren (Delivery-Status-Feld)
GET /api/v1/invoices?page[size]=1

# 3. OpenAPI Spec analysieren (optional)
curl https://raw.githubusercontent.com/xentral/api-spec-public/main/openapi/xentral-api.openapi-3.0.0.json | jq
```

---

## 9️⃣ ZUSAMMENFASSUNG - STATUS

| Kategorie | Status | Details |
|-----------|--------|---------|
| **Base-URL** | ✅ KOMPLETT | `https://644b6ff97320d.xentral.biz/api/v1` |
| **Auth** | ✅ KOMPLETT | Personal Access Token (Bearer) |
| **Kunden-API** | ⚠️ TEILWEISE | Endpoint ✅, Sales-Rep-Feld ❓ |
| **Rechnungs-API** | ⚠️ TEILWEISE | Endpoint ✅, Delivery-Status ❓ |
| **Filter-Syntax** | ✅ KOMPLETT | Query-Object-Pattern dokumentiert |
| **Rate Limits** | ✅ KOMPLETT | 10-20 req/min, Timeout 10s |
| **Testumgebung** | ✅ KOMPLETT | Test-Projekt mit `project.id` |
| **Webhooks** | ✅ KOMPLETT | Beta → Polling-Ansatz |
| **Polling-Frequenz** | ✅ KOMPLETT | 1x täglich 02:00 Uhr |

**Offene Punkte:** 2
1. ❓ Sales-Rep-Feld (Feldname + Format)
2. ❓ Order-Status-Feld für "Delivered" (Feldname + Wert)

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

**Letzte Aktualisierung:** 2025-10-19 (IT-Response verarbeitet, offene Punkte markiert)
**Owner:** Claude + Jörg (User Validation)
**Status:** ⚠️ 80% KOMPLETT - 2 offene Punkte
