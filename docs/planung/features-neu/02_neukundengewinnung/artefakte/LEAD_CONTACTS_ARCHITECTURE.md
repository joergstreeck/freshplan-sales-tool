---
doc_type: "architecture"
title: "Lead Contacts Architecture - Multi-Contact Support"
sprint: "2.1.6 Phase 5+"
status: "in_progress"
owner: "team/leads-backend"
created: "2025-10-08"
decision_id: "ADR-007"
---

# Lead Contacts Architecture - Multi-Contact Support

**📍 Navigation:** Home → Planung → Neukundengewinnung → Artefakte → Lead Contacts Architecture

## 🎯 Ziel & Kontext

**Problem:**
- Lead-Modul hat **flaches Feld** `contact_person VARCHAR(255)` ("Max Mustermann")
- Customer-Modul hat **separate Entity** `Contact` mit `first_name` + `last_name`
- Frontend (LeadWizard) sendet strukturierte Daten `{ firstName, lastName }` → Backend ignoriert sie
- **Nur 1 Kontakt** pro Lead möglich (User-Feedback: "weitere Kontakte können nicht nacherfasst werden")

**Entscheidung: OPTION C - 100% Parity mit customer_contacts**
Harmonisierung mit Customer-Modul durch **separate `lead_contacts` Tabelle** - **ALLE Felder übernommen**.

**Begründung für Option C (Full Parity):**
- ✅ **Produkt noch nicht live** - Keine Legacy-Daten, Clean Slate
- ✅ **Frontend erwartet strukturierte Daten** - { firstName, lastName } bereits implementiert
- ✅ **100% Konsistenz** - Lead + Customer haben identische Contact-Architektur
- ✅ **CRM Intelligence Ready** - warmth_score, data_quality_score, relationship data sofort verfügbar
- ✅ **Future-Proof** - Alle Customer-Features funktionieren sofort für Leads
- ✅ **Keine zweite Migration nötig** - Vermeidet spätere ADD COLUMN Migrations für Intelligence Features
- ✅ **Same Code, Same Tools** - ContactService/Mapper können für beide Entities genutzt werden

**Was wird übernommen:**
- ✅ **Basic Info:** salutation, title, first_name, last_name, position, decision_level
- ✅ **Contact Info:** email, phone, mobile
- ✅ **Relationship Data:** birthday, hobbies, family_status, children_count, personal_notes
- ✅ **Intelligence Data:** warmth_score, warmth_confidence, last_interaction_date, interaction_count
- ✅ **Data Quality:** data_quality_score, data_quality_recommendations
- ✅ **Flags:** is_primary, is_active, is_decision_maker, is_deleted

---

## 🏗️ Architektur-Vergleich

### **Aktuell (Legacy - Lead-Modul):**

```
┌─────────────────────────────────┐
│ leads                           │
├─────────────────────────────────┤
│ id: BIGINT PK                   │
│ company_name: VARCHAR(255)      │
│ contact_person: VARCHAR(255)    │ ← Flaches Feld ("Max Mustermann")
│ email: VARCHAR(255)             │
│ phone: VARCHAR(50)              │
│ ...                             │
└─────────────────────────────────┘
```

**Probleme:**
- ❌ Nur 1 Kontakt pro Lead
- ❌ firstName/lastName nicht getrennt (Anrede nicht möglich)
- ❌ Nicht erweiterbar (kein Platz für Position, isPrimary, etc.)
- ❌ Inkonsistent mit Customer-Modul

---

### **Ziel (Best Practice - wie Customer-Modul):**

```
┌─────────────────────────────────┐         ┌─────────────────────────────────┐
│ leads                           │         │ lead_contacts                   │
├─────────────────────────────────┤         ├─────────────────────────────────┤
│ id: BIGINT PK                   │◄────┐   │ id: UUID PK                     │
│ company_name: VARCHAR(255)      │     └───│ lead_id: BIGINT FK              │
│ first_contact_documented_at     │         │ first_name: VARCHAR(100) NN     │
│ ...                             │         │ last_name: VARCHAR(100) NN      │
└─────────────────────────────────┘         │ email: VARCHAR(255)             │
                                            │ phone: VARCHAR(50)              │
                                            │ is_primary: BOOLEAN DEFAULT false│
                                            │ position: VARCHAR(100)          │
                                            │ created_at: TIMESTAMP           │
                                            │ updated_at: TIMESTAMP           │
                                            └─────────────────────────────────┘

                                            Indizes:
                                            - idx_lead_contacts_lead_id (BTREE)
                                            - idx_lead_contacts_primary (BTREE, WHERE is_primary = true)
```

