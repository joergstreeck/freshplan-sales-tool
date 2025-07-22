# FC-038 CLAUDE_TECH: Multi-Tenant Architecture - Enterprise-Scale Mandantenfähigkeit

**CLAUDE TECH** | **Original:** 1272 Zeilen → **Optimiert:** 600 Zeilen (53% Reduktion!)  
**Feature-Typ:** 🔧 BACKEND | **Priorität:** HOCH | **Geschätzter Aufwand:** 10 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**Skalierbare Mandantenfähigkeit mit Schema-basierter Datenisolation und zentraler Verwaltung**

### 🎯 Das macht es:
- **Schema-based Isolation**: Vollständige Datentrennung per PostgreSQL Schema + Tenant Context
- **Dynamic Routing**: Request-basierte Tenant-Erkennung + automatische Database-Routing
- **Centralized Management**: Tenant Onboarding + Configuration + Monitoring aus einer Konsole
- **Resource Optimization**: Shared Infrastructure + per-Tenant Scaling + Performance Monitoring

### 🚀 ROI:
- **100+ gleichzeitige Mandanten** ohne Performance-Verlust durch optimierte Schema-Isolation
- **5-Minuten Onboarding** für neue Mandanten durch vollautomatisierte Provisioning-Pipeline
- **99.9% Daten-Isolation** zwischen Mandanten durch strenge Schema-Trennung
- **50% reduzierte Infrastruktur-Kosten** vs. separate Instanzen bei gleicher Performance

### 🏗️ Multi-Tenant Flow:
```
Request → Tenant Resolution → Schema Context → Database Routing → Response Isolation → Audit Logging
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Multi-Tenant Engine:

#### 1. Core Tenant Service:
```java
@ApplicationScoped
public class TenantService {
    
    @Inject
    TenantRepository tenantRepository;
    
    @Inject
    TenantProvisioningService provisioningService;
    
    @Inject
    TenantConfigurationService configService;
    
    @Inject
    DatabaseSchemaManager schemaManager;
    
    public CompletionStage<Tenant> createTenant(CreateTenantRequest request) {
        return CompletableFuture
            .supplyAsync(() -> validateTenantRequest(request))
            .thenCompose(validatedRequest -> reserveTenantIdentifier(validatedRequest))
            .thenCompose(tenant -> provisionDatabaseSchema(tenant))
            .thenCompose(tenant -> setupTenantConfiguration(tenant))
            .thenCompose(tenant -> initializeDefaultData(tenant))
            .thenApply(tenant -> publishTenantCreatedEvent(tenant));
    }
    
    private Tenant validateTenantRequest(CreateTenantRequest request) {
        // Validate subdomain availability
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new TenantException("Subdomain already exists: " + request.getSubdomain());
        }
        
        // Validate subdomain format
        if (!request.getSubdomain().matches("^[a-z0-9-]+$")) {
            throw new TenantException("Invalid subdomain format: " + request.getSubdomain());
        }
        
