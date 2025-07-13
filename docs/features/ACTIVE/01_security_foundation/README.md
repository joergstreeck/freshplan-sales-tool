# 🔒 Security Foundation - Tag 1

**Status:** 🚀 Ready to Start  
**Geschätzter Aufwand:** 1 Tag  
**Priorität:** KRITISCH - Blockiert alle anderen Features

## 🎯 Ziel

Security-Basis etablieren, damit alle folgenden Features mit User-Context arbeiten können.

## 📋 Implementation Checklist

### Morning (4h): Keycloak Integration
- [ ] AuthContext.tsx - Login implementieren (TODO Zeile 45)
- [ ] AuthContext.tsx - Logout implementieren (TODO Zeile 52)
- [ ] Token Refresh Mechanismus
- [ ] Error Handling für Auth-Fehler

### Afternoon (4h): Backend Security Context
- [ ] JWT Token Validation
- [ ] Security Context Provider
- [ ] User Extraction aus JWT
- [ ] Role-based Access Control

## 🚨 OFFENE FRAGEN (VOR START KLÄREN!)

### 🔴 Technische Fragen:
1. **Keycloak URL:** Ist `auth.z-catering.de` korrekt konfiguriert?
2. **Realm Name:** Welcher Realm soll verwendet werden?
3. **Client ID:** Wie lautet die Client ID für Frontend?
4. **Redirect URIs:** Welche URLs sind erlaubt?

### 🟡 Organisatorische Fragen:
1. **User Provisioning:** Wer legt neue User in Keycloak an?
2. **Rollen-Mapping:** Welche Rollen gibt es? (admin, manager, sales, viewer?)
3. **Session Timeout:** Wie lange sollen Sessions gültig sein?
4. **2FA:** Ist Two-Factor geplant? Wenn ja, wann?

### 🟢 Strategische Fragen:
1. **SSO-Strategie:** Nur Keycloak oder auch Social Login?
2. **API Keys:** Brauchen wir Service-Accounts für Integrationen?
3. **Audit Requirements:** Müssen wir Login/Logout loggen?

## 🧪 Test-Szenarien

```typescript
describe('Authentication Flow', () => {
  it('should redirect to Keycloak login when unauthenticated');
  it('should store JWT token after successful login');
  it('should auto-refresh token before expiry');
  it('should clear all data on logout');
  it('should handle network errors gracefully');
});
```

## 📍 Code Locations

### Frontend:
```bash
# Start hier:
cd frontend/src/contexts
vim AuthContext.tsx +45  # Login TODO
vim AuthContext.tsx +52  # Logout TODO

# Token Handling:
cd frontend/src/services/api
vim apiClient.ts  # Axios Interceptors
```

### Backend:
```bash
# Security Context:
cd backend/src/main/java/de/freshplan/infrastructure/security
vim SecurityContextProvider.java  # Neu erstellen

# JWT Validation:
cd backend/src/main/java/de/freshplan/infrastructure/security
vim JwtTokenValidator.java  # Neu erstellen
```

## ⚡ Quick Commands

```bash
# Frontend testen:
npm run dev
# Browser: http://localhost:5173 → sollte zu Keycloak redirecten

# Backend testen:
./mvnw quarkus:dev
# Test JWT: curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/me

# Keycloak Admin:
# Browser: http://localhost:8180/admin
```

## 🔗 Referenzen

- [Keycloak React Docs](https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)
- [Quarkus OIDC Guide](https://quarkus.io/guides/security-openid-connect)
- Existing Code: `frontend/src/contexts/AuthContext.tsx`
- Backend Security: Noch nicht implementiert

## ✅ Definition of Done

### Funktionale Anforderungen
- [ ] User kann sich einloggen
- [ ] JWT Token wird gespeichert
- [ ] Backend akzeptiert Token
- [ ] User-Info aus Token extrahiert
- [ ] Logout funktioniert

### 🏆 Quality Gates (PFLICHT!)
- [ ] **Tests:** Unit > 80%, Integration Tests für Auth-Flow
- [ ] **Formatierung:** Backend: `./mvnw spotless:check` ✓
- [ ] **Security:** Keine Hardcoded Secrets, Token sicher gespeichert
- [ ] **Two-Pass Review:** Durchgeführt und dokumentiert
- [ ] **Dokumentation:** Keycloak-Setup dokumentiert

### 🔄 Git-Workflow
- [ ] Morning Commit nach Keycloak Integration
- [ ] Afternoon Commit nach Backend Security
- [ ] PR: "feat: implement keycloak authentication"

## 🚀 Nächster Schritt

Nach Completion → [M4 Opportunity Pipeline](../02_opportunity_pipeline/README.md)