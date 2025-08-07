# 🔧 Backend-Änderungen für Sprint 2 V3

**Status:** 🆕 Erforderliche Anpassungen  
**Datum:** 31.07.2025  
**Impact:** CustomerLocation Entity & API

---

## 📍 Navigation
**← Sprint 2 V3:** [Final Structure](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/SPRINT2_V3_FINAL_STRUCTURE.md)  
**→ Migration:** [V7 Migration Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/MIGRATION_V7_GUIDE.md)

---

## 🎯 Übersicht der Änderungen

### 1. Customer Entity - EUR-Feld
```java
// Bereits vorhanden, aber Format-Anpassung nötig
@Column(name = "expected_annual_volume")
@Min(0)
@Max(10000000) // 10 Mio Max
private BigDecimal expectedAnnualVolume;

// NEU: Validation für Warnung bei > 1 Mio
@PrePersist
@PreUpdate
public void validateAnnualVolume() {
    if (expectedAnnualVolume != null && 
        expectedAnnualVolume.compareTo(new BigDecimal("1000000")) > 0) {
        // Log für Review
        log.warn("High annual volume: {} for customer {}", 
                 expectedAnnualVolume, companyName);
    }
}
```

### 2. CustomerLocation Entity - Service-Felder
```java
@Entity
@Table(name = "customer_locations")
public class CustomerLocation extends BaseEntity {
    
    // Bestehende Felder bleiben...
    
    // === NEU: Hotel Services ===
    @Column(name = "offers_breakfast")
    private Boolean offersBreakfast;
    
    @Column(name = "breakfast_warm")
    private Boolean breakfastWarm;
    
    @Column(name = "breakfast_guests_per_day")
    @Min(0)
    @Max(9999)
    private Integer breakfastGuestsPerDay;
    
    @Column(name = "offers_lunch")
    private Boolean offersLunch;
    
    @Column(name = "offers_dinner")
    private Boolean offersDinner;
    
    @Column(name = "offers_room_service")
    private Boolean offersRoomService;
    
    @Column(name = "offers_events")
    private Boolean offersEvents;
    
    @Column(name = "event_capacity")
    @Min(0)
    @Max(9999)
    private Integer eventCapacity;
    
    @Column(name = "room_count")
    @Min(1)
    @Max(9999)
    private Integer roomCount;
    
    @Column(name = "average_occupancy")
    @Min(0)
    @Max(100)
    private Integer averageOccupancy;
    
    // === NEU: Krankenhaus Services ===
    @Column(name = "meal_system")
    @Enumerated(EnumType.STRING)
    private MealSystem mealSystem;
    
    @Column(name = "beds_count")
    private Integer bedsCount;
    
    @Column(name = "meals_per_day")
    private Integer mealsPerDay;
    
    @Column(name = "offers_vegetarian")
    private Boolean offersVegetarian;
    
    @Column(name = "offers_vegan")
    private Boolean offersVegan;
    
    @Column(name = "offers_halal")
    private Boolean offersHalal;
    
    @Column(name = "offers_kosher")
    private Boolean offersKosher;
    
    // === NEU: Betriebsrestaurant Services ===
    @Column(name = "operating_days")
    @Min(1)
    @Max(7)
    private Integer operatingDays;
    
    @Column(name = "lunch_guests")
    private Integer lunchGuests;
    
    @Column(name = "subsidized")
    private Boolean subsidized;
}

// Enum für Meal Systems
public enum MealSystem {
    COOK_AND_SERVE,
    COOK_AND_CHILL,
    FROZEN
}
```

### 3. Contact Entity - Zuständigkeitsbereich
```java
@Entity
@Table(name = "contacts")
public class Contact extends BaseEntity {
    
    // Bestehende Felder...
    
    // NEU: Strukturierte Namen
    @Column(name = "salutation")
    @NotNull
    @Enumerated(EnumType.STRING)
    private Salutation salutation;
    
    @Column(name = "title")
    private String title; // Dr., Prof., etc.
    
    @Column(name = "first_name")
    @NotBlank
    private String firstName;
    
    @Column(name = "last_name")
    @NotBlank
    private String lastName;
    
    // NEU: Zuständigkeit
    @Column(name = "responsibility_scope")
    @Enumerated(EnumType.STRING)
    private ResponsibilityScope responsibilityScope = ResponsibilityScope.ALL;
    
    @ManyToMany
    @JoinTable(
        name = "contact_location_assignments",
        joinColumns = @JoinColumn(name = "contact_id"),
        inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Set<CustomerLocation> assignedLocations = new HashSet<>();
}

public enum Salutation {
    HERR, FRAU, DIVERS
}

public enum ResponsibilityScope {
    ALL, SPECIFIC
}
```

---

## 📊 DTOs anpassen

### CustomerLocationDTO erweitern:
```java
public class CustomerLocationDTO {
    // Bestehende Felder...
    
    // Service-Felder (gruppiert)
    private LocationServicesDTO services;
}

public class LocationServicesDTO {
    // Hotel
    private Boolean offersBreakfast;
    private Boolean breakfastWarm;
    private Integer breakfastGuestsPerDay;
    private Boolean offersLunch;
    private Boolean offersDinner;
    private Boolean offersRoomService;
    private Boolean offersEvents;
    private Integer eventCapacity;
    private Integer roomCount;
    private Integer averageOccupancy;
    
    // Krankenhaus
    private String mealSystem;
    private Integer bedsCount;
    private Integer mealsPerDay;
    private Boolean offersVegetarian;
    private Boolean offersVegan;
    private Boolean offersHalal;
    private Boolean offersKosher;
    
    // Betriebsrestaurant
    private Integer operatingDays;
    private Integer lunchGuests;
    private Boolean subsidized;
}
```

