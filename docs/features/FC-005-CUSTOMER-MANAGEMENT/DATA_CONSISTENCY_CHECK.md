# Datenbank-Konsistenzprüfung

## 📊 Aktuelle Datenlage (09.08.2025 17:50)

### ✅ Kunden: 69 
**Status:** KORREKT
- API Response: 69 Kunden
- Verschiedene Typen: TEST_CUST_*, normale Kunden, Spezial-Szenarien

### ⚠️ Kontakte: 25 (nicht 2!)
**Status:** DISKREPANZ GEFUNDEN
- **Tatsächlich in DB:** 25 Kontakte (über alle Kunden verteilt)
- **Frontend zeigt:** Nur 2 Kontakte (wahrscheinlich nur Mock-Daten)
- **Verteilung:**
  - Müller Gastro Gruppe (TEST_CUST_001): 8 Kontakte
  - BioFresh Märkte (TEST_CUST_002): 3 Kontakte  
  - Hotel Zur Post (TEST_CUST_011): 3 Kontakte
  - Bäckerei Goldkorn (TEST_CUST_012): 2 Kontakte
  - Weitere kleine Kunden: je 1 Kontakt
  - **TOTAL:** 25 Kontakte aus TestDataContactInitializer

### ✅ Opportunities: 20 (alle aktiv!)
**Status:** KORREKT, aber Missverständnis bei "aktiv"
- **Total:** 20 Opportunities
- **Aktive (nicht abgeschlossen):** 20
  - NEW_LEAD: 2
  - QUALIFICATION: 6
  - NEEDS_ANALYSIS: 4
  - PROPOSAL: 5
  - NEGOTIATION: 3
  - CLOSED_WON: 0
  - CLOSED_LOST: 0
- **Erklärung:** In OpportunityStage.isActive() gelten alle Stages außer CLOSED_WON und CLOSED_LOST als "aktiv"

## 🔍 Problem-Analyse

### Frontend zeigt nur 2 Kontakte
**Mögliche Ursachen:**
1. Frontend lädt noch Mock-Daten statt echte API-Daten
2. Frontend filtert Kontakte (z.B. nur Primary Contacts)
3. Frontend hat Pagination und zeigt nur erste Seite

### API-Test Befehle:
```bash
# Alle Kunden zählen
curl -s "http://localhost:8080/api/customers" | jq '.totalElements'
# Result: 69 ✅

# Alle Kontakte zählen (über alle Kunden)
curl -s "http://localhost:8080/api/customers?size=100" | jq -r '.content[].id' | \
  while read id; do 
    curl -s "http://localhost:8080/api/customers/$id/contacts" | jq '. | length'
  done | awk '{sum+=$1} END {print sum}'
# Result: 25 ✅

# Opportunities zählen
curl -s "http://localhost:8080/api/opportunities" | jq '. | length'
# Result: 20 ✅

# Aktive Opportunities (nicht CLOSED)
curl -s "http://localhost:8080/api/opportunities" | \
  jq '[.[] | select(.stage | test("^(NEW_LEAD|QUALIFICATION|NEEDS_ANALYSIS|PROPOSAL|NEGOTIATION|RENEWAL)$"))] | length'
# Result: 20 ✅
```

## 🎯 Korrektur-Empfehlungen

### 1. Frontend Contact-Anzeige fixen
Das Frontend sollte die echten 25 Kontakte aus der API laden, nicht nur 2 Mock-Kontakte.

**Zu prüfen im Frontend:**
- Wird `/api/customers/{id}/contacts` korrekt aufgerufen?
- Werden die Daten korrekt gemappt?
- Gibt es Filter, die Kontakte ausblenden?

### 2. Opportunity-Zählung verstehen
- 20 Opportunities insgesamt ✅
- 20 "aktive" (= nicht abgeschlossen) ✅
- 0 abgeschlossene (CLOSED_WON/CLOSED_LOST) ✅

Das ist korrekt! "Aktiv" bedeutet hier "nicht abgeschlossen".

### 3. Test-Befehle für Frontend-Debugging:
```javascript
// In Browser Console:
// Check if contacts are loaded from API
fetch('/api/customers/dd08c720-9478-4e08-a3fb-4cd7e27418b6/contacts')
  .then(r => r.json())
  .then(console.log)
// Should show 3 contacts for BioFresh

// Check all customers with contacts
fetch('/api/customers?size=100')
  .then(r => r.json())
  .then(data => {
    const promises = data.content.map(c => 
      fetch(`/api/customers/${c.id}/contacts`)
        .then(r => r.json())
        .then(contacts => ({customer: c.companyName, count: contacts.length}))
    );
    return Promise.all(promises);
  })
  .then(results => {
    const withContacts = results.filter(r => r.count > 0);
    console.log('Customers with contacts:', withContacts);
    console.log('Total contacts:', results.reduce((sum, r) => sum + r.count, 0));
  });
```

## 📊 Zusammenfassung

| Entität | Soll | Ist Backend | Ist Frontend | Status |
|---------|------|-------------|--------------|--------|
| Kunden | 69 | 69 ✅ | 69 ✅ | OK |
| Kontakte | 25 | 25 ✅ | 2 ❌ | FEHLER |
| Opportunities | 20 | 20 ✅ | 16? ⚠️ | PRÜFEN |
| Aktive Opp. | 20 | 20 ✅ | 16? ⚠️ | PRÜFEN |

## 🚨 Nächste Schritte

1. **Frontend Contact Service debuggen**
   - Prüfen ob echte API aufgerufen wird
   - Console.log für geladene Daten
   - Mock-Daten entfernen

2. **Opportunity-Anzeige prüfen**
   - Warum zeigt Frontend nur 16 statt 20?
   - Filter aktiv?
   - Pagination?

3. **Integration testen**
   - E2E Test schreiben
   - Alle Datenpfade verifizieren

---

**Geprüft von:** Claude
**Datum:** 09.08.2025 17:50
**Backend:** Port 8080 ✅
**Frontend:** Port 5173 ✅