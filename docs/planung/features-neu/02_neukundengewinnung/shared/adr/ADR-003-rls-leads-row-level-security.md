---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "adr"
adr_number: "003"
status: "proposed"
sprint: "2.1.6"
owner: "team/security"
updated: "2025-09-28"
---

# ADR-003: Row Level Security (RLS) für Lead Management

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Shared → ADR → ADR-003

## Status
**Proposed** - Für Sprint 2.1.6 geplant

## Context

Das Lead-Management-System benötigt eine mehrstufige Zugriffskontrolle:

1. **Partner-Isolation**: Partner A darf niemals Leads von Partner B sehen
2. **Team-Sichtbarkeit**: Leads können Teams zugeordnet werden (Sprint 2.1.6)
3. **Lead-Transfer**: Leads müssen zwischen Partnern übertragbar sein (mit Genehmigung)
4. **Admin-Durchgriff**: Admins benötigen Vollzugriff für Support und Compliance

PostgreSQL Row Level Security (RLS) bietet datenbank-seitige Zugriffskontrolle, die unabhängig von der Anwendungslogik greift.

## Decision

### Implementierung mit PostgreSQL RLS

Wir verwenden PostgreSQL Row Level Security mit folgendem Design:

#### 1. Datenmodell-Erweiterungen

```sql
-- Leads Tabelle erweitern
ALTER TABLE leads ADD COLUMN IF NOT EXISTS
  owner_user_id UUID REFERENCES users(id),
  owner_team_id UUID REFERENCES teams(id),
  visibility VARCHAR(20) DEFAULT 'private'
    CHECK (visibility IN ('private', 'team', 'transfer_pending'));

-- Team-Mitgliedschaften
CREATE TABLE IF NOT EXISTS team_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  team_id UUID REFERENCES teams(id),
  user_id UUID REFERENCES users(id),
  role VARCHAR(20) DEFAULT 'member',
  created_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(team_id, user_id)
);
```

#### 2. RLS Policies

```sql
-- RLS aktivieren
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;

-- Policy 1: Owner kann eigene Leads sehen
CREATE POLICY lead_owner_policy ON leads
  FOR ALL
  TO application_user
  USING (owner_user_id = current_setting('app.current_user_id')::UUID);

-- Policy 2: Team-Mitglieder können Team-Leads sehen
CREATE POLICY lead_team_policy ON leads
  FOR SELECT
  TO application_user
  USING (
    visibility = 'team'
    AND owner_team_id IN (
      SELECT team_id FROM team_members
      WHERE user_id = current_setting('app.current_user_id')::UUID
    )
  );

-- Policy 3: Admin-Bypass
CREATE POLICY lead_admin_policy ON leads
  FOR ALL
  TO admin_role
  USING (true)
  WITH CHECK (true);

-- Policy 4: Transfer-Sichtbarkeit
CREATE POLICY lead_transfer_policy ON leads
  FOR SELECT
  TO application_user
  USING (
    visibility = 'transfer_pending'
    AND id IN (
      SELECT lead_id FROM lead_transfers
      WHERE target_user_id = current_setting('app.current_user_id')::UUID
        AND status = 'pending'
    )
  );
```

#### 3. Session-Context

```java
// Backend: Session-Context setzen
@RequestScoped
public class RLSContext {

    @Inject
    EntityManager em;

    @Inject
    SecurityIdentity identity;

    @PostConstruct
    void setContext() {
        String userId = identity.getAttribute("user_id");
        String role = identity.getRoles().stream()
            .findFirst().orElse("guest");

        em.createNativeQuery(
            "SET LOCAL app.current_user_id = :userId")
            .setParameter("userId", userId)
            .executeUpdate();

        em.createNativeQuery(
            "SET LOCAL app.current_role = :role")
            .setParameter("role", role)
            .executeUpdate();
    }
}
```

#### 4. Transfer-Flow mit RLS

