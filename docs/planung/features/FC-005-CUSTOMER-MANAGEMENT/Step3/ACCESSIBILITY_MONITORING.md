# ♿ Accessibility & Monitoring - Barrierefreiheit & Überwachung

**Phase:** 1 - Core Requirements  
**Priorität:** 🔴 KRITISCH - Compliance & Qualität  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PERFORMANCE_OPTIMIZATION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- WCAG 2.1 Compliance
- User Experience für alle
- System-Überwachung

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Accessibility & Monitoring implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Accessibility Components
mkdir -p frontend/src/features/customers/components/accessibility
touch frontend/src/features/customers/components/accessibility/ScreenReaderAnnouncer.tsx
touch frontend/src/features/customers/components/accessibility/KeyboardNavigator.tsx
touch frontend/src/features/customers/components/accessibility/FocusManager.tsx
touch frontend/src/features/customers/components/accessibility/A11yProvider.tsx

# 2. Monitoring Services
mkdir -p backend/src/main/java/de/freshplan/monitoring
touch backend/src/main/java/de/freshplan/monitoring/service/MetricsService.java
touch backend/src/main/java/de/freshplan/monitoring/service/HealthCheckService.java
touch backend/src/main/java/de/freshplan/monitoring/service/ErrorTrackingService.java
touch backend/src/main/java/de/freshplan/monitoring/resource/MonitoringResource.java

# 3. A11y Testing
mkdir -p frontend/src/__tests__/accessibility
touch frontend/src/__tests__/accessibility/wcag.test.tsx
touch frontend/src/__tests__/accessibility/keyboard.test.tsx
touch frontend/src/__tests__/accessibility/screenreader.test.tsx

# 4. Monitoring Dashboard
mkdir -p frontend/src/features/monitoring/components
touch frontend/src/features/monitoring/components/SystemHealthDashboard.tsx
touch frontend/src/features/monitoring/components/MetricsDisplay.tsx

# 5. Tests
npm install --save-dev @testing-library/jest-dom jest-axe
npm install --save-dev @pa11y/pa11y
```

## 🎯 Das Problem: Barrieren & Blind Spots

**Accessibility-Probleme:**
- 🦯 Screen Reader nicht unterstützt
- ⌨️ Keine Keyboard-Navigation
- 🎨 Kontraste zu schwach
- 🔍 Fokus nicht sichtbar
- 📢 Keine Status-Announcements

**Monitoring-Lücken:**
- 📊 Keine Metriken sichtbar
- 🚨 Fehler unbemerkt
- 📉 Performance-Probleme zu spät erkannt
- 🔍 Keine User-Journey Tracking

## 💡 DIE LÖSUNG: Vollständige A11y & Monitoring

### 1. Accessibility Provider

**Datei:** `frontend/src/features/customers/components/accessibility/A11yProvider.tsx`

```typescript
// CLAUDE: Accessibility Provider mit ARIA Live Regions
// Pfad: frontend/src/features/customers/components/accessibility/A11yProvider.tsx

import React, { createContext, useContext, useState, useCallback, useRef, useEffect } from 'react';
import { Box, Portal } from '@mui/material';

interface A11yContextValue {
  announce: (message: string, priority?: 'polite' | 'assertive') => void;
  setFocusTrap: (enabled: boolean) => void;
  registerLandmark: (id: string, label: string) => void;
  navigateToLandmark: (id: string) => void;
  isReducedMotion: boolean;
  isHighContrast: boolean;
  isScreenReaderActive: boolean;
}

const A11yContext = createContext<A11yContextValue | null>(null);

export const useA11y = () => {
  const context = useContext(A11yContext);
  if (!context) {
    throw new Error('useA11y must be used within A11yProvider');
  }
  return context;
};

