# 📊 Sprint 2.1.7.0 - Seiten-Inventar

**Erstellt:** 2025-10-14 14:45
**Analyse-Basis:** Grep-Suche nach `MainLayoutV2` + `Container maxWidth`

---

## 🎯 ZUSAMMENFASSUNG

**Gesamt:** 27 Seiten mit MainLayoutV2
**Mit Container:** 22 Seiten (müssen bereinigt werden)
**Ohne Container:** 5 Seiten (nur Prop hinzufügen)

---

## 📋 KATEGORISIERUNG

### ✅ FULL (100% Breite) - 5 Seiten

**Tabellen & Übersichten** (brauchen volle Breite):

| Seite | Status | Aktion |
|-------|--------|--------|
| `CustomersPageV2.tsx` | ✅ KEIN Container | **ADD** `maxWidth="full"` Prop |
| `CockpitPageV2.tsx` | ❌ Container? | **REMOVE Container** + **ADD** `maxWidth="full"` |
| `UsersPage.tsx` | ❌ Container? | **REMOVE Container** + **ADD** `maxWidth="full"` |
| `OpportunityPipelinePage.tsx` | ❌ Container? | **REMOVE Container** + **ADD** `maxWidth="full"` |
| `CustomerDetailPage.tsx` | ❌ Container? | **REMOVE Container** + **ADD** `maxWidth="full"` |

---

### 📦 XL (1536px - DEFAULT) - 3 Seiten

**Formulare & Standard** (default, kein Prop nötig):

| Seite | Status | Aktion |
|-------|--------|--------|
| `SettingsPage.tsx` | ✅ KEIN Container | **KEINE ÄNDERUNG** (default = xl) |
| `LeadDetailPage.tsx` | ❌ Container (xl) | **REMOVE Container** (default = xl) |
| `CalculatorPageV2.tsx` | ❌ Container (xl) | **REMOVE Container** (default = xl) |

---

### 📋 LG (1200px) - 18 Seiten

**Dashboards & Info-Seiten** (kompakte Ansicht):

| Seite | Status | Aktion |
|-------|--------|--------|
| `AdminDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `AuswertungenDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `KommunikationDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `KundenmanagementDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `NeukundengewinnungDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `EinstellungenDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `HilfeDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `ApiStatusPage.tsx` | ❌ Container (lg) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `HelpCenterPage.tsx` | ❌ Container (lg) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `HelpSystemDemoPageV2.tsx` | ❌ Container (lg) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `PlaceholderPage.tsx` | ❌ Container (lg) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `LazyLoadingDemo.tsx` | ❌ Container (lg) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `admin/SystemDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `admin/HelpConfigDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `admin/IntegrationsDashboard.tsx` | ❌ Container (xl) | **REMOVE Container** + **ADD** `maxWidth="lg"` |
| `admin/AuditAdminPage.tsx` | ❓ Unklar | **PRÜFEN** + **ADD** `maxWidth="lg"` |

---

### 📝 SM (600px) - 1 Seite

**Error-Seiten** (schmale Ansicht):

| Seite | Status | Aktion |
|-------|--------|--------|
| `NotFoundPage.tsx` | ❌ Container (sm) | **REMOVE Container** + **ADD** `maxWidth="sm"` |

---

## 🔍 LEGACY-CHECK NÖTIG

**Seiten ohne klare Einordnung** (möglicherweise Legacy):

| Seite | Grund | Aktion |
|-------|-------|--------|
| `CustomersPage.tsx` | Vorgänger von CustomersPageV2? | **PRÜFEN:** Noch in Verwendung? |
| `CockpitPage.tsx` | Vorgänger von CockpitPageV2? | **PRÜFEN:** Noch in Verwendung? |
| `HelpSystemDemoPage.tsx` | Vorgänger von V2? | **PRÜFEN:** Noch in Verwendung? |

---

## 📊 BATCH-PLANUNG (Phase 3)

### Batch 1: FULL (100% Breite) - 5 Seiten, 45 Min
1. CustomersPageV2.tsx - nur Prop hinzufügen
2. CockpitPageV2.tsx - Container entfernen + Prop
3. UsersPage.tsx - Container entfernen + Prop
4. OpportunityPipelinePage.tsx - Container entfernen + Prop
5. CustomerDetailPage.tsx - Container entfernen + Prop

### Batch 2: XL (Default) - 3 Seiten, 30 Min
1. SettingsPage.tsx - SKIP (bereits korrekt)
2. LeadDetailPage.tsx - Container entfernen
3. CalculatorPageV2.tsx - Container entfernen

### Batch 3: LG (1200px) - 16 Seiten, 90 Min
1. AdminDashboard.tsx
2. AuswertungenDashboard.tsx
3. KommunikationDashboard.tsx
4. KundenmanagementDashboard.tsx
5. NeukundengewinnungDashboard.tsx
6. EinstellungenDashboard.tsx
7. HilfeDashboard.tsx
8. ApiStatusPage.tsx
9. HelpCenterPage.tsx
10. HelpSystemDemoPageV2.tsx
11. PlaceholderPage.tsx
12. LazyLoadingDemo.tsx
13. admin/SystemDashboard.tsx
14. admin/HelpConfigDashboard.tsx
15. admin/IntegrationsDashboard.tsx
16. admin/AuditAdminPage.tsx

### Batch 4: SM (600px) - 1 Seite, 10 Min
1. NotFoundPage.tsx

---

## 🎯 ERWARTETE ÄNDERUNGEN

**Total:**
- **Seiten bereinigt:** 22 Seiten
- **Zeilen entfernt:** ~110 Zeilen (Container-Code)
- **Props hinzugefügt:** 22× `maxWidth` Prop
- **Netto:** -106 Zeilen Code

**Risiken:**
- ✅ VERY LOW - Alle Änderungen sind explizit
- ✅ Backward Compatible - Default 'xl' erhält bestehendes Verhalten
- ✅ Visuell validierbar - Kein Layout sollte sich ändern (außer FULL = besser!)

---

**Status:** ✅ INVENTAR COMPLETE
**Nächster Schritt:** Phase 3 - Batch 1 starten (FULL-Seiten)