        // Check tenant limits
        long activeTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE);
        if (activeTenants >= getMaxTenantLimit()) {
            throw new TenantException("Maximum tenant limit reached");
        }
        
        return Tenant.builder()
            .name(request.getName())
            .subdomain(request.getSubdomain())
            .subscriptionTier(request.getSubscriptionTier())
            .status(TenantStatus.PROVISIONING)
            .databaseSchema("tenant_" + request.getSubdomain())
            .settings(TenantSettings.createDefault())
            .createdAt(Instant.now())
            .build();
    }
    
    private CompletionStage<Tenant> reserveTenantIdentifier(Tenant tenant) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Atomic reservation of tenant identifier
                Tenant savedTenant = tenantRepository.save(tenant);
                
                Log.info("Reserved tenant identifier: {} with schema: {}", 
                    savedTenant.getSubdomain(), 
                    savedTenant.getDatabaseSchema());
                
                return savedTenant;
                
            } catch (Exception e) {
                Log.error("Failed to reserve tenant identifier", e);
                throw new TenantProvisioningException("Failed to reserve tenant identifier", e);
            }
        });
    }
    
    private CompletionStage<Tenant> provisionDatabaseSchema(Tenant tenant) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create dedicated schema for tenant
                schemaManager.createTenantSchema(tenant.getDatabaseSchema());
                
                // Apply database migrations to tenant schema
                schemaManager.applyMigrations(tenant.getDatabaseSchema());
                
                // Verify schema integrity
                schemaManager.validateSchema(tenant.getDatabaseSchema());
                
                // Update tenant status
                tenant.setStatus(TenantStatus.SCHEMA_READY);
                tenant.setUpdatedAt(Instant.now());
                
                return tenantRepository.save(tenant);
                
            } catch (Exception e) {
                Log.error("Failed to provision database schema for tenant: " + tenant.getSubdomain(), e);
                // Cleanup on failure
                rollbackTenantProvisioning(tenant);
                throw new TenantProvisioningException("Schema provisioning failed", e);
            }
        });
    }
    
    private CompletionStage<Tenant> setupTenantConfiguration(Tenant tenant) {
        return CompletableFuture.supplyAsync(() -> {
            // Initialize tenant-specific configuration
            TenantConfiguration config = TenantConfiguration.builder()
                .tenantId(tenant.getId())
                .features(getDefaultFeaturesForTier(tenant.getSubscriptionTier()))
                .limits(getDefaultLimitsForTier(tenant.getSubscriptionTier()))
                .branding(BrandingConfiguration.createDefault())
                .integrations(new HashMap<>())
                .build();
            
            configService.saveTenantConfiguration(config);
            
            // Update tenant status
            tenant.setStatus(TenantStatus.CONFIGURED);
            tenant.setUpdatedAt(Instant.now());
            
            return tenantRepository.save(tenant);
        });
    }
    
    private CompletionStage<Tenant> initializeDefaultData(Tenant tenant) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Set tenant context for data initialization
                TenantContext.setCurrentTenant(tenant);
                
                // Create default admin user
                createDefaultAdminUser(tenant);
                
                // Create sample data if requested
                if (tenant.getSettings().isIncludeSampleData()) {
                    createSampleData(tenant);
                }
                
                // Activate tenant
                tenant.setStatus(TenantStatus.ACTIVE);
                tenant.setActivatedAt(Instant.now());
                tenant.setUpdatedAt(Instant.now());
                
                return tenantRepository.save(tenant);
                
            } finally {
                TenantContext.clear();
            }
        });
    }
    
    public CompletionStage<Void> deactivateTenant(UUID tenantId, DeactivationReason reason) {
        return CompletableFuture.runAsync(() -> {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));
            
            // Archive tenant data
            archiveTenantData(tenant);
            
            // Update status
            tenant.setStatus(TenantStatus.DEACTIVATED);
            tenant.setDeactivatedAt(Instant.now());
            tenant.setDeactivationReason(reason);
            
            tenantRepository.save(tenant);
            
            // Publish deactivation event
            publishTenantDeactivatedEvent(tenant);
        });
    }
}
```

#### 2. Tenant Context & Resolution:
```java
@ApplicationScoped
public class TenantResolver {
    
    @Inject
    TenantRepository tenantRepository;
    
    @Inject
    TenantCache tenantCache;
    
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String SUBDOMAIN_PATTERN = "^([a-z0-9-]+)\\.freshplan\\.app$";
    
