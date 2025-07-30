# 🍳 Step 2: Angebot & Pain Points

**Sprint:** 2  
**Component:** CustomerOnboardingWizard  
**Status:** 🆕 Verkaufsfokussiert  

---

## 📍 Navigation
**← Zurück:** [Step 1: Basis & Filialstruktur](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)  
**→ Weiter:** [Step 3: Ansprechpartner](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md)  
**⚙️ Implementierung:** [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md)

---

## 🎯 Zweck

Angebotsstruktur erfassen → Bedarf ableiten → Pain Points identifizieren → Potenzial berechnen

---

## 🖼️ UI Design (Beispiel Hotel)

```
┌─────────────────────────────────────┐
│ Schritt 2: Ihr Angebot & Bedarf     │
├─────────────────────────────────────┤
│                                     │
│ 🍳 Was bieten Sie Ihren Gästen?    │
│                                     │
│ ☑ Frühstück                        │
│   └─ ☑ Warme Komponenten           │
│   └─ Gäste/Tag: [120]              │
│                                     │
│ ☑ Restaurant/À la carte            │
│   └─ Öffnung: [Mittag & Abend ▼]   │
│                                     │
│ ☑ Halbpension/Vollpension          │
│ ☐ Roomservice 24/7                 │
│ ☑ Bankette/Events                  │
│   └─ Kapazität: [200] Personen     │
│ ☐ Vending/Automaten                │
│                                     │
│ ─────────────────────────────────── │
│                                     │
│ 👥 Ihre aktuellen Herausforderungen:│
│                                     │
│ ☑ Fachkräftemangel in der Küche    │
│ ☑ Schwankende Qualität             │
│ ☐ Hohe Warenverluste               │
│ ☑ Viele Diät-/Sonderwünsche       │
│ ☐ Zu hohe Personalkosten           │
│ ☑ Planungsunsicherheit             │
│ ☐ Lieferant-Unzufriedenheit        │
│                                     │
│ ─────────────────────────────────── │
│                                     │
│ 💰 Ihr Potenzial mit Freshfoodz:    │
│                                     │
│ 📊 Jahrespotenzial: 850.000 €      │
│    (45 Standorte × Erfahrungswert)  │
│                                     │
│ ✨ Ihre Quick Wins:                 │
│ • Cook&Fresh® löst Personalproblem  │
│ • 40 Tage Haltbarkeit = -30% Waste │
│ • Diätkost komplett verfügbar      │
│                                     │
│ [← Zurück] [Weiter →]               │
└─────────────────────────────────────┘
```

---

## 🔄 Dynamische Felder nach Branche

### Hotels
- Frühstück (warm/kalt, Gästezahl)
- Restaurant (Öffnungszeiten, Typ, externe Gäste?)
- HP/VP Angebot (Anzahl Gäste)
- Roomservice (24/7 oder begrenzt)
- Events/Bankette (Kapazität, Häufigkeit)
- Vending/Automaten (vorhanden/Interesse)

### Krankenhäuser
- Verpflegungssystem (Cook&Serve etc.)
- Mahlzeiten pro Tag
- Stationen/Bettenzahl
- Diätformen
- Cafeteria/Kiosk

### Betriebsrestaurants
- Mitarbeiteranzahl (zu verpflegen)
- Öffnungstage (5 oder 7)
- Subventioniert ja/nein
- Zusatzangebote (Kiosk, Automaten)
- Service-Typen (Selbstbedienung, serviert)
- Peak-Zeiten (Frühstück, Mittag)

---

## 🎯 Pain Point → Solution Mapping

### Zusätzliche Erfassungsfelder:
- **Aktueller Lieferant:** [___________]
- **Wechselbereitschaft:** 1-5 Sterne
- **Vertragsende:** [TT.MM.JJJJ]
- **Entscheidungs-Timeline:** [Q2 2025 ▼]

```typescript
const painPointSolutions = {
  'fachkraeftemangel': {
    solution: 'Cook&Fresh® - Keine Köche nötig!',
    products: ['Fertiggerichte', 'Convenience'],
    benefit: 'Spart 1-2 Vollzeitstellen'
  },
  'schwankende-qualitaet': {
    solution: 'Immer gleiche Premium-Qualität',
    products: ['Standardisierte Menüs'],
    benefit: 'Bessere Bewertungen'
  },
  'warenverluste': {
    solution: '40 Tage Haltbarkeit bei 7°C',
    products: ['Alle Cook&Fresh® Produkte'],
    benefit: '30-50% weniger Abfall'
  }
  'lieferant-unzufrieden': {
    solution: 'Zuverlässiger Partner aus Berlin',
    products: ['Komplettes Sortiment'],
    benefit: 'Eine Bestellung, eine Lieferung'
  },
  'planungsunsicherheit': {
    solution: '40 Tage = perfekte Planung',
    products: ['Alle Produkte'],
    benefit: 'Keine Überproduktion mehr'
  }
};
```

---

## 📊 Live-Potenzialberechnung

Siehe: [Potenzialberechnung Details](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)

---

## 🔗 Weiterführende Links

**Vorheriger Step:** [Step 1: Basis & Filialstruktur](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP1_BASIS_FILIALSTRUKTUR.md)  
**Nächster Step:** [Step 3: Ansprechpartner](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER.md)  
**Pain Points:** [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md)