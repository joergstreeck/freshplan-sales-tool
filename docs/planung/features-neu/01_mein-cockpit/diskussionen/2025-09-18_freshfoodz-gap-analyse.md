# 🍃 FreshFoodz Gap-Analyse für Sales Cockpit

**Erstellt:** 2025-09-18
**Basis:** Code-Analyse von SalesCockpitV2.tsx und Backend-Services
**Zweck:** Präzise Bewertung was für FreshFoodz Cook&Fresh® Cockpit fehlt

---

## 🎯 **COCKPIT-KERNFUNKTIONEN: STATUS**

### ✅ **SOLIDE BASIS VORHANDEN**

#### **Infrastructure & Layout:**
- **✅ 3-Spalten-Dashboard:** SalesCockpitV2.tsx production-ready
- **✅ ResizablePanels:** Professional drag-and-drop mit localStorage
- **✅ MUI-Integration:** Alle Komponenten bereits migriert
- **✅ Lazy Loading:** Suspense + ColumnSkeleton implementiert
- **✅ Backend CQRS:** SalesCockpitQueryService optimiert, 19/19 Tests grün

#### **Basic Dashboard Features:**
- **✅ Header-KPIs:** Kunden gesamt, Aktive, Risiko, Überfällig
- **✅ User Authentication:** useAuth() Hook integriert
- **✅ Data Fetching:** useDashboardData() React Query Hook
- **✅ Industry Classification:** HOTEL, RESTAURANT, CATERING, KANTINE = perfekt!

---

## ❌ **FEHLENDE FreshFoodz-DOMÄNE**

### **Spalte 1: "Mein Tag" → "Genussberater-Tag"**

#### **Aktuell (generisch):**
```typescript
// MyDayColumnMUI.tsx zeigt:
- Generische Aufgaben/Termine
- Standard CRM-Alerts
- Unspezifische KPIs
```

#### **FreshFoodz-Anforderung:**
```typescript
interface GenussberaterDay {
  sampleFeedbacks: SampleTestResult[];      // ❌ Fehlt komplett
  roiTermineHeute: ROIConsultation[];       // ❌ Fehlt komplett
  seasonalOpportunities: SeasonalLead[];    // ❌ Fehlt komplett
  cookFreshProductUpdates: ProductNews[];   // ❌ Fehlt komplett
}
```

### **Spalte 2: "Fokus-Liste" → "Multi-Channel-Pipeline"**

#### **Aktuell (generisch):**
```typescript
// FocusListColumnMUI.tsx zeigt:
- Standard Kundenliste
- Keine Channel-Unterscheidung
- Generische Filterung
```

#### **FreshFoodz-Anforderung:**
```typescript
interface MultiChannelPipeline {
  channelFilter: "all" | "direct" | "partner";     // ❌ Fehlt komplett
  businessTypeFilter: Industry[];                  // ✅ Industry enum vorhanden
  channelConflictAlerts: ConflictAlert[];          // ❌ Fehlt komplett
  sampleStatusTracking: SampleStatus[];           // ❌ Fehlt komplett
}
```

### **Spalte 3: "Aktions-Center" → "Account-Intelligence + ROI-Tools"**

#### **Aktuell (generisch):**
```typescript
// ActionCenterColumnMUI.tsx zeigt:
- Standard Kundendetails
- Generische Aktionen
- Keine FreshFoodz-Tools
```

#### **FreshFoodz-Anforderung:**
```typescript
interface AccountIntelligence {
  roiCalculatorModal: ROICalculatorModal;          // ❌ Fehlt komplett
  sampleHistory: SampleTestHistory[];             // ❌ Fehlt komplett
  cookFreshAffinityScore: number;                 // ❌ Fehlt komplett
  channelAssignmentInfo: ChannelInfo;             // ❌ Fehlt komplett
}
```

---

## 🏗️ **KONKRETE ENTWICKLUNGSARBEIT ERFORDERLICH**

### **Backend (3-5 Tage):**

