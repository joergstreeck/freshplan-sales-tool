# 🌐 API Endpoints - Sprint 2

**Sprint:** 2  
**Status:** 🆕 Zu implementieren  
**API Version:** v1  

---

## 📍 Navigation
**← Backend:** [Backend Requirements](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/BACKEND_REQUIREMENTS.md)  
**→ Entities:** [Entity Extensions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/ENTITY_EXTENSIONS.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 🎯 Neue Endpoints

### Customer Chain Structure
```http
PATCH /api/v1/customers/{id}/chain-structure
Content-Type: application/json

{
  "totalLocationsEU": 45,
  "locationsGermany": 28,
  "locationsAustria": 8,
  "locationsSwitzerland": 5,
  "locationsRestEU": 4
}
```

### Location Management
```http
# Hauptstandort erstellen/aktualisieren
PUT /api/v1/customers/{customerId}/main-location
Content-Type: application/json

{
  "locationName": "Hauptverwaltung",
  "street": "Beispielstraße",
  "houseNumber": "123",
  "postalCode": "10115",
  "city": "Berlin"
}

# Standorte abrufen
GET /api/v1/customers/{customerId}/locations
```

### Business Model & Pain Points
```http
PATCH /api/v1/customers/{id}/business-model
Content-Type: application/json

{
  "primaryFinancing": "public",
  "painPoints": [
    "staffingIssues",
    "qualityFluctuations",
    "foodWaste"
  ]
}
```

### Potential Calculation
```http
POST /api/v1/customers/{id}/calculate-potential
Content-Type: application/json

{
  "industry": "hotel",
  "serviceOfferings": {
    "breakfast": true,
    "breakfastWarm": true,
    "breakfastGuestsPerDay": 150
  }
}

Response:
{
  "monthlyPotential": 45000,
  "yearlyPotential": 540000,
  "quickWins": [
    {
      "title": "Warmes Frühstück optimieren",
      "potential": 15000,
      "product": "Cook&Fresh® Frühstück"
    }
  ]
}
```

---

## 🔒 Security

- Alle Endpoints erfordern Authentication
- Role-based Access Control
- Audit-Log für Änderungen

---

## 📊 Response Codes

- `200` - Success
- `201` - Created
- `400` - Validation Error
- `401` - Unauthorized
- `404` - Customer not found

---

**→ Weiter:** [Entity Extensions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/backend/ENTITY_EXTENSIONS.md)