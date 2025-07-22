# FC-021 CLAUDE_TECH: Integration Hub

**CLAUDE TECH** | **Original:** 1484 Zeilen → **Optimiert:** 550 Zeilen (63% Reduktion!)  
**Feature-Typ:** 🔀 FULLSTACK | **Priorität:** HOCH | **Geschätzter Aufwand:** 6-8 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**Zentrales Schnittstellen-Management für alle externen Integrationen**

### 🎯 Das macht es:
- **Einheitliches Adapter Pattern**: Alle Integrationen (Xentral, E-Mail, WhatsApp) folgen demselben Interface
- **Automatisches Monitoring**: Real-time Status, Health Checks und Error-Detection in 15 Min statt 2-4h
- **Sichere Credential-Verwaltung**: AES-256 verschlüsselte API-Keys mit automatischer Rotation
- **Resiliente Sync-Engine**: Exponential Backoff, Resume-Capability und Batch-Processing

### 🚀 ROI:
- **80% Zeitersparnis**: Neue Integrationen in 1-2 Tagen statt 5-10 Tagen
- **90% schnellere Fehlerdiagnose**: Sync-Probleme in 15 Min statt 2-4h lokalisieren
- **<10% Code-Duplikation**: Reduzierung von 60-80% durch einheitliches Pattern

### 🏗️ Architektur:
```
Integration Dashboard → Hub Service → Adapter Registry → External APIs
         ↓                 ↓             ↓                  ↓
    Status Monitor    Sync Scheduler  Xentral Adapter    Xentral API
    Health Checker    Event Publisher Email Adapter      SMTP/IMAP
    Credential Mgr    Retry Logic     WhatsApp Adapter   WhatsApp API
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. Integration Hub API:
```java
@Path("/api/integrations")
@Authenticated
public class IntegrationResource {
    
    @Inject IntegrationHubService hubService;
    @Inject IntegrationConfigService configService;
    
    @GET
    @RolesAllowed({"admin", "manager"})
    public List<IntegrationStatusDTO> getAllIntegrations() {
        return hubService.getAllIntegrationStatus();
    }
    
    @GET @Path("/{id}")
    @RolesAllowed({"admin", "manager", "sales"})
    public IntegrationDetailDTO getIntegration(@PathParam("id") UUID id) {
        return hubService.getIntegrationDetail(id);
    }
    
    @POST @Path("/{id}/sync")
    @RolesAllowed({"admin", "manager"})
    public Response triggerSync(@PathParam("id") UUID id) {
        hubService.triggerSync(id);
        return Response.accepted()
                      .header("Location", "/api/integrations/" + id + "/sync-status")
                      .build();
    }
    
    @GET @Path("/{id}/health")
    @RolesAllowed({"admin", "manager"})
    public HealthStatus checkHealth(@PathParam("id") UUID id) {
        return hubService.performHealthCheck(id);
    }
    
    @GET @Path("/{id}/logs")
    @RolesAllowed("admin")
    public List<IntegrationLogDTO> getLogs(
            @PathParam("id") UUID id,
            @QueryParam("limit") @DefaultValue("100") int limit) {
        return hubService.getIntegrationLogs(id, limit, 0);
    }
}
```

#### 2. Adapter Interface & Registry:
```java
// Core Interface - Alle Adapter implementieren dieses
public interface IntegrationAdapter {
    // Lifecycle
    CompletableFuture<Void> connect();
    CompletableFuture<Void> disconnect();
    
    // Operations
    CompletableFuture<SyncResult> sync(SyncConfig config);
    CompletableFuture<HealthStatus> healthCheck();
    
    // Metadata
    IntegrationMetadata getMetadata();
    List<ConfigParameter> getRequiredParameters();
}

// Metadata für UI und Registry
public record IntegrationMetadata(
    String id,
    String name,
    String description,
    String iconUrl,
    List<String> supportedOperations,
    Map<String, String> capabilities
) {}