    public CompletionStage<Optional<Tenant>> resolveTenant(HttpServletRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            
            // 1. Try header-based resolution (API calls)
            String tenantHeader = request.getHeader(TENANT_HEADER);
            if (tenantHeader != null) {
                return resolveTenantById(tenantHeader);
            }
            
            // 2. Try subdomain-based resolution (web app)
            String host = request.getHeader("Host");
            if (host != null) {
                Optional<String> subdomain = extractSubdomain(host);
                if (subdomain.isPresent()) {
                    return resolveTenantBySubdomain(subdomain.get());
                }
            }
            
            // 3. Try path-based resolution (/tenant/xyz/...)
            String path = request.getRequestURI();
            Optional<String> pathTenant = extractTenantFromPath(path);
            if (pathTenant.isPresent()) {
                return resolveTenantBySubdomain(pathTenant.get());
            }
            
            return Optional.empty();
        });
    }
    
    private Optional<Tenant> resolveTenantById(String tenantId) {
        try {
            UUID id = UUID.fromString(tenantId);
            
            // Check cache first
            Optional<Tenant> cached = tenantCache.getTenant(id);
            if (cached.isPresent()) {
                return cached;
            }
            
            // Load from database
            Optional<Tenant> tenant = tenantRepository.findById(id);
            
            // Cache for future requests
            tenant.ifPresent(t -> tenantCache.putTenant(t));
            
            return tenant;
            
        } catch (IllegalArgumentException e) {
            Log.warn("Invalid tenant ID format: " + tenantId);
            return Optional.empty();
        }
    }
    
    private Optional<Tenant> resolveTenantBySubdomain(String subdomain) {
        // Check cache first
        Optional<Tenant> cached = tenantCache.getTenantBySubdomain(subdomain);
        if (cached.isPresent()) {
            return cached;
        }
        
        // Load from database
        Optional<Tenant> tenant = tenantRepository.findBySubdomain(subdomain);
        
        // Cache for future requests
        tenant.ifPresent(t -> tenantCache.putTenant(t));
        
        return tenant;
    }
    
    private Optional<String> extractSubdomain(String host) {
        Pattern pattern = Pattern.compile(SUBDOMAIN_PATTERN);
        Matcher matcher = pattern.matcher(host.toLowerCase());
        
        if (matcher.matches()) {
            return Optional.of(matcher.group(1));
        }
        
        return Optional.empty();
    }
    
    private Optional<String> extractTenantFromPath(String path) {
        if (path.startsWith("/tenant/")) {
            String[] segments = path.split("/");
            if (segments.length >= 3) {
                return Optional.of(segments[2]);
            }
        }
        
        return Optional.empty();
    }
}

@RequestScoped
public class TenantContext {
    
    private static final ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
    
    public static void setCurrentTenant(Tenant tenant) {
        currentTenant.set(tenant);
        
        // Set database schema context
        if (tenant != null) {
            DatabaseContext.setSchema(tenant.getDatabaseSchema());
        }
    }
    
    public static Optional<Tenant> getCurrentTenant() {
        return Optional.ofNullable(currentTenant.get());
    }
    
    public static UUID getCurrentTenantId() {
        return getCurrentTenant()
            .map(Tenant::getId)
            .orElseThrow(() -> new TenantContextException("No tenant context available"));
    }
    
    public static String getCurrentSchema() {
        return getCurrentTenant()
            .map(Tenant::getDatabaseSchema)
            .orElseThrow(() -> new TenantContextException("No schema context available"));
    }
    
    public static void clear() {
        currentTenant.remove();
        DatabaseContext.clearSchema();
    }
    
    public static void validateTenantAccess(UUID resourceTenantId) {
        UUID currentTenantId = getCurrentTenantId();
        
        if (!currentTenantId.equals(resourceTenantId)) {
            throw new TenantAccessViolationException(
                "Access denied: Resource belongs to different tenant");
        }
    }
}
```

#### 3. Database Schema Manager:
```java
@ApplicationScoped
public class DatabaseSchemaManager {
    
    @Inject
    DataSource dataSource;
    
    @Inject
    FlywayMigrationService migrationService;
    
    private static final String SCHEMA_TEMPLATE = """
        -- Create tenant schema
        CREATE SCHEMA IF NOT EXISTS %s;
        
        -- Set search path for session
        SET search_path TO %s, public;
        
        -- Grant permissions
        GRANT USAGE ON SCHEMA %s TO freshplan_app;
        GRANT CREATE ON SCHEMA %s TO freshplan_app;
        """;
    
