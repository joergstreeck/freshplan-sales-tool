---
module: "03_kundenmanagement"
doc_type: "guideline"
status: "draft"
owner: "team/architecture"
updated: "2025-09-27"
---

# WebSocket Topics – Module 03 (Kundenmanagement)
- customer.activity.updated      { customerId, activityId, kind, occurredAt }
- customer.sample.status.changed { customerId, sampleId, oldStatus, newStatus, eventAt }
- customer.opportunity.changed   { customerId, opportunityId, stage, roiValueEstimate }
