# 👤 Step 3: Ansprechpartner & Details

**Sprint:** 2  
**Component:** CustomerOnboardingWizard  
**Status:** 🆕 Strukturiert für Vertrieb  

---

## 📍 Navigation
**← Zurück:** [Step 2: Angebot & Pain Points](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)  
**↑ Übersicht:** [Wizard Struktur V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V2.md)  
**💾 Speichern:** Nach diesem Schritt

---

## 🎯 Zweck

Strukturierte Erfassung der Ansprechpartner für professionelle Kommunikation.

---

## 🖼️ UI Design

```
┌─────────────────────────────────────┐
│ Schritt 3: Ansprechpartner          │
├─────────────────────────────────────┤
│                                     │
│ 👤 Hauptansprechpartner             │
│                                     │
│ Anrede: [Herr ▼]* Titel: [Dr.]     │
│ Vorname: [Max]*                     │
│ Nachname: [Mustermann]*             │
│                                     │
│ Position: [Einkaufsleiter]*         │
│ Abteilung: [Einkauf ▼]             │
│                                     │
│ 📧 E-Mail geschäftlich:             │
│ [m.mustermann@marriott.de]*         │
│                                     │
│ 📱 Mobil: [0171-1234567]           │
│ ☎️ Büro: [030-123456]              │
│ Durchwahl: [789]                    │
│                                     │
│ Entscheider-Level:                  │
│ ● Entscheidungsträger               │
│ ○ Einflussnehmer                    │
│ ○ Nutzer                            │
│                                     │
│ Bevorzugter Kontaktweg:             │
│ [E-Mail ▼]                          │
│                                     │
│ Beste Erreichbarkeit:               │
│ [Vormittags ▼]                      │
│                                     │
│ 📝 Notizen (für Beziehungsaufbau):  │
│ [Mag Golf, 2 Kinder, FC Bayern Fan] │
│                                     │
│ 🎂 Geburtstag: [TT.MM.JJJJ]        │
│                                     │
│ 🏷️ Tags:                            │
│ [VIP] [Technik-affin] [+]           │
│                                     │
│ [+ Weiteren Ansprechpartner]        │
│                                     │
│ [← Zurück] [💾 Kunde speichern]     │
└─────────────────────────────────────┘
```

---

## 📊 Datenstruktur

```typescript
interface Contact {
  // Basis (Pflicht)
  salutation: 'herr' | 'frau' | 'divers';
  firstName: string;
  lastName: string;
  position: string;
  email: string;
  
  // Erweitert (Optional)
  title?: string;
  department?: string;
  mobile?: string;
  phone?: string;
  extension?: string;
  
  // Vertriebsrelevant
  decisionLevel: 'decision_maker' | 'influencer' | 'user';
  preferredChannel: 'email' | 'phone' | 'visit';
  bestTimeToReach?: string;
  
  // Für Beziehungsaufbau
  notes?: string; // "Mag Golf, 2 Kinder"
  birthday?: Date;
  tags?: string[]; // ['VIP', 'Schwieriger Kunde', 'Technik-affin']
  
  // Sprache für Kommunikation
  language?: 'de' | 'en' | 'fr' | 'es';
}
```

---

## 💡 Besonderheiten

### Mehrere Ansprechpartner
- Hauptansprechpartner = Pflicht
- Weitere optional (unbegrenzt)
- Pro Standort unterschiedlich (später)

### Smart Defaults
- E-Mail-Domain aus Firmen-Website
- Anrede basierend auf Vorname
- Department basierend auf Position
- Sprache default 'de'

### Standort-Zuordnung (Sprint 2 vereinfacht)
- In Sprint 2: Ansprechpartner gehört zum Hauptstandort
- In Sprint 3: Ansprechpartner pro Standort erfassbar

---

## ✅ Nach dem Speichern

1. **Customer wird angelegt**
2. **Task automatisch erstellt:** "Neukunde begrüßen"
3. **Navigation zu:** Customer Detail Page
4. **Toast mit Action:** "Kunde erfolgreich angelegt [Aufgabe anzeigen]"

---

## 🔗 Weiterführende Links

**Vorheriger Step:** [Step 2: Angebot & Pain Points](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)  
**Nach Speichern:** [Task Preview Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/TASK_PREVIEW_INTEGRATION.md)