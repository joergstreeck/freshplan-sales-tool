# 🎯 UI CONSISTENCY STRATEGY - Layout, Design & Sprachregeln

**Erstellt:** 17.07.2025 18:45  
**Zweck:** Sicherstellen dass JEDER Menüpunkt gleich aussieht und spricht

---

## 🏗️ 3-SÄULEN-STRATEGIE für Konsistenz

### 1️⃣ **TECHNISCHE EBENE - Theme + Components**

```typescript
// frontend/src/theme/freshfoodz.ts - ERWEITERN mit UI-Konstanten
export const freshfoodzTheme = createTheme({
  // Bestehende Farben und Fonts...
  
  // NEU: UI-Konstanten für Konsistenz
  custom: {
    layout: {
      headerHeight: 64,
      sidebarWidth: 240,
      contentPadding: 3,
    },
    components: {
      cardElevation: 2,
      borderRadius: 8,
      transitionDuration: '0.3s',
    }
  }
});

// NEU: Base Components mit eingebauten Standards
// frontend/src/components/base/PageWrapper.tsx
export const PageWrapper = ({ title, children }) => (
  <Box sx={{ p: theme => theme.custom.layout.contentPadding }}>
    <Typography variant="h4" sx={{ mb: 3 }}>
      {t(title)} {/* IMMER durch i18n! */}
    </Typography>
    {children}
  </Box>
);

// frontend/src/components/base/SectionCard.tsx
export const SectionCard = ({ title, actions, children }) => (
  <Card sx={{ 
    elevation: theme => theme.custom.components.cardElevation,
    borderRadius: theme => theme.custom.components.borderRadius 
  }}>
    <CardHeader 
      title={t(title)} 
      action={actions}
      sx={{ borderBottom: 1, borderColor: 'divider' }}
    />
    <CardContent>{children}</CardContent>
  </Card>
);
```

### 2️⃣ **SPRACHLICHE EBENE - Zentralisierte Übersetzungen**

```typescript
// frontend/src/i18n/translations.ts - SINGLE SOURCE OF TRUTH
export const UI_TERMS = {
  // Navigation
  'nav.dashboard': 'Übersicht',
  'nav.cockpit': 'Verkaufszentrale',
  'nav.customers': 'Kunden',
  'nav.settings': 'Einstellungen',
  
  // Sections
  'section.myDay': 'Mein Tag',
  'section.inbox': 'Posteingang',
  'section.focusList': 'Arbeitsliste',
  'section.actionCenter': 'Arbeitsbereich',
  
  // Actions - IMMER gleiche Formulierung!
  'action.create': 'Neu anlegen',
  'action.edit': 'Bearbeiten',
  'action.delete': 'Löschen',
  'action.save': 'Speichern',
  'action.cancel': 'Abbrechen',
  
  // Status
  'status.loading': 'Lädt...',
  'status.saving': 'Speichert...',
  'status.success': 'Erfolgreich gespeichert',
  'status.error': 'Fehler aufgetreten',
} as const;

// Type-Safety für Übersetzungen
type TranslationKey = keyof typeof UI_TERMS;
```

### 3️⃣ **PROZESS-EBENE - Entwickler-Guidelines**

```typescript
// frontend/src/guidelines/UI_DEVELOPMENT_RULES.md

## ⚡ QUICK CHECKLIST für JEDEN neuen Screen:

1. **Layout Template verwenden:**
   ```tsx
   <MainLayoutV2>
     <PageWrapper title="nav.customers">
       <SectionCard title="section.customerList">
         {/* Content */}
       </SectionCard>
     </PageWrapper>
   </MainLayoutV2>
   ```

2. **NIEMALS Hardcoded Text:**
   ❌ <Button>Save</Button>
   ❌ <Button>Speichern</Button>
   ✅ <Button>{t('action.save')}</Button>

3. **Theme-Werte nutzen:**
   ❌ sx={{ p: 3, borderRadius: 8 }}
   ✅ sx={{ p: theme => theme.custom.layout.contentPadding }}

4. **Standard-Komponenten:**
   - PageWrapper für ALLE Seiten
   - SectionCard für ALLE Bereiche
   - StandardTable für ALLE Listen
   - FormDialog für ALLE Formulare
```

---

## 🚀 IMPLEMENTIERUNGS-PLAN

### Phase 1: Base Components (2 Tage)
```bash
frontend/src/components/base/
├── PageWrapper.tsx      # Seiten-Container
├── SectionCard.tsx      # Bereichs-Karten
├── StandardTable.tsx    # Einheitliche Tabellen
├── FormDialog.tsx       # Einheitliche Dialoge
├── ActionButtons.tsx    # Standard-Buttons
└── index.ts            # Re-exports
```

### Phase 2: Translation Enforcement (1 Tag)
```typescript
// ESLint Rule für hardcoded Text
{
  "rules": {
    "no-hardcoded-text": ["error", {
      "ignore": ["data-testid", "aria-label"]
    }]
  }
}

// Custom Hook mit Type-Safety
export const useTranslation = () => {
  const { t } = useTranslation();
  return {
    t: (key: TranslationKey) => t(key)
  };
};
```

### Phase 3: Migration Script (1 Tag)
```typescript
// Script zum Finden von Verstößen
const findHardcodedText = () => {
  // Suche nach <Typography>, <Button>, etc. ohne t()
  // Generiere Report mit allen Fundstellen
};

// Automatische Korrektur wo möglich
const autoFixTranslations = () => {
  // Ersetze bekannte Terme mit t() calls
};
```

---

## 📋 ENFORCEMENT-MECHANISMEN

### 1. **Pre-Commit Hooks**
```json
{
  "husky": {
    "hooks": {
      "pre-commit": "npm run lint:ui-consistency"
    }
  }
}
```

### 2. **Component Library Docs**
- Storybook mit ALLEN Base Components
- Copy-Paste Templates für häufige Patterns
- DO's and DON'Ts visuell dargestellt

### 3. **PR-Template**
```markdown
## UI Consistency Checklist
- [ ] Verwendet PageWrapper für Layout
- [ ] Alle Texte über t() 
- [ ] Theme-Werte statt Hardcoded
- [ ] Screenshot beigefügt
- [ ] Sprachregeln befolgt (UI_SPRACHREGELN.md)
```

### 4. **Automated Tests**
```typescript
describe('UI Consistency', () => {
  it('should use translation keys for all visible text', () => {
    render(<CustomerPage />);
    // Prüfe dass keine hardcoded Texte existieren
  });
  
  it('should follow theme spacing', () => {
    // Prüfe dass Components theme.custom nutzen
  });
});
```

---

## 🎯 ERWARTETES ERGEBNIS

**Entwickler kann NICHT mehr "falsch" entwickeln:**
1. Base Components erzwingen Layout-Konsistenz
2. TypeScript erzwingt Übersetzungs-Keys  
3. ESLint verhindert Hardcoded Text
4. PR-Checks sichern Qualität

**User Experience:**
- JEDE Seite sieht gleich aus
- JEDE Aktion heißt gleich
- KEINE Überraschungen in Navigation
- 100% Deutsche Oberfläche

---

## 📝 NÄCHSTE SCHRITTE

1. **Base Components** als erstes erstellen
2. **UI_TERMS** konsolidieren aus UI_SPRACHREGELN.md
3. **ESLint Rules** konfigurieren
4. **Migration** der bestehenden Screens
5. **Storybook** Documentation

**Mit dieser Strategie ist Konsistenz GARANTIERT, nicht gehofft!** 🚀