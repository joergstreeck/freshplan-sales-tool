# 🔒 Security Foundation Follow-Up Tasks

**Erstellt:** 2025-09-23
**Context:** Nach Sprint 1.2 PR #96
**Status:** Dokumentiert für zukünftige Sprints

## 🎯 Zusammenfassung
Die Security Foundation (V227) ist minimal und sauber implementiert. Zwei wichtige Architektur-Themen müssen in Phase 2 adressiert werden.

## 🚨 KRITISCH für Sprint 2.x: GUC-Kontext & Connection-Lebenszyklus

### Problem
Der `SessionSettingsFilter` setzt PostgreSQL GUCs über **eine eigene Connection**. Für RLS müssen die GUCs aber auf **derselben Connection** gesetzt werden, die die Queries ausführt.

### Lösung: DbContext Service
```java
@ApplicationScoped
public class DbContext {
    @Inject EntityManager em;

    @Transactional(Transactional.TxType.MANDATORY)
    public void apply(UUID userId, String orgId, String territory, Set<String> roles) {
        em.unwrap(org.hibernate.Session.class).doWork(conn -> {
            try (var ps = conn.prepareStatement(
                "SELECT set_app_context(?::uuid, ?, ?, string_to_array(?, ','))")) {
                ps.setObject(1, userId);
                ps.setString(2, orgId);
                ps.setString(3, territory);
                ps.setString(4, String.join(",", roles));
                ps.execute();
            }
        });
    }
}
```

### Implementation-Strategie
1. **Wann:** Sobald das erste Modul RLS-Policies bekommt (Sprint 2.x)
2. **Wo:** Am Anfang jeder `@Transactional` Service-Methode, die RLS-geschützte Tabellen nutzt
3. **Wie:** Modulweise einführen, nicht Big-Bang

### Beispiel-Usage
```java
@Transactional
public List<Lead> getMyLeads() {
    // ERSTER Aufruf in der Transaktion
    dbContext.apply(
        securityIdentity.getUserId(),
        securityIdentity.getOrgId(),
        securityIdentity.getTerritory(),
        securityIdentity.getRoles()
    );

    // Jetzt sind die GUCs für diese Connection/Transaction gesetzt
    return leadRepository.findAll();
}
```

## ⚠️ WICHTIG für PR #2: Settings Registry Duplikation vermeiden

### Aktuelle Situation
- V227 hat bereits `security_settings` Tabelle erstellt
- PR #2 soll allgemeine Settings Registry implementieren

### Optionen
1. **Option A:** V227-Tabelle verallgemeinern
   - Rename zu `app_settings`
   - Add `category` column (security, ui, feature)

2. **Option B:** Namespace in key
   - Behalte `security_settings`
   - Nutze Namespaces: `security.xyz`, `ui.theme`, etc.

### Empfehlung
Option B ist einfacher - keine Migration der bestehenden Tabelle nötig.

## 📝 Kleinere Verbesserungen (Optional)

### 1. has_role() Whitespace-Robustheit
```sql
-- Robuster gegen Whitespace in Rollen-Liste
CREATE OR REPLACE FUNCTION has_role(p_role TEXT) RETURNS BOOLEAN AS $$
DECLARE
    v_roles TEXT := current_setting('app.roles', true);
BEGIN
    RETURN EXISTS (
        SELECT 1
        FROM unnest(string_to_array(
            regexp_replace(COALESCE(v_roles,''), '\s+', '', 'g'), ','
        )) r
        WHERE r = p_role
    );
END;
$$ LANGUAGE plpgsql;
```

### 2. Flyway out-of-order nur in Dev
```properties
# application.properties
%dev.quarkus.flyway.out-of-order=true
%test.quarkus.flyway.out-of-order=true
quarkus.flyway.out-of-order=false  # Prod: strikt sequentiell
```

### 3. Logging-Hygiene
- Keine PII (Personally Identifiable Information) in INFO/ERROR logs
- User-IDs nur in DEBUG (in Prod disabled)

## 🔄 Integration in Sprint-Planung

### Sprint 2.1 Tasks
- [ ] DbContext Service implementieren
- [ ] Ersten RLS-Use-Case mit DbContext testen
- [ ] Documentation aktualisieren

### Sprint 2.2 Tasks
- [ ] Settings Registry konsolidieren (keine Duplikation)
- [ ] has_role() Whitespace-Fix (wenn Zeit)

### Pro Modul-Migration
Wenn ein Modul RLS bekommt, immer:
1. Migration mit RLS-Policies
2. Service-Layer: DbContext.apply() einbauen
3. Integration-Test mit verschiedenen Rollen

## 📊 Tracking
Diese Tasks werden in die jeweiligen TRIGGER_SPRINT_X.md Dateien übernommen, sobald relevant.

## 🎯 Definition of Done
- [ ] Alle RLS-geschützten Services nutzen DbContext
- [ ] Keine doppelten Settings-Tabellen
- [ ] Alle optionalen Verbesserungen evaluiert