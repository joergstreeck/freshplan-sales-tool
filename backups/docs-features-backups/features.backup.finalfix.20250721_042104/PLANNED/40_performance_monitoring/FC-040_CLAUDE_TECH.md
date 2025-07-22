# 📊 FC-040 Performance Monitoring - CLAUDE TECH ⚡

**Feature-Typ:** 🔀 FULLSTACK  
**Komplexität:** HOCH | **Aufwand:** 6 Tage | **Priorität:** HIGH  
**Status:** Ready | **Version:** 2.0 | **Letzte Aktualisierung:** 21.07.2025

## ⚡ QUICK-LOAD (30 Sekunden zur Produktivität)

**Was:** Umfassendes Performance-Monitoring mit Real-time Metriken, Core Web Vitals und intelligentes Alerting
**Warum:** Proaktive Problemerkennung mit 99.9% Uptime, User Experience Optimization und Cost Optimization
**Für wen:** DevOps Teams, Product Manager und Entwickler die System-Performance überwachen und optimieren

### 🎯 Sofort loslegen:
```bash
# 1. Backend Performance Setup
cd backend/src/main/java/de/freshplan/infrastructure
mkdir monitoring && cd monitoring
touch PerformanceCollector.java AlertManager.java

# 2. Frontend Monitoring
cd frontend/src/services/monitoring
touch PerformanceTracker.ts WebVitalsCollector.ts
```

### 📋 Copy-Paste Ready Recipes:

#### Performance Metrics Collector:
```java
@ApplicationScoped
public class PerformanceCollector {
    @Inject MeterRegistry meterRegistry;
    @Inject TenantContextManager tenantManager;
    
    private final Timer jvmGcTimer;
    private final Gauge memoryUsage;
    private final Counter exceptionCounter;
    
    @PostConstruct
    void initializeMetrics() {
        this.jvmGcTimer = Timer.builder("jvm.gc.duration")
            .description("JVM Garbage Collection Time")
            .register(meterRegistry);
            
        this.memoryUsage = Gauge.builder("jvm.memory.usage.ratio")
            .register(meterRegistry, this, PerformanceCollector::getMemoryUsageRatio);
            
        this.exceptionCounter = Counter.builder("application.exceptions.total")
            .register(meterRegistry);
            
        registerBusinessMetrics();
    }
    
    private void registerBusinessMetrics() {
        Gauge.builder("business.customers.count")
            .register(meterRegistry, this, collector -> 
                getTenantAwareMetric("customers", CustomerRepository::countActive));
        
        Timer.builder("business.calculation.duration")
            .description("Price calculation processing time")
            .register(meterRegistry);
    }
    
    @Scheduled(every = "30s")
    public void collectPerformanceMetrics() {
        collectJVMMetrics();
        collectDatabaseMetrics();
        collectTenantSpecificMetrics();
    }
    
    private void collectTenantSpecificMetrics() {
        List<TenantContext> activeTenants = getActiveTenants();
        
        for (TenantContext tenant : activeTenants) {
            tenantManager.setCurrentTenant(tenant);
            
            Tags tenantTags = Tags.of(
                "tenant_id", tenant.getTenantId().toString(),
                "subscription_tier", tenant.getSubscriptionTier().name()
            );
            
            long customerCount = customerRepository.countAll();
            double avgResponseTime = calculateAverageResponseTime(tenant);
            
            Gauge.builder("tenant.customers.total")
                .tags(tenantTags).register(meterRegistry, () -> customerCount);
            Gauge.builder("tenant.avg.response.time")
                .tags(tenantTags).register(meterRegistry, () -> avgResponseTime);
        }
    }
}
```

