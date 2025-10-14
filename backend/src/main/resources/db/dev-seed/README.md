# DEV-SEED Migrations

**Erstellt:** 2025-10-13
**Status:** Production-Ready
**Zweck:** Development-only Testdaten für realistische User Experience

---

## 📋 Übersicht

Dieser Folder enthält Flyway-Migrations mit **realistischen Testdaten** für die Entwicklungsumgebung.

**Nummerierung:** V90000+ (deutlich erkennbar als DEV-only)

**Strategie:** Separate DEV-SEED Migrations statt Vermischung mit Production Migrations.

---

## 🗂️ Vorhandene Migrations

### V90001: Seed DEV Customers Complete
**Datei:** `V90001__seed_dev_customers_complete.sql`

**Inhalt:**
- 5 realistische Customer-Szenarien (IDs 90001-90005)
- Verschiedene Branchen: Hotel, Catering, Betriebskantine, Restaurant, Bäckerei
- Verschiedene Größen: 50-500 Mitarbeiter
- Territory-Zuordnungen: Berlin, München, Freiburg, Hamburg, Dresden
- BusinessTypes: GASTRONOMIE, CATERING, GEMEINSCHAFTSVERPFLEGUNG, BAECKEREI
- Vollständige Daten: Adressen, Kontaktpersonen, Notes, Email/Phone

**Szenarien:**
1. **90001** - Fresh Hotel GmbH (Berlin, Hotel, 200 MA)
2. **90002** - Catering Excellence AG (München, Catering, 150 MA)
3. **90003** - Campus Gastro Service (Freiburg, Betriebskantine, 500 MA)
4. **90004** - Restaurant Bella Vista (Hamburg, Restaurant, 50 MA)
5. **90005** - Bäckerei Müller KG (Dresden, Bäckerei, 80 MA)

### V90002: Seed DEV Leads Complete
**Datei:** `V90002__seed_dev_leads_complete.sql`

**Inhalt:**
- 10 Lead-Szenarien (IDs 90001-90010)
- 21 Lead Contacts (0-3 pro Lead, verteilt)
- 21 Lead Activities (CREATED + Business Activities)
- Score-Range: 21-59 (System Ceiling validiert)
- Verschiedene Stati: REGISTERED, LEAD, LOST, INTERESSENT

**Hot Leads:**
- **90003** - Hotel Seeblick (Score 59): ADVOCATE Relationship + EMERGENCY Urgency
- **90007** - Uni Mensa Frankfurt (Score 57): DIRECT Decision Maker + EMERGENCY Urgency

**Edge Cases:**
- **90006** - Catering BüroFit (PreClaim): Stage 0, keine Contacts, first_contact_documented_at = NULL
- **90005** - Bistro Alpenblick (Grace Period): 68 Tage alt, kritischer Protection-Status
- **90004** - Mensa Köln-Nord (LOST): Competitor Won, lost_reason gesetzt

**Score-Verteilung:**
- 21-30 Points: 3 Leads (Cold/Low Engagement)
- 31-40 Points: 3 Leads (Moderate Fit)
- 41-50 Points: 2 Leads (Good Potential)
- 51-59 Points: 2 Leads (Hot Leads)

---

## 🏗️ Architektur-Entscheidung

### Warum separater Folder?

**Problem:** Wo speichert man DEV-only Testdaten, die NICHT in Production laufen sollen?

**Lösung:** Separater Folder `db/dev-seed/` mit V90000+ Nummerierung

**Vorteile:**
- ✅ Klare Trennung Production vs. DEV-only Daten
- ✅ DEV-SEED Daten können einfach ausgeschlossen werden (Production)
- ✅ Bessere Organisation und Wartbarkeit
- ✅ Keine Konflikte mit Production Migrations (V10000+)

### Flyway Configuration

```properties
# application.properties
quarkus.flyway.locations=classpath:db/migration,classpath:db/dev-seed
```