// Adapter Registry
@ApplicationScoped
public class AdapterRegistry {
    private final Map<String, Class<? extends IntegrationAdapter>> adapters = new HashMap<>();
    
    public void register(String type, Class<? extends IntegrationAdapter> adapterClass) {
        adapters.put(type, adapterClass);
    }
    
    public Class<? extends IntegrationAdapter> getAdapter(String type) {
        return adapters.get(type);
    }
    
    @PostConstruct
    void initializeAdapters() {
        register("xentral", XentralAdapter.class);
        register("email", EmailAdapter.class);
        register("whatsapp", WhatsAppAdapter.class);
    }
}
```

#### 3. Integration Hub Service:
```java
@ApplicationScoped
@Transactional
public class IntegrationHubService {
    
    @Inject AdapterRegistry adapterRegistry;
    @Inject CredentialService credentialService;
    @Inject EventBus eventBus;
    @Inject MeterRegistry metrics;
    
    private final Map<UUID, CompletableFuture<SyncResult>> runningSyncs = new ConcurrentHashMap<>();
    
    public CompletableFuture<SyncResult> triggerSync(UUID integrationId) {
        IntegrationConfig config = IntegrationConfig.findById(integrationId);
        if (config == null) {
            throw new NotFoundException("Integration not found: " + integrationId);
        }
        
        // Check if sync is already running
        if (runningSyncs.containsKey(integrationId)) {
            return runningSyncs.get(integrationId);
        }
        
        // Create and configure adapter
        IntegrationAdapter adapter = createAdapter(config);
        
        // Start sync with metrics and error handling
        CompletableFuture<SyncResult> syncFuture = CompletableFuture
            .supplyAsync(() -> {
                Timer.Sample sample = Timer.start(metrics);
                try {
                    logIntegration(config, LogLevel.INFO, LogType.SYNC_START, 
                                  "Starting sync", Map.of());
                    
                    SyncResult result = adapter.sync(buildSyncConfig(config))
                                              .get(5, TimeUnit.MINUTES);
                    
                    updateSyncState(config, result);
                    logSyncResult(config, result);
                    publishSyncEvent(config, result);
                    
                    return result;
                    
                } catch (Exception e) {
                    handleSyncError(config, e);
                    throw new RuntimeException("Sync failed", e);
                } finally {
                    sample.stop(metrics.timer("integration.sync.duration"));
                }
            })
            .whenComplete((result, error) -> {
                runningSyncs.remove(integrationId);
                config.lastSyncAt = LocalDateTime.now();
                config.status = error == null ? IntegrationStatus.ACTIVE : IntegrationStatus.ERROR;
                config.persist();
            });
        
        runningSyncs.put(integrationId, syncFuture);
        return syncFuture;
    }
    
    @Scheduled(every = "1m")
    void checkScheduledSyncs() {
        List<IntegrationConfig> dueForSync = IntegrationConfig
            .find("autoSync = true AND nextScheduledSync <= ?1 AND status != ?2",
                  LocalDateTime.now(), IntegrationStatus.SYNCING)
            .list();
            
        for (IntegrationConfig config : dueForSync) {
            try {
                triggerSync(config.id);
                config.nextScheduledSync = calculateNextSync(config.syncCronExpression);
                config.persist();
            } catch (Exception e) {
                Log.error("Failed to trigger scheduled sync for " + config.name, e);
            }
        }
    }
}
```

#### 4. Xentral Adapter Example:
```java
@ApplicationScoped
public class XentralAdapter implements IntegrationAdapter {
    
    @Inject @RestClient XentralApiClient xentralClient;
    @ConfigProperty(name = "xentral.base-url") String baseUrl;
    
    private String apiKey;
    private Map<String, Object> config;
    
