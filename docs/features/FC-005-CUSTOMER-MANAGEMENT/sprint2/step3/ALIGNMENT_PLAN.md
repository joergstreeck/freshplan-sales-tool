# 🔧 Code Alignment Plan - Phase 1

**Ziel:** Code auf Planungsstand bringen bevor wir neue Features implementieren  
**Strategie:** Nur Verbesserungen die uns später helfen  
**Branch:** feature/fc-005-step3-phase2-intelligence  

## 📋 Priorisierte Alignment-Tasks

### 1. ⭐ Contact Roles (KRITISCH)
**Warum:** Vertrieb braucht Multi-Role Support
- Ein Kontakt kann mehrere Rollen haben (Decision Maker + Technical Contact)
- Wichtig für Vertriebsprozess

**Implementation:**
```java
// 1. ContactRole Enum erstellen
public enum ContactRole {
    DECISION_MAKER,
    TECHNICAL_CONTACT,
    BILLING_CONTACT,
    OPERATIONS_CONTACT,
    INFLUENCER,
    CHAMPION,
    GATEKEEPER
}

// 2. In Contact Entity hinzufügen
@ElementCollection(fetch = FetchType.EAGER)
@CollectionTable(
    name = "contact_roles",
    joinColumns = @JoinColumn(name = "contact_id")
)
@Column(name = "role")
@Enumerated(EnumType.STRING)
private Set<ContactRole> roles = new HashSet<>();
```

### 2. ⭐ Responsibility Scope (WICHTIG)
**Warum:** Filialstruktur-Management
- Kontakt für alle Standorte oder nur bestimmte?
- Essentiell für große Kunden

**Implementation:**
```java
// In Contact Entity
@Column(name = "responsibility_scope", length = 20)
private String responsibilityScope = "all"; // 'all' or 'specific'

// Wenn specific, dann Multi-Location Assignment aktivieren
```

### 3. ⭐ Multi-Location Assignment (WICHTIG)
**Warum:** Große Kunden mit Filialen
- Ein Ansprechpartner für mehrere Standorte
- Flexiblere Zuordnung

**Implementation:**
```java
// Erweitern von Single zu Multi
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "contact_location_assignments",
    joinColumns = @JoinColumn(name = "contact_id"),
    inverseJoinColumns = @JoinColumn(name = "location_id")
)
private Set<CustomerLocation> assignedLocations = new HashSet<>();

// Backward compatibility: assignedLocation als deprecated markieren
@Deprecated
@Transient
public CustomerLocation getAssignedLocation() {
    return assignedLocations.isEmpty() ? null : assignedLocations.iterator().next();
}
```

### 4. 🔄 Soft-Delete Fields (NICE-TO-HAVE)
**Warum:** Besserer Audit Trail
- Wer hat gelöscht und warum?
- DSGVO-Compliance

**Implementation:**
```java
// Erweiterte Soft-Delete Fields
@Column(name = "deleted_at")
private LocalDateTime deletedAt;

@Column(name = "deleted_by", length = 100)
private String deletedBy;

@Column(name = "deletion_reason", length = 500)
private String deletionReason;

// isDeleted als computed field
@Transient
public boolean isDeleted() {
    return deletedAt != null;
}
```

## 📊 Migrations benötigt

### V209__add_contact_roles.sql
```sql
-- Contact Roles Table
CREATE TABLE contact_roles (
    contact_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (contact_id, role),
    CONSTRAINT fk_contact_roles_contact 
        FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_contact_roles_contact ON contact_roles(contact_id);
CREATE INDEX idx_contact_roles_role ON contact_roles(role);

-- Add responsibility_scope to contacts
ALTER TABLE customer_contacts 
ADD COLUMN responsibility_scope VARCHAR(20) DEFAULT 'all';

COMMENT ON COLUMN customer_contacts.responsibility_scope IS 
'Scope of responsibility: all = all locations, specific = selected locations only';
```

