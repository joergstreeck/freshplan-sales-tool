# 🔄 FC-009 MIGRATION PLAN

**Zweck:** Sicherer Übergang von Rollen zu Permissions  
**Risiko:** HOCH - betrifft alle User  
**Rollback:** Jederzeit möglich via Feature Flag  

---

## 📅 Migration Timeline

### Woche 1: Parallel-Betrieb
- Mo-Di: Backend Implementation
- Mi-Do: Frontend Integration  
- Fr: Testing & Monitoring Setup

### Woche 2: Schrittweise Aktivierung
- Mo: 10% der User (Pilot Group)
- Mi: 50% der User
- Fr: 100% der User

### Woche 3: Cleanup
- Legacy Code entfernen
- Performance optimieren
- Documentation update

---

## 🎯 Migration Strategie

### Phase 1: Feature Flag Setup
```properties
# application.properties
feature.new-permissions.enabled=false
feature.new-permissions.pilot-users=user1@example.com,user2@example.com
```

### Phase 2: Pilot User Testing
```java
@ApplicationScoped
public class FeatureToggle {
    @ConfigProperty(name = "feature.new-permissions.pilot-users")
    List<String> pilotUsers;
    
    public boolean useNewPermissions(String userEmail) {
        if (!featureEnabled) return false;
        return pilotUsers.contains(userEmail);
    }
}
```

### Phase 3: Gradual Rollout
```sql
-- Tag 1: Pilot Users
UPDATE users SET use_new_permissions = true 
WHERE email IN ('pilot1@example.com', 'pilot2@example.com');

-- Tag 3: 10%
UPDATE users SET use_new_permissions = true 
WHERE id IN (SELECT id FROM users ORDER BY random() LIMIT 100);

-- Tag 5: 50%
UPDATE users SET use_new_permissions = true 
WHERE MOD(CAST(SUBSTRING(id::text, 1, 8) AS INTEGER), 2) = 0;

-- Tag 7: 100%
UPDATE users SET use_new_permissions = true;
```

---

## ⚠️ Kritische Prüfungen

### Vor Migration:
- [ ] Backup der user_roles Tabelle
- [ ] Performance Baseline dokumentiert
- [ ] Rollback Script getestet
- [ ] Monitoring Dashboards ready

### Während Migration:
- [ ] Error Rate < 0.1%
- [ ] Response Time < 110% Baseline
- [ ] Keine Permission-Denied Fehler
- [ ] Cache Hit Rate > 90%

### Nach Migration:
- [ ] Alle User können einloggen
- [ ] Kritische Funktionen getestet
- [ ] Performance akzeptabel
- [ ] Audit Log vollständig

---

## 🚨 Rollback Prozedur

### Sofort-Rollback (< 5 Min):
```bash
# 1. Feature Flag deaktivieren
kubectl set env deployment/backend FEATURE_NEW_PERMISSIONS_ENABLED=false

# 2. Cache leeren
kubectl exec -it deployment/backend -- curl -X DELETE http://localhost:8080/api/cache/clear

# 3. Monitoring prüfen
```

### Daten-Rollback (falls nötig):
```sql
-- Restore user roles from backup
UPDATE users u SET role = b.role
FROM user_roles_backup_20250717 b
WHERE u.id = b.user_id;

-- Verify
SELECT role, COUNT(*) FROM users GROUP BY role;
```

---

## 📊 Monitoring & Alerts

### Key Metrics:
```yaml
alerts:
  - name: permission_check_slow
    condition: p95_latency > 50ms
    severity: warning
    
  - name: permission_denied_spike
    condition: rate > 10/min
    severity: critical
    
  - name: cache_hit_rate_low
    condition: hit_rate < 80%
    severity: warning
```

### Dashboard Queries:
```sql
-- Permission Check Performance
SELECT 
    date_trunc('minute', created_at) as minute,
    AVG(duration_ms) as avg_duration,
    PERCENTILE_CONT(0.95) WITHIN GROUP (ORDER BY duration_ms) as p95
FROM permission_check_logs
WHERE created_at > NOW() - INTERVAL '1 hour'
GROUP BY minute;

-- Error Analysis
SELECT 
    error_type,
    COUNT(*) as count,
    COUNT(DISTINCT user_id) as affected_users
FROM permission_errors
WHERE created_at > NOW() - INTERVAL '1 hour'
GROUP BY error_type;
```

---

## 🔍 Testing Checklist

### Automated Tests:
- [ ] Unit Tests: Permission calculation
- [ ] Integration Tests: API endpoints
- [ ] E2E Tests: User workflows
- [ ] Performance Tests: 1000 concurrent users
- [ ] Chaos Tests: Cache failure scenarios

### Manual Tests:
- [ ] Admin kann alles
- [ ] Manager sieht nur Team-Daten
- [ ] Sales sieht nur eigene Daten
- [ ] Permission-Änderung wirkt sofort
- [ ] Logout/Login funktioniert

---

## 📝 Communication Plan

### Woche -1:
- Email an alle User: "Verbessertes Rechtesystem kommt"
- Pilot User rekrutieren
- Support Team briefen

### Tag 0:
- Announcement: "Migration beginnt"
- Support Hotline aktiviert
- Slack Channel #permission-migration

### Tag 7:
- Success Mail: "Migration abgeschlossen"
- Feedback Survey
- Lessons Learned Meeting