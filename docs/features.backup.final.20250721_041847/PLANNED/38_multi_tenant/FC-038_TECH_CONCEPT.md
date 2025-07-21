# 🏢 FC-038: Multi-Tenant Architecture - TECH CONCEPT

**Feature-Code:** FC-038  
**Feature-Name:** Multi-Tenant Architecture  
**Kategorie:** Infrastructure & Skalierung  
**Priorität:** HIGH  
**Geschätzter Aufwand:** 10 Tage  
**Status:** 📋 PLANNED - Tech Concept verfügbar  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### ⚡ SOFORT STARTEN (2 Minuten):
```bash
# 1. Multi-Tenant Backend Setup
cd backend/src/main/java/de/freshplan/infrastructure
mkdir multitenant && cd multitenant
touch TenantResolver.java TenantContext.java DatabaseRouter.java

# 2. Frontend Tenant Management
cd frontend/src/features
mkdir tenant-management && cd tenant-management
touch TenantSelector.tsx TenantSettings.tsx TenantDashboard.tsx
```

### 📋 IMPLEMENTIERUNGS-CHECKLISTE:
- [ ] **Phase 1:** Tenant Database Schema & Isolation (Tag 1-3)
- [ ] **Phase 2:** Request Routing & Context Management (Tag 4-5)  
- [ ] **Phase 3:** Tenant Configuration & Onboarding (Tag 6-7)
- [ ] **Phase 4:** Performance & Monitoring (Tag 8-10)

---

## 🎯 FEATURE OVERVIEW

### Was ist Multi-Tenant Architecture?
Skalierbare Mandantenfähigkeit für FreshPlan, die es ermöglicht, mehrere Unternehmen in einer einzigen Anwendungsinstanz zu betreiben. Komplette Datenisolation, individuelle Konfigurationen und zentrale Verwaltung bei optimaler Resource-Nutzung.

### Business Value
- **Skalierung auf Enterprise-Level** mit hunderten Mandanten in einer Instanz
- **Kosteneffizienz** durch geteilte Infrastruktur bei vollständiger Datenisolation
- **Schnelles Onboarding** neuer Unternehmen ohne technischen Overhead
- **Zentrale Wartung** und Updates für alle Mandanten gleichzeitig

### Erfolgsmetriken
- 100+ gleichzeitige Mandanten ohne Performance-Verlust
- 5-Minuten Onboarding für neue Mandanten
- 99.9% Daten-Isolation zwischen Mandanten
- 50% reduzierte Infrastruktur-Kosten vs. separate Instanzen

---

## 🏗️ TENANT ISOLATION ARCHITECTURE

### Database Schema Design
```sql
-- Tenant Master Table
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    database_schema VARCHAR(100) NOT NULL,
    settings JSONB NOT NULL DEFAULT '{}',
    subscription_tier VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT valid_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'TRIAL')),
    CONSTRAINT valid_subdomain CHECK (subdomain ~ '^[a-z0-9-]+$')
);

-- Schema-based Isolation für jeden Tenant
CREATE SCHEMA tenant_freshplan_demo;
CREATE SCHEMA tenant_catering_company;
CREATE SCHEMA tenant_food_service;

-- Pro Tenant: Vollständige Datenstruktur
CREATE TABLE tenant_freshplan_demo.customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    sales_rep_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_tenant CHECK (tenant_id = 'tenant-specific-uuid')
);

-- Row-Level Security für Extra-Schutz
ALTER TABLE tenant_freshplan_demo.customers ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON tenant_freshplan_demo.customers
    FOR ALL TO application_role
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);
```

