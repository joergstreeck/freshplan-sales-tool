# RLS Compliance Checklist

## Sprint 1.6 - @RlsContext Migration

### Für jeden Service prüfen:

- [ ] **Transaktionale Methoden mit DB-Zugriff** sind mit `@RlsContext` annotiert
- [ ] **Asynchrone/Langläufer** (Listener/Scheduler/Batch) setzen session-scoped Kontext per `set_config(?, ?, false)` oder laufen durch Interceptor
- [ ] **Tests** referenzieren neue GUC-Keys (`app.user_context`, `app.role_context`, `app.territory_context`)
- [ ] **Keine Duplikate** von RLS-Guides in Modulen (nur Badge + Links zu ADR-0007)

### Code-Pattern für Services:

```java
@ApplicationScoped
@Transactional  // Klassen-Level
public class YourService {

    @Inject EntityManager em;

    @RlsContext  // Methoden-Level für DB-Zugriffe
    public List<YourEntity> findByTerritory() {
        // RLS filtert automatisch
        return em.createQuery("SELECT e FROM YourEntity e", YourEntity.class)
                 .getResultList();
    }
}
```

### Test-Pattern:

```java
@Test
@TestSecurity(user = "test-user", roles = "user")
@Transactional
void testWithRls() {
    // Service method call
    // Assertions with RLS context
}
```

### Import Requirements:

```java
import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.infrastructure.security.AppGuc;
```

### CI-Guard aktivieren:

```bash
# Lokal testen:
cd backend
bash tools/rls-guard.sh

# CI läuft automatisch bei PRs
```

## Referenzen

- [ADR-0007: RLS Connection Affinity Pattern](../planung/adr/ADR-0007-rls-connection-affinity.md)
- [SECURITY_UPDATE_SPRINT_1_5.md](../planung/SECURITY_UPDATE_SPRINT_1_5.md)
- [SPRINT_1_5_GAP_ANALYSIS.md](../planung/SPRINT_1_5_GAP_ANALYSIS.md)