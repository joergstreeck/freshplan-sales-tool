# 📊 Audit Admin Dashboard - Enterprise Compliance & Monitoring Center

**Phase:** 1 - Core Requirements  
**Priorität:** 🔴 KRITISCH - Rechtliche Anforderung & Admin-Tool  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AUDIT_TRAIL_SYSTEM.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Admin-Übersicht über alle Audit-Aktivitäten
- DSGVO-Compliance Reports
- Security Monitoring & Alerts
- Forensische Analyse

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Audit Admin Dashboard implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Admin Services
mkdir -p backend/src/main/java/de/freshplan/audit/admin
touch backend/src/main/java/de/freshplan/audit/admin/service/AuditAdminService.java
touch backend/src/main/java/de/freshplan/audit/admin/service/ComplianceReportService.java
touch backend/src/main/java/de/freshplan/audit/admin/service/AuditStatisticsService.java
touch backend/src/main/java/de/freshplan/audit/admin/resource/AuditAdminResource.java

# 2. Frontend Admin Dashboard
mkdir -p frontend/src/features/admin/audit
touch frontend/src/features/admin/audit/AuditAdminDashboard.tsx
touch frontend/src/features/admin/audit/components/AuditStatisticsCards.tsx
touch frontend/src/features/admin/audit/components/AuditActivityHeatmap.tsx
touch frontend/src/features/admin/audit/components/UserAuditProfile.tsx
touch frontend/src/features/admin/audit/components/ComplianceStatusPanel.tsx

# 3. Admin-spezifische Stores
mkdir -p frontend/src/stores/admin
touch frontend/src/stores/admin/auditAdminStore.ts

# 4. Migration für Admin Views (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__create_audit_admin_views.sql
```

## 🎯 Das Problem: Fehlende Admin-Übersicht & Compliance-Monitoring

**Administrative & Compliance Herausforderungen:**
- 🔍 **Keine Übersicht:** Admins sehen nicht, wer was macht
- 📊 **Keine Metriken:** Wie viele kritische Änderungen pro Tag?
- ⚠️ **Keine Alerts:** Verdächtige Aktivitäten werden nicht erkannt
- 📈 **Keine Reports:** DSGVO-Auditor fragt nach Dokumentation
- 🔒 **Keine Kontrolle:** Wer hat Zugriff auf sensible Daten?

## 💡 DIE LÖSUNG: Enterprise Audit Admin Dashboard

### 1. Backend Admin Services

**Datei:** `backend/src/main/java/de/freshplan/audit/admin/service/AuditAdminService.java`

```java
// CLAUDE: Erweiterte Admin Services für Audit Dashboard
// Pfad: backend/src/main/java/de/freshplan/audit/admin/service/AuditAdminService.java

