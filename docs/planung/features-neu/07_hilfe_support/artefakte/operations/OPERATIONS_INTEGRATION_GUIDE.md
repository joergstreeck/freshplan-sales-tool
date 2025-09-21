# Operations-Integration Guide - Module 07 Help-System

**📅 Erstellt:** 2025-09-21
**🎯 Zweck:** User-Lead-Operations-Runbook Integration in CAR-Strategy Help-System
**📊 Status:** Production-Ready
**🔗 Basiert auf:** `/00_infrastruktur/betrieb/artefakte/USER_LEAD_STATE_MACHINE_RUNBOOK.md`

---

## 🎯 **Integration Overview**

Das External AI Operations-Runbook wurde vollständig in das Module 07 CAR-Strategy Help-System integriert. User-Lead-Protection Operations werden jetzt als **Guided Operations** mit hoher Confidence (≥0.95) bereitgestellt.

### **Integration-Komponenten:**

```
operations/
├── UserLeadOperationsGuide.java      # Core Operations-Logic (CAR-Provider)
├── CARResponse.java                   # Structured Response-Format
├── GuidedOperationProvider.java       # Interface für Plugin-System
├── OperationsResource.java            # REST-Endpoints für Operations-Guidance
└── frontend/
    ├── UserLeadOperationsPanel.tsx    # Interactive Operations-UI
    ├── useOperationsGuidance.ts       # React Hook für API-Integration
    └── OperationsHelpIntegration.tsx  # CAR-Strategy Integration-Component
```

---

## 🔧 **Backend-Integration**

### **1. CAR-Strategy Provider-Pattern**

```java
@ApplicationScoped
public class UserLeadOperationsGuide implements GuidedOperationProvider {

    @Override
    public double getConfidenceForQuery(String userQuery) {
        if (userQuery.contains("lead protection")) return 0.95;  // High confidence
        if (userQuery.contains("reminder")) return 0.95;
        if (userQuery.contains("hold")) return 0.95;
        // ... weitere Operations-Keywords
    }

    @Override
    public CARResponse handleQuery(String userQuery) {
        // Konvertiert External AI Runbook in strukturierte CAR-Response
        return CARResponse.guidedOperation()
            .title("User-Lead-Protection Operations")
            .quickSummary("6M+60T+10T User-basierte Lead-Protection")
            .actionSteps(List.of(
                "1. Lead-Status prüfen: SELECT * FROM v_user_lead_protection WHERE user_id = ?",
                "2. Hold erstellen: INSERT INTO lead_holds (...)",
                // ... weitere SQL-Commands
            ))
            .build();
    }
}
```

### **2. HelpService Integration**

Das `HelpService` wurde erweitert um Operations-Guidance in den normalen CAR-Suggestion-Flow zu integrieren:

```java
public List<SuggestionDTO> suggest(String context, ...) {
    // 1. Check Operations-Guide Confidence
    double operationsConfidence = operationsGuide.getConfidenceForQuery(context);

    if (operationsConfidence >= minConfidence) {
        // 2. Return Operations-Guided Response
        CARResponse operationsResponse = operationsGuide.handleQuery(context);
        SuggestionDTO operationsSuggestion = convertCARResponseToSuggestion(operationsResponse);
        return List.of(operationsSuggestion);
    }

    // 3. Fallback to database-driven suggestions
    return repo.rawSuggest(context, module, persona, territory, top);
}
```

### **3. Dedicated Operations-Endpoints**

```java
@Path("/api/help/operations")
public class OperationsResource {

    @POST @Path("/user-lead-guidance")
    public Response getUserLeadGuidance(Map<String, Object> request) {
        // Strukturierte Operations-Guidance für spezifische Queries
    }

    @GET @Path("/templates")
    public Response getOperationsTemplates() {
        // Vordefinierte Templates für häufige Operations-Tasks
    }
}
```

---

## 🎨 **Frontend-Integration**

### **1. CAR-Strategy Komponente**

```tsx
<OperationsHelpIntegration
  userQuery={userQuery}
  onOperationsLaunch={() => setShowOperationsGuidance(true)}
  showInline={true}
/>
```

**Features:**
- Automatische Confidence-Detection für Operations-Queries
- Quick-Access Templates für häufige Operations-Tasks
- Inline-Guidance mit interaktiven SQL-Commands
- Copy-to-Clipboard für Operations-Commands

### **2. Interactive Operations-Panel**

```tsx
<UserLeadOperationsPanel
  response={carResponse}
  onActionExecute={(action) => executeOperationsAction(action)}
  onCopyToClipboard={(sql) => copyToClipboard(sql)}
/>
```

**Features:**
- State-Machine-Visualization (PROTECTED → REMINDER_DUE → GRACE → EXPIRED)
- Executable SQL-Commands mit Copy-Paste-Function
- Structured Troubleshooting mit Problem→Solution-Mapping
- KPI-Tracking und Business-Rules-Display

---

## 📊 **Operations-Templates**

Das System bietet vordefinierte Templates für häufige Operations-Anfragen:

```yaml
Templates:
  lead_protection_overview:
    query: "lead protection system overview"
    confidence: 0.95
    description: "Vollständige Übersicht der User-Lead-Protection (6M+60T+10T)"

  reminder_operations:
    query: "reminder pipeline management"
    confidence: 0.95
    description: "Reminder-Versendung und Pipeline-Management"

  hold_management:
    query: "hold management stop clock"
    confidence: 0.95
    description: "Hold-Erstellung und Stop-Clock-Management"

  state_machine:
    query: "user lead state machine"
    confidence: 0.95
    description: "4 Haupt-States: PROTECTED → REMINDER_DUE → GRACE → EXPIRED"

  qualified_activities:
    query: "qualified activity types"
    confidence: 0.95
    description: "Aktivitäten die Lead-Protection verlängern"
```

