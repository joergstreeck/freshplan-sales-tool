# Backend Customer API - Filtering-Capabilities Analyse

**Datum:** 2025-07-05  
**Autor:** Claude  
**Zweck:** Analyse der aktuellen Backend-Filtering-Funktionalität für Customer-Management

## 🎯 Zusammenfassung

Das Backend verfügt über **solide Grundlagen** für Filter-Funktionalität, ist aber noch **nicht Enterprise-Level** für komplexe Kombinationen und gespeicherte Ansichten. Die Architektur ist gut strukturiert und erweiterbar.

## 📋 Aktuelle Filter-Parameter (CustomerResource)

### 1. **Basis-Filter** (`GET /api/customers`)
- `page` (int) - Seitenzahl, Standard: 0
- `size` (int) - Seitengröße, Standard: 20, max: 100
- `status` (CustomerStatus) - Einzelner Status-Filter
- `industry` (Industry) - Einzelner Branchen-Filter

### 2. **Such-Endpoints**
- `GET /api/customers/search?q=<term>` - Firmenname/Handelsname Suche
- `GET /api/customers/analytics/risk-assessment?minRiskScore=<score>` - Risiko-Filter

### 3. **Verfügbare Enum-Werte**

#### CustomerStatus:
- `LEAD` - Erstkontakt
- `PROSPECT` - Qualifizierter Lead
- `AKTIV` - Aktiver Kunde
- `RISIKO` - Gefährdeter Kunde
- `INAKTIV` - Inaktiver Kunde
- `ARCHIVIERT` - Archiviert

#### Industry:
- `HOTEL`, `RESTAURANT`, `CATERING`, `KANTINE`
- `GESUNDHEITSWESEN`, `BILDUNG`, `VERANSTALTUNG`
- `EINZELHANDEL`, `SONSTIGE`

## 🔍 Repository-Capabilities (CustomerRepository)

### **Bereits implementierte Filter-Methoden:**

1. **Status-basierte Filter:**
   - `findByStatus(CustomerStatus, Page)` - Einzelner Status
   - `findByStatusIn(List<CustomerStatus>, Page)` - Mehrere Status
   - `findByLifecycleStage(CustomerLifecycleStage, Page)`

2. **Branchen-Filter:**
   - `findByIndustry(Industry, Page)`

3. **Suchfunktionen:**
   - `searchByCompanyName(String, Page)` - LIKE-basiert
   - `findPotentialDuplicates(String)` - Ähnlichkeitssuche

4. **Risiko-Management:**
   - `findAtRisk(int minRiskScore, Page)`
   - `findOverdueFollowUps(Page)`
   - `findNotContactedSince(int days, Page)`

5. **Finanz-Filter:**
   - `findByExpectedVolumeRange(BigDecimal min, BigDecimal max, Page)`

6. **Hierarchie-Filter:**
   - `findChildren(UUID parentId)`
   - `findRootCustomers(Page)`

7. **Zeitbasierte Filter:**
   - `findRecentlyCreated(int days, Page)`
   - `findRecentlyUpdated(int days, Page)`

## 🏗️ Architektur-Bewertung

### **Stärken:**
✅ **Saubere Architektur**: Domain-driven Design mit Repository Pattern  
✅ **Pagination**: Überall konsistent implementiert  
✅ **Soft Delete**: Korrekt berücksichtigt in allen Queries  
✅ **Typsicherheit**: Enum-basierte Filter  
✅ **Performance**: Datenbankindizes vorhanden  
✅ **Erweiterbar**: Neue Filter-Methoden einfach hinzufügbar  

### **Schwächen für Enterprise-Level:**
❌ **Keine Kombinationsfilter**: Nur einzelne Parameter pro Request  
❌ **Keine dynamische Queries**: Feste HQL-Queries  
❌ **Keine Specification Pattern**: Für komplexe Kombinationen  
❌ **Keine Filter-Serialisierung**: Für gespeicherte Ansichten  
❌ **Keine Range-Filter**: Außer für Finanzvolumen  
❌ **Keine erweiterte Suche**: Nur einfache LIKE-Suche  

## 📊 Lücken für Enterprise-Level Filtering

### **1. Kombinationsfilter (Aktuell fehlend)**
```java
// Gewünscht: Kombinierte Filter
GET /api/customers?status=AKTIV,PROSPECT&industry=HOTEL&riskScore=0-50
```

