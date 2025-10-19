<!-- CLAUDE_SLIM_BEGIN -->
# 🤖 Claude Meta-Prompt für FreshPlan Sales Tool

**📅 Dokumentstand: 2025-10-18** *(Claude sieht aktuelles Datum in `<env>Today's date`)*

## ⚡ SINGLE SOURCE OF TRUTH
**Master Plan V5:** `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
**Trigger Index:** `/docs/planung/TRIGGER_INDEX.md`
**AI Context:** `/docs/planung/CRM_AI_CONTEXT_SCHNELL.md`
**Roadmap:** `/docs/planung/PRODUCTION_ROADMAP_2025.md`

## 🚀 PFLICHT-STARTSEQUENZ (jede Session)
1. **`/docs/planung/TRIGGER_INDEX.md` lesen** (7-Dokumente-Reihenfolge)
2. **`/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` öffnen** (aktueller Stand/Links)
3. **Migration-Check:** `./scripts/get-next-migration.sh` (niemals Nummern hardcoden!)
4. **TodoWrite/Arbeitslog** laut Trigger-Workflow aktualisieren

## 🤝 COMPACT_CONTRACT v2 (verbindlich)

**Single Source of Truth (SoT):**
- `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` (**MP5**)
- Übergaben liegen unter: `/docs/planung/claude-work/daily-work/YYYY-MM-DD/`
- Trigger-Prozess: **Trigger V3.2 – Teil 1/2**

### Grundprinzipien
1. **Compact = MP5-Update, niemals Eigen-Dokumente.**
   Bei jedem Compact (manuell oder automatisch) schreibe ich **MP5 QUICK UPDATE** in die MP5‑Anker (siehe unten).
2. **Keine neuen Dateien beim Compact.**
   Ausnahme: Das *universelle Handover‑Script* erzeugt eine Übergabe unter `claude-work/…` – das ist erlaubt.
3. **Vorbeugen statt heilen.**
   Bei ~70 % Kontext oder nach großen Meilensteinen **/checkpoint** ausführen (MP5‑Update jetzt, nicht später).
4. **Fallback statt Chaos.**
   Wenn Auto‑Compact droht oder passiert ist: **Emergency Handover** (Minimal‑Inhalte), danach bei nächster Antwort **MP5 QUICK UPDATE** anwenden.

### MP5 QUICK UPDATE (Format)

**SESSION LOG – Eintrag anhängen**
- `YYYY-MM-DD HH:MM` — *Modul/Kontext kurz*: *Was erledigt*, **Migration**: V### (falls relevant), **Tests**: OK/BROKEN

**NEXT STEPS – Liste ersetzen/aktualisieren**
- Konkreter nächster Schritt 1
- Konkreter nächster Schritt 2

**RISKS – optional**
- *RiskID*: kurze Beschreibung – *Mitigation/Owner*

**DECISIONS – optional (ADR‑Verweise)**
- *ADR‑XXXX*: Entscheidung + 1‑Satz‑Begründung

### Kurzbefehle

- **/checkpoint**
  Erzeuge **sofort** ein *MP5 QUICK UPDATE* (Session‑Log anhängen, Next‑Steps aktualisieren).
  *Zweck:* Frühzeitiger Checkpoint vor Auto‑Compact.

- **/finalize**
  Beende die Session mit einem **MP5 QUICK UPDATE** (Session‑Log, Next‑Steps, ggf. Risks/Decisions).
  Danach **keine** neuen Dateien anlegen.

### MP5‑Anker (Zielpositionen in MP5)
Ich schreibe meine Updates **zwischen** diese Anker in MP5:

- `<!-- MP5:SESSION_LOG:START --> … <!-- MP5:SESSION_LOG:END -->`
- `<!-- MP5:NEXT_STEPS:START --> … <!-- MP5:NEXT_STEPS:END -->`
- `<!-- MP5:RISKS:START --> … <!-- MP5:RISKS:END -->`
- `<!-- MP5:DECISIONS:START --> … <!-- MP5:DECISIONS:END -->`

## 🚨 META-ARBEITSREGELN (14 Kern-Regeln)
1. **SPRACHE:** IMMER Deutsch - auch bei Status-Updates, Zusammenfassungen
2. **VALIDIERUNG PFLICHT:** ALLE Änderungen sofort validieren - Links, Dateien, Commands prüfen!
3. **KI-ZEITSCHÄTZUNG:** Task-Klasse bestimmen (A=Doc-Patch:10-30min, B=Code-Skeleton:1-3h, C=Feature:3-8h, D=Migration:2-5h, E=Security:1-2T, F=Infra:1-3T) + C/H/CI aufschlüsseln
4. **TESTS PFLICHT:** PR ≥80% Coverage + keine Verschlechterung; Modul ≥85%
5. **KLEINE PRS:** 1-2 Tage, <50 Files, <2000 LOC - inkrementell arbeiten
6. **NEUE DATEIEN ERLAUBT:** Kuratiert in Modul-/Testpfaden, im PR deklarieren
7. **FRESHFOODZ CI:** Theme V2 (#94C456, #004F7B, Antonio Bold, Poppins)
8. **CODE-HYGIENE:** Spotless/Prettier vor Review + SOLID/DRY/KISS
9. **SECURITY:** Keine Hardcoded Secrets, Input Validation, CORS korrekt
10. **GRÜNDLICHKEIT:** Vor Schnelligkeit - keine Quick-Fixes ohne Doku
11. **ARCHITEKTUR:** Backend: domain/api/infrastructure, Frontend: features/components
12. **PERFORMANCE:** Bundle ≤200KB, API <200ms P95, keine N+1 Queries
13. **CI GRÜN:** Bei roter CI selbstständig debuggen (nur Feature-Branches!)
14. **REPOSITORY SAUBER:** `./scripts/quick-cleanup.sh` vor jedem Push

## ⚠️ KRITISCHE GUARDRAILS

### 🗄️ BACKEND/FRONTEND PARITY (ZERO TOLERANCE!)
**REGEL:** Jedes Frontend-Feld MUSS im Backend existieren. Keine Exceptions!

✅ **RICHTIG: Backend → API → Frontend**
```typescript
// 1. Backend: Customer.java
private FinancingType primaryFinancing;