    @Override
    public void configure(Map<String, String> credentials, Map<String, Object> config) {
        this.apiKey = credentials.get("apiKey");
        this.config = config;
        
        // Configure REST client
        ((ConfigurableRestClient) xentralClient).baseUrl(baseUrl)
                                                .header("X-API-Key", apiKey);
    }
    
    @Override
    public CompletableFuture<SyncResult> sync(SyncConfig syncConfig) {
        return CompletableFuture.supplyAsync(() -> {
            SyncResult result = new SyncResult();
            
            try {
                // Sync Customers
                if (syncConfig.isEntityEnabled("customers")) {
                    SyncResult.EntityResult customerResult = syncCustomers(syncConfig);
                    result.addEntityResult("customers", customerResult);
                }
                
                // Sync Products
                if (syncConfig.isEntityEnabled("products")) {
                    SyncResult.EntityResult productResult = syncProducts(syncConfig);
                    result.addEntityResult("products", productResult);
                }
                
                result.setStatus(SyncStatus.SUCCESS);
                result.setCompletedAt(LocalDateTime.now());
                
            } catch (Exception e) {
                result.setStatus(SyncStatus.ERROR);
                result.setErrorMessage(e.getMessage());
            }
            
            return result;
        });
    }
    
    private SyncResult.EntityResult syncCustomers(SyncConfig config) {
        int page = 0;
        int pageSize = 100;
        int totalProcessed = 0;
        int totalErrors = 0;
        
        while (true) {
            try {
                XentralCustomerPage customerPage = 
                    xentralClient.getCustomers(page, pageSize);
                    
                for (XentralCustomer xentralCustomer : customerPage.data) {
                    try {
                        Customer customer = mapToCustomer(xentralCustomer);
                        customer.persist();
                        totalProcessed++;
                    } catch (Exception e) {
                        totalErrors++;
                        Log.error("Failed to sync customer: " + xentralCustomer.id, e);
                    }
                }
                
                if (!customerPage.hasNextPage) break;
                page++;
                
            } catch (Exception e) {
                throw new SyncException("Customer sync failed at page " + page, e);
            }
        }
        
        return new SyncResult.EntityResult(totalProcessed, totalErrors);
    }
    
    @Override
    public CompletableFuture<HealthStatus> healthCheck() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                XentralHealthResponse health = xentralClient.checkHealth();
                return new HealthStatus(
                    health.status.equals("ok"),
                    health.message,
                    Map.of("version", health.version, "uptime", health.uptime)
                );
            } catch (Exception e) {
                return new HealthStatus(false, "Health check failed: " + e.getMessage());
            }
        });
    }
    
    @Override
    public IntegrationMetadata getMetadata() {
        return new IntegrationMetadata(
            "xentral",
            "Xentral ERP",
            "Synchronisiert Kunden, Produkte und Aufträge mit Xentral",
            "/assets/icons/xentral.svg",
            List.of("customers", "products", "orders", "invoices"),
            Map.of(
                "supportsWebhooks", "true",
                "supportsBidirectional", "true",
                "requiresApiKey", "true"
            )
        );
    }
}
```

#### 5. Database Schema:
```sql
-- V5.0__create_integration_hub_tables.sql

-- Integration Configuration
CREATE TABLE integration_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    adapter_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    encrypted_credentials TEXT,
    configuration JSONB DEFAULT '{}',
    metadata JSONB DEFAULT '{}',
    last_sync_at TIMESTAMP,
    next_scheduled_sync TIMESTAMP,
    sync_cron_expression VARCHAR(100),
    auto_sync BOOLEAN NOT NULL DEFAULT true,
    retry_attempts INTEGER NOT NULL DEFAULT 3,
    retry_delay_seconds INTEGER NOT NULL DEFAULT 60,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_id UUID REFERENCES users(id),
    updated_by_id UUID REFERENCES users(id),
    
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'ERROR', 'SYNCING'))
);

