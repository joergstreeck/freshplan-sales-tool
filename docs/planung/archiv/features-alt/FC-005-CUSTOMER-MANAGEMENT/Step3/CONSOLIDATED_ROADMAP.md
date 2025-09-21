# 🗺️ Konsolidierte Step 3 Roadmap - Single Source of Truth

**Erstellt:** 01.08.2025  
**Status:** ✅ Offiziell und verbindlich  
**Basis:** Team-Feedback + Business Requirements  

## 🎯 Verbindliche Feature-Priorisierung

Diese Roadmap ist die **einzige gültige Quelle** für die Step 3 Priorisierung. Sie basiert auf dem dokumentierten Team-Feedback und den geschäftlichen Anforderungen.

## 📊 Sprint-Planung Übersicht

### Sprint 2 (Aktuell) - Foundation
**Ziel:** Basis-Funktionalität für Multi-Contact Management

| Feature | Status | Begründung |
|---------|--------|------------|
| Contact CRUD Operations | **Must-Have** | Kernfunktionalität |
| Multi-Contact per Customer | **Must-Have** | Basis-Anforderung |
| Primary Contact Flag | **Must-Have** | Business-kritisch |
| Location Assignment (Multi) | **Must-Have** | Filialstruktur-Support |
| Basic Audit Trail | **Must-Have** | Compliance-Grundlage |

### Sprint 3 - Intelligence & Compliance
**Ziel:** Intelligente Features und DSGVO-Compliance

| Feature | Priority | Team-Feedback | Business Value |
|---------|----------|---------------|----------------|
| DSGVO Consent Management | **HIGH** | "Kritisch wichtig und zeitgemäß" | Rechtliche Anforderung, Wettbewerbsfähigkeit |
| Relationship Warmth | **HIGH** | "Genial! Echter Vertriebs-Vorsprung" | Proaktiver Vertrieb, KPI-Messung |
| Contact Timeline | **MEDIUM** | "Gold wert für Geschäftsanalysen" | Transparenz, Nachvollziehbarkeit |
| Smart Suggestions | **MEDIUM** | "Gamechanger für Adoption" | User Experience, Effizienz |

### Sprint 4+ - Enhanced Features & Kritische Erfolgsfaktoren
**Ziel:** Erweiterte Funktionen und wichtige Unterstützungs-Features

#### 🔴 KRITISCHE ERFOLGSFAKTOREN - MUSS VOR INTELLIGENCE FEATURES!

**⚠️ WICHTIG:** Diese Features sind VORAUSSETZUNGEN für erfolgreiche Intelligence-Features!

| Feature | Priority | Status | Implementierung | Kernaspekte | Dokument |
|---------|----------|--------|-----------------|-------------|----------|
| **Data Strategy Intelligence** | **CRITICAL** | 📋 TODO | VOR Warmth Score! | • 4-stufiges Freshness-System<br>• Wöchentliche Update-Kampagnen<br>• Data Quality Dashboard | [→ Data Strategy](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/DATA_STRATEGY_INTELLIGENCE.md) |
| **Offline Conflict Resolution** | **HIGH** | 📋 TODO | Mit Mobile Features | • Bulk-Konfliktlösung (5+)<br>• Intelligente Merge-Strategien<br>• Preview mit Kontrolle | [→ Conflict Resolution](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/OFFLINE_CONFLICT_RESOLUTION.md) |
| **Cost Management Services** | **HIGH** | 📋 TODO | VOR AI Features | • "Sparmodus" statt "Limit"<br>• Gamification<br>• Transparenz-Dashboard | [→ Cost Management](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COST_MANAGEMENT_EXTERNAL_SERVICES.md) |
| **In-App Help System** | **HIGH** | 📋 TODO | Parallel zu Features | • Frustrations-Erkennung<br>• 4 Struggle-Patterns<br>• Adaptive Intensität | [→ In-App Help](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/IN_APP_HELP_SYSTEM.md) |
| **Feature Adoption Tracking** | **HIGH** | 📋 TODO | Ab Sprint 3 | • Stakeholder-Dashboards<br>• Office Live-Feed<br>• Rollen-basierte Views | [→ Adoption Tracking](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FEATURE_ADOPTION_TRACKING.md) |