### Tenant Resolution Service
```java
@ApplicationScoped
public class TenantResolver {
    
    @Inject
    TenantRepository tenantRepository;
    
    @Inject
    Instance<HttpServletRequest> requestInstance;
    
    public Optional<TenantContext> resolveTenant() {
        HttpServletRequest request = requestInstance.get();
        
        // 1. Subdomain-based Resolution (Primary)
        String host = request.getHeader("Host");
        if (host != null) {
            String subdomain = extractSubdomain(host);
            if (subdomain != null) {
                return resolveTenantBySubdomain(subdomain);
            }
        }
        
        // 2. Header-based Resolution (API/Mobile)
        String tenantHeader = request.getHeader("X-Tenant-ID");
        if (tenantHeader != null) {
            return resolveTenantById(UUID.fromString(tenantHeader));
        }
        
        // 3. Domain-based Resolution (Custom Domains)
        return resolveTenantByDomain(host);
    }
    
    private Optional<TenantContext> resolveTenantBySubdomain(String subdomain) {
        return tenantRepository.findBySubdomain(subdomain)
            .map(this::createTenantContext);
    }
    
    private String extractSubdomain(String host) {
        // freshplan-demo.freshplan.app -> freshplan-demo
        // demo.localhost:5173 -> demo
        
        if (host.contains("localhost")) {
            String[] parts = host.split("\\.");
            return parts.length > 1 ? parts[0] : null;
        }
        
        if (host.endsWith(".freshplan.app")) {
            return host.substring(0, host.indexOf(".freshplan.app"));
        }
        
        return null;
    }
    
    private TenantContext createTenantContext(Tenant tenant) {
        return TenantContext.builder()
            .tenantId(tenant.getId())
            .name(tenant.getName())
            .subdomain(tenant.getSubdomain())
            .databaseSchema(tenant.getDatabaseSchema())
            .settings(tenant.getSettings())
            .subscriptionTier(tenant.getSubscriptionTier())
            .build();
    }
}
```

### Request Context Management
```java
@ApplicationScoped
public class TenantContextManager {
    
    private static final ThreadLocal<TenantContext> CURRENT_TENANT = new ThreadLocal<>();
    
    public void setCurrentTenant(TenantContext tenant) {
        CURRENT_TENANT.set(tenant);
        
        // Set database session variable für Row-Level Security
        if (tenant != null) {
            setDatabaseTenantContext(tenant.getTenantId());
        }
    }
    
    public TenantContext getCurrentTenant() {
        TenantContext tenant = CURRENT_TENANT.get();
        if (tenant == null) {
            throw new TenantContextException("No tenant context available");
        }
        return tenant;
    }
    
    public void clearCurrentTenant() {
        CURRENT_TENANT.remove();
        clearDatabaseTenantContext();
    }
    
    @Transactional
    void setDatabaseTenantContext(UUID tenantId) {
        entityManager.createNativeQuery(
            "SELECT set_config('app.current_tenant_id', ?1, true)"
        ).setParameter(1, tenantId.toString())
         .executeUpdate();
         
        entityManager.createNativeQuery(
            "SET search_path TO " + getCurrentTenant().getDatabaseSchema() + ", public"
        ).executeUpdate();
    }
    
    void clearDatabaseTenantContext() {
        try {
            entityManager.createNativeQuery(
                "SELECT set_config('app.current_tenant_id', null, true)"
            ).executeUpdate();
            
            entityManager.createNativeQuery(
                "SET search_path TO public"
            ).executeUpdate();
        } catch (Exception e) {
            Log.warn("Failed to clear database tenant context", e);
        }
    }
}
```

---

## 🔀 REQUEST ROUTING & FILTERING

### Tenant Filter Implementation
```java
@WebFilter(urlPatterns = "/api/*")
@Priority(100) // High priority - execute early
public class TenantContextFilter implements Filter {
    
    @Inject
    TenantResolver tenantResolver;
    
    @Inject
    TenantContextManager contextManager;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Skip tenant resolution for public endpoints
            if (isPublicEndpoint(httpRequest.getRequestURI())) {
                chain.doFilter(request, response);
                return;
            }
            
            // Resolve tenant from request
            Optional<TenantContext> tenantOpt = tenantResolver.resolveTenant();
            
            if (tenantOpt.isEmpty()) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("{\"error\":\"No valid tenant context\"}");
                return;
            }
            
            TenantContext tenant = tenantOpt.get();
            
            // Validate tenant status
            if (tenant.getStatus() != TenantStatus.ACTIVE) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.getWriter().write(String.format(
                    "{\"error\":\"Tenant %s is %s\"}", 
                    tenant.getName(), 
                    tenant.getStatus()
                ));
                return;
            }
            
            // Set tenant context for current request
            contextManager.setCurrentTenant(tenant);
            
            // Add tenant info to response headers
            httpResponse.setHeader("X-Tenant-ID", tenant.getTenantId().toString());
            httpResponse.setHeader("X-Tenant-Name", tenant.getName());
            
            // Continue with request processing
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            Log.error("Tenant context resolution failed", e);
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write("{\"error\":\"Tenant resolution failed\"}");
        } finally {
            // Always clear context after request
            contextManager.clearCurrentTenant();
        }
    }
    
    private boolean isPublicEndpoint(String uri) {
        List<String> publicEndpoints = List.of(
            "/api/health", "/api/metrics", "/api/auth/login",
            "/api/tenant/register", "/api/docs"
        );
        
        return publicEndpoints.stream()
            .anyMatch(uri::startsWith);
    }
}
```

