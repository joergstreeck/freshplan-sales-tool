# API Spezifikation: [Feature Name]

**Feature-Code:** FC-XXX
**API Version:** v1.0
**Erstellt:** [YYYY-MM-DD]
**Status:** 🟡 Entwurf / 🔵 Review / 🟢 Freigegeben
**Base URL:** `/api/v1/[resource]`

## 📋 Overview

### Resource Description
**Resource:** `[resource-name]`
**Beschreibung:** [Was repräsentiert diese Resource?]
**Business Context:** [In welchem Business-Kontext wird sie verwendet?]

### Authentication & Authorization
- **Authentication:** JWT Bearer Token
- **Required Roles:** [`admin`, `manager`, `user`]
- **Permissions:** [`read`, `write`, `delete`]

## 🏗️ Data Models

### Primary Entity
```typescript
interface [EntityName] {
  id: string;                    // UUID
  [field1]: string;              // Beschreibung
  [field2]: number;              // Beschreibung
  [field3]?: boolean;            // Optional field
  createdAt: string;             // ISO 8601 DateTime
  updatedAt: string;             // ISO 8601 DateTime
  createdBy: string;             // User ID
  updatedBy: string;             // User ID
}
```

### Request DTOs
```typescript
// Create Request
interface Create[EntityName]Request {
  [field1]: string;              // Required, min 1, max 255 chars
  [field2]: number;              // Required, min 0, max 999999
  [field3]?: boolean;            // Optional, default false
}

// Update Request
interface Update[EntityName]Request {
  [field1]?: string;             // Optional, min 1, max 255 chars
  [field2]?: number;             // Optional, min 0, max 999999
  [field3]?: boolean;            // Optional
}

// Query Parameters
interface [EntityName]QueryParams {
  page?: number;                 // Default: 0
  size?: number;                 // Default: 20, max: 100
  sort?: string;                 // Format: "field,direction" (asc|desc)
  search?: string;               // Search term
  [filter1]?: string;            // Filter by field1
  [filter2]?: number;            // Filter by field2
}
```

### Response DTOs
```typescript
// Single Entity Response
interface [EntityName]Response {
  id: string;
  [field1]: string;
  [field2]: number;
  [field3]: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
}

// List Response
interface [EntityName]ListResponse {
  content: [EntityName]Response[];
  page: {
    number: number;              // Current page (0-based)
    size: number;                // Page size
    totalElements: number;       // Total count
    totalPages: number;          // Total pages
  };
}

// Error Response
interface ErrorResponse {
  timestamp: string;             // ISO 8601 DateTime
  status: number;                // HTTP Status Code
  error: string;                 // Error type
  message: string;               // Error message
  path: string;                  // Request path
  traceId?: string;              // Trace ID for debugging
}
```

## 🚀 API Endpoints

### GET /api/v1/[resource]
**Beschreibung:** Liste aller [resource] abrufen
**Authorization:** Requires `read` permission

#### Request
```http
GET /api/v1/[resource]?page=0&size=20&sort=createdAt,desc&search=term
Authorization: Bearer {jwt_token}
```

#### Query Parameters
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | number | No | 0 | Page number (0-based) |
| `size` | number | No | 20 | Page size (max 100) |
| `sort` | string | No | `createdAt,desc` | Sort field and direction |
| `search` | string | No | - | Search term |
| `[filter1]` | string | No | - | Filter by field1 |

#### Response
**Success (200 OK):**
```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "[field1]": "example value",
      "[field2]": 42,
      "[field3]": true,
      "createdAt": "2023-01-01T10:00:00Z",
      "updatedAt": "2023-01-01T10:00:00Z",
      "createdBy": "user123",
      "updatedBy": "user123"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Error (400 Bad Request):**
```json
{
  "timestamp": "2023-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid page size. Maximum allowed: 100",
  "path": "/api/v1/[resource]"
}
```

### GET /api/v1/[resource]/{id}
**Beschreibung:** Einzelne [resource] abrufen
**Authorization:** Requires `read` permission

#### Request
```http
GET /api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer {jwt_token}
```

#### Path Parameters
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | [Resource] ID |

#### Response
**Success (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "[field1]": "example value",
  "[field2]": 42,
  "[field3]": true,
  "createdAt": "2023-01-01T10:00:00Z",
  "updatedAt": "2023-01-01T10:00:00Z",
  "createdBy": "user123",
  "updatedBy": "user123"
}
```

**Error (404 Not Found):**
```json
{
  "timestamp": "2023-01-01T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "[Resource] not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000"
}
```

### POST /api/v1/[resource]
**Beschreibung:** Neue [resource] erstellen
**Authorization:** Requires `write` permission

#### Request
```http
POST /api/v1/[resource]
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "[field1]": "example value",
  "[field2]": 42,
  "[field3]": true
}
```

#### Validation Rules
| Field | Rules |
|-------|-------|
| `[field1]` | Required, min 1, max 255 characters |
| `[field2]` | Required, min 0, max 999999 |
| `[field3]` | Optional, boolean |