#### Intelligent Alert Manager:
```java
@ApplicationScoped
public class AlertManager {
    @Inject MeterRegistry meterRegistry;
    @Inject NotificationService notificationService;
    
    @ConfigProperty(name = "monitoring.alert.thresholds.cpu", defaultValue = "80")
    double cpuThreshold;
    
    private final Map<String, AlertState> alertStates = new ConcurrentHashMap<>();
    
    @Scheduled(every = "1m")
    public void checkAlertConditions() {
        checkSystemResourceAlerts();
        checkApplicationPerformanceAlerts();
        checkTenantSpecificAlerts();
    }
    
    private void checkSystemResourceAlerts() {
        double cpuUsage = getCurrentCpuUsage();
        if (cpuUsage > cpuThreshold) {
            triggerAlert(AlertType.HIGH_CPU_USAGE, 
                String.format("CPU usage is %.1f%% (threshold: %.1f%%)", cpuUsage, cpuThreshold),
                AlertSeverity.WARNING);
        }
        
        double errorRate = getCurrentErrorRate();
        if (errorRate > 5.0) {
            triggerAlert(AlertType.HIGH_ERROR_RATE, 
                String.format("Error rate is %.1f%%", errorRate),
                AlertSeverity.CRITICAL);
        }
    }
    
    private void triggerAlert(AlertType type, String message, AlertSeverity severity) {
        String alertKey = type.name();
        AlertState currentState = alertStates.get(alertKey);
        
        if (currentState == null || !currentState.isActive()) {
            AlertState newState = AlertState.builder()
                .type(type).message(message).severity(severity)
                .firstTriggered(LocalDateTime.now()).active(true).build();
                
            alertStates.put(alertKey, newState);
            sendAlert(newState);
        }
    }
    
    private void sendAlert(AlertState alert) {
        AlertNotification notification = AlertNotification.builder()
            .type(alert.getType()).severity(alert.getSeverity())
            .message(alert.getMessage()).build();
        
        switch (alert.getSeverity()) {
            case CRITICAL:
                notificationService.sendPagerDutyAlert(notification);
                notificationService.sendSlackAlert(notification, "#alerts-critical");
                break;
            case HIGH:
                notificationService.sendSlackAlert(notification, "#alerts-high");
                break;
            case WARNING:
                notificationService.sendSlackAlert(notification, "#monitoring");
                break;
        }
    }
}
```

#### Real User Monitoring (RUM):
```typescript
export class PerformanceTracker {
  private static instance: PerformanceTracker;
  private metricsQueue: PerformanceMetric[] = [];
  
  static getInstance(): PerformanceTracker {
    if (!PerformanceTracker.instance) {
      PerformanceTracker.instance = new PerformanceTracker();
    }
    return PerformanceTracker.instance;
  }
  
  init() {
    this.setupPerformanceObserver();
    this.trackCoreWebVitals();
    this.trackUserInteractions();
    this.setupBeaconSending();
  }
  
  private trackCoreWebVitals() {
    // Largest Contentful Paint (LCP)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lcp = entries[entries.length - 1];
      
      this.recordMetric({
        name: 'largest_contentful_paint',
        value: lcp.startTime,
        timestamp: Date.now(),
        page: window.location.pathname
      });
    }).observe({ entryTypes: ['largest-contentful-paint'] });
    
    // First Input Delay (FID)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const fid = entries[0];
      
      if (fid) {
        this.recordMetric({
          name: 'first_input_delay',
          value: (fid as any).processingStart - fid.startTime,
          timestamp: Date.now(),
          page: window.location.pathname
        });
      }
    }).observe({ entryTypes: ['first-input'] });
    
    // Cumulative Layout Shift (CLS)
    let clsValue = 0;
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach(entry => {
        if (!(entry as any).hadRecentInput) {
          clsValue += (entry as any).value;
        }
      });
      
      this.recordMetric({
        name: 'cumulative_layout_shift',
        value: clsValue,
        timestamp: Date.now(),
        page: window.location.pathname
      });
    }).observe({ entryTypes: ['layout-shift'] });
  }
  
  public trackCustomEvent(eventName: string, duration: number, metadata: any = {}) {
    this.recordMetric({
      name: `custom_${eventName}`,
      value: duration,
      timestamp: Date.now(),
      page: window.location.pathname,
      ...metadata
    });
  }
  
  public startTimer(timerName: string): () => void {
    const startTime = performance.now();
    
    return () => {
      const endTime = performance.now();
      const duration = endTime - startTime;
      
      this.recordMetric({
        name: `timer_${timerName}`,
        value: duration,
        timestamp: Date.now(),
        page: window.location.pathname
      });
    };
  }
  
  private async sendMetrics(useBeacon: boolean = false) {
    if (this.metricsQueue.length === 0) return;
    
    const payload = {
      metrics: [...this.metricsQueue],
      timestamp: Date.now()
    };
    this.metricsQueue = [];
    
    if (useBeacon && 'sendBeacon' in navigator) {
      navigator.sendBeacon('/api/monitoring/frontend-metrics', JSON.stringify(payload));
    } else {
      await fetch('/api/monitoring/frontend-metrics', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }
  }
}

// React Hook für Component Performance
export const usePerformanceTracking = (componentName: string) => {
  const performanceTracker = PerformanceTracker.getInstance();
  
  useEffect(() => {
    const stopTimer = performanceTracker.startTimer(`component_render_${componentName}`);
    return stopTimer;
  }, [componentName]);
  
  const trackUserAction = useCallback((actionName: string, metadata?: any) => {
    const stopTimer = performanceTracker.startTimer(`user_action_${actionName}`);
    
    return (additionalMetadata?: any) => {
      stopTimer();
      performanceTracker.trackCustomEvent(actionName, 1, {
        component: componentName, ...metadata, ...additionalMetadata
      });
    };
  }, [componentName]);
  
  return { trackUserAction };
};
```

