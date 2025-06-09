# API Cheatsheet - Frontend Quick Reference

**Backend Base URL:** `http://localhost:8080`

## 🔐 Authentication

### Mock JWT Tokens (Development)

```bash
# Admin Token
curl http://localhost:8080/api/dev/users/admin

# Manager Token
curl http://localhost:8080/api/dev/users/manager

# Sales Token
curl http://localhost:8080/api/dev/users/sales
```

### Token im Frontend nutzen

```typescript
// Automatisch via apiClient.ts
const token = localStorage.getItem('auth-token');
headers['Authorization'] = `Bearer ${token}`;
```

## 📋 User Management

### Get All Users

```typescript
GET /api/users
Response: User[]

// Mit React Query:
const { data: users } = useUsers();
```

### Get Single User

```typescript
GET / api / users / { id };
Response: User;

// Mit React Query:
const { data: user } = useUser(userId);
```

### Create User

```typescript
POST /api/users
Body: {
  username: string,
  email: string,
  firstName: string,
  lastName: string,
  roles: string[],     // ['admin', 'manager', 'sales']
  enabled: boolean
}
Response: User
```

### Update User

```typescript
PUT / api / users / { id };
Body: Partial<User>;
Response: User;
```

### Delete User

```typescript
DELETE /api/users/{id}
Response: 204 No Content
```

### Enable/Disable User

```typescript
PUT /api/users/{id}/enable
PUT /api/users/{id}/disable
Response: 204 No Content
```

## 🧮 Calculator

### Calculate

```typescript
POST /api/calculator/calculate
Body: {
  basePrice: number,
  quantity: number,
  discountRate?: number,
  taxRate?: number
}
Response: {
  subtotal: number,
  discount: number,
  tax: number,
  total: number
}
```

## 🏥 Health Check

### Ping

```typescript
GET /api/ping
Response: {
  message: "pong",
  timestamp: string,
  dbTime: string,
  user?: string
}
```

## 📝 Response Formats

### Success Response

```typescript
{
  data: T,
  status: 200-299
}
```

### Error Response

```typescript
{
  error: {
    code: string,
    message: string,
    details?: any
  },
  status: 400-599
}
```

## 🛠️ Quick Testing mit curl

### Users abrufen (mit Mock-Token)

```bash
TOKEN=$(curl -s http://localhost:8080/api/dev/users/admin | jq -r '.token')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users
```

### User erstellen

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "test.user",
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "roles": ["sales"],
    "enabled": true
  }'
```

## 🔍 Debugging

### API Client Logs

```typescript
// In src/shared/lib/apiClient.ts temporär einbauen:
console.log('API Request:', { url, method, headers, body });
```

### Network Tab nutzen

- Filter: "XHR" oder "Fetch"
- Check: Request Headers (Token?)
- Check: Response Status & Body

### Mock Service Worker

```typescript
// Falls Backend nicht läuft, MSW nutzen:
// src/mocks/handlers.ts anpassen
```
