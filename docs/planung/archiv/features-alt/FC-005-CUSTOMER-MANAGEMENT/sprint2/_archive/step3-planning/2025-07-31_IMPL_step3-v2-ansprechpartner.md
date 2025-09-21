# 👥 Step 3 V2 - Ansprechpartner mit Standort-Zuordnung

**Datum:** 31.07.2025  
**Sprint:** Sprint 2 - Customer UI Integration  
**Status:** ✅ Implementiert

---

## 📋 Was wurde umgesetzt?

Step 3 wurde zu V2 erweitert mit folgenden Features:

### 1. Mehrere Ansprechpartner
- Dynamisches Hinzufügen/Entfernen von Kontakten
- Hauptansprechpartner ist geschützt (kann nicht gelöscht werden)
- Klare visuelle Kennzeichnung des Primärkontakts

### 2. Dropdown-Felder mit Freitext-Option
- **Titel**: Vordefinierte Optionen (Dr., Prof., etc.) + individuelle Eingabe
- **Position/Funktion**: Umfangreiche Liste typischer Positionen + individuelle Eingabe
- Verwendet Material-UI Autocomplete mit `freeSolo` Option

### 3. Standort-Zuordnung
- **Bei Einzelbetrieb**: Automatisch ausgeblendet
- **Bei Ketten**: 
  - "Für alle Standorte" (Standard)
  - "Für bestimmte Standorte" mit Checkbox-Liste
- Visuelle Gruppierung in grauem Box-Container

### 4. Strukturierte Erfassung
- Getrennte Felder: Anrede, Titel, Vorname, Nachname
- Entscheider-Ebene Dropdown (Entscheider, Beeinflusser, Gatekeeper, Nutzer)
- Kontaktdaten: E-Mail (Pflicht), Telefon, Mobil

---

## 🔧 Technische Details

### Neue Komponenten:
- `Step3AnsprechpartnerV2.tsx` - Hauptkomponente
- `contact.types.ts` - TypeScript Type Definitionen

### Dropdown-Listen:

**Titel-Optionen:**
```typescript
const TITLE_OPTIONS = [
  'Dr.', 'Prof.', 'Prof. Dr.', 
  'Dipl.-Ing.', 'Dipl.-Kfm.', 'Dipl.-Inf.',
  'M.A.', 'M.Sc.', 'B.A.', 'B.Sc.', 'MBA'
];
```

**Positions-Optionen (Auszug):**
- Management: Geschäftsführer/in, Direktor/in, Inhaber/in
- Hotel: Hoteldirektor/in, F&B Manager/in, Küchenchef/in
- Krankenhaus: Verwaltungsdirektor/in, Küchenleitung
- Betriebsrestaurant: Kantinenchef/in, Gastronomiemanager/in

### Store-Integration:
- Kontakte werden als Array in `customerData.contacts` gespeichert
- Automatische Synchronisation mit dem Store bei jeder Änderung

---

## 🎨 UI/UX Features

### Adaptive Layout:
- Verwendet `AdaptiveFormContainer` für responsive Feldanordnung
- Flexible Box-Layouts mit `flexWrap` für mobile Ansicht
- Intelligente Feldgrößen basierend auf Inhalt

### Visuelles Feedback:
- Kontakt-Vorschau Card zeigt formatierten Namen
- Primary-Kontakt mit Chip gekennzeichnet
- Lösch-Button nur bei Nicht-Primary-Kontakten
- Fehlerhinweis wenn keine Standorte ausgewählt

### Benutzerführung:
- Info-Box mit Tipp zur Standort-Zuordnung
- Placeholder-Texte für bessere Orientierung
- Icons zur visuellen Unterstützung

---

## ✅ Vorteile der Implementierung

1. **Flexibilität**: Freitext + vordefinierte Optionen
2. **Skalierbarkeit**: Von Einzelbetrieb bis Großkette
3. **Datenkonsistenz**: Strukturierte Erfassung verhindert Fehler
4. **Vertriebsfokus**: Entscheider-Ebene hilft bei der Ansprache
5. **Zukunftssicher**: Vorbereitet für Backend-Integration

---

## 🚀 Nächste Schritte

Frontend für Sprint 2 ist damit komplett:
- ✅ Step 1: Basis & Filialstruktur
- ✅ Step 2: Herausforderungen & Potenzial (mit Kalkulationshilfe)
- ✅ Step 3: Ansprechpartner V2
- ✅ Step 4: Angebot & Services

Als nächstes stehen die Backend-Anpassungen an (TODOs 19-23).