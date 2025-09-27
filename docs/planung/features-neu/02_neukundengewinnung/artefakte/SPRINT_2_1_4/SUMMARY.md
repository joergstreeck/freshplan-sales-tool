---
module: "02_neukundengewinnung"
sprint: "2.1.4"
doc_type: "konzept"
status: "draft"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.4 – Artefakte Summary

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → SPRINT_2_1_4 → Summary

## 🎯 Sprint-Ziel
Lead Deduplication & Data Quality – Phase 1: Normalisierung, Soft-Deduplizierung und Idempotenz

## 📋 Deliverables

### **1. Datenbank-Migration**
- **Migration:** V247 (dynamisch ermittelt via `./scripts/get-next-migration.sh`)
- **Neue Felder:** `email_normalized`, `name_normalized`, `phone_e164`
- **Partial UNIQUE Index:** `(tenant_id, email_normalized) WHERE email_normalized IS NOT NULL`

### **2. Backend-Services**
- **LeadNormalizationService:** Normalisierungs-Logik für E-Mail, Name, Telefon
- **IdempotencyService:** 24h TTL Store für Request-Deduplizierung
- **LeadService (erweitert):** Konflikt-Handling mit RFC7807

### **3. API-Erweiterungen**
- **Header:** `Idempotency-Key` Support
- **Responses:**
  - 201 Created: Neuer Lead erstellt
  - 200 OK: Idempotente Wiederholung
  - 409 Conflict: Duplikat erkannt (mit `errors.email` Details)

### **4. Tests**
- Unit-Tests für Normalisierung
- Integration-Tests für Unique-Constraint
- Idempotenz-Tests mit Request-Hash-Validierung
- Konflikt-Handling-Tests (409 Response)

## 📊 Metriken
- `leads_conflicts_total{tenant_id}` - Anzahl Duplikat-Konflikte
- `idempotency_hits_total` - Anzahl idempotente Wiederholungen
- `normalization_errors_total{field}` - Fehler bei Normalisierung

## 🔗 Verweise
- **Trigger:** [TRIGGER_SPRINT_2_1_4.md](../../../../TRIGGER_SPRINT_2_1_4.md)
- **Backend-Übersicht:** [backend/_index.md](../../backend/_index.md)
- **ADR:** [ADR-002-normalization.md](../../shared/adr/ADR-002-normalization.md)

## ✅ Definition of Done
- [ ] Migration V247 erfolgreich ausgeführt
- [ ] Normalisierung funktioniert für alle Felder
- [ ] Unique-Constraint verhindert neue Duplikate
- [ ] Idempotenz-Store aktiv mit 24h TTL
- [ ] RFC7807 409-Responses korrekt
- [ ] Tests grün (Unit + Integration)
- [ ] Metriken erfasst