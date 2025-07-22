# 📊 FC-040: Performance Monitoring - TECH CONCEPT

**Feature-Code:** FC-040  
**Feature-Name:** Performance Monitoring  
**Kategorie:** Operations & Observability  
**Priorität:** HIGH  
**Geschätzter Aufwand:** 6 Tage  
**Status:** 📋 PLANNED - Tech Concept verfügbar  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### ⚡ SOFORT STARTEN (2 Minuten):
```bash
# 1. Performance Monitoring Backend Setup
cd backend/src/main/java/de/freshplan/infrastructure
mkdir monitoring && cd monitoring
touch PerformanceCollector.java AlertManager.java MetricsAggregator.java

# 2. Frontend Performance Tracking
cd frontend/src/services
mkdir monitoring && cd monitoring
touch PerformanceTracker.ts WebVitalsCollector.ts UserExperienceMonitor.ts
```

### 📋 IMPLEMENTIERUNGS-CHECKLISTE:
- [ ] **Phase 1:** Backend Performance Metrics (Tag 1-2)
- [ ] **Phase 2:** Frontend/UX Monitoring (Tag 3-4)  
- [ ] **Phase 3:** Alerting & Auto-Scaling (Tag 5-6)

---

## 🎯 FEATURE OVERVIEW

### Was ist Performance Monitoring?
Umfassendes Performance-Monitoring-System mit Real-time Metriken für Backend-Services, Frontend-Performance, User Experience und automatischen Alerting. Optimiert für Multi-Tenant-Umgebungen mit tenant-spezifischen SLAs und proaktiven Performance-Optimierungen.

### Business Value
- **Proaktive Problemerkennung** mit 99.9% Uptime durch frühzeitige Warnungen
- **User Experience Optimization** mit Core Web Vitals und Real User Monitoring
- **Cost Optimization** durch intelligente Auto-Scaling basierend auf Performance-Daten
- **SLA Compliance** mit tenant-spezifischen Performance-Garantien

### Erfolgsmetriken
- 99.9% System Availability mit < 5 Sekunden MTTR für kritische Issues
- 90% Performance Issues werden proaktiv erkannt (vor User Impact)
- 50% reduzierte Infrastruktur-Kosten durch optimales Auto-Scaling
- 95% User Experience Score (Lighthouse) für alle kritischen User Flows

---

## 🏗️ BACKEND PERFORMANCE MONITORING

