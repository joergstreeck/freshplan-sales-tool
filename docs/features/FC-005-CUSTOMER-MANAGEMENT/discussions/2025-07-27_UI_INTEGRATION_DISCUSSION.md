# 🎯 FC-005 UI Integration - Diskussionsdokument

**Datum:** 27.07.2025  
**Teilnehmer:** Jörg, Claude  
**Status:** 🔄 In Diskussion  
**Sprint:** Sprint 2 - Customer UI Integration  

---

## 📋 Zusammenfassung der bisherigen Diskussion

### 🎯 Kernfragen und Entscheidungen

#### 1. **Wo wird der Einstiegspunkt für Kundenerfassung sein?**

**Diskutiert:**
- Initial: "+ Neuer Kunde" Button in der CustomersPage
- Frage: Soll der Button in der Sidebar sein?
- Frage: Soll der Button immer im Header präsent sein?

**Konsens:**
- ❌ NICHT in der Sidebar (Sidebar = Navigation, nicht Aktionen)
- ❌ NICHT global im Header (nur kontextbezogen)
- ✅ NUR auf der Kundenliste-Seite (`/customers`)
- ✅ Als kontextbezogene Aktion

#### 2. **Button-Platzierung auf der Kundenliste**

**Optionen diskutiert:**

**Option A: Im Page-Header**
```
┌─────────────────────────────────────────────┐
│ Alle Kunden                    [+ Neuer Kunde] │
├─────────────────────────────────────────────┤
```
- Pro: Sofort sichtbar, Standard-Pattern
- Contra: Könnte mit globalem Header verwechselt werden

**Option B: Über der Tabelle (bei Filter/Suche)**
```
Alle Kunden
─────────────────────────────────
🔍 Suche...            Filter ▼   [+ Neuer Kunde]
─────────────────────────────────
```
- Pro: Näher am Kontext, scrollt nicht weg
- Contra: Weniger prominent

**Option C: Als Karte bei leerer Liste**
```
┌─────────────────────────┐
│         📋              │
│  Noch keine Kunden      │
│                         │
│ [+ Ersten Kunden        │
│    anlegen]             │
└─────────────────────────┘
```
- Pro: Perfekt für Onboarding
- Contra: Versteckt bei gefüllter Liste

**Tendenz:** Hybrid-Ansatz (C für leere Liste, B für gefüllte Liste)

#### 3. **Kundenliste vs. Cockpit - Konzeptuelle Unterschiede**

**Cockpit (Dashboard):**
- Zweck: "Was muss ich heute tun?"
- Zeigt: Top 5-10 Kunden als Karten mit KPIs
- Fokus: Tagesgeschäft, Quick-Actions
- Neuer Kunde: ❌ Nicht hier

**Kundenliste:**
- Zweck: "Wo finde ich Kunde X?" / "Kunden verwalten"
- Zeigt: Alle Kunden in Tabellenform (100-500 erwartet)
- Fokus: Verwaltung, Suche, CRUD-Operationen
- Neuer Kunde: ✅ Hier anlegen

#### 4. **Rechteverwaltung und Datensichtbarkeit**

**Wichtige Erkenntnis:** Verkäuferschutz gilt überall!

- **Verkäufer:** Sieht NUR eigene Kunden (in Cockpit UND Kundenliste)
- **Manager:** Sieht Team-Kunden
- **Admin:** Sieht alle Kunden

**Implikation:**
- Kundenliste zeigt für Verkäufer "Meine Kunden (23)"
- Cockpit zeigt Subset davon (wichtigste 5-10)
- Beide nutzen gleichen Filter, unterschiedliche Darstellung

---

## 🔄 Offene Punkte zur Klärung

### 1. **Button-Platzierung Final**
- [ ] Endgültige Entscheidung: Option B (über Tabelle) oder Hybrid?
- [ ] Soll ein Floating Action Button (FAB) zusätzlich implementiert werden?

### 2. **Wizard-Verhalten**
- [ ] Modal vs. Drawer vs. Neue Seite?
- [ ] Nach Anlegen: Direkt zur Detail-Seite oder zurück zur Liste?

