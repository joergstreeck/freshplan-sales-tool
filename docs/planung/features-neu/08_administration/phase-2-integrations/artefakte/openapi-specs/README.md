# OpenAPI 3.1 Specifications

**Zweck:** Foundation-compliant REST API Documentation für Phase 2

## API Documentation Files

### `lead-protection.yaml`
**Endpoints:**
- `GET /api/leads/{id}/protection` - Lead Protection Status
- `POST /api/leads/{id}/holds` - Stop-the-Clock Management
- `DELETE /api/leads/{id}/holds/{holdId}` - Hold Release

**Features:**
- ABAC Security Integration
- RFC7807 Problem Details
- Named Parameters + Components
- Comprehensive Response Codes

### `lead-collaboration.yaml`
**Endpoints:**
- `GET /api/leads/{id}/collaborators` - Multi-Contact Management
- `POST /api/leads/{id}/collaborators` - Add Collaborator
- `DELETE /api/leads/{id}/collaborators/{userId}` - Remove Access

**Features:**
- Role-based Access Control (OWNER, VIEW, ASSIST, NEGOTIATE)
- Lead Protection Integration
- Audit Trail Support

### `sample-management.yaml`
**Endpoints:**
- `POST /api/sample-boxes` - Test Configuration
- `GET /api/test-phases/{id}/feedback` - Feedback Collection
- `POST /api/feedback/{token}` - Public Customer Portal

**Features:**
- Token-based Public Access
- Multi-source Feedback (EMAIL/FORM/PHONE/VISIT)
- Automatic Activity Generation

## Foundation Standards Compliance

### OpenAPI 3.1 Features
```yaml
openapi: 3.1.0
info:
  title: FreshPlan Lead Protection API
  version: 1.0.0
  description: |
    Lead Protection System für Handelsvertretervertrag Compliance

    ## Authentication
    Bearer Token required for all endpoints except public feedback

    ## Error Handling
    All errors follow RFC7807 Problem Details format
```

### Named Parameters Pattern
```yaml
components:
  parameters:
    LeadIdPath:
      name: id
      in: path
      required: true
      schema:
        type: string
        format: uuid
    HoldIdPath:
      name: holdId
      in: path
      required: true
      schema:
        type: string
        format: uuid
```

### RFC7807 Error Responses
```yaml
components:
  schemas:
    ProblemDetails:
      type: object
      required: [type, title, status]
      properties:
        type:
          type: string
          format: uri
          example: "https://freshplan.de/problems/lead-protection-expired"
        title:
          type: string
          example: "Lead Protection Expired"
        status:
          type: integer
          example: 403
        detail:
          type: string
          example: "Lead protection expired on 2025-09-15. Grace period ended."
        instance:
          type: string
          format: uri
          example: "/api/leads/12345/protection"
```

## Integration with Backend Services

### Lead Protection Service Integration
```java
// Controller Implementation
@Path("/api/leads/{id}/protection")
public class LeadProtectionController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProtectionStatus(@PathParam("id") String leadId) {
        // Implementation folgt OpenAPI Spec
    }
}
```

### ABAC Security Integration
```yaml
# Security Scheme
security:
  - bearerAuth: []

# Per-Endpoint Security
paths:
  /api/leads/{id}/protection:
    get:
      security:
        - bearerAuth: []
      description: |
        Requires ABAC permission:
        - OWNER: Full access
        - COLLABORATOR: Limited access based on role
        - AUDITOR: Full read access
        - MANAGER: Override access
```

## API Response Examples

### Lead Protection Status
```json
{
  "leadId": "12345678-1234-1234-1234-123456789abc",
  "status": "ACTIVE",
  "validUntil": "2026-03-15T23:59:59Z",
  "progressStatus": "ON_TRACK",
  "nextReminderDate": "2025-11-15T00:00:00Z",
  "gracePeriodDays": 10,
  "activeHolds": [
    {
      "holdId": "hold-001",
      "type": "FFZ_PRICE_APPROVAL",
      "startDate": "2025-09-01T00:00:00Z",
      "endDate": null,
      "reason": "Pricing approval pending"
    }
  ],
  "collaborators": [
    {
      "userId": "user-456",
      "role": "ASSIST",
      "addedDate": "2025-09-01T00:00:00Z"
    }
  ]
}
```

### Error Response (RFC7807)
```json
{
  "type": "https://freshplan.de/problems/lead-protection-conflict",
  "title": "Lead Protection Conflict",
  "status": 409,
  "detail": "Cannot add hold: Lead is already in GRACE period",
  "instance": "/api/leads/12345/holds",
  "leadId": "12345678-1234-1234-1234-123456789abc",
  "currentStatus": "GRACE",
  "conflictingAction": "ADD_HOLD"
}
```

## Public Customer Feedback API

### Token-based Access
```yaml
/api/feedback/{token}:
  post:
    summary: Submit customer feedback (public)
    security: []  # No authentication required
    parameters:
      - name: token
        in: path
        required: true
        schema:
          type: string
          pattern: '^[A-Za-z0-9]{32}$'
```

### Feedback Payload
```json
{
  "contactPerson": "Hans Müller",
  "contactRole": "DECISION_MAKER",
  "overallSatisfaction": 4,
  "feedbackItems": [
    {
      "productSku": "FF-APPLE-001",
      "productName": "Bio Äpfel Premium",
      "rating": 5,
      "comment": "Excellent quality, customers love them",
      "wouldReorder": true
    }
  ],
  "additionalComments": "Great service, fast delivery",
  "source": "FORM"
}
```

## Deployment Integration

### Postman Collection Generation
```bash
# Generate Postman collections from OpenAPI specs
openapi2postman -s lead-protection.yaml -o lead-protection.postman.json
openapi2postman -s lead-collaboration.yaml -o lead-collaboration.postman.json
openapi2postman -s sample-management.yaml -o sample-management.postman.json
```

### API Gateway Integration
```yaml
# AWS API Gateway / Kong Configuration
- name: lead-protection-api
  url: http://backend:8080/api
  plugins:
    - name: jwt
    - name: rate-limiting
      config:
        minute: 1000
    - name: correlation-id
```

## Testing Strategy

### Contract Testing
- **Pact Testing:** Frontend ↔ Backend Contract Validation
- **Schema Validation:** Request/Response gegen OpenAPI Schema
- **Security Testing:** OWASP ZAP gegen API Endpoints

### Performance Testing
```bash
# k6 Load Testing
k6 run --vus 100 --duration 30s lead-protection-load-test.js
```

### API Documentation Testing
- **Swagger UI:** Interactive Documentation
- **ReDoc:** Beautiful API Documentation
- **Insomnia/Postman:** API Testing Collections

---

**📋 Complete OpenAPI 3.1 specifications ready for immediate backend implementation!**