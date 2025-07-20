# 🔍 FC-013 DUPLICATE DETECTION (KOMPAKT)

**Feature Code:** FC-013  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 3-4 Tage  
**Priorität:** HIGH - Datenqualität kritisch  
**ROI:** Verhindert Chaos bei 5000+ Kunden  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Doppelte Kundeneinträge = Vertriebschaos, verlorene Historie, peinliche Doppelansprache  
**Lösung:** Intelligente Duplicate Detection mit Fuzzy-Matching beim Anlegen  
**Impact:** 95% weniger Dubletten, saubere Datenbank, professioneller Auftritt  

---

## 🧠 WIE ES FUNKTIONIERT

```
NEUER KUNDE EINGEBEN
        ↓
[Fuzzy-Matching läuft]
   • Name (Levenshtein)
   • Email (Domain-Check)
   • Telefon (normalized)
        ↓
ÄHNLICHKEIT > 80%?
        ↓
[⚠️ Mögliche Dublette!]
        ↓
   Zeige Top 3 Matches:
   • FreshFoodz GmbH (95%)
   • Fresh Foods AG (82%)
   • FreshFoodz Berlin (81%)
        ↓
[Trotzdem anlegen] [Vorhandenen nutzen]
```

---

## 📋 FEATURES IM DETAIL

### 1. Multi-Field Fuzzy Matching

```typescript
interface DuplicateCheckResult {
  score: number;          // 0-100%
  matchedFields: {
    name: number;         // Levenshtein-Distanz
    email: number;        // Domain-Ähnlichkeit
    phone: number;        // Normalized Match
    address: number;      // Geo-Distance
  };
  suggestedAction: 'merge' | 'review' | 'create';
}
```

### 2. Smart Matching Algorithmen

```typescript
// Name Matching mit Varianten
"FreshFoodz GmbH" ≈ "Fresh Foodz GmbH" (95%)
"FreshFoodz" ≈ "FreshFoodz Berlin GmbH" (85%)
"Müller AG" ≈ "Mueller AG" (90% - Umlaut-Handling)

// Email Domain Intelligence
"info@freshfoodz.de" ≈ "kontakt@freshfoodz.de" (95%)
"max@freshfoodz.de" ≈ "max@fresh-foodz.de" (80%)

// Phone Normalization
"+49 30 12345" = "030 12345" = "030-123-45"
```

### 3. UI/UX Flow

```typescript
// In CustomerCreateModal.tsx
const DuplicateWarning: React.FC<{duplicates}> = () => (
  <Alert severity="warning" sx={{ mb: 2 }}>
    <AlertTitle>Mögliche Dublette gefunden!</AlertTitle>
    
    <List>
      {duplicates.map(dup => (
        <ListItem key={dup.id}>
          <ListItemText
            primary={`${dup.name} (${dup.score}% Übereinstimmung)`}
            secondary={
              <>
                <Chip label={dup.city} size="small" />
                <Chip label={`${dup.opportunityCount} Opportunities`} />
                <Chip label={`Erstellt: ${formatDate(dup.createdAt)}`} />
              </>
            }
          />
          <ListItemSecondaryAction>
            <Button onClick={() => navigateToCustomer(dup.id)}>
              Anzeigen
            </Button>
            <Button onClick={() => mergeWithCustomer(dup.id)}>
              Zusammenführen
            </Button>
          </ListItemSecondaryAction>
        </ListItem>
      ))}
    </List>
    
    <Box mt={2}>
      <Button variant="contained" onClick={createAnyway}>
        Trotzdem neu anlegen
      </Button>
      <Typography variant="caption" display="block" mt={1}>
        Tipp: Prüfen Sie zuerst die vorhandenen Einträge
      </Typography>
    </Box>
  </Alert>
);
```

### 4. Backend API

```java
@Path("/api/customers/check-duplicates")
@POST
@RolesAllowed({"sales", "admin"})
public Response checkDuplicates(DuplicateCheckRequest request) {
    List<DuplicateMatch> matches = customerService
        .findPotentialDuplicates(request)
        .stream()
        .filter(match -> match.getScore() > 70)
        .limit(5)
        .collect(Collectors.toList());
    
    return Response.ok(new DuplicateCheckResponse(matches)).build();
}
```

### 5. Konfigurierbare Schwellenwerte

```typescript
// Admin-Settings
interface DuplicateDetectionConfig {
  thresholds: {
    autoMerge: 95,      // Automatisch zusammenführen
    warning: 80,        // Warnung anzeigen
    suggestion: 70      // Als Vorschlag anzeigen
  };
  weights: {
    name: 0.4,
    email: 0.3,
    phone: 0.2,
    address: 0.1
  };
  ignoredWords: ['GmbH', 'AG', 'UG', 'e.K.'];
}
```

---

## 🎯 BUSINESS VALUE

- **Verhinderte Dubletten:** ~200 pro Monat bei 5000 Kunden
- **Zeitersparnis:** 5 Min pro verhinderte Dublette
- **Datenqualität:** Score steigt von 75% auf 95%
- **Compliance:** DSGVO-konform durch saubere Daten

---

## 🚀 QUICK WINS

1. **Phase 1:** Name + Email Matching (80% der Dubletten)
2. **Phase 2:** Phone + Address hinzufügen
3. **Phase 3:** ML-basiertes Matching
4. **Phase 4:** Bulk Merge Tool für Altdaten

---

## 🔗 ABHÄNGIGKEITEN

- **Benötigt:** Customer Entity (✅ vorhanden)
- **Nice-to-have:** Elasticsearch für Fuzzy Search
- **Integration:** Activity Timeline (zeigt Merge-Historie)

---

## 📊 SUCCESS METRICS

- **Duplicate Rate:** < 2% (aktuell ~10%)
- **False Positive Rate:** < 5%
- **User Acceptance:** > 90% folgen Empfehlungen
- **Data Quality Score:** > 95%

---

**Nächster Schritt:** Backend Fuzzy-Matching implementieren mit Apache Commons Text

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Entity & Repository
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - @RolesAllowed für API
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Integration in Create Dialog

### ⚡ Datenquellen:
- **[📥 FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_KOMPAKT.md)** - Bulk-Duplicate-Check beim Import
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Merge-Historie tracken
- **[📧 FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_KOMPAKT.md)** - Email-basierte Dubletten

### 🚀 Ermöglicht folgende Features:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Saubere Kundenlisten
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Eindeutige Kundenzuordnung
- **[📊 M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md)** - Korrekte Statistiken

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Admin-Menü für Settings
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Duplicate Detection Config
- **[📊 FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_KOMPAKT.md)** - Data Quality Metrics

### 🔧 Technische Details:
- **[FC-013_IMPLEMENTATION_GUIDE.md](./FC-013_IMPLEMENTATION_GUIDE.md)** - Fuzzy Matching Algorithmen
- **[FC-013_DECISION_LOG.md](./FC-013_DECISION_LOG.md)** - Elasticsearch vs. PostgreSQL
- **[MERGE_STRATEGY.md](./MERGE_STRATEGY.md)** - Merge-Konflikt-Auflösung