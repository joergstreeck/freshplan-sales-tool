# ADR-006: Admin Frontend Architecture - Integrierte Lösung mit Rollen

**Status:** Akzeptiert  
**Datum:** 09.08.2025  
**Entscheider:** Jörg & Claude

## Kontext

Für das Audit Admin Dashboard und weitere Admin-Features mussten wir entscheiden, ob wir:
1. Eine separate Admin-Anwendung (eigene Subdomain/Port) erstellen
2. Admin-Features in die Hauptanwendung integrieren mit Rollen-basierter Sichtbarkeit

## Entscheidung

Wir haben uns für **Option 2** entschieden: Integrierte Admin-Features mit Rollen-basierter Sichtbarkeit.

## Begründung

### Vorteile der gewählten Lösung:
- ✅ **Single Sign-On:** Nutzt bestehendes Keycloak Setup ohne zweite Anmeldung
- ✅ **Code-Wiederverwendung:** Gemeinsame Components, Services und Stores
- ✅ **Bessere UX:** Nahtlose Navigation zwischen Admin- und normalen Features
- ✅ **Wartbarkeit:** Ein Repository, ein Deployment, ein Build-Prozess
- ✅ **Best Practice:** Folgt dem Muster erfolgreicher Projekte (GitLab, Grafana, Jira)

### Nachteile der Alternative (Separate App):
- ❌ Doppelte Authentifizierung nötig
- ❌ Code-Duplikation bei gemeinsamen Components
- ❌ Komplexere Infrastruktur und Deployment
- ❌ Höhere Wartungskosten

## Technische Umsetzung

### 1. Protected Routes
```typescript
// Rollen-basierter Schutz für Admin-Routen
<Route path="/admin" element={
  <ProtectedRoute allowedRoles={['admin', 'auditor']}>
    <AdminLayout />
  </ProtectedRoute>
}>
```

### 2. Struktur
```
frontend/src/
├── components/
│   ├── auth/
│   │   └── ProtectedRoute.tsx
│   └── layout/
│       └── AdminLayout.tsx
├── pages/
│   └── admin/
│       └── AuditAdminPage.tsx
└── features/
    └── audit/
        └── admin/
```

### 3. V2 Theme
- Material-UI v5 mit sx prop
- FreshFoodz CI Farben (#94C456, #004F7B)
- Antonio Bold + Poppins Schriften

### 4. Security
- Frontend: ProtectedRoute Component
- Backend: @RolesAllowed Annotations
- Keycloak: Token-basierte Authentifizierung

## Konsequenzen

### Positiv:
- Einheitliche User Experience
- Reduzierte Komplexität
- Schnellere Entwicklung neuer Admin-Features
- Bessere Performance durch Code-Sharing

### Neutral:
- Bundle-Größe erhöht sich (mitigiert durch Lazy Loading)
- Komplexere Routing-Logik (gut handhabbar)

### Negativ:
- Keine vollständige Isolation von Admin-Features
- Potentiell größerer Blast-Radius bei Fehlern

## Migration bestehender Features

Features die aktuell unter separaten Routen liegen (z.B. http://localhost:5173/admin-tool) werden schrittweise in die neue Struktur migriert.

## Links

- [Audit Admin Dashboard Planung](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_ADMIN_DASHBOARD.md)
- [Frontend Architecture Guide](/Users/joergstreeck/freshplan-sales-tool/docs/technical/FRONTEND_BACKEND_SPECIFICATION.md)
- [Keycloak Integration](/Users/joergstreeck/freshplan-sales-tool/docs/guides/KEYCLOAK_SETUP.md)