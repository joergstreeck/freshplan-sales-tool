# Technical Concept – Module 03 Kundenmanagement
Stand: 2025-09-19

## Architektur (monolithisch, bewusst)
- REST-APIs, Services, Repositories in einem Monolithen (de.freshplan.*)
- ABAC (JWT Claims) + RLS (DB) für Territory-Scoping
- Design System V2 global (Tokens, Theme), WCAG 2.1 AA Focus Styles

## B2B-Convenience-Food Spezifika
- Cook&Fresh® Samples (Sample-Box Tracking)
- ROI-basierte Opportunities (labor_savings, food_waste_reduction, fit_score)
- Gastronomie-Typen: HOTEL, RESTAURANT, BETRIEBSGASTRONOMIE, CATERING

## Observability & Performance
- P95 < 200 ms für /api/customers/*, /api/samples/*, /api/activities/*
- Index-Strategie in SQL, Snapshots optional
- WS Events: Badges live, KPIs via Polling falls Bus down

## Security
- JWT Parsing (SmallRye), Claims: territories[], chain_id
- Dev-Fallback: Header (X-Territories), in Prod deaktivieren
- RLS Policies in allen Kern-Tabellen