export const A11yProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [announcements, setAnnouncements] = useState<{
    polite: string;
    assertive: string;
  }>({ polite: '', assertive: '' });
  
  const [landmarks, setLandmarks] = useState<Map<string, string>>(new Map());
  const [focusTrapEnabled, setFocusTrapEnabled] = useState(false);
  const [isReducedMotion, setIsReducedMotion] = useState(false);
  const [isHighContrast, setIsHighContrast] = useState(false);
  const [isScreenReaderActive, setIsScreenReaderActive] = useState(false);
  
  const announcementTimeouts = useRef<{
    polite?: NodeJS.Timeout;
    assertive?: NodeJS.Timeout;
  }>({});
  
  // Detect user preferences
  useEffect(() => {
    // Reduced motion
    const motionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    setIsReducedMotion(motionQuery.matches);
    
    const handleMotionChange = (e: MediaQueryListEvent) => {
      setIsReducedMotion(e.matches);
    };
    motionQuery.addEventListener('change', handleMotionChange);
    
    // High contrast
    const contrastQuery = window.matchMedia('(prefers-contrast: high)');
    setIsHighContrast(contrastQuery.matches);
    
    const handleContrastChange = (e: MediaQueryListEvent) => {
      setIsHighContrast(e.matches);
    };
    contrastQuery.addEventListener('change', handleContrastChange);
    
    // Screen reader detection (heuristic)
    detectScreenReader();
    
    return () => {
      motionQuery.removeEventListener('change', handleMotionChange);
      contrastQuery.removeEventListener('change', handleContrastChange);
    };
  }, []);
  
  const detectScreenReader = () => {
    // Various heuristics to detect screen readers
    const indicators = [
      // NVDA
      window.navigator.userAgent.includes('NVDA'),
      // JAWS
      window.navigator.userAgent.includes('JAWS'),
      // VoiceOver
      window.navigator.userAgent.includes('VoiceOver'),
      // Check for aria-live regions being accessed
      document.activeElement?.getAttribute('aria-live') !== null,
      // Check for specific keyboard patterns
      checkKeyboardPatterns()
    ];
    
    setIsScreenReaderActive(indicators.some(Boolean));
  };
  
  const checkKeyboardPatterns = (): boolean => {
    // Track rapid tab navigation (common with screen readers)
    let tabCount = 0;
    let lastTabTime = 0;
    
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Tab') {
        const now = Date.now();
        if (now - lastTabTime < 100) {
          tabCount++;
        } else {
          tabCount = 0;
        }
        lastTabTime = now;
        
        if (tabCount > 5) {
          return true;
        }
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    setTimeout(() => window.removeEventListener('keydown', handleKeyDown), 5000);
    
    return false;
  };
  
  const announce = useCallback((message: string, priority: 'polite' | 'assertive' = 'polite') => {
    // Clear previous timeout
    if (announcementTimeouts.current[priority]) {
      clearTimeout(announcementTimeouts.current[priority]);
    }
    
    // Set new announcement
    setAnnouncements(prev => ({
      ...prev,
      [priority]: message
    }));
    
    // Clear after announcement
    announcementTimeouts.current[priority] = setTimeout(() => {
      setAnnouncements(prev => ({
        ...prev,
        [priority]: ''
      }));
    }, 1000);
  }, []);
  
  const setFocusTrap = useCallback((enabled: boolean) => {
    setFocusTrapEnabled(enabled);
  }, []);
  
  const registerLandmark = useCallback((id: string, label: string) => {
    setLandmarks(prev => new Map(prev).set(id, label));
  }, []);
  
  const navigateToLandmark = useCallback((id: string) => {
    const element = document.getElementById(id);
    if (element) {
      element.focus();
      element.scrollIntoView({ behavior: 'smooth', block: 'center' });
      announce(`Navigated to ${landmarks.get(id) || id}`);
    }
  }, [landmarks, announce]);
  
  return (
    <A11yContext.Provider
      value={{
        announce,
        setFocusTrap,
        registerLandmark,
        navigateToLandmark,
        isReducedMotion,
        isHighContrast,
        isScreenReaderActive
      }}
    >
      {children}
      
      {/* ARIA Live Regions */}
      <Portal>
        <Box
          aria-live="polite"
          aria-atomic="true"
          sx={{
            position: 'absolute',
            left: '-10000px',
            width: '1px',
            height: '1px',
            overflow: 'hidden'
          }}
        >
          {announcements.polite}
        </Box>
        
        <Box
          aria-live="assertive"
          aria-atomic="true"
          sx={{
            position: 'absolute',
            left: '-10000px',
            width: '1px',
            height: '1px',
            overflow: 'hidden'
          }}
        >
          {announcements.assertive}
        </Box>
        
        {/* Skip Links */}
        <Box
          component="nav"
          aria-label="Skip links"
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            zIndex: 9999,
            '& a': {
              position: 'absolute',
              left: '-10000px',
              top: 'auto',
              width: '1px',
              height: '1px',
              overflow: 'hidden',
              '&:focus': {
                position: 'static',
                width: 'auto',
                height: 'auto',
                padding: 2,
                background: 'primary.main',
                color: 'primary.contrastText'
              }
            }
          }}
        >
          <a href="#main-content">Skip to main content</a>
          <a href="#main-navigation">Skip to navigation</a>
          <a href="#search">Skip to search</a>
        </Box>
      </Portal>
    </A11yContext.Provider>
  );
};
```

### 2. Keyboard Navigation Manager

**Datei:** `frontend/src/features/customers/components/accessibility/KeyboardNavigator.tsx`

```typescript
// CLAUDE: Advanced Keyboard Navigation mit Roving TabIndex
// Pfad: frontend/src/features/customers/components/accessibility/KeyboardNavigator.tsx

