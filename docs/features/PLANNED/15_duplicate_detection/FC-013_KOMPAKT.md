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