### Comprehensive Metrics Collection
```java
@ApplicationScoped
public class PerformanceCollector {
    
    @Inject
    MeterRegistry meterRegistry;
    
    @Inject
    TenantContextManager tenantManager;
    
    @Inject
    DatabaseMetricsCollector dbMetrics;
    
    private final Timer jvmGcTimer;
    private final Gauge memoryUsage;
    private final Counter exceptionCounter;
    private final DistributionSummary requestProcessingTime;
    
    @PostConstruct
    void initializeMetrics() {
        // JVM Metrics
        this.jvmGcTimer = Timer.builder("jvm.gc.duration")
            .description("JVM Garbage Collection Time")
            .register(meterRegistry);
            
        this.memoryUsage = Gauge.builder("jvm.memory.usage.ratio")
            .description("JVM Memory Usage Ratio")
            .register(meterRegistry, this, PerformanceCollector::getMemoryUsageRatio);
            
        // Application Metrics
        this.exceptionCounter = Counter.builder("application.exceptions.total")
            .description("Total Application Exceptions")
            .register(meterRegistry);
            
        this.requestProcessingTime = DistributionSummary.builder("application.request.processing.time")
            .description("Request Processing Time Distribution")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(meterRegistry);
            
        // Custom Business Metrics
        registerBusinessMetrics();
        
        // Setup periodic collection
        setupPeriodicMetrics();
    }
    
    private void registerBusinessMetrics() {
        // Customer-related performance metrics
        Gauge.builder("business.customers.count")
            .description("Total number of active customers")
            .register(meterRegistry, this, collector -> 
                getTenantAwareMetric("customers", CustomerRepository::countActive));
        
        Gauge.builder("business.opportunities.active")
            .description("Number of active opportunities")
            .register(meterRegistry, this, collector -> 
                getTenantAwareMetric("opportunities", OpportunityRepository::countActive));
        
        // Performance-critical operation metrics
        Timer.builder("business.calculation.duration")
            .description("Price calculation processing time")
            .register(meterRegistry);
            
        Timer.builder("business.report.generation.duration")
            .description("Report generation processing time")
            .register(meterRegistry);
    }
    
    @Scheduled(every = "30s")
    public void collectPerformanceMetrics() {
        collectJVMMetrics();
        collectDatabaseMetrics();
        collectTenantSpecificMetrics();
        collectApplicationMetrics();
    }
    
    private void collectJVMMetrics() {
        // Memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        Gauge.builder("jvm.memory.heap.used")
            .register(meterRegistry, heapUsage, MemoryUsage::getUsed);
            
        Gauge.builder("jvm.memory.heap.max")
            .register(meterRegistry, heapUsage, MemoryUsage::getMax);
            
        Gauge.builder("jvm.memory.nonheap.used")
            .register(meterRegistry, nonHeapUsage, MemoryUsage::getUsed);
        
        // Thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Gauge.builder("jvm.threads.live")
            .register(meterRegistry, threadBean, ThreadMXBean::getThreadCount);
            
        Gauge.builder("jvm.threads.daemon")
            .register(meterRegistry, threadBean, ThreadMXBean::getDaemonThreadCount);
        
        // GC metrics
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            String gcName = gcBean.getName().replace(" ", "_");
            
            Gauge.builder("jvm.gc.collections.count")
                .tag("gc", gcName)
                .register(meterRegistry, gcBean, GarbageCollectorMXBean::getCollectionCount);
                
            Gauge.builder("jvm.gc.collections.time")
                .tag("gc", gcName)
                .register(meterRegistry, gcBean, GarbageCollectorMXBean::getCollectionTime);
        }
    }
    
    private void collectDatabaseMetrics() {
        // Connection pool metrics per tenant
        List<String> tenantSchemas = getTenantSchemas();
        
        for (String schema : tenantSchemas) {
            HikariDataSource dataSource = tenantDataSourceRouter.getTenantDataSource(schema);
            HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
            
            Tags tenantTags = Tags.of("tenant_schema", schema);
            
            Gauge.builder("database.connections.active")
                .tags(tenantTags)
                .register(meterRegistry, poolBean, HikariPoolMXBean::getActiveConnections);
                
            Gauge.builder("database.connections.idle")
                .tags(tenantTags)
                .register(meterRegistry, poolBean, HikariPoolMXBean::getIdleConnections);
                
            Gauge.builder("database.connections.waiting")
                .tags(tenantTags)
                .register(meterRegistry, poolBean, HikariPoolMXBean::getThreadsAwaitingConnection);
        }
        
        // Query performance metrics
        Timer.builder("database.query.execution.time")
            .description("Database query execution time")
            .register(meterRegistry);
            
        Counter.builder("database.query.slow.count")
            .description("Count of slow queries (> 1 second)")
            .register(meterRegistry);
    }
    
    private void collectTenantSpecificMetrics() {
        List<TenantContext> activeTenants = getActiveTenants();
        
        for (TenantContext tenant : activeTenants) {
            tenantManager.setCurrentTenant(tenant);
            
            try {
                Tags tenantTags = Tags.of(
                    "tenant_id", tenant.getTenantId().toString(),
                    "tenant_name", tenant.getSubdomain(),
                    "subscription_tier", tenant.getSubscriptionTier().name()
                );
                
                // Business metrics per tenant
                long customerCount = customerRepository.countAll();
                long activeOpportunities = opportunityRepository.countByStatus("ACTIVE");
                double avgResponseTime = calculateAverageResponseTime(tenant);
                
                Gauge.builder("tenant.customers.total")
                    .tags(tenantTags)
                    .register(meterRegistry, () -> customerCount);
                    
                Gauge.builder("tenant.opportunities.active")
                    .tags(tenantTags)
                    .register(meterRegistry, () -> activeOpportunities);
                    
                Gauge.builder("tenant.avg.response.time")
                    .tags(tenantTags)
                    .register(meterRegistry, () -> avgResponseTime);
                    
                // Resource utilization per tenant
                collectTenantResourceMetrics(tenant, tenantTags);
                
            } finally {
                tenantManager.clearCurrentTenant();
            }
        }
    }
    
    private void collectTenantResourceMetrics(TenantContext tenant, Tags tenantTags) {
        // Database query count and performance
        long queryCount = getTenantQueryCount(tenant);
        double avgQueryTime = getTenantAverageQueryTime(tenant);
        long storageUsed = getTenantStorageUsage(tenant);
        
        Gauge.builder("tenant.database.queries.count")
            .tags(tenantTags)
            .register(meterRegistry, () -> queryCount);
            
        Gauge.builder("tenant.database.queries.avg.time")
            .tags(tenantTags)
            .register(meterRegistry, () -> avgQueryTime);
            
        Gauge.builder("tenant.storage.used.bytes")
            .tags(tenantTags)
            .register(meterRegistry, () -> storageUsed);
    }
    
    public void recordSlowQuery(String query, long executionTimeMs, TenantContext tenant) {
        if (executionTimeMs > 1000) { // Slow query threshold: 1 second
            Counter.builder("database.query.slow.count")
                .tag("tenant", tenant.getSubdomain())
                .tag("query_type", extractQueryType(query))
                .register(meterRegistry)
                .increment();
                
            Log.warn("Slow query detected for tenant {}: {}ms - {}", 
                tenant.getSubdomain(), executionTimeMs, query);
        }
    }
    
    private double getMemoryUsageRatio() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        return (double) heapUsage.getUsed() / heapUsage.getMax();
    }
}
```

