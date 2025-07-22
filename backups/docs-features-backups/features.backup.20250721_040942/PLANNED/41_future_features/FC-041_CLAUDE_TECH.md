# FC-041 Future Features (Plugin Architecture) - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Erweiterbare Plugin-Architektur für zukünftige Features ohne Core-Änderungen  
**Stack:** Dynamic Imports + Plugin API + Event Bus + React Error Boundaries  
**Status:** 📋 Future Slot - Vorbereitet für unbekannte Anforderungen  
**Dependencies:** FC-008 Security (Plugin isolation), FC-009 Permissions | Erweitert: Alle Core Features  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🔮 Plugin Examples](#-plugin-examples)

**Core Purpose in 1 Line:** `Plugin Manifest → Dynamic Loading → Event Integration → Isolated Execution → Platform Extension`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Plugin System in 5 Minuten
```typescript
// 1. Plugin Interface Definition (copy-paste ready)
export interface FreshPlanPlugin {
    // Metadata
    id: string;
    name: string;
    version: string;
    description: string;
    author: string;
    icon?: string;
    
    // Lifecycle
    onInstall?: () => Promise<void>;
    onActivate?: () => Promise<void>;
    onDeactivate?: () => Promise<void>;
    onUninstall?: () => Promise<void>;
    
    // UI Extensions
    routes?: PluginRoute[];
    menuItems?: MenuItem[];
    widgets?: DashboardWidget[];
    
    // Event Subscriptions
    events?: EventSubscription[];
    
    // Permissions Required
    requiredPermissions?: string[];
}

// 2. Plugin Manager Implementation
export class PluginManager {
    private plugins = new Map<string, FreshPlanPlugin>();
    private activePlugins = new Set<string>();
    
    async loadPlugin(manifestUrl: string): Promise<void> {
        try {
            // Load manifest
            const manifest = await fetch(manifestUrl).then(r => r.json());
            
            // Validate plugin
            this.validatePlugin(manifest);
            
            // Dynamic import
            const pluginModule = await import(manifest.entryPoint);
            const plugin: FreshPlanPlugin = pluginModule.default;
            
            // Register
            this.plugins.set(plugin.id, plugin);
            
            // Install hook
            await plugin.onInstall?.();
            
            console.log(`Plugin ${plugin.name} loaded successfully`);
        } catch (error) {
            console.error('Plugin load failed:', error);
            throw new PluginLoadError(manifestUrl, error);
        }
    }
    
    async activatePlugin(pluginId: string): Promise<void> {
        const plugin = this.plugins.get(pluginId);
        if (!plugin) throw new Error(`Plugin ${pluginId} not found`);
        
        // Check permissions
        if (plugin.requiredPermissions) {
            const hasPermissions = await this.checkPermissions(plugin.requiredPermissions);
            if (!hasPermissions) throw new Error('Insufficient permissions');
        }
        
        // Activate
        await plugin.onActivate?.();
        this.activePlugins.add(pluginId);
        
        // Register routes
        this.registerPluginRoutes(plugin);
        
        // Subscribe to events
        this.subscribePluginEvents(plugin);
    }
}

// 3. Plugin Loader Component
export const PluginLoader: React.FC = () => {
    const [plugins, setPlugins] = useState<FreshPlanPlugin[]>([]);
    const [loading, setLoading] = useState(false);
    const pluginManager = useRef(new PluginManager()).current;
    
    const installPlugin = async (manifestUrl: string) => {
        setLoading(true);
        try {
            await pluginManager.loadPlugin(manifestUrl);
            const allPlugins = Array.from(pluginManager.getAllPlugins());
            setPlugins(allPlugins);
            toast.success('Plugin installed successfully');
        } catch (error) {
            toast.error(`Plugin installation failed: ${error.message}`);
        } finally {
            setLoading(false);
        }
    };
    
    return (
        <Box>
            <TextField
                placeholder="Plugin manifest URL"
                onKeyPress={(e) => {
                    if (e.key === 'Enter') {
                        installPlugin(e.target.value);
                    }
                }}
            />
            <List>
                {plugins.map(plugin => (
                    <PluginCard 
                        key={plugin.id} 
                        plugin={plugin}
                        onActivate={() => pluginManager.activatePlugin(plugin.id)}
                    />
                ))}
            </List>
        </Box>
    );
};
```

### Recipe 2: Backend Plugin API
```java
// Backend Plugin Registry
@Path("/api/plugins")
@ApplicationScoped
@RolesAllowed("admin")
public class PluginResource {
    @Inject PluginRegistry registry;
    @Inject Event<PluginEvent> pluginEvents;
    
    @POST
    @Path("/register")
    public Response registerPlugin(PluginManifest manifest) {
        // Validate manifest
        validateManifest(manifest);
        
        // Register plugin
        PluginDescriptor descriptor = PluginDescriptor.from(manifest);
        registry.register(descriptor);
        
        // Fire registration event
        pluginEvents.fire(new PluginRegisteredEvent(descriptor));
        
        return Response.ok(descriptor).build();
    }
    
    @POST
    @Path("/{pluginId}/events")
    public Response handlePluginEvent(
        @PathParam("pluginId") String pluginId,
        PluginEventRequest request
    ) {
        // Verify plugin is active
        if (!registry.isActive(pluginId)) {
            return Response.status(403).entity("Plugin not active").build();
        }
        
        // Process event
        PluginEvent event = new PluginEvent(pluginId, request.getEventType(), request.getData());
        pluginEvents.fire(event);
        
        return Response.accepted().build();
    }
}

// Plugin Event Bus
@ApplicationScoped
public class PluginEventBus {
    private final Map<String, List<PluginEventHandler>> handlers = new ConcurrentHashMap<>();
    
    public void subscribe(String eventType, String pluginId, PluginEventHandler handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }
    
    void onPluginEvent(@Observes PluginEvent event) {
        List<PluginEventHandler> eventHandlers = handlers.get(event.getType());
        if (eventHandlers != null) {
            eventHandlers.parallelStream()
                .forEach(handler -> {
                    try {
                        handler.handle(event);
                    } catch (Exception e) {
                        log.error("Plugin event handler failed", e);
                    }
                });
        }
    }
}
```

### Recipe 3: Plugin Widget System
```typescript
// Dynamic Dashboard Widgets
export const PluginWidgetRenderer: React.FC<{ widget: DashboardWidget }> = ({ widget }) => {
    const [Component, setComponent] = useState<React.ComponentType | null>(null);
    const [error, setError] = useState<Error | null>(null);
    
    useEffect(() => {
        loadPluginComponent(widget.componentPath)
            .then(setComponent)
            .catch(setError);
    }, [widget.componentPath]);
    
    if (error) {
        return (
            <Card>
                <CardContent>
                    <Alert severity="error">
                        Widget failed to load: {error.message}
                    </Alert>
                </CardContent>
            </Card>
        );
    }
    
    if (!Component) {
        return <Skeleton variant="rectangular" height={widget.height || 200} />;
    }
    
    return (
        <ErrorBoundary fallback={<WidgetErrorFallback widget={widget} />}>
            <Component {...widget.props} />
        </ErrorBoundary>
    );
};

// Plugin Route Integration
export const PluginRoutes: React.FC = () => {
    const { plugins } = usePluginManager();
    
    return (
        <Routes>
            {plugins.flatMap(plugin => 
                plugin.routes?.map(route => (
                    <Route
                        key={`${plugin.id}-${route.path}`}
                        path={`/plugins/${plugin.id}${route.path}`}
                        element={
                            <PluginPageWrapper plugin={plugin}>
                                <route.component />
                            </PluginPageWrapper>
                        }
                    />
                )) ?? []
            )}
        </Routes>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Plugin Loading Test
```typescript
describe('PluginManager', () => {
    let pluginManager: PluginManager;
    
    beforeEach(() => {
        pluginManager = new PluginManager();
    });
    
    it('should load valid plugin', async () => {
        const mockPlugin = {
            id: 'test-plugin',
            name: 'Test Plugin',
            version: '1.0.0',
            onInstall: jest.fn()
        };
        
        // Mock dynamic import
        jest.doMock('/plugins/test/index.js', () => ({
            default: mockPlugin
        }));
        
        await pluginManager.loadPlugin('/plugins/test/manifest.json');
        
        expect(mockPlugin.onInstall).toHaveBeenCalled();
        expect(pluginManager.getPlugin('test-plugin')).toBe(mockPlugin);
    });
    
    it('should handle plugin errors gracefully', async () => {
        const errorPlugin = {
            id: 'error-plugin',
            onActivate: jest.fn().mockRejectedValue(new Error('Activation failed'))
        };
        
        pluginManager.plugins.set('error-plugin', errorPlugin);
        
        await expect(pluginManager.activatePlugin('error-plugin'))
            .rejects.toThrow('Activation failed');
    });
});
```

### Pattern 2: Plugin Isolation Test
```java
@QuarkusTest
class PluginIsolationTest {
    @Inject PluginRegistry registry;
    
    @Test
    void pluginEvents_shouldBeIsolated() {
        // Register two plugins
        registry.register(createPlugin("plugin-a"));
        registry.register(createPlugin("plugin-b"));
        
        // Plugin A event should not affect Plugin B
        PluginEvent eventA = new PluginEvent("plugin-a", "test", Map.of("data", "a"));
        
        // Verify isolation
        verify(pluginAHandler).handle(eventA);
        verify(pluginBHandler, never()).handle(any());
    }
}
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Navigation System (M1)
```typescript
// Dynamic menu items from plugins
export const NavigationWithPlugins: React.FC = () => {
    const { plugins } = usePluginManager();
    const pluginMenuItems = plugins.flatMap(p => p.menuItems ?? []);
    
    return (
        <List>
            {/* Core menu items */}
            <ListItem button>
                <ListItemText primary="Dashboard" />
            </ListItem>
            
            {/* Plugin menu items */}
            {pluginMenuItems.length > 0 && (
                <>
                    <Divider />
                    <ListSubheader>Plugins</ListSubheader>
                    {pluginMenuItems.map(item => (
                        <PluginMenuItem key={item.id} item={item} />
                    ))}
                </>
            )}
        </List>
    );
};
```

### Mit Event System
```java
// Plugin event integration
@ApplicationScoped
public class CustomerEventPluginBridge {
    @Inject PluginEventBus pluginEventBus;
    
    void onCustomerCreated(@Observes CustomerCreatedEvent event) {
        // Forward to plugins
        PluginEvent pluginEvent = new PluginEvent(
            "system",
            "customer.created",
            Map.of(
                "customerId", event.getCustomerId(),
                "customerName", event.getCustomerName()
            )
        );
        
        pluginEventBus.publish(pluginEvent);
    }
}
```

---

## 🔮 PLUGIN EXAMPLES

### Example: Weather Plugin
```typescript
// Simple weather widget plugin
export default {
    id: 'weather-plugin',
    name: 'Weather Widget',
    version: '1.0.0',
    
    widgets: [{
        id: 'weather-widget',
        title: 'Local Weather',
        component: WeatherWidget,
        defaultSize: { width: 2, height: 1 },
        refreshInterval: 600000 // 10 min
    }],
    
    async onActivate() {
        // Request location permission
        const permission = await navigator.permissions.query({ name: 'geolocation' });
        if (permission.state !== 'granted') {
            throw new Error('Location permission required');
        }
    }
} as FreshPlanPlugin;

const WeatherWidget: React.FC = () => {
    const { data: weather } = useQuery({
        queryKey: ['weather'],
        queryFn: fetchLocalWeather,
        refetchInterval: 600000
    });
    
    return (
        <Card>
            <CardContent>
                <Typography variant="h6">{weather?.temperature}°C</Typography>
                <Typography variant="body2">{weather?.condition}</Typography>
            </CardContent>
        </Card>
    );
};
```

### Example: Analytics Extension
```java
// Backend analytics plugin
@PluginDescriptor(
    id = "advanced-analytics",
    name = "Advanced Analytics",
    requiredPermissions = {"analytics:read", "customers:read"}
)
public class AdvancedAnalyticsPlugin implements Plugin {
    
    @Override
    public void onActivate(PluginContext context) {
        // Register custom metrics
        context.getMetricsRegistry().register(
            "customer.engagement.score",
            new EngagementScoreMetric()
        );
        
        // Subscribe to events
        context.subscribe("customer.activity", this::calculateEngagement);
    }
    
    private void calculateEngagement(PluginEvent event) {
        UUID customerId = UUID.fromString(event.getData().get("customerId"));
        double score = calculateEngagementScore(customerId);
        
        // Publish result
        context.publish(new PluginEvent(
            getId(),
            "engagement.calculated",
            Map.of("customerId", customerId, "score", score)
        ));
    }
}
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🔒 Plugin Security Model</summary>

### Permission Sandboxing
```typescript
// Plugin permission system
interface PluginPermissions {
    // API Access
    api: {
        allowedEndpoints: string[];
        maxRequestsPerMinute: number;
    };
    
    // UI Access
    ui: {
        canAccessDOM: boolean;
        canShowNotifications: boolean;
        canAccessClipboard: boolean;
    };
    
    // Data Access
    data: {
        allowedEntities: string[];
        readOnly: boolean;
    };
}

// Runtime permission checking
class PluginSandbox {
    checkPermission(plugin: FreshPlanPlugin, action: string): boolean {
        const permissions = this.getPluginPermissions(plugin.id);
        return this.evaluatePermission(permissions, action);
    }
}
```

### Plugin Isolation
```java
// ClassLoader isolation for plugins
public class PluginClassLoader extends URLClassLoader {
    private final String pluginId;
    
    public PluginClassLoader(URL[] urls, String pluginId) {
        super(urls, PluginClassLoader.class.getClassLoader());
        this.pluginId = pluginId;
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Prevent access to system classes
        if (isRestrictedClass(name)) {
            throw new SecurityException("Plugin cannot access: " + name);
        }
        return super.loadClass(name, resolve);
    }
}
```

</details>

<details>
<summary>📦 Plugin Distribution</summary>

### Plugin Manifest Schema
```json
{
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "required": ["id", "name", "version", "entryPoint"],
    "properties": {
        "id": {
            "type": "string",
            "pattern": "^[a-z0-9-]+$"
        },
        "name": {
            "type": "string"
        },
        "version": {
            "type": "string",
            "pattern": "^\\d+\\.\\d+\\.\\d+$"
        },
        "entryPoint": {
            "type": "string",
            "format": "uri"
        },
        "dependencies": {
            "type": "object",
            "additionalProperties": {
                "type": "string"
            }
        }
    }
}
```

### Plugin Registry API
```typescript
// Public plugin registry
interface PluginRegistryAPI {
    search(query: string): Promise<PluginInfo[]>;
    getDetails(pluginId: string): Promise<PluginDetails>;
    getManifest(pluginId: string): Promise<PluginManifest>;
    reportIssue(pluginId: string, issue: Issue): Promise<void>;
}
```

</details>

---

**🎯 Nächster Schritt:** Plugin Manager mit Dynamic Import System implementieren