**Vorteile:**
- ✅ **Mehrere Kontakte** pro Lead (N:1 Beziehung)
- ✅ **Strukturierte Daten** (firstName, lastName getrennt)
- ✅ **Erweiterbar** (Position, decisionLevel, etc. später hinzufügbar)
- ✅ **Konsistent** mit Customer-Modul (gleiche Architektur)
- ✅ **Type-Safe** (JPA Entities mit Validierung)

---

## 📋 Migration V276 - lead_contacts Tabelle

### **Phase 1: Neue Tabelle erstellen**

```sql
-- V276__lead_contacts_table.sql

-- 1. Create lead_contacts table (100% PARITY mit customer_contacts)
CREATE TABLE lead_contacts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,

  -- Basic Info (IDENTICAL to customer_contacts)
  salutation VARCHAR(20),              -- herr, frau, divers
  title VARCHAR(50),                   -- Dr., Prof., etc.
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  position VARCHAR(100),
  decision_level VARCHAR(50),          -- executive, manager, operational, influencer

  -- Contact Info (IDENTICAL to customer_contacts)
  email VARCHAR(255),
  phone VARCHAR(50),
  mobile VARCHAR(50),

  -- Flags (IDENTICAL to customer_contacts)
  is_primary BOOLEAN NOT NULL DEFAULT false,
  is_active BOOLEAN NOT NULL DEFAULT true,

  -- Relationship Data (IDENTICAL to customer_contacts - CRM Intelligence)
  birthday DATE,
  hobbies VARCHAR(500),                -- Comma-separated list
  family_status VARCHAR(50),           -- single, married, divorced, widowed
  children_count INTEGER,
  personal_notes TEXT,

  -- Intelligence Data (IDENTICAL to customer_contacts - Future-proof)
  warmth_score INTEGER DEFAULT 50,    -- 0-100, default neutral
  warmth_confidence INTEGER DEFAULT 0, -- 0-100, confidence in warmth score
  last_interaction_date TIMESTAMP,
  interaction_count INTEGER DEFAULT 0,

  -- Data Quality & Freshness (IDENTICAL to customer_contacts)
  data_quality_score INTEGER,          -- 0-100, overall data quality score
  data_quality_recommendations TEXT,   -- Semicolon-separated recommendations

  -- Legacy field from customer_contacts (for future compatibility)
  is_decision_maker BOOLEAN NOT NULL DEFAULT false,
  is_deleted BOOLEAN NOT NULL DEFAULT false,

  -- Audit fields
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by VARCHAR(100),
  updated_by VARCHAR(100),

  -- Constraints
  CONSTRAINT chk_lead_contact_email_or_phone_or_mobile
    CHECK (email IS NOT NULL OR phone IS NOT NULL OR mobile IS NOT NULL),
  CONSTRAINT chk_lead_contact_names_not_empty
    CHECK (LENGTH(TRIM(first_name)) > 0 AND LENGTH(TRIM(last_name)) > 0)
);

-- 2. Create indexes (IDENTICAL pattern to customer_contacts)
CREATE INDEX idx_lead_contacts_lead_id ON lead_contacts(lead_id);
CREATE INDEX idx_lead_contacts_primary ON lead_contacts(lead_id, is_primary) WHERE is_primary = true;
CREATE INDEX idx_lead_contacts_active ON lead_contacts(is_active) WHERE is_active = true;
CREATE INDEX idx_lead_contacts_email ON lead_contacts(email) WHERE email IS NOT NULL;
CREATE INDEX idx_lead_contacts_deleted ON lead_contacts(is_deleted) WHERE is_deleted = false;

-- 3. Create trigger for updated_at
CREATE TRIGGER trg_update_lead_contacts_updated_at
  BEFORE UPDATE ON lead_contacts
  FOR EACH ROW
  EXECUTE FUNCTION update_updated_at_column();

-- 4. Data migration from existing leads (contactPerson → firstName + lastName)
-- Split "Max Mustermann" into first_name="Max", last_name="Mustermann"
INSERT INTO lead_contacts (lead_id, first_name, last_name, email, phone, is_primary, created_at)
SELECT
  l.id AS lead_id,
  -- Split contact_person: "Max Mustermann" → first_name="Max", last_name="Mustermann"
  CASE
    WHEN contact_person IS NOT NULL AND contact_person LIKE '% %'
      THEN SPLIT_PART(contact_person, ' ', 1)
    WHEN contact_person IS NOT NULL
      THEN contact_person
    ELSE 'Unknown'
  END AS first_name,
  CASE
    WHEN contact_person IS NOT NULL AND contact_person LIKE '% %'
      THEN SUBSTRING(contact_person FROM POSITION(' ' IN contact_person) + 1)
    ELSE 'Contact'
  END AS last_name,
  l.email,
  l.phone,
  true AS is_primary, -- Alle migrierten Kontakte werden Primary
  l.created_at
FROM leads l
WHERE l.contact_person IS NOT NULL
   OR l.email IS NOT NULL
   OR l.phone IS NOT NULL;

-- 5. Deprecate old contact fields (keep for backward compatibility)
COMMENT ON COLUMN leads.contact_person IS 'DEPRECATED: Use lead_contacts table. Will be removed in V280.';
COMMENT ON COLUMN leads.email IS 'DEPRECATED: Use lead_contacts table for contact email. Will be removed in V280.';
COMMENT ON COLUMN leads.phone IS 'DEPRECATED: Use lead_contacts table for contact phone. Will be removed in V280.';

-- 6. Add constraint: Every lead MUST have at least 1 active primary contact
-- (deferred to V277 - requires LeadContact Entity + Service Layer first)
```