#### Web Vitals Dashboard:
```typescript
export const WebVitalsDashboard: React.FC = () => {
  const [vitals, setVitals] = useState<WebVitalsData | null>(null);
  const [timeRange, setTimeRange] = useState<TimeRange>('24h');
  
  useEffect(() => {
    loadWebVitals();
    const interval = setInterval(loadWebVitals, 60000);
    return () => clearInterval(interval);
  }, [timeRange]);
  
  const loadWebVitals = async () => {
    const data = await monitoringApi.getWebVitals({ timeRange });
    setVitals(data);
  };
  
  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      <Typography variant="h4" fontFamily="Antonio Bold" sx={{ color: '#004F7B', mb: 3 }}>
        Web Vitals Dashboard
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <WebVitalCard
            title="Largest Contentful Paint"
            value={vitals?.lcp?.median || 0}
            threshold={{ good: 2500, poor: 4000 }}
            unit="ms"
            description="Misst die wahrgenommene Ladeperformance"
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <WebVitalCard
            title="First Input Delay"
            value={vitals?.fid?.median || 0}
            threshold={{ good: 100, poor: 300 }}
            unit="ms"
            description="Misst die Interaktivität"
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <WebVitalCard
            title="Cumulative Layout Shift"
            value={vitals?.cls?.median || 0}
            threshold={{ good: 0.1, poor: 0.25 }}
            unit=""
            description="Misst die visuelle Stabilität"
          />
        </Grid>
        
        <Grid item xs={12} md={8}>
          <Card>
            <CardHeader title="Performance Trends" />
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={vitals?.trends || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="timestamp" />
                  <YAxis />
                  <Line type="monotone" dataKey="lcp" stroke="#94C456" name="LCP (ms)" />
                  <Line type="monotone" dataKey="fid" stroke="#004F7B" name="FID (ms)" />
                  <Line type="monotone" dataKey="cls" stroke="#FFA726" name="CLS (×100)" />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};
```

## 🏗️ ARCHITEKTUR ÜBERBLICK

### Multi-Layer Monitoring Approach:
```
Monitoring Stack:
├── Backend Performance    # JVM, Database, Application Metrics
├── Frontend Performance   # Core Web Vitals, Real User Monitoring
├── Business Metrics      # Customer Count, Revenue, Conversions
├── Tenant-Specific SLAs  # Multi-tenant performance guarantees
└── Intelligent Alerting  # Severity-based escalation
```

### Performance Data Flow:
- **Real-time Collection:** 30s intervals für Backend, kontinuierlich für Frontend
- **Smart Aggregation:** Tenant-spezifische Metriken mit SLA-Enforcement
- **Proactive Alerting:** 90% Issues erkannt vor User Impact
- **Auto-Scaling Integration:** Performance-basierte Ressourcen-Optimierung

## 📊 BUSINESS VALUE

### Proaktive System-Stabilität:
- **99.9% Uptime:** Durch frühzeitige Problemekennung
- **< 5s MTTR:** Automatische Alert-Eskalation zu On-Call Teams
- **Performance SLAs:** Tenant-spezifische Garantien per Subscription Tier

### Cost Optimization:
- **50% Infrastruktur-Einsparung:** Durch intelligente Auto-Scaling
- **Resource Right-Sizing:** Basierend auf tatsächlichen Performance-Daten
- **Capacity Planning:** Predictive Scaling basierend auf Business Metrics

