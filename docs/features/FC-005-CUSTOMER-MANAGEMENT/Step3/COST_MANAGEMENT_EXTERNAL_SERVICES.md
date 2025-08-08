# 💰 Cost Management External Services - Budget-Kontrolle für KI-Features

**Phase:** 0 - Critical Foundation  
**Priorität:** 🔴 KRITISCH - Unkontrollierte Kosten = Projekt-Stopp  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/OFFLINE_CONFLICT_RESOLUTION.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/IN_APP_HELP_SYSTEM.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Blockiert:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md`
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AI_FEATURES.md`

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Cost Management implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Budget Configuration
mkdir -p backend/src/main/java/de/freshplan/api/budget
touch backend/src/main/java/de/freshplan/api/budget/ApiCostManager.java
touch backend/src/main/java/de/freshplan/api/budget/BudgetConfiguration.java
touch backend/src/main/resources/api-budget.properties

# 2. Frontend Budget Dashboard
mkdir -p frontend/src/features/customers/components/budget
touch frontend/src/features/customers/components/budget/BudgetDashboard.tsx
touch frontend/src/features/customers/components/budget/CostIndicator.tsx
touch frontend/src/features/customers/components/budget/FeatureModeToggle.tsx

# 3. Service Level Management
mkdir -p frontend/src/features/customers/services/budget
touch frontend/src/features/customers/services/budget/ServiceLevelManager.ts
touch frontend/src/features/customers/services/budget/CostCalculator.ts

# 4. Tests
mkdir -p backend/src/test/java/de/freshplan/api/budget
touch backend/src/test/java/de/freshplan/api/budget/ApiCostManagerTest.java
```

## 🎯 Das Problem: KI-Features können teuer werden

**Kostentreiber:**
- 🤖 OpenAI API: $0.002 pro 1K Tokens
- 📍 Google Geocoding: $0.005 pro Request
- ✉️ Email Validation: $0.001 pro Check
- 📊 Sentiment Analysis: $0.003 pro Text
- 🔍 LinkedIn Enrichment: $0.10 pro Profil

**Bei 50 Usern × 100 Kontakte × 10 Actions/Tag = 💸 $1,500/Monat!**

## 💡 DIE LÖSUNG: Positives Framing & Transparenz

### 1. Service Level Configuration

**Datei:** `backend/src/main/resources/api-budget.properties`

```properties
# CLAUDE: Budget-Konfiguration für externe Services
# Pfad: backend/src/main/resources/api-budget.properties

# Monatliche Budgets (in EUR)
budget.monthly.total=500.00
budget.monthly.warning.threshold=0.80
budget.monthly.critical.threshold=0.95

# Service-spezifische Limits
budget.openai.monthly=200.00
budget.openai.daily=10.00
budget.openai.per.user=5.00

budget.geocoding.monthly=50.00
budget.email.validation.monthly=30.00
budget.sentiment.analysis.monthly=100.00
budget.linkedin.enrichment.monthly=120.00

# Service Levels
service.level.premium.features=AI_SUGGESTIONS,SENTIMENT_ANALYSIS,AUTO_ENRICHMENT
service.level.standard.features=RULE_SUGGESTIONS,BASIC_VALIDATION
service.level.basic.features=STATIC_SUGGESTIONS,MANUAL_ENTRY

# Fallback-Strategien
fallback.openai=RULE_ENGINE
fallback.geocoding=MANUAL_ENTRY
fallback.email.validation=REGEX_CHECK
fallback.sentiment=KEYWORD_ANALYSIS
fallback.linkedin=SKIP

# Gamification Points
gamification.points.smart.saver=50
gamification.points.efficiency.master=100
gamification.points.budget.conscious=75
```

### 2. Backend Cost Manager

**Datei:** `backend/src/main/java/de/freshplan/api/budget/ApiCostManager.java`

```java
// CLAUDE: Intelligente API-Kosten Verwaltung
// Pfad: backend/src/main/java/de/freshplan/api/budget/ApiCostManager.java