### 3. **Kundenlisten-Features**
- [ ] Welche Spalten sind Must-Have? (Name, Branche, Status, ...?)
- [ ] Bulk-Actions gewünscht? (Mehrere Kunden bearbeiten)
- [ ] Export-Funktionalität?

### 4. **Performance bei 500 Kunden**
- [ ] Client-side vs. Server-side Pagination?
- [ ] Virtualized Scrolling nötig?
- [ ] Such-Performance (Debouncing)?

---

## 💡 Workflow-Konzept (Aktueller Stand)

### User Journey: Neuen Kunden anlegen

1. **Navigation:**
   - Sidebar: "👥 Kundenmanagement" → "Alle Kunden"
   
2. **Aktion initiieren:**
   - Klick auf "+ Neuer Kunde" Button (Platzierung TBD)
   
3. **Wizard-Flow:**
   ```
   Step 1: Basisdaten (Pflicht)
   - Firmenname*
   - Branche* (dropdown)
   - Kette ja/nein* (trigger für Standorte)
   
   Step 2: Standorte (conditional)
   - Nur wenn chainCustomer='ja'
   - Multiple Standorte möglich
   
   Step 3: Zusatzinfos (optional)
   - Branchenspezifische Felder
   - Basierend auf Field Catalog
   ```

4. **Nach Speichern:**
   - API Call: POST /api/customers
   - Bei Erfolg: Navigation zu Detail-Seite (TBD)
   - Kunde erscheint in Liste UND ggf. Cockpit

### Technische Integration

```typescript
// CustomersPage.tsx
export function CustomersPage() {
  const { user } = useAuth();
  const [wizardOpen, setWizardOpen] = useState(false);
  
  // Verkäuferschutz-Filter
  const customers = useCustomers({
    filter: user.role === 'sales' ? { assignedTo: user.id } : undefined
  });
  
  return (
    <PageLayout>
      {/* TBD: Button-Platzierung */}
      <CustomerList customers={customers} />
      
      <CustomerOnboardingWizard
        open={wizardOpen}
        onClose={() => setWizardOpen(false)}
        onSuccess={handleCustomerCreated}
      />
    </PageLayout>
  );
}
```

---

## 📝 Nächste Schritte

1. **Entscheidungen treffen:**
   - [ ] Button-Platzierung finalisieren
   - [ ] Wizard-Verhalten klären
   - [ ] Feature-Scope für Kundenliste definieren

2. **Implementierung planen:**
   - [ ] CustomersPage anpassen
   - [ ] CustomerOnboardingWizard integrieren
   - [ ] API-Endpoints anbinden
   - [ ] Tests schreiben

---

## 🌟 NEUER VORSCHLAG: Ganzheitliche Cockpit-Customer Management Integration

### 🧭 Grundprinzip (von Jörg eingebracht)
Das Cockpit wird zum zentralen **"Arbeitsplatz für Aufgaben und Handlungsbedarf"** – die Kundenverwaltung bleibt ein übersichtlicher, administrativer Bereich für Suchen, Filtern, Anlegen und Pflegen von Kunden.

### 🎯 Claude's Analyse im Kontext unserer Vision

Dieser Vorschlag passt PERFEKT zu unserer Vision im V5 Master Plan:
> **"Wir bauen ein intelligentes Sales Command Center, das unsere Vertriebsmitarbeiter lieben, weil es ihnen proaktiv die Informationen, Insights und geführten Prozesse liefert"**

Die Kernprinzipien werden optimal umgesetzt:
1. **Geführte Freiheit:** Task-First im Cockpit, aber freie Navigation zur Verwaltung
2. **Alles verbunden:** Nahtloser Wechsel zwischen Aufgaben und Administration
3. **Skalierbare Exzellenz:** Progressive Disclosure, Smart Empty States

### 📊 Konkretisierung für unsere Architektur

#### A. Cockpit-Evolution (FC-011 vorbereiten)

