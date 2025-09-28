---
module: "02_neukundengewinnung"
domain: "shared"
sprint: "2.1.3"
doc_type: "guideline"
status: "approved"
owner: "team/qa"
updated: "2025-09-28"
---

# QA-Checklist – Sprint 2.1.3

- [x] Name < 2 Zeichen → Inline-Fehler
- [x] Ungültige E-Mail → Inline-Fehler
- [x] Duplicate E-Mail → 409 Warning-Alert + Feldfehler
- [x] i18n de/en – Dialogtitel, Buttons, Empty State
- [x] Keine Dev-Konsolenfehler (MSW-Start sauber)
- [x] Lint & Prettier grün
- [x] Tests grün (CI)