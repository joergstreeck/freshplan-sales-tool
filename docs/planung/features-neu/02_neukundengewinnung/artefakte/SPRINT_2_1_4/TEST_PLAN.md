---
module: "02_neukundengewinnung"
sprint: "2.1.4"
doc_type: "konzept"
status: "draft"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Test Plan – Sprint 2.1.4

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → SPRINT_2_1_4 → Test Plan

## 🎯 Test-Scope
Validierung der Normalisierung, Deduplizierung und Idempotenz für Lead-Erfassung

## 🧪 Unit Tests

### **Normalisierung**
```java
@Test
void shouldNormalizeEmail() {
    // " JoHn.Doe+test@EXAMPLE.COM " → "john.doe@example.com"
    // Mit Flag EMAIL_REMOVE_PLUS_TAGS: → "john.doe@example.com"
}

@Test
void shouldNormalizeName() {
    // "  Jörg   MÜLLER  " → "jorg muller"
    // Unicode-Normalisierung + Whitespace + Lowercase
}

@Test
void shouldNormalizePhoneToE164() {
    // "+49 (30) 123-456" → "+4930123456"
    // "030/123456" mit DE-Kontext → "+4930123456"
}
```

### **Idempotenz-Store**
```java
@Test
void shouldStoreAndRetrieveIdempotencyKey() {
    // Save mit TTL
    // Retrieve innerhalb TTL → Hit
    // Retrieve nach TTL → Miss
}

@Test
void shouldValidateRequestHash() {
    // Gleicher Key, anderer Request → Reject
    // Gleicher Key, gleicher Request → Accept
}
```

## 🔗 Integration Tests

### **API-Tests**
```java
@Test
void shouldCreateLeadSuccessfully() {
    // POST /api/leads → 201
    // Normalisierte Felder gesetzt
}

@Test
void shouldReturn409OnDuplicateEmail() {
    // Erster POST → 201
    // Zweiter POST (gleiche E-Mail) → 409
    // Response enthält errors.email Array
}

@Test
void shouldHandleIdempotentRequests() {
    // POST mit Idempotency-Key → 201
    // Wiederholung mit gleichem Key → 200 (identische Response)
}

@Test
void shouldNotConflictAcrossTenants() {
    // Tenant A: john@example.com → 201
    // Tenant B: john@example.com → 201 (kein Konflikt)
}
```

### **Database-Tests**
```java
@Test
void partialUniqueIndexShouldWork() {
    // Insert mit email_normalized=NULL → OK
    // Insert mit gleicher email_normalized → Constraint Violation
}

@Test
void backfillShouldNormalizeExistingData() {
    // Alte Daten ohne Normalisierung
    // Migration-Run
    // Alle Felder normalisiert
}
```

## 📊 Performance Tests

```java
@Test
void normalizationShouldBeFast() {
    // P95 < 5ms für Normalisierung
    // Batch-Normalisierung: 1000 Records < 500ms
}

@Test
void idempotencyLookupShouldBeFast() {
    // Cache-Hit < 1ms
    // DB-Lookup < 10ms
}
```

## 🔒 Security Tests

```java
@Test
@TestSecurity(user = "sales", roles = {"SALES"})
void shouldEnforceRBACForLeadCreation() {
    // SALES Role → 201
    // No Role → 403
}

@Test
void shouldPreventInjectionInNormalization() {
    // SQL-Injection-Attempts in E-Mail/Name
    // XSS-Attempts
    // Unicode-Exploits
}
```

## ✅ Test Coverage Requirements
- **Unit Tests:** ≥90% für Normalisierungs-Services
- **Integration Tests:** Alle API-Endpoints
- **DB Tests:** Migration + Constraints
- **Performance:** P95 Benchmarks validiert

## 🚨 Edge Cases
- Null/Empty-Werte in allen Feldern
- Unicode-Edge-Cases (Emojis, RTL-Text)
- Sehr lange Strings (>1000 Zeichen)
- Ungültige E-Mail-Formate
- Internationale Telefonnummern ohne Ländercode
- Race-Conditions bei gleichzeitigen Inserts