# ADR-005: User Management Frontend Architecture

**Status:** accepted
**Date:** 2025-01-07
**Accepted:** 2025-01-07 04:00 CET
**Authors:** Team FRONT (Claude)
**Reviewers:** @ChatGPT (Team ADVISOR)

## Context

Nach erfolgreichem Tailwind + shadcn/ui Setup beginnen wir mit Sprint 1 Features. Das erste Major Feature ist User Management UI.

**Backend Status:**
- User API vollständig implementiert (CRUD operations)
- Endpoints: GET/POST /api/users, GET/PUT/DELETE /api/users/{id}
- User Model: id, username, email, firstName, lastName, roles, enabled
- Authentication über Keycloak bereits vorbereitet

**Frontend Requirements:**
- User Dashboard mit Liste aller Users
- Create/Edit User Forms mit Validation
- Role Management (admin, manager, sales, viewer)
- Search & Filter Funktionalität
- Responsive Design für Desktop + Mobile
- Accessibility compliance

**Technical Context:**
- React 18 + TypeScript + Vite
- Tailwind CSS + shadcn/ui Design System
- React Router 7.6 für Navigation
- Target: Interne Tool für ~25 Mitarbeiter

## Decision

**ACCEPTED:** Feature-based Architecture + React Query + React Hook Form + Zod + Zustand

**Final Architecture Stack:**
- **Structure:** Feature-based with shared UI (`src/features/users/` + `src/shared/ui/`)
- **Server State:** React Query (TanStack Query) 
- **UI State:** Zustand (minimal global store)
- **Forms:** React Hook Form + Zod resolver
- **Routing:** Nested react-router with `/app/users/*` structure

## Options Under Consideration

### 1. **Component Architecture**

#### Option A: Feature-based Organization
```
src/features/users/
├── components/
│   ├── UserList.tsx
│   ├── UserForm.tsx
│   ├── UserCard.tsx
│   └── UserFilters.tsx
├── hooks/
│   ├── useUsers.ts
│   └── useUserForm.ts
├── services/
│   └── userService.ts
└── types/
    └── user.ts
```

#### Option B: Layer-based Organization
```
src/
├── components/
│   ├── users/
│   └── common/
├── hooks/
├── services/
└── types/
```

### 2. **State Management**

#### Option A: React Query + useState
- **Pros:** Einfach, server state separation, caching
- **Cons:** Lokaler State scattered across components

#### Option B: Zustand Store
- **Pros:** Zentrale State, TypeScript support, klein
- **Cons:** Overhead für kleine App

#### Option C: React Context + useReducer
- **Pros:** Built-in, gute TypeScript integration
- **Cons:** Performance bei vielen Updates

### 3. **Forms Management**

#### Option A: React Hook Form
- **Pros:** Performance, minimal re-renders, Zod integration
- **Cons:** Learning curve

#### Option B: Formik
- **Pros:** Mature, gute Community
- **Cons:** Mehr re-renders

#### Option C: Uncontrolled Forms
- **Pros:** Einfach für kleine Forms
- **Cons:** Weniger Kontrolle

### 4. **Data Fetching**

#### Option A: React Query (TanStack Query)
- **Pros:** Caching, background refetch, offline support
- **Cons:** Bundle size

#### Option B: SWR
- **Pros:** Kleiner, einfache API
- **Cons:** Weniger Features

#### Option C: Eigene useApi Hook
- **Pros:** Volle Kontrolle, kein vendor lock-in
- **Cons:** Mehr eigener Code

### 5. **Routing Structure**

#### Option A: Nested Routes
```
/users
├── /users/list
├── /users/create
├── /users/:id/edit
└── /users/:id/view
```

#### Option B: Flat Routes with Query Params
```
/users
/users?action=create
/users?action=edit&id=123
```

## Consequences

### Positive Benefits:
- **Bundle Size:** React Query + RHF + Zod ≈ 50kb gzipped (lightweight)
- **Developer Experience:** Feature-based structure enables parallel team development
- **Type Safety:** Zod schemas as Single Source of Truth for Frontend + Backend types
- **Performance:** React Hook Form minimal re-renders, React Query caching reduces API calls
- **Testing:** Feature folders keep tests close to code, MSW mocking simplified
- **Maintainability:** Clear separation of Server State (RQ) vs UI State (Zustand)

### Implementation Impact:
- **Setup Time:** ~2 days for foundation (acceptable for long-term benefits)
- **Learning Curve:** React Hook Form uncontrolled approach vs Formik
- **Bundle Trade-off:** Additional dependencies offset by better UX/DX

### Risk Mitigation:
- **Vendor Lock-in:** All libraries have escape hatches (RQ → fetch, RHF → native forms)
- **Complexity:** Feature-based structure isolates complexity per domain
- **Performance:** Zod validation can be lazy-loaded for complex schemas

## Implementation Notes

**Nach Advisor-Entscheidung:**
1. Setup gewählte Libraries
2. Create base User components
3. Implement User List with CRUD operations
4. Add Search & Filter functionality
5. Accessibility testing

## References

- [React Query Documentation](https://tanstack.com/query/latest)
- [React Hook Form Guide](https://react-hook-form.com/)
- [Frontend Architecture Best Practices](https://frontend.guide/)
- Backend User API Contract (siehe TEAM_SYNC_LOG.md)

---

**@ChatGPT - ADVISOR REQUEST:**

Basierend auf unserer Enterprise Sales Tool Situation mit 25 internen Usern:

1. **Welche Architektur-Kombinationen empfiehlst du?**
2. **Begründung basierend auf Team-Größe und Projekt-Scope**
3. **Implementation Priority (was zuerst, was später)**
4. **Potentielle Risiken und Mitigation**

**Review Checklist für @advisor:**
- [ ] Scalability für 25+ User Tool berücksichtigt
- [ ] Performance impact bewertet (Bundle size, runtime)
- [ ] Developer Experience optimiert
- [ ] Migration path für zukünftige Features definiert
- [ ] Testing strategy berücksichtigt