### Database Connection Routing
```java
@ApplicationScoped
public class TenantDataSourceRouter {
    
    @Inject
    TenantContextManager contextManager;
    
    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String baseJdbcUrl;
    
    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUsername;
    
    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPassword;
    
    // Connection pool per tenant schema
    private final Map<String, HikariDataSource> tenantDataSources = new ConcurrentHashMap<>();
    
    public DataSource getTenantDataSource() {
        TenantContext tenant = contextManager.getCurrentTenant();
        return getTenantDataSource(tenant.getDatabaseSchema());
    }
    
    public DataSource getTenantDataSource(String schema) {
        return tenantDataSources.computeIfAbsent(schema, this::createTenantDataSource);
    }
    
    private HikariDataSource createTenantDataSource(String schema) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(baseJdbcUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setSchema(schema);
        
        // Connection pool configuration per tenant
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Set default schema for all connections
        config.setConnectionInitSql("SET search_path TO " + schema + ", public");
        
        config.setPoolName("TenantPool-" + schema);
        
        return new HikariDataSource(config);
    }
    
    @PreDestroy
    void closeTenantDataSources() {
        tenantDataSources.values().forEach(HikariDataSource::close);
        tenantDataSources.clear();
    }
}
```

---

## 🎨 FRONTEND TENANT MANAGEMENT

