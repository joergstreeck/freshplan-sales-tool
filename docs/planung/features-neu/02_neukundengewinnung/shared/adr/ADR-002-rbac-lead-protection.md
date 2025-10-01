---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "adr"
status: "accepted"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# ADR-002: RBAC für Lead Protection und Transfer

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Shared → ADR → RBAC Lead Protection

## Status

**Accepted** (2025-09-28)

## Context

Das Lead Management System benötigt ein Role-Based Access Control (RBAC) System für:
- Lead-Schutz (6 Monate gemäß Handelsvertretervertrag)
- Progressive Profiling (Stage 0/1/2)
- Stop-the-Clock Mechanismus
- Lead-Transfer zwischen Partnern
- Merge/Unmerge Operationen

Die vertraglichen Vorgaben definieren klare Schutzregeln, aber die technische Umsetzung der Zugriffsrechte muss zwischen Flexibilität und Sicherheit balancieren.

## Decision

Wir implementieren ein 3-Stufen RBAC-Modell mit optionalem RLS-Layer:

### Rollen-Definition

#### 1. User (Basis-Rolle)
- **Create**: Stage 0/1 Leads (eigene)
- **Read**: Eigene Leads + Team-Leads
- **Update**: Eigene aktive Leads
- **Delete**: Soft-Delete eigener Leads (< 24h alt)
- **Activities**: Eigene Lead-Aktivitäten erfassen
- **Stop-the-Clock**: Antrag mit Pflichtgrund

#### 2. Manager (Erweiterte Rolle)
- **Inherits**: Alle User-Rechte
- **Create**: Stage 2 Leads (qualifiziert)
- **Read**: Team-übergreifend im Bereich
- **Force-Create**: Bei 409/202 Responses
- **Transfer**: Leads zwischen Team-Mitgliedern
- **Merge**: Duplikate zusammenführen
- **Stop-the-Clock**: Genehmigung + Override
- **Reports**: Team-Statistiken und KPIs

#### 3. Admin (Volle Kontrolle)
- **Inherits**: Alle Manager-Rechte
- **Read**: System-weit alle Leads
- **Transfer**: Partner-übergreifend
- **Unmerge**: Merge-Operationen rückgängig
- **Delete**: Hard-Delete mit Audit
- **Protection-Override**: Schutzfristen ändern
- **Audit**: Vollzugriff auf Audit-Logs
- **Backdating**: registered_at ändern mit Audit

### Permission Matrix

#### Backdating-spezifische Berechtigungen

| Aktion                               | SALES | MANAGER | ADMIN |
|--------------------------------------|:-----:|:-------:|:-----:|
| Lead anlegen (Standard)              |  ✅   |   ✅    |  ✅   |
| `registeredAt` beim CREATE setzen    |  ❌   |   ✅    |  ✅   |
| `registeredAt` nachträglich ändern   |  ❌   |   ✅    |  ✅   |

### Permission Matrix

```typescript
interface Permission {
  resource: string;
  action: string;
  scope: 'own' | 'team' | 'area' | 'global';
  conditions?: string[];
}

const permissions: Record<Role, Permission[]> = {
  USER: [
    { resource: 'lead', action: 'create', scope: 'own',
      conditions: ['stage IN (0,1)'] },
    { resource: 'lead', action: 'read', scope: 'team' },
    { resource: 'lead', action: 'update', scope: 'own',
      conditions: ['protection.status != "expired"'] },
    { resource: 'activity', action: 'create', scope: 'own' },
    { resource: 'stop_clock', action: 'request', scope: 'own' }
  ],

  MANAGER: [
    ...USER_PERMISSIONS,
    { resource: 'lead', action: 'create', scope: 'team',
      conditions: ['stage = 2'] },
    { resource: 'lead', action: 'read', scope: 'area' },
    { resource: 'lead', action: 'force_create', scope: 'team' },
    { resource: 'lead', action: 'transfer', scope: 'team' },
    { resource: 'lead', action: 'merge', scope: 'area' },
    { resource: 'stop_clock', action: 'approve', scope: 'team' }
  ],

  ADMIN: [
    { resource: '*', action: '*', scope: 'global' }
  ]
};
```

### Row-Level Security (RLS) - Phase 2

Für Phase 2 planen wir RLS-Policies auf PostgreSQL-Ebene:

```sql
-- RLS Policy Examples
CREATE POLICY lead_user_read ON leads
  FOR SELECT
  USING (
    owner_user_id = current_user_id()
    OR team_id IN (SELECT team_id FROM user_teams WHERE user_id = current_user_id())
  );

CREATE POLICY lead_manager_update ON leads
  FOR UPDATE
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE id = current_user_id()
      AND role IN ('MANAGER', 'ADMIN')
    )
  );
```

### Implementation Strategy

#### Phase 1: Application-Level RBAC (Sprint 2.1.5)
- JWT enthält Rolle und Team-IDs
- Service-Layer prüft Permissions
- Audit-Log für alle Aktionen

#### Phase 2: Database-Level RLS (Sprint 2.2.x)
- PostgreSQL RLS Policies
- Connection-Pool per Role
- Performance-Optimierung

## Consequences

### Positive
- **Vertragskonform**: 6-Monats-Schutz technisch durchgesetzt
- **Flexibel**: Manager können Konflikte lösen
- **Auditierbar**: Alle Aktionen nachvollziehbar
- **Skalierbar**: RLS-ready für große Teams
- **DSGVO-konform**: Zugriff nur bei Berechtigung

### Negative
- **Komplexität**: 3-Stufen-System erfordert Training
- **Performance**: Permission-Checks bei jedem Request
- **Migration**: Bestehende Daten müssen Rollen erhalten
- **Testing**: Viele Permutation zu testen

### Risks & Mitigation

| Risk | Mitigation |
|------|------------|
| Permission-Bypass | Whitelist-Approach, Default Deny |
| Role-Explosion | Maximal 3 Basis-Rollen + Feature-Flags |
| Performance | Permission-Cache (5min TTL) |
| Audit-Overload | Sampling für Read-Operations |

## Alternatives Considered

### Alternative 1: Team-Based Permissions
- Pro: Einfacher, weniger Rollen
- Contra: Keine individuelle Kontrolle
- **Rejected**: Vertragspartner brauchen individuelle Leads

### Alternative 2: Attribute-Based (ABAC)
- Pro: Maximale Flexibilität
- Contra: Sehr komplex, schwer zu debuggen
- **Rejected**: Overkill für unseren Use-Case

### Alternative 3: Simple Owner-Check
- Pro: Sehr einfach
- Contra: Kein Team-Support, keine Hierarchie
- **Rejected**: Nicht ausreichend für B2B-Anforderungen

## References

- Handelsvertretervertrag §3.2 (Lead-Schutz)
- DSGVO Art. 32 (Technische Maßnahmen)
- PostgreSQL RLS Documentation
- [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- Sprint 2.1.5 Requirements
- Sprint 2.1.6 Transfer Requirements (upcoming)

## Decision Records

- **2025-09-28**: Initial ADR accepted
- **Future**: RLS-Migration (Phase 2) pending

## Review & Sign-Off

| Reviewer | Role | Date | Status |
|----------|------|------|--------|
| Tech Lead | Architecture | 2025-09-28 | ✅ Approved |
| Product Owner | Business | - | ⏳ Pending |
| Security | Compliance | - | ⏳ Pending |
| Data Protection | DSGVO | - | ⏳ Pending |