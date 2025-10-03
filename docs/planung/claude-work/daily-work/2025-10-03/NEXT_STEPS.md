# 📋 NEXT STEPS - Sprint 2.1.5 Dokumentation

**Erstellt:** 2025-10-03 21:55
**Status:** 2/7 Tasks COMPLETE, 5/7 PENDING
**Branch:** feature/mod02-sprint-2.1.5-implementation

---

## ✅ COMPLETED (Session 1):

1. ✅ **3 neue Artefakte erstellt:**
   - `artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md`
   - `artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md`
   - `artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md`

2. ✅ **TRIGGER_SPRINT_2_1_5.md aktualisiert:**
   - DSGVO Consent → Source-abhängig
   - Pre-Claim Mechanik dokumentiert
   - Dedupe Policy hinzugefügt
   - entry_points erweitert

---

## 🔧 TODO (Session 2 - START HIER):

### **Task 3: Master Plan V5 aktualisieren**

**Datei:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`

**Änderungen:**
```markdown
<!-- MP5:SESSION_LOG:START -->
### 2025-10-03 21:30 - Sprint 2.1.5 Planungsdokumentation COMPLETE

**Kontext:** Lead-Erfassung Redesign (Pre-Claim, Dedupe, Quellen-Pflichtfelder)

**Erledigt:**
- ✅ 3 neue Artefakte: PRE_CLAIM_LOGIC, DEDUPE_POLICY, ACTIVITY_TYPES_PROGRESS_MAPPING
- ✅ TRIGGER_SPRINT_2_1_5 aktualisiert (DSGVO Consent Source-abhängig)
- ✅ Pre-Claim Mechanik dokumentiert (10 Tage Frist, Migration-Ausnahme)
- ✅ Dedupe Policy definiert (Hard/Soft Collisions, kein Fuzzy in 2.1.5)
- ✅ 13 Activity-Types verbindlich gemapped (5 Progress, 5 Non-Progress, 3 System)

**Tests:** N/A (reine Dokumentation)

**Migration:** Keine neue Migration (V255-V257 bereits deployed)
<!-- MP5:SESSION_LOG:END -->

<!-- MP5:DECISIONS:START -->
### 2025-10-03 - Pre-Claim Mechanik + Dedupe Policy + Consent-Logik

**Entscheidung:**
1. **Pre-Claim Stage 0:** Lead ohne Kontakt/Erstkontakt = Pre-Claim (registered_at = NULL, 10 Tage Frist)
   - Schutz startet erst bei Kontakt ODER dokumentiertem Erstkontakt
   - Ausnahme: Bestandsleads bei Migration → sofortiger Schutz
2. **DSGVO Consent Source-abhängig:**
   - `source = WEB_FORMULAR` → Consent PFLICHT (Art. 6 Abs. 1 lit. a)
   - `source != WEB_FORMULAR` → Berechtigtes Interesse (Art. 6 Abs. 1 lit. f)
3. **Dedupe Policy 2.1.5:**
   - Harte Kollisionen (Email/Phone/Firma+PLZ exakt) → BLOCK + Manager-Override
   - Weiche Kollisionen (Domain+Stadt, Firma+Stadt) → WARN + Fortfahren
   - KEIN Fuzzy-Matching (pg_trgm) → Sprint 2.1.6

**Begründung:**
- Vertrag §2(8)(a): "Firma, Ort und zentraler Kontakt ODER dokumentierter Erstkontakt"
- B2B-Partner-Erfassung: berechtigtes Interesse ausreichend (ChatGPT + Claude Validierung)
- Pragmatische Dedupe-Strategie: Harte Blocks sofort, Fuzzy später (ChatGPT Empfehlung)

**Referenz:** Handelsvertretervertrag.pdf, ChatGPT Session 2025-10-03
<!-- MP5:DECISIONS:END -->
```

---

### **Task 4: CRM_AI_CONTEXT_SCHNELL.md aktualisieren**

**Datei:** `/docs/planung/CRM_AI_CONTEXT_SCHNELL.md`

**Änderungen:**
- Lead-Management Abschnitt erweitern um Pre-Claim Mechanik
- Dedupe-Policy kurz erwähnen (Hard/Soft, kein Fuzzy in 2.1.5)
- Activity-Types Progress-Mapping Referenz hinzufügen

**Beispiel:**
```markdown
### Lead-Management (Modul 02)
...
**Pre-Claim Mechanik:**
- Stage 0 ohne Kontakt → Pre-Claim (registered_at = NULL, 10 Tage Frist)
- Schutz startet bei Kontakt ODER dokumentiertem Erstkontakt
- Migration-Ausnahme: Bestandsleads → sofortiger Schutz

