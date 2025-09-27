---
module: "standards"
domain: "shared"
doc_type: "guideline"
status: "active"
owner: "team/qa"
updated: "2025-09-27"
---

# 📘 Rollout-Plan – Docs‑Compliance auf alle Module (Hybrid-Methodik)

**Ziel:** Die bereits produktive Docs‑Compliance‑Pipeline (frontmatter‑lint + structure‑guard + docs‑compliance) wird schrittweise
von den modernisierten Modulen **02**/**03** auf **00**, **01** und die übrigen Module (**04–08**) ausgerollt – ohne die CI‑Stabilität zu gefährden.

---

## 1) Aktueller Ist‑Stand (nach PR #120 und #121)

**CI/Jenkins‑Äquivalent (GitHub Actions):**
- **`.github/workflows/docs-compliance.yml`** – Analyse + PR‑Kommentare + JSON‑Artefakte
- **`.github/workflows/structure-guard.yml`** – Strukturschutz (8‑Items‑Regel in modernisierter Form)
- **`.github/scripts/frontmatter_lint.py`** – Front‑Matter‑Lint mit **modularem Scope**

**Wirksamkeit pro Modul (Stand jetzt):**

| Modul | Guard‑Limit (Verzeichnisanzahl, Stubs ausgenommen) | Lint‑Scope | Status |
|------:|:----------------------------------------------------|:-----------|:------|
| 00_infrastruktur | **15** (tolerant) | **Nein** | Legacy (noch nicht restrukturiert) |
| 01_mein-cockpit  | **15** (tolerant) | **Nein** | Legacy (noch nicht restrukturiert) |
| 02_neukundengewinnung | **5 + legacy-planning** (strict) | **Ja** | Modernisiert ✅ |
| 03_kundenmanagement  | **5 + legacy-planning** (strict) | **Ja** | Modernisiert ✅ |
| 04_… | **10** (tolerant) | **Nein** | Legacy |
| 05_… | **10** (tolerant) | **Nein** | Legacy |
| 06_… | **10** (tolerant) | **Nein** | Legacy |
| 07_… | **10** (tolerant) | **Nein** | Legacy |
| 08_… | **10** (tolerant) | **Nein** | Legacy |

**Definitionen:**
- *legacy-planning Zusatzslot*: Ein zusätzlicher erlaubter Ordner `legacy-planning/`.
- *strict*: 5 Kernverzeichnisse (**backend**, **frontend**, **shared**, **analyse**, **artefakte**) **+** optional `legacy-planning/`.
- *tolerant*: Übergangsgrenzen zur Vermeidung unnötiger CI‑Blockaden.

---

## 2) Dokumentierte Guard‑Erweiterung (Structure‑Guard)

**Was wurde geändert (Kurzfassung):**
- **Nur Verzeichnisse zählen** (Dateien werden ignoriert).
- **Stub‑Ordner** werden **ausgeschlossen**, wenn `./_index.md` `doc_type: "stub"` enthält.
- **Modul‑spezifische Limits**:
  - `02_…` / `03_…`: **5 + legacy-planning**
  - `00_…` / `01_…`: **15**
  - alle anderen (`04–08`): **10**
- **Robuste Bash‑Schleifen** (kein `mapfile`, kein Word‑Splitting via `ls`).
- **Explizites Exit‑Handling** (`set +e`, eigener `fail`‑Code).
- Temporäre **Debug‑Logs** (werden nach stabiler Phase wieder entfernt).

> **Konsequenz:** 02/03 bleiben streng, alle anderen Module blockieren die Pipeline nicht mehr – bis sie gezielt modernisiert werden.

---

## 3) Rollout‑Prinzip (inkrementell & risikominimiert)

Wir modernisieren **ein Modul nach dem anderen**. Je Modul folgen wir einem **fixen Ablauf**:

### 3.1 DoD je Modul (Definition of Done)
1) **Struktur**
   - Root enthält nur die 5 Kernordner (**backend**, **frontend**, **shared**, **analyse**, **artefakte**)
     + optional **legacy-planning**.
   - **README.md** im Modul‑Root ist ein **Redirect‑Stub** → `_index.md`.

2) **Front‑Matter**
   - Alle relevanten `.md`‑Dateien (insb. `_index.md`, `SPRINT_MAP.md`, `technical-concept.md`, Artefakt‑Übersichten)
     haben vollständiges Front‑Matter:
   ```yaml
   module: "<NN_modulname>"
   domain: "shared"     # oder backend/frontend – falls sinnvoll
   doc_type: "<stub|technical_concept|sprint_map|analyse|guideline|...>"
   status: "<active|draft|moved|archived>"
   owner: "team/<verantwortliche_einheit>"
   updated: "YYYY-MM-DD"
   # falls moved/stub:
   moved_to: "../_index.md"
   ```

   * **Stub‑Regel**: Wenn `doc_type: "stub"` **oder** `status: "moved"`, **muss** `moved_to` gesetzt sein.

3) **Legacy‑Konsolidierung**
   * Historische Inhalte wandern – wenn sinnvoll – unter `legacy-planning/` (Navigation‑Hub mit `_index.md`).

4) **CI**
   * **Lint‑Scope** wird um das Modul erweitert (siehe Abschnitt 4.1).
   * **Guard‑Limit** des Moduls wird schrittweise auf **strict** abgesenkt (siehe Abschnitt 4.2).
   * **Alle Checks grün** vor Merge (Policy).

---

## 4) Schrittweises Schärfen der CI

### 4.1 Front‑Matter Lint – Scope erweitern

Der Linter (`.github/scripts/frontmatter_lint.py`) validiert aktuell **nur** `02_…` und `03_…`.

**Schritt je Modul:**

1. In `frontmatter_lint.py` das Modul zur Liste `MODULES_IN_SCOPE` hinzufügen.
2. PR öffnen → CI prüfen.
3. Falls Fehler: gezielt Front‑Matter ergänzen, **keine** großen Re‑Struktur‑Sprünge im selben PR.

**Beispiel‑Diff:**

```diff
- MODULES_IN_SCOPE = ("02_neukundengewinnung", "03_kundenmanagement")
+ MODULES_IN_SCOPE = ("02_neukundengewinnung", "03_kundenmanagement", "00_infrastruktur")
```

### 4.2 Structure‑Guard – Limits reduzieren

Wenn das Modul die Struktur erfüllt, reduzieren wir die Toleranz **stufenweise**:

| Phase | Guard‑Limit für das Modul        |
| :---: | :------------------------------- |
|   A   | 15 (nur 00/01) / 10 (04–08)      |
|   B   | 8                                |
|   C   | **5 + legacy-planning** (strict) |

> Die Reduktion erfolgt **nach** erfolgreichem Lint‑Scope‑Rollout und grünem Build auf dem jeweiligen Modul.

---

## 5) Konkreter Rollout‑Plan (Sequenz & Inhalte)

### Phase 0 – Stabilisierung (✅ erledigt)

* Guard robust gemacht, Lint auf 02/03 fokussiert, Permissions und Artefakte gefixt.

### Phase 1 – Modul 00_infrastruktur

* **Ziele**: Minimal‑Modernisierung, Navigation konsolidieren.
* **Arbeitsplan (2–3 PRs):**

  1. **PR‑A**: `_index.md`, `SPRINT_MAP.md`, `technical-concept.md` anlegen/prüfen (mit Front‑Matter).
     README → Redirect‑Stub. Legacy‑Material unter `legacy-planning/` sammeln.
  2. **PR‑B**: `frontmatter_lint.py` – `00_infrastruktur` in `MODULES_IN_SCOPE` aufnehmen.
     Front‑Matter‑Fehler fixen (kleine, gezielte Änderungen).
  3. **PR‑C**: Guard‑Limit **15 → 8** (und später **8 → 5+legacy**).
* **DoD:** CI grün bei Limit 8; Nachfixe → Limit 5+legacy.

### Phase 2 – Modul 01_mein-cockpit

* Gleicher Ablauf wie Modul 00.
* Besonderheit: Prüfen, ob es eigenständige Artefakt‑Hubs gibt → ggf. `artefakte/_index.md` ergänzen.

### Phase 3 – restliche Module (04–08)

* Nacheinander, je Modul in 2–3 PRs:

  1. Struktur/Navi + Redirects.
  2. Lint‑Scope aufnehmen + Front‑Matter minimal fixen.
  3. Guard‑Limit **10 → 8 → 5+legacy**.

> **Parallelisierung:** Max. **ein** Modul gleichzeitig auf **strict** umstellen, um CI‑Rauschen zu vermeiden.

---

## 6) PR‑Checkliste (immer verwenden)

**Beschreibung (PR‑Template konform):**

* **Ziel:** (1‑2 Sätze)
* **Risiken:** (Breaking Links? Hohe Dateizahl?)
* **Migration:** (nur Doku)
* **Performance:** (n/a)
* **Security:** (n/a)
* **Single Source of Truth (SoT):** (Verweise auf `_index.md`, `SPRINT_MAP.md`)

**Technische Punkte:**

* [ ] **README Redirect‑Stub** vorhanden (→ `_index.md`)
* [ ] **Front‑Matter** auf `_index.md`, `SPRINT_MAP.md`, `technical-concept.md`
* [ ] **Stub‑Ordner** mit `doc_type: "stub"` + `moved_to`
* [ ] **legacy-planning/_index.md** (falls verwendet)
* [ ] **Lint‑Scope** angepasst (**wenn** das Modul in die Prüfung aufgenommen wird)
* [ ] **Guard‑Limit** angepasst (**wenn** die Struktur steht)

**Merge‑Policy (verbindlich):**

> **Nie** mergen, wenn **irgendein** CI‑Check rot ist. Erst grün fixen, dann mergen.

---

## 7) Governance & Ownership

* **DRI für Rollout:** `team/qa` (koordiniert), Modul‑DRIs liefern Inhalte.
* **Änderungen an Regeln:**

  * **Guard‑Limits**: `.github/workflows/structure-guard.yml`
  * **Lint‑Scope**: `.github/scripts/frontmatter_lint.py` (`MODULES_IN_SCOPE`)
* **Debug‑Logs entfernen:**
  Nach **2 stabilen Wochen** ohne Guard/Lint‑Fehler die Debug‑Ausgaben in Guard/Lint entfernen.

---

## 8) Anhang

### 8.1 Muster: Redirect‑Stub (`README.md`)

```md
---
module: "NN_modulname"
domain: "shared"
doc_type: "stub"
status: "moved"
owner: "team/<owner>"
updated: "YYYY-MM-DD"
moved_to: "./_index.md"
---
# ➡️ Siehe Modul-Einstieg
Weiter zu [_index.md](./_index.md).
```

### 8.2 Muster: `legacy-planning/_index.md`

```md
---
module: "NN_modulname"
domain: "shared"
doc_type: "guideline"
status: "archived"
owner: "team/<owner>"
updated: "YYYY-MM-DD"
---
# 📚 Legacy Planning – <Modulname>
Kurzbeschreibung & Linksammlung historischer Dokumente.
```

### 8.3 Typische Fehler & schnelle Fixes

* **Fehlendes `owner`** → immer `team/<owner>` ergänzen.
* **Stub ohne `moved_to`** → Ziel angeben (meist `../_index.md`).
* **Zuviele Root‑Verzeichnisse** → echte Ordner prüfen; Stub‑Ordner zählen nicht; ggf. `legacy-planning/` nutzen.