# Enterprise Test Plan - Sprint 2.1.7.7

**Sprint:** 2.1.7.7 - RBAC Enhancement & Auth-Bypass Fixes
**Datum:** 2025-11-03
**Testabdeckungs-Ziel:** ≥85% für neue Features, ≥80% gesamt

## 📋 Test-Kategorien

### 1. **Unit Tests** (Isolierte Funktionen/Komponenten)
- **Ziel:** ≥90% Coverage für neue Funktionen
- **Framework:** Vitest + React Testing Library
- **Laufzeit:** < 5 Sekunden

### 2. **Integration Tests** (Feature-Flows)
- **Ziel:** Alle kritischen User-Flows getestet
- **Framework:** Vitest + React Testing Library + MSW
- **Laufzeit:** < 30 Sekunden

### 3. **E2E Tests** (Browser-basiert)
- **Ziel:** Happy Paths + Critical Paths
- **Framework:** Playwright (wenn verfügbar)
- **Laufzeit:** < 2 Minuten

## 🎯 Test-Scope für Sprint 2.1.7.7

### **Feature 1: Auth-Bypass Fixes**

#### 1.1 **useAuth Hook**
**File:** `frontend/src/hooks/__tests__/useAuth.test.tsx`

**Test Cases:**
- ✅ `should return user object with all fields (firstName, lastName, name)`
- ✅ `should handle case-insensitive role checks (ADMIN vs admin)`
- ✅ `should return correct permissions for admin role`
- ✅ `should return correct permissions for sales role`
- ✅ `should return correct permissions for manager role`
- ✅ `should return empty array when user has no roles`
- ✅ `should work in Auth-Bypass mode`
- ✅ `should work in Keycloak mode`

**Priorität:** 🔴 CRITICAL (Security)

#### 1.2 **AuthContext**
**File:** `frontend/src/contexts/__tests__/AuthContext.enhanced.test.tsx` (extend existing)

**Test Cases:**
- ✅ `should initialize with localStorage user in Auth-Bypass mode (lazy initializer)`
- ✅ `should not show "Gast" on first render when user is in localStorage`
- ✅ `should handle user roles correctly (lowercase)`
- ✅ `should update when localStorage changes (cross-tab sync)`
- ✅ `should fallback to default mock user when no localStorage`

**Priorität:** 🔴 CRITICAL (Security)

---

### **Feature 2: RBAC - Admin Routes Protection**

#### 2.1 **ProtectedRoute Component**
**File:** `frontend/src/components/auth/__tests__/ProtectedRoute.test.tsx`

**Test Cases:**
- ✅ `should render children when user has required role (admin)`
- ✅ `should redirect to /login when user is not authenticated`
- ✅ `should redirect to /unauthorized when user lacks required role`
- ✅ `should allow access with any matching role (admin OR auditor)`
- ✅ `should handle multiple roles correctly`

**Priorität:** 🔴 CRITICAL (Security)

#### 2.2 **Admin Routes (Integration)**
**File:** `frontend/src/__tests__/admin-routes.integration.test.tsx`

**Test Cases:**
- ✅ `/admin` - blocks sales/manager, allows admin
- ✅ `/admin/users` - blocks non-admins
- ✅ `/admin/system` - blocks non-admins
- ✅ `/admin/integrations` - blocks non-admins
- ✅ `/admin/audit` - allows admin + auditor, blocks others

**Priorität:** 🔴 CRITICAL (Security)

---

### **Feature 3: RBAC - Navigation Filtering**

#### 3.1 **SidebarNavigation**
**File:** `frontend/src/components/layout/__tests__/SidebarNavigation.rbac.test.tsx`

**Test Cases:**
- ✅ `should calculate permissions from user roles (admin)`
- ✅ `should calculate permissions from user roles (sales)`
- ✅ `should calculate permissions from user roles (manager)`
- ✅ `should filter navigation items based on permissions`
- ✅ `should hide admin menu for sales/manager`
- ✅ `should show admin menu for admin`
- ✅ `should show audit dashboard for auditor`

**Priorität:** 🔴 CRITICAL (Security)