#### Response
**Success (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "[field1]": "example value",
  "[field2]": 42,
  "[field3]": true,
  "createdAt": "2023-01-01T10:00:00Z",
  "updatedAt": "2023-01-01T10:00:00Z",
  "createdBy": "user123",
  "updatedBy": "user123"
}
```

**Error (400 Bad Request):**
```json
{
  "timestamp": "2023-01-01T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/[resource]",
  "errors": [
    {
      "field": "[field1]",
      "message": "Field is required"
    },
    {
      "field": "[field2]",
      "message": "Value must be between 0 and 999999"
    }
  ]
}
```

### PUT /api/v1/[resource]/{id}
**Beschreibung:** [Resource] vollständig aktualisieren
**Authorization:** Requires `write` permission

#### Request
```http
PUT /api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "[field1]": "updated value",
  "[field2]": 84,
  "[field3]": false
}
```

#### Response
**Success (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "[field1]": "updated value",
  "[field2]": 84,
  "[field3]": false,
  "createdAt": "2023-01-01T10:00:00Z",
  "updatedAt": "2023-01-01T12:00:00Z",
  "createdBy": "user123",
  "updatedBy": "user456"
}
```

### PATCH /api/v1/[resource]/{id}
**Beschreibung:** [Resource] teilweise aktualisieren
**Authorization:** Requires `write` permission

#### Request
```http
PATCH /api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "[field1]": "partially updated value"
}
```

#### Response
**Success (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "[field1]": "partially updated value",
  "[field2]": 42,
  "[field3]": true,
  "createdAt": "2023-01-01T10:00:00Z",
  "updatedAt": "2023-01-01T14:00:00Z",
  "createdBy": "user123",
  "updatedBy": "user789"
}
```

### DELETE /api/v1/[resource]/{id}
**Beschreibung:** [Resource] löschen
**Authorization:** Requires `delete` permission

#### Request
```http
DELETE /api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000
Authorization: Bearer {jwt_token}
```

#### Response
**Success (204 No Content):**
```
(Empty response body)
```

**Error (404 Not Found):**
```json
{
  "timestamp": "2023-01-01T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "[Resource] not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "path": "/api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000"
}
```

## 🔒 Security Considerations

### Authentication
- **Token Type:** JWT Bearer Token
- **Token Expiry:** 1 hour (access token), 7 days (refresh token)
- **Token Location:** Authorization header: `Bearer {token}`

### Authorization
- **Role-Based:** Permissions checked per endpoint
- **Resource-Level:** Users can only access their own resources (unless admin)
- **Audit Trail:** All operations logged with user context

### Input Validation
- **Server-Side:** All inputs validated on backend
- **SQL Injection:** Prevented by JPA/Hibernate
- **XSS Protection:** Input sanitization and output encoding
- **CSRF Protection:** CSRF tokens for state-changing operations

### Rate Limiting
- **Read Operations:** 1000 requests/hour per user
- **Write Operations:** 100 requests/hour per user
- **Admin Operations:** 500 requests/hour per user

## 📊 Performance

### Response Times
- **GET /[resource]:** < 200ms P95
- **GET /[resource]/{id}:** < 100ms P95
- **POST /[resource]:** < 500ms P95
- **PUT/PATCH /[resource]/{id}:** < 300ms P95
- **DELETE /[resource]/{id}:** < 200ms P95

### Caching Strategy
- **HTTP Caching:** ETags for GET operations
- **Application Caching:** Redis for frequently accessed data
- **Cache TTL:** 5 minutes for lists, 1 hour for single entities

### Pagination
- **Default Page Size:** 20 items
- **Maximum Page Size:** 100 items
- **Offset-Based:** Traditional page/size pagination

## 🧪 Testing

### Test Cases
```gherkin
Feature: [Resource] Management

  Scenario: Get all [resource]
    Given I am authenticated as "user"
    And I have "read" permission
    When I GET "/api/v1/[resource]"
    Then the response status should be 200
    And the response should contain a list of [resource]

  Scenario: Create new [resource]
    Given I am authenticated as "user"
    And I have "write" permission
    When I POST to "/api/v1/[resource]" with valid data
    Then the response status should be 201
    And the response should contain the created [resource]

  Scenario: Unauthorized access
    Given I am not authenticated
    When I GET "/api/v1/[resource]"
    Then the response status should be 401
```

### Load Testing
```yaml
# k6 test scenario
scenarios:
  get_list:
    executor: constant-vus
    vus: 10
    duration: 30s
  create_resource:
    executor: constant-vus
    vus: 5
    duration: 30s
```

## 📝 Examples

### cURL Examples
```bash
# Get all resources
curl -X GET "https://api.freshplan.de/api/v1/[resource]?page=0&size=20" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Accept: application/json"

# Create new resource
curl -X POST "https://api.freshplan.de/api/v1/[resource]" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "[field1]": "example value",
    "[field2]": 42,
    "[field3]": true
  }'

# Update resource
curl -X PUT "https://api.freshplan.de/api/v1/[resource]/123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "[field1]": "updated value",
    "[field2]": 84,
    "[field3]": false
  }'
```

### JavaScript/TypeScript Examples
```typescript
// API Client Example
class [EntityName]ApiClient {
  private baseUrl = '/api/v1/[resource]';

  async getAll(params?: [EntityName]QueryParams): Promise<[EntityName]ListResponse> {
    const url = new URL(this.baseUrl, window.location.origin);
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          url.searchParams.set(key, String(value));
        }
      });
    }

    const response = await fetch(url.toString(), {
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return response.json();
  }

  async create(data: Create[EntityName]Request): Promise<[EntityName]Response> {
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return response.json();
  }
}
```

## 📋 Changelog

### v1.0 - [YYYY-MM-DD]
- Initial API specification
- Basic CRUD operations
- Authentication and authorization
- Input validation and error handling

---

**📋 Template verwendet:** API_SPEC_TEMPLATE.md v1.0
**📅 Letzte Aktualisierung:** [YYYY-MM-DD]
**👨‍💻 API Owner:** [Team/Person]

**🎯 Diese Spezifikation ist der Contract zwischen Frontend und Backend für Feature FC-XXX**