### Intelligent Alerting System
```java
@ApplicationScoped
public class AlertManager {
    
    @Inject
    MeterRegistry meterRegistry;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    @ConfigProperty(name = "monitoring.alert.thresholds.cpu", defaultValue = "80")
    double cpuThreshold;
    
    @Inject
    @ConfigProperty(name = "monitoring.alert.thresholds.memory", defaultValue = "85")
    double memoryThreshold;
    
    @Inject
    @ConfigProperty(name = "monitoring.alert.thresholds.response.time", defaultValue = "5000")
    long responseTimeThreshold;
    
    private final Map<String, AlertState> alertStates = new ConcurrentHashMap<>();
    
    @Scheduled(every = "1m")
    public void checkAlertConditions() {
        checkSystemResourceAlerts();
        checkApplicationPerformanceAlerts();
        checkTenantSpecificAlerts();
        checkBusinessMetricAlerts();
    }
    
    private void checkSystemResourceAlerts() {
        // CPU Usage Alert
        double cpuUsage = getCurrentCpuUsage();
        if (cpuUsage > cpuThreshold) {
            triggerAlert(AlertType.HIGH_CPU_USAGE, 
                String.format("CPU usage is %.1f%% (threshold: %.1f%%)", cpuUsage, cpuThreshold),
                AlertSeverity.WARNING);
        }
        
        // Memory Usage Alert
        double memoryUsage = getCurrentMemoryUsage();
        if (memoryUsage > memoryThreshold) {
            triggerAlert(AlertType.HIGH_MEMORY_USAGE, 
                String.format("Memory usage is %.1f%% (threshold: %.1f%%)", memoryUsage, memoryThreshold),
                AlertSeverity.WARNING);
        }
        
        // Disk Space Alert
        double diskUsage = getCurrentDiskUsage();
        if (diskUsage > 90) {
            triggerAlert(AlertType.LOW_DISK_SPACE, 
                String.format("Disk usage is %.1f%%", diskUsage),
                AlertSeverity.CRITICAL);
        }
    }
    
    private void checkApplicationPerformanceAlerts() {
        // Response Time Alert
        double avgResponseTime = getAverageResponseTime();
        if (avgResponseTime > responseTimeThreshold) {
            triggerAlert(AlertType.HIGH_RESPONSE_TIME, 
                String.format("Average response time is %.0fms (threshold: %dms)", 
                    avgResponseTime, responseTimeThreshold),
                AlertSeverity.WARNING);
        }
        
        // Error Rate Alert
        double errorRate = getCurrentErrorRate();
        if (errorRate > 5.0) { // 5% error rate threshold
            triggerAlert(AlertType.HIGH_ERROR_RATE, 
                String.format("Error rate is %.1f%% (threshold: 5.0%%)", errorRate),
                AlertSeverity.CRITICAL);
        }
        
        // Database Connection Pool Alert
        checkDatabaseConnectionPoolAlerts();
    }
    
    private void checkTenantSpecificAlerts() {
        List<TenantContext> activeTenants = getActiveTenants();
        
        for (TenantContext tenant : activeTenants) {
            // Tenant-specific SLA thresholds based on subscription tier
            PerformanceSLA sla = getPerformanceSLA(tenant.getSubscriptionTier());
            
            double tenantResponseTime = getTenantAverageResponseTime(tenant);
            if (tenantResponseTime > sla.getMaxResponseTime()) {
                triggerTenantAlert(tenant, AlertType.TENANT_SLA_VIOLATION, 
                    String.format("Response time %.0fms exceeds SLA limit of %.0fms", 
                        tenantResponseTime, sla.getMaxResponseTime()),
                    AlertSeverity.HIGH);
            }
            
            double tenantErrorRate = getTenantErrorRate(tenant);
            if (tenantErrorRate > sla.getMaxErrorRate()) {
                triggerTenantAlert(tenant, AlertType.TENANT_SLA_VIOLATION, 
                    String.format("Error rate %.1f%% exceeds SLA limit of %.1f%%", 
                        tenantErrorRate, sla.getMaxErrorRate()),
                    AlertSeverity.HIGH);
            }
            
            // Resource usage alerts per tenant
            long tenantStorageUsed = getTenantStorageUsage(tenant);
            long tenantStorageLimit = tenant.getStorageLimit();
            if (tenantStorageUsed > tenantStorageLimit * 0.9) { // 90% of limit
                triggerTenantAlert(tenant, AlertType.TENANT_STORAGE_LIMIT, 
                    String.format("Storage usage %.1fGB exceeds 90%% of limit %.1fGB", 
                        tenantStorageUsed / 1_000_000_000.0, tenantStorageLimit / 1_000_000_000.0),
                    AlertSeverity.WARNING);
            }
        }
    }
    
    private void triggerAlert(AlertType type, String message, AlertSeverity severity) {
        String alertKey = type.name();
        AlertState currentState = alertStates.get(alertKey);
        
        // Implement alert debouncing - only trigger if condition persists
        if (currentState == null || !currentState.isActive()) {
            AlertState newState = AlertState.builder()
                .type(type)
                .message(message)
                .severity(severity)
                .firstTriggered(LocalDateTime.now())
                .lastTriggered(LocalDateTime.now())
                .active(true)
                .triggerCount(1)
                .build();
                
            alertStates.put(alertKey, newState);
            sendAlert(newState);
            
        } else {
            // Update existing alert
            currentState.setLastTriggered(LocalDateTime.now());
            currentState.setTriggerCount(currentState.getTriggerCount() + 1);
            
            // Send escalation if alert persists
            if (currentState.getTriggerCount() % 5 == 0) { // Every 5 minutes
                sendEscalationAlert(currentState);
            }
        }
    }
    
    private void sendAlert(AlertState alert) {
        // Send to different channels based on severity
        AlertNotification notification = AlertNotification.builder()
            .type(alert.getType())
            .severity(alert.getSeverity())
            .message(alert.getMessage())
            .timestamp(alert.getFirstTriggered())
            .build();
        
        switch (alert.getSeverity()) {
            case CRITICAL:
                // Send to on-call team immediately
                notificationService.sendPagerDutyAlert(notification);
                notificationService.sendSlackAlert(notification, "#alerts-critical");
                notificationService.sendEmailAlert(notification, getOnCallTeam());
                break;
                
            case HIGH:
                // Send to team channels
                notificationService.sendSlackAlert(notification, "#alerts-high");
                notificationService.sendEmailAlert(notification, getDevTeam());
                break;
                
            case WARNING:
                // Send to monitoring channels
                notificationService.sendSlackAlert(notification, "#monitoring");
                break;
                
            case INFO:
                // Log only
                Log.info("Performance alert: {}", notification);
                break;
        }
    }
    
    public void resolveAlert(AlertType type) {
        String alertKey = type.name();
        AlertState state = alertStates.get(alertKey);
        
        if (state != null && state.isActive()) {
            state.setActive(false);
            state.setResolvedAt(LocalDateTime.now());
            
            // Send resolution notification
            notificationService.sendAlertResolution(state);
        }
    }
}
```

