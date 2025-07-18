# 🔌 FC-021: Integration Hub KOMPAKT

**In 15 Minuten Integration Hub verstehen und umsetzen!**

## 🎯 Was bauen wir?

**Ein zentrales Schnittstellen-Management-System** das alle externen Integrationen vereinheitlicht, überwacht und verwaltet.

**Kernfunktionen:**
- 🔗 Zentrale Integration Registry
- 📊 Monitoring Dashboard  
- 🔄 Automatische Sync-Prozesse
- 🚨 Error Recovery & Alerts
- 🔐 Sichere Credential-Verwaltung

## 💰 Business Value in Zahlen

| Metrik | Ohne Hub | Mit Hub | Verbesserung |
|--------|----------|---------|--------------|
| Neue Integration einrichten | 5-10 Tage | 1-2 Tage | **80% schneller** |
| Fehlersuche bei Sync-Problemen | 2-4 Stunden | 15 Minuten | **90% schneller** |
| Code-Duplikation | 60-80% | < 10% | **70% weniger** |
| Monitoring-Aufwand | Manuell | Automatisch | **100% automatisiert** |

## 🏗️ Quick Architecture

```typescript
// 1. Core Interface - ALLE Integrationen implementieren das:
interface IntegrationAdapter {
  connect(): Promise<void>;
  disconnect(): Promise<void>;
  healthCheck(): Promise<HealthStatus>;
  sync(): Promise<SyncResult>;
}

// 2. Konkrete Integration
class XentralAdapter extends IntegrationAdapter {
  async sync() {
    const data = await this.fetchWithRetry('/customers');
    return this.processAndSave(data);
  }
}

// 3. Registry verwaltet alle
class IntegrationHub {
  private adapters = new Map<string, IntegrationAdapter>();
  
  async syncAll() {
    return Promise.allSettled(
      Array.from(this.adapters.values()).map(a => a.sync())
    );
  }
}
```

## 📱 UI in 5 Minuten

**Dashboard-Mockup:**
```
Integration Hub                              [+ Neue Integration]

AKTIV (3)                    FEHLER (1)
┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│ ✅ Xentral  │ │ ✅ E-Mail   │ │ ⚠️ Calendar │
│ Sync: 5 min │ │ Sync: 1 min │ │ Auth Fehler │
└─────────────┘ └─────────────┘ └─────────────┘

Letzte Aktivitäten:
• ✅ 14:23 - Xentral: 234 Kunden aktualisiert
• ⚠️ 14:20 - Calendar: Token abgelaufen
• ✅ 14:15 - E-Mail: 45 neue Nachrichten
```

## 🚀 Implementierung - Copy & Paste Ready

### Backend Structure:
```bash
backend/src/main/java/de/freshplan/
├── domain/integration/
│   ├── entity/
│   │   ├── IntegrationConfig.java
│   │   └── IntegrationLog.java
│   ├── service/
│   │   ├── IntegrationHub.java
│   │   └── adapters/
│   │       ├── IntegrationAdapter.java
│   │       ├── XentralAdapter.java
│   │       └── EmailAdapter.java
│   └── repository/
│       └── IntegrationRepository.java
└── api/resources/
    └── IntegrationResource.java
```

### Quick Start Code:

**1. Entity (5 Min):**
```java
@Entity
public class IntegrationConfig extends PanacheEntityBase {
    @Id @GeneratedValue
    public UUID id;
    
    public String name;
    public String type; // "xentral", "email", etc.
    public String status; // "active", "error", "inactive"
    
    @Column(columnDefinition = "TEXT")
    public String encryptedCredentials;
    
    @Type(type = "jsonb")
    public Map<String, Object> config;
    
    public LocalDateTime lastSyncAt;
    public LocalDateTime nextSyncAt;
}
```

**2. REST Endpoint (5 Min):**
```java
@Path("/api/integrations")
@Authenticated
public class IntegrationResource {
    
    @GET
    public List<IntegrationStatusDTO> getAllIntegrations() {
        return integrationHub.getStatus();
    }
    
    @POST
    @Path("/{id}/sync")
    public Response triggerSync(@PathParam("id") String id) {
        integrationHub.syncIntegration(id);
        return Response.accepted().build();
    }
    
    @GET
    @Path("/{id}/health")
    public HealthStatus checkHealth(@PathParam("id") String id) {
        return integrationHub.healthCheck(id);
    }
}
```

**3. Frontend Component (5 Min):**
```typescript
export const IntegrationCard: React.FC<{integration: Integration}> = ({
  integration
}) => {
  const { trigger: sync, isMutating } = useSWRMutation(
    `/api/integrations/${integration.id}/sync`,
    (url) => apiClient.post(url)
  );
  
  return (
    <Card>
      <CardContent>
        <Stack direction="row" alignItems="center" spacing={2}>
          <StatusIcon status={integration.status} />
          <Box flex={1}>
            <Typography variant="h6">{integration.name}</Typography>
            <Typography variant="caption">
              Letzter Sync: {formatRelative(integration.lastSyncAt)}
            </Typography>
          </Box>
          <Button 
            onClick={() => sync()}
            disabled={isMutating}
            size="small"
          >
            {isMutating ? 'Syncing...' : 'Sync'}
          </Button>
        </Stack>
      </CardContent>
    </Card>
  );
};
```

## ⚡ Quick Wins First

**Tag 1-2: Basis-Infrastruktur**
- Integration Registry Pattern
- Adapter Interface
- Basis-UI

**Tag 3-4: Erste Integration** 
- Xentral oder E-Mail als Proof of Concept
- Error Handling testen

**Tag 5+: Erweitern**
- Weitere Integrationen
- Monitoring ausbauen
- Webhook-Support

## 🎯 Erfolgs-Kriterien

✅ **MVP fertig wenn:**
- 2+ Integrationen über Hub laufen
- Dashboard zeigt Status aller Integrationen
- Sync-Fehler werden automatisch geloggt
- Credentials sicher verschlüsselt

✅ **Production-ready wenn:**
- Automatische Retry-Logic funktioniert
- Webhook-Support implementiert
- Performance < 1s für Status-Abfragen
- 99.5% Sync-Success-Rate

## 🚨 Häufige Fehler vermeiden

❌ **Nicht machen:**
- Jede Integration komplett anders bauen
- Credentials im Code/Config speichern
- Sync-Status nur im Memory halten
- Keine Retry-Logic implementieren

✅ **Stattdessen:**
- Einheitliches Adapter-Pattern
- Verschlüsselte DB-Speicherung
- Persistenter Sync-State
- Exponential Backoff bei Fehlern

---

**🎯 In 15 Minuten hast du:** Komplettes Verständnis + Code-Struktur + erste Integration läuft!