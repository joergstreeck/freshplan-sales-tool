# 🚀 Sprint 2.1.7.6 - Customer Lifecycle Management

**Sprint-ID:** 2.1.7.6
**Status:** 📋 PLANNING
**Priority:** P2 (Medium - nach Status Architecture)
**Estimated Effort:** 8h (1 Arbeitstag)
**Owner:** TBD
**Created:** 2025-10-19
**Updated:** 2025-10-19
**Dependencies:** Sprint 2.1.7.4 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

Automatische Lifecycle-Warnungen für Prospects und Advanced Seasonal Patterns:
- **PROSPECT Lifecycle:** Warnungen nach 60/90 Tagen (keine Auto-Archivierung!)
- **Advanced Seasonal Patterns:** Custom Month Picker, Pattern-Templates
- **Churn Prevention:** Frühwarnsystem für At-Risk Customers

**Key Deliverables:**
- PROSPECT Review Nightly Job (60/90 Tage Warnungen)
- Advanced Seasonal Pattern UI (Custom Month Picker)
- Dashboard Widgets (PROSPECT At-Risk, Seasonal Paused)

**Business Impact:**
- Kein Prospect geht "vergessen" (Warnungen nach 60 Tagen)
- Saisonbetriebe flexibel konfigurierbar
- Vertriebsleiter sieht "handlungsbedürftige" Prospects

---

## 📦 DELIVERABLES

### **1. PROSPECT Lifecycle Nightly Job** (3h)

**Ziel:** Automatische Warnungen für Prospects ohne Bestellung

**Business Rules:**
```
PROSPECT (0-60 Tage): Normal
  ↓
PROSPECT (60-90 Tage): ⚠️ Warnung an Owner
  ↓
PROSPECT (90+ Tage): 🚨 Review Required (kein Auto-Archive!)
```

**Service:**
```java
// ProspectLifecycleService.java (NEU!)

@Scheduled(cron = "0 0 2 * * *") // Daily 2 AM
public void checkProspectStatus() {
  List<Customer> prospects = Customer.find("status = 'PROSPECT'").list();

  for (Customer prospect : prospects) {
    int days = prospect.getDaysSinceConversion();

    if (days >= 60 && days < 90) {
      // Warnung (einmalig bei Tag 60)
      if (!prospect.hasTag("WARNED_60_DAYS")) {
        notifyOwner(prospect, "Prospect at risk: 60 days without order");
        prospect.addTag("WARNED_60_DAYS");
      }

    } else if (days >= 90) {
      // Review Required
      if (!prospect.hasTag("REVIEW_REQUIRED")) {
        prospect.addTag("REVIEW_REQUIRED");
        notifyOwner(prospect, "Prospect cold: 90+ days - please review!");
        notifyManager("Prospect Review Required: " + prospect.getCompanyName());
      }
    }
  }
}
```

**Dashboard Widget:**
```tsx
<Alert severity="warning">
  <AlertTitle>Prospects At Risk</AlertTitle>
  <Typography>
    5 Prospects ohne Bestellung seit >60 Tagen
  </Typography>
  <Button variant="contained" onClick={() => navigate('/prospects/at-risk')}>
    Jetzt reviewen
  </Button>
</Alert>
```

**Tests:** 8 Tests (Job Logic, Notification, Tag Management)

---

### **2. Advanced Seasonal Patterns UI** (3h)

**Ziel:** Flexibles UI für Seasonal Business Konfiguration

**Frontend - Edit Customer Dialog:**
```tsx
// SeasonalBusinessConfig Component

<FormControlLabel
  control={<Switch checked={isSeasonalBusiness} />}
  label="Saisonbetrieb"
/>

{isSeasonalBusiness && (
  <Box sx={{ mt: 2 }}>
    {/* Pattern Templates */}
    <Select label="Saison-Pattern" value={pattern}>
      <MenuItem value="SUMMER">Sommer (März-Oktober)</MenuItem>
      <MenuItem value="WINTER">Winter (Dezember-März)</MenuItem>
      <MenuItem value="SPRING">Frühling (März-Mai)</MenuItem>
      <MenuItem value="AUTUMN">Herbst (September-November)</MenuItem>
      <MenuItem value="CHRISTMAS">Weihnachten (November-Dezember)</MenuItem>
      <MenuItem value="CUSTOM">Benutzerdefiniert</MenuItem>
    </Select>

    {/* Custom Month Picker */}
    {pattern === 'CUSTOM' && (
      <MonthPicker
        label="Aktive Monate"
        value={selectedMonths}
        onChange={setSelectedMonths}
        helperText="Wählen Sie alle Monate, in denen der Kunde aktiv ist"
      />
    )}

    {/* Preview */}
    <Alert severity="info" sx={{ mt: 2 }}>
      <Typography variant="caption">
        Churn-Monitoring aktiv: {getActiveMonthsLabel(selectedMonths)}
        <br />
        Churn-Monitoring pausiert: {getInactiveMonthsLabel(selectedMonths)}
      </Typography>
    </Alert>
  </Box>
)}
```

