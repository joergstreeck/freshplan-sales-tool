# FC-002-M7: Einstellungen (User Management & Auth)

**Modul:** M7  
**Feature:** FC-002  
**Status:** 🔍 Analyse durchgeführt (NEU)  
**Geschätzter Aufwand:** Backend: 1 Tag | Frontend: 2 Tage  
**Abhängigkeit:** Keycloak (Auth)  
**Letztes Update:** 09.07.2025 - Kompakte Analyse durchgeführt

## 📊 Kompakte Modul-Analyse

### Backend-Analyse

#### User Management
**Was ist vorhanden?**
```
backend/src/main/java/de/freshplan/
├── api/
│   ├── UserResource.java           # CRUD Endpoints
│   └── DevUserResource.java        # Dev-Modus Features
└── domain/user/
    ├── entity/
    │   └── User.java               # JPA Entity
    ├── repository/
    │   └── UserRepository.java     # Panache Repository
    └── service/
        ├── UserService.java        # Business Logic
        ├── mapper/
        │   └── UserMapper.java     # DTO Mapping
        ├── dto/
        │   ├── CreateUserRequest.java
        │   ├── UpdateUserRequest.java
        │   ├── UpdateUserRolesRequest.java
        │   └── UserResponse.java
        └── exception/
            ├── UserNotFoundException.java
            ├── DuplicateUsernameException.java
            └── UserAlreadyExistsException.java
```

**Code-Qualität:** ⭐⭐⭐⭐⭐ (Exzellent)
- Perfekte Domain-Driven Design Struktur
- Vollständige Exception-Handling
- Saubere DTOs und Mapper
- Rollen-System implementiert (admin, manager, sales, viewer)

**Wiederverwendbarkeit:** 100%
- Kann komplett übernommen werden
- Bereits production-ready

#### Auth (Keycloak)
**Was ist vorhanden?**
- Keycloak-Integration über Quarkus OIDC
- JWT-Token Validation
- Role-Based Access Control
- Keine eigene Auth-Implementierung (gut!)

**Integration-Status:** ✅ Vollständig

### Frontend-Analyse

#### User Management
**Was ist vorhanden?**
```
frontend/src/
├── pages/
│   └── UsersPage.tsx              # Hauptseite
├── features/users/
│   ├── components/
│   │   ├── UserList.tsx          # Tabellen-Ansicht
│   │   ├── UserForm.tsx          # Create/Edit Form
│   │   └── UserListSkeleton.tsx # Loading State
│   ├── api/
│   │   └── usersApi.ts          # API Client
│   └── types/
│       └── user.ts              # TypeScript Types
└── hooks/
    └── useAuth.ts               # Auth Hook
```

**Code-Qualität:** ⭐⭐⭐⭐ (Sehr gut)
- Modern mit React Query
- TypeScript typisiert
- Skeleton Loading implementiert
- Fehlerbehandlung vorhanden

**Wiederverwendbarkeit:** 85%
- API-Layer perfekt
- UI muss minimal angepasst werden
- Bereits MUI-kompatibel strukturiert

#### Auth Frontend
**Was ist vorhanden?**
- KeycloakProvider implementiert
- AuthContext für Token-Management
- Auto-Refresh Mechanismus
- Protected Routes implementiert

## 🎯 Anpassungen für FC-002

### Backend (1 Tag)
1. **Erweiterungen:**
   - [ ] User-Preferences API (Theme, Language, etc.)
   - [ ] Audit-Log für User-Aktionen
   - [ ] Bulk-Operations (Import/Export)
   - [ ] Password-Policy Integration

### Frontend (2 Tage)
1. **Einstellungen-Bereich:**
   - [ ] Settings-Dashboard mit Tabs
   - [ ] Persönliche Einstellungen
   - [ ] System-Einstellungen (Admin)
   - [ ] Keycloak Self-Service Links

2. **User Management UI:**
   - [ ] Erweiterte Filter und Suche
   - [ ] Bulk-Actions (Multi-Select)
   - [ ] Role-Management UI
   - [ ] Activity-Log Viewer

3. **Integration:**
   - [ ] In Hauptnavigation als "Einstellungen"
   - [ ] Quick-Access für Profil-Settings
   - [ ] Admin-Panel Zugang

## 🚀 Empfehlung

**✅ WIEDERVERWENDEN mit minimalen Anpassungen**

**Begründung:**
- Backend ist production-ready
- Frontend hat solide Basis
- Hauptaufwand in UI-Erweiterungen

**Priorität:** HOCH - Basis-Funktionalität für alle User

## 📋 Implementierungs-Strategie

### Phase 1: Settings-Struktur (0.5 Tage)
- Settings-Page mit Tab-Navigation
- Routing einrichten
- Basis-Layout

### Phase 2: User-Preferences (1 Tag)
- Backend API erweitern
- Frontend Forms erstellen
- Local Storage Integration

### Phase 3: Admin-Features (1 Tag)
- Erweiterte User-Verwaltung
- System-Einstellungen
- Audit-Log Viewer

### Phase 4: Polish (0.5 Tage)
- Keycloak Self-Service Integration
- Help-Texte und Tooltips
- Responsive Design

## 🔒 Sicherheits-Überlegungen

1. **Kritische Aktionen:**
   - Rollen-Änderungen nur durch Admins
   - Audit-Trail für alle Änderungen
   - 2FA für Admin-Accounts empfohlen

2. **Keycloak-Integration:**
   - Self-Service Password Reset
   - Account-Sperrung bei Fehlversuchen
   - Session-Management

**Gesamtaufwand:** 3 Personentage