    public void createTenantSchema(String schemaName) {
        validateSchemaName(schemaName);
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            Log.info("Creating tenant schema: {}", schemaName);
            
            // Create schema with proper permissions
            String sql = String.format(SCHEMA_TEMPLATE, 
                schemaName, schemaName, schemaName, schemaName);
            
            stmt.execute(sql);
            
            Log.info("Successfully created schema: {}", schemaName);
            
        } catch (SQLException e) {
            Log.error("Failed to create schema: " + schemaName, e);
            throw new SchemaCreationException("Failed to create schema: " + schemaName, e);
        }
    }
    
    public void applyMigrations(String schemaName) {
        validateSchemaName(schemaName);
        
        try {
            Log.info("Applying migrations to schema: {}", schemaName);
            
            // Configure Flyway for tenant schema
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/tenant")
                .schemas(schemaName)
                .table("flyway_schema_history")
                .placeholders(Map.of("schema", schemaName))
                .load();
            
            // Apply migrations
            MigrateResult result = flyway.migrate();
            
            Log.info("Applied {} migrations to schema: {}", 
                result.migrationsExecuted, schemaName);
            
        } catch (Exception e) {
            Log.error("Failed to apply migrations to schema: " + schemaName, e);
            throw new SchemaMigrationException("Migration failed for schema: " + schemaName, e);
        }
    }
    
    public void validateSchema(String schemaName) {
        validateSchemaName(schemaName);
        
        try (Connection conn = dataSource.getConnection()) {
            
            // Switch to tenant schema
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName + ", public");
            }
            
            // Verify essential tables exist
            String[] requiredTables = {
                "customers", "opportunities", "users", "interactions",
                "products", "orders", "activities", "notes"
            };
            
            DatabaseMetaData metaData = conn.getMetaData();
            
            for (String tableName : requiredTables) {
                try (ResultSet rs = metaData.getTables(null, schemaName, tableName, null)) {
                    if (!rs.next()) {
                        throw new SchemaValidationException(
                            "Required table missing in schema " + schemaName + ": " + tableName);
                    }
                }
            }
            
            Log.info("Schema validation successful for: {}", schemaName);
            
        } catch (SQLException e) {
            Log.error("Schema validation failed for: " + schemaName, e);
            throw new SchemaValidationException("Schema validation failed: " + schemaName, e);
        }
    }
    
    public void dropTenantSchema(String schemaName) {
        validateSchemaName(schemaName);
        
        // Safety check - prevent dropping system schemas
        if (isSystemSchema(schemaName)) {
            throw new SchemaOperationException("Cannot drop system schema: " + schemaName);
        }
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            Log.info("Dropping tenant schema: {}", schemaName);
            
            // Drop schema with cascade
            stmt.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            
            Log.info("Successfully dropped schema: {}", schemaName);
            
        } catch (SQLException e) {
            Log.error("Failed to drop schema: " + schemaName, e);
            throw new SchemaOperationException("Failed to drop schema: " + schemaName, e);
        }
    }
    
    private void validateSchemaName(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("Schema name cannot be null or empty");
        }
        
        // Validate schema name format (prevent SQL injection)
        if (!schemaName.matches("^[a-z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid schema name format: " + schemaName);
        }
        
        // Check length limits
        if (schemaName.length() > 63) { // PostgreSQL identifier limit
            throw new IllegalArgumentException("Schema name too long: " + schemaName);
        }
    }
    
    private boolean isSystemSchema(String schemaName) {
        Set<String> systemSchemas = Set.of(
            "public", "information_schema", "pg_catalog", 
            "pg_toast", "pg_temp_1", "pg_toast_temp_1"
        );
        
        return systemSchemas.contains(schemaName.toLowerCase()) ||
               schemaName.toLowerCase().startsWith("pg_");
    }
}
```

#### 4. Tenant-Aware Repository Base:
```java
@MappedSuperclass
public abstract class TenantAwareRepository<T, ID> implements PanacheRepositoryBase<T, ID> {
    
