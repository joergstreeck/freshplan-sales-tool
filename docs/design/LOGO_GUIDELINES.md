# FreshPlan Logo Guidelines

**Erstellt:** 09.07.2025  
**Status:** ✅ Verbindlich  

## 📍 Offizielles Logo

### Primäres Logo
- **Datei:** `freshfoodzlogo.png`
- **Größe:** 19 KB
- **Original-Pfad:** `/Users/joergstreeck/freshplan-testing/legacy/assets/images/freshfoodzlogo.png`
- **Projekt-Pfad:** `/public/freshplan-logo.png`

### Logo-Spezifikationen
- **Format:** PNG mit Transparenz
- **Verwendung:** Header, Login-Seite, Dokumente
- **Mindestgröße:** 32px Höhe (Mobile)
- **Standardgröße:** 40px Höhe (Desktop)

## 🎨 Logo-Integration im Code

### Header-Integration
```typescript
// Desktop - Volles Logo
<Logo 
  variant="full" 
  height={40}
  onClick={() => navigate('/')}
/>

// Mobile - Icon-Version
<Logo 
  variant="icon" 
  height={32}
  onClick={() => navigate('/')}
/>
```

### Logo-Komponente
**Pfad:** `/frontend/src/components/common/Logo.tsx`

Die Logo-Komponente bietet:
- Automatisches Fallback bei fehlender Bilddatei
- Responsive Größenanpassung
- Click-Handler für Navigation
- Freshfoodz CI-konforme Farben im Fallback

## 🔒 Logo-Schutz

### Dos:
- ✅ Logo immer auf weißem oder sehr hellem Hintergrund
- ✅ Ausreichend Freiraum um das Logo (min. 16px)
- ✅ Logo als klickbares Element zur Startseite
- ✅ Proportionen beibehalten

### Don'ts:
- ❌ Logo nicht verzerren oder strecken
- ❌ Keine Farbänderungen am Logo
- ❌ Kein Text direkt am Logo
- ❌ Nicht auf farbigem Hintergrund ohne weißen Container

## 📐 Layout-Richtlinien

### Header-Layout
```
[Logo] [-------------- Suche --------------] [Notifications] [User Menu]
  40px         max-width: 500px                                         
```

### Mobile Header
```
[☰] [Icon] -------------------- [🔔] [Avatar]
     32px                                    
```

## 🎯 Verwendungsorte

1. **Header** - Immer sichtbar, links positioniert
2. **Login-Seite** - Zentriert, größere Darstellung (80px)
3. **Loading-States** - Als Spinner-Alternative
4. **Fehlerseiten** - Mit Fehlertext kombiniert
5. **E-Mail-Templates** - Header von Benachrichtigungen
6. **PDF-Exports** - Dokumenten-Header

## 🔄 Fallback-Strategie

Falls das Logo-Bild nicht lädt, zeigt die Logo-Komponente:
- Grüner Kreis (#94C456) mit Blumen-Icon
- "FreshPlan" Text in Antonio Bold (#004F7B)
- Gleiche Klick-Funktionalität

## 📋 Checkliste für neue Seiten

- [ ] Logo im Header sichtbar?
- [ ] Logo klickbar und führt zu "/"?
- [ ] Ausreichend Freiraum vorhanden?
- [ ] Mobile-Version getestet?
- [ ] Fallback funktioniert?

## 🚀 Zukünftige Erweiterungen

1. **SVG-Version** für bessere Skalierung
2. **Dark-Mode-Variante** (weißes Logo)
3. **Animierte Version** für Ladebildschirme
4. **Favicon-Set** für Browser-Tabs

---

**Hinweis:** Bei Fragen zur Logo-Verwendung bitte das Design-Team kontaktieren.