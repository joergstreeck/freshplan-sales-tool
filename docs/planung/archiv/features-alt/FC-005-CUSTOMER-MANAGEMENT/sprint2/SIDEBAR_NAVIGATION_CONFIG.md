# 🎯 Sprint 2 - Sidebar Navigation Konfiguration

**Sprint:** Sprint 2  
**Datum:** 27.07.2025  
**Status:** ✅ Entschieden

---

## 📋 Navigation
**Parent:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**Previous:** [Lead-Kunde-Trennung](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/LEAD_CUSTOMER_SEPARATION_DECISION.md)  
**Next:** [Key Decisions Summary](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_KEY_DECISIONS_SUMMARY.md)  
**Related:** [Sidebar Discussion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SIDEBAR_LEAD_DISCUSSION.md) | [DAY1 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY1_IMPLEMENTATION.md)

---

## 🎯 Finale Sidebar-Struktur für Sprint 2

```typescript
// ⚠️ WICHTIG: import type für TypeScript!
import type { NavigationConfig } from '@/types/navigation';

export const sidebarConfig: NavigationConfig = {
  items: [
    {
      id: 'cockpit',
      label: '🏠 Mein Cockpit',
      path: '/cockpit',
      icon: 'Dashboard',
      // Keine Untermenüs - Top Level
    },
    {
      id: 'neukundengewinnung',
      label: '👤 Neukundengewinnung',
      icon: 'PersonAdd',
      children: [
        {
          id: 'email-inbox',
          label: 'E-Mail Posteingang',
          path: '/inbox',
          icon: 'Email'
        },
        {
          id: 'lead-erfassung',
          label: 'Lead-Erfassung',
          path: '/leads',
          icon: 'ContactMail',
          disabled: true,  // Sprint 2: Noch nicht implementiert
          tooltip: 'Verfügbar in Phase 2 (FC-020)'
        },
        {
          id: 'kampagnen',
          label: 'Kampagnen',
          path: '/campaigns',
          icon: 'Campaign'
        }
      ]
    },
    {
      id: 'kundenmanagement',
      label: '👥 Kundenmanagement',
      icon: 'People',
      children: [
        {
          id: 'alle-kunden',
          label: 'Alle Kunden',
          path: '/customers',
          icon: 'ViewList'
        },
        {
          id: 'neuer-kunde',
          label: 'Neuer Kunde',  // ✅ NEU in Sprint 2
          path: '/customers/new',
          icon: 'PersonAdd',
          // Alternativ: onClick Handler statt path
          onClick: () => openCustomerWizard()
        },
        {
          id: 'verkaufschancen',
          label: 'Verkaufschancen',
          path: '/opportunities',
          icon: 'TrendingUp'
        },
        {
          id: 'aktivitaeten',
          label: 'Aktivitäten',
          path: '/activities',
          icon: 'Event'
        }
      ]
    },
    {
      id: 'auswertungen',
      label: '📊 Auswertungen',
      icon: 'Analytics',
      children: [
        {
          id: 'umsatz',
          label: 'Umsatzübersicht',
          path: '/reports/revenue',
          icon: 'AttachMoney'
        },
        {
          id: 'kundenanalyse',
          label: 'Kundenanalyse',
          path: '/reports/customers',
          icon: 'PieChart'
        },
        {
          id: 'aktivitaetsberichte',
          label: 'Aktivitätsberichte',
          path: '/reports/activities',
          icon: 'Assessment'
        }
      ]
    },
    {
      id: 'einstellungen',
      label: '⚙️ Einstellungen',
      path: '/settings',
      icon: 'Settings',
      // Keine Untermenüs - Top Level
    }
  ]
};
```

## 🎯 Zusätzliche Quick-Action Buttons

Ergänzend zur Sidebar gibt es Quick-Action Buttons im Header:

```typescript
// components/layout/HeaderQuickActions.tsx
export const HeaderQuickActions = () => {
  return (
    <Box sx={{ display: 'flex', gap: 1 }}>
      <Button
        startIcon={<AddIcon />}
        onClick={() => openCustomerWizard()}
        variant="contained"
        size="small"
      >
        Neuer Kunde
      </Button>
      <Button
        startIcon={<TrendingUpIcon />}
        onClick={() => openOpportunityWizard()}
        variant="outlined"
        size="small"
      >
        Neue Chance
      </Button>
      <IconButton>
        <NotificationsIcon />
      </IconButton>
    </Box>
  );
};
```

## 📊 Implementierungs-Details

### 1. **Sidebar Component**
```typescript
// components/layout/Sidebar.tsx
import type { NavigationConfig } from '@/types/navigation';
import { sidebarConfig } from '@/config/navigation';

export const Sidebar = () => {
  const location = useLocation();
  
  return (
    <Drawer variant="permanent" sx={{ width: 240 }}>
      <List>
        {sidebarConfig.items.map(item => (
          <SidebarItem key={item.id} item={item} />
        ))}
      </List>
    </Drawer>
  );
};
```

### 2. **Keyboard Shortcuts**
```typescript
// Globale Shortcuts ergänzen die Navigation
useKeyboardShortcut('ctrl+n', () => openCustomerWizard());
useKeyboardShortcut('ctrl+o', () => openOpportunityWizard());
useKeyboardShortcut('ctrl+k', () => openCommandPalette());
```

## 🎯 Wichtige Entscheidungen

1. **"Neuer Kunde" unter Kundenmanagement** ✅
   - Logische Gruppierung
   - Folgt CRM-Standards

2. **Lead-Erfassung disabled in Sprint 2** ✅
   - Kommt in Phase 2 (FC-020)
   - Platzhalter zeigt zukünftige Struktur

3. **Quick-Actions im Header** ✅
   - Ergänzt Sidebar für Power-User
   - Prominente Platzierung häufiger Aktionen

4. **Keyboard Shortcuts** ✅
   - Produktivität für erfahrene Nutzer
   - Standard-Shortcuts (Ctrl+N für "Neu")

---

**Status:** Verbindlich für Sprint 2 Implementation