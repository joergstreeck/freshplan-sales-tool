# 🚨 KRITISCHER ARCHITEKTUR-FIX: Einheitliches Layout-System

**Datum:** 27.07.2025  
**Priorität:** KRITISCH - Muss SOFORT behoben werden  
**Erstellt von:** Jörg & Claude  
**Problem identifiziert:** Sprint 2, Tag 1 Implementation

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**→ Quick Fix:** [CRITICAL FIX Guide](/Users/joergstreeck/freshplan-sales-tool/docs/CRITICAL_FIX_MAINLAYOUT_MISSING.md)  
**↑ Parent:** [FC-005 Customer Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)  
**📚 Related:** [DAY1 Implementation](./DAY1_IMPLEMENTATION.md) | [Sidebar Discussion](./SIDEBAR_LEAD_DISCUSSION.md)

---

## 🔴 DAS PROBLEM

### Screenshots zeigen folgende kritische Fehler:

1. **Fehlende Sidebar bei "Neuer Kunde"**
   - Nutzer kann nicht zurück navigieren (nur Browser-Back)
   - Komplett aus dem Navigationskontext gefallen

2. **Redundante Kundenanzeige**
   - Kundenliste wird im "Neuer Kunde"-Formular angezeigt
   - Obwohl Sidebar-Menüpunkt "Alle Kunden" existiert

3. **Inkonsistentes Layout**
   - Manche Seiten nutzen MainLayoutV2 (Cockpit ✅)
   - Andere Seiten ignorieren es komplett (CustomersPageV2 ❌)

4. **Route-basierte Wizard-Navigation**
   - `/customers/new` als separate Route statt Modal
   - Widerspricht der dokumentierten Sprint 2 Planung

---

## 📊 IST-ZUSTAND ANALYSE

### Aktuelle fehlerhafte Implementierung:

```typescript
// ❌ FALSCH - CustomersPageV2.tsx
export function CustomersPageV2() {
  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Eigenes Layout OHNE MainLayoutV2 */}
      <CustomerListHeader />
      <Box>
        <CustomerTable />
      </Box>
      <CustomerOnboardingWizard />
    </Box>
  );
}

// ❌ FALSCH - providers.tsx Zeile 83
<Route path="/customers/new" element={<CustomersPageV2 openWizard={true} />} />
```

### Korrekte Implementierung (nur im Cockpit):

```typescript
// ✅ RICHTIG - CockpitPage.tsx
export function CockpitPage() {
  return (
    <MainLayoutV2>
      <SalesCockpitV2 />
    </MainLayoutV2>
  );
}
```

---

## 🎯 SOLL-ZUSTAND (laut Dokumentation)

### Aus Sprint 2 Planning (DAY1_IMPLEMENTATION.md):
- **CustomerOnboardingWizard als Modal/Drawer** (Zeile 207)
- **Sidebar bleibt IMMER sichtbar**
- **Keine separate Route für Wizard**
- **Button öffnet Modal, keine Navigation**

### Aus Sidebar-Diskussion (SIDEBAR_LEAD_DISCUSSION.md):
> "Die Sidebar ist unser navigation backbone, darf niemals verschwinden"

### Aus Frontend Architecture Docs:
- Einheitliches Layout mit MainLayoutV2
- Konsistente User Experience
- Theme-First Approach

---

## 🛠️ LÖSUNGSPLAN

### Phase 1: Layout-Standardisierung (2h)

#### 1.1 CustomersPageV2 korrigieren
```typescript
// frontend/src/pages/CustomersPageV2.tsx
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export function CustomersPageV2() {
  const [wizardOpen, setWizardOpen] = useState(false);
  
  return (
    <MainLayoutV2>
      {/* Inhalt OHNE eigenes Layout */}
      <CustomerListContent onAddCustomer={() => setWizardOpen(true)} />
      
      {/* Wizard als Modal */}
      <CustomerOnboardingWizardModal
        open={wizardOpen}
        onClose={() => setWizardOpen(false)}
      />
    </MainLayoutV2>
  );
}
```

#### 1.2 Standard Page Template erstellen
```typescript
// frontend/src/templates/StandardPageTemplate.tsx
/**
 * VERBINDLICHES TEMPLATE für ALLE Seiten in FreshPlan 2.0
 * 
 * REGEL: Jede Seite MUSS MainLayoutV2 verwenden!
 * Ausnahmen: Login, Error Pages
 */
export const StandardPageTemplate = () => {
  return (
    <MainLayoutV2>
      {/* Page specific content */}
    </MainLayoutV2>
  );
};
```

### Phase 2: Wizard als Modal (3h)

