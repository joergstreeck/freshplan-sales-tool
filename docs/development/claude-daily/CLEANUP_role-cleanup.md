# Zusammenfassung: Bereinigung der User Roles

**Datum:** 08.06.2025  
**Durchgeführt von:** Claude

## ✅ Durchgeführte Änderungen

### 1. Entfernung der "viewer" Rolle
Die "viewer" (Zuschauer) Rolle wurde komplett entfernt, da sie in einem Sales Tool keinen Sinn macht.

**Neue Rollenstruktur:**
- **admin**: Software-Administration, User-Verwaltung
- **manager**: Geschäftsleitung, alle Berichte, Credit Checks  
- **sales**: Verkäufer, Kunden anlegen, Kalkulationen

### 2. API Contract aktualisiert

#### Entfernt:
- Eigene Auth-Endpoints (`/auth/login`, `/auth/refresh`, `/auth/logout`)
- "viewer" aus der Rollenliste

#### Hinzugefügt:
- Keycloak-Dokumentation für Authentication
- Calculator API
- Customer API
- Locations API
- Credit Check API
- UI Configuration API
- Translations API
- Health Check API

### 3. Backend-Anpassungen
- **RoleValidator.java**: "viewer" aus ALLOWED_ROLES entfernt
- **RoleValidatorTest.java**: Tests angepasst (3 statt 4 Rollen)
- **CalculatorResource.java**: Security hinzugefügt `@RolesAllowed({"sales", "manager", "admin"})`

### 4. Frontend-Anpassungen
- **api.ts**: UserRole type auf 3 Rollen reduziert
- **userSchemas.ts**: 
  - UserRole enum auf 3 Rollen reduziert
  - DEFAULT_USER_ROLE von "viewer" auf "sales" geändert
- **FRONTEND_BACKEND_SPECIFICATION.md**: currentUser.role auf 3 Rollen angepasst
- **LoginBypassPage.tsx**: 
  - "viewer" Mock-Login entfernt
  - Rollenbeschreibungen auf Deutsch aktualisiert

## 📋 Security-Verbesserung

Die Calculator API war versehentlich mit `@PermitAll` annotiert. Dies wurde korrigiert zu:
```java
@RolesAllowed({"sales", "manager", "admin"})
```

## 🎯 Resultat

Das System hat jetzt eine klare 3-Rollen-Struktur:
1. **admin**: Technische Administration
2. **manager**: Business-Entscheidungen
3. **sales**: Operative Arbeit

Keine unnötigen "Nur-Lese" Benutzer mehr!