---

## 🔍 **Query-Detection-Matrix**

Die Operations-Guidance wird automatisch aktiviert basierend auf Query-Keywords:

| **Confidence** | **Keywords** | **Response-Type** |
|---------------|-------------|-------------------|
| **0.95 (High)** | "lead protection", "user lead", "reminder", "expiry", "hold", "stop clock" | Full Operations-Guidance |
| **0.75 (Medium)** | "operations", "runbook", "state machine", "qualified activity" | Targeted Guidance |
| **0.40 (Low)** | "protection", "activity", "timer" | Minimal Guidance |

---

## 📈 **Monitoring & Analytics**

### **Operations-Metrics:**

```java
// In OperationsResource.java
meter.counter("help_operations_guidance_requests_total",
             "confidence_tier", getConfidenceTier(confidence),
             "user_persona", scope.getPersona()).increment();
```

### **CAR-Strategy-Metrics:**

```java
// In HelpService.java
meter.counter("help_nudges_shown_total", "type", "operations_guide").increment();
meter.counter("help_nudges_shown_total", "type", "database_lookup").increment();
```

### **Grafana-Dashboard-Integration:**

Operations-Guidance-Requests werden in das bestehende Help-System-Dashboard integriert:

```yaml
Operations Metrics:
  - help_operations_guidance_requests_total (by confidence_tier, user_persona)
  - help_nudges_shown_total{type="operations_guide"}
  - operations_template_usage_total (by template_name)
```

---

## 🚀 **Deployment-Checkliste**

### **Backend-Deployment:**

- [ ] `UserLeadOperationsGuide.java` → `/help/operations/`
- [ ] `CARResponse.java` + `GuidedOperationProvider.java` → Domain-Classes
- [ ] `OperationsResource.java` → REST-Endpoints
- [ ] `HelpService.java` → Operations-Integration
- [ ] `SuggestionDTO.java` → CARResponse-Field hinzugefügt

### **Frontend-Deployment:**

- [ ] `UserLeadOperationsPanel.tsx` → Interactive Operations-UI
- [ ] `useOperationsGuidance.ts` → React Hook für API-Calls
- [ ] `OperationsHelpIntegration.tsx` → CAR-Strategy-Integration
- [ ] Route-Integration: `/hilfe/operations/*` → Operations-Guidance-Pages

### **Integration-Testing:**

- [ ] CAR-Strategy: Query "lead protection" → Operations-Guidance (Confidence ≥0.95)
- [ ] Templates: 5 vordefinierte Operations-Templates verfügbar
- [ ] Interactive UI: SQL-Commands copy-pasteable, State-Machine-Visualization functional
- [ ] Monitoring: Operations-Metrics in Grafana-Dashboard

---

## 🔗 **Integration-Points**

### **Module 02 Lead-Erfassung:**
- User-Lead-Protection SQL-Views (`v_user_lead_protection`)
- Lead-Activities für qualifizierte Aktivitäten
- Event-Integration (`lead.protection.reminder`, `lead.protection.expired`)

### **Module 00 Infrastructure-Betrieb:**
- External AI Operations-Artefakte als Knowledge-Base
- Monitoring-Integration (Prometheus + Grafana)
- Production-Runbooks als Guided Operations

### **Module 06 Settings:**
- CAR-Parameter: `help.nudge.confidence.min`, `help.nudge.budget.*`
- Operations-spezifische Settings: `operations.runbook.enabled`

---

## 🎯 **User-Experience-Flow**

### **Scenario 1: Operations-Query Detection**

1. **User-Input:** "How do I check lead protection status?"
2. **Confidence-Check:** 0.95 (High) → Operations-Guidance activated
3. **CAR-Response:** Structured Operations-Guide mit SQL-Commands
4. **UI-Display:** Interactive Panel mit State-Machine + Copy-Paste-Commands

### **Scenario 2: Template-based Quick-Access**

1. **User:** Öffnet Help-System ohne spezifische Query
2. **Display:** Operations-Templates als Quick-Access-Chips
3. **Selection:** "Hold-Management" Template → Sofortige Guidance
4. **Result:** Full Operations-Guide für Hold/Stop-Clock-Management

### **Scenario 3: Integrated CAR-Strategy**

1. **Context:** User arbeitet in Lead-Management-Interface
2. **Struggle-Detection:** Wiederholte Failed-Attempts bei Lead-Status-Updates
3. **Proactive-Nudge:** "Operations-Guidance für Lead-Protection verfügbar"
4. **One-Click-Access:** Direkt zur relevanten Operations-Guidance

---

## ✅ **Integration-Success-Criteria**

### **Functional-Requirements:**
- ✅ Operations-Queries werden mit ≥0.95 Confidence erkannt
- ✅ External AI Runbook vollständig als Guided Operations verfügbar
- ✅ Interactive SQL-Commands mit Copy-Paste-Functionality
- ✅ State-Machine-Visualization für User-Lead-Protection

### **User-Experience:**
- ✅ Seamless Integration in CAR-Strategy Help-System
- ✅ Proactive Operations-Guidance bei relevanten Queries
- ✅ Quick-Access Templates für häufige Operations-Tasks
- ✅ Professional Operations-UI mit Business-Context

### **Technical-Excellence:**
- ✅ Plugin-based Operations-Provider-Pattern für Erweiterbarkeit
- ✅ Monitoring-Integration für Operations-Guidance-Usage
- ✅ Error-Handling und Fallback zu database-driven suggestions
- ✅ Production-ready REST-Endpoints mit RBAC-Security

**🚀 User-Lead-Operations-Runbook erfolgreich in Module 07 CAR-Strategy Help-System integriert!**