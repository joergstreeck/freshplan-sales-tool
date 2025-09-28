---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "contract"
status: "draft"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Lead Management API Contract

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Analyse → API → Leads OpenAPI

## OpenAPI 3.0 Specification

```yaml
openapi: 3.0.3
info:
  title: Lead Management API
  version: 2.1.5
  description: Progressive B2B Lead Capture with Protection and Deduplication

servers:
  - url: https://api.freshfoodz.de/v1
    description: Production
  - url: https://staging-api.freshfoodz.de/v1
    description: Staging

paths:
  /leads:
    post:
      summary: Create new lead with progressive profiling
      operationId: createLead
      tags:
        - Leads
      parameters:
        - name: X-Idempotency-Key
          in: header
          required: false
          schema:
            type: string
            format: uuid
          description: Unique request ID for idempotent operations

      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LeadCreateRequest'
            examples:
              stage0:
                summary: Stage 0 - Vormerkung (Minimal)
                value:
                  stage: 0
                  companyName: "Hotel Beispiel"
                  city: "Berlin"
                  industry: "Hospitality"

              stage1:
                summary: Stage 1 - Registration with Contact
                value:
                  stage: 1
                  companyName: "Restaurant Muster GmbH"
                  city: "München"
                  industry: "Gastronomy"
                  contact:
                    firstName: "Max"
                    lastName: "Mustermann"
                    email: "max@restaurant-muster.de"
                    phone: "+49 89 12345678"
                  source: "partner"
                  ownerUserId: "user-123"

              stage2:
                summary: Stage 2 - Qualified Lead
                value:
                  stage: 2
                  companyName: "Großküche Berlin GmbH"
                  city: "Berlin"
                  vatId: "DE123456789"
                  expectedVolume: 50000
                  keyAccount: true
                  chainAffiliation: "REWE Group"

      responses:
        '201':
          description: Lead created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LeadCreateResponse'
              example:
                id: "lead-uuid-123"
                stage: 1
                companyName: "Hotel Beispiel"
                protection:
                  status: "protected"
                  until: "2026-03-28T10:00:00Z"
                  nextActivityDue: "2025-11-28T10:00:00Z"
                  daysRemaining: 180
                createdAt: "2025-09-28T10:00:00Z"

        '202':
          description: Potential duplicates found - review required
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DuplicateCandidatesResponse'
              example:
                message: "Potential duplicates found"
                candidates:
                  - leadId: "lead-uuid-456"
                    score: 0.85
                    matchReasons: ["similar_name", "same_city"]
                    companyName: "Hotel Beispiel GmbH"
                    city: "Berlin"
                    protection:
                      status: "protected"
                      owner: "Partner ABC"
                  - leadId: "lead-uuid-789"
                    score: 0.72
                    matchReasons: ["fuzzy_name_match"]
                    companyName: "Hotel Beispiel & Co"
                    city: "Berlin"
                reviewUrl: "/leads/review?candidates=lead-uuid-456,lead-uuid-789"
                forceCreateToken: "token-xyz"

        '409':
          description: Hard duplicate detected
          content:
            application/problem+json:
              schema:
                $ref: '#/components/schemas/ProblemDetails'
              example:
                type: "https://api.freshfoodz.de/problems/duplicate-lead"
                title: "Duplicate Lead"
                status: 409
                detail: "Lead with email 'max@hotel.de' already exists"
                instance: "/leads/lead-uuid-existing"
                extensions:
                  existingLeadId: "lead-uuid-existing"
                  duplicateField: "email"
                  protectionStatus: "protected"

        '400':
          description: Validation error
          content:
            application/problem+json:
              schema:
                $ref: '#/components/schemas/ValidationProblem'

        '429':
          description: Rate limit exceeded
          headers:
            X-RateLimit-Limit:
              schema:
                type: integer
              example: 100
            X-RateLimit-Remaining:
              schema:
                type: integer
              example: 0
            X-RateLimit-Reset:
              schema:
                type: integer
              example: 1695894000

  /leads/{leadId}/protection:
    get:
      summary: Get lead protection status
      operationId: getLeadProtection
      tags:
        - Lead Protection
      parameters:
        - name: leadId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Protection status
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProtectionStatus'

    patch:
      summary: Update protection (Stop-the-Clock)
      operationId: updateLeadProtection
      tags:
        - Lead Protection
      parameters:
        - name: leadId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/StopTheClockRequest'
      responses:
        '200':
          description: Protection updated
        '403':
          description: Manager approval required

  /leads/{leadId}/activities:
    post:
      summary: Record lead activity
      operationId: recordActivity
      tags:
        - Lead Activities
      parameters:
        - name: leadId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ActivityRequest'
      responses:
        '201':
          description: Activity recorded
        '409':
          description: Lead protection expired

components:
  schemas:
    LeadCreateRequest:
      type: object
      required:
        - stage
        - companyName
        - city
      properties:
        stage:
          type: integer
          enum: [0, 1, 2]
          description: Progressive profiling stage
        companyName:
          type: string
          minLength: 2
          maxLength: 255
        city:
          type: string
          minLength: 2
          maxLength: 100
        industry:
          type: string
          maxLength: 100
        contact:
          $ref: '#/components/schemas/Contact'
        source:
          type: string
          enum: [manual, partner, marketing, import, other]
          default: manual
        ownerUserId:
          type: string
          format: uuid
        vatId:
          type: string
          pattern: '^[A-Z]{2}[0-9A-Z]+$'
        expectedVolume:
          type: number
          minimum: 0
        keyAccount:
          type: boolean
          default: false
        chainAffiliation:
          type: string

    Contact:
      type: object
      properties:
        firstName:
          type: string
          maxLength: 100
        lastName:
          type: string
          maxLength: 100
        email:
          type: string
          format: email
          maxLength: 255
        phone:
          type: string
          pattern: '^\+?[1-9]\d{1,14}$'

    LeadCreateResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        stage:
          type: integer
        companyName:
          type: string
        protection:
          $ref: '#/components/schemas/ProtectionStatus'
        createdAt:
          type: string
          format: date-time

    ProtectionStatus:
      type: object
      properties:
        status:
          type: string
          enum: [protected, warning, expired, released]
        until:
          type: string
          format: date-time
        nextActivityDue:
          type: string
          format: date-time
        daysRemaining:
          type: integer
        stopTheClock:
          type: object
          properties:
            active:
              type: boolean
            reason:
              type: string
            until:
              type: string
              format: date-time

    DuplicateCandidatesResponse:
      type: object
      properties:
        message:
          type: string
        candidates:
          type: array
          maxItems: 5
          items:
            type: object
            properties:
              leadId:
                type: string
                format: uuid
              score:
                type: number
                minimum: 0
                maximum: 1
              matchReasons:
                type: array
                items:
                  type: string
              companyName:
                type: string
              city:
                type: string
              protection:
                $ref: '#/components/schemas/ProtectionStatus'
        reviewUrl:
          type: string
        forceCreateToken:
          type: string

    StopTheClockRequest:
      type: object
      required:
        - action
        - reason
      properties:
        action:
          type: string
          enum: [pause, resume]
        reason:
          type: string
          minLength: 10
          maxLength: 500
        pauseUntil:
          type: string
          format: date

    ActivityRequest:
      type: object
      required:
        - type
        - description
      properties:
        type:
          type: string
          enum: [call, email, meeting, demo, proposal, other]
        description:
          type: string
          maxLength: 1000
        countsAsProgress:
          type: boolean
          default: true

    ProblemDetails:
      type: object
      properties:
        type:
          type: string
        title:
          type: string
        status:
          type: integer
        detail:
          type: string
        instance:
          type: string
        extensions:
          type: object

    ValidationProblem:
      allOf:
        - $ref: '#/components/schemas/ProblemDetails'
        - type: object
          properties:
            errors:
              type: array
              items:
                type: object
                properties:
                  field:
                    type: string
                  message:
                    type: string

  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

security:
  - bearerAuth: []
```

## Implementation Notes

### Idempotency
- Use `X-Idempotency-Key` header for safe retries
- Keys are stored for 24 hours
- Same key returns cached response with 200 (not 201)

### Rate Limiting
- Default: 100 requests per minute per user
- Fuzzy matching: 20 requests per minute
- Headers indicate limit, remaining, and reset time

### Protection Rules
- 6 months protection from registration
- 60 days activity requirement
- Warning after 53 days without activity
- Expired after 60 days without activity
- Stop-the-Clock requires reason and optional end date

### Duplicate Detection
- Hard duplicates: Same email (normalized) → 409
- Soft duplicates: Fuzzy name/city match → 202
- Score threshold: 0.7 (configurable)
- Maximum 5 candidates returned

### RBAC Matrix
| Action | User | Manager | Admin |
|--------|------|---------|-------|
| Create Stage 0/1 | ✓ | ✓ | ✓ |
| Create Stage 2 | - | ✓ | ✓ |
| Force Create (409) | - | ✓ | ✓ |
| Stop-the-Clock | ✓* | ✓ | ✓ |
| View Protection | ✓** | ✓ | ✓ |

*With reason, **Own leads only