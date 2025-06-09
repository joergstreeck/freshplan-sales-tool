# API Testing Guide - Backend

**Tools:** curl, HTTPie, Postman, Insomnia

## 🚀 Quick Start - Mock JWT Tokens

### 1. Token holen (Development Mode)
```bash
# Admin Token
ADMIN_TOKEN=$(curl -s http://localhost:8080/api/dev/users/admin | jq -r '.token')

# Manager Token
MANAGER_TOKEN=$(curl -s http://localhost:8080/api/dev/users/manager | jq -r '.token')

# Sales Token  
SALES_TOKEN=$(curl -s http://localhost:8080/api/dev/users/sales | jq -r '.token')
```

### 2. Token in Variable speichern
```bash
# Für die Session:
export TOKEN=$ADMIN_TOKEN

# Oder direkt nutzen:
curl -H "Authorization: Bearer $ADMIN_TOKEN" http://localhost:8080/api/users
```

## 📋 User Management Tests

### GET - Alle User abrufen
```bash
# curl
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users | jq

# HTTPie  
http GET localhost:8080/api/users \
  "Authorization: Bearer $TOKEN"
```

### GET - Einzelnen User abrufen
```bash
# User ID aus der Liste nehmen
USER_ID="56ea1795-fd70-4123-8c6d-917c018430b6"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/$USER_ID | jq
```

### POST - User erstellen
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "peter.parker",
    "email": "peter@dailybugle.com",
    "firstName": "Peter",
    "lastName": "Parker",
    "roles": ["sales"],
    "enabled": true
  }' | jq

# HTTPie
http POST localhost:8080/api/users \
  "Authorization: Bearer $TOKEN" \
  username="peter.parker" \
  email="peter@dailybugle.com" \
  firstName="Peter" \
  lastName="Parker" \
  roles:='["sales"]' \
  enabled:=true
```

### PUT - User aktualisieren
```bash
curl -X PUT http://localhost:8080/api/users/$USER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "firstName": "Peter Benjamin",
    "roles": ["sales", "manager"]
  }' | jq
```

### DELETE - User löschen
```bash
curl -X DELETE http://localhost:8080/api/users/$USER_ID \
  -H "Authorization: Bearer $TOKEN" -i
# Erwarte: HTTP/1.1 204 No Content
```

### PUT - User aktivieren/deaktivieren
```bash
# Deaktivieren
curl -X PUT http://localhost:8080/api/users/$USER_ID/disable \
  -H "Authorization: Bearer $TOKEN" -i

# Aktivieren  
curl -X PUT http://localhost:8080/api/users/$USER_ID/enable \
  -H "Authorization: Bearer $TOKEN" -i
```

## 🧮 Calculator Tests

```bash
curl -X POST http://localhost:8080/api/calculator/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "basePrice": 1000.00,
    "quantity": 10,
    "discountRate": 0.15,
    "taxRate": 0.19
  }' | jq

# Erwartete Response:
{
  "subtotal": 10000.00,
  "discount": 1500.00,
  "tax": 1615.00,
  "total": 10115.00
}
```

## 🏥 Health Check

```bash
# Ohne Auth
curl http://localhost:8080/api/ping | jq

# Mit Auth (zeigt User)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/ping | jq
```

## 📝 Postman Collection

### Environment Variables
```javascript
{
  "baseUrl": "http://localhost:8080",
  "adminToken": "{{admin_jwt}}",
  "userId": ""
}
```

### Pre-request Script (Token holen)
```javascript
pm.sendRequest({
    url: pm.environment.get("baseUrl") + "/api/dev/users/admin",
    method: 'GET'
}, function (err, res) {
    pm.environment.set("adminToken", res.json().token);
});
```

### Tests für User Creation
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("User has ID", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.environment.set("userId", jsonData.id);
});
```

## 🐛 Fehlerbehandlung testen

### 400 - Bad Request
```bash
# Ungültige Email
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "test",
    "email": "keine-email",
    "firstName": "Test",
    "lastName": "User",
    "roles": ["sales"]
  }'
```

### 401 - Unauthorized
```bash
# Ohne Token
curl http://localhost:8080/api/users
```

### 404 - Not Found
```bash
# Nicht existierende ID
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/00000000-0000-0000-0000-000000000000
```

### 409 - Conflict
```bash
# Doppelter Username
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "admin",
    "email": "new@example.com",
    "firstName": "Duplicate",
    "lastName": "User",
    "roles": ["sales"]
  }'
```

## 🔍 Debugging

### Verbose Output
```bash
# curl mit -v
curl -v -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users

# HTTPie mit --verbose  
http --verbose GET localhost:8080/api/users \
  "Authorization: Bearer $TOKEN"
```

### Request/Response als Datei
```bash
# Response speichern
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users > users.json

# Request aus Datei
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d @new-user.json
```

## 📚 Nützliche Scripte

### Alle Users löschen (außer Admin)
```bash
#!/bin/bash
TOKEN=$ADMIN_TOKEN
for id in $(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users | jq -r '.[] | select(.username != "admin") | .id'); do
    echo "Deleting user $id"
    curl -X DELETE -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/$id
done
```

### Bulk User Creation
```bash
#!/bin/bash
TOKEN=$ADMIN_TOKEN
for i in {1..10}; do
    curl -X POST http://localhost:8080/api/users \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{
        \"username\": \"testuser$i\",
        \"email\": \"test$i@example.com\",
        \"firstName\": \"Test\",
        \"lastName\": \"User$i\",
        \"roles\": [\"sales\"],
        \"enabled\": true
      }"
done
```