---

## 🎨 FRONTEND PERFORMANCE MONITORING

### Real User Monitoring (RUM)
```typescript
export class PerformanceTracker {
  private static instance: PerformanceTracker;
  private metricsQueue: PerformanceMetric[] = [];
  private observer: PerformanceObserver | null = null;
  
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
    this.trackResourceLoading();
    this.setupBeaconSending();
  }
  
  private setupPerformanceObserver() {
    if ('PerformanceObserver' in window) {
      this.observer = new PerformanceObserver((list) => {
        const entries = list.getEntries();
        
        entries.forEach(entry => {
          this.processPerformanceEntry(entry);
        });
      });
      
      // Observe different types of performance entries
      this.observer.observe({ 
        entryTypes: ['navigation', 'resource', 'paint', 'layout-shift', 'largest-contentful-paint']
      });
    }
  }
  
  private trackCoreWebVitals() {
    // First Contentful Paint (FCP)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const fcp = entries.find(entry => entry.name === 'first-contentful-paint');
      
      if (fcp) {
        this.recordMetric({
          name: 'first_contentful_paint',
          value: fcp.startTime,
          timestamp: Date.now(),
          page: window.location.pathname,
          userAgent: navigator.userAgent,
          connectionType: (navigator as any).connection?.effectiveType || 'unknown'
        });
      }
    }).observe({ entryTypes: ['paint'] });
    
    // Largest Contentful Paint (LCP)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lcp = entries[entries.length - 1]; // LCP is the last entry
      
      this.recordMetric({
        name: 'largest_contentful_paint',
        value: lcp.startTime,
        timestamp: Date.now(),
        page: window.location.pathname,
        element: (lcp as any).element?.tagName || 'unknown'
      });
    }).observe({ entryTypes: ['largest-contentful-paint'] });
    
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
    
    // First Input Delay (FID)
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const fid = entries[0];
      
      if (fid) {
        this.recordMetric({
          name: 'first_input_delay',
          value: (fid as any).processingStart - fid.startTime,
          timestamp: Date.now(),
          page: window.location.pathname,
          eventType: (fid as any).name
        });
      }
    }).observe({ entryTypes: ['first-input'] });
  }
  
  private trackUserInteractions() {
    // Track click interactions
    document.addEventListener('click', (event) => {
      const target = event.target as HTMLElement;
      const interactionStart = performance.now();
      
      // Use requestIdleCallback to measure interaction response time
      requestIdleCallback(() => {
        const interactionEnd = performance.now();
        const duration = interactionEnd - interactionStart;
        
        this.recordMetric({
          name: 'user_interaction',
          value: duration,
          timestamp: Date.now(),
          page: window.location.pathname,
          elementType: target.tagName,
          elementId: target.id || 'unknown',
          elementClass: target.className || 'unknown'
        });
      });
    });
    
    // Track form submissions
    document.addEventListener('submit', (event) => {
      const form = event.target as HTMLFormElement;
      
      this.recordMetric({
        name: 'form_submission',
        value: 1,
        timestamp: Date.now(),
        page: window.location.pathname,
        formId: form.id || 'unknown',
        formAction: form.action || 'unknown'
      });
    });
  }
  
  private trackResourceLoading() {
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      
      entries.forEach(entry => {
        const resourceEntry = entry as PerformanceResourceTiming;
        
        // Track slow resources (> 2 seconds)
        if (resourceEntry.duration > 2000) {
          this.recordMetric({
            name: 'slow_resource',
            value: resourceEntry.duration,
            timestamp: Date.now(),
            page: window.location.pathname,
            resourceUrl: resourceEntry.name,
            resourceType: this.getResourceType(resourceEntry.name),
            transferSize: resourceEntry.transferSize || 0
          });
        }
        
        // Track API calls
        if (resourceEntry.name.includes('/api/')) {
          this.recordMetric({
            name: 'api_call',
            value: resourceEntry.duration,
            timestamp: Date.now(),
            page: window.location.pathname,
            apiEndpoint: this.extractApiEndpoint(resourceEntry.name),
            method: 'GET', // Default, can be enhanced with request interceptors
            statusCode: this.extractStatusFromHeaders(resourceEntry)
          });
        }
      });
    }).observe({ entryTypes: ['resource'] });
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
  
  public trackError(error: Error, context: string = 'unknown') {
    this.recordMetric({
      name: 'javascript_error',
      value: 1,
      timestamp: Date.now(),
      page: window.location.pathname,
      errorMessage: error.message,
      errorStack: error.stack || 'no_stack',
      context: context
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
  
  private recordMetric(metric: PerformanceMetric) {
    // Add tenant context
    const tenantId = this.getCurrentTenantId();
    const userId = this.getCurrentUserId();
    
    const enrichedMetric = {
      ...metric,
      tenantId,
      userId,
      sessionId: this.getSessionId(),
      buildVersion: this.getBuildVersion(),
      browserInfo: this.getBrowserInfo()
    };
    
    this.metricsQueue.push(enrichedMetric);
    
    // Send immediately for critical metrics
    if (this.isCriticalMetric(metric.name)) {
      this.sendMetrics();
    }
  }
  
  private setupBeaconSending() {
    // Send metrics every 30 seconds
    setInterval(() => {
      this.sendMetrics();
    }, 30000);
    
    // Send metrics on page unload
    window.addEventListener('beforeunload', () => {
      this.sendMetrics(true); // Use beacon for reliable sending
    });
    
    // Send metrics when page becomes hidden
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'hidden') {
        this.sendMetrics(true);
      }
    });
  }
  
  private async sendMetrics(useBeacon: boolean = false) {
    if (this.metricsQueue.length === 0) return;
    
    const metricsToSend = [...this.metricsQueue];
    this.metricsQueue = [];
    
    const payload = {
      metrics: metricsToSend,
      timestamp: Date.now(),
      batchId: this.generateBatchId()
    };
    
    try {
      if (useBeacon && 'sendBeacon' in navigator) {
        // Use beacon for reliable sending during page unload
        navigator.sendBeacon(
          '/api/monitoring/frontend-metrics',
          JSON.stringify(payload)
        );
      } else {
        // Use regular fetch for normal sending
        await fetch('/api/monitoring/frontend-metrics', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(payload)
        });
      }
    } catch (error) {
      console.warn('Failed to send performance metrics:', error);
      // Re-queue metrics for retry
      this.metricsQueue.unshift(...metricsToSend);
    }
  }
  
  private isCriticalMetric(metricName: string): boolean {
    const criticalMetrics = [
      'javascript_error',
      'api_call',
      'form_submission'
    ];
    return criticalMetrics.includes(metricName);
  }
  
  private getBrowserInfo() {
    return {
      userAgent: navigator.userAgent,
      viewport: {
        width: window.innerWidth,
        height: window.innerHeight
      },
      screen: {
        width: screen.width,
        height: screen.height
      },
      connectionType: (navigator as any).connection?.effectiveType || 'unknown',
      language: navigator.language
    };
  }
}

// React Hook for Component Performance Tracking
export const usePerformanceTracking = (componentName: string) => {
  const performanceTracker = PerformanceTracker.getInstance();
  
  useEffect(() => {
    const stopTimer = performanceTracker.startTimer(`component_render_${componentName}`);
    
    return () => {
      stopTimer();
    };
  }, [componentName, performanceTracker]);
  
  const trackUserAction = useCallback((actionName: string, metadata?: any) => {
    const stopTimer = performanceTracker.startTimer(`user_action_${actionName}`);
    
    return (additionalMetadata?: any) => {
      stopTimer();
      performanceTracker.trackCustomEvent(actionName, 1, {
        component: componentName,
        ...metadata,
        ...additionalMetadata
      });
    };
  }, [componentName, performanceTracker]);
  
  return { trackUserAction };
};
```

