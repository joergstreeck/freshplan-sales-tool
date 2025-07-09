# Freshfoodz CI Implementation Plan

**Datum:** 09.07.2025  
**Status:** 🔄 In Arbeit  
**Ziel:** Vollständige Implementierung der Freshfoodz Corporate Identity im Frontend

## 🎨 CI-Standards (Referenz)

### Farben:
- **Primärgrün:** `#94C456` 
- **Dunkelblau:** `#004F7B`
- **Weiß:** `#FFFFFF`
- **Schwarz:** `#000000`

### Typografie:
- **Headlines:** Antonio Bold
- **Fließtext:** Poppins Regular/Medium

## ✅ Bereits umgesetzt

1. **Theme-Integration:**
   - ✅ ThemeProvider in AppProviders
   - ✅ freshfoodzTheme global aktiviert
   - ✅ CssBaseline für konsistente Baseline

2. **Sidebar Navigation:**
   - ✅ Freshfoodz-Farben implementiert
   - ✅ Antonio-Font für Logo
   - ✅ Poppins für Navigation-Items
   - ✅ Grüne Akzente für aktive States
   - ✅ Border und Hover-States in CI-Farben

3. **Layout:**
   - ✅ MainLayoutV2 mit 320px Sidebar
   - ✅ AppBar mit Antonio-Font

## 🔄 Zu überprüfende Komponenten

### Kritische UI-Komponenten:

1. **Buttons:**
   - [ ] Primary Buttons → #94C456 mit weißem Text
   - [ ] Secondary Buttons → #004F7B mit weißem Text
   - [ ] Text Buttons → #004F7B
   - [ ] Hover-States mit Transparenz

2. **Typography:**
   - [ ] Alle h1-h6 → Antonio Bold + #004F7B
   - [ ] Body Text → Poppins + #000000
   - [ ] Links → #94C456 mit Hover-Effekt

3. **Form Elements:**
   - [ ] TextField Focus → #94C456 Border
   - [ ] Checkbox/Radio → #94C456 wenn aktiv
   - [ ] Select Dropdowns → #94C456 Highlights

4. **Data Display:**
   - [ ] Tables → Header mit #004F7B Background
   - [ ] Cards → Subtle #94C456 Akzente
   - [ ] Chips → CI-konforme Farben

5. **Feedback:**
   - [ ] Success Messages → #94C456
   - [ ] Error Messages → Rot (Standard)
   - [ ] Info Messages → #004F7B

### Seiten-spezifische Anpassungen:

1. **Settings Page:**
   - [x] Sidebar bereits CI-konform
   - [ ] Tab-Leiste → #94C456 für aktive Tabs
   - [ ] User-Table Headers → #004F7B

2. **Cockpit Page:**
   - [ ] Header → Antonio Bold
   - [ ] Cards → CI-Akzente
   - [ ] Charts → #94C456 als Primärfarbe

3. **Calculator Page:**
   - [ ] Form Labels → Poppins
   - [ ] Result Display → Antonio für Zahlen
   - [ ] Submit Button → #94C456

## 📋 Implementierungs-Strategie

### Phase 1: Globale Komponenten (Heute)
1. Update aller MUI-Komponenten-Overrides im Theme
2. Erstelle wiederverwendbare CI-Komponenten
3. Teste Theme-Konsistenz

### Phase 2: Seiten-Migration (Morgen)
1. Settings Page komplett CI-konform
2. Cockpit Page Update
3. Calculator Page V2 direkt mit CI

### Phase 3: Legacy-Cleanup
1. Entfernung alter CSS-Klassen
2. Migration verbleibender Komponenten
3. Final Review

## 🚀 Nächste Schritte

1. **Theme-Overrides erweitern** für alle MUI-Komponenten
2. **CI-Component-Library** erstellen mit:
   - FreshButton
   - FreshCard
   - FreshTypography
3. **Storybook** für CI-Komponenten aufsetzen