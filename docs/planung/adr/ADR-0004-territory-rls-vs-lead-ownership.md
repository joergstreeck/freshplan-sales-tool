# ADR-0004: Territory = RLS-Datenraum, Lead-Protection = User-based

**Status:** Akzeptiert
**Datum:** 12.09.2025
**Autor:** Development Team

## Kontext

Bei der CRM-Implementierung entstand Verwirrung über "Territory-Management":
- Ursprünglich interpretiert als **Gebietsschutz** (geografische Exklusivität)
- Tatsächliche Anforderung: **Datenraumtrennung** (DE/CH/AT) + **Lead-Protection**

Klarstellung der Begriffe und Implementierung nötig.

## Entscheidung

Wir trennen klar zwischen:

### **Territory = RLS-Datenraum:**
- Datenraumtrennung Deutschland/Schweiz/Österreich
- Row-Level-Security (RLS) für geografische Datenisolation
- Währung, MwSt, Compliance je Territory
- **KEIN Gebietsschutz zwischen Vertriebsmitarbeitern**

### **Lead-Protection = User-basiertes Ownership:**
- 6M Lead-Exklusivität für Lead-Ersteller
- 60T Kontakt-Protection bei aktivem Engagement
- 10T Stop-Clock bei Inaktivität
- **Deutschland-weit verfügbare Leads** (kein geografischer Schutz)

## Begründung

### Pro Begriffstrennung:
- **Klarheit:** Keine Vermischung von Datenraum und Gebietsschutz
- **Flexibilität:** Lead-Protection unabhängig von Geografie
- **Business-Requirement:** Deutschland-weite Lead-Verfügbarkeit gewünscht
- **Compliance:** RLS für länder-spezifische Regulations

### Contra Gebietsschutz:
- **Business-Wunsch:** Alle Leads sollen deutschlandweit verfügbar sein
- **Flexibilität:** Vertriebsmitarbeiter sollen überregional arbeiten können
- **Komplexität:** Geografischer Schutz wäre zusätzliche Business-Logic

## Konsequenzen

### Positive:
- Klare Begriffstrennung verhindert Missverständnisse
- Flexible Lead-Workflows ohne geografische Constraints
- Einfachere Business-Logic
- Compliance durch RLS-Datenraumtrennung

### Negative:
- Dokumentation muss Territory-Begriff präzise definieren
- Möglicherweise spätere Anforderung für echten Gebietsschutz
- Team muss Begriffe internalisieren

### Mitigationen:
- Glossar mit klarer Definition von Territory vs Lead-Protection
- Dokumentation in allen relevanten Modulen
- Training für Team über korrekte Begrifflichkeiten

## Implementation Details

### RLS-Territory (Datenraum):
```sql
-- Territory-Policy für Datenraumtrennung
CREATE POLICY territory_isolation ON leads
FOR ALL TO app_user
USING (territory = current_setting('app.territory'));

-- Territories: 'DE', 'CH', 'AT'
```

### Lead-Protection (User-based):
```sql
-- Lead-Ownership-Policy
CREATE POLICY lead_ownership ON leads
FOR ALL TO app_user
USING (
  created_by = current_user_id() OR
  last_contact + INTERVAL '60 days' < NOW() OR
  (last_activity + INTERVAL '10 days' < NOW() AND activity_count = 0)
);
```

### Business Rules:
- **Territory:** `user.territory IN ['DE', 'CH', 'AT']` bestimmt Datenraum
- **Lead-Protection:** User-basiert, territorial neutral
- **Währung/MwSt:** Territory-spezifisch (EUR/CHF, 19%/7.7%)

## Alternativen

1. **Echter Gebietsschutz:** Abgelehnt, Business will deutschland-weite Leads
2. **Keine Lead-Protection:** Abgelehnt, Chaos bei paralleler Bearbeitung
3. **Global ohne RLS:** Abgelehnt, Compliance-Probleme

## Compliance

- **RLS-Policies:** `rls_policies_active = 1` für Territory-Isolation
- **Lead-Protection:** `lead_ownership_violations = 0` in Contract-Tests
- **Audit-Trail:** Territoriale Datenzugriffe geloggt
- **Documentation:** Glossar mit Territory vs Lead-Protection Definition

## Sprint 2.1 Implementation Update (2025-09-25)

### Umgesetzte Entscheidungen:
- **Territory ohne Gebietsschutz:** Implementiert in V229 Migration
- **UserLeadSettingsService:** Thread-sicherer Service statt statischer Methoden
- **ElementCollection Tables:** V231 Migration für Join-Tabellen
- **Dev/Prod Trennung:** Flyway Locations strikt getrennt

### Implementierte Tabellen:
- `territories`: Currency, Tax, Business Rules
- `leads`: User-based Protection implementiert
- `user_lead_settings`: Präferenzen ohne geografische Restriktion
- `lead_activities`: Activity Tracking für Protection-Rules

### Bestätigte Trade-offs:
- ✅ Keine geografische Lead-Exklusivität - bewusst akzeptiert
- ✅ Protection rein user-basiert - funktioniert wie geplant
- ✅ Territory nur für Currency/Tax - vereinfacht Business Logic
- ✅ ETag/Cache-Semantik klar definiert

### Referenz:
- PR #103: Territory Management Implementation
- Migration V229-V231: Vollständige Umsetzung