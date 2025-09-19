# Technical Concept – Module 01 Cockpit (Foundation Core)
Stand: 2025-09-19

## Ziel
Multi-Channel Sales Dashboard (DIRECT + PARTNER) mit ROI-Kalkulator, ABAC (Territory + Channel), Theme V2.

## Architektur (monolithisch, de.freshplan.*)
- API (Quarkus): /api/cockpit/* – Summary, Filters, ROI Calc, Channels
- Services: CockpitService (wrapt bestehende Aggregationen), ROIService (Formel-basiert)
- ABAC: JWT Claims **territories[]**, **channels[]**, optional chain_id; **kein** Fallback in Prod
- Frontend: React + MUI Theme V2 (Tokens), MultiChannelDashboard, ROICalculator

## Performance & Standards
- P95 < 200 ms für /api/cockpit/*
- RFC7807 Error-Handling
- Named Parameters in Queries
- DTOs/Validation in allen Endpoints
- Design System V2 – keine Hex-Codes

## Roadmap (Phase 2 – Advanced)
- SQL-Projektionen & RLS Policies (territory) für KPIs
- BDD-Test-Suite + CI + k6
- WebSocket Badges + Replay
- Enge Integration mit Modulen 02/03 (Samples/Activities)