### Web Vitals Dashboard Component
```typescript
export const WebVitalsDashboard: React.FC = () => {
  const [vitals, setVitals] = useState<WebVitalsData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [timeRange, setTimeRange] = useState<TimeRange>('24h');
  
  useEffect(() => {
    loadWebVitals();
    const interval = setInterval(loadWebVitals, 60000); // Update every minute
    
    return () => clearInterval(interval);
  }, [timeRange]);
  
  const loadWebVitals = async () => {
    try {
      const data = await monitoringApi.getWebVitals({ timeRange });
      setVitals(data);
    } catch (error) {
      console.error('Failed to load web vitals', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  if (isLoading) {
    return <WebVitalsSkeleton />;
  }
  
  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography 
          variant="h4" 
          fontFamily="Antonio Bold"
          sx={{ color: '#004F7B' }}
        >
          Web Vitals Dashboard
        </Typography>
        
        <FormControl size="small">
          <InputLabel>Zeitraum</InputLabel>
          <Select
            value={timeRange}
            onChange={(e) => setTimeRange(e.target.value as TimeRange)}
            label="Zeitraum"
          >
            <MenuItem value="1h">Letzte Stunde</MenuItem>
            <MenuItem value="24h">Letzte 24 Stunden</MenuItem>
            <MenuItem value="7d">Letzte 7 Tage</MenuItem>
            <MenuItem value="30d">Letzte 30 Tage</MenuItem>
          </Select>
        </FormControl>
      </Box>
      
      <Grid container spacing={3}>
        {/* Core Web Vitals Overview */}
        <Grid item xs={12} md={4}>
          <WebVitalCard
            title="Largest Contentful Paint"
            value={vitals?.lcp?.median || 0}
            threshold={{ good: 2500, poor: 4000 }}
            unit="ms"
            description="Misst die wahrgenommene Ladeperformance"
            trend={vitals?.lcp?.trend}
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <WebVitalCard
            title="First Input Delay"
            value={vitals?.fid?.median || 0}
            threshold={{ good: 100, poor: 300 }}
            unit="ms"
            description="Misst die Interaktivität"
            trend={vitals?.fid?.trend}
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <WebVitalCard
            title="Cumulative Layout Shift"
            value={vitals?.cls?.median || 0}
            threshold={{ good: 0.1, poor: 0.25 }}
            unit=""
            description="Misst die visuelle Stabilität"
            trend={vitals?.cls?.trend}
          />
        </Grid>
        
        {/* Performance Trends */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardHeader title="Performance Trends" />
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={vitals?.trends || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="timestamp" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="lcp" 
                    stroke="#94C456" 
                    name="LCP (ms)"
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="fid" 
                    stroke="#004F7B" 
                    name="FID (ms)"
                    strokeWidth={2}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="cls" 
                    stroke="#FFA726" 
                    name="CLS (×100)"
                    strokeWidth={2}
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Performance Score */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardHeader title="Lighthouse Score" />
            <CardContent sx={{ textAlign: 'center' }}>
              <Box sx={{ position: 'relative', display: 'inline-flex' }}>
                <CircularProgress
                  variant="determinate"
                  value={vitals?.lighthouseScore || 0}
                  size={120}
                  thickness={4}
                  sx={{
                    color: getScoreColor(vitals?.lighthouseScore || 0),
                  }}
                />
                <Box
                  sx={{
                    top: 0,
                    left: 0,
                    bottom: 0,
                    right: 0,
                    position: 'absolute',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Typography
                    variant="h4"
                    fontFamily="Antonio Bold"
                    component="div"
                    color="text.secondary"
                  >
                    {vitals?.lighthouseScore || 0}
                  </Typography>
                </Box>
              </Box>
              
              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                Gesamt-Performance Score
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

const WebVitalCard: React.FC<WebVitalCardProps> = ({
  title,
  value,
  threshold,
  unit,
  description,
  trend
}) => {
  const getVitalStatus = (value: number) => {
    if (value <= threshold.good) return 'good';
    if (value <= threshold.poor) return 'needs-improvement';
    return 'poor';
  };
  
  const status = getVitalStatus(value);
  const statusColor = {
    good: '#94C456',
    'needs-improvement': '#FFA726',
    poor: '#FF5722'
  }[status];
  
  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            {title}
          </Typography>
          <Chip
            label={status === 'good' ? 'Gut' : status === 'needs-improvement' ? 'Verbesserung' : 'Schlecht'}
            size="small"
            sx={{ 
              bgcolor: statusColor,
              color: 'white'
            }}
          />
        </Box>
        
        <Typography 
          variant="h3" 
          fontFamily="Antonio Bold"
          sx={{ color: statusColor, mb: 1 }}
        >
          {value.toFixed(value < 1 ? 3 : 0)}{unit}
        </Typography>
        
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {description}
        </Typography>
        
        {trend && (
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            {trend > 0 ? (
              <TrendingUpIcon color="error" />
            ) : (
              <TrendingDownIcon color="success" />
            )}
            <Typography 
              variant="body2" 
              color={trend > 0 ? 'error' : 'success'}
              sx={{ ml: 0.5 }}
            >
              {Math.abs(trend).toFixed(1)}% vs. vorher
            </Typography>
          </Box>
        )}
      </CardContent>
    </Card>
  );
};
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
- [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_TECH_CONCEPT.md)

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
- [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multitenant/FC-038_TECH_CONCEPT.md)
- [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)
- **→ FC-040 Performance Monitoring** ← **SIE SIND HIER** ⭐ **SESSION 15 ABGESCHLOSSEN!**

### 📋 ABHÄNGIGKEITEN
- **Integriert:** FC-038 Multi-Tenant Architecture (Tenant-spezifische Metriken)
- **Integriert:** FC-039 API Gateway (Gateway Performance Monitoring)
- **Basis für:** Proaktive System-Optimierung und SLA-Management

### 🔒 MONITORING COVERAGE
- **Backend Performance:** JVM, Database, Application-Level Metrics
- **Frontend Performance:** Core Web Vitals, Real User Monitoring (RUM)
- **Multi-Tenant Metrics:** Tenant-spezifische SLAs und Resource Usage
- **Intelligent Alerting:** Severity-basierte Eskalation mit Auto-Resolution

### 🚀 DEPLOYMENT STRATEGY
```yaml
# Phase 1 (Tag 1-2): Backend Monitoring
- Performance Metrics Collection
- JVM & Database Monitoring
- Basic Alerting Setup