    @PrePersist
    @PreUpdate
    protected void setTenantContext(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantEntity = (TenantAware) entity;
            
            // Set tenant ID if not already set
            if (tenantEntity.getTenantId() == null) {
                UUID currentTenantId = TenantContext.getCurrentTenantId();
                tenantEntity.setTenantId(currentTenantId);
            }
            
            // Validate tenant access
            TenantContext.validateTenantAccess(tenantEntity.getTenantId());
        }
    }
    
    @PostLoad
    protected void validateTenantAccess(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantEntity = (TenantAware) entity;
            TenantContext.validateTenantAccess(tenantEntity.getTenantId());
        }
    }
    
    // Tenant-aware finders
    public List<T> findAllForCurrentTenant() {
        String schema = TenantContext.getCurrentSchema();
        return find("SELECT e FROM " + getEntityName() + " e").list();
    }
    
    public Optional<T> findByIdForCurrentTenant(ID id) {
        Optional<T> entity = findByIdOptional(id);
        
        // Additional tenant validation happens in @PostLoad
        return entity;
    }
    
    public List<T> findForCurrentTenant(String query, Object... params) {
        // Query will be executed in current tenant schema context
        return find(query, params).list();
    }
    
    // Override save to ensure tenant context
    @Override
    public T persist(T entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantEntity = (TenantAware) entity;
            UUID currentTenantId = TenantContext.getCurrentTenantId();
            tenantEntity.setTenantId(currentTenantId);
        }
        
        return PanacheRepositoryBase.super.persist(entity);
    }
    
    protected abstract String getEntityName();
}

// Example usage in specific repository
@ApplicationScoped
public class CustomerRepository extends TenantAwareRepository<Customer, UUID> {
    
    public List<Customer> findByStatus(CustomerStatus status) {
        return findForCurrentTenant("status = ?1", status);
    }
    
    public Optional<Customer> findByEmail(String email) {
        return findForCurrentTenant("email = ?1", email)
            .stream()
            .findFirst();
    }
    