### **Phase 2: Backward Compatibility (V277)**

```sql
-- V277__lead_contacts_backward_compat.sql

-- Trigger: Sync lead_contacts changes back to leads.contact_person (for legacy API clients)
CREATE OR REPLACE FUNCTION sync_primary_contact_to_lead()
RETURNS TRIGGER AS $$
BEGIN
  -- Update lead.contact_person when primary contact changes
  IF NEW.is_primary = true THEN
    UPDATE leads
    SET contact_person = NEW.first_name || ' ' || NEW.last_name,
        email = NEW.email,
        phone = NEW.phone
    WHERE id = NEW.lead_id;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_primary_contact
  AFTER INSERT OR UPDATE ON lead_contacts
  FOR EACH ROW
  WHEN (NEW.is_primary = true)
  EXECUTE FUNCTION sync_primary_contact_to_lead();

-- Constraint: Only 1 primary contact per lead
CREATE UNIQUE INDEX idx_lead_contacts_one_primary
  ON lead_contacts(lead_id)
  WHERE is_primary = true;
```

### **Phase 3: Cleanup (V280 - Sprint 2.1.7+)**

```sql
-- V280__remove_deprecated_contact_fields.sql (FUTURE)

-- Remove deprecated columns after all clients migrated
ALTER TABLE leads DROP COLUMN contact_person;
ALTER TABLE leads DROP COLUMN email;
ALTER TABLE leads DROP COLUMN phone;

-- Drop backward compatibility trigger
DROP TRIGGER IF EXISTS trg_sync_primary_contact ON lead_contacts;
DROP FUNCTION IF EXISTS sync_primary_contact_to_lead();
```

---

## 🔧 Backend: LeadContact Entity