import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useA11y } from './A11yProvider';

interface KeyboardNavigatorProps {
  children: React.ReactNode;
  onEscape?: () => void;
  onEnter?: () => void;
  trapFocus?: boolean;
  ariaLabel?: string;
}

export const KeyboardNavigator: React.FC<KeyboardNavigatorProps> = ({
  children,
  onEscape,
  onEnter,
  trapFocus = false,
  ariaLabel
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [focusedIndex, setFocusedIndex] = useState(0);
  const { announce, setFocusTrap } = useA11y();
  
  // Get all focusable elements
  const getFocusableElements = useCallback((): HTMLElement[] => {
    if (!containerRef.current) return [];
    
    const selector = [
      'button:not([disabled])',
      'a[href]',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])'
    ].join(',');
    
    return Array.from(containerRef.current.querySelectorAll(selector));
  }, []);
  
  // Handle keyboard navigation
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const focusableElements = getFocusableElements();
      if (focusableElements.length === 0) return;
      
      switch (e.key) {
        case 'Tab':
          if (trapFocus) {
            e.preventDefault();
            const nextIndex = e.shiftKey
              ? (focusedIndex - 1 + focusableElements.length) % focusableElements.length
              : (focusedIndex + 1) % focusableElements.length;
            
            setFocusedIndex(nextIndex);
            focusableElements[nextIndex].focus();
          }
          break;
          
        case 'ArrowDown':
        case 'ArrowRight':
          e.preventDefault();
          const nextIndex = (focusedIndex + 1) % focusableElements.length;
          setFocusedIndex(nextIndex);
          focusableElements[nextIndex].focus();
          break;
          
        case 'ArrowUp':
        case 'ArrowLeft':
          e.preventDefault();
          const prevIndex = (focusedIndex - 1 + focusableElements.length) % focusableElements.length;
          setFocusedIndex(prevIndex);
          focusableElements[prevIndex].focus();
          break;
          
        case 'Home':
          e.preventDefault();
          setFocusedIndex(0);
          focusableElements[0].focus();
          announce('Moved to first item');
          break;
          
        case 'End':
          e.preventDefault();
          const lastIndex = focusableElements.length - 1;
          setFocusedIndex(lastIndex);
          focusableElements[lastIndex].focus();
          announce('Moved to last item');
          break;
          
        case 'Escape':
          if (onEscape) {
            e.preventDefault();
            onEscape();
            announce('Closed');
          }
          break;
          
        case 'Enter':
        case ' ':
          if (onEnter && e.target === containerRef.current) {
            e.preventDefault();
            onEnter();
          }
          break;
      }
    };
    
    const container = containerRef.current;
    if (container) {
      container.addEventListener('keydown', handleKeyDown);
    }
    
    return () => {
      if (container) {
        container.removeEventListener('keydown', handleKeyDown);
      }
    };
  }, [focusedIndex, trapFocus, onEscape, onEnter, announce, getFocusableElements]);
  
  // Set up focus trap
  useEffect(() => {
    if (trapFocus) {
      setFocusTrap(true);
      
      // Focus first element
      const focusableElements = getFocusableElements();
      if (focusableElements.length > 0) {
        focusableElements[0].focus();
      }
      
      return () => {
        setFocusTrap(false);
      };
    }
  }, [trapFocus, setFocusTrap, getFocusableElements]);
  
  // Roving tabindex pattern
  useEffect(() => {
    const focusableElements = getFocusableElements();
    
    focusableElements.forEach((element, index) => {
      if (index === focusedIndex) {
        element.setAttribute('tabindex', '0');
      } else {
        element.setAttribute('tabindex', '-1');
      }
    });
  }, [focusedIndex, getFocusableElements]);
  
  return (
    <div
      ref={containerRef}
      role="region"
      aria-label={ariaLabel}
      tabIndex={-1}
    >
      {children}
    </div>
  );
};