### V210__add_contact_location_assignments.sql
```sql
-- Multi-Location Assignment Table
CREATE TABLE contact_location_assignments (
    contact_id UUID NOT NULL,
    location_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    PRIMARY KEY (contact_id, location_id),
    CONSTRAINT fk_contact_location_contact 
        FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_contact_location_location 
        FOREIGN KEY (location_id) 
        REFERENCES customer_locations(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_contact_location_contact ON contact_location_assignments(contact_id);
CREATE INDEX idx_contact_location_location ON contact_location_assignments(location_id);

-- Migrate existing single assignments
INSERT INTO contact_location_assignments (contact_id, location_id)
SELECT id, assigned_location_id 
FROM customer_contacts 
WHERE assigned_location_id IS NOT NULL;

-- Keep old column for backward compatibility (mark as deprecated)
COMMENT ON COLUMN customer_contacts.assigned_location_id IS 
'DEPRECATED: Use contact_location_assignments table. Kept for backward compatibility.';
```

### V211__add_soft_delete_fields.sql
```sql
-- Enhanced Soft-Delete Fields
ALTER TABLE customer_contacts 
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted_by VARCHAR(100),
ADD COLUMN deletion_reason VARCHAR(500);

-- Index for soft-delete queries
CREATE INDEX idx_contact_deleted ON customer_contacts(deleted_at) 
WHERE deleted_at IS NOT NULL;

-- Update existing is_deleted to new structure
UPDATE customer_contacts 
SET deleted_at = CURRENT_TIMESTAMP,
    deleted_by = 'MIGRATION',
    deletion_reason = 'Migrated from is_deleted flag'
WHERE is_deleted = true;

-- Keep is_deleted for backward compatibility
COMMENT ON COLUMN customer_contacts.is_deleted IS 
'DEPRECATED: Use deleted_at instead. Kept for backward compatibility.';
```

## 🧪 Test-Anforderungen

### Unit Tests
- ContactRole Enum Tests
- Multi-Location Assignment Tests
- Responsibility Scope Logic Tests
- Soft-Delete Behavior Tests

### Integration Tests
- Contact mit mehreren Roles erstellen
- Location Assignment testen
- Soft-Delete mit Audit Trail

### Migration Tests
- Backward Compatibility sicherstellen
- Daten-Migration verifizieren

## 📈 Implementierungs-Reihenfolge

1. **Tag 1: Backend Entities + Migrations**
   - ContactRole Enum
   - Entity-Erweiterungen
   - Migrations V209-V211

2. **Tag 2: Services + DTOs**
   - ContactService erweitern
   - DTOs anpassen
   - Mapper updaten

3. **Tag 3: Frontend Integration**
   - Types updaten
   - API Services erweitern
   - UI Components anpassen

4. **Tag 4: Tests + Documentation**
   - Alle Tests grün
   - Documentation updaten
   - PR erstellen

## ✅ Definition of Done

- [ ] Alle Entity-Erweiterungen implementiert
- [ ] Migrations erfolgreich
- [ ] Backward Compatibility gewährleistet
- [ ] Tests grün (Unit + Integration)
- [ ] Frontend funktioniert mit neuen Features
- [ ] Documentation aktualisiert
- [ ] PR reviewed und gemerged

## 🎯 Erwartete Benefits

1. **Flexiblere Kontaktverwaltung** durch Multi-Roles
2. **Bessere Filialstruktur** durch Responsibility Scope
3. **Skalierbar für große Kunden** durch Multi-Location
4. **DSGVO-compliant** durch erweiterte Soft-Delete
5. **Zukunftssicher** für weitere Features

## ⚠️ Risiken

- Backward Compatibility brechen
- Migration-Fehler bei Bestandsdaten
- Performance bei Multi-Location Queries

## 🚀 Nach dem Merge

Sobald diese Alignments gemerged sind:
1. Warmth Indicator UI implementieren
2. Contact Timeline Component
3. Smart Suggestions
4. Location Intelligence