```sql
-- Transfer-Request Tabelle
CREATE TABLE lead_transfers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  lead_id UUID REFERENCES leads(id),
  source_user_id UUID REFERENCES users(id),
  target_user_id UUID REFERENCES users(id),
  reason TEXT,
  status VARCHAR(20) DEFAULT 'pending',
  decided_by UUID REFERENCES users(id),
  decided_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW()
);

-- Transfer genehmigen
CREATE FUNCTION approve_lead_transfer(transfer_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
  UPDATE leads l
  SET
    owner_user_id = t.target_user_id,
    visibility = 'private'
  FROM lead_transfers t
  WHERE t.id = transfer_id
    AND t.lead_id = l.id
    AND t.status = 'pending';

  UPDATE lead_transfers
  SET
    status = 'approved',
    decided_by = current_setting('app.current_user_id')::UUID,
    decided_at = NOW()
  WHERE id = transfer_id;

  RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
```

## Consequences

### Positive

1. **Datenbank-garantierte Sicherheit**: Zugriffskontrolle unabhängig von Anwendungslogik
2. **Performance**: Filterung auf DB-Ebene, keine zusätzlichen Queries
3. **Audit-Trail**: Alle Zugriffe protokollierbar
4. **Compliance**: DSGVO-konforme Datentrennung
5. **Flexibilität**: Policies können ohne Code-Änderung angepasst werden

### Negative

1. **Komplexität**: RLS-Policies müssen sorgfältig getestet werden
2. **Debugging**: Schwieriger zu debuggen als Anwendungslogik
3. **Migration**: Bestehende Daten müssen owner_user_id erhalten
4. **PostgreSQL-Lock-in**: Nicht portabel zu anderen Datenbanken

### Neutral

1. **Testing**: Spezielle Test-Setup für RLS erforderlich
2. **Performance-Monitoring**: RLS-Impact muss gemessen werden
3. **Dokumentation**: Policies müssen gut dokumentiert sein

## Migration Strategy

### Phase 1: Sprint 2.1.6 - Basis
- owner_user_id und owner_team_id Felder
- Basis-Policies (Owner, Admin)
- Migration bestehender Leads

### Phase 2: Sprint 2.1.6 - Teams
- Team-Tabellen und Policies
- Team-Sichtbarkeit UI

### Phase 3: Sprint 2.1.6 - Transfer
- Transfer-Flow und Policies
- Genehmigungsprozess

## Test Strategy

```java
@QuarkusTest
@TestTransaction
class RLSLeadTest {

    @Test
    void userCanOnlySeeOwnLeads() {
        // Given: 2 Users mit je 2 Leads
        // When: User A queries all leads
        // Then: Nur 2 Leads sichtbar
    }

    @Test
    void teamMemberCanSeeTeamLeads() {
        // Given: Team mit 3 Members und 5 Leads
        // When: Member queries mit team visibility
        // Then: Alle 5 Team-Leads sichtbar
    }

    @Test
    void adminCanSeeAllLeads() {
        // Given: 10 Leads verschiedener Owner
        // When: Admin queries
        // Then: Alle 10 Leads sichtbar
    }
}
```

## Monitoring

```sql
-- RLS Performance Monitoring
CREATE VIEW rls_performance AS
SELECT
  queryid,
  query,
  calls,
  mean_exec_time,
  total_exec_time
FROM pg_stat_statements
WHERE query LIKE '%leads%'
ORDER BY mean_exec_time DESC;
```

## Alternatives Considered

1. **Application-Level Security**: Zu fehleranfällig, keine DB-Garantie
2. **Separate Schemas per Partner**: Zu komplex für Transfers
3. **Materialized Views**: Performance-Overhead, Sync-Probleme

## References

- [PostgreSQL RLS Documentation](https://www.postgresql.org/docs/current/ddl-rowsecurity.html)
- [CONTRACT_MAPPING.md](../../artefakte/SPRINT_2_1_5/CONTRACT_MAPPING.md)
- [ADR-002-rbac-lead-protection.md](./ADR-002-rbac-lead-protection.md)

## Decision Records

- **2025-09-28**: Initial proposal for Sprint 2.1.6
- **TBD**: Review and approval
- **TBD**: Implementation start