### Tenant Selector Component
```typescript
export const TenantSelector: React.FC<TenantSelectorProps> = ({
  onTenantChange,
  currentTenant
}) => {
  const [availableTenants, setAvailableTenants] = useState<Tenant[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const { user } = useAuth();
  
  useEffect(() => {
    loadUserTenants();
  }, [user]);
  
  const loadUserTenants = async () => {
    setIsLoading(true);
    try {
      const tenants = await tenantApi.getUserTenants();
      setAvailableTenants(tenants);
    } catch (error) {
      toast.error('Fehler beim Laden der Mandanten');
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleTenantSwitch = async (tenantId: string) => {
    try {
      const tenant = availableTenants.find(t => t.id === tenantId);
      if (!tenant) return;
      
      // Update tenant context
      await tenantApi.switchTenant(tenantId);
      
      // Redirect to tenant subdomain
      const newUrl = `https://${tenant.subdomain}.freshplan.app${window.location.pathname}`;
      window.location.href = newUrl;
      
      onTenantChange(tenant);
    } catch (error) {
      toast.error('Fehler beim Wechseln des Mandanten');
    }
  };
  
  if (availableTenants.length <= 1) {
    return null; // Single tenant - no selector needed
  }
  
  return (
    <FormControl size="small" sx={{ minWidth: 200 }}>
      <InputLabel>Mandant</InputLabel>
      <Select
        value={currentTenant?.id || ''}
        onChange={(e) => handleTenantSwitch(e.target.value)}
        disabled={isLoading}
        label="Mandant"
        startAdornment={
          <BusinessIcon sx={{ color: '#004F7B', mr: 1 }} />
        }
      >
        {availableTenants.map(tenant => (
          <MenuItem key={tenant.id} value={tenant.id}>
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Box
                sx={{
                  width: 8,
                  height: 8,
                  borderRadius: '50%',
                  bgcolor: tenant.status === 'ACTIVE' ? '#94C456' : '#FFA726',
                  mr: 1
                }}
              />
              <Typography variant="body2">
                {tenant.name}
              </Typography>
              <Chip
                size="small"
                label={tenant.subscriptionTier}
                sx={{ ml: 1, fontSize: '0.7rem' }}
                color={tenant.subscriptionTier === 'ENTERPRISE' ? 'primary' : 'default'}
              />
            </Box>
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
```

### Tenant Context Hook
```typescript
interface TenantContextValue {
  currentTenant: Tenant | null;
  isLoading: boolean;
  switchTenant: (tenantId: string) => Promise<void>;
  refreshTenant: () => Promise<void>;
}

const TenantContext = createContext<TenantContextValue | null>(null);

export const TenantProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [currentTenant, setCurrentTenant] = useState<Tenant | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  
  useEffect(() => {
    initializeTenant();
  }, []);
  
  const initializeTenant = async () => {
    try {
      // Resolve tenant from subdomain
      const hostname = window.location.hostname;
      const subdomain = extractSubdomain(hostname);
      
      if (subdomain) {
        const tenant = await tenantApi.getTenantBySubdomain(subdomain);
        setCurrentTenant(tenant);
        
        // Store tenant context globally
        axios.defaults.headers.common['X-Tenant-ID'] = tenant.id;
      }
    } catch (error) {
      console.error('Failed to initialize tenant context', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const switchTenant = async (tenantId: string) => {
    const tenant = await tenantApi.getTenant(tenantId);
    
    // Update API client tenant context
    axios.defaults.headers.common['X-Tenant-ID'] = tenantId;
    
    setCurrentTenant(tenant);
  };
  
  const refreshTenant = async () => {
    if (currentTenant) {
      const refreshed = await tenantApi.getTenant(currentTenant.id);
      setCurrentTenant(refreshed);
    }
  };
  
  const value: TenantContextValue = {
    currentTenant,
    isLoading,
    switchTenant,
    refreshTenant
  };
  
  return (
    <TenantContext.Provider value={value}>
      {children}
    </TenantContext.Provider>
  );
};

export const useTenant = (): TenantContextValue => {
  const context = useContext(TenantContext);
  if (!context) {
    throw new Error('useTenant must be used within TenantProvider');
  }
  return context;
};

function extractSubdomain(hostname: string): string | null {
  if (hostname.includes('localhost')) {
    // development: demo.localhost:5173 -> demo
    const parts = hostname.split('.');
    return parts.length > 1 ? parts[0] : null;
  }
  
  if (hostname.endsWith('.freshplan.app')) {
    // production: freshplan-demo.freshplan.app -> freshplan-demo
    return hostname.substring(0, hostname.indexOf('.freshplan.app'));
  }
  
  return null;
}
```

---

## 🚀 TENANT ONBOARDING & MANAGEMENT

### Automated Tenant Provisioning
```java
@ApplicationScoped
public class TenantProvisioningService {
    
    @Inject
    TenantRepository tenantRepository;
    
    @Inject
    @ConfigProperty(name = "tenant.default.database.url")
    String defaultDatabaseUrl;
    
    @Transactional
    public CompletionStage<Tenant> provisionNewTenant(TenantProvisioningRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.info("Starting tenant provisioning for: " + request.getCompanyName());
                
                // 1. Validate subdomain availability
                validateSubdomainAvailability(request.getSubdomain());
                
                // 2. Create tenant record
                Tenant tenant = createTenantRecord(request);
                
                // 3. Create database schema
                String schemaName = createDatabaseSchema(tenant);
                
                // 4. Initialize tenant data
                initializeTenantData(tenant, request);
                
                // 5. Setup default configurations
                setupDefaultConfigurations(tenant, request);
                
                // 6. Create initial admin user
                createInitialAdminUser(tenant, request);
                
                // 7. Send welcome email
                sendWelcomeEmail(tenant, request);
                
                Log.info("Successfully provisioned tenant: " + tenant.getId());
                return tenant;
                
            } catch (Exception e) {
                Log.error("Tenant provisioning failed for: " + request.getCompanyName(), e);
                // Cleanup partial provisioning
                rollbackTenantProvisioning(request.getSubdomain());
                throw new TenantProvisioningException("Failed to provision tenant", e);
            }
        });
    }
    
    private void validateSubdomainAvailability(String subdomain) {
        if (tenantRepository.findBySubdomain(subdomain).isPresent()) {
            throw new SubdomainAlreadyExistsException("Subdomain already exists: " + subdomain);
        }
        
        // Validate subdomain format
        if (!subdomain.matches("^[a-z0-9-]{2,30}$")) {
            throw new InvalidSubdomainException("Invalid subdomain format: " + subdomain);
        }
        
        // Block reserved subdomains
        List<String> reserved = List.of("www", "api", "admin", "app", "mail", "ftp");
        if (reserved.contains(subdomain)) {
            throw new InvalidSubdomainException("Subdomain is reserved: " + subdomain);
        }
    }
    
    private Tenant createTenantRecord(TenantProvisioningRequest request) {
        Tenant tenant = Tenant.builder()
            .name(request.getCompanyName())
            .subdomain(request.getSubdomain())
            .status(TenantStatus.TRIAL)
            .databaseSchema("tenant_" + request.getSubdomain().replace("-", "_"))
            .subscriptionTier(request.getSubscriptionTier())
            .settings(createDefaultSettings(request))
            .build();
        
        return tenantRepository.save(tenant);
    }
    
    @Transactional
    void createDatabaseSchema(Tenant tenant) {
        String schemaName = tenant.getDatabaseSchema();
        
        // Create schema
        entityManager.createNativeQuery(
            "CREATE SCHEMA IF NOT EXISTS " + schemaName
        ).executeUpdate();
        
        // Create all tenant tables
        List<String> tableCreationSQL = loadTenantTableSchemas();
        
        for (String sql : tableCreationSQL) {
            String tenantSQL = sql.replace("${schema}", schemaName);
            entityManager.createNativeQuery(tenantSQL).executeUpdate();
        }
        
        // Set up row-level security
        setupRowLevelSecurity(schemaName, tenant.getId());
        
        Log.info("Created database schema: " + schemaName);
    }
    
    private void initializeTenantData(Tenant tenant, TenantProvisioningRequest request) {
        // Sample customers for demo
        if (request.isIncludeSampleData()) {
            createSampleCustomers(tenant);
            createSampleOpportunities(tenant);
            createSampleReports(tenant);
        }
        
        // Default settings and configurations
        createDefaultUserRoles(tenant);
        createDefaultEmailTemplates(tenant);
        createDefaultReportTemplates(tenant);
    }
    
    private void createInitialAdminUser(Tenant tenant, TenantProvisioningRequest request) {
        UserRequest adminRequest = UserRequest.builder()
            .username(request.getAdminEmail())
            .email(request.getAdminEmail())
            .firstName(request.getAdminFirstName())
            .lastName(request.getAdminLastName())
            .role("TENANT_ADMIN")
            .tenantId(tenant.getId())
            .isActive(true)
            .build();
        
        userService.createTenantAdminUser(adminRequest);
        
        // Send password setup email
        passwordResetService.sendPasswordSetupEmail(
            request.getAdminEmail(), 
            tenant.getName()
        );
    }
}
```

### Tenant Configuration Management
```typescript
export const TenantSettingsPage: React.FC = () => {
  const { currentTenant, refreshTenant } = useTenant();
  const [settings, setSettings] = useState<TenantSettings | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  
  useEffect(() => {
    loadTenantSettings();
  }, [currentTenant]);
  
  const loadTenantSettings = async () => {
    if (!currentTenant) return;
    
    try {
      const tenantSettings = await tenantApi.getTenantSettings(currentTenant.id);
      setSettings(tenantSettings);
    } catch (error) {
      toast.error('Fehler beim Laden der Mandanten-Einstellungen');
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleSaveSettings = async (updatedSettings: Partial<TenantSettings>) => {
    if (!currentTenant || !settings) return;
    
    setIsSaving(true);
    try {
      const newSettings = { ...settings, ...updatedSettings };
      await tenantApi.updateTenantSettings(currentTenant.id, newSettings);
      setSettings(newSettings);
      await refreshTenant();
      toast.success('Einstellungen erfolgreich gespeichert');
    } catch (error) {
      toast.error('Fehler beim Speichern der Einstellungen');
    } finally {
      setIsSaving(false);
    }
  };
  
  if (isLoading) {
    return <TenantSettingsSkeleton />;
  }
  
  if (!settings) {
    return (
      <Alert severity="error">
        Mandanten-Einstellungen konnten nicht geladen werden.
      </Alert>
    );
  }
  
  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography 
        variant="h4" 
        fontFamily="Antonio Bold" 
        gutterBottom
        sx={{ color: '#004F7B' }}
      >
        Mandanten-Einstellungen
      </Typography>
      
      <Grid container spacing={3}>
        {/* Grundeinstellungen */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader title="Grundeinstellungen" />
            <CardContent>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Firmenname"
                    value={settings.companyName}
                    onChange={(e) => handleSaveSettings({ 
                      companyName: e.target.value 
                    })}
                    disabled={isSaving}
                  />
                </Grid>
                
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Subdomain"
                    value={currentTenant.subdomain}
                    disabled
                    helperText="Subdomain kann nach der Erstellung nicht geändert werden"
                  />
                </Grid>
                
                <Grid item xs={6}>
                  <FormControl fullWidth>
                    <InputLabel>Zeitzone</InputLabel>
                    <Select
                      value={settings.timezone}
                      onChange={(e) => handleSaveSettings({ 
                        timezone: e.target.value 
                      })}
                    >
                      <MenuItem value="Europe/Berlin">Europe/Berlin</MenuItem>
                      <MenuItem value="Europe/Vienna">Europe/Vienna</MenuItem>
                      <MenuItem value="Europe/Zurich">Europe/Zurich</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                
                <Grid item xs={6}>
                  <FormControl fullWidth>
                    <InputLabel>Währung</InputLabel>
                    <Select
                      value={settings.currency}
                      onChange={(e) => handleSaveSettings({ 
                        currency: e.target.value 
                      })}
                    >
                      <MenuItem value="EUR">EUR (€)</MenuItem>
                      <MenuItem value="CHF">CHF (Fr.)</MenuItem>
                      <MenuItem value="USD">USD ($)</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Abonnement-Informationen */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader 
              title="Abonnement" 
              action={
                <Chip
                  label={currentTenant.subscriptionTier}
                  color={currentTenant.subscriptionTier === 'ENTERPRISE' ? 'primary' : 'default'}
                />
              }
            />
            <CardContent>
              <Box sx={{ mb: 2 }}>
                <Typography variant="body2" color="text.secondary">
                  Status: {currentTenant.status}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Benutzer: {settings.userCount} / {settings.maxUsers}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Speicher: {formatBytes(settings.storageUsed)} / {formatBytes(settings.maxStorage)}
                </Typography>
              </Box>
              
              <LinearProgress
                variant="determinate"
                value={(settings.userCount / settings.maxUsers) * 100}
                sx={{ mb: 1 }}
              />
              
              <LinearProgress
                variant="determinate"
                value={(settings.storageUsed / settings.maxStorage) * 100}
              />
              
              <Button
                variant="outlined"
                sx={{ mt: 2 }}
                onClick={() => window.open('/billing', '_blank')}
              >
                Abonnement verwalten
              </Button>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Feature-Konfiguration */}
        <Grid item xs={12}>
          <Card>
            <CardHeader title="Feature-Konfiguration" />
            <CardContent>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6} md={3}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.features.emailIntegration}
                        onChange={(e) => handleSaveSettings({
                          features: {
                            ...settings.features,
                            emailIntegration: e.target.checked
                          }
                        })}
                      />
                    }
                    label="E-Mail Integration"
                  />
                </Grid>
                
                <Grid item xs={12} sm={6} md={3}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.features.advancedReporting}
                        onChange={(e) => handleSaveSettings({
                          features: {
                            ...settings.features,
                            advancedReporting: e.target.checked
                          }
                        })}
                      />
                    }
                    label="Advanced Reporting"
                  />
                </Grid>
                
                <Grid item xs={12} sm={6} md={3}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.features.mobileApp}
                        onChange={(e) => handleSaveSettings({
                          features: {
                            ...settings.features,
                            mobileApp: e.target.checked
                          }
                        })}
                      />
                    }
                    label="Mobile App"
                  />
                </Grid>
                
                <Grid item xs={12} sm={6} md={3}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={settings.features.whatsappIntegration}
                        onChange={(e) => handleSaveSettings({
                          features: {
                            ...settings.features,
                            whatsappIntegration: e.target.checked
                          }
                        })}
                      />
                    }
                    label="WhatsApp Integration"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};