-- Integration Logs
CREATE TABLE integration_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    integration_id UUID NOT NULL REFERENCES integration_config(id) ON DELETE CASCADE,
    level VARCHAR(10) NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    details JSONB DEFAULT '{}',
    records_processed INTEGER,
    records_failed INTEGER,
    duration_ms BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (level IN ('INFO', 'WARN', 'ERROR')),
    CHECK (type IN ('SYNC_START', 'SYNC_COMPLETE', 'SYNC_ERROR', 'HEALTH_CHECK'))
);

-- Sync State for Resume Capability
CREATE TABLE sync_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    integration_id UUID NOT NULL UNIQUE REFERENCES integration_config(id) ON DELETE CASCADE,
    state JSONB DEFAULT '{}',
    last_processed_id VARCHAR(255),
    last_processed_timestamp TIMESTAMP,
    total_records BIGINT,
    processed_records BIGINT,
    sync_status VARCHAR(20) NOT NULL DEFAULT 'IDLE',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CHECK (sync_status IN ('IDLE', 'RUNNING', 'PAUSED', 'ERROR'))
);

-- Performance Indices
CREATE INDEX idx_integration_config_status ON integration_config(status);
CREATE INDEX idx_integration_config_next_sync ON integration_config(next_scheduled_sync) 
    WHERE auto_sync = true;
CREATE INDEX idx_integration_log_integration_timestamp 
    ON integration_log(integration_id, timestamp DESC);
