---
module: "02_neukundengewinnung"
doc_type: "adr"
status: "proposed"
owner: "team/leads-backend"
updated: "2025-10-07"
date: "2025-10-07"
decision-makers: ["team/leads-backend", "team/security"]
consulted: ["ChatGPT Code Review"]
informed: ["team/product"]
---

# ADR-007: HMAC-SHA256 for GDPR Pseudonymization (Enhancement)

## Context

**Current Implementation (Sprint 2.1.6 Phase 3):**
- Email-Pseudonymisierung via `SHA-256(email)` für Duplikaterkennung
- DSGVO Art. 4 konform: Personenbezogene Daten werden pseudonymisiert
- Problem: **Dictionary-Anfälligkeit** - SHA-256 ohne Secret ist anfällig für Wörterbuchangriffe

**Code Review Feedback (ChatGPT):**
> SHA-256(Email) ist pseudonymisiert, aber **dictionary-anfällig**. Empfehlung: Ersetze durch **HMAC-SHA-256(email, secretPepper)** mit Key-Versionierung.

## Decision

**Status:** ✅ **PROPOSED** (Sprint 2.1.7+ Enhancement)

Ersetze `SHA-256(email)` durch `HMAC-SHA-256(email, secretPepper)` mit **Key-Versionierung** für DSGVO-Pseudonymisierung.

### Implementation Plan:

#### 1. **Hash-Algorithmus**
```java
// AKTUELL (Sprint 2.1.6):
String emailHash = sha256(lead.email);

// ENHANCEMENT (Sprint 2.1.7+):
String emailHash = hmacSha256(lead.email, getSecretPepper(KEY_VERSION));
```

#### 2. **Key-Management**
- **Secret Storage:** Vault/ENV (NICHT in DB/Code)
- **Key-Versionierung:** `hash_key_version` Column in `leads` Table
- **Rotation:** Bei Key-Wechsel beide Versionen temporär akzeptieren (Übergangszeit)

#### 3. **DB-Schema-Änderung** (Migration V269+)
```sql
-- Add hash metadata columns
ALTER TABLE leads
ADD COLUMN email_hash_algo VARCHAR(20) DEFAULT 'SHA-256',
ADD COLUMN email_hash_key_version INT DEFAULT 1;

-- Index für Duplikat-Check bleibt
CREATE INDEX IF NOT EXISTS idx_leads_email_hash ON leads(email) WHERE email LIKE 'hmac:%';

COMMENT ON COLUMN leads.email_hash_algo IS
  'Sprint 2.1.7+: Hash-Algorithmus für Pseudonymisierung (SHA-256 → HMAC-SHA256)';

COMMENT ON COLUMN leads.email_hash_key_version IS
  'Sprint 2.1.7+: Key-Version für HMAC-Secret-Rotation (default: 1)';
```

#### 4. **Duplikat-Check mit Key-Version**
```java
// Duplikat-Check unterstützt beide Versionen während Übergang
List<Lead> duplicates = Lead.find(
    "email = ?1 OR (email = ?2 AND email_hash_key_version = ?3)",
    sha256Hash,        // Legacy SHA-256
    hmacHash,          // Neuer HMAC
    CURRENT_KEY_VERSION
).list();
```

## Rationale

### ✅ **Vorteile:**

1. **Security Hardening:**
   - Verhindert Wörterbuchangriffe (Rainbow Tables nutzlos ohne Secret)
   - HMAC-SHA256 ist DSGVO-Best-Practice für Pseudonymisierung

2. **Duplikaterkennung bleibt erhalten:**
   - `HMAC(email, secret)` ist deterministisch
   - Gleiche Email = gleicher Hash (mit gleichem Secret)

3. **Key-Rotation möglich:**
   - Key-Versionierung ermöglicht Secret-Wechsel ohne Datenverlust
   - Übergangszeit: Beide Versionen akzeptieren

4. **Compliance:**
   - DSGVO Art. 32 (Technische & organisatorische Maßnahmen)
   - Höherer Schutz personenbezogener Daten

### ⚠️ **Nachteile:**

1. **Secret-Management erforderlich:**
   - Vault/ENV-Integration notwendig
   - Operational Overhead (Secret-Rotation)

2. **Migration Complexity:**
   - Bestehende SHA-256-Hashes müssen koexistieren
   - Duplikat-Check muss beide Versionen unterstützen