```

---

## 🧪 TESTING & PERFORMANCE

### Multi-Tenant Integration Tests
```java
@QuarkusTest
@TestProfile(MultiTenantTestProfile.class)
class MultiTenantIntegrationTest {
    
    @Inject
    TenantContextManager contextManager;
    
    @Inject
    CustomerRepository customerRepository;
    
    @Test
    void shouldIsolateDataBetweenTenants() {
        // Given: Two different tenants
        TenantContext tenant1 = createTestTenant("company1", "tenant_company1");
        TenantContext tenant2 = createTestTenant("company2", "tenant_company2");
        
        // When: Create customers for each tenant
        contextManager.setCurrentTenant(tenant1);
        Customer customer1 = Customer.builder()
            .name("Customer for Tenant 1")
            .email("customer1@tenant1.com")
            .build();
        customerRepository.save(customer1);
        
        contextManager.setCurrentTenant(tenant2);
        Customer customer2 = Customer.builder()
            .name("Customer for Tenant 2")
            .email("customer2@tenant2.com")
            .build();
        customerRepository.save(customer2);
        
        // Then: Each tenant should only see their own data
        contextManager.setCurrentTenant(tenant1);
        List<Customer> tenant1Customers = customerRepository.findAll();
        assertThat(tenant1Customers).hasSize(1);
        assertThat(tenant1Customers.get(0).getName()).isEqualTo("Customer for Tenant 1");
        
        contextManager.setCurrentTenant(tenant2);
        List<Customer> tenant2Customers = customerRepository.findAll();
        assertThat(tenant2Customers).hasSize(1);
        assertThat(tenant2Customers.get(0).getName()).isEqualTo("Customer for Tenant 2");
    }
    