### **2. Erweiterte Suchoperatoren**
```java
// Gewünscht: Flexible Operatoren
GET /api/customers?companyName[contains]=GmbH&expectedVolume[gte]=50000
```

### **3. Gespeicherte Filter-Ansichten**
```java
// Gewünscht: Filter-Definitionen speichern
POST /api/customers/filters
{
  "name": "Hochwertige Hotels",
  "filters": {
    "industry": ["HOTEL"],
    "status": ["AKTIV", "PROSPECT"],
    "expectedVolume": {"min": 100000}
  }
}
```

## 🛠️ Empfohlene Implementierungsstrategie

### **Phase 1: Query-Parameter-Erweiterung**
```java
@QueryParam("status") List<CustomerStatus> statuses,
@QueryParam("industry") List<Industry> industries,
@QueryParam("riskScoreMin") Integer riskScoreMin,
@QueryParam("riskScoreMax") Integer riskScoreMax,
@QueryParam("lastContactDays") Integer lastContactDays
```

### **Phase 2: Specification Pattern**
```java
public interface CustomerSpecification {
    Predicate toPredicate(Root<Customer> root, CriteriaBuilder cb);
}

// Implementierung für kombinierte Filter
public class CustomerFilterSpecification implements CustomerSpecification {
    private final CustomerFilter filter;
    
    @Override
    public Predicate toPredicate(Root<Customer> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        
        if (filter.getStatuses() != null) {
            predicates.add(root.get("status").in(filter.getStatuses()));
        }
        
        if (filter.getIndustries() != null) {
            predicates.add(root.get("industry").in(filter.getIndustries()));
        }
        
        if (filter.getRiskScoreRange() != null) {
            if (filter.getRiskScoreRange().getMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("riskScore"), filter.getRiskScoreRange().getMin()));
            }
            if (filter.getRiskScoreRange().getMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                    root.get("riskScore"), filter.getRiskScoreRange().getMax()));
            }
        }
        
        // Soft Delete immer berücksichtigen
        predicates.add(cb.isFalse(root.get("isDeleted")));
        
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
```

### **Phase 3: Filter-Persistence**
```java
@Entity
@Table(name = "saved_customer_filters")
public class SavedCustomerFilter {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(columnDefinition = "TEXT")
    private String filterJson;
    
    private boolean isDefault;
    private boolean isPublic;
}
```

## 🎯 Priorisierung für nächste Schritte

### **Hoch (Sprint 1-2):**
1. **Kombinierte Query-Parameter** - Mehrere Status/Industries gleichzeitig
2. **Range-Filter** - Risiko-Score, Volumen, Datum-Bereiche
3. **Erweiterte Suche** - Mehrere Suchbegriffe, Präzisere Matching

### **Mittel (Sprint 3-4):**
1. **Specification Pattern** - Für komplexe Kombinationen
2. **Filter-DTO** - Strukturierte Filter-Objekte
3. **Sortierung** - Erweiterte Sortier-Optionen

### **Niedrig (Sprint 5+):**
1. **Gespeicherte Ansichten** - User-spezifische Filter
2. **Filter-Vorlagen** - Vordefinierte Filter-Sets
3. **Bulk-Operationen** - Auf gefilterte Ergebnisse

## 🔧 Technische Implementierung

### **Aktuelle Datenbank-Struktur:**
- ✅ Alle notwendigen Felder für Filter vorhanden
- ✅ Indizes für Performance optimiert  
- ✅ Enum-Werte in DB gespeichert
- ✅ Soft Delete korrekt implementiert

### **Fehlende Komponenten:**
- ❌ FilterDTO-Klassen
- ❌ Specification-Interfaces
- ❌ Criteria API Integration
- ❌ Filter-Serialisierung

## 📝 Fazit

Das Backend ist **gut vorbereitet** für Enterprise-Level Filtering. Die Grundarchitektur ist solid und erweiterbar. Hauptaufgabe ist die Implementierung von:

1. **Kombinationsfiltern** (mehrere Parameter gleichzeitig)
2. **Specification Pattern** (für komplexe Logik)
3. **Gespeicherte Ansichten** (User-Experience)

Die bestehende Repository-Struktur kann **schrittweise erweitert** werden, ohne Breaking Changes. Die Criteria API Integration würde die Flexibilität erheblich steigern.

**Empfehlung:** Beginne mit einfachen Kombinationsfiltern über Query-Parameter, dann schrittweise zu Specification Pattern und gespeicherten Ansichten.