```

### 🎨 Frontend Starter Kit

#### 1. Integration Dashboard:
```typescript
// IntegrationDashboard.tsx
export const IntegrationDashboard: React.FC = () => {
  const { integrations, isLoading, integrationsCount } = useIntegrations();
  const [filterStatus, setFilterStatus] = useState<IntegrationStatus | 'all'>('all');

  const filteredIntegrations = useMemo(() => {
    if (filterStatus === 'all') return integrations;
    return integrations.filter(i => i.status === filterStatus);
  }, [integrations, filterStatus]);

  if (isLoading) {
    return (
      <Box sx={{ p: 3 }}>
        <Skeleton variant="text" width={300} height={40} />
        <Grid container spacing={3} sx={{ mt: 2 }}>
          {[1, 2, 3].map((i) => (
            <Grid item xs={12} md={6} lg={4} key={i}>
              <Skeleton variant="rectangular" height={200} />
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Integration Hub
      </Typography>

      {integrationsCount.error > 0 && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {integrationsCount.error} Integration{integrationsCount.error > 1 ? 'en' : ''} 
          mit Fehlern. Bitte prüfen Sie die betroffenen Services.
        </Alert>
      )}

      <Paper sx={{ mb: 3 }}>
        <Tabs value={filterStatus} onChange={(_, value) => setFilterStatus(value)}>
          <Tab 
            label={<Badge badgeContent={integrationsCount.total}>Alle</Badge>} 
            value="all" 
          />
          <Tab 
            label={<Badge badgeContent={integrationsCount.active} color="success">Aktiv</Badge>} 
            value={IntegrationStatus.ACTIVE} 
          />
          <Tab 
            label={<Badge badgeContent={integrationsCount.syncing} color="info">Synchronisiert</Badge>} 
            value={IntegrationStatus.SYNCING} 
          />
          <Tab 
            label={<Badge badgeContent={integrationsCount.error} color="error">Fehler</Badge>} 
            value={IntegrationStatus.ERROR} 
          />
        </Tabs>
      </Paper>

      <Grid container spacing={3}>
        {filteredIntegrations.map((integration) => (
          <Grid item xs={12} md={6} lg={4} key={integration.id}>
            <IntegrationCard integration={integration} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};
```

#### 2. Integration Card:
```typescript
// IntegrationCard.tsx
export const IntegrationCard: React.FC<{ integration: Integration }> = ({ integration }) => {
  const router = useRouter();
  const { triggerSync, isSyncing } = useSyncTrigger(integration.id);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);

  const statusConfig = {
    [IntegrationStatus.ACTIVE]: { icon: <CheckCircle />, color: 'success', label: 'Aktiv' },
    [IntegrationStatus.ERROR]: { icon: <Error />, color: 'error', label: 'Fehler' },
    [IntegrationStatus.SYNCING]: { icon: <HourglassEmpty />, color: 'info', label: 'Synchronisiert' },
    [IntegrationStatus.INACTIVE]: { icon: <Schedule />, color: 'default', label: 'Inaktiv' },
  };

  const status = statusConfig[integration.status];

  return (
    <Card sx={{ 
      height: '100%', 
      display: 'flex', 
      flexDirection: 'column',
      ...(integration.status === IntegrationStatus.SYNCING && {
        '&::before': {
          content: '""',
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: 3,
          bgcolor: 'info.main',
          animation: 'pulse 2s infinite',
        },
      }),
    }}>
      {integration.status === IntegrationStatus.SYNCING && (
        <LinearProgress sx={{ position: 'absolute', top: 0, left: 0, right: 0 }} />
      )}

      <CardContent sx={{ flex: 1 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
          <Box>
            <Typography variant="h6" gutterBottom>
              {integration.name}
            </Typography>
            <Chip
              icon={status.icon}
              label={status.label}
              color={status.color}
              size="small"
              sx={{ mb: 2 }}
            />
          </Box>
          <IconButton size="small" onClick={(e) => setAnchorEl(e.currentTarget)}>
            <MoreVert />
          </IconButton>
        </Stack>

        <Stack spacing={1}>
          <HealthStatusBadge integrationId={integration.id} />
          
          {integration.lastSyncAt && (
            <Typography variant="body2" color="text.secondary">
              Letzter Sync: {formatDistance(
                new Date(integration.lastSyncAt),
                new Date(),
                { addSuffix: true, locale: de }
              )}
            </Typography>
          )}

          {integration.nextScheduledSync && integration.autoSync && (
            <Typography variant="body2" color="text.secondary">
              Nächster Sync: {formatDistance(
                new Date(integration.nextScheduledSync),
                new Date(),
                { addSuffix: true, locale: de }
              )}
            </Typography>
          )}
        </Stack>
      </CardContent>

      <CardActions>
        <Button
          startIcon={<Sync />}
          onClick={() => triggerSync()}
          disabled={isSyncing || integration.status === IntegrationStatus.SYNCING}
          fullWidth
        >
          {isSyncing || integration.status === IntegrationStatus.SYNCING 
            ? 'Synchronisiert...' 
            : 'Jetzt synchronisieren'}
        </Button>
      </CardActions>

      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
        <MenuItem onClick={() => router.push(`/integrations/${integration.id}`)}>
          Details anzeigen
        </MenuItem>
        <MenuItem onClick={() => router.push(`/integrations/${integration.id}/edit`)}>
          Konfiguration bearbeiten
        </MenuItem>
      </Menu>
    </Card>
  );
};
```

#### 3. React Query Hooks:
```typescript
// useIntegrations.ts
export const useIntegrations = () => {
  const { data, error, mutate } = useSWR<Integration[]>(
    '/api/integrations',
    apiClient.get,
    {
      refreshInterval: 10000, // Poll every 10 seconds
      revalidateOnFocus: true,
    }
  );

  const integrationsCount = {
    total: data?.length || 0,
    active: data?.filter(i => i.status === IntegrationStatus.ACTIVE).length || 0,
    error: data?.filter(i => i.status === IntegrationStatus.ERROR).length || 0,
    syncing: data?.filter(i => i.status === IntegrationStatus.SYNCING).length || 0,
  };

  return {
    integrations: data || [],
    isLoading: !error && !data,
    isError: error,
    integrationsCount,
    mutate,
  };
};

// useSyncTrigger.ts
export const useSyncTrigger = (integrationId: string) => {
  const { trigger, isMutating } = useSWRMutation(
    `/api/integrations/${integrationId}/sync`,
    (url) => apiClient.post(url),
    {
      onSuccess: () => toast.success('Sync gestartet'),
      onError: (error) => toast.error(`Sync fehlgeschlagen: ${error.message}`),
    }
  );

  return {
    triggerSync: trigger,
    isSyncing: isMutating,
  };
};

// useIntegrationDetail.ts
export const useIntegrationDetail = (integrationId: string) => {
  const { data: integration } = useSWR<IntegrationDetail>(
    integrationId ? `/api/integrations/${integrationId}` : null,
    apiClient.get
  );

  const { data: logs } = useSWR<IntegrationLog[]>(
    integrationId ? `/api/integrations/${integrationId}/logs` : null,
    apiClient.get,
    {
      refreshInterval: integration?.status === 'SYNCING' ? 2000 : 0,
    }
  );

  return {
    integration,
    logs: logs || [],
    isLoading: !integration,
  };
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Core Infrastructure (3 Tage)
1. **Backend Foundation** (1.5 Tage)
   - Database Schema + Entity Classes
   - IntegrationAdapter Interface + Registry
   - IntegrationHubService Grundgerüst
   - REST Endpoints + Credential Encryption

2. **Frontend Foundation** (1.5 Tage)
   - Component Structure + TypeScript Types
   - Integration Dashboard UI + Cards
   - API Client + Basic Navigation

### Phase 2: First Adapter & Monitoring (2 Tage)
1. **Xentral Adapter** (1 Tag)
   - XentralAdapter Implementation
   - Customer + Product Sync Logic
   - Error Handling + Retry Logic

2. **Monitoring & Logging** (1 Tag)
   - Sync Status Tracking + Logs UI
   - Real-time Status Updates
   - Health Checks + Error Notifications

### Phase 3: Advanced Features (2 Tage)
1. **Automation** (1 Tag)
   - Scheduled Sync + Cron Expression Support
   - Auto-retry on Errors + Resume Capability

2. **Polish & Testing** (1 Tag)
   - Integration Tests + UI Polish
   - Performance Optimization + Documentation

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **80% Zeitersparnis**: Neue Integrationen in 1-2 Tagen statt 5-10 Tagen
- **90% schnellere Fehlerdiagnose**: Sync-Probleme in 15 Min statt 2-4h
- **<10% Code-Duplikation**: Reduzierung von 60-80% durch einheitliches Pattern
- **Proaktives Fehlermanagement**: Automatische Retry-Logic + Error-Alerting

### Technische Vorteile:
- **Einheitliche Architektur**: Alle Integrationen folgen demselben Pattern
- **Automatisches Monitoring**: Real-time Status + Health Checks
- **Sichere Credential-Verwaltung**: AES-256 Verschlüsselung
- **Resilience**: Exponential Backoff + Resume-Capability

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **FC-008 Security Foundation**: Credential Encryption + User Authentication
- **FC-023 Event Sourcing**: Domain Events für Integration History

### Enables:
- **FC-005 Xentral Integration**: Als erster konkreter Adapter
- **FC-003 E-Mail Integration**: SMTP/IMAP Adapter
- **FC-028 WhatsApp Business**: WhatsApp API Adapter

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **Adapter Pattern**: Zentrale Abstraktion für alle Integrationen (einheitlich, erweiterbar)
2. **Sync Strategy**: Hybrid Pull + Webhook Support (Balance zwischen Aktualität + Ressourcen)
3. **Credential Storage**: AES-256 in DB (sicher genug, schneller Zugriff für Auto-Syncs)
4. **Monitoring**: Real-time Status mit 10s Polling + Event-driven Updates

---

**Status:** Ready for Implementation | **Phase 1:** Database Schema starten | **Next:** Xentral API studieren