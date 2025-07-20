# 🧭 M1 NAVIGATION (KOMPAKT)

**Erstellt:** 17.07.2025 16:35  
**Status:** 🟡 40% FERTIG - MainLayoutV2 vorhanden, Navigation fehlt  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** ⭐ FOUNDATION - Basis für alle Features

## 🚨 BEI FRONTEND-ARBEIT:
```bash
./scripts/ui-development-start.sh --module=navigation
```

---

## 🧠 WAS WIR AUSBAUEN

**Realität:** MainLayoutV2 mit Basis-Layout bereits implementiert!  
**Basis:** MainLayoutV2.tsx + MUI AppBar + Drawer-Navigation  
**Enhancement:** Dynamische Navigation + Breadcrumbs + User-Context

## ⚠️ UI-KONSISTENZ BEACHTEN:
- Nutze `/docs/UI_SPRACHREGELN.md` für deutsche Begriffe
- Verwende Base Components aus `/frontend/src/components/base/`
- KEINE hardcoded Texte - immer `useTranslation` Hook

> **Live Code:** `/frontend/src/components/layout/MainLayoutV2.tsx` ✅  
> **Live URL:** http://localhost:5173/ ✅  
> **Layout funktioniert:** Header + Sidebar + Content Area ✅  

### 🎯 Enhancement-Vision:
1. **Dynamische Navigation** - Rolle-basierte Menüpunkte (neu)
2. **Breadcrumbs** - Kontext-Navigation für Deep-Links (neu)  
3. **User-Context** - Avatar + Quick-Actions + Notifications (neu)

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Bestehende Layout-Basis verstehen:**
```bash
# Live Navigation testen:
open http://localhost:5173/

# Code-Basis analysieren:
cat frontend/src/components/layout/MainLayoutV2.tsx | head -50

# Verfügbare Layout-Features:
ls frontend/src/components/layout/
# → MainLayoutV2.tsx ✅ (erweitern, nicht neu)
# → Navigation components ✅ (erweitern, nicht neu)
```

### 2. **Navigation Enhancement planen:**
```typescript
// Bestehende Navigation erweitern:
interface EnhancedNavigationData {
  // Bestehend (behalten):
  currentRoute: string;
  user: UserData;
  
  // NEU (hinzufügen):
  dynamicMenuItems: RoleBasedMenuItem[];
  breadcrumbs: BreadcrumbItem[];
  notifications: NotificationItem[];
  quickActions: QuickActionItem[];
}
```

### 3. **Rolle-basierte Navigation testen:**
```bash
# Current User Role prüfen:
curl http://localhost:8080/api/users/current | jq '.roles'
# Diese Rollen bestimmen Navigation
```

**Geschätzt: 2-3 Tage für Navigation Enhancement**

---

## 📋 WAS IST FERTIG?

✅ **Layout-Struktur** - MainLayoutV2 mit AppBar + Drawer  
✅ **MUI Integration** - Theme-kompatible Komponenten  
✅ **Responsive Design** - Mobile-first Ansatz  
✅ **Routing Integration** - React Router kompatibel  
✅ **User Context** - Basis-User-Daten verfügbar  

**🎯 BASIS IST SOLIDE! Jetzt intelligente Navigation hinzufügen.**

---

## 🚨 WAS FEHLT FÜR SMART NAVIGATION?

❌ **Rolle-basierte Menüs** → Dynamische Navigation basierend auf Permissions  
❌ **Breadcrumbs** → Context-Navigation für bessere UX  
❌ **Quick Actions** → Häufige Aktionen direkt aus Navigation  
❌ **Notifications** → Real-time Updates im Header  
❌ **Search Integration** → Global Search aus Navigation  

---

## 🔗 VOLLSTÄNDIGE DETAILS

**Direkter Implementation Guide:** `/docs/features/ACTIVE/05_ui_foundation/guides/M1_NAVIGATION_GUIDE.md`
- Phase 1: Rolle-basierte Navigation (Tag 1-2)
- Phase 2: Breadcrumbs System (Tag 3)
- Phase 3: Notifications (Tag 4)
- Vollständige Code-Beispiele + Copy-paste ready

**Navigation:** 
- **IMPLEMENTATION_GUIDE.md:** `/docs/features/ACTIVE/05_ui_foundation/IMPLEMENTATION_GUIDE.md` (Übersicht aller Module)
- **V5 Master Plan:** `/docs/CRM_COMPLETE_MASTER_PLAN_V5.md` (Gesamt-Roadmap)
- **Feature Overview:** `/docs/features/MASTER/FEATURE_OVERVIEW.md` (Status Dashboard)

**Entscheidungen:** `/docs/features/ACTIVE/05_ui_foundation/DECISION_LOG.md`
- Navigation-Style: Sidebar vs. Top-Navigation vs. Hybrid?
- Breadcrumb-Strategie: Auto-generiert vs. Manual vs. Hybrid?
- Search-Integration: Elastic vs. Database vs. Client-side?

---

## 📞 NÄCHSTE SCHRITTE

1. **Rolle-basierte Navigation** - Permissions-System in Navigation integrieren
2. **Breadcrumbs** - Context-Navigation für alle Routen implementieren  
3. **Search Integration** - Global Search aus Navigation
4. **Notifications** - Real-time Updates im Header

**WICHTIG:** Die Layout-Basis ist exzellent - wir machen sie intelligent!

**KRITISCH:** Status ist 40% fertig, nicht 0% geplant!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User Context & Authentication
- **[👥 FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)** - Rolle-basierte Navigation

### ⚡ Direkt integriert mit:
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - FAB Integration
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Haupt-Arbeitsbereich
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Navigation zu Settings

### 🚀 Ermöglicht Navigation zu:
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - /opportunities Route
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)** - /calculator Route
- **[👨‍💼 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - /dashboard Route
- **[📱 FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_KOMPAKT.md)** - Mobile Navigation Pattern

### 🎨 UI Components:
- **[🎯 FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_KOMPAKT.md)** - Command Palette Integration
- **[🔍 FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_KOMPAKT.md)** - Quick Actions in Header

### 🔧 Technische Details:
- [M1_NAVIGATION_GUIDE.md](./guides/M1_NAVIGATION_GUIDE.md) *(geplant)* - Schritt-für-Schritt Implementation
- [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) *(geplant)* - UI Foundation Übersicht
- [DECISION_LOG.md](./DECISION_LOG.md) *(geplant)* - Navigation Entscheidungen