package de.freshplan.api.budget;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ApiCostManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(ApiCostManager.class);
    
    @ConfigProperty(name = "budget.monthly.total")
    BigDecimal monthlyBudget;
    
    @ConfigProperty(name = "budget.monthly.warning.threshold")
    BigDecimal warningThreshold;
    
    @Inject
    OpenAiService openAiService;
    
    @Inject
    RuleEngineService ruleEngine;
    
    @Inject
    NotificationService notifications;
    
    // Track costs per service
    private final Map<String, ServiceUsage> serviceUsage = new ConcurrentHashMap<>();
    
    // Current service level
    private ServiceLevel currentLevel = ServiceLevel.PREMIUM;
    
    public enum ServiceLevel {
        PREMIUM("⚡ Turbo-Modus", "Alle KI-Features aktiv", "#4CAF50"),
        STANDARD("🌱 Sparmodus", "Basis-Features verfügbar", "#FFC107"),
        BASIC("💤 Ruhemodus", "Minimale Features", "#9E9E9E");
        
        private final String displayName;
        private final String description;
        private final String color;
        
        ServiceLevel(String displayName, String description, String color) {
            this.displayName = displayName;
            this.description = description;
            this.color = color;
        }
    }
    
    /**
     * Intelligente Suggestions mit Budget-Awareness
     */
    public SuggestionResponse getSuggestions(Contact contact, String userId) {
        ServiceUsage usage = getCurrentUsage();
        
        // Check user-specific budget
        BigDecimal userSpent = getUserMonthlySpent(userId);
        BigDecimal userLimit = new BigDecimal("5.00");
        
        if (userSpent.compareTo(userLimit) >= 0) {
            // User hat persönliches Limit erreicht
            return createBudgetAwareSuggestion(
                "💡 Dein persönliches KI-Budget ist aufgebraucht. " +
                "Nutze die Regel-basierten Vorschläge!",
                ruleEngine.getBasicSuggestions(contact)
            );
        }
        
        // Check global budget capacity
        if (hasCapacity(ServiceLevel.PREMIUM)) {
            trackCost("openai", new BigDecimal("0.02"), userId);
            return openAiService.getSmartSuggestions(contact);
            
        } else if (hasCapacity(ServiceLevel.STANDARD)) {
            // Hybrid-Ansatz: Einfache KI + Rules
            return combineApproaches(contact);
            
        } else {
            // Nur regelbasierte Suggestions
            return createBudgetAwareSuggestion(
                "🌱 Sparmodus aktiv - schone das Budget für wichtige Aktionen!",
                ruleEngine.getBasicSuggestions(contact)
            );
        }
    }
    
    /**
     * Berechnet aktuelle Burn Rate
     */
    public BurnRateAnalysis analyzeBurnRate() {
        BigDecimal totalSpent = calculateMonthlyTotal();
        int dayOfMonth = LocalDateTime.now().getDayOfMonth();
        int daysInMonth = LocalDateTime.now().toLocalDate().lengthOfMonth();
        
        BigDecimal expectedSpent = monthlyBudget
            .multiply(new BigDecimal(dayOfMonth))
            .divide(new BigDecimal(daysInMonth), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal burnRate = totalSpent.divide(expectedSpent, 2, BigDecimal.ROUND_HALF_UP);
        
        return BurnRateAnalysis.builder()
            .currentSpent(totalSpent)
            .expectedSpent(expectedSpent)
            .burnRate(burnRate)
            .daysRemaining(daysInMonth - dayOfMonth)
            .projectedTotal(calculateProjection(totalSpent, dayOfMonth, daysInMonth))
            .recommendation(generateRecommendation(burnRate))
            .build();
    }
    
    /**
     * Stündliche Budget-Überwachung
     */
    @Scheduled(cron = "0 0 * * * *")
    public void monitorBudget() {
        BurnRateAnalysis analysis = analyzeBurnRate();
        
        if (analysis.getBurnRate().compareTo(new BigDecimal("1.2")) > 0) {
            // 20% über Plan
            LOG.warn("Budget burn rate at {}%, switching to STANDARD mode", 
                analysis.getBurnRate().multiply(new BigDecimal("100")));
            
            currentLevel = ServiceLevel.STANDARD;
            
            notifications.notifyAdmins(
                "⚠️ Budget-Warnung",
                String.format("Burn Rate bei %.0f%%. Wechsel zu Sparmodus.",
                    analysis.getBurnRate().multiply(new BigDecimal("100")).doubleValue())
            );
            
        } else if (analysis.getBurnRate().compareTo(new BigDecimal("1.5")) > 0) {
            // 50% über Plan - Kritisch!
            currentLevel = ServiceLevel.BASIC;
            
            notifications.notifyAdmins(
                "🚨 Budget Kritisch",
                "Nur noch Basis-Features aktiv!"
            );
        }
        
        // Positive Reinforcement für sparsame Teams
        if (analysis.getBurnRate().compareTo(new BigDecimal("0.8")) < 0) {
            awardGamificationPoints("SMART_SAVER", 50);
        }
    }
    
    /**
     * Dashboard Metrics für Frontend
     */
    public BudgetDashboardData getDashboardData() {
        BigDecimal totalSpent = calculateMonthlyTotal();
        BigDecimal remaining = monthlyBudget.subtract(totalSpent);
        
        // Top Cost Drivers
        List<CostDriver> topDrivers = serviceUsage.entrySet().stream()
            .map(e -> new CostDriver(
                e.getKey(),
                e.getValue().getMonthlyTotal(),
                e.getValue().getCallCount()
            ))
            .sorted((a, b) -> b.getCost().compareTo(a.getCost()))
            .limit(5)
            .toList();
        
        // User Rankings (Gamification)
        List<UserEfficiency> rankings = calculateUserRankings();
        
        return BudgetDashboardData.builder()
            .monthlyBudget(monthlyBudget)
            .currentSpent(totalSpent)
            .remaining(remaining)
            .percentUsed(totalSpent.divide(monthlyBudget, 2, BigDecimal.ROUND_HALF_UP))
            .serviceLevel(currentLevel)
            .topCostDrivers(topDrivers)
            .userRankings(rankings)
            .projection(calculateProjection())
            .savingsTips(generateSavingsTips())
            .achievements(getUserAchievements())
            .build();
    }
    
    /**
     * Intelligente Service-Degradation
     */
    private SuggestionResponse combineApproaches(Contact contact) {
        // Nutze KI nur für wichtigste Features
        List<Suggestion> suggestions = new ArrayList<>();
        
        // Regel-basierte Basis-Suggestions (kostenlos)
        suggestions.addAll(ruleEngine.getBasicSuggestions(contact));
        
        // KI nur für Decision Maker
        if ("EXECUTIVE".equals(contact.getDecisionLevel())) {
            trackCost("openai", new BigDecimal("0.01"));
            suggestions.add(openAiService.getExecutiveSuggestion(contact));
        }
        
        return new SuggestionResponse(suggestions, ServiceLevel.STANDARD);
    }
    
    /**
     * Generiert Spar-Tipps basierend auf Nutzung
     */
    private List<String> generateSavingsTips() {
        List<String> tips = new ArrayList<>();
        
        ServiceUsage openAiUsage = serviceUsage.get("openai");
        if (openAiUsage != null && openAiUsage.getCallCount() > 100) {
            tips.add("💡 Tipp: Nutze Batch-Processing für AI-Suggestions (-30% Kosten)");
        }
        
        if (currentLevel == ServiceLevel.PREMIUM && 
            calculateMonthlyTotal().compareTo(monthlyBudget.multiply(new BigDecimal("0.5"))) > 0) {
            tips.add("🎯 Fokussiere KI auf Key Accounts für besseren ROI");
        }
        
        tips.add("🏆 Challenge: Einen Tag ohne Premium-Features = 100 Punkte!");
        
        return tips;
    }
    
    /**
     * Gamification: Achievements für sparsame Nutzung
     */
    private void awardGamificationPoints(String achievement, int points) {
        LOG.info("Achievement unlocked: {} (+{} points)", achievement, points);
        
        // Store in user profile
        notifications.showAchievement(
            "🏆 " + achievement,
            "+" + points + " Punkte für effiziente Nutzung!"
        );
    }
    
    private static class ServiceUsage {
        private BigDecimal monthlyTotal = BigDecimal.ZERO;
        private int callCount = 0;
        private LocalDateTime lastReset = LocalDateTime.now();
        
        public synchronized void addCost(BigDecimal cost) {
            // Reset monthly
            if (LocalDateTime.now().getMonth() != lastReset.getMonth()) {
                monthlyTotal = BigDecimal.ZERO;
                callCount = 0;
                lastReset = LocalDateTime.now();
            }
            
            monthlyTotal = monthlyTotal.add(cost);
            callCount++;
        }
        
        // Getters...
    }
}
```

### 3. Frontend Budget Dashboard

**Datei:** `frontend/src/features/customers/components/budget/BudgetDashboard.tsx`

```typescript
// CLAUDE: User-freundliches Budget Dashboard
// Pfad: frontend/src/features/customers/components/budget/BudgetDashboard.tsx