```java
package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * LeadContact Entity - 100% PARITY mit customer_contacts (Full CRM Intelligence Support)
 *
 * <p>Architektur-Entscheidung (ADR-007 - Option C):
 * - VOLLSTÄNDIGE Harmonisierung mit Customer-Modul (ALLE Felder übernommen)
 * - Multi-Contact Support (N:1 Beziehung)
 * - Strukturierte Daten (firstName + lastName getrennt)
 * - CRM Intelligence Ready (warmth_score, data_quality_score, relationship data)
 * - Future-Proof (alle Customer-Features sofort verfügbar)
 *
 * <p>Migration: V276__lead_contacts_table.sql
 *
 * @since Sprint 2.1.6 Phase 5+ (Option C - Full Parity)
 * @see de.freshplan.domain.customer.entity.Contact (IDENTICAL structure)
 */
@Entity
@Table(
    name = "lead_contacts",
    indexes = {
      @Index(name = "idx_lead_contacts_lead_id", columnList = "lead_id"),
      @Index(name = "idx_lead_contacts_active", columnList = "is_active")
    })
public class LeadContact extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  public UUID id;

  // Relationship
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lead_id", nullable = false)
  public Lead lead;

  // Basic Info (IDENTICAL to customer_contacts)
  @Size(max = 20)
  @Column(name = "salutation", length = 20)
  public String salutation; // herr, frau, divers

  @Size(max = 50)
  @Column(name = "title", length = 50)
  public String title; // Dr., Prof., etc.

  @NotNull
  @Size(min = 1, max = 100)
  @Column(name = "first_name", nullable = false, length = 100)
  public String firstName;

  @NotNull
  @Size(min = 1, max = 100)
  @Column(name = "last_name", nullable = false, length = 100)
  public String lastName;

  @Size(max = 100)
  @Column(name = "position", length = 100)
  public String position;

  @Size(max = 50)
  @Column(name = "decision_level", length = 50)
  public String decisionLevel; // executive, manager, operational, influencer

  // Contact Info (IDENTICAL to customer_contacts)
  @Email
  @Size(max = 255)
  @Column(name = "email", length = 255)
  public String email;

  @Size(max = 50)
  @Column(name = "phone", length = 50)
  public String phone;

  @Size(max = 50)
  @Column(name = "mobile", length = 50)
  public String mobile;

  // Flags (IDENTICAL to customer_contacts)
  @Column(name = "is_primary", nullable = false)
  public boolean isPrimary = false;

  @Column(name = "is_active", nullable = false)
  public boolean isActive = true;

  // Relationship Data (Beziehungsebene - IDENTICAL to customer_contacts)
  @Column(name = "birthday")
  public LocalDate birthday;

  @Size(max = 500)
  @Column(name = "hobbies", length = 500)
  public String hobbies; // Comma-separated list

  @Size(max = 50)
  @Column(name = "family_status", length = 50)
  public String familyStatus; // single, married, divorced, widowed

  @Column(name = "children_count")
  public Integer childrenCount;

  @Column(name = "personal_notes", columnDefinition = "TEXT")
  public String personalNotes;

  // Intelligence Data (CRM Intelligence - IDENTICAL to customer_contacts)
  @Column(name = "warmth_score")
  public Integer warmthScore = 50; // 0-100, default neutral

  @Column(name = "warmth_confidence")
  public Integer warmthConfidence = 0; // 0-100, confidence in warmth score

  @Column(name = "last_interaction_date")
  public LocalDateTime lastInteractionDate;

  @Column(name = "interaction_count")
  public Integer interactionCount = 0;

  // Data Quality & Freshness (IDENTICAL to customer_contacts)
  @Column(name = "data_quality_score")
  public Integer dataQualityScore; // 0-100, overall data quality score

  @Column(name = "data_quality_recommendations", columnDefinition = "TEXT")
  public String dataQualityRecommendations; // Semicolon-separated recommendations

  // Legacy fields from customer_contacts (for compatibility)
  @Column(name = "is_decision_maker", nullable = false)
  public Boolean isDecisionMaker = false;

  @Column(name = "is_deleted", nullable = false)
  public Boolean isDeleted = false;

  // Audit Fields
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  public LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt;

  @Size(max = 100)
  @Column(name = "created_by", length = 100)
  public String createdBy;

  @Size(max = 100)
  @Column(name = "updated_by", length = 100)
  public String updatedBy;

  // Business Methods
  public String getFullName() {
    return firstName + " " + lastName;
  }

  // Builder Pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final LeadContact contact = new LeadContact();

    public Builder lead(Lead lead) {
      contact.lead = lead;
      return this;
    }

    public Builder firstName(String firstName) {
      contact.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      contact.lastName = lastName;
      return this;
    }

    public Builder email(String email) {
      contact.email = email;
      return this;
    }

    public Builder phone(String phone) {
      contact.phone = phone;
      return this;
    }

    public Builder isPrimary(boolean isPrimary) {
      contact.isPrimary = isPrimary;
      return this;
    }

    public Builder position(String position) {
      contact.position = position;
      return this;
    }

    public LeadContact build() {
      return contact;
    }
  }
}
```

---

## 🔧 Backend: Lead Entity Update