# Phase 2 (Tag 3-4): Frontend Monitoring
- Real User Monitoring (RUM)
- Core Web Vitals Tracking
- Error Tracking & Reporting

# Phase 3 (Tag 5-6): Advanced Features
- Intelligent Alerting System
- Auto-Scaling Integration
- Performance Optimization Recommendations
```

### 🎯 SUCCESS CRITERIA
- ✅ 99.9% System Availability mit < 5 Sekunden MTTR
- ✅ Core Web Vitals Tracking für alle kritischen User Flows
- ✅ Tenant-spezifische Performance SLAs implementiert
- ✅ Real User Monitoring (RUM) funktionsfähig
- ✅ Intelligent Alerting mit Severity-basierter Eskalation
- ✅ Performance Dashboard mit Real-time Metriken
- ✅ 90% Performance Issues werden proaktiv erkannt

---

**⏱️ GESCHÄTZTE IMPLEMENTIERUNGSZEIT:** 6 Tage  
**🎯 BUSINESS IMPACT:** Sehr Hoch (Proaktive Stabilität)  
**🔧 TECHNISCHE KOMPLEXITÄT:** Hoch (Multi-Layer Monitoring)  
**📊 ROI:** Break-even nach 2 Monaten durch reduzierte Downtime und optimierte Infrastruktur