### User Experience:
- **Core Web Vitals:** < 2.5s LCP, < 100ms FID, < 0.1 CLS
- **Real User Monitoring:** Echte Performance-Daten statt synthetische Tests
- **Performance Budget:** Automatische Warnung bei Performance-Regression

## 🔄 ABHÄNGIGKEITEN

### Benötigt:
- **Micrometer Metrics** - für Backend Monitoring ✅
- **Frontend Web APIs** - Performance Observer, Navigator Beacon ✅

### Ermöglicht:
- **Auto-Scaling** - Performance-basierte Skalierung
- **SLA Management** - Tenant-spezifische Performance-Garantien
- **Proactive Support** - Issues lösen bevor User betroffen sind

### Integriert mit:
- **FC-038 Multi-Tenant** - Tenant-spezifische Metriken
- **FC-039 API Gateway** - Gateway Performance Monitoring
- **Alle Features** - Performance Impact Measurement

## 🧪 TESTING STRATEGY

### Performance Test Suite:
```typescript
describe('Performance Monitoring', () => {
  test('sollte Core Web Vitals korrekt tracken', async () => {
    const tracker = PerformanceTracker.getInstance();
    
    // Simuliere LCP Event
    const mockLCP = new PerformanceEntry();
    mockLCP.startTime = 2000; // 2 seconds
    
    tracker.processPerformanceEntry(mockLCP);
    
    expect(tracker.getMetrics()).toContainEqual(
      expect.objectContaining({
        name: 'largest_contentful_paint',
        value: 2000
      })
    );
  });
  
  test('sollte kritische Metriken sofort senden', async () => {
    const tracker = PerformanceTracker.getInstance();
    
    tracker.trackCustomEvent('javascript_error', 1);
    
    expect(fetch).toHaveBeenCalledWith('/api/monitoring/frontend-metrics', 
      expect.objectContaining({ method: 'POST' }));
  });
});
```

### Alert Testing:
```java
@Test
void testCriticalAlertEscalation() {
    alertManager.triggerAlert(AlertType.HIGH_ERROR_RATE, 
        "Error rate exceeds 5%", AlertSeverity.CRITICAL);
    
    verify(notificationService).sendPagerDutyAlert(any());
    verify(notificationService).sendSlackAlert(any(), eq("#alerts-critical"));
}
```

## 🚀 IMPLEMENTATION PLAN

### Phase 1: Backend Performance Monitoring (2 Tage)
- JVM Metrics Collection (Memory, GC, Threads)
- Database Performance Monitoring (Connection Pools, Query Times)
- Tenant-Specific Business Metrics
- Basic Alert Setup

### Phase 2: Frontend Performance Monitoring (2 Tage)
- Core Web Vitals Implementation (LCP, FID, CLS)
- Real User Monitoring (RUM) Setup
- Error Tracking & Reporting
- Performance Dashboard UI

### Phase 3: Intelligent Alerting & Auto-Scaling (2 Tage)
- Severity-Based Alert Escalation
- Tenant-Specific SLA Enforcement
- Auto-Scaling Integration
- Performance Optimization Recommendations

## 📈 SUCCESS CRITERIA

### System Availability:
- ✅ 99.9% Uptime mit < 5 Sekunden MTTR
- ✅ 90% Performance Issues proaktiv erkannt
- ✅ Tenant-spezifische SLAs implementiert

### Frontend Performance:
- ✅ Core Web Vitals Tracking für alle kritischen User Flows
- ✅ Real User Monitoring funktionsfähig
- ✅ Performance Budget Enforcement

### Business Impact:
- ✅ 50% reduzierte Infrastruktur-Kosten durch optimales Auto-Scaling
- ✅ 95% User Experience Score (Lighthouse)
- ✅ Break-even nach 2 Monaten durch reduzierte Downtime

### Alert System:
- ✅ Intelligent Alerting mit Severity-basierter Eskalation
- ✅ Auto-Resolution für temporäre Issues
- ✅ Performance Dashboard mit Real-time Metriken

---
**📊 FC-040 Performance Monitoring - Ready for Proactive Excellence!**  
**Optimiert für:** 6 Tage | **ROI:** 50% Infrastruktur-Einsparung + 99.9% Uptime | **Performance:** < 5s MTTR