**Task-Driven Dashboard:**
```typescript
// CockpitPage.tsx
interface CockpitCard {
  customerId: string;
  customerName: string;
  priority: 'urgent' | 'high' | 'normal';
  tasks: Task[];
  alerts: Alert[];  // "60 Tage kein Kontakt", "Angebot läuft ab"
  quickActions: QuickAction[];
}

// Nur Kunden mit Handlungsbedarf
const relevantCustomers = customers.filter(c => 
  c.hasPendingTasks || 
  c.hasAlerts || 
  c.daysSinceLastContact > 60
);
```

**Smart Task Generation:**
- Renewal-Reminder (aus FC-009)
- Follow-up nach Angebot (aus M4)
- Inaktivitäts-Warnungen
- Vertrags-Ablauf-Benachrichtigungen

#### B. Customer Management als "Administrative Heimat"

**Klare Trennung:**
- **Cockpit:** "Was muss ich HEUTE tun?" (Action-Oriented)
- **Kundenliste:** "Wo finde ich Infos? Wo lege ich an?" (Data-Oriented)

**Button-Platzierung FINAL:**
```
Meine Kunden (23)                        [🔍 Suche] [⚙️ Filter] [+ Neuer Kunde]
────────────────────────────────────────────────────────────────────────────
Name ↕         Branche      Status      Letzer Kontakt    Aktionen
```
→ Über der Tabelle bei Filter/Suche (Option B)
→ Bei leerer Liste: Hero-CTA (Option C)

#### C. Integration mit Field-Based Architecture

Unsere FC-005 Implementierung unterstützt das perfekt:
- **Dynamic Fields:** Branchenspezifische Anzeige
- **Conditional Logic:** Intelligente Formulare
- **Field Catalog:** Zentrale Feldverwaltung

### 💡 Konkrete Umsetzungsschritte für Sprint 2

1. **CustomersPage erweitern:**
   ```typescript
   // Hybrid Empty State
   {customers.length === 0 ? (
     <EmptyStateHero onCreateClick={() => setWizardOpen(true)} />
   ) : (
     <>
       <CustomerListHeader>
         <SearchBar />
         <FilterDropdown />
         <Button variant="primary" onClick={() => setWizardOpen(true)}>
           + Neuer Kunde
         </Button>
       </CustomerListHeader>
       <CustomerTable customers={customers} />
     </>
   )}
   ```

2. **Wizard-Integration:**
   - Als Modal (Desktop) / Drawer (Mobile)
   - Nach Erfolg → Navigation zu Detail
   - Auto-Assignment an aktuellen User

3. **Task-Engine Vorbereitung:**
   ```typescript
   // Basis für spätere Task-Generierung
   interface TaskRule {
     condition: (customer: Customer) => boolean;
     generateTask: (customer: Customer) => Task;
   }
   ```

### 🚀 Vorteile dieser Lösung

1. **User Experience:**
   - Verkäufer werden nicht mit Admin-Tasks belastet
   - Klare Aufgaben-Fokussierung im Tagesgeschäft
   - Intuitive Navigation zwischen Contexts

2. **Technisch:**
   - Saubere Trennung der Concerns
   - Wiederverwendbare Komponenten
   - Vorbereitet für FC-011 Cockpit-Integration

3. **Business Value:**
   - Höhere Produktivität durch Task-Fokus
   - Weniger vergessene Follow-ups
   - Bessere Kundenbindung durch proaktive Betreuung

### 📝 Offene Entscheidungen (aktualisiert)

- [x] **Button-Platzierung:** Option B (über Tabelle) mit Option C (Empty State)
- [x] **Wizard-Verhalten:** Modal/Drawer je nach Device
- [x] **Nach Anlegen:** Navigation zu Detail-Seite
- [x] **Task-Preview in Sprint 2:** Ja! MVP mit erstem Follow-up Task
- [ ] **Task-Rules:** Welche automatischen Aufgaben generieren? (für FC-011)
- [ ] **Mobile FAB:** Zusätzlich für Tablet-Nutzer im Außendienst?
- [ ] **Customer Profile Generator:** Als neues Feature definieren?