#### 3.2 **NavigationItem**
**File:** `frontend/src/components/layout/__tests__/NavigationItem.rbac.test.tsx`

**Test Cases:**
- ✅ `should filter subItems based on permissions`
- ✅ `should pass userPermissions to component`
- ✅ `should show all admin submenus for admin`
- ✅ `should hide admin-only submenus for non-admins`

**Priorität:** 🟡 HIGH

---

### **Feature 4: UX Fixes**

#### 4.1 **LeadsPage - Stop-the-Clock Button**
**File:** `frontend/src/pages/__tests__/LeadsPage.rbac.test.tsx`

**Test Cases:**
- ✅ `should show Stop-the-Clock button for admin`
- ✅ `should show Stop-the-Clock button for manager`
- ✅ `should hide Stop-the-Clock button for sales`
- ✅ `should not render button element when hidden (not just disabled)`

**Priorität:** 🟢 MEDIUM

#### 4.2 **App.tsx - Admin Card**
**File:** `frontend/src/__tests__/App.rbac.test.tsx`

**Test Cases:**
- ✅ `should show "Benutzerverwaltung" card for admin`
- ✅ `should hide "Benutzerverwaltung" card for sales`
- ✅ `should hide "Benutzerverwaltung" card for manager`

**Priorität:** 🟢 MEDIUM

---

## 🔒 Security Test Requirements

### **Security-Critical Tests (Must Pass)**
1. ✅ All RBAC Tests (Admin Route Protection)
2. ✅ All Permission-based Filtering Tests
3. ✅ Auth Context Tests (hasRole case-sensitivity)

**Acceptance Criteria:**
- 100% Pass Rate für Security Tests
- Code Coverage ≥90% für RBAC-Code
- Keine hardcoded Permissions in Production Code

---

## 📊 Coverage Targets

| Module | Current Coverage | Target | Priority |
|--------|-----------------|--------|----------|
| `useAuth.ts` | 0% (NEW) | 90% | 🔴 CRITICAL |
| `AuthContext.tsx` | ~70% | 90% | 🔴 CRITICAL |
| `ProtectedRoute.tsx` | Unknown | 100% | 🔴 CRITICAL |
| `SidebarNavigation.tsx` | ~50% | 85% | 🔴 CRITICAL |
| `NavigationItem.tsx` | ~50% | 80% | 🟡 HIGH |
| `LeadsPage.tsx` | Unknown | 70% | 🟢 MEDIUM |
| `App.tsx` | Unknown | 60% | 🟢 MEDIUM |

---

## 🚀 Execution Plan

### **Phase 1: Critical Security Tests (2-3h)**
1. ✅ useAuth Hook Tests
2. ✅ ProtectedRoute Tests
3. ✅ Admin Routes Integration Tests
4. ✅ SidebarNavigation RBAC Tests

### **Phase 2: Feature Tests (1-2h)**
5. ✅ NavigationItem Permission Filtering Tests
6. ✅ LeadsPage RBAC Tests
7. ✅ App.tsx RBAC Tests

### **Phase 3: Regression Tests (1h)**
8. ✅ Run full test suite
9. ✅ Fix any breaking changes
10. ✅ Verify coverage targets met

---

## ✅ Success Criteria

- [ ] All Security Tests Pass (100%)
- [ ] Code Coverage ≥85% für neue Features
- [ ] Keine Regressions in bestehenden Tests
- [ ] CI Pipeline grün
- [ ] Manual QA: Admin/Sales/Manager User Flows getestet

---

## 📝 Notes

**Test Infrastructure:**
- Vitest als Test Runner
- React Testing Library für Component Tests
- MSW für API Mocking
- @testing-library/user-event für User Interactions

**Mock Strategy:**
- Mock useAuth Hook für Role-basierte Tests
- Mock Keycloak Context für Auth-Flow Tests
- Mock API Responses via MSW

**Edge Cases:**
- User ohne Rollen
- User mit mehreren Rollen (admin + auditor)
- Role case-sensitivity (ADMIN vs admin)
- Navigation ohne Permissions-Config

---

**Erstellt:** 2025-11-03
**Letztes Update:** 2025-11-03
**Status:** 🟢 READY FOR IMPLEMENTATION
