# 📊 FC-011 BONITÄTSPRÜFUNG & VERTRAGSMANAGEMENT ⚡

**Feature Code:** FC-011  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 8-10 Tage  
**Priorität:** HIGH - Kritisch für Angebotsprozess  
**ROI:** Risikominimierung + automatisierte Vertragsverwaltung  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Keine Bonitätsprüfung = Zahlungsrisiko, manuelle Vertragserstellung = Zeitverlust  
**Lösung:** Integrierte Bonitätsprüfung + automatische PDF-Generierung mit Kundendaten  
**Impact:** 90% weniger Zahlungsausfälle, 15 Min → 1 Min Vertragserstellung  

---

## 🧭 INTUITIVE NUTZERFÜHRUNG

```
OPPORTUNITY STAGE: "Qualified"
         ↓
[🔍 Bonitätsprüfung starten]
         ↓
    ✅ Geprüft → Weiter
    ❌ Risiko → Stopp/Eskalation
         ↓
[📄 Vertrag erstellen]
         ↓
    Automatisch befüllt:
    • Kundendaten
    • Konditionen
    • Laufzeit
         ↓
[📧 Senden] oder [🖨️ Drucken]
         ↓
KUNDE: Vertrag gespeichert
    + Erinnerung vor Ablauf
```

---

## 📋 FEATURES IM DETAIL

### 1. Bonitätsprüfung Integration
```typescript
// In OpportunityCard.tsx
{stage === 'qualified' && !creditCheckDone && (
  <Alert severity="warning">
    <AlertTitle>Bonitätsprüfung erforderlich</AlertTitle>
    Vor Angebotserstellung muss die Bonität geprüft werden.
    <Button 
      startIcon={<SecurityIcon />}
      onClick={handleCreditCheck}
      sx={{ ml: 2 }}
    >
      Jetzt prüfen
    </Button>
  </Alert>
)}
```

### 2. Smart Contract Generator
```typescript
interface ContractData {
  customer: Customer;
  creditCheck: CreditCheckResult;
  contractType: 'partnership' | 'standard';
  duration: number; // Monate
  startDate: Date;
  discountTier: string;
  specialConditions?: string[];
}

// Automatisches Befüllen
const generateContract = async (data: ContractData) => {
  const template = await loadTemplate('partnership');
  
  return fillTemplate(template, {
    customerName: data.customer.name,
    customerNumber: data.customer.number,
    address: formatAddress(data.customer),
    creditLimit: data.creditCheck.approvedLimit,
    validUntil: addMonths(data.startDate, data.duration),
    rabattplan: calculateDiscountTier(data.customer.revenue),
    // ... weitere Felder
  });
};
```

### 3. Vertragsverwaltung beim Kunden
```typescript
// CustomerContracts.tsx
export const CustomerContracts: React.FC<{customerId: string}> = () => {
  const contracts = useCustomerContracts(customerId);
  
  return (
    <Card>
      <CardHeader title="Verträge & Vereinbarungen" />
      <CardContent>
        {contracts.map(contract => (
          <ContractCard key={contract.id}>
            <Typography variant="h6">
              {contract.type} - Gültig bis {formatDate(contract.validUntil)}
            </Typography>
            
            {isExpiringSoon(contract) && (
              <Alert severity="warning">
                Vertrag läuft in {daysUntil(contract.validUntil)} Tagen aus!
                <Button onClick={() => renewContract(contract)}>
                  Verlängern
                </Button>
              </Alert>
            )}
            
            <ButtonGroup>
              <Button startIcon={<CalculateIcon />}>
                Neues Angebot
              </Button>
              <Button startIcon={<EmailIcon />}>
                Per E-Mail senden
              </Button>
              <Button startIcon={<PrintIcon />}>
                Drucken
              </Button>
            </ButtonGroup>
          </ContractCard>
        ))}
      </CardContent>
    </Card>
  );
};
```

### 4. Automatische Erinnerungen
```java
@Scheduled(cron = "0 9 * * *") // Täglich 9 Uhr
public void checkExpiringContracts() {
    var expiringContracts = contractRepository
        .findExpiringWithin(30) // 30 Tage vorher
        .filter(c -> !c.isReminderSent());
    
    expiringContracts.forEach(contract -> {
        // Notification an Verkäufer
        notificationService.send(
            contract.getSalesRep(),
            "Vertrag läuft aus: " + contract.getCustomerName(),
            NotificationType.CONTRACT_EXPIRING
        );
        
        // Task in Sales Cockpit
        taskService.createTask(
            "Vertrag verlängern: " + contract.getCustomerName(),
            contract.getSalesRep(),
            contract.getExpiryDate().minusDays(7)
        );
        
        contract.setReminderSent(true);
    });
}
```

---

## 🔗 INTEGRATION MIT OPPORTUNITY PIPELINE