#### Nice-to-Have Features
| Feature | Priority | Implementierung | Begründung |
|---------|----------|-----------------|------------|
| Offline Mobile Support | **MEDIUM** | Bei Mobile-First Initiative | "Top-UX für Außendienst/Messe" |
| Birthday Reminders | **LOW** | Quick Win möglich | Beziehungspflege |
| Advanced Analytics | **LOW** | Nach Datensammlung | Strategische Insights |
| AI Features | **LOW** | Zukunft | Erst Basis-Intelligenz etablieren |

## 🚫 Out of Scope (Bewusste Entscheidungen)

| Ausgeschlossen | Grund | Alternative |
|----------------|-------|-------------|
| Event Sourcing | Overhead bei 65-110 Events/Tag | Pragmatischer CRUD + Audit |
| Complex AI | Zu früh, zu komplex | Regelbasierte Intelligenz |
| Real-time Sync | Nicht erforderlich | Standard REST API |

## 📈 Metriken für Priorisierung

Die Priorisierung basiert auf:

1. **Team-Feedback Score** (Wie begeistert ist das Team?)
2. **Business Impact** (ROI, Compliance, Wettbewerbsvorteil)
3. **User Adoption** (Wird es genutzt werden?)
4. **Technical Effort** (Aufwand vs. Nutzen)

### Beispiel: DSGVO Consent Management
- Team-Feedback: ⭐⭐⭐⭐⭐ "Kritisch wichtig"
- Business Impact: ⭐⭐⭐⭐⭐ Rechtlich notwendig
- User Adoption: ⭐⭐⭐⭐ Muss genutzt werden
- Technical Effort: ⭐⭐⭐ Mittel
- **→ Ergebnis: HIGH Priority für Sprint 3**

## 🎯 Key Takeaways

1. **DSGVO ist KEIN "nice-to-have"** - Das Team hat klar kommuniziert, dass es kritisch ist
2. **Relationship Warmth ist ein USP** - "Echter Vertriebs-Vorsprung" rechtfertigt hohe Priorität
3. **Mobile-First ist wichtig** - Aber Offline-Support kann schrittweise kommen
4. **Pragmatismus siegt** - CRUD statt Event Sourcing bei unserem Volumen

## 📊 IMPLEMENTIERUNGS-FORTSCHRITT

### Abgeschlossene Komponenten ✅
- CustomerContact Entity (vollständig mit allen Feldern)
- ContactRole Entity (Rollen-System)
- Database Schema (customer_contacts Tabelle)
- Basis CRUD Operationen
- Audit Trail (created/updated Fields)

### In Arbeit ⏳
- ContactInteraction Entity (für Timeline)
- V201 Migration (contact_interactions Tabelle)
- InteractionTrackingService
- RelationshipWarmthService

### Nächste Schritte 📋
1. ContactInteraction Entity fertigstellen
2. Migration V201 ausführen
3. Services implementieren
4. Frontend ContactStore erstellen
5. UI Components entwickeln

## 📝 Änderungsmanagement

Diese Roadmap kann nur durch:
- Dokumentierte Team-Entscheidung
- Business-Requirement Änderung
- Technische Blocker

geändert werden. Alle Änderungen müssen in diesem Dokument reflektiert werden.

## 🔗 WICHTIGE LINKS FÜR CLAUDE

### Implementierungs-Guides
- **Backend Start:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_INTELLIGENCE.md`
- **Frontend Start:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md`
- **Testing Plan:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md`

### Code-Locations
- **Backend Entity:** `backend/src/main/java/de/freshplan/domain/customer/entity/CustomerContact.java`
- **Intelligence Domain:** `backend/src/main/java/de/freshplan/domain/intelligence/`
- **Migrations:** `backend/src/main/resources/db/migration/`

---

**Diese Roadmap ersetzt alle anderen Priorisierungen in anderen Dokumenten!**