### ContactDTO V2:
```java
public class ContactDTOV2 {
    private UUID id;
    private String salutation;
    private String title;
    private String firstName;
    private String lastName;
    private String position;
    private String email;
    private String phone;
    private String mobile;
    private boolean isPrimary;
    private List<String> roles;
    
    // NEU
    private String responsibilityScope;
    private List<UUID> assignedLocationIds;
}
```

---

## 🔄 Migration V7

```sql
-- V7__add_location_services_and_contact_updates.sql

-- 1. Location Services für Hotels
ALTER TABLE customer_locations
ADD COLUMN offers_breakfast BOOLEAN DEFAULT FALSE,
ADD COLUMN breakfast_warm BOOLEAN DEFAULT FALSE,
ADD COLUMN breakfast_guests_per_day INTEGER,
ADD COLUMN offers_lunch BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_dinner BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_room_service BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_events BOOLEAN DEFAULT FALSE,
ADD COLUMN event_capacity INTEGER,
ADD COLUMN room_count INTEGER,
ADD COLUMN average_occupancy INTEGER CHECK (average_occupancy >= 0 AND average_occupancy <= 100);

-- 2. Location Services für Krankenhäuser
ALTER TABLE customer_locations
ADD COLUMN meal_system VARCHAR(50),
ADD COLUMN beds_count INTEGER,
ADD COLUMN meals_per_day INTEGER,
ADD COLUMN offers_vegetarian BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_vegan BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_halal BOOLEAN DEFAULT FALSE,
ADD COLUMN offers_kosher BOOLEAN DEFAULT FALSE;

-- 3. Location Services für Betriebsrestaurants
ALTER TABLE customer_locations
ADD COLUMN operating_days INTEGER CHECK (operating_days >= 1 AND operating_days <= 7),
ADD COLUMN lunch_guests INTEGER,
ADD COLUMN subsidized BOOLEAN DEFAULT FALSE;

-- 4. Contact Updates
ALTER TABLE contacts
ADD COLUMN salutation VARCHAR(20) NOT NULL DEFAULT 'HERR',
ADD COLUMN title VARCHAR(50),
ADD COLUMN first_name VARCHAR(100),
ADD COLUMN last_name VARCHAR(100),
ADD COLUMN responsibility_scope VARCHAR(20) DEFAULT 'ALL';

-- Migrate existing name data
UPDATE contacts 
SET first_name = SUBSTRING(name FROM 1 FOR POSITION(' ' IN name) - 1),
    last_name = SUBSTRING(name FROM POSITION(' ' IN name) + 1)
WHERE name IS NOT NULL AND POSITION(' ' IN name) > 0;

-- 5. Contact-Location Assignment Table
CREATE TABLE contact_location_assignments (
    contact_id UUID NOT NULL,
    location_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (contact_id, location_id),
    FOREIGN KEY (contact_id) REFERENCES contacts(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES customer_locations(id) ON DELETE CASCADE
);

-- 6. Indexes für Performance
CREATE INDEX idx_location_services_hotel ON customer_locations(customer_id, offers_breakfast, offers_lunch, offers_dinner);
CREATE INDEX idx_location_services_hospital ON customer_locations(customer_id, meal_system, beds_count);
CREATE INDEX idx_location_services_canteen ON customer_locations(customer_id, operating_days, subsidized);
CREATE INDEX idx_contact_assignments ON contact_location_assignments(contact_id, location_id);

-- 7. Comments für Dokumentation
COMMENT ON COLUMN customer_locations.offers_breakfast IS 'Hotel: Bietet Frühstück an';
COMMENT ON COLUMN customer_locations.meal_system IS 'Krankenhaus: Cook&Serve, Cook&Chill oder Tiefkühl';
COMMENT ON COLUMN customer_locations.operating_days IS 'Betriebsrestaurant: Betriebstage pro Woche (1-7)';
```

---

## 🚀 API Endpoints

### Neue Endpoints für Step 4:
```java
@Path("/api/customers/{customerId}/locations/{locationId}/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocationServicesResource {
    
    @PUT
    @RolesAllowed({"admin", "sales"})
    public Response updateLocationServices(
            @PathParam("customerId") UUID customerId,
            @PathParam("locationId") UUID locationId,
            LocationServicesDTO services) {
        // Update services for specific location
        return Response.ok().build();
    }
    
    @POST
    @Path("/copy")
    @RolesAllowed({"admin", "sales"})
    public Response copyLocationServices(
            @PathParam("customerId") UUID customerId,
            @PathParam("locationId") UUID targetLocationId,
            @QueryParam("sourceLocationId") UUID sourceLocationId) {
        // Copy services from source to target
        return Response.ok().build();
    }
    
    @POST
    @Path("/bulk-update")
    @RolesAllowed({"admin", "sales"})
    public Response bulkUpdateServices(
            @PathParam("customerId") UUID customerId,
            BulkUpdateRequest request) {
        // Update multiple locations at once
        return Response.ok().build();
    }
}
```

---

## ✅ Deployment Checklist

1. **Database Migration:**
   - [ ] Review V7 Migration SQL
   - [ ] Test auf Dev-DB
   - [ ] Backup Prod-DB
   - [ ] Execute Migration
   - [ ] Verify Schema

2. **Backend Deployment:**
   - [ ] Update Entities
   - [ ] Update DTOs
   - [ ] Update Mappers
   - [ ] Add new Endpoints
   - [ ] Update Tests

3. **API Documentation:**
   - [ ] Update OpenAPI Spec
   - [ ] Update Postman Collection
   - [ ] Document Breaking Changes

---

## 🔗 Weiterführende Links

**Frontend Integration:**
- [API Client Updates](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/API_CLIENT_V3.md)
- [Type Definitions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/types/BACKEND_TYPES_V3.md)