// Custom Hook for keyboard shortcuts
export const useKeyboardShortcuts = (shortcuts: Record<string, () => void>) => {
  const { announce } = useA11y();
  
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const key = [
        e.ctrlKey && 'ctrl',
        e.altKey && 'alt',
        e.shiftKey && 'shift',
        e.metaKey && 'meta',
        e.key.toLowerCase()
      ].filter(Boolean).join('+');
      
      if (shortcuts[key]) {
        e.preventDefault();
        shortcuts[key]();
        
        // Announce action for screen readers
        const actionName = key.replace(/[+-]/g, ' ');
        announce(`Executed ${actionName}`);
      }
    };
    
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [shortcuts, announce]);
};
```

### 3. System Monitoring Service

**Datei:** `backend/src/main/java/de/freshplan/monitoring/service/MetricsService.java`

```java
// CLAUDE: Comprehensive System Monitoring mit Alerts
// Pfad: backend/src/main/java/de/freshplan/monitoring/service/MetricsService.java

package de.freshplan.monitoring.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import io.micrometer.core.instrument.*;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class MetricsService {
    
    @Inject
    MeterRegistry registry;
    
    @Inject
    AlertingService alertingService;
    
    @Inject
    HealthCheckService healthCheckService;
    
    private final Map<String, MetricSnapshot> snapshots = new ConcurrentHashMap<>();
    
    // Business Metrics
    private Counter contactsCreated;
    private Counter contactsUpdated;
    private Counter contactsDeleted;
    private Gauge activeUsers;
    private Timer apiResponseTime;
    private DistributionSummary contactsPerCustomer;
    
    @PostConstruct
    void init() {
        // Initialize counters
        contactsCreated = Counter.builder("contacts.created")
            .description("Number of contacts created")
            .tag("type", "business")
            .register(registry);
            
        contactsUpdated = Counter.builder("contacts.updated")
            .description("Number of contacts updated")
            .tag("type", "business")
            .register(registry);
            
        contactsDeleted = Counter.builder("contacts.deleted")
            .description("Number of contacts deleted")
            .tag("type", "business")
            .register(registry);
            
        // Active users gauge
        activeUsers = Gauge.builder("users.active", this, MetricsService::countActiveUsers)
            .description("Number of active users")
            .tag("type", "business")
            .register(registry);
            
        // API response time
        apiResponseTime = Timer.builder("api.response.time")
            .description("API response time")
            .tag("type", "performance")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);
            
        // Distribution summary
        contactsPerCustomer = DistributionSummary.builder("contacts.per.customer")
            .description("Distribution of contacts per customer")
            .tag("type", "business")
            .publishPercentiles(0.5, 0.75, 0.95)
            .register(registry);
    }
    
    /**
     * Track contact operation
     */
    public void trackContactOperation(OperationType type, UUID contactId, Duration duration) {
        switch (type) {
            case CREATE:
                contactsCreated.increment();
                break;
            case UPDATE:
                contactsUpdated.increment();
                break;
            case DELETE:
                contactsDeleted.increment();
                break;
        }
        
        // Track response time
        apiResponseTime.record(duration);
        
        // Check for slow operations
        if (duration.toMillis() > 1000) {
            alertingService.sendAlert(
                AlertLevel.WARNING,
                "Slow Operation",
                String.format("Operation %s took %dms for contact %s",
                    type, duration.toMillis(), contactId)
            );
        }
    }
    
    /**
     * Track feature usage
     */
    public void trackFeatureUsage(String feature, UUID userId) {
        Counter featureCounter = Counter.builder("feature.usage")
            .description("Feature usage counter")
            .tag("feature", feature)
            .tag("type", "usage")
            .register(registry);
            
        featureCounter.increment();
        
        // Track user activity
        updateUserActivity(userId);
    }
    
    /**
     * System health metrics
     */
    public SystemHealthMetrics getSystemHealth() {
        SystemHealthMetrics health = new SystemHealthMetrics();
        
        // CPU Usage
        health.setCpuUsage(getCpuUsage());
        
        // Memory Usage
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        health.setMemoryUsage(new MemoryMetrics(
            usedMemory,
            totalMemory,
            maxMemory,
            (double) usedMemory / maxMemory * 100
        ));
        
        // Database health
        health.setDatabaseHealth(checkDatabaseHealth());
        
        // API health
        health.setApiHealth(calculateApiHealth());
        
        // Error rate
        health.setErrorRate(calculateErrorRate());
        
        // Active sessions
        health.setActiveSessions(countActiveSessions());
        
        return health;
    }
    
    /**
     * Performance metrics dashboard
     */
    public PerformanceDashboard getPerformanceMetrics() {
        PerformanceDashboard dashboard = new PerformanceDashboard();
        
        // Response times
        dashboard.setApiResponseTimes(Map.of(
            "p50", apiResponseTime.percentile(0.5),
            "p95", apiResponseTime.percentile(0.95),
            "p99", apiResponseTime.percentile(0.99),
            "mean", apiResponseTime.mean()
        ));
        
        // Throughput
        dashboard.setRequestsPerSecond(calculateThroughput());
        
        // Cache performance
        dashboard.setCacheHitRate(calculateCacheHitRate());
        
        // Database performance
        dashboard.setDatabaseMetrics(getDatabasePerformanceMetrics());
        
        // Queue metrics
        dashboard.setQueueMetrics(getQueueMetrics());
        
        return dashboard;
    }
    
    /**
     * Business metrics dashboard
     */
    public BusinessDashboard getBusinessMetrics() {
        BusinessDashboard dashboard = new BusinessDashboard();
        
        // Contact metrics
        dashboard.setTotalContacts(countTotalContacts());
        dashboard.setContactsCreatedToday(contactsCreated.count());
        dashboard.setContactsPerCustomer(contactsPerCustomer.mean());
        
        // User metrics
        dashboard.setActiveUsers(countActiveUsers());
        dashboard.setNewUsersToday(countNewUsersToday());
        
        // Feature adoption
        dashboard.setFeatureAdoption(calculateFeatureAdoption());
        
        // Data quality
        dashboard.setDataQualityScore(calculateDataQualityScore());
        
        return dashboard;
    }
    
    /**
     * Alert on threshold violations
     */
    @Scheduled(every = "1m")
    void checkThresholds() {
        // Check CPU usage
        double cpuUsage = getCpuUsage();
        if (cpuUsage > 80) {
            alertingService.sendAlert(
                AlertLevel.HIGH,
                "High CPU Usage",
                String.format("CPU usage at %.2f%%", cpuUsage)
            );
        }
        
        // Check memory usage
        double memoryUsage = getMemoryUsagePercentage();
        if (memoryUsage > 85) {
            alertingService.sendAlert(
                AlertLevel.HIGH,
                "High Memory Usage",
                String.format("Memory usage at %.2f%%", memoryUsage)
            );
        }
        
        // Check error rate
        double errorRate = calculateErrorRate();
        if (errorRate > 5) {
            alertingService.sendAlert(
                AlertLevel.CRITICAL,
                "High Error Rate",
                String.format("Error rate at %.2f%%", errorRate)
            );
        }
        
        // Check response time
        double p99ResponseTime = apiResponseTime.percentile(0.99);
        if (p99ResponseTime > 2000) {
            alertingService.sendAlert(
                AlertLevel.WARNING,
                "Slow Response Times",
                String.format("P99 response time at %.0fms", p99ResponseTime)
            );
        }
    }
    
    /**
     * Export metrics for external monitoring
     */
    public MetricsExport exportMetrics(ExportFormat format) {
        MetricsExport export = new MetricsExport();
        export.setTimestamp(LocalDateTime.now());
        export.setFormat(format);
        
        // Collect all metrics
        Map<String, Object> metrics = new HashMap<>();
        
        registry.getMeters().forEach(meter -> {
            String name = meter.getId().getName();
            
            if (meter instanceof Counter) {
                metrics.put(name, ((Counter) meter).count());
            } else if (meter instanceof Gauge) {
                metrics.put(name, ((Gauge) meter).value());
            } else if (meter instanceof Timer) {
                Timer timer = (Timer) meter;
                metrics.put(name, Map.of(
                    "count", timer.count(),
                    "mean", timer.mean(TimeUnit.MILLISECONDS),
                    "max", timer.max(TimeUnit.MILLISECONDS)
                ));
            }
        });
        
        export.setMetrics(metrics);
        
        // Format based on type
        switch (format) {
            case PROMETHEUS:
                export.setFormatted(formatPrometheus(metrics));
                break;
            case JSON:
                export.setFormatted(formatJson(metrics));
                break;
            case CSV:
                export.setFormatted(formatCsv(metrics));
                break;
        }
        
        return export;
    }
    
    private double countActiveUsers() {
        // Count users active in last 15 minutes
        return UserSession.count("lastActivity > ?1", 
            LocalDateTime.now().minusMinutes(15));
    }
    
    private double getCpuUsage() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
            .getProcessCpuLoad() * 100;
    }
    
    private double getMemoryUsagePercentage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return (double) usedMemory / maxMemory * 100;
    }
}