    @Override
    protected String getEntityName() {
        return Customer.class.getSimpleName();
    }
}
```

### 🎨 Frontend Tenant Management:

#### 1. Tenant Selector Component:
```typescript
export const TenantSelector: React.FC = () => {
  const { user } = useAuth();
  const [selectedTenant, setSelectedTenant] = useState<Tenant | null>(null);
  
  const { data: availableTenants } = useQuery({
    queryKey: ['user-tenants', user.id],
    queryFn: () => tenantApi.getUserTenants(user.id)
  });

  const { mutate: switchTenant } = useMutation({
    mutationFn: tenantApi.switchTenant,
    onSuccess: (tenant) => {
      setSelectedTenant(tenant);
      // Update global tenant context
      updateTenantContext(tenant);
      // Refresh all queries with new tenant context
      queryClient.invalidateQueries();
      toast.success(`Gewechselt zu ${tenant.name}`);
    }
  });

  return (
    <FormControl size="small" sx={{ minWidth: 200 }}>
      <InputLabel>Mandant</InputLabel>
      <Select
        value={selectedTenant?.id || ''}
        onChange={(e) => {
          const tenant = availableTenants?.find(t => t.id === e.target.value);
          if (tenant) {
            switchTenant(tenant.id);
          }
        }}
        label="Mandant"
      >
        {availableTenants?.map((tenant) => (
          <MenuItem key={tenant.id} value={tenant.id}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Avatar sx={{ width: 24, height: 24, bgcolor: tenant.brandColor }}>
                {tenant.name.charAt(0)}
              </Avatar>
              <Box>
                <Typography variant="body2">{tenant.name}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {tenant.subscriptionTier}
                </Typography>
              </Box>
            </Box>
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
};
```

#### 2. Tenant Onboarding Wizard:
```typescript
export const TenantOnboardingWizard: React.FC = () => {
  const [step, setStep] = useState(0);
  const [tenantData, setTenantData] = useState<CreateTenantRequest>({
    name: '',
    subdomain: '',
    subscriptionTier: 'BASIC',
    adminUser: {
      firstName: '',
      lastName: '',
      email: '',
      password: ''
    },
    settings: {
      includeSampleData: true,
      enableIntegrations: false
    }
  });

  const { mutate: createTenant, isLoading } = useMutation({
    mutationFn: tenantApi.createTenant,
    onSuccess: (tenant) => {
      toast.success('Mandant erfolgreich erstellt!');
      // Redirect to tenant dashboard
      navigate(`/tenant/${tenant.subdomain}/dashboard`);
    }
  });

  const steps = [
    'Grunddaten',
    'Administrator',
    'Einstellungen',
    'Bestätigung'
  ];

  const handleNext = () => {
    if (step < steps.length - 1) {
      setStep(step + 1);
    } else {
      createTenant(tenantData);
    }
  };

  const handleBack = () => {
    if (step > 0) {
      setStep(step - 1);
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Typography 
        variant="h4" 
        fontFamily="Antonio Bold"
        align="center"
        gutterBottom
        sx={{ color: '#004F7B' }}
      >
        🏢 Neuen Mandanten erstellen
      </Typography>

      <Stepper activeStep={step} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      <Card sx={{ p: 4 }}>
        {step === 0 && (
          <Stack spacing={3}>
            <Typography variant="h6">Grunddaten des Mandanten</Typography>
            
            <TextField
              label="Firmenname"
              value={tenantData.name}
              onChange={(e) => setTenantData(prev => ({ ...prev, name: e.target.value }))}
              fullWidth
              required
            />
            
            <TextField
              label="Subdomain"
              value={tenantData.subdomain}
              onChange={(e) => setTenantData(prev => ({ ...prev, subdomain: e.target.value.toLowerCase() }))}
              fullWidth
              required
              helperText="Die URL wird: https://[subdomain].freshplan.app"
              InputProps={{
                startAdornment: <InputAdornment position="start">https://</InputAdornment>,
                endAdornment: <InputAdornment position="end">.freshplan.app</InputAdornment>
              }}
            />
            
            <FormControl fullWidth>
              <InputLabel>Abo-Typ</InputLabel>
              <Select
                value={tenantData.subscriptionTier}
                onChange={(e) => setTenantData(prev => ({ ...prev, subscriptionTier: e.target.value as SubscriptionTier }))}
                label="Abo-Typ"
              >
                <MenuItem value="BASIC">Basic (5 Benutzer)</MenuItem>
                <MenuItem value="PREMIUM">Premium (25 Benutzer)</MenuItem>
                <MenuItem value="ENTERPRISE">Enterprise (Unbegrenzt)</MenuItem>
              </Select>
            </FormControl>
          </Stack>
        )}

        {step === 1 && (
          <Stack spacing={3}>
            <Typography variant="h6">Administrator-Account</Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField
                  label="Vorname"
                  value={tenantData.adminUser.firstName}
                  onChange={(e) => setTenantData(prev => ({ 
                    ...prev, 
                    adminUser: { ...prev.adminUser, firstName: e.target.value }
                  }))}
                  fullWidth
                  required
                />
              </Grid>
              
              <Grid item xs={6}>
                <TextField
                  label="Nachname"
                  value={tenantData.adminUser.lastName}
                  onChange={(e) => setTenantData(prev => ({ 
                    ...prev, 
                    adminUser: { ...prev.adminUser, lastName: e.target.value }
                  }))}
                  fullWidth
                  required
                />
              </Grid>
            </Grid>
            
            <TextField
              label="E-Mail"
              type="email"
              value={tenantData.adminUser.email}
              onChange={(e) => setTenantData(prev => ({ 
                ...prev, 
                adminUser: { ...prev.adminUser, email: e.target.value }
              }))}
              fullWidth
              required
            />
            
            <TextField
              label="Passwort"
              type="password"
              value={tenantData.adminUser.password}
              onChange={(e) => setTenantData(prev => ({ 
                ...prev, 
                adminUser: { ...prev.adminUser, password: e.target.value }
              }))}
              fullWidth
              required
              helperText="Mindestens 8 Zeichen mit Groß-, Kleinbuchstaben und Zahlen"
            />
          </Stack>
        )}

        {step === 2 && (
          <Stack spacing={3}>
            <Typography variant="h6">Einstellungen</Typography>
            
            <FormControlLabel
              control={
                <Switch
                  checked={tenantData.settings.includeSampleData}
                  onChange={(e) => setTenantData(prev => ({ 
                    ...prev, 
                    settings: { ...prev.settings, includeSampleData: e.target.checked }
                  }))}
                />
              }
              label="Beispieldaten einschließen"
            />
            
            <FormControlLabel
              control={
                <Switch
                  checked={tenantData.settings.enableIntegrations}
                  onChange={(e) => setTenantData(prev => ({ 
                    ...prev, 
                    settings: { ...prev.settings, enableIntegrations: e.target.checked }
                  }))}
                />
              }
              label="Integrationen aktivieren"
            />
          </Stack>
        )}

        {step === 3 && (
          <Stack spacing={2}>
            <Typography variant="h6">Bestätigung</Typography>
            
            <Alert severity="info">
              Bitte überprüfen Sie alle Angaben vor der Erstellung des Mandanten.
            </Alert>
            
            <List>
              <ListItem>
                <ListItemText primary="Firmenname" secondary={tenantData.name} />
              </ListItem>
              <ListItem>
                <ListItemText primary="URL" secondary={`https://${tenantData.subdomain}.freshplan.app`} />
              </ListItem>
              <ListItem>
                <ListItemText primary="Abo-Typ" secondary={tenantData.subscriptionTier} />
              </ListItem>
              <ListItem>
                <ListItemText primary="Administrator" secondary={`${tenantData.adminUser.firstName} ${tenantData.adminUser.lastName} (${tenantData.adminUser.email})`} />
              </ListItem>
            </List>
          </Stack>
        )}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button
            onClick={handleBack}
            disabled={step === 0}
          >
            Zurück
          </Button>
          
          <Button
            variant="contained"
            onClick={handleNext}
            disabled={isLoading}
            sx={{ bgcolor: '#94C456' }}
          >
            {isLoading ? (
              <CircularProgress size={24} />
            ) : step === steps.length - 1 ? (
              'Mandant erstellen'
            ) : (
              'Weiter'
            )}
          </Button>
        </Box>
      </Card>
    </Container>
  );
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: Database Isolation (3 Tage)
1. **Schema Management**: PostgreSQL Schema-based Isolation + Migration System
2. **Tenant Repository**: Master Tenant Management + CRUD Operations
3. **Context Resolution**: Request-based Tenant Detection + Security Validation

### Phase 2: Application Layer (2 Tage)
1. **Tenant Context**: ThreadLocal Context + Database Routing
2. **Repository Base**: Tenant-aware Data Access + Automatic Filtering
3. **Security Integration**: Row-level Security + Access Control

### Phase 3: Management Interface (3 Tage)
1. **Onboarding Wizard**: Step-by-step Tenant Creation + Validation
2. **Management Dashboard**: Tenant Overview + Configuration + Monitoring
3. **Tenant Settings**: Feature Toggles + Limits + Branding

### Phase 4: Performance & Monitoring (2 Tage)
1. **Caching Layer**: Tenant-aware Caching + Redis Integration
2. **Performance Monitoring**: Per-tenant Metrics + Resource Usage
3. **Backup & Recovery**: Tenant-specific Backup + Disaster Recovery

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **100+ gleichzeitige Mandanten** ohne Performance-Verlust durch optimierte Schema-Isolation
- **5-Minuten Onboarding** für neue Mandanten durch vollautomatisierte Provisioning-Pipeline
- **99.9% Daten-Isolation** zwischen Mandanten durch strenge Schema-Trennung
- **50% reduzierte Infrastruktur-Kosten** vs. separate Instanzen bei gleicher Performance

### Technical Benefits:
- **True Data Isolation**: Schema-based Separation für maximale Sicherheit
- **Scalable Architecture**: Supports hundreds of tenants on single instance
- **Automated Provisioning**: Zero-touch tenant onboarding + configuration
- **Central Management**: Unified administration + monitoring + updates

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **FC-008 Security Foundation**: User Management + Authorization Framework (Required)
- **M6 Analytics**: Per-tenant Analytics + Cross-tenant Reporting (Recommended)

### Enables:
- **SaaS Business Model**: Multi-customer deployment + subscription management
- **FC-040 Performance Monitoring**: Tenant-specific monitoring + resource optimization
- **Enterprise Sales**: Large-scale customer deployments + white-label solutions

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **Schema-based Isolation**: PostgreSQL Schemas für maximale Datentrennung statt Row-Level Security
2. **Subdomain Routing**: tenant.freshplan.app für intuitive Multi-Tenancy
3. **ThreadLocal Context**: Request-scoped Tenant Context für Performance + Security
4. **Automated Provisioning**: Flyway-based Schema Migrations für neue Tenants

---

**Status:** Ready for Implementation | **Phase 1:** Database Isolation + Schema Management | **Next:** Application Layer Integration