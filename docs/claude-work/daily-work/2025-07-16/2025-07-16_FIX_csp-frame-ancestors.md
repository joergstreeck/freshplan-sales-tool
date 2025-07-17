# 🔧 FIX: CSP frame-ancestors Fehler - 16.07.2025 21:20

## Problem
Browser-Konsole zeigte CSP (Content Security Policy) Fehler:
```
Content Security Policy of your site blocks some resources
Resource: http://localhost:8180/
Status: blocked
Directive: frame-ancestors
```

## Ursache
Keycloak (läuft auf Port 8180) setzt standardmäßig eine `frame-ancestors 'self'` CSP-Direktive, die verhindert, dass es von anderen Origins (wie localhost:5173) in einem iframe eingebettet wird.

Die `silentCheckSsoRedirectUri` Option in der Keycloak-Konfiguration versucht, einen Silent SSO Check durchzuführen, was ein iframe verwendet. Dies kollidiert mit der CSP-Policy von Keycloak.

## Lösung
Deaktivierung der `silentCheckSsoRedirectUri` Option in der Keycloak-Konfiguration, da wir bereits `checkLoginIframe: false` gesetzt haben.

### Geänderte Datei:
- `frontend/src/lib/keycloak.ts`

### Code-Änderung:
```typescript
// Keycloak Initialisierungsoptionen
export const keycloakInitOptions = {
  onLoad: 'check-sso' as const,
  // silentCheckSsoRedirectUri deaktiviert um CSP iframe-ancestors Fehler zu vermeiden
  // silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
  pkceMethod: 'S256' as const,
  checkLoginIframe: false, // Deaktiviert iframe-basierte Session Checks
  enableLogging: import.meta.env.DEV,
};
```

## Ergebnis
✅ CSP frame-ancestors Fehler sollte jetzt verschwunden sein
✅ Keycloak verwendet keine iframes mehr für Session Checks
✅ SSO funktioniert weiterhin über den normalen Redirect-Flow

## Hinweis
Dies ist eine Dev-Mode Optimierung. In Production könnte man alternativ:
1. Keycloak so konfigurieren, dass es die Frontend-Origin in der frame-ancestors Policy erlaubt
2. Oder einen separaten silent-check-sso.html Endpoint bereitstellen, der die CSP-Regeln erfüllt