#### **1. Customer Entity erweitern:**
```java
@Entity Customer {
  // ✅ Industry bereits kompatibel

  @Enumerated(EnumType.STRING)
  private ChannelType channelType;               // 🔧 NEU hinzufügen

  @Column(name = "cookfresh_affinity_score")
  private Integer cookFreshAffinityScore;        // 🔧 NEU hinzufügen

  @Column(name = "partner_assignment")
  private String partnerAssignment;              // 🔧 NEU hinzufügen
}
```

#### **2. SalesCockpitQueryService erweitern:**
```java
// ✅ Service bereits optimiert, nur Methods hinzufügen:
public FreshFoodzKPIs getFreshFoodzDashboardKPIs(UUID userId);
public List<SampleTestSummary> getTodaysSampleTests(UUID userId);
public List<ChannelAccount> getChannelAwareAccounts(ChannelFilter filter);
```

#### **3. Neue Enums/DTOs:**
```java
public enum ChannelType { DIRECT, PARTNER }      // 🔧 NEU erstellen
public class SampleTestResult { ... }            // 🔧 NEU erstellen
public class ROIConsultation { ... }             // 🔧 NEU erstellen
```

### **Frontend (5-7 Tage):**

#### **1. MyDayColumnMUI für FreshFoodz anpassen:**
```typescript
// ✅ Komponente vorhanden, nur Content ändern:
- Generische Tasks → Sample-Feedback-Alerts
- Standard KPIs → Cook&Fresh® ROI-Termine
- CRM-Alerts → Seasonal Opportunities
```

#### **2. FocusListColumnMUI um Channel-Filter erweitern:**
```typescript
// ✅ Komponente vorhanden, nur Filter hinzufügen:
<ChannelFilter
  options={["all", "direct", "partner"]}
  onChannelChange={handleChannelFilter}
/>
```

#### **3. ActionCenterColumnMUI um ROI-Modal erweitern:**
```typescript
// ✅ Komponente vorhanden, Modal hinzufügen:
<ROICalculatorModal
  open={roiModalOpen}
  account={selectedAccount}
  onCalculationComplete={handleROIResult}
/>
```

---

## ⏱️ **REALISTISCHER AUFWAND**

### **Phase 1: Multi-Channel Foundation (2-3 Wochen)**
- **Backend:** ChannelType + FreshFoodz KPIs (1 Woche)
- **Frontend:** Channel-Filter + FreshFoodz-Header-KPIs (1-2 Wochen)

### **Phase 2: ROI-Calculator Integration (2 Wochen)**
- **ROI-Modal-Component:** Neue Entwicklung (1 Woche)
- **Integration in ActionCenterColumnMUI:** (1 Woche)

### **Gesamtaufwand: 4-5 Wochen** (statt ursprünglich 12 Wochen!)

---

## 💡 **STRATEGISCHE ERKENNTNISSE**

### **Positive Überraschung:**
- **90% Infrastructure bereits da** → Fokus auf Geschäftslogik!
- **Industry Enum perfekt** → Keine Datenmodel-Migration nötig
- **Backend CQRS optimiert** → Performance bereits gelöst

### **FreshFoodz-Transformation notwendig:**
- **Domain-Logic:** Cook&Fresh® statt generisches CRM
- **Multi-Channel:** Direct/Partner-Vertrieb statt Single-Channel
- **ROI-Tools:** Gastronomy-Beratung statt Standard-Sales

### **Architektur-Entscheidung bestätigt:**
- **3-Spalten-Layout optimal** für FreshFoodz Genussberater-Workflow
- **Modal-Integration** für ROI-Calculator besser als 4. Spalte
- **Channel-Filter** in bestehender Pipeline besser als separate View

---

## 🎯 **NÄCHSTE KONKRETE SCHRITTE**

1. **ChannelType Enum** erstellen und Customer Entity erweitern
2. **MyDayColumnMUI** für Cook&Fresh®-KPIs anpassen
3. **FocusListColumnMUI** um Direct/Partner-Filter erweitern
4. **ROI-Calculator-Modal** als neue Komponente entwickeln

**Das Cockpit ist das Herzstück - und die Basis ist besser als erwartet! 🚀**