**Dedupe-Policy (2.1.5):**
- Hard: Email/Phone/Firma+PLZ exakt → BLOCK
- Soft: Domain+Stadt, Firma+Stadt → WARN
- Fuzzy: Sprint 2.1.6 (pg_trgm)
...
```

---

### **Task 5: TRIGGER_INDEX.md aktualisieren**

**Datei:** `/docs/planung/TRIGGER_INDEX.md`

**Änderungen:**
```markdown
🔧 TRIGGER_SPRINT_2_1_5.md - Lead Protection & Progressive Profiling
   - 6-Monats-Schutz + 60-Tage-Aktivitätsstandard
   - Progressive Profiling (Stage 0/1/2)
   - **PRE-CLAIM MECHANIK:** 10 Tage Frist (registered_at = NULL)
   - **DEDUPE POLICY:** Hard/Soft Collisions (kein Fuzzy in 2.1.5)
   - **ACTIVITY-TYPES:** 13 Types verbindlich gemapped (5 Progress, 8 Non-Progress/System)
   - Status: 🔧 IN PROGRESS (Frontend Phase 2)
   - **NEUE ARTEFAKTE:**
     - [Pre-Claim Logic](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md)
     - [Dedupe Policy](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md)
     - [Activity Types Mapping](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md)
```

---

### **Task 6: SPRINT_MAP.md aktualisieren**

**Datei:** `features-neu/02_neukundengewinnung/SPRINT_MAP.md`

**Änderungen:**
```markdown
### Sprint 2.1.5 Artefakte

**Backend:**
- [ADR-004: Inline-First Architecture](shared/adr/ADR-004-lead-protection-inline-first.md)
- [DELTA_LOG_2_1_5](artefakte/SPRINT_2_1_5/DELTA_LOG_2_1_5.md)
- [CONTRACT_MAPPING](artefakte/SPRINT_2_1_5/CONTRACT_MAPPING.md)
- [TEST_PLAN](artefakte/SPRINT_2_1_5/TEST_PLAN.md)
- [PRE_CLAIM_LOGIC](artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md) ⭐ NEU
- [DEDUPE_POLICY](artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md) ⭐ NEU
- [ACTIVITY_TYPES_PROGRESS_MAPPING](artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md) ⭐ NEU

**Frontend:**
- [FRONTEND_ACCESSIBILITY](artefakte/SPRINT_2_1_5/FRONTEND_ACCESSIBILITY.md)
- [SUMMARY](artefakte/SPRINT_2_1_5/SUMMARY.md)
```

---

### **Task 7: Validierung**

**Checkliste:**
- [ ] Link-Check: Alle Artefakt-Links funktionieren
- [ ] Breadcrumbs vorhanden in allen 3 neuen Artefakten (✅ bereits vorhanden)
- [ ] Front-Matter Lint: module, domain, doc_type, status, owner, updated (✅ bereits vorhanden)
- [ ] Konsistenz: Gleiche Terminologie (Pre-Claim vs. Vormerkung)
- [ ] Datums-Check: updated = 2025-10-03 in allen geänderten Dateien

**Commands:**
```bash
# Link-Check (manuell)
grep -r "PRE_CLAIM_LOGIC" docs/planung/
grep -r "DEDUPE_POLICY" docs/planung/
grep -r "ACTIVITY_TYPES_PROGRESS_MAPPING" docs/planung/

# Front-Matter Check
head -20 docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md
head -20 docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DEDUPE_POLICY.md
head -20 docs/planung/features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/ACTIVITY_TYPES_PROGRESS_MAPPING.md
```

---

## 🚀 Quick-Start für Session 2:

```bash
# 1. Branch prüfen
git branch --show-current
# Sollte sein: feature/mod02-sprint-2.1.5-implementation

# 2. Handover lesen
cat docs/planung/claude-work/daily-work/2025-10-03/2025-10-03_HANDOVER_21-55.md

# 3. Diese Datei öffnen
cat docs/planung/claude-work/daily-work/2025-10-03/NEXT_STEPS.md

# 4. Task 3 starten (Master Plan V5)
# Siehe Änderungen oben kopieren
```

---

## 📊 Progress-Tracking:

| Task | Status | Datei | Zeilen |
|------|--------|-------|--------|
| 1. Neue Artefakte | ✅ COMPLETE | PRE_CLAIM_LOGIC.md | ~600 |
| 1. Neue Artefakte | ✅ COMPLETE | DEDUPE_POLICY.md | ~650 |
| 1. Neue Artefakte | ✅ COMPLETE | ACTIVITY_TYPES_PROGRESS_MAPPING.md | ~550 |
| 2. TRIGGER Update | ✅ COMPLETE | TRIGGER_SPRINT_2_1_5.md | ~100 |
| 3. MP5 Update | 🔧 TODO | CRM_COMPLETE_MASTER_PLAN_V5.md | ~50 |
| 4. AI Context | 🔧 TODO | CRM_AI_CONTEXT_SCHNELL.md | ~30 |
| 5. Trigger Index | 🔧 TODO | TRIGGER_INDEX.md | ~20 |
| 6. Sprint Map | 🔧 TODO | SPRINT_MAP.md | ~15 |
| 7. Validierung | 🔧 TODO | - | - |

**Total:** 2/7 COMPLETE, 5/7 TODO

---

**Erstellt:** 2025-10-03 21:55
**Geschätzte Zeit für Session 2:** ~30min (5 x ~6min pro Task)
**Nächster Start:** Direkt mit Task 3 (Master Plan V5)