---

## 🌟 NEUER VORSCHLAG: Task-Preview MVP in Sprint 2

### 💡 Jörgs Empfehlung: Task-Preview einbauen

**Warum sinnvoll:**
- Sofortiger Aha-Effekt: "Das System denkt mit!"
- Fundament für FC-011 wird gelegt
- Einfache Implementierung, großer UX-Gewinn

### 📊 Konkrete Umsetzung

#### 1. Nach CustomerOnboardingWizard Abschluss:
```typescript
// customerOnboardingStore.ts
const submitForm = async () => {
  const customer = await createCustomer(formData);
  
  // Task-Preview: Automatisch erste Aufgabe generieren
  const welcomeTask = await createTask({
    customerId: customer.id,
    title: "Neukunde kontaktieren - Willkommensanruf",
    description: "Erstkontakt herstellen und Infopaket versenden",
    dueDate: addDays(new Date(), 2),
    priority: 'high',
    assignedTo: currentUser.id,
    type: 'follow-up',
    source: 'customer-onboarding'
  });
  
  // Navigation mit Task-Highlight
  navigate(`/customers/${customer.id}?highlight=new-task`);
};
```

#### 2. Task-Rule Interface (Vorbereitung):
```typescript
// services/taskRules.ts
interface TaskRule {
  trigger: 'customer-created' | 'quote-sent' | 'contract-expiring';
  condition: (context: any) => boolean;
  generateTask: (context: any) => Task;
}

const customerCreatedRule: TaskRule = {
  trigger: 'customer-created',
  condition: () => true, // Immer bei neuen Kunden
  generateTask: (customer) => ({
    title: `Neukunde ${customer.name} kontaktieren`,
    dueDate: addDays(new Date(), 2),
    // ...
  })
};
```

#### 3. Cockpit-Preview:
```typescript
// In Cockpit werden neue Tasks prominent angezeigt
<TaskCard highlighted={task.source === 'customer-onboarding'}>
  <Badge color="green">NEU</Badge>
  {task.title}
</TaskCard>
```

---

## 🚀 NEUER VORSCHLAG: Customer Profile Generator / 360° Dashboard

### 🎯 Jörgs Vision: Modularer Profil-Generator

**Kern-Idee:** Automatisch generierte, rollenbasierte Kundenprofile die sich selbst aktualisieren

### 📊 Use Cases nach Rolle

#### Geschäftsleitung:
- Wer ist der Kunde? (Überblick)
- Welches Umsatzpotential?
- Strategische Bedeutung
- Risk Indicators

#### Vertrieb:
- Kontakthistorie
- Offene Opportunities
- Nächste Schritte
- Beziehungsstatus

#### Innendienst/Operations:
- Bestellplattform
- Lieferdetails
- Produktionsdaten
- Prozess-Spezifika

### 🏗️ Architektur-Konzept

#### 1. Widget-basiertes System:
```typescript
interface ProfileWidget {
  id: string;
  title: string;
  component: React.Component;
  dataSource: (customerId: string) => Promise<any>;
  roles: UserRole[];
  size: 'small' | 'medium' | 'large';
}

// Beispiel-Widgets
const widgets: ProfileWidget[] = [
  {
    id: 'basic-info',
    title: 'Stammdaten',
    component: BasicInfoWidget,
    dataSource: getCustomerBasicInfo,
    roles: ['all'],
    size: 'medium'
  },
  {
    id: 'revenue-potential',
    title: 'Umsatzpotential',
    component: RevenuePotentialWidget,
    dataSource: calculateRevenuePotential,
    roles: ['management', 'sales'],
    size: 'small'
  },
  {
    id: 'production-details',
    title: 'Produktionsdetails',
    component: ProductionWidget,
    dataSource: getProductionData,
    roles: ['operations'],
    size: 'large'
  }
];
```

#### 2. Profile Templates nach Rolle:
```typescript
const profileTemplates = {
  management: ['basic-info', 'revenue-potential', 'strategic-value', 'risk-indicators'],
  sales: ['basic-info', 'contact-history', 'opportunities', 'next-actions'],
  operations: ['basic-info', 'order-platforms', 'delivery-specs', 'production-details']
};
```

