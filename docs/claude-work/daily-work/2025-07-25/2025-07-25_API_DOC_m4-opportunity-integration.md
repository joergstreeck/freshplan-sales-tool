# M4 Opportunity API Integration Documentation

**Datum:** 25.07.2025  
**Autor:** Claude  
**Feature:** FC-002-M4 Opportunity Pipeline  
**Status:** ✅ Integration abgeschlossen

## 🎯 Übersicht

Die M4 Opportunity Pipeline API-Integration verbindet das React Frontend mit dem Quarkus Backend, um eine vollständige Drag & Drop Pipeline-Verwaltung zu ermöglichen.

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/opportunities
```

### CRUD Operations

#### 1. Get All Opportunities
```http
GET /api/opportunities?page=0&size=20
```

**Query Parameters:**
- `page` (optional): Seitennummer (default: 0)
- `size` (optional): Anzahl pro Seite (default: 20)
- `assignedToId` (optional): Filter nach Benutzer
- `customerId` (optional): Filter nach Kunde
- `stage` (optional): Filter nach Stage
- `valueMin` (optional): Mindestwert
- `valueMax` (optional): Maximalwert
- `expectedCloseDateFrom` (optional): Von-Datum
- `expectedCloseDateTo` (optional): Bis-Datum

**Response:**
```json
[
  {
    "id": "4ad7d844-e1b4-4a6e-bc77-9938b465258d",
    "name": "Q1 Zielauftrag: Restaurant-Modernisierung",
    "description": "Wichtig für Q1 Zielerreichung",
    "stage": "NEEDS_ANALYSIS",
    "stageDisplayName": "Bedarfsanalyse",
    "stageColor": "#009688",
    "customerId": "97b80502-f94c-439e-bba7-d079b4dda1a6",
    "customerName": "Event Catering München GmbH",
    "assignedToId": "67f3f09d-f890-4b89-b883-d783c9eb9177",
    "assignedToName": "testuser",
    "expectedValue": 32000.00,
    "expectedCloseDate": "2025-08-19",
    "probability": 40,
    "createdAt": "2025-07-25T14:07:04.974822",
    "updatedAt": "2025-07-25T14:07:04.974822"
  }
]
```

#### 2. Get Single Opportunity
```http
GET /api/opportunities/{id}
```

#### 3. Create Opportunity
```http
POST /api/opportunities
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Neue Verkaufschance",
  "description": "Beschreibung",
  "customerId": "97b80502-f94c-439e-bba7-d079b4dda1a6",
  "assignedToId": "67f3f09d-f890-4b89-b883-d783c9eb9177",
  "expectedValue": 50000.00,
  "expectedCloseDate": "2025-09-30",
  "stage": "NEW_LEAD",
  "probability": 20
}
```

#### 4. Update Opportunity
```http
PUT /api/opportunities/{id}
Content-Type: application/json
```

**Request Body:** Gleiche Struktur wie Create, alle Felder optional

#### 5. Delete Opportunity
```http
DELETE /api/opportunities/{id}
```

**Status:** ⚠️ Noch nicht implementiert (Soft-Delete geplant)

### Stage Management

#### Change Stage (für Drag & Drop)
```http
PUT /api/opportunities/{id}/stage/{stage}?reason={reason}
```

**Path Parameters:**
- `id`: Opportunity ID
- `stage`: Neue Stage (siehe Stage Enum)

**Query Parameters:**
- `reason` (optional): Grund für Stage-Änderung

**Beispiel:**
```http
PUT /api/opportunities/4ad7d844-e1b4-4a6e-bc77-9938b465258d/stage/PROPOSAL?reason=Test%20drag%20and%20drop
```

**Response:** Aktualisierte Opportunity

### Pipeline Analytics

#### Pipeline Overview
```http
GET /api/opportunities/pipeline/overview
```

**Response:**
```json
{
  "totalOpportunities": 31,
  "totalValue": 1455000.00,
  "averageValue": 46935.48,
  "stageDistribution": {
    "NEW_LEAD": { "count": 7, "value": 259000.00 },
    "QUALIFICATION": { "count": 6, "value": 287000.00 },
    "NEEDS_ANALYSIS": { "count": 6, "value": 262000.00 },
    "PROPOSAL": { "count": 6, "value": 323000.00 },
    "NEGOTIATION": { "count": 6, "value": 324000.00 }
  },
  "conversionRates": {
    "LEAD_TO_QUALIFIED": 85.71,
    "QUALIFIED_TO_PROPOSAL": 100.0,
    "PROPOSAL_TO_NEGOTIATION": 100.0,
    "NEGOTIATION_TO_CLOSED": 0.0
  }
}
```

#### Get Opportunities by Stage
```http
GET /api/opportunities/stage/{stage}
```

#### Get Opportunities by User
```http
GET /api/opportunities/assigned/{userId}
```

### Activity Management

#### Add Activity
```http
POST /api/opportunities/{id}/activities?type={type}&title={title}&description={description}
```

**Status:** ⚠️ Noch nicht implementiert

## 🔄 Stage Enum Mapping

### Backend Stages
```java
public enum OpportunityStage {
    NEW_LEAD,
    QUALIFICATION,
    NEEDS_ANALYSIS,
    PROPOSAL,
    NEGOTIATION,
    CLOSED_WON,
    CLOSED_LOST
}
```

### Frontend Mapping
```typescript
export enum OpportunityStage {
  LEAD = "NEW_LEAD",
  QUALIFIED = "QUALIFICATION", 
  NEEDS_ANALYSIS = "NEEDS_ANALYSIS",
  PROPOSAL = "PROPOSAL",
  NEGOTIATION = "NEGOTIATION",
  CLOSED_WON = "CLOSED_WON",
  CLOSED_LOST = "CLOSED_LOST"
}
```

## 🚀 Frontend Integration

### React Query Hooks

```typescript
// Alle Opportunities laden
const { data, isLoading, error } = useOpportunities();