3. **Performance (minimal):**
   - HMAC geringfügig langsamer als SHA-256 (~5-10%)
   - Vernachlässigbar bei <1000 Leads/Tag

## Alternatives Considered

### ❌ **Alternative 1: Salt + SHA-256**
- Würde Dictionary-Attacks erschweren
- Aber: **Secret-less** - Salt kann in DB liegen → weniger sicher als HMAC

### ❌ **Alternative 2: Vollständige Anonymisierung (Email → NULL)**
- Höchster Datenschutz
- Aber: **Duplikaterkennung unmöglich** → Business-Anforderung nicht erfüllt

### ❌ **Alternative 3: Encryption statt Hashing**
- Reversibel (mit Key)
- Aber: **Nicht DSGVO-konform** für Pseudonymisierung (Art. 4 Abs. 5)

## Consequences

### **Positive:**
- ✅ DSGVO-Best-Practice für Pseudonymisierung
- ✅ Dictionary-Angriffe praktisch unmöglich
- ✅ Duplikaterkennung bleibt erhalten
- ✅ Key-Rotation möglich (Operational Flexibility)

### **Negative:**
- ⚠️ Secret-Management erforderlich (Vault/ENV)
- ⚠️ Migration-Komplexität (Legacy SHA-256 + HMAC koexistieren)
- ⚠️ Minimaler Performance-Overhead (~5-10% langsamer)

### **Risks:**
- **Secret-Verlust:** Wenn Secret verloren → Duplikaterkennung für alte Hashes unmöglich
  - **Mitigation:** Backup-Strategie für Secrets (Vault-Snapshots)
- **Key-Rotation-Fehler:** Falsche Version → Duplikate nicht erkannt
  - **Mitigation:** Automatisierte Tests für Key-Rotation

## Implementation Checklist

### **Sprint 2.1.7+ (Enhancement):**

- [ ] **Secret-Management:**
  - [ ] Vault-Integration oder ENV-Variable für `HMAC_SECRET_PEPPER`
  - [ ] Key-Versionierung implementieren (`getCurrentKeyVersion()`, `getSecretPepper(version)`)

- [ ] **DB-Migration:**
  - [ ] Migration V269+: `email_hash_algo`, `email_hash_key_version` Columns
  - [ ] Index für HMAC-Hashes: `idx_leads_email_hash`

- [ ] **Code-Änderungen:**
  - [ ] `hmacSha256(email, secret)` Methode in `LeadMaintenanceService`
  - [ ] Duplikat-Check unterstützt SHA-256 + HMAC (Übergangszeit)
  - [ ] Legacy SHA-256 Hashes schrittweise auf HMAC migrieren (Batch-Job)

- [ ] **Tests:**
  - [ ] Unit-Tests für HMAC-Hashing mit Mock-Secret
  - [ ] Integration-Tests für Duplikat-Check (SHA-256 + HMAC)
  - [ ] Key-Rotation-Tests (Version 1 → Version 2)

- [ ] **Dokumentation:**
  - [ ] Secret-Rotation-Runbook (Modul 08 - Betrieb)
  - [ ] DSGVO-Compliance-Nachweis aktualisieren
  - [ ] AUTOMATED_JOBS_SPECIFICATION.md aktualisieren (HMAC statt SHA-256)

## Related Documents

- **AUTOMATED_JOBS_SPECIFICATION.md** - Job 3: DSGVO Pseudonymization (aktuelle SHA-256 Implementation)
- **ADR-003:** B2B-Pseudonymisierung (Original Decision)
- **DSGVO Art. 4 Abs. 5:** Definition Pseudonymisierung
- **DSGVO Art. 32:** Technische & organisatorische Maßnahmen

## Timeline

- **Sprint 2.1.6:** SHA-256 Implementation (aktuell)
- **Sprint 2.1.7+:** HMAC-SHA256 Enhancement (geplant)
- **Übergangszeit:** 6 Monate (beide Algorithmen parallel)
- **Final Cutover:** Nach vollständiger Migration aller Legacy-Hashes

---

**Status:** ✅ **PROPOSED** (Code Review ChatGPT Enhancement)
**Owner:** team/leads-backend + team/security
**Decision Date:** TBD (Sprint 2.1.7 Planning)