    @Test
    void shouldHandleConcurrentTenantRequests() throws InterruptedException {
        // Given: Multiple tenants
        List<TenantContext> tenants = List.of(
            createTestTenant("tenant1", "tenant_tenant1"),
            createTestTenant("tenant2", "tenant_tenant2"),
            createTestTenant("tenant3", "tenant_tenant3")
        );
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(30);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        
        // When: Simulate concurrent requests from different tenants
        for (int i = 0; i < 30; i++) {
            final int requestId = i;
            final TenantContext tenant = tenants.get(i % 3);
            
            executor.submit(() -> {
                try {
                    contextManager.setCurrentTenant(tenant);
                    
                    // Create customer with unique name
                    Customer customer = Customer.builder()
                        .name("Customer " + requestId + " for " + tenant.getName())
                        .email("customer" + requestId + "@" + tenant.getSubdomain() + ".com")
                        .build();
                    
                    customerRepository.save(customer);
                    
                    // Verify customer was saved in correct tenant
                    List<Customer> customers = customerRepository.findAll();
                    boolean found = customers.stream()
                        .anyMatch(c -> c.getName().contains("Customer " + requestId));
                    
                    if (!found) {
                        throw new AssertionError("Customer not found in tenant " + tenant.getName());
                    }
                    
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    contextManager.clearCurrentTenant();
                    latch.countDown();
                }
            });
        }
        
        // Then: All requests should complete successfully
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertThat(exceptions).isEmpty();
        
        // Verify data isolation
        for (TenantContext tenant : tenants) {
            contextManager.setCurrentTenant(tenant);
            List<Customer> customers = customerRepository.findAll();
            
            // Each tenant should have exactly 10 customers
            assertThat(customers).hasSize(10);
            
            // All customers should belong to this tenant
            customers.forEach(customer -> {
                assertThat(customer.getName()).contains("for " + tenant.getName());
            });
            
            contextManager.clearCurrentTenant();
        }
    }
}
```

### Performance Monitoring
```java
@ApplicationScoped
public class TenantPerformanceMonitor {
    