// 2. EnumResource.java
@GET @Path("/api/enums/financing-types")

// 3. Frontend: fieldCatalog.json
{"key": "primaryFinancing", "fieldType": "enum", "enumSource": "financing-types"}
```

❌ **FALSCH: Hardcoded Frontend-Felder**
```json
// NIEMALS hardcoded options ohne Backend-Enum!
{"fieldType": "select", "options": [...]}
```

**Bei neuen Feldern:**
1. Migration + Customer.java Entity-Feld hinzufügen
2. Falls Enum: EnumResource.java Endpoint erstellen (`/api/enums/xyz`)
3. fieldCatalog.json mit `enumSource` (NICHT `options`)

**Automatische Guards (installiert):**
- 🔒 Pre-commit Hook: `./scripts/setup-git-hooks.sh` (blockt Commits)
- 🔒 CI Check: `.github/workflows/field-parity-check.yml` (blockt PRs)
- 🔍 Manuell: `python3 scripts/check-field-parity.py`

### 🎨 DESIGN SYSTEM COMPLIANCE (NO HARDCODING!)
**REGEL:** Nutze MUI Theme System. Keine hardcoded Colors/Fonts. UI in Deutsch.

✅ **RICHTIG: Theme System nutzen**
```typescript
// Colors: theme.palette verwenden
sx={{ bgcolor: 'primary.main', color: 'text.primary' }}

// Fonts: theme.typography verwenden
<Typography variant="h4"> // Antonio Bold
<Typography variant="body1"> // Poppins Regular

// UI Text: Deutsch
<Button>Speichern</Button>
<Button>Löschen</Button>
```

❌ **FALSCH: Hardcoded Values**
```typescript
// NIEMALS direkte Hex-Colors!
sx={{ bgcolor: '#94C456', color: '#004F7B' }}

// NIEMALS direkte Font-Angaben!
sx={{ fontFamily: 'Antonio, sans-serif' }}

// NIEMALS englische UI-Texte!
<Button>Save</Button>
<Button>Delete</Button>
```

**Design System Referenz:**
- **Farben:** Primärgrün #94C456, Dunkelblau #004F7B
- **Fonts:** Antonio Bold (Headlines), Poppins (Body)
- **Docs:** `docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Automatische Guards (installiert):**
- 🔒 Pre-commit Hook: `./scripts/setup-git-hooks.sh` (blockt Commits)
- 🔒 CI Check: `.github/workflows/design-system-check.yml` (blockt PRs)
- 🔍 Manuell: `python3 scripts/check-design-system.py`

### 🔢 Migration-Nummern (2-Sequenzen-Modell)
**NIEMALS hardcoden! IMMER `./scripts/get-next-migration.sh` nutzen!**

**Sequenz 1 (GEMEINSAM):** Production (`db/migration/`) + Test (`db/dev-migration/`) teilen V1-V89999
- NEXT = MAX(highest_prod, highest_test) + 1
- Erlaubt Test→Production ohne Renumbering (nur Ordner wechselt)

**Sequenz 2 (EIGENE):** SEED (`db/dev-seed/`) nutzt V90001+ (nur %dev Profile, UI-Test-Daten)
- NEXT = highest_seed + 1

```bash
# IMMER verwenden (ermittelt automatisch korrekte Sequenz):
./scripts/get-next-migration.sh
```

### 🔧 CI-Auto-Fix Limits
- **NUR Feature-Branches** (niemals main!)
- **Required Reviews bleiben** bestehen
- **Token-Scope minimal** (read:repo, write:packages falls nötig)

### 🚫 GIT PUSH POLICY (KRITISCH!)
- **NIEMALS `git push` ohne explizite User-Erlaubnis!**
- **NUR `git commit` automatisch erlaubt** (wenn User darum bittet)
- **User entscheidet:** Wann und ob gepusht wird
- **PR-Erstellung:** Nur auf explizite Anforderung

## 💻 MINIMAL-KOMMANDOS
```bash
# Frontend
npm run lint && npm run test:coverage && npm run security:audit

# Backend
./mvnw verify -DskipITs=false
# optional: OWASP Dependency-Check / SpotBugs

# CI-Logs
gh run list --branch <branch> --status failure --limit 1
gh run view <RUN_ID> --log-failed
```

## 🔒 KRITISCHE DOKUMENTE (niemals löschen)
- `/docs/planung/TRIGGER_INDEX.md` - Sprint-Trigger mit Pflicht-Reihenfolge
- `/CLAUDE.md` - Diese Meta-Arbeitsregeln
- `/docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md` - Aktueller Projektstand

## 🎯 ALLES WEITERE
**Im Master Plan V5:** Troubleshooting, Debug-Guides, Technical Concepts, Workflows
**Bei Problemen:** Alle Lösungen sind im Master Plan V5 verlinkt!

---
**🤖 Zeilen-Budget: ≤150 | Keine Details hier - nur Meta-Regeln + Quick-Start + Guardrails**
<!-- CLAUDE_SLIM_END -->