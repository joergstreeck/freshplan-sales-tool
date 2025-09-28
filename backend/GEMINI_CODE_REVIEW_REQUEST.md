# 🔍 Code Review Request für Gemini

## 📅 Datum: 28.09.2025
## 🎯 Sprint: 2.1.4 - Lead Deduplication & Data Quality
## 📁 Branch: feat/mod02-backend-sprint-2.1.4
## ⏱️ CI Status: In Progress (Run #18080191291)

## 🚀 Übersicht der Änderungen

### 1. **Lead Normalization & Deduplication** ✅
Implementierung einer robusten Lead-Deduplizierung mit Normalisierung.

#### Neue Dateien:
- `LeadNormalizationService.java` - Normalisierung von Email, Telefon, Firmennamen
- `IdempotencyService.java` - Request-Deduplizierung mit SHA-256 Hashing
- `LeadDeduplicationService.java` - Deduplizierungs-Logik mit Fuzzy-Matching

#### Migrations:
- `V247__leads_normalization_deduplication.sql` - Neue Spalten und Idempotency-Tabelle
- `V248__leads_unique_indexes_simple.sql` - Unique Indexes für Deduplizierung

### 2. **CI/CD Pipeline Fixes** 🔧
Behebung kritischer CI-Probleme die durch Sprint-Änderungen entstanden.

#### Probleme gelöst:
- Database Growth Check Workflow-Fehler
- Foreign Key Constraint Violations in Tests
- Test-Isolation und Self-Contained Tests
- YAML-Syntax-Fehler in GitHub Actions

### 3. **Test-Verbesserungen** 🧪
Tests sind jetzt vollständig selbstständig ohne Seed-Daten-Abhängigkeiten.

#### ⚠️ WICHTIGE ÄNDERUNG: V10005 Seed-Migration deaktiviert
- **`V10005__seed_sample_customers.sql`** → **`V10005__seed_sample_customers.sql.disabled`**
- **KEINE Seed-Kunden mehr in der Datenbank**
- **Grund:** Tests sollen self-contained sein und ihre eigenen Daten erstellen/aufräumen
- **Impact:** Alle Tests die Seed-Daten erwarteten wurden angepasst oder entfernt

#### Gelöschte Tests:
- `A01_SeedSmokeTest.java` - Erwartete 20 Seed-Kunden
- `SeedDataVerificationTest.java` - Verifizierte Seed-Daten
- `SimpleSeedTest.java` - Testete Seed-Migration
- `MID_SeedDataCheckTest.java` - Mittlerer Seed-Check

#### Angepasste Tests:
- `OpportunityServiceStageTransitionTest.java` - Erstellt eigene Test-Daten mit unique IDs
- `A00_EnvDiagTest.java` - Erwartet jetzt LEERE Datenbank (customerCount == 0)

## 📝 Wichtige Implementierungs-Details

### Lead Normalization Service
```java
@ApplicationScoped
public class LeadNormalizationService {

    public String normalizeEmail(String email) {
        // Lowercase, trim, remove dots from local part (Gmail-style)
        // Handle plus-addressing
    }

    public String normalizePhoneNumber(String phone) {
        // E.164 format conversion
        // Deutsche Nummern: +49 Präfix
    }

    public String normalizeCompanyName(String name) {
        // Remove GmbH, AG, etc.
        // Normalize whitespace
        // Remove special characters
    }
}
```

### Idempotency Service
```java
@ApplicationScoped
public class IdempotencyService {

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    public String generateKey(Object... parts) {
        // SHA-256 hashing
        // Combines multiple request parts
    }

    public boolean isDuplicate(String tenantId, String key) {
        // Check within TTL window
        // Automatic cleanup of old keys
    }
}
```

### Database Schema Änderungen
```sql
-- Neue normalisierte Spalten
ALTER TABLE leads ADD COLUMN email_normalized TEXT;
ALTER TABLE leads ADD COLUMN phone_e164 TEXT;
ALTER TABLE leads ADD COLUMN company_name_normalized TEXT;

-- Idempotency Keys Tabelle
CREATE TABLE idempotency_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(50) DEFAULT 'default',
    idempotency_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    request_hash VARCHAR(64),
    UNIQUE(tenant_id, idempotency_key)
);

-- Unique Indexes für Deduplizierung
CREATE UNIQUE INDEX uq_leads_email_canonical_v2
    ON leads(email_normalized)
    WHERE email_normalized IS NOT NULL
    AND is_canonical = true
    AND status != 'DELETED';
```

## ⚠️ Kritische Punkte zur Review