Flyway lädt automatisch aus beiden Folders in der korrekten Reihenfolge (nach Versionsnummer).

---

## 🚀 Verwendung

### DEV-SEED in Development aktivieren

**Standard:** DEV-SEED Migrations werden automatisch geladen (siehe Flyway Config oben).

### DEV-SEED in Production deaktivieren

**Option 1:** Flyway locations anpassen (nur `db/migration`)
```properties
quarkus.flyway.locations=classpath:db/migration
```

**Option 2:** Environment-spezifische Configuration
```properties
# application-prod.properties
%prod.quarkus.flyway.locations=classpath:db/migration
```

### Datenbank komplett neu aufsetzen

```bash
# 1. Alle Daten löschen
PGPASSWORD=freshplan123 psql -h localhost -U freshplan_user -d freshplan_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# 2. Migrations neu ausführen
cd backend
./mvnw flyway:migrate

# 3. DEV-SEED Daten sind jetzt verfügbar (IDs 90001-90010)
```

---

## 📊 Daten-Übersicht

### Customers
- **IDs:** 90001-90005
- **Territories:** Berlin, München, Freiburg, Hamburg, Dresden
- **BusinessTypes:** GASTRONOMIE, CATERING, GEMEINSCHAFTSVERPFLEGUNG, BAECKEREI

### Leads
- **IDs:** 90001-90010
- **Score-Range:** 21-59 (System Ceiling: 60)
- **Hot Leads:** 90003 (59), 90007 (57)
- **Edge Cases:** PreClaim (90006), Grace Period (90005), LOST (90004)

### Contacts
- **Total:** 21 Lead Contacts
- **Verteilung:** 0-3 pro Lead
- **Realistisch:** Verschiedene Rollen (Geschäftsführer, Einkaufsleiter, etc.)

### Activities
- **Total:** 21 Lead Activities
- **Types:** CREATED + PHONE_CALL, EMAIL_SENT, MEETING, etc.
- **Timeline:** Letzte 90 Tage

---

## 🔧 Neue Migration hinzufügen

### Nächste Nummer ermitteln

```bash
# Automatisch (empfohlen)
./scripts/get-next-migration.sh
# Wähle Option 3: "db/dev-seed/"

# Manuell
ls -la backend/src/main/resources/db/dev-seed/ | tail -1
# Nächste Nummer: V90003
```

### Template

```sql
-- V90003__your_description_here.sql

-- DEV-SEED Migration: Your description
-- Created: YYYY-MM-DD
-- IDs: 900XX-900XX (DEV-SEED ID Range)

-- =============================================================================
-- IMPORTANT: This migration is DEV-only and will NOT run in production!
-- =============================================================================

-- Your SQL here...
```

### Best Practices

1. **ID-Range:** 90000+ für alle DEV-SEED Entities
2. **Realistische Daten:** Production-ähnliche Szenarien
3. **Dokumentation:** Header mit Beschreibung + ID-Range
4. **Edge Cases:** Verschiedene Stati/Stages abdecken
5. **Referenzen:** Foreign Keys korrekt setzen (z.B. territory_id, user_id)

---

## 📝 Referenzen

**Dokumentation:**
- [DEV_SEED_INFRASTRUCTURE_SUMMARY.md](../../../../docs/planung/features-neu/00_infrastruktur/migrationen/artefakte/DEV_SEED_INFRASTRUCTURE_SUMMARY.md)
- [Master Plan V5](../../../../docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md) - Session Log 2025-10-13
- [Production Roadmap](../../../../docs/planung/PRODUCTION_ROADMAP_2025.md) - Sprint 2.1.6.2

**Code:**
- [V90001 Migration](./V90001__seed_dev_customers_complete.sql)
- [V90002 Migration](./V90002__seed_dev_leads_complete.sql)

---

**✅ DEV-SEED Strategy etabliert am 2025-10-13**
