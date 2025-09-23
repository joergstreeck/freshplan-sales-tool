# 🔍 KI-Review Fixes für Sprint 1.1 CQRS Light Foundation

**Datum:** 2025-09-23
**PR:** #94
**Status:** In Bearbeitung

## 📋 Übersicht

Finale KI-Review hat 3 Must-Fix und 5 Should-Fix Items identifiziert.
Wir setzen die kritischen Items JETZT um, dokumentieren alles für später.

## 🔴 MUST-FIX Items (vor Merge)

### 1. ✅ PostgreSQL NOTIFY 8KB Limit
**Problem:** Config erlaubt 10KB, PostgreSQL max 8KB
**Lösung:**
- Payload-Size auf 7900 Bytes reduzieren
- Validation in EventPublisher und DB-Trigger
**Status:** UMGESETZT

### 2. ✅ UUID-Extension pgcrypto
**Problem:** gen_random_uuid() braucht pgcrypto
**Lösung:** CREATE EXTENSION IF NOT EXISTS pgcrypto
**Status:** UMGESETZT

### 3. ✅ Causation-ID Unique Index
**Problem:** Keine Dubletten-Protection
**Lösung:** Unique Index auf causation_id WHERE NOT NULL
**Status:** UMGESETZT

### 4. ✅ Dev-Seeds Profil-Trennung
**Problem:** Dev-Migrations könnten in Prod laufen
**Lösung:** Flyway locations nur in dev-Profile
**Status:** UMGESETZT

## 🟡 SHOULD-FIX Items (für später)

### 1. Event-Type Regex erweitern
**Wann:** Bei Bedarf
**Was:** Zahlen/Unterstriche/Bindestriche erlauben
**Ticket:** Erstellen wenn erste Events diese brauchen

### 2. Quarkus @Scheduled Migration
**Wann:** Sprint 3 (Refactoring-Slot)
**Was:** ExecutorService → @Scheduled
**Benefit:** Besseres Lifecycle-Management

### 3. Observability Hooks
**Wann:** Sprint 3 (mit Monitoring-Stack)
**Was:** Last-NOTIFY-Time, Event-Lag Metriken
**Benefit:** Better insights

### 4. Mock-Guard Scope
**Wann:** Wenn False-Positives auftreten
**Was:** Auf Frontend beschränken
**Status:** Beobachten

## 📝 Implementierungs-Details

### Migration V225 Änderungen:
```sql
-- Neu am Anfang:
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Neuer Index:
CREATE UNIQUE INDEX idx_domain_events_causation_unique
  ON domain_events(causation_id)
  WHERE causation_id IS NOT NULL;

-- Trigger mit Size-Check:
IF octet_length(event_json::text) > 7900 THEN
  RAISE WARNING 'Event payload too large: % bytes', octet_length(event_json::text);
  -- Kompakter Pointer statt volles Event
END IF;
```

### Application Properties:
```properties
# Von 10240 auf 7900 reduziert
cqrs.events.max-payload-size=7900

# Dev-Profile only:
%dev.quarkus.flyway.locations=classpath:db/migration,classpath:db/dev-migration
%prod.quarkus.flyway.locations=classpath:db/migration
```

### EventPublisher.java:
- Payload-Size Check auf 7900
- Bessere Error Message bei Überschreitung

## 🚀 Deployment-Hinweise

1. Migration V225 updated - pgcrypto Extension Check
2. Dev-Seeds nur noch in Dev-Environment
3. Payload-Limit produktionssicher
4. Causation-ID Protection aktiv

## 📊 Impact-Analyse

**Positive Impacts:**
- ✅ Keine stillen Failures bei großen Events
- ✅ Robuste UUID-Generierung
- ✅ Idempotenz-Protection ready
- ✅ Prod-Sicherheit erhöht

**Keine Breaking Changes:**
- Bestehende Events funktionieren weiter
- Nur zusätzliche Sicherheits-Checks

## 🔄 Follow-Up Tasks

Erstelle JIRA-Tickets für Should-Fix Items:
- [ ] FRESH-XXX: Event-Type Regex erweitern (wenn benötigt)
- [ ] FRESH-XXX: Migrate to @Scheduled (Sprint 3)
- [ ] FRESH-XXX: Add CQRS Observability Metrics (Sprint 3)

---
*Dokumentiert am 2025-09-23 nach finaler KI-Review*