// Alert Levels
public enum AlertLevel {
    INFO, WARNING, HIGH, CRITICAL
}

// Operation Types
public enum OperationType {
    CREATE, UPDATE, DELETE, READ
}

// Export Formats
public enum ExportFormat {
    PROMETHEUS, JSON, CSV, GRAPHITE
}
```

### 4. System Health Dashboard

**Datei:** `frontend/src/features/monitoring/components/SystemHealthDashboard.tsx`

```typescript
// CLAUDE: Real-time System Health Dashboard
// Pfad: frontend/src/features/monitoring/components/SystemHealthDashboard.tsx

import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  LinearProgress,
  Chip,
  Alert,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  IconButton,
  Tooltip,
  CircularProgress,
  useTheme
} from '@mui/material';
import {
  CheckCircle as HealthyIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  Speed as PerformanceIcon,
  Memory as MemoryIcon,
  Storage as StorageIcon,
  Api as ApiIcon,
  Refresh as RefreshIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon
} from '@mui/icons-material';
import { Line, Bar } from 'react-chartjs-2';
import { useA11y } from '../../customers/components/accessibility/A11yProvider';

interface SystemHealthData {
  overall: 'healthy' | 'warning' | 'critical';
  cpu: number;
  memory: {
    used: number;
    total: number;
    percentage: number;
  };
  api: {
    responseTime: number;
    errorRate: number;
    throughput: number;
  };
  database: {
    connections: number;
    queryTime: number;
    status: 'healthy' | 'degraded' | 'down';
  };
  alerts: Alert[];
}