    @Inject
    MeterRegistry meterRegistry;
    
    private final Timer tenantResolutionTimer;
    private final Counter tenantContextErrors;
    private final Gauge activeTenants;
    
    public TenantPerformanceMonitor() {
        this.tenantResolutionTimer = Timer.builder("tenant.resolution.time")
            .description("Time to resolve tenant context")
            .register(meterRegistry);
            
        this.tenantContextErrors = Counter.builder("tenant.context.errors")
            .description("Number of tenant context resolution errors")
            .register(meterRegistry);
            
        this.activeTenants = Gauge.builder("tenant.active.count")
            .description("Number of active tenants")
            .register(meterRegistry, this, TenantPerformanceMonitor::getActiveTenantCount);
    }
    
    public Timer.Sample startTenantResolution() {
        return Timer.start(meterRegistry);
    }
    
    public void recordTenantResolution(Timer.Sample sample, String tenantId) {
        sample.stop(tenantResolutionTimer.tag("tenant", tenantId));
    }
    
    public void recordTenantContextError(String errorType) {
        tenantContextErrors.increment(Tags.of("error.type", errorType));
    }
    
    private double getActiveTenantCount() {
        return tenantRepository.countByStatus(TenantStatus.ACTIVE);
    }
    
    @Scheduled(every = "5m")
    public void recordTenantMetrics() {
        // Record per-tenant metrics
        List<Tenant> activeTenants = tenantRepository.findByStatus(TenantStatus.ACTIVE);
        
        for (Tenant tenant : activeTenants) {
            recordTenantSpecificMetrics(tenant);
        }
    }
    
