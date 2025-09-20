# 🔍 Konsistenz-Analyse: Hybrid-Modell vs. Planungsdokumente

**Erstellt:** 12.07.2025  
**Analysebereich:** FC-002 Module und neue Features (FC-003 bis FC-007)  
**Fokus:** Inkonsistenzen mit dem neuen prozessorientierten Hybrid-Modell  

## 📊 Executive Summary

Nach Durchsicht aller Planungsdokumente zeigen sich mehrere **konzeptionelle Inkonsistenzen** zwischen der ursprünglichen funktionsgetriebenen Planung und dem neuen prozessorientierten Hybrid-Modell. Die gute Nachricht: Die technische Umsetzung ist größtenteils konsistent, nur die konzeptionelle Beschreibung muss angepasst werden.

## ✅ Was ist konsistent

### 1. **Implementierungs-Timeline**
- Master-Plan (FC-002-IMPLEMENTATION_PLAN.md) ist aktuell
- Reihenfolge M4 → M8 → M5 ist korrekt dokumentiert
- Aufwandsschätzungen wurden bereits angepasst

### 2. **Technische Architektur**
- M4 (Opportunity Pipeline) ist bereits prozessorientiert geplant
- M8 (Rechner) ist korrekt als Modal dokumentiert
- Integration zwischen Modulen ist stimmig

### 3. **Neue Features (FC-003 bis FC-007)**
- Sind von Anfang an prozessorientiert konzipiert
- Passen gut ins Hybrid-Modell
- Keine konzeptionellen Konflikte

## ❌ Hauptinkonsistenzen

### 1. **M2 (Quick-Create) - Konzeptioneller Konflikt**

**Problem:** Noch als separates "System" dokumentiert statt als kontextuelle Funktion

**Ist-Zustand (FC-002-M2-quick-create.md):**
```typescript
// Globaler Button im Header
<QuickCreateButton />
// Dropdown mit allen Optionen
- Neuer Kunde
- Neuer Kontakt  
- Neue Aufgabe
- Neue Notiz
```

**Soll-Zustand (Hybrid-Modell):**
```typescript
// Kontextabhängige Quick-Actions
if (currentContext === 'opportunity_pipeline') {
  quickActions = ['Neue Opportunity', 'Neuer Lead'];
} else if (currentContext === 'customer_detail') {
  quickActions = ['Neuer Kontakt', 'Neue Notiz'];
}
```

**Empfehlung:** M2 als "kontextuelle Quick-Actions" neu konzipieren

### 2. **M6 (Berichte) - Veraltetes Konzept**

**Problem:** Noch als eigenständiges "Analytics Modul" geplant

**Ist-Zustand (FC-002-M6-berichte.md):**
- Separater Menüpunkt "Berichte"
- Zentralisierte Auswertungen
- Export-Funktionen

**Soll-Zustand (Hybrid-Modell):**
- Berichte erscheinen im Kontext (z.B. Pipeline-Metriken in M4)
- Dashboard-Widgets statt separater Seite
- Inline-Analytics in jedem Modul

**Empfehlung:** M6 in "Embedded Analytics" umbenennen und neu konzipieren

### 3. **Navigation - Inkonsequente Benennung**

**Problem:** Menüpunkte reflektieren noch funktionale statt prozessorientierte Sicht

**Ist-Zustand:**
- "Neukundengewinnung" → sollte "Opportunities" heißen
- "Kundenmanagement" → sollte "Kunden & Kontakte" heißen
- "Berichte" → sollte entfallen zugunsten kontextueller Analytics

### 4. **M8 (Rechner) - Dokumentations-Inkonsistenz**

**Problem:** In FC-002-M8-rechner.md steht noch viel über Migration als eigenständige Seite

**Tatsächliche Planung:** 
- Rechner ist korrekt als Modal konzipiert
- Wird aus Opportunity-Pipeline getriggert
- KEIN eigener Menüpunkt

**Empfehlung:** Dokument auf Modal-Only Ansatz reduzieren

## 📝 Dokumentations-Updates erforderlich

### Priorität 1 (Sofort):
1. **FC-002-M2-quick-create.md**
   - Von "System" zu "Kontextuelle Aktionen" 
   - Use Cases pro Kontext definieren
   
2. **FC-002-M6-berichte.md**
   - Komplett neu als "Embedded Analytics"
   - Verteilung auf Module dokumentieren

3. **FC-002-hub.md**
   - Navigation-Begriffe anpassen
   - Prozessorientierung betonen

### Priorität 2 (Vor Implementierung):
1. **FC-002-M8-rechner.md**
   - Legacy-Migrations-Teile entfernen
   - Fokus auf Modal-Integration

2. **FC-002-M4-neukundengewinnung.md**
   - Umbenennung in "opportunities.md"
   - Ist bereits korrekt konzipiert!

## 🚀 Empfohlene Sofortmaßnahmen

### 1. Begriffliche Klarheit schaffen
```yaml
Alt → Neu:
- "Neukundengewinnung" → "Opportunity Pipeline"
- "Quick-Create System" → "Kontextuelle Aktionen"
- "Berichte-Modul" → "Embedded Analytics"
- "Tools" → "Prozess-Werkzeuge"
```

### 2. Navigation anpassen
```typescript
// Neue Hauptnavigation
const navigation = [
  { label: "Mein Tag", path: "/cockpit" },
  { label: "Opportunities", path: "/opportunities" },
  { label: "Kunden & Kontakte", path: "/customers" },
  { label: "Einstellungen", path: "/settings" }
  // KEIN "Berichte", KEIN "Rechner"
];
```

### 3. Kontext-Framework definieren
```typescript
interface ProcessContext {
  module: string;
  stage?: string;
  entity?: Entity;
  availableActions: Action[];
  availableTools: Tool[];
}
```

## ✅ Was NICHT geändert werden muss

1. **Technische Implementierungspläne** - sind bereits korrekt
2. **API-Definitionen** - passen zum Hybrid-Modell
3. **Neue Features (FC-003 bis FC-007)** - sind bereits prozessorientiert
4. **Zeitschätzungen** - wurden bereits angepasst

## 🎯 Fazit

Die Inkonsistenzen sind hauptsächlich **konzeptioneller Natur** in der Dokumentation. Die tatsächliche technische Planung (besonders in FC-002-IMPLEMENTATION_PLAN.md und 2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md) ist bereits auf das Hybrid-Modell ausgerichtet.

**Empfehlung:** Dokumentations-Updates durchführen, aber KEINE Änderung der technischen Implementierung nötig.

---

**Nächster Schritt:** Entscheidung über Dokumentations-Updates vor oder nach der Implementierung