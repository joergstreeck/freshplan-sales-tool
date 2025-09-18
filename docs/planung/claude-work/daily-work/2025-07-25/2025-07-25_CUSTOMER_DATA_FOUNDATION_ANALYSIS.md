# 🏗️ CUSTOMER DATA FOUNDATION ANALYSIS

**Datum:** 2025-07-25 20:50  
**Scope:** Customer-Contract-Opportunity Datenmodell  
**Status:** 🔴 KRITISCHE LÜCKEN IDENTIFIZIERT  

---

## 🚨 KERN-PROBLEM GEFUNDEN

### **Das ist der Grund für die Verwirrung:**

```
Aktueller Zustand:
Customer Entity ❌ KEINE Contract-Beziehung
Opportunity Entity ❌ KEINE Customer-Beziehung  
RENEWAL Stage ❌ OHNE Contract-Kontext
```

**Das gesamte Datenmodell ist fragmentiert!**

---

## 📊 AKTUELLE CUSTOMER ENTITY ANALYSE

### ✅ **Was existiert bereits:**
```java
@Entity
public class Customer {
    // Basic Information ✅
    private String customerNumber;
    private String companyName;
    
    // Hierarchy Support ✅ (für Kettenkunden!)
    @ManyToOne private Customer parentCustomer;
    @OneToMany private List<Customer> childCustomers;
    private CustomerHierarchyType hierarchyType;
    
    // Financial Information ✅
    private BigDecimal expectedAnnualVolume;
    private BigDecimal actualAnnualVolume;
    
    // Partner Status ✅ (aber ungenutzt!)
    private PartnerStatus partnerStatus = PartnerStatus.KEIN_PARTNER;
}
```

### ❌ **Was komplett fehlt:**
```java
// FEHLENDE Contract-Beziehungen:
@OneToMany private List<FreshPlanContract> freshPlanContracts; // ❌
private LocalDate currentContractStart; // ❌
private LocalDate currentContractEnd; // ❌
private Boolean hasActiveFreshPlanContract; // ❌
private ContractRenewalStatus renewalStatus; // ❌

// FEHLENDE Opportunity-Beziehungen:
@OneToMany private List<Opportunity> opportunities; // ❌

// FEHLENDE FreshPlan-spezifische Felder:
private FreshPlanRabattStatus rabattStatus; // ❌
private BigDecimal currentPriceIndex; // ❌
private LocalDate lastPriceAdjustment; // ❌
```

---

## 🔗 FEHLENDE ENTITY-BEZIEHUNGEN

### **Aktuell:**
```
Customer (isoliert)
Opportunity (isoliert)
RENEWAL Stage (ohne Kontext)
```

### **Benötigt:**
```
Customer 
│
├─── FreshPlanContract (1:n) ⭐ KERN-MISSING
│    ├── contractStart/End
│    ├── priceIndex
│    ├── renewalStatus
│    └── automaticRenewal
│
├─── Opportunities (1:n) ⭐ MISSING
│    ├── RENEWAL Opportunities
│    └── relatedContract
│
└─── CustomerLocations (1:n) ✅ EXISTS
     └── für Kettenkunden-Lieferung
```

---

## 💼 BUSINESS LOGIC GAPS

### **1. Kettenkunden-Problem:**
```java
// CURRENT: Hierarchie existiert, aber Contract-Logic fehlt
Customer hotelgruppe = parentCustomer;
List<Customer> standorte = hotelgruppe.getChildCustomers();

// MISSING: Wie funktioniert FreshPlan-Vertrag?
// - Ein Vertrag für alle Standorte? ✅ (laut Business Rules)
// - Oder pro Standort? ❌
// - Rabatt-Berechtigung wie verteilt?
```

### **2. Rabatt-Validation Problem:**
```java
// CURRENT: Keine Contract-Prüfung möglich
public BigDecimal calculateRabatt(Customer customer, Order order) {
    // ❌ Kann nicht prüfen ob FreshPlan-Vertrag aktiv
    // ❌ Kann nicht prüfen ob Vertrag abgelaufen
    // ❌ Kann nicht prüfen ob Preisanpassung fällig
}
```

### **3. RENEWAL Stage Problem:**
```java
// CURRENT: RENEWAL ohne Customer-Context
Opportunity renewal = new Opportunity();
renewal.setStage(OpportunityStage.RENEWAL);
// ❌ Für welchen Customer?
// ❌ Welcher Vertrag läuft ab?
// ❌ Welche Konditionen gelten?
```

---

## 🎯 ERFORDERLICHE DATENMODELL-ERWEITERUNGEN

### **1. FreshPlanContract Entity (NEU):**
```java
@Entity
@Table(name = "freshplan_contracts")
public class FreshPlanContract {
    @Id private UUID id;
    
    // Customer-Beziehung
    @ManyToOne(nullable = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    // Contract Lifecycle
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private ContractStatus status; // ACTIVE, RENEWAL_PENDING, EXPIRED
    private Boolean automaticRenewal = true;
    
    // Business Terms
    private BigDecimal targetVolume; // Zielvolumen (unverbindlich)
    private BigDecimal currentPriceIndex;
    private LocalDate lastPriceAdjustment;
    
    // Renewal Management
    private LocalDate lastRenewalCheck;
    private RenewalUrgency renewalUrgency; // GREEN, YELLOW, RED
    
    // Business Methods
    public boolean isNearingExpiry(int daysThreshold) { }
    public boolean isPriceAdjustmentRequired(BigDecimal newIndex) { }
    public boolean isRabattValid(LocalDate orderDate) { }
}
```

