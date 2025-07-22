# 🤖 Claude-Optimierte Dokumentations-Struktur

**Zweck:** Dokumentation so strukturieren, dass Claude effektiv arbeiten kann ohne Kontext zu verlieren

## 🎯 Die Kern-Dokumente für Claude

### 1️⃣ **Master Plan** - Der Kompass
```markdown
# CRM_COMPLETE_MASTER_PLAN_V5.md

## 🎯 Aktuelle Arbeitsphase
**Modul:** M4 - Opportunity Pipeline
**Status:** Backend-Entwicklung Tag 2/4.5
**Arbeits-Dokument:** ./features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md ⭐
**API-Spec:** ./features/M4_opportunity_pipeline/api_spec.yaml
**Test-Szenarios:** ./features/M4_opportunity_pipeline/test_scenarios.md

## 📚 Context-Dokumente für M4
- Business Rules: ./features/M4_opportunity_pipeline/business_rules.md
- Integration Points: ./features/M4_opportunity_pipeline/integrations.md
- Security Concept: ./features/M4_opportunity_pipeline/security.md

## 🔗 Abhängigkeiten
- M8 (Calculator) wartet auf M4 completion
- M5 (Customer) kann parallel starten
```

### 2️⃣ **Übergabe-Integration**
```markdown
# In jeder HANDOVER.md

## 📍 CLAUDE QUICK CONTEXT
**Master Plan:** docs/CRM_COMPLETE_MASTER_PLAN_V5.md (Zeilen 120-150) ⭐
**Aktives Modul:** M4 - docs/features/2025-07-12_TECH_CONCEPT_M4-opportunity-pipeline.md
**Letzte Datei:** backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java
**Nächster TODO:** Stage-Validierung implementieren (Zeile 87)

## 🧭 Navigation für Claude
1. Master Plan → Aktuelle Phase → Link zum Tech-Konzept
2. Tech-Konzept → Implementation Checklist → Aktueller Punkt
3. Bei Fragen → business_rules.md oder integration.md
```

### 3️⃣ **Tech-Konzept mit Claude-Hints**
```markdown
# Tech-Konzept M4

## 🤖 Claude Implementation Guide

### Backend (Tag 1-2)
```bash
# Claude: Start hier
cd backend/src/main/java/de/freshplan/domain/opportunity

# 1. Entity (DONE ✅)
cat entity/Opportunity.java

# 2. Repository (CURRENT 👈)
vim repository/OpportunityRepository.java
# TODO: Implementiere findByStageAndAssignedTo()

# 3. Service (NEXT)
vim service/OpportunityService.java
# TODO: Stage-Validierung mit BusinessRules
```

### Frontend (Tag 3-4)
```bash
# Claude: Komponenten-Struktur
cd frontend/src/features/opportunity
tree -L 2

# Start mit Pipeline-Container
vim components/OpportunityPipeline.tsx
```
```

## 🔄 Optimaler Arbeitsfluss für Claude

### Session-Start:
1. **Übergabe lesen** → Quick Context Section
2. **Master Plan** → Nur die markierte Aktuelle Phase
3. **Tech-Konzept** → Implementation Guide
4. **Letzte Datei** → Continue where left off

### Während der Arbeit:
```bash
# Claude's Mental Model
CURRENT_MODULE="M4"
CURRENT_FILE="OpportunityRepository.java"
NEXT_TODO="findByStageAndAssignedTo()"

# Bei Unsicherheit
cat $MASTER_PLAN | grep -A 10 "Aktuelle Arbeitsphase"
cat $TECH_KONZEPT | grep -A 20 "Claude Implementation Guide"
```

### Context-Bewahrung:
```markdown
## In jedem Dokument: Claude-Breadcrumbs
<!-- 
CLAUDE-NAV: Master Plan > M4 Pipeline > Backend > Repository
NEXT: Service Layer mit Stage-Validierung
DEPENDENCY: business_rules.md für erlaubte Stage-Übergänge
-->
```

## 📋 Dokumenten-Template für neue Features

```markdown
# FC-XXX: [Feature Name]

## 🤖 Claude Quick Start
**Geschätzter Aufwand:** X Tage
**Abhängigkeiten:** [Module]
**Start-Punkt:** backend/src/...

## 📍 Implementation Checkpoint
- [ ] Backend Entity ← YOU ARE HERE
- [ ] Repository 
- [ ] Service Layer
- [ ] REST Endpoints
- [ ] Frontend Components
- [ ] Integration Tests

## 🧭 Claude Navigation
Bei Fragen zu:
- Business Logic → ./business_rules.md
- API Design → ./api_spec.yaml
- Security → ./security_concept.md
```

## 🚨 Kritische Regeln für Dokumente

1. **Keine Redundanz** - Jede Info nur an EINEM Ort
2. **Klare Verlinkung** - Immer mit relativen Pfaden
3. **Status-Markierungen** - ✅ DONE, 👈 CURRENT, ⏳ NEXT
4. **Code-Pfade** - Immer absolute Pfade vom Projekt-Root
5. **Zeilennummern** - Bei spezifischen TODOs

## 💡 Beispiel: Optimale Übergabe

```markdown
# STANDARDÜBERGABE - 12.07.2025 14:30

## 📍 CLAUDE QUICK CONTEXT
**Du warst hier:** OpportunityRepository.java:45 (findByStage implementiert)
**Nächster Schritt:** OpportunityService.java - validateStageTransition() 
**Regel nachschlagen:** business_rules.md:23-45 (Stage Transition Matrix)

## 🎯 Heutiges Ziel
M4 Backend fertigstellen (noch 3 von 5 Komponenten)

## 🔗 Arbeits-Dokumente
1. Master Plan: Zeilen 156-178 (Aktuelle Phase)
2. Tech-Konzept: ./features/2025-07-12_TECH_CONCEPT_M4 (Implementation Guide)
3. Business Rules: ./features/M4_opportunity_pipeline/business_rules.md

## ⚡ Quick Commands
```bash
# Direkt zur Arbeit
cd backend/src/main/java/de/freshplan/domain/opportunity/service
vim OpportunityService.java +87

# Tests ausführen  
./mvnw test -Dtest=OpportunityServiceTest

# Business Rules checken
cat ../../features/M4_opportunity_pipeline/business_rules.md | grep -A 20 "Stage Transitions"
```
```

Diese Struktur stellt sicher, dass:
- Ich immer weiß, wo ich bin
- Ich schnell die richtigen Dokumente finde
- Ich den Kontext zwischen Sessions behalte
- Die Übergabe alle Links enthält, die ich brauche