### 1. **Normalisierungs-Strategie**
- Ist die Email-Normalisierung (Gmail-Style) für alle Use-Cases geeignet?
- Phone-Normalisierung: Nur deutsche Nummern oder international?
- Company-Name: Weitere Suffixe die entfernt werden sollten?

### 2. **Idempotency TTL**
- 24 Stunden TTL angemessen?
- Cleanup-Strategie für alte Keys optimal?

### 3. **Test-Isolation**
- Alle Tests erstellen eigene Daten - Performance-Impact?
- Customer-Number auf 13 Zeichen limitiert - ausreichend?
- **Keine Seed-Daten mehr** - Richtige Entscheidung?
- Alternative: Test-Profile mit/ohne Seeds?

### 4. **Index-Strategie**
- Partial Indexes nur für `is_canonical = true` - korrekt?
- Performance bei großen Datenmengen?

## 🔄 Migration von Bestandsdaten

### Offene Frage:
Wie sollen Bestandsdaten migriert werden?
- Batch-Job für Normalisierung?
- Trigger-basierte Migration?
- Lazy Migration bei nächstem Update?

## 🧪 Test Coverage

- Lead Normalization: ✅ Unit Tests
- Idempotency Service: ✅ Unit Tests
- Lead Deduplication: ✅ Integration Tests
- Test Isolation: ✅ Self-contained tests ohne Seed-Daten
- Transaction Management: ✅ Korrekte Transaktionsgrenzen
- CI/CD Pipeline: 🔄 In Progress (E2E ✅, Backend läuft)

## 📊 Performance-Überlegungen

### Indexes:
- `email_normalized` - B-Tree Index
- `phone_e164` - B-Tree Index
- `company_name_normalized` - B-Tree Index
- Composite Index auf `(email_normalized, is_canonical, status)`?

### Caching:
- Idempotency Keys im Memory-Cache?
- Normalisierte Werte cachen?

## 🚨 Breaking Changes

### Production Code:
**KEINE** - Alle Änderungen sind rückwärtskompatibel:
- Neue Spalten sind nullable
- Bestehende APIs unverändert
- Migration optional

### Test-Umgebung:
**⚠️ BREAKING:** Keine Seed-Daten mehr!
- V10005 Migration deaktiviert
- Tests die Kunden erwarten müssen diese selbst erstellen
- Entwickler müssen lokale Test-Daten selbst anlegen oder Test-Scripts verwenden

## ✅ Checklist für Review

- [ ] Normalisierungs-Logik korrekt und vollständig?
- [ ] Idempotency-Implementierung thread-safe?
- [ ] Datenbank-Indexes optimal?
- [ ] Test-Coverage ausreichend?
- [ ] Performance bei großen Datenmengen bedacht?
- [ ] Fehlerbehandlung robust?
- [ ] Logging ausreichend?
- [ ] Dokumentation vollständig?

## 💡 Vorschläge erwünscht für:

1. **Fuzzy-Matching Algorithmus** für Company Names
2. **Internationale Phone-Normalisierung**
3. **Batch-Processing** für Bestands-Migration
4. **Monitoring & Alerting** für Duplikate
5. **A/B Testing** der Deduplizierungs-Schwellwerte

## 📈 Metriken nach Deployment

Folgende Metriken sollten überwacht werden:
- Anzahl erkannter Duplikate
- False-Positive Rate
- Normalisierungs-Performance
- Idempotency Cache Hit Rate

## 📝 Letzte Fixes (28.09.2025 23:40)

### Test-Transaktions-Management:
- `@Transactional` aus inneren Test-Klassen entfernt (CDI Limitation)
- `entityManager.clear()` und reload Pattern für Cross-Transaction Visibility
- `em.refresh()` durch `em.find()` ersetzt (Entity not managed Fehler)

### Test-Daten-Initialisierung:
- Lead Follow-up Flags explizit auf `false` gesetzt
- Opportunity mit allen Required Fields erstellt
- Customer-Number auf 13 Zeichen begrenzt

## 🔗 Relevante Links

- PR: #123 feat/mod02-backend-sprint-2.1.4
- CI Run: https://github.com/joergstreeck/freshplan-sales-tool/actions/runs/18080191291
- Jira: FRESH-2024-MOD02-SPRINT-2.1.4
- Design Doc: `/docs/planung/features-neu/02_neukundengewinnung/`

---

**Bitte um Review folgender Aspekte:**
1. **Correctness** - Logik-Fehler, Edge Cases
2. **Performance** - Skalierbarkeit, Index-Strategie
3. **Security** - Input Validation, SQL Injection
4. **Maintainability** - Code-Qualität, Testbarkeit
5. **Business Logic** - Erfüllung der Requirements

Vielen Dank für das Review! 🙏