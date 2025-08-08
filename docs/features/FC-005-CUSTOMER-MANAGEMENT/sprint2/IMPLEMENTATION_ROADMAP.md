# 🚀 Sprint 2 Implementierungs-Roadmap

**Sprint:** 2  
**Dauer:** 3.5 Tage  
**Status:** 🆕 Neu ausgerichtet auf Verkaufsfokus  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**↗️ Diskussion:** [Customer Structure Redesign](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/discussions/2025-07-30_CUSTOMER_STRUCTURE_REDESIGN.md)  
**📋 TODOs:** Siehe unten

---

## 🎯 Priorisierung nach Business Value

### 🥇 Woche 1: MVP - Sofort umsetzen

#### Tag 1: Field Catalog & Wizard-Struktur
1. **Field Catalog erweitern** (TODO-8)
   - Filialstruktur-Felder hinzufügen
   - Geschäftsmodell-Radio-Buttons
   - Pain Point Checkboxes
   - → [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)

2. **Wizard auf 3 neue Steps umbauen** (TODO-5)
   - Step 1: Basis + Filialstruktur
   - Step 2: Angebot + Pain Points
   - Step 3: Ansprechpartner strukturiert
   - → [Wizard Struktur V2](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/WIZARD_STRUCTURE_V2.md)

#### Tag 2: Angebotsstruktur & Pain Points
3. **Angebotsstruktur für Hotels** (TODO-9)
   - Frühstück (warm/kalt)
   - Restaurant/À la carte
   - Events/Bankette
   - → [Step 2 Details](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP2_ANGEBOT_PAINPOINTS.md)

4. **Pain Point Mapping** (TODO-10)
   - Universelle Pain Points
   - Freshfoodz-Lösungen
   - UI-Integration
   - → [Pain Point Mapping](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/PAIN_POINT_MAPPING.md)

### 🥈 Woche 2: Intelligence Layer

#### Tag 3: Potenzialberechnung
5. **Einfache Potenzialberechnung** (NEU)
   - Erfahrungswerte-basiert
   - Branchenspezifisch
   - Live im Wizard
   - → [Potential Calculation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/POTENTIAL_CALCULATION.md)

6. **Quick-Win-Generator** (NEU)
   - Top 3 Verkaufschancen
   - Basierend auf Pain Points
   - Mit Produktempfehlungen

#### Tag 3.5: Task Preview & Polish
7. **Task Preview MVP** (TODO-6)
   - Nach Kundenanlage
   - "Neukunde begrüßen" Task
   - Toast mit Action
   - → [Task Preview Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/TASK_PREVIEW_INTEGRATION.md)

---

## 📊 Deliverables pro Tag

### Tag 1 Deliverables:
- [ ] Field Catalog mit neuen Feldern
- [ ] Wizard-Struktur umgebaut
- [ ] Conditional Logic funktioniert

### Tag 2 Deliverables:
- [ ] Angebotsstruktur erfassbar
- [ ] Pain Points wählbar
- [ ] Lösungen werden angezeigt

### Tag 3 Deliverables:
- [ ] Potenzial wird berechnet
- [ ] Quick Wins generiert
- [ ] Task nach Speichern erstellt

---

## 🧪 Test-Strategie

1. **Unit Tests** für:
   - Potenzialberechnung
   - Pain Point Mapping
   - Field Validierung

2. **Integration Tests** für:
   - Wizard-Flow komplett
   - Conditional Fields
   - Task-Erstellung

3. **E2E Test** für:
   - Happy Path: Hotel mit Filialstruktur

---

## 🚫 Scope Management

### Was wir NICHT machen (Sprint 3+):
- KI-basierte Potenzialberechnung
- Historische Daten
- Komplexe Wenn-Dann-Regeln
- Multi-Location-Verwaltung

### Fokus behalten auf:
- 80%-Lösung
- Verkaufsmehrwert
- Einfache Bedienung
- Freshfoodz-USPs

---

## ✅ Definition of Done

- [ ] Alle neuen Felder im Field Catalog
- [ ] Wizard zeigt Verkaufspotenzial
- [ ] Pain Points → Lösungen funktioniert
- [ ] Tests grün (Unit + Integration)
- [ ] Dokumentation aktuell
- [ ] Code Review abgeschlossen

---

## 🔗 Quick Links

**TODOs:**
- TODO-5: Wizard-Struktur finalisieren
- TODO-6: Task Preview MVP
- TODO-8: Field Catalog erweitern
- TODO-9: Angebotsstruktur Hotels
- TODO-10: Personalmangel Pain Point

**Start hier:** [Field Catalog Extension](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_EXTENSION.md)