#### 2.1 CustomerOnboardingWizardModal erstellen
```typescript
// frontend/src/features/customers/components/wizard/CustomerOnboardingWizardModal.tsx
import { Dialog, DialogContent, IconButton } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface Props {
  open: boolean;
  onClose: () => void;
  onComplete: (customer: Customer) => void;
}

export function CustomerOnboardingWizardModal({ open, onClose, onComplete }: Props) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="lg"
      fullWidth
      PaperProps={{
        sx: {
          height: '90vh',
          maxHeight: '900px'
        }
      }}
    >
      <IconButton
        onClick={onClose}
        sx={{ position: 'absolute', right: 8, top: 8 }}
      >
        <CloseIcon />
      </IconButton>
      
      <DialogContent>
        <CustomerOnboardingWizard
          onComplete={(customer) => {
            onComplete(customer);
            onClose();
          }}
          onCancel={onClose}
        />
      </DialogContent>
    </Dialog>
  );
}
```

#### 2.2 Route /customers/new entfernen
```typescript
// frontend/src/providers.tsx
// ENTFERNEN: <Route path="/customers/new" element={<CustomersPageV2 openWizard={true} />} />
```

#### 2.3 Sidebar-Menüpunkt anpassen
```typescript
// frontend/src/config/navigation.ts
{
  id: 'new-customer',
  label: 'Neuer Kunde',
  icon: <AddIcon />,
  // NICHT: path: '/customers/new'
  // SONDERN: onClick Handler der Modal öffnet
  action: 'OPEN_CUSTOMER_WIZARD'
}
```

### Phase 3: Alle anderen Seiten prüfen (1h)

#### 3.1 Checklist aller Seiten:
- [ ] `/users` - UsersPage
- [ ] `/opportunities` - OpportunityPipelinePage  
- [ ] `/calculator-v2` - CalculatorPageV2
- [ ] `/einstellungen` - SettingsPage
- [ ] Weitere...

#### 3.2 Migration Template:
```typescript
// VORHER
export function SomePage() {
  return <div>Content</div>;
}

// NACHHER
import { MainLayoutV2 } from '../components/layout/MainLayoutV2';

export function SomePage() {
  return (
    <MainLayoutV2>
      <div>Content</div>
    </MainLayoutV2>
  );
}
```

### Phase 4: Dokumentation & Guidelines (1h)

#### 4.1 Frontend Guidelines erstellen
```markdown
# FreshPlan 2.0 Frontend Guidelines

## 🚨 GOLDENE REGEL: MainLayoutV2 ist PFLICHT!

### Für JEDE Seite gilt:
1. IMMER MainLayoutV2 als Wrapper verwenden
2. NIEMALS eigene Layout-Strukturen bauen
3. Sidebar ist IMMER sichtbar (außer Login)
4. Neue Entitäten als Modal/Drawer, nicht als Route

### Verbotene Patterns:
- ❌ Separate Routes für Wizards (/entity/new)
- ❌ Custom Layout ohne MainLayoutV2
- ❌ Sidebar verstecken oder entfernen
- ❌ Redundante Navigation
```

---

## 📋 IMPLEMENTATION CHECKLIST

### Sofort (Sprint 2, Tag 1):
- [ ] CustomersPageV2 mit MainLayoutV2 wrappen
- [ ] CustomerOnboardingWizardModal implementieren
- [ ] Route /customers/new entfernen
- [ ] Sidebar-Menüpunkt für Modal-Trigger

### Kurzfristig (Sprint 2, Tag 2):
- [ ] Alle anderen Seiten migrieren
- [ ] Standard Page Template dokumentieren
- [ ] ESLint Rule für MainLayoutV2 Usage

### Tests:
- [ ] Modal öffnet/schließt korrekt
- [ ] ESC-Taste schließt Modal
- [ ] Sidebar bleibt sichtbar
- [ ] Navigation konsistent

---

## ⚠️ WARNUNG FÜR NEUE ENTWICKLER

**DIESER FIX IST KRITISCH!** 

Ohne diesen Fix:
- Nutzer verlieren Orientierung
- Navigation ist inkonsistent  
- Design-System wird nicht eingehalten
- User Experience leidet massiv

**REGEL:** Wenn du eine neue Seite erstellst, MUSST du MainLayoutV2 verwenden!

---

## 🎯 ERWARTETES ERGEBNIS

1. **Konsistente Navigation** - Sidebar überall sichtbar
2. **Keine redundanten Elemente** - Klare Trennung von Concerns
3. **Modal-basierte Wizards** - Kontext bleibt erhalten
4. **Einheitliches Design** - Theme wird überall angewendet

---

## 📚 REFERENZEN

- [Sprint 2 Planning](./DAY1_IMPLEMENTATION.md)
- [Sidebar Diskussion](./SIDEBAR_LEAD_DISCUSSION.md)
- [Frontend Architecture](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)
- [MainLayoutV2 Component](/Users/joergstreeck/freshplan-sales-tool/frontend/src/components/layout/MainLayoutV2.tsx)

---

**WICHTIG:** Dieser Plan muss als ERSTES umgesetzt werden, bevor weitere Features implementiert werden!

---

## 🔗 Weiterführende Links

**← Zurück zu:** [Sprint 2 Planung](./README.md)  
**→ Weiter zu:** [Quick Fix Guide](/Users/joergstreeck/freshplan-sales-tool/docs/CRITICAL_FIX_MAINLAYOUT_MISSING.md)  
**↑ Übergeordnet:** [FC-005 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)