package de.freshplan.audit.admin.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class AuditAdminService {
    
    @Inject
    AuditRepository auditRepository;
    
    @Inject
    SecurityAlertService alertService;
    
    @Inject
    ComplianceReportService complianceService;
    
    /**
     * Dashboard Statistics for Admin Overview
     */
    public AuditDashboardStats getDashboardStats(LocalDateTime from, LocalDateTime to) {
        AuditDashboardStats stats = new AuditDashboardStats();
        
        // Total Events
        stats.totalEvents = auditRepository.count(
            "occurredAt BETWEEN ?1 AND ?2", from, to
        );
        
        // Events by Type
        stats.eventsByType = auditRepository.find(
            "SELECT action, COUNT(*) FROM AuditLog " +
            "WHERE occurredAt BETWEEN ?1 AND ?2 " +
            "GROUP BY action", from, to
        ).stream().collect(Collectors.toMap(
            row -> (AuditAction) row[0],
            row -> (Long) row[1]
        ));
        
        // Critical Events
        stats.criticalEvents = auditRepository.count(
            "action IN ?1 AND occurredAt BETWEEN ?2 AND ?3",
            List.of(AuditAction.DELETE, AuditAction.BULK_DELETE, 
                   AuditAction.PERMISSION_CHANGE, AuditAction.EXPORT),
            from, to
        );
        
        // DSGVO Relevant
        stats.dsgvoRelevantEvents = auditRepository.count(
            "isDsgvoRelevant = true AND occurredAt BETWEEN ?1 AND ?2",
            from, to
        );
        
        // Active Users
        stats.activeUsers = auditRepository.find(
            "SELECT DISTINCT userId FROM AuditLog " +
            "WHERE occurredAt BETWEEN ?1 AND ?2", from, to
        ).size();
        
        // Top Users by Activity
        stats.topUsersByActivity = auditRepository.find(
            "SELECT userId, userName, COUNT(*) as cnt FROM AuditLog " +
            "WHERE occurredAt BETWEEN ?1 AND ?2 " +
            "GROUP BY userId, userName " +
            "ORDER BY cnt DESC " +
            "LIMIT 10", from, to
        ).stream().map(row -> new UserActivity(
            (UUID) row[0],
            (String) row[1],
            (Long) row[2]
        )).collect(Collectors.toList());
        
        // Entities with Most Changes
        stats.hottestEntities = auditRepository.find(
            "SELECT entityType, entityId, COUNT(*) as cnt FROM AuditLog " +
            "WHERE occurredAt BETWEEN ?1 AND ?2 " +
            "GROUP BY entityType, entityId " +
            "ORDER BY cnt DESC " +
            "LIMIT 20", from, to
        ).stream().map(row -> new HotEntity(
            (EntityType) row[0],
            (UUID) row[1],
            (Long) row[2]
        )).collect(Collectors.toList());
        
        // Compliance Score
        stats.complianceScore = calculateComplianceScore(from, to);
        
        // Security Alerts
        stats.openSecurityAlerts = alertService.countOpenAlerts();
        
        // Integrity Status
        stats.integrityValid = verifyAuditIntegrity(from, to);
        
        return stats;
    }
    
    /**
     * Activity Heatmap Data for Visualization
     */
    public ActivityHeatmap getActivityHeatmap(
        LocalDate from, 
        LocalDate to,
        String granularity // HOUR, DAY, WEEK
    ) {
        ActivityHeatmap heatmap = new ActivityHeatmap();
        heatmap.granularity = granularity;
        
        // Get activity data grouped by time period
        String groupBy = switch(granularity) {
            case "HOUR" -> "DATE_TRUNC('hour', occurred_at)";
            case "DAY" -> "DATE_TRUNC('day', occurred_at)";
            case "WEEK" -> "DATE_TRUNC('week', occurred_at)";
            default -> "DATE_TRUNC('day', occurred_at)";
        };
        
        List<Object[]> data = auditRepository.find(
            "SELECT " + groupBy + " as period, " +
            "COUNT(*) as total, " +
            "COUNT(DISTINCT user_id) as unique_users, " +
            "COUNT(CASE WHEN is_dsgvo_relevant THEN 1 END) as dsgvo_events " +
            "FROM audit_logs " +
            "WHERE occurred_at BETWEEN ?1 AND ?2 " +
            "GROUP BY period " +
            "ORDER BY period",
            from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        ).list();
        
        heatmap.dataPoints = data.stream().map(row -> {
            HeatmapDataPoint point = new HeatmapDataPoint();
            point.timestamp = (LocalDateTime) row[0];
            point.totalEvents = (Long) row[1];
            point.uniqueUsers = (Long) row[2];
            point.dsgvoEvents = (Long) row[3];
            point.intensity = calculateIntensity(point.totalEvents);
            return point;
        }).collect(Collectors.toList());
        
        // Peak times analysis
        heatmap.peakHours = analyzePeakTimes(from, to);
        heatmap.quietPeriods = analyzeQuietPeriods(from, to);
        
        return heatmap;
    }
    
    /**
     * User Audit Profile - Deep dive into specific user
     */
    public UserAuditProfile getUserAuditProfile(UUID userId, LocalDateTime from, LocalDateTime to) {
        UserAuditProfile profile = new UserAuditProfile();
        profile.userId = userId;
        
        // Basic user info
        User user = userRepository.findById(userId);
        profile.userName = user.getName();
        profile.userRole = user.getRole();
        profile.department = user.getDepartment();
        
        // Activity Statistics
        profile.totalActions = auditRepository.count(
            "userId = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            userId, from, to
        );
        
        // Actions by type
        profile.actionBreakdown = auditRepository.find(
            "SELECT action, COUNT(*) FROM AuditLog " +
            "WHERE userId = ?1 AND occurredAt BETWEEN ?2 AND ?3 " +
            "GROUP BY action",
            userId, from, to
        ).stream().collect(Collectors.toMap(
            row -> (AuditAction) row[0],
            row -> (Long) row[1]
        ));
        
        // Entities accessed
        profile.entitiesAccessed = auditRepository.find(
            "SELECT DISTINCT entityType, entityId FROM AuditLog " +
            "WHERE userId = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            userId, from, to
        ).size();
        
        // DSGVO relevant actions
        profile.dsgvoActions = auditRepository.count(
            "userId = ?1 AND isDsgvoRelevant = true AND occurredAt BETWEEN ?2 AND ?3",
            userId, from, to
        );
        
        // Activity timeline
        profile.activityTimeline = auditRepository.find(
            "userId = ?1 AND occurredAt BETWEEN ?2 AND ?3 ORDER BY occurredAt DESC",
            userId, from, to
        ).list();
        
        // Risk score
        profile.riskScore = calculateUserRiskScore(userId, from, to);
        
        // Anomalies detected
        profile.anomalies = detectUserAnomalies(userId, from, to);
        
        // Access patterns
        profile.accessPatterns = analyzeAccessPatterns(userId, from, to);
        
        // IP addresses used
        profile.ipAddresses = auditRepository.find(
            "SELECT DISTINCT ipAddress, COUNT(*) FROM AuditLog " +
            "WHERE userId = ?1 AND occurredAt BETWEEN ?2 AND ?3 " +
            "GROUP BY ipAddress",
            userId, from, to
        ).stream().collect(Collectors.toMap(
            row -> (String) row[0],
            row -> (Long) row[1]
        ));
        
        return profile;
    }
    
    /**
     * Suspicious Activity Detection
     */
    public List<SuspiciousActivity> detectSuspiciousActivities(LocalDateTime from, LocalDateTime to) {
        List<SuspiciousActivity> suspicious = new ArrayList<>();
        
        // 1. Bulk operations outside business hours
        List<AuditLog> bulkOpsAtNight = auditRepository.find(
            "action IN ?1 AND EXTRACT(HOUR FROM occurredAt) NOT BETWEEN 7 AND 19 " +
            "AND occurredAt BETWEEN ?2 AND ?3",
            List.of(AuditAction.BULK_UPDATE, AuditAction.BULK_DELETE, AuditAction.EXPORT),
            from, to
        ).list();
        
        for (AuditLog log : bulkOpsAtNight) {
            suspicious.add(new SuspiciousActivity(
                SuspiciousType.UNUSUAL_TIME,
                log,
                "Bulk operation outside business hours",
                Severity.MEDIUM
            ));
        }
        
        // 2. Rapid succession of exports
        Map<UUID, List<AuditLog>> exportsByUser = auditRepository.find(
            "action = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            AuditAction.EXPORT, from, to
        ).stream().collect(Collectors.groupingBy(log -> log.userId));
        
        for (Map.Entry<UUID, List<AuditLog>> entry : exportsByUser.entrySet()) {
            List<AuditLog> exports = entry.getValue();
            if (exports.size() > 5) {
                // Check time between exports
                for (int i = 1; i < exports.size(); i++) {
                    Duration between = Duration.between(
                        exports.get(i-1).occurredAt,
                        exports.get(i).occurredAt
                    );
                    if (between.toMinutes() < 5) {
                        suspicious.add(new SuspiciousActivity(
                            SuspiciousType.RAPID_EXPORTS,
                            exports.get(i),
                            "Multiple exports in rapid succession",
                            Severity.HIGH
                        ));
                    }
                }
            }
        }
        
        // 3. Access to recently deleted items
        List<UUID> deletedEntities = auditRepository.find(
            "action = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            AuditAction.DELETE, from, to
        ).stream().map(log -> log.entityId).collect(Collectors.toList());
        
        List<AuditLog> accessToDeleted = auditRepository.find(
            "action = ?1 AND entityId IN ?2 AND occurredAt > " +
            "(SELECT occurredAt FROM AuditLog WHERE action = ?3 AND entityId = entityId LIMIT 1)",
            AuditAction.READ, deletedEntities, AuditAction.DELETE
        ).list();
        
        for (AuditLog log : accessToDeleted) {
            suspicious.add(new SuspiciousActivity(
                SuspiciousType.ACCESS_DELETED,
                log,
                "Attempted access to deleted entity",
                Severity.LOW
            ));
        }
        
        // 4. Permission escalation patterns
        List<AuditLog> permissionChanges = auditRepository.find(
            "action = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            AuditAction.PERMISSION_CHANGE, from, to
        ).list();
        
        for (AuditLog log : permissionChanges) {
            // Check if user changed their own permissions
            if (log.entityId.equals(log.userId)) {
                suspicious.add(new SuspiciousActivity(
                    SuspiciousType.SELF_PERMISSION_CHANGE,
                    log,
                    "User changed their own permissions",
                    Severity.CRITICAL
                ));
            }
        }
        
        // 5. Unusual data access patterns
        Map<UUID, Set<UUID>> userEntityAccess = new HashMap<>();
        List<AuditLog> allAccess = auditRepository.find(
            "action IN ?1 AND occurredAt BETWEEN ?2 AND ?3",
            List.of(AuditAction.READ, AuditAction.UPDATE),
            from, to
        ).list();
        
        for (AuditLog log : allAccess) {
            userEntityAccess
                .computeIfAbsent(log.userId, k -> new HashSet<>())
                .add(log.entityId);
        }
        
        // Flag users accessing >100 different entities in timeframe
        for (Map.Entry<UUID, Set<UUID>> entry : userEntityAccess.entrySet()) {
            if (entry.getValue().size() > 100) {
                suspicious.add(new SuspiciousActivity(
                    SuspiciousType.EXCESSIVE_ACCESS,
                    null,
                    String.format("User %s accessed %d different entities", 
                        entry.getKey(), entry.getValue().size()),
                    Severity.MEDIUM
                ));
            }
        }
        
        return suspicious;
    }
    
    /**
     * Compliance Score Calculation
     */
    private Double calculateComplianceScore(LocalDateTime from, LocalDateTime to) {
        double score = 100.0;
        
        // Check: All critical operations have audit logs
        long criticalOpsWithoutAudit = countCriticalOpsWithoutAudit(from, to);
        if (criticalOpsWithoutAudit > 0) {
            score -= (criticalOpsWithoutAudit * 5); // -5 points per missing audit
        }
        
        // Check: DSGVO operations have legal basis
        long dsgvoWithoutBasis = auditRepository.count(
            "isDsgvoRelevant = true AND legalBasis IS NULL " +
            "AND occurredAt BETWEEN ?1 AND ?2",
            from, to
        );
        if (dsgvoWithoutBasis > 0) {
            score -= (dsgvoWithoutBasis * 10); // -10 points per missing legal basis
        }
        
        // Check: Retention policies followed
        long overdueForDeletion = auditRepository.count(
            "retentionUntil < ?1", LocalDateTime.now()
        );
        if (overdueForDeletion > 0) {
            score -= (overdueForDeletion * 2); // -2 points per overdue
        }
        
        // Check: Audit integrity
        if (!verifyAuditIntegrity(from, to)) {
            score -= 20; // -20 points for integrity failure
        }
        
        return Math.max(0, score);
    }
    
    /**
     * Generate Compliance Report for DSGVO
     */
    public DSGVOComplianceReport generateDSGVOReport(LocalDate from, LocalDate to) {
        DSGVOComplianceReport report = new DSGVOComplianceReport();
        report.reportPeriod = new DateRange(from, to);
        report.generatedAt = LocalDateTime.now();
        report.generatedBy = securityContext.getUserName();
        
        // Data Processing Activities
        report.processingActivities = auditRepository.find(
            "SELECT entityType, action, legalBasis, COUNT(*) " +
            "FROM AuditLog " +
            "WHERE isDsgvoRelevant = true " +
            "AND occurredAt BETWEEN ?1 AND ?2 " +
            "GROUP BY entityType, action, legalBasis",
            from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        ).stream().map(row -> new ProcessingActivity(
            (EntityType) row[0],
            (AuditAction) row[1],
            (LegalBasis) row[2],
            (Long) row[3]
        )).collect(Collectors.toList());
        
        // Data Subject Requests
        report.subjectRequests = dataSubjectRequestRepository.find(
            "receivedAt BETWEEN ?1 AND ?2",
            from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        ).list();
        
        // Consent Statistics
        report.consentStats = new ConsentStatistics();
        report.consentStats.given = auditRepository.count(
            "action = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            AuditAction.CONSENT_GIVEN, from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        );
        report.consentStats.withdrawn = auditRepository.count(
            "action = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            AuditAction.CONSENT_WITHDRAWN, from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        );
        
        // Data Breaches (if any)
        report.dataBreaches = securityAlertRepository.find(
            "alertType = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            AlertType.DATA_BREACH, from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        ).list();
        
        // Third Country Transfers
        report.thirdCountryTransfers = auditRepository.find(
            "newValues->>'recipient_country' NOT IN ('DE', 'AT', 'CH') " +
            "AND occurredAt BETWEEN ?1 AND ?2",
            from.atStartOfDay(), to.plusDays(1).atStartOfDay()
        ).list();
        
        // Compliance Score
        report.complianceScore = calculateComplianceScore(
            from.atStartOfDay(), 
            to.plusDays(1).atStartOfDay()
        );
        
        // Recommendations
        report.recommendations = generateComplianceRecommendations(report);
        
        // Sign the report
        report.signature = signReport(report);
        
        return report;
    }
}
```

### 2. Frontend Admin Dashboard Component

**Datei:** `frontend/src/features/admin/audit/AuditAdminDashboard.tsx`

```typescript
// CLAUDE: Hauptkomponente für Audit Admin Dashboard
// Pfad: frontend/src/features/admin/audit/AuditAdminDashboard.tsx

import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Tabs,
  Tab,
  Alert,
  IconButton,
  Button,
  Menu,
  MenuItem,
  Chip,
  Tooltip,
  Badge,
  Drawer,
  Divider
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  Security as SecurityIcon,
  Assessment as AssessmentIcon,
  Warning as WarningIcon,
  People as PeopleIcon,
  Timeline as TimelineIcon,
  FilterList as FilterIcon,
  Download as DownloadIcon,
  Refresh as RefreshIcon,
  Settings as SettingsIcon,
  NotificationsActive as AlertIcon
} from '@mui/icons-material';
import { useAuditAdminStore } from '@/stores/admin/auditAdminStore';
import { AuditStatisticsCards } from './components/AuditStatisticsCards';
import { AuditActivityHeatmap } from './components/AuditActivityHeatmap';
import { UserAuditProfile } from './components/UserAuditProfile';
import { ComplianceStatusPanel } from './components/ComplianceStatusPanel';
import { SuspiciousActivityList } from './components/SuspiciousActivityList';
import { AuditStreamMonitor } from './components/AuditStreamMonitor';
import { ComplianceReportGenerator } from './components/ComplianceReportGenerator';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index, ...other }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`audit-tabpanel-${index}`}
      aria-labelledby={`audit-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

export const AuditAdminDashboard: React.FC = () => {
  const [currentTab, setCurrentTab] = useState(0);
  const [dateRange, setDateRange] = useState({
    from: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    to: new Date()
  });
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [refreshInterval, setRefreshInterval] = useState(30000); // 30 seconds
  const [showFilters, setShowFilters] = useState(false);
  const [selectedUser, setSelectedUser] = useState<string | null>(null);
  
  const {
    dashboardStats,
    activityHeatmap,
    suspiciousActivities,
    complianceStatus,
    isLoading,
    error,
    fetchDashboardData,
    fetchSuspiciousActivities,
    generateComplianceReport
  } = useAuditAdminStore();
  
  // Auto-refresh
  useEffect(() => {
    if (autoRefresh) {
      const interval = setInterval(() => {
        fetchDashboardData(dateRange);
        fetchSuspiciousActivities();
      }, refreshInterval);
      
      return () => clearInterval(interval);
    }
  }, [autoRefresh, refreshInterval, dateRange]);
  
  // Initial load
  useEffect(() => {
    fetchDashboardData(dateRange);
    fetchSuspiciousActivities();
  }, [dateRange]);
  
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };
  
  const handleExportReport = async (format: 'pdf' | 'excel' | 'csv') => {
    try {
      const report = await generateComplianceReport({
        dateRange,
        format,
        includeDetails: true
      });
      
      // Download the report
      const blob = new Blob([report.data], { 
        type: format === 'pdf' ? 'application/pdf' : 
              format === 'excel' ? 'application/vnd.ms-excel' : 
              'text/csv'
      });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit-report-${format}-${new Date().toISOString()}.${format}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    } catch (error) {
      console.error('Export failed:', error);
    }
  };
  
  return (
    <Box sx={{ flexGrow: 1, p: 3, bgcolor: '#f5f5f5', minHeight: '100vh' }}>
      {/* Header */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container alignItems="center" spacing={2}>
          <Grid item xs>
            <Typography variant="h4" component="h1" sx={{ display: 'flex', alignItems: 'center' }}>
              <SecurityIcon sx={{ mr: 2, color: '#004F7B' }} />
              Audit Admin Dashboard
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              Compliance Monitoring & Security Analysis
            </Typography>
          </Grid>
          
          <Grid item>
            <Box sx={{ display: 'flex', gap: 1 }}>
              {/* Alert Badge */}
              <Badge badgeContent={suspiciousActivities?.length || 0} color="error">
                <IconButton onClick={() => setCurrentTab(2)}>
                  <AlertIcon />
                </IconButton>
              </Badge>
              
              {/* Auto Refresh Toggle */}
              <Tooltip title={autoRefresh ? "Auto-Refresh aktiv" : "Auto-Refresh inaktiv"}>
                <IconButton 
                  onClick={() => setAutoRefresh(!autoRefresh)}
                  color={autoRefresh ? "primary" : "default"}
                >
                  <RefreshIcon />
                </IconButton>
              </Tooltip>
              
              {/* Filter Drawer Toggle */}
              <IconButton onClick={() => setShowFilters(!showFilters)}>
                <FilterIcon />
              </IconButton>
              
              {/* Export Menu */}
              <Button
                variant="contained"
                startIcon={<DownloadIcon />}
                onClick={(e) => setExportMenuAnchor(e.currentTarget)}
                sx={{ bgcolor: '#94C456' }}
              >
                Export
              </Button>
              
              {/* Settings */}
              <IconButton>
                <SettingsIcon />
              </IconButton>
            </Box>
          </Grid>
        </Grid>
      </Paper>
      
      {/* Compliance Alert Bar */}
      {complianceStatus?.score < 80 && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          <Typography variant="subtitle2">
            ⚠️ Compliance Score unter 80%: {complianceStatus.score.toFixed(1)}% - 
            Bitte prüfen Sie die identifizierten Probleme im Compliance-Tab
          </Typography>
        </Alert>
      )}
      
      {/* Main Content */}
      <Paper sx={{ width: '100%' }}>
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          indicatorColor="primary"
          textColor="primary"
          variant="fullWidth"
        >
          <Tab 
            label="Übersicht" 
            icon={<DashboardIcon />} 
            iconPosition="start"
          />
          <Tab 
            label="Aktivitäts-Analyse" 
            icon={<TimelineIcon />} 
            iconPosition="start"
          />
          <Tab 
            label={
              <Badge badgeContent={suspiciousActivities?.length || 0} color="error">
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <WarningIcon sx={{ mr: 1 }} />
                  Sicherheit
                </Box>
              </Badge>
            }
          />
          <Tab 
            label="Benutzer-Profile" 
            icon={<PeopleIcon />} 
            iconPosition="start"
          />
          <Tab 
            label="Compliance" 
            icon={<AssessmentIcon />} 
            iconPosition="start"
          />
        </Tabs>
        
        {/* Tab 0: Overview */}
        <TabPanel value={currentTab} index={0}>
          <Grid container spacing={3}>
            {/* Statistics Cards */}
            <Grid item xs={12}>
              <AuditStatisticsCards 
                stats={dashboardStats} 
                dateRange={dateRange}
              />
            </Grid>
            
            {/* Activity Heatmap */}
            <Grid item xs={12} lg={8}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Aktivitäts-Heatmap (7 Tage)
                </Typography>
                <AuditActivityHeatmap 
                  data={activityHeatmap}
                  granularity="HOUR"
                />
              </Paper>
            </Grid>
            
            {/* Top Users */}
            <Grid item xs={12} lg={4}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Aktivste Benutzer
                </Typography>
                <TopUsersList 
                  users={dashboardStats?.topUsersByActivity}
                  onUserClick={setSelectedUser}
                />
              </Paper>
            </Grid>
            
            {/* Real-time Activity Stream */}
            <Grid item xs={12}>
              <Paper sx={{ p: 2 }}>
                <Typography variant="h6" gutterBottom>
                  Live Activity Stream
                </Typography>
                <AuditStreamMonitor maxEntries={10} />
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>
        
        {/* Tab 1: Activity Analysis */}
        <TabPanel value={currentTab} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <ActivityAnalysisPanel dateRange={dateRange} />
            </Grid>
          </Grid>
        </TabPanel>
        
        {/* Tab 2: Security */}
        <TabPanel value={currentTab} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h5" gutterBottom>
                Verdächtige Aktivitäten
              </Typography>
              <SuspiciousActivityList 
                activities={suspiciousActivities}
                onInvestigate={(activity) => {
                  // Open investigation modal
                }}
              />
            </Grid>
            
            <Grid item xs={12}>
              <SecurityMetricsPanel />
            </Grid>
          </Grid>
        </TabPanel>
        
        {/* Tab 3: User Profiles */}
        <TabPanel value={currentTab} index={3}>
          {selectedUser ? (
            <UserAuditProfile 
              userId={selectedUser}
              dateRange={dateRange}
              onBack={() => setSelectedUser(null)}
            />
          ) : (
            <UserSearchPanel onSelectUser={setSelectedUser} />
          )}
        </TabPanel>
        
        {/* Tab 4: Compliance */}
        <TabPanel value={currentTab} index={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <ComplianceStatusPanel 
                status={complianceStatus}
                dateRange={dateRange}
              />
            </Grid>
            
            <Grid item xs={12}>
              <ComplianceReportGenerator 
                onGenerate={(config) => generateComplianceReport(config)}
              />
            </Grid>
          </Grid>
        </TabPanel>
      </Paper>
      
      {/* Filter Drawer */}
      <Drawer
        anchor="right"
        open={showFilters}
        onClose={() => setShowFilters(false)}
      >
        <Box sx={{ width: 350, p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Filter & Einstellungen
          </Typography>
          <Divider sx={{ mb: 2 }} />
          
          <AuditFilterPanel
            dateRange={dateRange}
            onDateRangeChange={setDateRange}
            refreshInterval={refreshInterval}
            onRefreshIntervalChange={setRefreshInterval}
          />
        </Box>
      </Drawer>
    </Box>
  );
};

// Sub-components (would be in separate files)

const TopUsersList: React.FC<{
  users: UserActivity[];
  onUserClick: (userId: string) => void;
}> = ({ users, onUserClick }) => {
  return (
    <Box>
      {users?.map((user, index) => (
        <Box
          key={user.userId}
          sx={{
            display: 'flex',
            alignItems: 'center',
            p: 1,
            borderRadius: 1,
            mb: 1,
            cursor: 'pointer',
            '&:hover': { bgcolor: 'action.hover' }
          }}
          onClick={() => onUserClick(user.userId)}
        >
          <Typography variant="h6" sx={{ mr: 1, color: 'text.secondary' }}>
            #{index + 1}
          </Typography>
          <Box sx={{ flex: 1 }}>
            <Typography variant="subtitle2">{user.userName}</Typography>
            <Typography variant="caption" color="text.secondary">
              {user.activityCount} Aktionen
            </Typography>
          </Box>
          <Chip 
            label={user.role} 
            size="small" 
            color={user.role === 'admin' ? 'error' : 'default'}
          />
        </Box>
      ))}
    </Box>
  );
};
```

### 3. Audit Admin Store (Zustand)

**Datei:** `frontend/src/stores/admin/auditAdminStore.ts`

```typescript
// CLAUDE: Zustand Store für Audit Admin Dashboard
// Pfad: frontend/src/stores/admin/auditAdminStore.ts

import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import { auditAdminApi } from '@/services/admin/auditAdminApi';

interface AuditAdminState {
  // Dashboard Data
  dashboardStats: AuditDashboardStats | null;
  activityHeatmap: ActivityHeatmap | null;
  suspiciousActivities: SuspiciousActivity[];
  complianceStatus: ComplianceStatus | null;
  userProfiles: Map<string, UserAuditProfile>;
  
  // UI State
  isLoading: boolean;
  error: string | null;
  selectedTimeRange: DateRange;
  filters: AuditFilters;
  
  // Real-time Stream
  liveStream: AuditEntry[];
  isStreamConnected: boolean;
  
  // Actions
  fetchDashboardData: (dateRange: DateRange) => Promise<void>;
  fetchActivityHeatmap: (dateRange: DateRange, granularity: string) => Promise<void>;
  fetchSuspiciousActivities: () => Promise<void>;
  fetchUserProfile: (userId: string, dateRange: DateRange) => Promise<void>;
  generateComplianceReport: (config: ReportConfig) => Promise<ReportResult>;
  
  // Real-time
  connectToStream: () => void;
  disconnectFromStream: () => void;
  addStreamEntry: (entry: AuditEntry) => void;
  
  // Filtering
  setFilters: (filters: Partial<AuditFilters>) => void;
  clearFilters: () => void;
  
  // Security Actions
  acknowledgeAlert: (alertId: string) => Promise<void>;
  investigateActivity: (activityId: string) => Promise<InvestigationResult>;
  blockUser: (userId: string, reason: string) => Promise<void>;
}

export const useAuditAdminStore = create<AuditAdminState>()(
  devtools(
    persist(
      (set, get) => ({
        // Initial State
        dashboardStats: null,
        activityHeatmap: null,
        suspiciousActivities: [],
        complianceStatus: null,
        userProfiles: new Map(),
        isLoading: false,
        error: null,
        selectedTimeRange: {
          from: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
          to: new Date()
        },
        filters: {},
        liveStream: [],
        isStreamConnected: false,
        
        // Fetch Dashboard Data
        fetchDashboardData: async (dateRange) => {
          set({ isLoading: true, error: null });
          try {
            const [stats, compliance] = await Promise.all([
              auditAdminApi.getDashboardStats(dateRange),
              auditAdminApi.getComplianceStatus(dateRange)
            ]);
            
            set({
              dashboardStats: stats,
              complianceStatus: compliance,
              isLoading: false
            });
          } catch (error) {
            set({
              error: error.message,
              isLoading: false
            });
          }
        },
        
        // Fetch Activity Heatmap
        fetchActivityHeatmap: async (dateRange, granularity) => {
          try {
            const heatmap = await auditAdminApi.getActivityHeatmap(
              dateRange,
              granularity
            );
            set({ activityHeatmap: heatmap });
          } catch (error) {
            console.error('Failed to fetch heatmap:', error);
          }
        },
        
        // Fetch Suspicious Activities
        fetchSuspiciousActivities: async () => {
          try {
            const activities = await auditAdminApi.getSuspiciousActivities();
            set({ suspiciousActivities: activities });
          } catch (error) {
            console.error('Failed to fetch suspicious activities:', error);
          }
        },
        
        // Fetch User Profile
        fetchUserProfile: async (userId, dateRange) => {
          try {
            const profile = await auditAdminApi.getUserAuditProfile(
              userId,
              dateRange
            );
            
            set((state) => ({
              userProfiles: new Map(state.userProfiles).set(userId, profile)
            }));
          } catch (error) {
            console.error('Failed to fetch user profile:', error);
          }
        },
        
        // Generate Compliance Report
        generateComplianceReport: async (config) => {
          set({ isLoading: true });
          try {
            const report = await auditAdminApi.generateComplianceReport(config);
            set({ isLoading: false });
            return report;
          } catch (error) {
            set({ isLoading: false, error: error.message });
            throw error;
          }
        },
        
        // Real-time Stream Connection
        connectToStream: () => {
          const ws = new WebSocket('wss://api.freshplan.de/admin/audit-stream');
          
          ws.onopen = () => {
            set({ isStreamConnected: true });
          };
          
          ws.onmessage = (event) => {
            const entry = JSON.parse(event.data);
            get().addStreamEntry(entry);
            
            // Check if it's suspicious
            if (entry.isSuspicious) {
              set((state) => ({
                suspiciousActivities: [entry, ...state.suspiciousActivities]
              }));
            }
          };
          
          ws.onclose = () => {
            set({ isStreamConnected: false });
            // Reconnect after 5 seconds
            setTimeout(() => get().connectToStream(), 5000);
          };
        },
        
        // Add Stream Entry
        addStreamEntry: (entry) => {
          set((state) => ({
            liveStream: [entry, ...state.liveStream].slice(0, 100) // Keep last 100
          }));
        },
        
        // Security Actions
        acknowledgeAlert: async (alertId) => {
          await auditAdminApi.acknowledgeAlert(alertId);
          set((state) => ({
            suspiciousActivities: state.suspiciousActivities.map(a =>
              a.id === alertId ? { ...a, acknowledged: true } : a
            )
          }));
        },
        
        investigateActivity: async (activityId) => {
          const result = await auditAdminApi.investigateActivity(activityId);
          return result;
        },
        
        blockUser: async (userId, reason) => {
          await auditAdminApi.blockUser(userId, reason);
          // Refresh user profile
          const dateRange = get().selectedTimeRange;
          await get().fetchUserProfile(userId, dateRange);
        },
        
        // Filtering
        setFilters: (filters) => {
          set((state) => ({
            filters: { ...state.filters, ...filters }
          }));
        },
        
        clearFilters: () => {
          set({ filters: {} });
        }
      }),
      {
        name: 'audit-admin-storage',
        partialize: (state) => ({
          selectedTimeRange: state.selectedTimeRange,
          filters: state.filters
        })
      }
    )
  )
);
```

### 4. Database Views für Admin Dashboard

**Datei:** `backend/src/main/resources/db/migration/V[NEXT]__create_audit_admin_views.sql`

```sql
-- CLAUDE: Materialized Views für performante Admin Dashboard Queries
-- WICHTIG: Ersetze [NEXT] mit nächster freier Nummer!

-- View für Dashboard Statistics
CREATE MATERIALIZED VIEW audit_dashboard_stats AS
SELECT 
    DATE_TRUNC('hour', occurred_at) as hour,
    COUNT(*) as total_events,
    COUNT(DISTINCT user_id) as unique_users,
    COUNT(CASE WHEN action IN ('DELETE', 'BULK_DELETE') THEN 1 END) as deletions,
    COUNT(CASE WHEN action IN ('CREATE', 'UPDATE') THEN 1 END) as modifications,
    COUNT(CASE WHEN action = 'EXPORT' THEN 1 END) as exports,
    COUNT(CASE WHEN is_dsgvo_relevant THEN 1 END) as dsgvo_events,
    COUNT(DISTINCT entity_id) as unique_entities,
    COUNT(DISTINCT session_id) as unique_sessions
FROM audit_logs
WHERE occurred_at > CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY DATE_TRUNC('hour', occurred_at);

CREATE INDEX idx_audit_stats_hour ON audit_dashboard_stats(hour DESC);

-- View für User Activity Profiles
CREATE MATERIALIZED VIEW user_activity_profiles AS
SELECT 
    user_id,
    user_name,
    user_role,
    COUNT(*) as total_actions,
    COUNT(DISTINCT DATE(occurred_at)) as active_days,
    COUNT(DISTINCT entity_id) as entities_touched,
    COUNT(CASE WHEN is_dsgvo_relevant THEN 1 END) as dsgvo_actions,
    MIN(occurred_at) as first_action,
    MAX(occurred_at) as last_action,
    COUNT(DISTINCT ip_address) as unique_ips,
    ARRAY_AGG(DISTINCT action ORDER BY action) as action_types,
    AVG(processing_time_ms) as avg_processing_time
FROM audit_logs
WHERE occurred_at > CURRENT_TIMESTAMP - INTERVAL '90 days'
GROUP BY user_id, user_name, user_role;

CREATE INDEX idx_user_profiles_user ON user_activity_profiles(user_id);
CREATE INDEX idx_user_profiles_actions ON user_activity_profiles(total_actions DESC);

-- View für Suspicious Activity Patterns
CREATE MATERIALIZED VIEW suspicious_patterns AS
WITH hourly_activity AS (
    SELECT 
        user_id,
        DATE_TRUNC('hour', occurred_at) as hour,
        COUNT(*) as actions_count,
        COUNT(DISTINCT entity_id) as entities_count
    FROM audit_logs
    WHERE occurred_at > CURRENT_TIMESTAMP - INTERVAL '7 days'
    GROUP BY user_id, DATE_TRUNC('hour', occurred_at)
),
unusual_hours AS (
    SELECT *
    FROM hourly_activity
    WHERE EXTRACT(HOUR FROM hour) NOT BETWEEN 7 AND 19
    AND actions_count > 10
),
rapid_exports AS (
    SELECT 
        user_id,
        occurred_at,
        LAG(occurred_at) OVER (PARTITION BY user_id ORDER BY occurred_at) as prev_export,
        occurred_at - LAG(occurred_at) OVER (PARTITION BY user_id ORDER BY occurred_at) as time_between
    FROM audit_logs
    WHERE action = 'EXPORT'
    AND occurred_at > CURRENT_TIMESTAMP - INTERVAL '7 days'
)
SELECT 
    'UNUSUAL_HOURS' as pattern_type,
    user_id,
    hour as detected_at,
    actions_count as severity_score,
    jsonb_build_object(
        'hour', hour,
        'actions', actions_count,
        'entities', entities_count
    ) as details
FROM unusual_hours

UNION ALL

SELECT 
    'RAPID_EXPORTS' as pattern_type,
    user_id,
    occurred_at as detected_at,
    CASE 
        WHEN time_between < INTERVAL '1 minute' THEN 100
        WHEN time_between < INTERVAL '5 minutes' THEN 75
        WHEN time_between < INTERVAL '10 minutes' THEN 50
        ELSE 25
    END as severity_score,
    jsonb_build_object(
        'export_time', occurred_at,
        'previous_export', prev_export,
        'time_between_seconds', EXTRACT(EPOCH FROM time_between)
    ) as details
FROM rapid_exports
WHERE time_between < INTERVAL '10 minutes';

CREATE INDEX idx_suspicious_type ON suspicious_patterns(pattern_type);
CREATE INDEX idx_suspicious_user ON suspicious_patterns(user_id);
CREATE INDEX idx_suspicious_severity ON suspicious_patterns(severity_score DESC);

-- Refresh Function for Views
CREATE OR REPLACE FUNCTION refresh_audit_admin_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY audit_dashboard_stats;
    REFRESH MATERIALIZED VIEW CONCURRENTLY user_activity_profiles;
    REFRESH MATERIALIZED VIEW CONCURRENTLY suspicious_patterns;
END;
$$ LANGUAGE plpgsql;

-- Schedule automatic refresh every 5 minutes
-- (Requires pg_cron extension or external scheduler)
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Backend Admin Services (90 Min)
- [ ] AuditAdminService mit Dashboard-Statistiken
- [ ] ComplianceReportService für DSGVO-Reports
- [ ] AuditStatisticsService für Metriken
- [ ] SuspiciousActivityDetector
- [ ] AuditAdminResource REST Endpoints

### Phase 2: Database Optimization (30 Min)
- [ ] Materialized Views für Performance
- [ ] Indizes für Admin-Queries
- [ ] Refresh-Strategien implementieren
- [ ] Partitionierung vorbereiten

### Phase 3: Frontend Dashboard (90 Min)
- [ ] AuditAdminDashboard Hauptkomponente
- [ ] StatisticsCards für Metriken
- [ ] ActivityHeatmap Visualisierung
- [ ] SuspiciousActivityList
- [ ] ComplianceStatusPanel

### Phase 4: Real-time Features (60 Min)
- [ ] WebSocket für Live-Stream
- [ ] Alert-System für kritische Events
- [ ] Auto-Refresh Mechanismus
- [ ] Push-Notifications

### Phase 5: Compliance & Export (45 Min)
- [ ] DSGVO Report Generator
- [ ] Export in PDF/Excel/CSV
- [ ] Digitale Signatur für Reports
- [ ] Audit des Audits (Meta-Audit)

### Phase 6: Testing & Documentation (45 Min)
- [ ] Unit Tests für Admin Services
- [ ] E2E Tests für Dashboard
- [ ] Performance Tests für Views
- [ ] Admin-Dokumentation

## 🔗 INTEGRATION POINTS

1. **Audit Trail System** - Basis-Audit aus Step3
2. **User Management** - Benutzer-Profile und Rollen
3. **Security Alerts** - Integration mit Alert-System
4. **DSGVO Module** - Compliance-Features
5. **Export Service** - Report-Generierung

## ⚠️ KRITISCHE ÜBERLEGUNGEN

1. **Performance bei großen Datenmengen**
   → Lösung: Materialized Views & Pagination

2. **Echtzeit vs. Performance**
   → Lösung: WebSocket mit Throttling

3. **Datenschutz im Admin-Bereich**
   → Lösung: Audit des Audit-Zugriffs

4. **Report-Signierung**
   → Lösung: Digitale Signaturen mit Zeitstempel

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 360 Minuten (6 Stunden)  
**Nächstes Dokument:** [→ Smart Contact Cards](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md)  
**Parent:** [↑ Step3 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Enterprise Compliance + Admin Control = Trust! 📊🔒✨**