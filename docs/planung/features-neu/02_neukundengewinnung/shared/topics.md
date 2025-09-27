---
module: "02_neukundengewinnung"
doc_type: "guideline"
status: "draft"
owner: "team/architecture"
updated: "2025-09-27"
---

# WebSocket Topics – Modul 02 (Foundation V2)
- leads.created            { leadId, name, territory, createdAt }
- email.thread.updated     { threadId, change, classification }
- campaign.performance     { campaignId, metric, delta, value }
