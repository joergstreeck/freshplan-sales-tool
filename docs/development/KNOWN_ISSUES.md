# Known Issues - FreshPlan 2.0

**📅 Aktuelles Datum: <!-- AUTO_DATE --> (System: 08.06.2025)**

**Letzte Aktualisierung:** 08.06.2025

## 🐛 Aktuelle bekannte Probleme

### Frontend

#### 1. Keycloak Integration noch nicht implementiert
- **Status:** In Entwicklung
- **Workaround:** Dev-Login unter `/login-bypass`
- **Geplante Lösung:** Sprint 2

### Backend

#### 1. Dev-Modus ohne Security
- **Status:** By Design
- **Details:** `%dev.quarkus.oidc.enabled=false` für einfacheres Development
- **Produktion:** Automatisch aktiviert

### Legacy

#### 1. Übersetzung dynamischer Tabs
- **Status:** Bekanntes Problem aus Legacy-Code
- **Workaround:** Wird in Phase 2 gesucht
- **Priorität:** Niedrig

#### 2. Performance bei großen Datenmengen
- **Status:** Bei sehr vielen Positionen im Calculator
- **Details:** Verzögerungen bei > 100 Locations
- **Geplante Lösung:** Optimierungen für spätere Phasen

## ✅ Gelöste Probleme

### ~~Calculator API ungeschützt~~ (08.06.2025)
- **Gelöst:** `@RolesAllowed` hinzugefügt
- **Commit:** Calculator Security Fix

### ~~Widersprüchliche Rollendefinitionen~~ (08.06.2025)
- **Gelöst:** "viewer" Rolle entfernt, einheitlich 3 Rollen
- **Details:** Siehe ROLE_CLEANUP_SUMMARY.md

## 📝 Problem melden

Neue Issues bitte in GitHub erstellen oder im Team-Sync ansprechen.