#### 3. Dynamische Profil-Generierung:
```typescript
// CustomerProfilePage.tsx
const CustomerProfile = ({ customerId, viewAs = currentUser.role }) => {
  const template = profileTemplates[viewAs];
  const widgets = useProfileWidgets(customerId, template);
  
  return (
    <ProfileLayout>
      <ProfileHeader customer={customer} />
      <WidgetGrid>
        {widgets.map(widget => (
          <WidgetContainer key={widget.id} size={widget.size}>
            <widget.component data={widget.data} />
          </WidgetContainer>
        ))}
      </WidgetGrid>
    </ProfileLayout>
  );
};
```

### 💡 Integration mit FC-005 Field-Based Architecture

Unsere Field-Catalog Architektur ist PERFEKT dafür geeignet:
- Felder können Widget-Zuordnungen haben
- Conditional Logic für dynamische Widgets
- Industry-spezifische Profile

```typescript
// fieldCatalog.json Erweiterung
{
  "fields": [{
    "name": "roomCount",
    "widget": "hotel-capacity",
    "widgetRoles": ["management", "sales"],
    "widgetPriority": "high"
  }]
}
```

### 🚀 Umsetzungsplan

#### Phase 1 (MVP - Sprint 3?):
- [ ] Basis-Widget-System
- [ ] 3-4 Core Widgets (Stammdaten, Kontakte, Aktivitäten)
- [ ] Feste Templates pro Rolle

#### Phase 2:
- [ ] 10+ spezialisierte Widgets
- [ ] Widget-Builder UI
- [ ] Custom Templates speichern

#### Phase 3:
- [ ] KI-gestützte Insights
- [ ] Predictive Analytics Widgets
- [ ] Cross-Customer Vergleiche

### 📝 Status Customer Profile Generator

✅ **Feature Code vergeben:** FC-019 (in Feature Roadmap aufgenommen)
✅ **Geschätzter Aufwand:** 6-8 Tage
✅ **Phase:** Analytics & Intelligence (Phase 3)

**Noch zu klären:**
- [ ] Genaue Priorisierung innerhalb Phase 3
- [ ] Widget-Entwicklung: Zentral oder pro Modul?
- [ ] MVP-Scope für erste Version

---

## 🔄 Änderungshistorie

- **27.07.2025 16:45:** Initiale Erstellung des Dokuments
- Diskussion über Button-Platzierung (Sidebar vs. Seite)
- Klärung Cockpit vs. Kundenliste
- Verkäuferschutz-Konzept hinzugefügt

- **27.07.2025 17:05:** Großes Update mit Jörgs Vorschlag
- Ganzheitliche Cockpit-Customer Integration
- Task-First Approach für Cockpit definiert
- Konkrete Implementierungsschritte für Sprint 2
- Verbindung zu FC-011 und Vision hergestellt

- **27.07.2025 17:25:** Task-Preview MVP und Customer Profile Generator
- Task-Preview für Sprint 2 bestätigt
- Customer Profile Generator als FC-019 definiert
- Widget-basierte Architektur konzipiert
- In Feature Roadmap Phase 3 eingeordnet

---

## 📋 Zusammenfassung der Entscheidungen

### ✅ Für Sprint 2 beschlossen:
1. **UI Integration:** CustomerOnboardingWizard in Kundenliste
2. **Button-Platzierung:** Hybrid (über Tabelle + Empty State)
3. **Task-Preview MVP:** Erste Follow-up Task nach Kundenanlage
4. **Navigation:** Nach Anlegen → Customer Detail Page

### 📅 Für spätere Sprints:
1. **FC-011:** Vollständige Cockpit-Integration mit Task Engine
2. **FC-019:** Customer Profile Generator (Phase 3, 6-8 Tage)

---

**Hinweis:** Dieses Dokument dokumentiert die abgeschlossene Diskussion zur UI Integration.