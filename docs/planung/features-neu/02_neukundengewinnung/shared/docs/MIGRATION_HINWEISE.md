# ⚠️ MIGRATION-NUMMER-HINWEISE

## 🚨 KRITISCH: Variable Migration-Nummern

**Problem:** Migration-Nummern ändern sich zwischen Planung und Implementation!

**Aktueller Stand (2025-09-19):** V225 war frei
**Bei Implementation:** Neue Nummer ermitteln!

## 🔧 KORREKTE VORGEHENSWEISE:

### SCHRITT 1: Aktuelle Nummer ermitteln
```bash
./scripts/get-next-migration.sh
```

### SCHRITT 2: Migration-File umbenennen
```bash
# Von Template:
V225__create_lead_table.sql

# Zu aktueller Nummer (Beispiel):
V237__create_lead_table.sql
```

### SCHRITT 3: File-Header anpassen
```sql
-- VXXX: Create Lead Management Tables (NUMMER AKTUALISIEREN!)
-- ↓ ÄNDERN ZU:
-- V237: Create Lead Management Tables
```

## 📋 BETROFFENE DATEIEN:
- `V225__create_lead_table.sql` → umbenennen
- `README_PATCH.md` → Anweisungen beachten
- Alle Dokumentation → Migration-Nummer als variabel behandeln

## ✅ BEST PRACTICE:
**IMMER vor DB-Arbeit:** `./scripts/get-next-migration.sh` ausführen!