### Stage-Gates implementieren:
```java
public enum OpportunityStage {
    LEAD(0, "Neu"),
    QUALIFIED(10, "Qualifiziert", 
        new StageRequirement[]{
            StageRequirement.CONTACT_INFO_COMPLETE
        }),
    CREDIT_CHECKED(15, "Bonität geprüft", // NEU!
        new StageRequirement[]{
            StageRequirement.CREDIT_CHECK_PASSED
        }),
    PROPOSAL(20, "Angebot", 
        new StageRequirement[]{
            StageRequirement.CREDIT_CHECK_PASSED,
            StageRequirement.CONTRACT_VALID
        }),
    // ...
}
```

---

## 📊 BONITÄTSPRÜFUNG WORKFLOW

### Externe Integration (Phase 1: Mock)
```typescript
interface CreditCheckService {
  checkCredit(customer: Customer): Promise<CreditCheckResult>;
}

interface CreditCheckResult {
  status: 'approved' | 'rejected' | 'manual_review';
  score: number; // 0-100
  creditLimit: number;
  riskLevel: 'low' | 'medium' | 'high';
  validUntil: Date;
  notes?: string;
}

// Mock Implementation
class MockCreditCheckService implements CreditCheckService {
  async checkCredit(customer: Customer): Promise<CreditCheckResult> {
    // Simulation basierend auf Umsatz
    const score = Math.min(100, customer.revenue / 10000);
    
    return {
      status: score > 30 ? 'approved' : 'rejected',
      score,
      creditLimit: customer.revenue * 0.2, // 20% vom Jahresumsatz
      riskLevel: score > 70 ? 'low' : score > 40 ? 'medium' : 'high',
      validUntil: addMonths(new Date(), 12)
    };
  }
}
```

---

## 🎨 UI/UX DESIGN

### Bonitätsprüfung Dialog
```
┌─────────────────────────────────────┐
│ 🔍 Bonitätsprüfung                  │
├─────────────────────────────────────┤
│ Kunde: Müller GmbH                  │
│ Umsatz: 500.000 €/Jahr              │
│                                     │
│ [Prüfung läuft...]                 │
│                                     │
│ ✅ Bonität: Gut (Score: 85/100)    │
│ 💰 Kreditlimit: 100.000 €          │
│ ⚠️ Risiko: Niedrig                 │
│                                     │
│ [Ablehnen] [Manuell prüfen] [OK]   │
└─────────────────────────────────────┘
```

### Vertrags-Generator
```
┌─────────────────────────────────────┐
│ 📄 Vertrag erstellen                │
├─────────────────────────────────────┤
│ Vertragstyp:                        │
│ [✓] Partnerschaftsvereinbarung     │
│ [ ] Standardvertrag                 │
│                                     │
│ Laufzeit: [12 Monate ▼]            │
│ Start: [18.07.2025]                 │
│                                     │
│ Rabattstufe: Gold (8-10%)          │
│ ☑ AGBs anhängen                    │
│ ☑ Rabattplan anhängen              │
│                                     │
│ [Vorschau] [Erstellen & Senden]    │
└─────────────────────────────────────┘
```

---

## 📈 SUCCESS METRICS

- **Zahlungsausfälle:** -90% durch Bonitätsprüfung
- **Vertragserstellung:** 15 Min → 1 Min
- **Vertragsverlängerung:** 95% rechtzeitig (durch Erinnerungen)
- **Kundenzufriedenheit:** Professionelle Dokumente

---

## 🚀 IMPLEMENTATION ROADMAP

**Phase 1: Bonitätsprüfung (3 Tage)**
- Mock Service implementieren
- UI in Opportunity Pipeline
- Stage-Gate Logic

**Phase 2: PDF-Generator (4 Tage)**
- Template Engine (Apache FOP)
- Kundendaten-Mapping
- Preview & Download

**Phase 3: Vertragsmanagement (3 Tage)**
- Speicherung beim Kunden
- Ablauf-Erinnerungen
- Quick Actions

---

**Implementation Guide:** [FC-011_IMPLEMENTATION_GUIDE.md](./FC-011_IMPLEMENTATION_GUIDE.md)  
**Decision Log:** [FC-011_DECISION_LOG.md](./FC-011_DECISION_LOG.md)

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - User Authentication für Bonitätszugriff
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Parent-Feature (Integration)
- **[👥 FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)** - Wer darf Bonitätsprüfung durchführen

### ⚡ Direkt integriert in:
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)** - Bonitätsdaten für Angebote
- **[📄 PDF-001 PDF Generator](/docs/features/ACTIVE/pdf-generator/)** - Vertrags-PDFs generieren

### 🚀 Ermöglicht folgende Features:
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Bonität als Schutz-Kriterium
- **[👨‍💼 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Risiko-Analytics
- **[📈 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Bonitäts-KPIs

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Bonitäts-Status in Cards
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Bonitäts-Schwellwerte konfigurieren

### 🔧 Technische Details:
- **[IMPLEMENTATION_BACKEND.md](./IMPLEMENTATION_BACKEND.md)** - Backend-Implementation
- **[IMPLEMENTATION_FRONTEND.md](./IMPLEMENTATION_FRONTEND.md)** - Frontend-Integration