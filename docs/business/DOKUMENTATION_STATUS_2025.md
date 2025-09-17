# 📝 Dokumentations-Status nach Rabattlogik-Änderung

**Stand:** 17.09.2025
**Grund:** Komplette Änderung der Rabattlogik ab 01.10.2025

## 🔄 Überblick der Änderungen

### Was entfällt:
- ❌ Bestellwert-basierte Rabatte
- ❌ Frühbucher-Rabatte (Vorlaufzeit)
- ❌ Abhol-Rabatt
- ❌ Partnerschaftsvereinbarung
- ❌ Alter Calculator

### Was kommt neu:
- ✅ Jahresumsatz-basierte Rabattstufen (2-10%)
- ✅ Jährliche Rückergütung (1-5%)
- ✅ Welcome-Bonus für Neukunden
- ✅ Skonto bei Lastschrift (1%)
- ✅ Nur noch AGBs (keine Partnerschaftsvereinbarung)

## 📁 Bearbeitete Dokumente

### ✅ Neue Dokumentation erstellt:
- `/docs/business/rabattlogik_2025_NEU.md` - Komplette neue Rabattlogik

### ⚠️ Als veraltet markiert:
- `/docs/business/freshplan_summary.md` - Mit Hinweis auf neue Logik versehen
- `/docs/features/FC-002-M8-rechner.md` - Calculator-Modul als OBSOLET markiert
- `/docs/CRM_COMPLETE_MASTER_PLAN.md` - Rabattrechner als veraltet markiert

### 🔍 Noch zu prüfen:
- [ ] `/docs/technical/API_CONTRACT.md` - Calculator-Endpoints dokumentieren
- [ ] `/docs/adr/` - Architecture Decision Record für neue Rabattlogik
- [ ] `/docs/features/FC-002-anhang-B-backend.md` - Backend-Anpassungen planen
- [ ] Weitere Feature-Dokumente auf Calculator-Referenzen prüfen

## 🛠️ Implementierungs-Hinweise

### Backend-Anpassungen (später in neuem Feature-Branch):
1. **Neue Services benötigt:**
   - `CustomerTierService` - Verwaltung der Rabattstufen
   - `AnnualRevenueTracker` - Jahresumsatz-Tracking
   - `WelcomeBonusService` - Neukunden-Bonus-Verwaltung
   - `RebateCalculationService` - Rückergütungs-Berechnung

2. **Datenbank-Änderungen:**
   - Neue Tabelle: `customer_revenue_tiers`
   - Neue Tabelle: `annual_rebates`
   - Neue Felder in `customers`: `tier_level`, `annual_revenue`, `welcome_bonus_expires`

3. **Zu entfernende Services:**
   - `CalculatorService` (alter Bestellwert-Calculator)
   - `PartnershipAgreementService`

### Frontend-Anpassungen (später):
1. **Neue Komponenten:**
   - `CustomerTierDisplay` - Anzeige der aktuellen Rabattstufe
   - `RevenueProgressBar` - Fortschritt zur nächsten Stufe
   - `RebateOverview` - Übersicht über Rabatte und Rückergütungen

2. **Zu entfernen:**
   - `CalculatorForm`, `CalculatorResults` (alte Calculator-UI)
   - Alle Calculator-bezogenen Seiten und Routen

## 📅 Nächste Schritte

1. **Sofort:** Dokumentation weiter bereinigen
2. **Demnächst:** Technisches Konzept für neue Rabattlogik erstellen
3. **Später:** Implementation in neuem Feature-Branch
4. **Vor Go-Live:** Migration der Testdaten auf neue Struktur

## 💡 Wichtige Hinweise

- **Keine Live-Kunden:** Da noch keine echten Kunden existieren, ist keine Migration nötig
- **Clean Slate:** Wir können direkt mit dem neuen System starten
- **Priorität:** Dokumentation vor Code-Änderungen
- **Feature-Branch:** Alle Code-Änderungen später in eigenem Branch