// Stage ändern (Drag & Drop)
const mutation = useChangeOpportunityStage();
mutation.mutate({
  id: opportunityId,
  request: {
    newStage: OpportunityStage.PROPOSAL,
    reason: "Stage changed via drag & drop"
  }
});
```

### Type Mapping
```typescript
// Backend IOpportunity → Frontend Opportunity
function mapBackendToFrontend(backendOpportunity: IOpportunity): Opportunity {
  return {
    id: backendOpportunity.id,
    name: backendOpportunity.name,
    stage: backendOpportunity.stage,
    value: backendOpportunity.value || backendOpportunity.expectedValue,
    // ... weitere Felder
  };
}
```

## 🧪 Testing

### API Test mit cURL
```bash
# Alle Opportunities abrufen
curl http://localhost:8080/api/opportunities

# Stage ändern
curl -X PUT "http://localhost:8080/api/opportunities/{id}/stage/PROPOSAL?reason=Test"
```

### Frontend Tests
- ✅ KanbanBoard.test.tsx: 14/14 Tests grün
- ✅ OpportunityCard.test.tsx: 16/17 Tests grün
- ✅ Integration mit QueryClient Provider

## 🔒 Security

- **Authentication:** Temporär deaktiviert für Development
- **Roles:** `@RolesAllowed({"admin", "manager", "sales"})` vorbereitet
- **CORS:** Konfiguriert für localhost:5173

## 📊 Performance

- **Pagination:** Standard 20 Items pro Seite
- **React Query Cache:** 5 Minuten staleTime
- **Optimistic Updates:** UI reagiert sofort
- **Lazy Loading:** Nur sichtbare Stages werden gerendert

## 🐛 Bekannte Limitierungen

1. **Soft Delete:** Noch nicht implementiert
2. **Activity Management:** Placeholder vorhanden
3. **Bulk Operations:** Nicht unterstützt
4. **WebSocket Updates:** Geplant für Echtzeit-Sync

## 🔮 Nächste Schritte

1. **TODO-64:** RENEWAL Stage hinzufügen
2. **TODO-61:** Optimistische Updates verfeinern
3. **TODO-62:** Error-Handling für fehlgeschlagene Stage-Wechsel
4. **WebSocket:** Echtzeit-Updates zwischen Clients