interface Alert {
  id: string;
  level: 'info' | 'warning' | 'error' | 'critical';
  message: string;
  timestamp: Date;
}

export const SystemHealthDashboard: React.FC = () => {
  const [healthData, setHealthData] = useState<SystemHealthData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [refreshInterval, setRefreshInterval] = useState(30000); // 30 seconds
  const { announce } = useA11y();
  const theme = useTheme();
  
  useEffect(() => {
    loadHealthData();
    
    if (autoRefresh) {
      const interval = setInterval(loadHealthData, refreshInterval);
      return () => clearInterval(interval);
    }
  }, [autoRefresh, refreshInterval]);
  
  const loadHealthData = async () => {
    try {
      const response = await fetch('/api/monitoring/health');
      const data = await response.json();
      setHealthData(data);
      
      // Announce critical alerts to screen readers
      if (data.overall === 'critical') {
        announce('Critical system alert detected', 'assertive');
      }
    } catch (error) {
      console.error('Failed to load health data:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const getHealthColor = (status: string) => {
    switch (status) {
      case 'healthy': return theme.palette.success.main;
      case 'warning': return theme.palette.warning.main;
      case 'critical': return theme.palette.error.main;
      default: return theme.palette.grey[500];
    }
  };
  
  const getHealthIcon = (status: string) => {
    switch (status) {
      case 'healthy': return <HealthyIcon color="success" />;
      case 'warning': return <WarningIcon color="warning" />;
      case 'critical': return <ErrorIcon color="error" />;
      default: return null;
    }
  };
  
  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <CircularProgress />
      </Box>
    );
  }
  
  if (!healthData) {
    return (
      <Alert severity="error">
        Failed to load system health data
      </Alert>
    );
  }
  
  return (
    <Box>
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">
          System Health Monitor
        </Typography>
        
        <Box display="flex" gap={2} alignItems="center">
          <Chip
            icon={getHealthIcon(healthData.overall)}
            label={healthData.overall.toUpperCase()}
            color={
              healthData.overall === 'healthy' ? 'success' :
              healthData.overall === 'warning' ? 'warning' : 'error'
            }
          />
          
          <Tooltip title="Refresh">
            <IconButton onClick={loadHealthData}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
      
      {/* Metrics Grid */}
      <Grid container spacing={3}>
        {/* CPU Usage */}
        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">CPU Usage</Typography>
                <PerformanceIcon color="primary" />
              </Box>
              
              <Typography variant="h3" gutterBottom>
                {healthData.cpu.toFixed(1)}%
              </Typography>
              
              <LinearProgress
                variant="determinate"
                value={healthData.cpu}
                color={
                  healthData.cpu > 80 ? 'error' :
                  healthData.cpu > 60 ? 'warning' : 'success'
                }
                sx={{ height: 8, borderRadius: 4 }}
              />
              
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                {healthData.cpu > 80 ? 'High load' :
                 healthData.cpu > 60 ? 'Moderate load' : 'Normal'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Memory Usage */}
        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">Memory</Typography>
                <MemoryIcon color="primary" />
              </Box>
              
              <Typography variant="h3" gutterBottom>
                {healthData.memory.percentage.toFixed(1)}%
              </Typography>
              
              <LinearProgress
                variant="determinate"
                value={healthData.memory.percentage}
                color={
                  healthData.memory.percentage > 85 ? 'error' :
                  healthData.memory.percentage > 70 ? 'warning' : 'success'
                }
                sx={{ height: 8, borderRadius: 4 }}
              />
              
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                {(healthData.memory.used / 1024 / 1024 / 1024).toFixed(1)} GB / 
                {(healthData.memory.total / 1024 / 1024 / 1024).toFixed(1)} GB
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        {/* API Performance */}
        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">API Response</Typography>
                <ApiIcon color="primary" />
              </Box>
              
              <Typography variant="h3" gutterBottom>
                {healthData.api.responseTime.toFixed(0)}ms
              </Typography>
              
              <Box display="flex" justifyContent="space-between" mt={2}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Error Rate
                  </Typography>
                  <Typography variant="body2" color={
                    healthData.api.errorRate > 5 ? 'error' : 'textPrimary'
                  }>
                    {healthData.api.errorRate.toFixed(2)}%
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Throughput
                  </Typography>
                  <Typography variant="body2">
                    {healthData.api.throughput} req/s
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Database Status */}
        <Grid item xs={12} md={6} lg={3}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">Database</Typography>
                <StorageIcon color="primary" />
              </Box>
              
              <Chip
                label={healthData.database.status.toUpperCase()}
                color={
                  healthData.database.status === 'healthy' ? 'success' :
                  healthData.database.status === 'degraded' ? 'warning' : 'error'
                }
                sx={{ mb: 2 }}
              />
              
              <Box display="flex" justifyContent="space-between">
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Connections
                  </Typography>
                  <Typography variant="body2">
                    {healthData.database.connections}
                  </Typography>
                </Box>
                
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Query Time
                  </Typography>
                  <Typography variant="body2">
                    {healthData.database.queryTime}ms
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        {/* Recent Alerts */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Alerts
              </Typography>
              
              {healthData.alerts.length === 0 ? (
                <Alert severity="success">
                  No active alerts - System running smoothly
                </Alert>
              ) : (
                <List>
                  {healthData.alerts.map(alert => (
                    <ListItem key={alert.id}>
                      <ListItemIcon>
                        {alert.level === 'critical' ? <ErrorIcon color="error" /> :
                         alert.level === 'error' ? <ErrorIcon color="error" /> :
                         alert.level === 'warning' ? <WarningIcon color="warning" /> :
                         <InfoIcon color="info" />}
                      </ListItemIcon>
                      <ListItemText
                        primary={alert.message}
                        secondary={new Date(alert.timestamp).toLocaleString()}
                      />
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Accessibility Foundation (60 Min)
- [ ] A11yProvider implementieren
- [ ] ARIA Live Regions
- [ ] Skip Links
- [ ] Screen Reader Detection

### Phase 2: Keyboard Navigation (45 Min)
- [ ] KeyboardNavigator Component
- [ ] Roving TabIndex
- [ ] Focus Management
- [ ] Keyboard Shortcuts

### Phase 3: WCAG Compliance (45 Min)
- [ ] Color Contrast Check
- [ ] Focus Indicators
- [ ] Error Messages
- [ ] Form Labels

### Phase 4: Monitoring Backend (45 Min)
- [ ] MetricsService
- [ ] HealthCheckService
- [ ] AlertingService
- [ ] Performance Tracking

### Phase 5: Monitoring Frontend (45 Min)
- [ ] System Health Dashboard
- [ ] Real-time Updates
- [ ] Alert Display
- [ ] Performance Graphs

## 🔗 INTEGRATION POINTS

1. **All Components** - A11y Provider wrappen
2. **Forms** - Keyboard Navigation einbauen
3. **API Calls** - Metrics tracken
4. **Error Handling** - Alerts senden

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Fehlende Alt-Texte**
   → Lösung: Automatische Checks einbauen

2. **Keine Keyboard-Navigation**
   → Lösung: Von Anfang an mitdenken

3. **Monitoring zu spät**
   → Lösung: Von Tag 1 implementieren

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 240 Minuten  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Accessibility & Monitoring = Qualität für alle! ♿✨**