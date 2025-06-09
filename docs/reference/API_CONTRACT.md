# API Contract - Frontend/Backend Schnittstelle

**📅 Aktuelles Datum: 09.06.2025 (System: 09.06.2025)**

## Base Configuration
```typescript
const API_BASE_URL = 'http://localhost:8080/api';
```

## Authentication
⚠️ **Authentication erfolgt über Keycloak** 
- Keycloak URL: `http://localhost:8180/realms/freshplan`
- Client ID: `freshplan-frontend`
- Authentication Flow: OAuth2/OIDC mit PKCE

Alle API-Requests benötigen ein gültiges Bearer Token von Keycloak:
```typescript
Headers: { Authorization: 'Bearer ${token}' }
```

## User Management

### Types
```typescript
interface UserResponse {
  id: string;           // UUID
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  enabled: boolean;
  roles: string[];      // ⚠️ BREAKING CHANGE: Jetzt IMMER vorhanden!
  createdAt: string;    // ISO 8601
  updatedAt: string;    // ISO 8601
}

interface CreateUserRequest {
  username: string;     // min: 3, max: 60
  firstName: string;    // min: 1, max: 60
  lastName: string;     // min: 1, max: 60
  email: string;        // valid email
}

interface UpdateUserRequest {
  username?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  enabled?: boolean;
}
```

### Endpoints
```typescript
// List Users (paginated)
GET /api/users?page=0&size=20&search=&enabledOnly=false
Response: UserResponse[]
Headers: X-Total-Count, X-Page-Number, X-Page-Size

// Get User
GET /api/users/{id}
Response: UserResponse | 404

// Search by Email
GET /api/users/search?email={email}
Response: UserResponse | 404

// Create User
POST /api/users
Request: CreateUserRequest
Response: 201 + UserResponse
Headers: Location: /api/users/{id}

// Update User  
PUT /api/users/{id}
Request: UpdateUserRequest
Response: UserResponse | 404

// Delete User
DELETE /api/users/{id}
Response: 204 | 404

// Enable/Disable
PUT /api/users/{id}/enable
PUT /api/users/{id}/disable
Response: 204 | 404

// Assign Roles ✅ FERTIG
PUT /api/users/{id}/roles
Request: { roles: string[] }  // Erlaubt: "admin", "manager", "sales"
Response: UserResponse | 404
Authorization: Bearer token mit "admin" Rolle erforderlich!
```

## Error Responses
```typescript
interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp?: string;
}
```

## Status Codes
- 200: OK
- 201: Created
- 204: No Content  
- 400: Bad Request (Validation)
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 409: Conflict (Duplicate)

## Calculator API

### Types
```typescript
interface CalculatorRequest {
  basePrice: number;
  customerType: 'new' | 'existing';
  isChainCustomer: boolean;
  chainType?: string;
  numberOfLocations: number;
  servicesPerLocation: {
    breakfast: number;
    lunch: number;
    vendingMachines: number;
  };
}

interface CalculatorResponse {
  totalPrice: number;
  discount: number;
  discountPercentage: number;
  pricePerLocation: number;
  savings: number;
}
```

### Endpoints
```typescript
// Calculate pricing
POST /api/calculator/calculate
Request: CalculatorRequest
Response: CalculatorResponse
Authorization: Roles: "admin", "manager", "sales"
```

## Customer API

### Types
```typescript
interface Customer {
  id: string;
  companyName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: {
    street: string;
    zip: string;
    city: string;
  };
  customerType: 'new' | 'existing';
  isChainCustomer: boolean;
  createdAt: string;
  updatedAt: string;
}

interface CreateCustomerRequest {
  companyName: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: {
    street: string;
    zip: string;
    city: string;
  };
  customerType: 'new' | 'existing';
  isChainCustomer: boolean;
}
```

### Endpoints
```typescript
// Create customer
POST /api/customers
Request: CreateCustomerRequest
Response: 201 + Customer

// Get customer
GET /api/customers/{id}
Response: Customer | 404

// Update customer
PUT /api/customers/{id}
Request: Partial<Customer>
Response: Customer | 404

// List customers
GET /api/customers?page=0&size=20&search=
Response: Customer[]
Headers: X-Total-Count, X-Page-Number, X-Page-Size
```

## Locations API

### Types
```typescript
interface Location {
  id: string;
  customerId: string;
  name: string;
  address: {
    street: string;
    zip: string;
    city: string;
  };
  services: {
    breakfast: number;
    lunch: number;
    vendingMachines: number;
  };
}
```

### Endpoints
```typescript
// Add location
POST /api/customers/{customerId}/locations
Request: Omit<Location, 'id' | 'customerId'>
Response: 201 + Location

// Get locations
GET /api/customers/{customerId}/locations
Response: Location[]
```

## Credit Check API

### Types
```typescript
interface CreditCheckRequest {
  customerId: string;
  requestedAmount: number;
}

interface CreditCheckResponse {
  checkId: string;
  status: 'pending' | 'approved' | 'rejected';
  creditLimit?: number;
  validUntil?: string;
}
```

### Endpoints
```typescript
// Initiate check
POST /api/creditcheck/initiate
Request: CreditCheckRequest
Response: CreditCheckResponse
Authorization: Roles: "admin", "manager"

// Check status
GET /api/creditcheck/{checkId}
Response: CreditCheckResponse
```

## UI Configuration API

```typescript
// Get navigation state
GET /api/ui/navigation/{customerId}
Response: {
  completedSteps: string[];
  currentStep: string;
  availableSteps: string[];
}
```

## Translations API

```typescript
// Get translations
GET /api/translations/{locale}
Response: Record<string, string>
```

## Health Check

```typescript
// Ping endpoint (no auth required)
GET /api/ping
Response: { message: string, timestamp: string }
```

---

**Letzte Änderung:** 2025-06-08 - API Contract vervollständigt, viewer-Rolle entfernt, Keycloak-Auth dokumentiert