import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  Grid,
  Typography,
  LinearProgress,
  Chip,
  Box,
  Button,
  Alert,
  IconButton,
  Tooltip,
  Tabs,
  Tab,
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Badge
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  EmojiEvents as TrophyIcon,
  Lightbulb as TipIcon,
  Speed as SpeedIcon,
  Eco as EcoIcon,
  BatteryFull as BatteryIcon,
  Info as InfoIcon,
  Star as StarIcon
} from '@mui/icons-material';
import { budgetService } from '../../services/budget/BudgetService';
import { ServiceLevel, BudgetData } from '../../types/budget.types';

export const BudgetDashboard: React.FC = () => {
  const [budgetData, setBudgetData] = useState<BudgetData | null>(null);
  const [activeTab, setActiveTab] = useState(0);
  const [showDetails, setShowDetails] = useState(false);
  
  useEffect(() => {
    loadBudgetData();
    const interval = setInterval(loadBudgetData, 60000); // Update jede Minute
    return () => clearInterval(interval);
  }, []);
  
  const loadBudgetData = async () => {
    const data = await budgetService.getDashboardData();
    setBudgetData(data);
  };
  
  if (!budgetData) return <div>Loading...</div>;
  
  const getServiceLevelIcon = (level: ServiceLevel) => {
    switch (level) {
      case 'PREMIUM': return <SpeedIcon sx={{ color: '#4CAF50' }} />;
      case 'STANDARD': return <EcoIcon sx={{ color: '#FFC107' }} />;
      case 'BASIC': return <BatteryIcon sx={{ color: '#9E9E9E' }} />;
    }
  };
  
  const getProgressColor = (percent: number): 'success' | 'warning' | 'error' => {
    if (percent < 60) return 'success';
    if (percent < 85) return 'warning';
    return 'error';
  };
  
  return (
    <Grid container spacing={3}>
      {/* Header mit Service Level */}
      <Grid item xs={12}>
        <Card sx={{ 
          background: `linear-gradient(135deg, ${budgetData.serviceLevel.color}22, white)`,
          border: `2px solid ${budgetData.serviceLevel.color}`
        }}>
          <CardContent>
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box display="flex" alignItems="center" gap={2}>
                {getServiceLevelIcon(budgetData.serviceLevel)}
                <Box>
                  <Typography variant="h5" fontWeight="bold">
                    {budgetData.serviceLevel.displayName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {budgetData.serviceLevel.description}
                  </Typography>
                </Box>
              </Box>
              
              <Box textAlign="right">
                <Typography variant="h4" fontWeight="bold">
                  {budgetData.remaining.toFixed(2)}€
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  verfügbar diese Monat
                </Typography>
              </Box>
            </Box>
            
            {/* Budget Progress Bar */}
            <Box sx={{ mt: 3 }}>
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography variant="body2">
                  Budget-Verbrauch
                </Typography>
                <Typography variant="body2" fontWeight="bold">
                  {(budgetData.percentUsed * 100).toFixed(0)}%
                </Typography>
              </Box>
              <LinearProgress 
                variant="determinate" 
                value={budgetData.percentUsed * 100}
                color={getProgressColor(budgetData.percentUsed * 100)}
                sx={{ height: 10, borderRadius: 5 }}
              />
              <Box display="flex" justifyContent="space-between" mt={1}>
                <Typography variant="caption">
                  {budgetData.currentSpent.toFixed(2)}€ ausgegeben
                </Typography>
                <Typography variant="caption">
                  {budgetData.monthlyBudget.toFixed(2)}€ Budget
                </Typography>
              </Box>
            </Box>
            
            {/* Projection */}
            {budgetData.projection > budgetData.monthlyBudget && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                <Typography variant="body2">
                  📊 Hochrechnung: {budgetData.projection.toFixed(2)}€ 
                  ({((budgetData.projection / budgetData.monthlyBudget - 1) * 100).toFixed(0)}% über Budget)
                </Typography>
              </Alert>
            )}
          </CardContent>
        </Card>
      </Grid>
      
      {/* Tabs für verschiedene Ansichten */}
      <Grid item xs={12}>
        <Card>
          <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
            <Tab label="Übersicht" />
            <Tab label="Top Kostentreiber" />
            <Tab label="Team Rankings" />
            <Tab label="Spar-Tipps" />
          </Tabs>
          
          <CardContent>
            {activeTab === 0 && (
              <Grid container spacing={2}>
                {/* Quick Stats */}
                <Grid item xs={12} md={3}>
                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="subtitle2" color="text.secondary">
                        Burn Rate
                      </Typography>
                      <Typography variant="h4">
                        {budgetData.burnRate.toFixed(1)}x
                      </Typography>
                      {budgetData.burnRate > 1 ? (
                        <Chip 
                          icon={<TrendingUpIcon />} 
                          label="Über Plan" 
                          color="warning" 
                          size="small" 
                        />
                      ) : (
                        <Chip 
                          icon={<TrendingDownIcon />} 
                          label="Unter Plan" 
                          color="success" 
                          size="small" 
                        />
                      )}
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12} md={3}>
                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="subtitle2" color="text.secondary">
                        Tage verbleibend
                      </Typography>
                      <Typography variant="h4">
                        {budgetData.daysRemaining}
                      </Typography>
                      <Typography variant="caption">
                        im aktuellen Monat
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12} md={3}>
                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="subtitle2" color="text.secondary">
                        Heute ausgegeben
                      </Typography>
                      <Typography variant="h4">
                        {budgetData.todaySpent.toFixed(2)}€
                      </Typography>
                      <LinearProgress 
                        variant="determinate" 
                        value={(budgetData.todaySpent / budgetData.dailyBudget) * 100}
                        sx={{ mt: 1 }}
                      />
                    </CardContent>
                  </Card>
                </Grid>
                
                <Grid item xs={12} md={3}>
                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="subtitle2" color="text.secondary">
                        Effizienz-Score
                      </Typography>
                      <Typography variant="h4">
                        {budgetData.efficiencyScore}
                      </Typography>
                      <Box display="flex" gap={0.5}>
                        {[...Array(5)].map((_, i) => (
                          <StarIcon 
                            key={i}
                            sx={{ 
                              color: i < Math.floor(budgetData.efficiencyScore / 20) 
                                ? '#FFD700' 
                                : '#E0E0E0',
                              fontSize: 16
                            }}
                          />
                        ))}
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              </Grid>
            )}
            
            {activeTab === 1 && (
              <List>
                {budgetData.topCostDrivers.map((driver, index) => (
                  <ListItem key={driver.service}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: '#1976d2' }}>
                        {index + 1}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={driver.serviceName}
                      secondary={`${driver.callCount} Aufrufe`}
                    />
                    <Typography variant="h6" color="error">
                      {driver.cost.toFixed(2)}€
                    </Typography>
                  </ListItem>
                ))}
              </List>
            )}
            
            {activeTab === 2 && (
              <List>
                {budgetData.userRankings.map((user, index) => (
                  <ListItem key={user.userId}>
                    <ListItemAvatar>
                      <Badge 
                        badgeContent={index === 0 ? '👑' : `#${index + 1}`}
                        color={index === 0 ? 'warning' : 'default'}
                      >
                        <Avatar>{user.name.charAt(0)}</Avatar>
                      </Badge>
                    </ListItemAvatar>
                    <ListItemText
                      primary={user.name}
                      secondary={
                        <Box>
                          <Typography variant="caption">
                            Effizienz: {user.efficiency}% | 
                            Gespart: {user.saved.toFixed(2)}€
                          </Typography>
                        </Box>
                      }
                    />
                    {user.achievements.map(achievement => (
                      <Chip 
                        key={achievement}
                        label={achievement} 
                        size="small" 
                        sx={{ ml: 1 }}
                      />
                    ))}
                  </ListItem>
                ))}
              </List>
            )}
            
            {activeTab === 3 && (
              <Grid container spacing={2}>
                {budgetData.savingsTips.map((tip, index) => (
                  <Grid item xs={12} key={index}>
                    <Alert 
                      severity="info" 
                      icon={<TipIcon />}
                      action={
                        <Button size="small" color="primary">
                          Mehr erfahren
                        </Button>
                      }
                    >
                      {tip}
                    </Alert>
                  </Grid>
                ))}
                
                {/* Achievements */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>
                    🏆 Deine Achievements
                  </Typography>
                  <Box display="flex" gap={1} flexWrap="wrap">
                    {budgetData.achievements.map(achievement => (
                      <Chip
                        key={achievement.id}
                        icon={<TrophyIcon />}
                        label={`${achievement.name} (+${achievement.points})`}
                        color={achievement.unlocked ? 'success' : 'default'}
                        variant={achievement.unlocked ? 'filled' : 'outlined'}
                      />
                    ))}
                  </Box>
                </Grid>
              </Grid>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

### 4. Feature Mode Toggle

**Datei:** `frontend/src/features/customers/components/budget/FeatureModeToggle.tsx`

```typescript
// CLAUDE: Toggle zwischen Service Levels
// Pfad: frontend/src/features/customers/components/budget/FeatureModeToggle.tsx

import React from 'react';
import { 
  ToggleButtonGroup, 
  ToggleButton,
  Tooltip,
  Box,
  Typography
} from '@mui/material';
import {
  Speed as PremiumIcon,
  Eco as StandardIcon,
  PowerSettingsNew as BasicIcon
} from '@mui/icons-material';

interface FeatureModeToggleProps {
  currentLevel: 'PREMIUM' | 'STANDARD' | 'BASIC';
  availableLevels: string[];
  onLevelChange: (level: string) => void;
  budgetRemaining: number;
}

export const FeatureModeToggle: React.FC<FeatureModeToggleProps> = ({
  currentLevel,
  availableLevels,
  onLevelChange,
  budgetRemaining
}) => {
  
  const getModeInfo = (level: string) => {
    switch (level) {
      case 'PREMIUM':
        return {
          icon: <PremiumIcon />,
          label: 'Turbo',
          color: '#4CAF50',
          tooltip: '⚡ Alle KI-Features aktiv (ca. 2€/Tag)',
          cost: 2.0
        };
      case 'STANDARD':
        return {
          icon: <StandardIcon />,
          label: 'Sparmodus',
          color: '#FFC107',
          tooltip: '🌱 Basis-KI + Regeln (ca. 0.50€/Tag)',
          cost: 0.5
        };
      case 'BASIC':
        return {
          icon: <BasicIcon />,
          label: 'Minimal',
          color: '#9E9E9E',
          tooltip: '💤 Nur Regel-basiert (kostenlos)',
          cost: 0
        };
      default:
        return null;
    }
  };
  
  const canAfford = (level: string): boolean => {
    const info = getModeInfo(level);
    if (!info) return false;
    return budgetRemaining >= info.cost * 10; // 10 Tage Puffer
  };
  
  return (
    <Box>
      <Typography variant="caption" color="text.secondary" gutterBottom>
        Feature-Modus
      </Typography>
      <ToggleButtonGroup
        value={currentLevel}
        exclusive
        onChange={(e, value) => value && onLevelChange(value)}
        size="small"
        sx={{ mt: 1 }}
      >
        {['PREMIUM', 'STANDARD', 'BASIC'].map(level => {
          const info = getModeInfo(level);
          if (!info) return null;
          
          const isAvailable = availableLevels.includes(level);
          const isAffordable = canAfford(level);
          
          return (
            <Tooltip 
              key={level}
              title={
                <Box>
                  <Typography variant="body2">{info.tooltip}</Typography>
                  {!isAffordable && (
                    <Typography variant="caption" color="error">
                      ⚠️ Budget reicht nicht für 10 Tage
                    </Typography>
                  )}
                </Box>
              }
            >
              <ToggleButton 
                value={level}
                disabled={!isAvailable || !isAffordable}
                sx={{
                  '&.Mui-selected': {
                    backgroundColor: `${info.color}22`,
                    borderColor: info.color
                  }
                }}
              >
                <Box display="flex" alignItems="center" gap={0.5}>
                  {info.icon}
                  <Typography variant="button">
                    {info.label}
                  </Typography>
                </Box>
              </ToggleButton>
            </Tooltip>
          );
        })}
      </ToggleButtonGroup>
      
      {currentLevel !== 'BASIC' && (
        <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
          💡 Wechsle zu Sparmodus für 50 Bonus-Punkte!
        </Typography>
      )}
    </Box>
  );
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Configuration (30 Min)
- [ ] api-budget.properties erstellen
- [ ] Budget-Limits definieren
- [ ] Service-Level konfigurieren

### Phase 2: Backend Services (45 Min)
- [ ] ApiCostManager implementieren
- [ ] BudgetConfiguration erstellen
- [ ] Fallback-Strategien einrichten

### Phase 3: Frontend Dashboard (45 Min)
- [ ] BudgetDashboard Component
- [ ] FeatureModeToggle
- [ ] CostIndicator in UI

### Phase 4: Gamification (30 Min)
- [ ] Achievement System
- [ ] User Rankings
- [ ] Spar-Challenges

### Phase 5: Testing (30 Min)
- [ ] Unit Tests für Cost Calculation
- [ ] Integration Tests für Fallback
- [ ] UI Tests für Dashboard

## 🔗 INTEGRATION POINTS

1. **SuggestionEngine** - Fallback zu Rules bei Budget-Limit
2. **ContactEnrichment** - Skip bei niedrigem Budget
3. **AdminDashboard** - Budget-Monitoring
4. **UserProfile** - Achievements & Points

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Negative User Experience**
   → Lösung: Positives Framing statt Fehlermeldungen

2. **Intransparente Kosten**
   → Lösung: Live-Dashboard mit Projektion

3. **Harte Limits frustrieren**
   → Lösung: Graceful Degradation mit Alternativen

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 180 Minuten  
**Nächstes Dokument:** [→ In-App Help System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/IN_APP_HELP_SYSTEM.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Budget-Kontrolle = Nachhaltiger Erfolg! 💰✨**