    private void recordTenantSpecificMetrics(Tenant tenant) {
        Tags tenantTags = Tags.of("tenant", tenant.getSubdomain());
        
        // Database connection pool metrics
        HikariDataSource dataSource = tenantDataSourceRouter.getTenantDataSource(
            tenant.getDatabaseSchema()
        );
        
        Gauge.builder("tenant.db.pool.active")
            .tags(tenantTags)
            .register(meterRegistry, dataSource.getHikariPoolMXBean(), 
                HikariPoolMXBean::getActiveConnections);
        
        Gauge.builder("tenant.db.pool.idle")
            .tags(tenantTags)
            .register(meterRegistry, dataSource.getHikariPoolMXBean(), 
                HikariPoolMXBean::getIdleConnections);
        
        // Tenant-specific business metrics
        contextManager.setCurrentTenant(createTenantContext(tenant));
        try {
            int customerCount = customerRepository.countAll();
            int activeOpportunities = opportunityRepository.countByStatus("ACTIVE");
            
            Gauge.builder("tenant.customers.count")
                .tags(tenantTags)
                .register(meterRegistry, () -> customerCount);
                
            Gauge.builder("tenant.opportunities.active")
                .tags(tenantTags)
                .register(meterRegistry, () -> activeOpportunities);
                
        } finally {
            contextManager.clearCurrentTenant();
        }
    }
}
```

---

## 🔗 NAVIGATION & DEPENDENCIES

### 🧭 VOLLSTÄNDIGE FEATURE-NAVIGATION (40 Features)

#### 🟢 ACTIVE Features (9)
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md)
- [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md)
- [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)
- [FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md)
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md)
- [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md)
- [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md)
- [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_TECH_CONCEPT.md)

#### 🔵 PLANNED Features (31)
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md)
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md)
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md)
- [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md)
- [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md)
- [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md)
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_TECH_CONCEPT.md)
- [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md)
- [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md)
- [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md)
- [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md)
- [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md)
- [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md)
- [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md)
- [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md)
- [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md)
- [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md)
- [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md)
- [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)
- [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md)
- [FC-037 Advanced Reporting Engine](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md)
- **→ FC-038 Multi-Tenant Architecture** ← **SIE SIND HIER**
- [FC-039 API Gateway](#) ← **NÄCHSTES TECH CONCEPT**
- [FC-040 Performance Monitoring](#) ← **SESSION 15 FINAL**

### 📋 ABHÄNGIGKEITEN
- **Benötigt:** FC-008 Security Foundation (Authentication/Authorization)
- **Ergänzt:** FC-009 Permissions System (Tenant-based Permissions)
- **Ermöglicht:** Skalierung auf Enterprise-Level mit hunderten Mandanten

### 🔒 SECURITY & AUTHORIZATION
- **Schema-based Isolation:** Vollständige Datentrennung auf Datenbankebene
- **Row-Level Security:** Zusätzlicher Schutz durch PostgreSQL RLS
- **Request Filtering:** Automatische Tenant-Validierung bei jeder API-Anfrage
- **Connection Pooling:** Isolierte Verbindungspools pro Mandant

### 🚀 DEPLOYMENT STRATEGY
```yaml
# Phase 1 (Tag 1-3): Foundation
- Database Schema Design
- Tenant Resolution Service
- Basic Request Filtering

# Phase 2 (Tag 4-5): Routing
- Multi-Schema Support
- Connection Pooling
- Context Management

# Phase 3 (Tag 6-7): Management
- Tenant Onboarding API
- Frontend Components
- Configuration Management

# Phase 4 (Tag 8-10): Optimization
- Performance Monitoring
- Caching Strategies
- Load Testing
```

### 🎯 SUCCESS CRITERIA
- ✅ 100+ gleichzeitige Mandanten ohne Performance-Verlust
- ✅ Schema-based Datenisolation funktionsfähig
- ✅ 5-Minuten Onboarding für neue Mandanten
- ✅ Subdomain-based Routing implementiert
- ✅ Tenant-spezifische Konfigurationen
- ✅ Performance-Monitoring für alle Mandanten
- ✅ 99.9% Daten-Isolation zwischen Mandanten

---

**⏱️ GESCHÄTZTE IMPLEMENTIERUNGSZEIT:** 10 Tage  
**🎯 BUSINESS IMPACT:** Sehr Hoch (Enterprise Skalierung)  
**🔧 TECHNISCHE KOMPLEXITÄT:** Sehr Hoch (Multi-Schema, Connection Routing)  
**📊 ROI:** Break-even nach 50 Mandanten durch geteilte Infrastruktur-Kosten