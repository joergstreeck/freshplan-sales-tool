---
module: "02_neukundengewinnung"
doc_type: "guideline"
status: "draft"
owner: "team/architecture"
updated: "2025-09-27"
---

# Foundation Standards Compliance – Modul 02
Stand: 2025-09-19

**Ziel:** 92%+ Compliance, identisch zu Modul 04.

## Erfüllte Punkte
- Design System V2: Tokens + MUI Theme (keine Hardcodes)
- API Standards: JavaDoc mit /grundlagen References, RFC7807 Fehler
- Security: ABAC Scope Filter + Named Parameters in Queries
- Testing: BDD-Tests (LeadResourceTest, EmailServiceTest, CampaignDashboard, E2E)
- Performance: k6 Script, Budget (P95 < 200ms), Batch-Targets
- Real-time: WS Topics für Leads/Email/Campaign
- Universal Export: JSONL + klassische Formate
- SmartLayout: Lead-Form & Campaign-Dashboard
- B2B-Food: Seasonal Window + Lead Scoring

## Offene Punkte
- JWT-Parsing im SecurityScopeFilter konkretisieren (abhängig von eurer Auth)
- Repo-Implementierungen (LeadRepository) verdrahten
- CI: Coverage Gate 80% in Maven/Jacoco & Jest konfigurieren