**Month Picker Component:**
```tsx
// MonthPicker.tsx - Reusable Component

const MONTHS = [
  { value: 1, label: 'Jan' },
  { value: 2, label: 'Feb' },
  // ... 3-12
];

export const MonthPicker = ({ value, onChange }) => {
  const handleToggle = (month) => {
    const newValue = value.includes(month)
      ? value.filter(m => m !== month)
      : [...value, month].sort();
    onChange(newValue);
  };

  return (
    <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 1 }}>
      {MONTHS.map(month => (
        <Chip
          key={month.value}
          label={month.label}
          onClick={() => handleToggle(month.value)}
          color={value.includes(month.value) ? 'primary' : 'default'}
          variant={value.includes(month.value) ? 'filled' : 'outlined'}
        />
      ))}
    </Box>
  );
};
```

**Tests:** 5 Tests (Month Picker, Pattern Templates, Validation)

---

### **3. Dashboard Enhancements** (2h)

**Ziel:** Neue Metrics für PROSPECT und Seasonal

**Metrics:**
```java
public record CustomerMetrics(
  // ... existing ...

  // NEW (Sprint 2.1.7.6):
  int prospectsAtRisk,       // 60-90 days without order
  int prospectsReviewRequired, // 90+ days
  int seasonalInSeason,      // Currently active
  int seasonalOffSeason      // Currently paused
) {}
```

**Dashboard Widgets:**
```tsx
<Grid container spacing={2}>
  <Grid item xs={12} md={3}>
    <MetricCard
      title="Prospects At Risk"
      value={metrics.prospectsAtRisk}
      subtitle="60+ Tage ohne Bestellung"
      icon={<WarningIcon />}
      color="warning"
      onClick={() => navigate('/prospects/at-risk')}
    />
  </Grid>

  <Grid item xs={12} md={3}>
    <MetricCard
      title="Review Required"
      value={metrics.prospectsReviewRequired}
      subtitle="90+ Tage - Aktion erforderlich"
      icon={<NotificationImportantIcon />}
      color="error"
      onClick={() => navigate('/prospects/review')}
    />
  </Grid>
</Grid>
```

**Tests:** 3 Tests (Metrics Calculation, Widget Rendering)

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 16 Tests (8 Lifecycle + 8 Service Logic)
- Frontend: 8 Tests (5 Month Picker + 3 Dashboard)
- **Total: 24 Tests**

**Business Impact:**
- ✅ Kein Prospect geht vergessen (60/90 Tage Warnungen)
- ✅ Flexibles Seasonal Pattern Management
- ✅ Dashboard zeigt handlungsbedürftige Prospects

---

## ✅ DEFINITION OF DONE

### **Functional**
- [x] PROSPECT Lifecycle Nightly Job läuft
- [x] Warnungen nach 60/90 Tagen funktionieren
- [x] Seasonal Pattern UI vollständig
- [x] Dashboard Metrics aktualisiert

### **Technical**
- [x] ProspectLifecycleService implementiert
- [x] MonthPicker Component reusable
- [x] Notifications an Owner + Manager
- [x] Tag-System für "WARNED_60_DAYS", "REVIEW_REQUIRED"

### **Quality**
- [x] Tests: 24/24 GREEN
- [x] TypeScript: type-check PASSED
- [x] Code Review: Self-reviewed

---

## 📅 TIMELINE

**Tag 1 (8h):**
- PROSPECT Lifecycle Job (3h)
- Seasonal Pattern UI (3h)
- Dashboard Enhancements (2h)

**Total:** 8h (1 Arbeitstag)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
- ProspectLifecycleService
- MonthPicker Component
- Dashboard Metrics

**Design Decisions:**
- Warum keine Auto-Archivierung?
- Warum 60/90 Tage Schwellwerte?

**Related Work:**
- Sprint 2.1.7.4: Seasonal Business Foundation
- Sprint 2.1.7.2: Xentral Integration (für automatische Aktivierung)

---

## 🎯 NÄCHSTE SCHRITTE

1. Sprint 2.1.7.4 abschließen
2. Sprint 2.1.7.6 starten (Customer Lifecycle Management)

---

## 📝 NOTES

### **User-Decision (2025-10-19)**

**Warnungen statt Auto-Archivierung:**
- NICHT automatisch archivieren nach 90 Tagen
- Stattdessen: Warnungen + manuelle Review
- Begründung: B2B Food hat oft 3-6 Monate Vorlauf

**Scope-Aufteilung:**
- Sprint 2.1.7.4: Seasonal Business Flag + Monate (DB + Basic Logic)
- Sprint 2.1.7.6: PROSPECT Lifecycle + Advanced Patterns

---

**✅ SPRINT STATUS: 📋 PLANNING - Wartet auf Sprint 2.1.7.4**

**Letzte Aktualisierung:** 2025-10-19 (Sprint neu angelegt)