```java
@Entity
@Table(name = "leads")
public class Lead extends PanacheEntityBase {

  // ... existing fields ...

  // NEW: Relationship to contacts (1:N)
  @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL, orphanRemoval = true)
  public List<LeadContact> contacts = new ArrayList<>();

  // DEPRECATED: Legacy fields (backward compatibility until V280)
  /**
   * @deprecated Use {@link #contacts} instead. This field will be removed in V280.
   * @see LeadContact
   */
  @Deprecated(since = "2.1.6 Phase 5+", forRemoval = true)
  @Size(max = 255)
  @Column(name = "contact_person")
  public String contactPerson;

  /**
   * @deprecated Use {@link #contacts} instead. This field will be removed in V280.
   */
  @Deprecated(since = "2.1.6 Phase 5+", forRemoval = true)
  @Email
  @Size(max = 255)
  public String email;

  /**
   * @deprecated Use {@link #contacts} instead. This field will be removed in V280.
   */
  @Deprecated(since = "2.1.6 Phase 5+", forRemoval = true)
  @Size(max = 50)
  public String phone;

  // Business Methods
  public LeadContact getPrimaryContact() {
    return contacts.stream()
        .filter(c -> c.isPrimary && c.isActive)
        .findFirst()
        .orElse(null);
  }

  public void addContact(LeadContact contact) {
    contacts.add(contact);
    contact.lead = this;

    // If this is the first contact, make it primary
    if (contacts.size() == 1) {
      contact.isPrimary = true;
    }
  }

  public void removeContact(LeadContact contact) {
    contacts.remove(contact);
    contact.lead = null;
  }
}
```

---

## 📦 API: LeadCreateRequest Update

```java
package de.freshplan.modules.leads.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class LeadCreateRequest {

  @NotNull(message = "Company name is required")
  @Size(min = 1, max = 255)
  public String companyName;

  // NEW: Structured contacts (replaces flat contactPerson)
  @Valid
  public List<ContactData> contacts;

  // Address fields
  @Size(max = 255)
  public String street;

  @Size(max = 20)
  public String postalCode;

  @Size(max = 100)
  public String city;

  @Size(min = 2, max = 2)
  public String countryCode = "DE";

  // Business fields
  @Size(max = 100)
  public String businessType;

  @Size(max = 20)
  public String kitchenSize;

  public Integer employeeCount;
  public BigDecimal estimatedVolume;

  @Size(max = 50)
  public String industry;

  // Lead source tracking
  @Size(max = 100)
  public String source;

  @Size(max = 255)
  public String sourceCampaign;

  // Nested DTO for contact data
  public static class ContactData {
    @NotNull
    @Size(min = 1, max = 100)
    public String firstName;

    @NotNull
    @Size(min = 1, max = 100)
    public String lastName;

    @Email
    @Size(max = 255)
    public String email;

    @Size(max = 50)
    public String phone;

    @Size(max = 100)
    public String position;

    public boolean isPrimary = false;
  }

  // DEPRECATED: Backward compatibility (legacy API clients)
  /**
   * @deprecated Use {@link #contacts} instead. This field will be removed in API v2.
   */
  @Deprecated(since = "2.1.6 Phase 5+", forRemoval = true)
  @Size(max = 255)
  public String contactPerson;

  /**
   * @deprecated Use {@link #contacts} instead.
   */
  @Deprecated(since = "2.1.6 Phase 5+", forRemoval = true)
  @Email
  @Size(max = 255)
  public String email;

  /**
   * @deprecated Use {@link #contacts} instead.
   */
  @Deprecated(since = "2.1.6 Phase 5+", forRemoval = true)
  @Size(max = 50)
  public String phone;
}
```

---

## 📦 Frontend: TypeScript Types Update

```typescript
// types.ts

export type LeadContact = {
  id?: string; // UUID
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  position?: string;
  isPrimary: boolean;
  isActive?: boolean;
  createdAt?: string;
};

export type Lead = {
  id: string;

  // Company Information
  companyName: string;
  city?: string;
  postalCode?: string;

  // NEW: Structured contacts (replaces flat contact fields)
  contacts: LeadContact[];

  // DEPRECATED: Legacy fields (backward compatibility)
  /** @deprecated Use contacts[0] instead */
  contactPerson?: string;
  /** @deprecated Use contacts[0].email instead */
  email?: string;
  /** @deprecated Use contacts[0].phone instead */
  phone?: string;

  // ... other fields
};

// Helper: Get primary contact
export function getPrimaryContact(lead: Lead): LeadContact | undefined {
  return lead.contacts.find(c => c.isPrimary && c.isActive !== false);
}
```

---

## 🔄 Migrationsplan

