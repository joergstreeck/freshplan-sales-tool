# API Contract - Frontend/Backend Schnittstelle

## Base Configuration
```typescript
const API_BASE_URL = 'http://localhost:8080/api';
const AUTH_ENDPOINT = 'http://localhost:8080/auth';
```

## Authentication
```typescript
// Login
POST /auth/login
Request: { username: string, password: string }
Response: { access_token: string, refresh_token: string, expires_in: number }

// Refresh
POST /auth/refresh  
Request: { refresh_token: string }
Response: { access_token: string, refresh_token: string, expires_in: number }

// Logout
POST /auth/logout
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
  roles?: string[];     // NEU in Sprint 1!
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

// Assign Roles (🚧 Team BACK heute)
PUT /api/users/{id}/roles
Request: { roles: string[] }
Response: UserResponse | 404
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

---

**Letzte Änderung:** 2025-01-06 - Initial Contract