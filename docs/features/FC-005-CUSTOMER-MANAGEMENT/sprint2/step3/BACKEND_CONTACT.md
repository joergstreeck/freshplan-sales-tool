# 🔧 Tag 1: Backend Contact Entity

**Datum:** Tag 1 der Step 3 Implementation  
**Fokus:** JPA Entity & Repository Setup  
**Ziel:** Contact Entity mit Audit Trail  

## 🧭 Navigation

**← Übersicht:** [Step 3 Guide](./README.md)  
**→ Nächster Tag:** [Frontend Foundation](./FRONTEND_FOUNDATION.md)  
**↑ Sprint:** [Sprint 2 Master Plan](../SPRINT2_MASTER_PLAN.md)  

## 🎯 Tagesziel

Implementierung der Contact Entity mit:
- JPA Annotations
- Customer Relationship
- Audit Fields
- Repository mit Custom Queries

## 💻 Implementation

### 1. Contact Entity

```java
package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Audited // Aktiviert Hibernate Envers
public class Contact extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    // Basic Info
    @Column(name = "salutation", length = 20)
    private String salutation;
    
    @Column(name = "title", length = 50)
    private String title;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "position", length = 100)
    private String position;
    
    @Column(name = "decision_level", length = 50)
    private String decisionLevel;
    
    // Contact Info
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Column(name = "mobile", length = 50)
    private String mobile;
    
    // Flags
    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    // Location Assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_location_id")
    private CustomerLocation assignedLocation;
    
    // Relationship Data (Beziehungsebene)
    @Column(name = "birthday")
    private LocalDate birthday;
    
    @Column(name = "hobbies", length = 500)
    private String hobbies;
    
    @Column(name = "family_status", length = 50)
    private String familyStatus;
    
    @Column(name = "children_count")
    private Integer childrenCount;
    
    @Column(name = "personal_notes", columnDefinition = "TEXT")
    private String personalNotes;
    
    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // Getters, Setters, Builder...
}
```

### 2. Contact Repository

```java
package de.freshplan.domain.customer.repository;

import de.freshplan.domain.customer.entity.Contact;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<Contact, UUID> {
    
    /**
     * Find all contacts for a customer
     */
    public List<Contact> findByCustomerId(UUID customerId) {
        return find("customer.id = ?1 and isActive = true", customerId)
            .list();
    }
    
    /**
     * Find primary contact for a customer
     */
    public Optional<Contact> findPrimaryByCustomerId(UUID customerId) {
        return find("customer.id = ?1 and isPrimary = true and isActive = true", 
            customerId)
            .firstResultOptional();
    }
    
    /**
     * Find contacts assigned to a location
     */
    public List<Contact> findByLocationId(UUID locationId) {
        return find("assignedLocation.id = ?1 and isActive = true", locationId)
            .list();
    }
    
    /**
     * Set new primary contact (unset others)
     */
    @Transactional
    public void setPrimaryContact(UUID customerId, UUID contactId) {
        // Unset all primary flags
        update("isPrimary = false where customer.id = ?1", customerId);
        
        // Set new primary
        update("isPrimary = true where id = ?1", contactId);
    }
}
```

### 3. Migration Script

```sql
-- V7__create_contacts_table.sql

CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    
    -- Basic Info
    salutation VARCHAR(20),
    title VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    position VARCHAR(100),
    decision_level VARCHAR(50),
    
    -- Contact Info
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    
    -- Flags
    is_primary BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Location
    assigned_location_id UUID REFERENCES customer_locations(id),
    
    -- Relationship Data
    birthday DATE,
    hobbies VARCHAR(500),
    family_status VARCHAR(50),
    children_count INTEGER,
    personal_notes TEXT,
    
    -- Audit
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT unique_primary_per_customer UNIQUE (customer_id, is_primary) 
        WHERE is_primary = true
);

-- Indices
CREATE INDEX idx_contact_customer ON contacts(customer_id);
CREATE INDEX idx_contact_location ON contacts(assigned_location_id);
CREATE INDEX idx_contact_active ON contacts(is_active);

-- Audit Table (Hibernate Envers)
CREATE TABLE contacts_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    -- Alle Felder von contacts...
    PRIMARY KEY (id, rev)
);
```

### 4. Contact Service

```java
package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.repository.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class ContactService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    SecurityContext securityContext;
    
    public Contact createContact(UUID customerId, Contact contact) {
        // Set audit fields
        contact.setCreatedBy(securityContext.getUserPrincipal().getName());
        contact.setUpdatedBy(securityContext.getUserPrincipal().getName());
        
        // If first contact, make it primary
        boolean hasContacts = contactRepository
            .count("customer.id = ?1", customerId) > 0;
        if (!hasContacts) {
            contact.setPrimary(true);
        }
        
        contactRepository.persist(contact);
        return contact;
    }
    
    public void updatePrimaryContact(UUID customerId, UUID contactId) {
        contactRepository.setPrimaryContact(customerId, contactId);
    }
}
```

## 🧪 Tests

```java
@QuarkusTest
class ContactRepositoryTest {
    
    @Test
    void shouldFindPrimaryContact() {
        // Given
        Customer customer = createTestCustomer();
        Contact primary = createContact(customer, true);
        Contact secondary = createContact(customer, false);
        
        // When
        Optional<Contact> found = contactRepository
            .findPrimaryByCustomerId(customer.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(primary.getId());
    }
}
```

## 📝 Checkliste

- [ ] Contact Entity erstellt
- [ ] Repository implementiert
- [ ] Migration V7 ausgeführt
- [ ] Service Layer erstellt
- [ ] Unit Tests geschrieben
- [ ] Integration Tests grün

## 🔗 Nächste Schritte

**Morgen:** [Frontend Foundation](./FRONTEND_FOUNDATION.md) - Types & Store Setup

---

**Status:** 📋 Bereit zur Implementierung