### **Sprint 2.1.6 Phase 5+ (JETZT):**

1. ✅ **Dokumentation** (dieses Dokument)
2. ✅ **Migration V276** - lead_contacts Tabelle + Daten-Migration
3. ✅ **Migration V277** - Backward Compatibility Trigger
4. ✅ **Backend:**
   - LeadContact Entity + Repository
   - Lead.contacts Beziehung
   - LeadCreateRequest.contacts
   - LeadResource anpassen (POST /api/leads)
5. ✅ **Frontend:**
   - types.ts Update
   - LeadWizard: contacts[] senden statt flat fields
   - LeadList: getPrimaryContact() Helper nutzen
6. ✅ **Tests:**
   - LeadContactTest (CRUD)
   - LeadResourceTest (mit contacts)
   - LeadWizard Integration Tests

### **Sprint 2.1.7+ (LATER):**

7. 🔄 **Multi-Contact UI:**
   - Kontakte-Management Dialog (wie Customer)
   - Add/Edit/Remove Kontakte
   - Set Primary Contact
8. 🔄 **Migration V280** - Remove deprecated fields (contact_person, email, phone)

---

## ✅ Validierung & Tests

### **Backend Tests:**

```java
@QuarkusTest
class LeadContactTest {

  @Test
  @Transactional
  void testCreateLeadWithMultipleContacts() {
    Lead lead = createTestLead("Company XY");

    LeadContact primary = LeadContact.builder()
        .lead(lead)
        .firstName("Max")
        .lastName("Mustermann")
        .email("max@example.com")
        .isPrimary(true)
        .build();

    LeadContact secondary = LeadContact.builder()
        .lead(lead)
        .firstName("Anna")
        .lastName("Schmidt")
        .email("anna@example.com")
        .isPrimary(false)
        .build();

    lead.addContact(primary);
    lead.addContact(secondary);
    lead.persist();

    assertEquals(2, lead.contacts.size());
    assertEquals("Max", lead.getPrimaryContact().firstName);
  }

  @Test
  void testPrimaryContactConstraint() {
    // Only 1 primary contact per lead allowed
    Lead lead = createTestLead("Company");

    LeadContact c1 = LeadContact.builder()
        .lead(lead).firstName("A").lastName("B")
        .isPrimary(true).build();

    LeadContact c2 = LeadContact.builder()
        .lead(lead).firstName("C").lastName("D")
        .isPrimary(true).build();

    lead.addContact(c1);
    lead.addContact(c2);

    assertThrows(ConstraintViolationException.class, () -> lead.persist());
  }
}
```

### **Frontend Tests:**

```typescript
describe('LeadWizard - Multi-Contact Support', () => {
  it('sends structured contacts to API', async () => {
    const { result } = renderHook(() => useLeadWizard());

    await act(async () => {
      await result.current.createLead({
        companyName: 'Test Company',
        contacts: [
          {
            firstName: 'Max',
            lastName: 'Mustermann',
            email: 'max@example.com',
            isPrimary: true,
          },
        ],
      });
    });

    expect(mockFetch).toHaveBeenCalledWith(
      expect.any(String),
      expect.objectContaining({
        body: expect.stringContaining('"firstName":"Max"'),
      })
    );
  });
});
```

---

## 📚 Referenzen

- **Customer.contacts Implementierung:** `backend/src/main/java/de/freshplan/domain/customer/entity/Contact.java`
- **TRIGGER_SPRINT_2_1_6.md:** Phase 5+ Scope-Erweiterung
- **ADR-006:** Lead Scoring (ähnliche Architektur-Entscheidung)
- **VARIANTE_B_MIGRATION_GUIDE.md:** Pre-Claim Logic mit firstContactDocumentedAt

---

## 📝 Zusammenfassung

**Vorher (Legacy):**
- `contact_person VARCHAR(255)` - flaches Feld
- Nur 1 Kontakt pro Lead
- firstName/lastName nicht getrennt

**Nachher (Best Practice):**
- `lead_contacts` Tabelle (N:1 zu leads)
- Mehrere Kontakte pro Lead möglich
- Strukturierte Daten (firstName + lastName getrennt)
- Konsistent mit Customer-Modul
- Backward-compatible durch Trigger (V277)

**Zeitaufwand:** ~3-5h (Migration + Entity + API + Tests)

**Status:** 🚧 In Progress (Sprint 2.1.6 Phase 5+)