### **2. Customer Entity Erweiterung:**
```java
@Entity
public class Customer {
    // EXISTING fields...
    
    // ⭐ NEU: Contract Management
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<FreshPlanContract> freshPlanContracts = new ArrayList<>();
    
    @Column(name = "has_active_freshplan_contract")
    private Boolean hasActiveFreshPlanContract = false;
    
    @Column(name = "freshplan_status")
    @Enumerated(EnumType.STRING)
    private FreshPlanStatus freshPlanStatus = FreshPlanStatus.KEIN_VERTRAG;
    
    // ⭐ NEU: Opportunity Relationship
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Opportunity> opportunities = new ArrayList<>();
    
    // Business Methods
    public FreshPlanContract getActiveFreshPlanContract() {
        return freshPlanContracts.stream()
            .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
            .findFirst()
            .orElse(null);
    }
    
    public boolean isRabattBerechtigt(LocalDate orderDate) {
        FreshPlanContract contract = getActiveFreshPlanContract();
        return contract != null && contract.isRabattValid(orderDate);
    }
    
    // Kettenkunden-Logic
    public FreshPlanContract getEffectiveFreshPlanContract() {
        // Zuerst eigener Vertrag prüfen
        FreshPlanContract own = getActiveFreshPlanContract();
        if (own != null) return own;
        
        // Falls Child: Parent-Vertrag prüfen
        if (parentCustomer != null) {
            return parentCustomer.getActiveFreshPlanContract();
        }
        
        return null;
    }
}
```

### **3. Opportunity Entity Erweiterung:**
```java
@Entity
public class Opportunity {
    // EXISTING fields...
    
    // ⭐ NEU: Customer-Beziehung
    @ManyToOne(nullable = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    // ⭐ NEU: Contract-Beziehung (für RENEWAL)
    @ManyToOne
    @JoinColumn(name = "related_contract_id")
    private FreshPlanContract relatedContract;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "opportunity_type")
    private OpportunityType opportunityType = OpportunityType.NEW_BUSINESS;
    
    // Business Methods für RENEWAL
    public boolean isContractRenewal() {
        return opportunityType == OpportunityType.CONTRACT_RENEWAL 
            && stage == OpportunityStage.RENEWAL;
    }
    
    public LocalDate getContractExpiryDate() {
        return relatedContract != null ? relatedContract.getContractEnd() : null;
    }
}
```

---

## 🏗️ MIGRATION STRATEGY

### **Phase 1: Basis-Entities erstellen (1 Tag)**
1. FreshPlanContract Entity erstellen
2. Database Migration für neue Tabelle
3. Basic Contract Repository + Service

### **Phase 2: Customer-Integration (1 Tag)**  
4. Customer Entity um Contract-Felder erweitern
5. Customer-Contract Relationships implementieren
6. Kettenkunden Contract-Logic implementieren

### **Phase 3: Opportunity-Integration (1 Tag)**
7. Opportunity-Customer Beziehung hinzufügen
8. Opportunity-Contract Beziehung für RENEWAL
9. RENEWAL Stage mit echter Contract-Logic verbinden

### **Phase 4: Business Logic (1 Tag)**
10. Rabatt-Validation basierend auf Contract
11. Automatische Contract Monitoring Jobs
12. RENEWAL Opportunity Auto-Creation

---

## 🎯 IMMEDIATE IMPACT

### **Nach der Foundation:**
✅ RENEWAL Stage hat echten Business-Kontext  
✅ Rabatte nur mit gültigem FreshPlan-Vertrag  
✅ Kettenkunden-Logic funktioniert korrekt  
✅ Opportunity-Customer-Contract Verzahnung  
✅ Basis für FC-009 Contract Renewal Management  

### **Business Value:**
- **Revenue Protection:** Kein Rabatt ohne Vertrag
- **Compliance:** FreshPlan-AGB werden eingehalten  
- **Automation:** Contract Monitoring funktioniert
- **Transparency:** Komplette Customer Journey sichtbar

---

## ✅ CONCLUSION

**Das Customer-Datenmodell ist die FOUNDATION für alles:**
- Ohne Customer-Contract-Beziehung → Kein sinnvolles RENEWAL
- Ohne Opportunity-Customer-Beziehung → Kein CRM-Workflow  
- Ohne Kettenkunden-Contract-Logic → Falsche Rabatt-Verteilung

**Empfehlung:** Customer Foundation zuerst, dann FC-009 Contract Renewal.

---

**Analysiert:** 2025-07-25 20:50  
**Status:** 🔴 FOUNDATION REQUIRED  
**Next:** Customer-Contract-Opportunity Datenmodell implementieren