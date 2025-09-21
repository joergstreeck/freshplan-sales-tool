# 🔧 Backend Requirements - Sprint 2

**Sprint:** 2  
**Status:** 🆕 Zu implementieren  
**Modul:** FC-005 Customer Management  

---

## 📍 Navigation
**← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  
**→ API:** [API Endpoints](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/API_ENDPOINTS.md)  
**→ Entities:** [Entity Extensions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/ENTITY_EXTENSIONS.md)  

---

## 🎯 Übersicht

Backend-Anpassungen für die verkaufsfokussierte Customer-Struktur.

---

## 📊 Neue Datenfelder

### Customer Entity Erweiterungen:
```java
// Filialstruktur
private Integer totalLocationsEU;
private Integer locationsGermany;
private Integer locationsAustria;
private Integer locationsSwitzerland;
private Integer locationsRestEU;

// Geschäftsmodell
private String primaryFinancing; // 'private', 'public', 'mixed'

// Pain Points (als JSON)
private String painPoints; // JSON Array
```

### Location Entity (NEU):
```java
@Entity
public class Location {
    @Id
    private UUID id;
    
    @ManyToOne
    private Customer customer;
    
    private String locationName;
    private String street;
    private String houseNumber;
    private String postalCode;
    private String city;
    private Boolean isMainLocation;
    
    // Branchenspezifische Felder als JSON
    private String serviceOfferings;
    private String locationDetails;
}
```

---

## 🔄 Migration

1. **Customer Tabelle erweitern**
   - Neue Spalten via Flyway
   - Default-Werte setzen

2. **Location Tabelle erstellen**
   - Hauptstandort aus Customer migrieren
   - isMainLocation = true

3. **Contacts anpassen**
   - Relation zu Location statt Customer

---

## ✅ Akzeptanzkriterien

- [ ] Flyway-Migrations erstellt
- [ ] Entities erweitert
- [ ] DTOs angepasst
- [ ] Mapper aktualisiert
- [ ] Tests grün

---

**→ Weiter:** [API Endpoints](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/API_ENDPOINTS.md)