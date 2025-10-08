# Lead Scoring Specification

**Erstellt:** 2025-10-08 (Sprint 2.1.6 Phase 4)
**Status:** ✅ Implementiert (PR #135)
**Owner:** Modul 02 - Neukundengewinnung

---

## 🎯 Übersicht

Das Lead-Scoring-System bewertet Leads nach 4 Faktoren mit einer Gesamtpunktzahl von **0-100 Punkten**.

**Backend-Service:** `LeadScoringService.java`
**Frontend-Komponente:** `LeadScoreIndicator.tsx`
**Migration:** V269 (`leads.lead_score INT CHECK (lead_score >= 0 AND lead_score <= 100)`)

---

## 🎨 Farbcodierung

Die Farbcodierung folgt dem **FreshFoodz Design System** (`/docs/planung/grundlagen/DESIGN_SYSTEM.md`).

| Score-Bereich | Farbe | Hex-Code | Kategorie | Bedeutung |
|---------------|-------|----------|-----------|-----------|
| **0-39** | Rot | `#f44336` | Schwache Leads | Geringes Potenzial, niedrige Priorität |
| **40-69** | Orange | `#ff9800` | Mittlere Leads | Moderates Potenzial, Standard-Priorität |
| **70-100** | Grün | `#94C456` | Top-Leads | Hohes Potenzial, höchste Priorität (**FreshFoodz CI**) |

**Referenz-Code:** `LeadScoreIndicator.tsx:42-46`
```typescript
const getScoreColor = (value: number): string => {
  if (value < 40) return '#f44336'; // Rot
  if (value < 70) return '#ff9800'; // Orange
  return '#94C456'; // FreshFoodz Grün
};
```

---

## 🧮 Berechnungslogik (4 Faktoren)

Jeder Faktor trägt **25%** zur Gesamtpunktzahl bei.

### **Faktor 1: Umsatzpotenzial (25%)**

**Sub-Faktoren:**
- **estimatedVolume (max 15 Punkte):**
  - ≥€50k → 15 Punkte
  - €25k-€49k → 10 Punkte
  - €10k-€24k → 5 Punkte
  - >€0 → 2 Punkte
  - NULL → 0 Punkte

- **employeeCount (max 10 Punkte):**
  - ≥25 Mitarbeiter → 10 Punkte
  - 10-24 Mitarbeiter → 6 Punkte
  - 5-9 Mitarbeiter → 3 Punkte
  - 1-4 Mitarbeiter → 1 Punkt
  - NULL → 0 Punkte

**Gesamt:** Min(25, volumeScore + employeeScore)

---

### **Faktor 2: Engagement (25%)**

**Sub-Faktoren:**
- **lastActivityAt (max 15 Punkte):**
  - <7 Tage → 15 Punkte (sehr aktiv)
  - <30 Tage → 10 Punkte (aktiv)
  - <90 Tage → 5 Punkte (mäßig aktiv)
  - ≥90 Tage / NULL → 0 Punkte (inaktiv)

- **followupCount (max 10 Punkte):**
  - ≥6 Follow-ups → 10 Punkte
  - 3-5 Follow-ups → 7 Punkte
  - 1-2 Follow-ups → 3 Punkte
  - 0 Follow-ups → 0 Punkte

**Gesamt:** Min(25, activityScore + followupScore)

---

### **Faktor 3: Fit (25%)**

**Sub-Faktoren:**
- **businessType (max 15 Punkte):**
  - GASTRONOMY, CATERER → 15 Punkte (Haupt-Zielgruppe)
  - RETAIL, WHOLESALE → 10 Punkte (Sekundär-Zielgruppe)
  - OTHER → 5 Punkte
  - NULL → 0 Punkte

- **kitchenSize (max 5 Punkte):**
  - LARGE → 5 Punkte
  - MEDIUM → 3 Punkte
  - SMALL → 1 Punkt
  - NULL → 0 Punkte

- **stage (max 5 Punkte):**
  - QUALIFIED, OPPORTUNITY, PROPOSAL → 5 Punkte
  - CONTACT, LEAD → 3 Punkte
  - REGISTERED → 1 Punkt
  - NULL → 0 Punkte

**Gesamt:** Min(25, businessTypeScore + kitchenSizeScore + stageScore)

---

### **Faktor 4: Dringlichkeit (25%)**

**Sub-Faktoren:**
- **progressDeadline Proximity (max 15 Punkte):**
  - <7 Tage bis Deadline → 15 Punkte (hoch dringlich)
  - 7-30 Tage → 10 Punkte (dringlich)
  - 30-60 Tage → 5 Punkte (moderat)
  - >60 Tage / NULL → 0 Punkte (nicht dringlich)

- **protectionUntil Proximity (max 10 Punkte):**
  - <30 Tage bis Expiry → 10 Punkte (Lead-Schutz läuft bald ab)
  - 30-60 Tage → 5 Punkte
  - >60 Tage / NULL → 0 Punkte

**Gesamt:** Min(25, progressScore + protectionScore)

---

## 📊 UI-Darstellung

### **LeadScoreIndicator Komponente**

**Elemente:**
- **Score-Zahl:** "75/100" in fetter Schrift
- **Progress-Bar:** Farbcodiert (rot/orange/grün)
- **Trend-Icon:**
  - ≥70 → TrendingUp (grün)
  - <40 → TrendingDown (rot)
  - 40-69 → TrendingFlat (orange)
- **Tooltip:** Breakdown der 4 Faktoren mit Einzelpunktzahlen

**Null-Handling:**
- `lead_score = NULL` → Zeigt "Noch nicht bewertet" statt Score

---

## 🔄 Aktualisierung

**Zeitpunkt:**
- **Initial:** Bei Lead-Erfassung (LeadCaptureService)
- **Automatisch:** Nightly Job (LeadMaintenanceService - geplant Sprint 2.1.7)
- **Manuell:** Admin-Action "Score neu berechnen" (geplant Sprint 2.1.7)

**Methode:**
```java
LeadScoringService.calculateScore(lead); // Updates lead.leadScore in-place
```

---

## ✅ Tests

**Backend:** `LeadScoringServiceTest.java` (19 Tests)
- Alle 4 Faktoren einzeln getestet
- Edge-Cases: NULL-Werte, Grenzwerte, Score-Capping (0-100)

**Frontend:** `LeadScoreIndicator.test.tsx` (geplant Sprint 2.1.7)

---

## 📚 Referenzen

- **ADR-006 Phase 2:** Lead Intelligence Features
- **Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md` (Farbpalette)
- **PR #135:** Sprint 2.1.6 Phase 4 Implementation
- **Backend Service:** `backend/src/main/java/de/freshplan/modules/leads/service/LeadScoringService.java`
- **Frontend Komponente:** `frontend/src/features/leads/LeadScoreIndicator.tsx`
