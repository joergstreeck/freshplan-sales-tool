# 🤖 AI Data Enrichment - Intelligente Daten-Anreicherung

**Phase:** 3 - Premium Features  
**Priorität:** 🟢 MITTEL - Automatisierung spart Zeit  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SOCIAL_MEDIA_INTEGRATION.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PERFORMANCE_OPTIMIZATION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Datenqualität (Automatische Vervollständigung)
- Effizienz (Zeitersparnis)
- Intelligence (Mehr Insights)

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - AI Data Enrichment implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend AI Services
mkdir -p backend/src/main/java/de/freshplan/ai
touch backend/src/main/java/de/freshplan/ai/service/AIEnrichmentService.java
touch backend/src/main/java/de/freshplan/ai/service/CompanyDataService.java
touch backend/src/main/java/de/freshplan/ai/service/ContactPredictionService.java
touch backend/src/main/java/de/freshplan/ai/entity/EnrichmentTask.java

# 2. Frontend AI Components
mkdir -p frontend/src/features/customers/components/ai
touch frontend/src/features/customers/components/ai/AIEnrichmentPanel.tsx
touch frontend/src/features/customers/components/ai/DataSuggestions.tsx
touch frontend/src/features/customers/components/ai/EnrichmentProgress.tsx
touch frontend/src/features/customers/components/ai/AIConfidenceIndicator.tsx

# 3. AI Models Configuration
touch backend/src/main/resources/ai-models-config.json

# 4. Migration (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__create_ai_enrichment_tables.sql

# 5. Tests
mkdir -p backend/src/test/java/de/freshplan/ai
touch backend/src/test/java/de/freshplan/ai/AIEnrichmentServiceTest.java
```

## 🎯 Das Problem: Lückenhafte Daten

**Datenlücken kosten Zeit & Geld:**
- 📧 50% der Emails fehlen
- 📞 Telefonnummern veraltet
- 🏢 Firmendaten unvollständig
- 👤 Titel und Positionen unklar
- 📊 Firmengrößen geschätzt

## 💡 DIE LÖSUNG: KI-gestützte Daten-Anreicherung

### 1. AI Enrichment Service

**Datei:** `backend/src/main/java/de/freshplan/ai/service/AIEnrichmentService.java`

```java
// CLAUDE: AI-gestützte Datenanreicherung mit verschiedenen Datenquellen
// Pfad: backend/src/main/java/de/freshplan/ai/service/AIEnrichmentService.java

package de.freshplan.ai.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Transactional
public class AIEnrichmentService {
    
    @Inject
    CompanyDataService companyDataService;
    
    @Inject
    ContactPredictionService contactPredictionService;
    
    @Inject
    EmailFinderService emailFinderService;
    
    @Inject
    EnrichmentTaskRepository taskRepository;
    
    @Inject
    AIModelService aiModelService;
    
    @ConfigProperty(name = "ai.enrichment.confidence.threshold", defaultValue = "0.7")
    double confidenceThreshold;
    
    @ConfigProperty(name = "ai.enrichment.batch.size", defaultValue = "10")
    int batchSize;
    
    @ConfigProperty(name = "ai.enrichment.daily.limit", defaultValue = "500")
    int dailyLimit;
    
    private final Map<String, Pattern> emailPatterns = new HashMap<>();
    
    @PostConstruct
    void init() {
        // Common German company email patterns
        emailPatterns.put("firstname.lastname", Pattern.compile("^[a-z]+\\.[a-z]+@"));
        emailPatterns.put("f.lastname", Pattern.compile("^[a-z]\\.[a-z]+@"));
        emailPatterns.put("firstname", Pattern.compile("^[a-z]+@"));
        emailPatterns.put("firstnamelastname", Pattern.compile("^[a-z]+[a-z]+@"));
    }
    
    /**
     * Enrich customer data with AI
     */
    public CompletableFuture<EnrichmentResult> enrichCustomer(Customer customer) {
        return CompletableFuture.supplyAsync(() -> {
            EnrichmentResult result = new EnrichmentResult();
            result.setCustomerId(customer.getId());
            result.setStartTime(LocalDateTime.now());
            
            // Check daily limits
            if (!checkDailyLimit()) {
                result.setStatus(EnrichmentStatus.LIMIT_EXCEEDED);
                return result;
            }
            
            try {
                // 1. Enrich company data
                CompanyEnrichment companyData = enrichCompanyData(customer);
                if (companyData != null) {
                    applyCompanyEnrichment(customer, companyData);
                    result.addEnrichment("company", companyData);
                }
                
                // 2. Enrich contact data
                for (CustomerContact contact : customer.getContacts()) {
                    ContactEnrichment contactData = enrichContactData(contact, customer);
                    if (contactData != null) {
                        applyContactEnrichment(contact, contactData);
                        result.addEnrichment("contact_" + contact.getId(), contactData);
                    }
                }
                
                // 3. Predict missing data
                PredictionResult predictions = predictMissingData(customer);
                if (predictions != null) {
                    applyPredictions(customer, predictions);
                    result.addPredictions(predictions);
                }
                
                // 4. Validate and score
                DataQualityScore qualityScore = calculateQualityScore(customer);
                result.setQualityScore(qualityScore);
                
                // 5. Generate insights
                List<EnrichmentInsight> insights = generateInsights(customer, result);
                result.setInsights(insights);
                
                result.setStatus(EnrichmentStatus.SUCCESS);
                
            } catch (Exception e) {
                log.error("Enrichment failed for customer {}: {}", 
                    customer.getId(), e.getMessage());
                result.setStatus(EnrichmentStatus.FAILED);
                result.setErrorMessage(e.getMessage());
            }
            
            result.setEndTime(LocalDateTime.now());
            saveEnrichmentTask(result);
            
            return result;
        });
    }
    
    /**
     * Enrich company data from external sources
     */
    private CompanyEnrichment enrichCompanyData(Customer customer) {
        if (customer.getName() == null) return null;
        
        try {
            // Try multiple data sources
            CompanyEnrichment enrichment = new CompanyEnrichment();
            
            // 1. Handelsregister API (German companies)
            if (isGermanCompany(customer)) {
                HandelsregisterData hrData = companyDataService
                    .fetchFromHandelsregister(customer.getName());
                if (hrData != null) {
                    enrichment.setLegalForm(hrData.getLegalForm());
                    enrichment.setRegistrationNumber(hrData.getRegistrationNumber());
                    enrichment.setRegisteredAddress(hrData.getAddress());
                    enrichment.setConfidence(0.95); // High confidence for official data
                }
            }
            
            // 2. Company website scraping
            if (customer.getWebsite() != null) {
                WebsiteData websiteData = companyDataService
                    .scrapeWebsite(customer.getWebsite());
                if (websiteData != null) {
                    enrichment.setIndustry(websiteData.getIndustry());
                    enrichment.setEmployeeCount(websiteData.getEmployeeCount());
                    enrichment.setDescription(websiteData.getDescription());
                    enrichment.setSocialLinks(websiteData.getSocialLinks());
                }
            }
            
            // 3. Business databases (Dun & Bradstreet, etc.)
            if (customer.getTaxId() != null) {
                BusinessData businessData = companyDataService
                    .fetchFromBusinessDB(customer.getTaxId());
                if (businessData != null) {
                    enrichment.setRevenue(businessData.getRevenue());
                    enrichment.setCreditRating(businessData.getCreditRating());
                    enrichment.setYearFounded(businessData.getYearFounded());
                }
            }
            
            // 4. AI-based inference
            if (enrichment.getIndustry() == null) {
                String predictedIndustry = aiModelService.predictIndustry(
                    customer.getName(),
                    customer.getDescription()
                );
                if (predictedIndustry != null) {
                    enrichment.setIndustry(predictedIndustry);
                    enrichment.setConfidence(enrichment.getConfidence() * 0.7); // Lower confidence for AI
                }
            }
            
            return enrichment.hasData() ? enrichment : null;
            
        } catch (Exception e) {
            log.warn("Company enrichment failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Enrich contact data
     */
    private ContactEnrichment enrichContactData(CustomerContact contact, Customer customer) {
        ContactEnrichment enrichment = new ContactEnrichment();
        
        // 1. Find missing email
        if (contact.getEmail() == null && contact.getFirstName() != null && 
            contact.getLastName() != null && customer.getWebsite() != null) {
            
            String domain = extractDomain(customer.getWebsite());
            List<String> possibleEmails = generateEmailVariations(
                contact.getFirstName(),
                contact.getLastName(),
                domain
            );
            
            String verifiedEmail = emailFinderService.verifyEmails(possibleEmails);
            if (verifiedEmail != null) {
                enrichment.setEmail(verifiedEmail);
                enrichment.setEmailConfidence(0.85);
            }
        }
        
        // 2. Predict job title from context
        if (contact.getJobTitle() == null) {
            String predictedTitle = contactPredictionService.predictJobTitle(
                contact,
                customer,
                contact.getDepartment()
            );
            if (predictedTitle != null) {
                enrichment.setJobTitle(predictedTitle);
                enrichment.setTitleConfidence(0.75);
            }
        }
        
        // 3. Find phone number patterns
        if (contact.getPhone() == null && customer.getPhone() != null) {
            String predictedPhone = predictPhoneExtension(
                customer.getPhone(),
                contact.getDepartment()
            );
            if (predictedPhone != null) {
                enrichment.setPhone(predictedPhone);
                enrichment.setPhoneConfidence(0.6);
            }
        }
        
        // 4. Department inference
        if (contact.getDepartment() == null && contact.getJobTitle() != null) {
            String department = inferDepartmentFromTitle(contact.getJobTitle());
            if (department != null) {
                enrichment.setDepartment(department);
                enrichment.setDepartmentConfidence(0.8);
            }
        }
        
        // 5. Decision level prediction
        if (contact.getDecisionLevel() == null) {
            DecisionLevel level = predictDecisionLevel(
                contact.getJobTitle(),
                contact.getDepartment()
            );
            if (level != null) {
                enrichment.setDecisionLevel(level);
                enrichment.setDecisionLevelConfidence(0.7);
            }
        }
        
        return enrichment.hasData() ? enrichment : null;
    }
    
    /**
     * Generate email variations
     */
    private List<String> generateEmailVariations(String firstName, String lastName, String domain) {
        List<String> variations = new ArrayList<>();
        String fn = firstName.toLowerCase().replaceAll("[^a-z]", "");
        String ln = lastName.toLowerCase().replaceAll("[^a-z]", "");
        
        variations.add(fn + "." + ln + "@" + domain);
        variations.add(fn.charAt(0) + "." + ln + "@" + domain);
        variations.add(fn + "@" + domain);
        variations.add(ln + "@" + domain);
        variations.add(fn + ln + "@" + domain);
        variations.add(fn.charAt(0) + ln + "@" + domain);
        variations.add(fn + "_" + ln + "@" + domain);
        variations.add(fn + "-" + ln + "@" + domain);
        
        return variations;
    }
    
    /**
     * Predict missing data using ML models
     */
    private PredictionResult predictMissingData(Customer customer) {
        PredictionResult result = new PredictionResult();
        
        // Revenue prediction based on employee count and industry
        if (customer.getRevenue() == null && customer.getEmployeeCount() != null) {
            RevenuePrediction revenuePred = aiModelService.predictRevenue(
                customer.getEmployeeCount(),
                customer.getIndustry(),
                customer.getLocation()
            );
            if (revenuePred.getConfidence() > confidenceThreshold) {
                result.setPredictedRevenue(revenuePred);
            }
        }
        
        // Employee count prediction based on revenue
        if (customer.getEmployeeCount() == null && customer.getRevenue() != null) {
            EmployeeCountPrediction empPred = aiModelService.predictEmployeeCount(
                customer.getRevenue(),
                customer.getIndustry()
            );
            if (empPred.getConfidence() > confidenceThreshold) {
                result.setPredictedEmployeeCount(empPred);
            }
        }
        
        // Industry classification
        if (customer.getIndustry() == null) {
            IndustryPrediction indPred = aiModelService.classifyIndustry(
                customer.getName(),
                customer.getDescription(),
                customer.getProducts()
            );
            if (indPred.getConfidence() > confidenceThreshold) {
                result.setPredictedIndustry(indPred);
            }
        }
        
        return result.hasPredictions() ? result : null;
    }
    
    /**
     * Infer department from job title
     */
    private String inferDepartmentFromTitle(String jobTitle) {
        String title = jobTitle.toLowerCase();
        
        if (title.contains("sales") || title.contains("vertrieb") || 
            title.contains("account")) {
            return "Vertrieb";
        }
        if (title.contains("marketing") || title.contains("brand") || 
            title.contains("communication")) {
            return "Marketing";
        }
        if (title.contains("it") || title.contains("tech") || 
            title.contains("entwickl") || title.contains("software")) {
            return "IT";
        }
        if (title.contains("finance") || title.contains("accounting") || 
            title.contains("controlling")) {
            return "Finanzen";
        }
        if (title.contains("hr") || title.contains("personal") || 
            title.contains("human")) {
            return "Personal";
        }
        if (title.contains("ceo") || title.contains("geschäftsführ") || 
            title.contains("director")) {
            return "Geschäftsführung";
        }
        
        return null;
    }
    
    /**
     * Predict decision level from title
     */
    private DecisionLevel predictDecisionLevel(String jobTitle, String department) {
        if (jobTitle == null) return null;
        
        String title = jobTitle.toLowerCase();
        
        // Executive level
        if (title.contains("ceo") || title.contains("cto") || title.contains("cfo") ||
            title.contains("geschäftsführer") || title.contains("vorstand") ||
            title.contains("president")) {
            return DecisionLevel.EXECUTIVE;
        }
        
        // Manager level
        if (title.contains("director") || title.contains("leiter") ||
            title.contains("head of") || title.contains("manager") &&
            !title.contains("assistant") && !title.contains("junior")) {
            return DecisionLevel.MANAGER;
        }
        
        // Influencer level
        if (title.contains("senior") || title.contains("lead") ||
            title.contains("expert") || title.contains("specialist")) {
            return DecisionLevel.INFLUENCER;
        }
        
        // Operational level
        return DecisionLevel.OPERATIONAL;
    }
    
    /**
     * Scheduled batch enrichment
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    void runBatchEnrichment() {
        log.info("Starting batch enrichment job");
        
        // Find customers with low data quality
        List<Customer> customersToEnrich = Customer.find(
            "SELECT c FROM Customer c WHERE c.dataQualityScore < ?1 " +
            "AND c.lastEnrichedAt < ?2 OR c.lastEnrichedAt IS NULL",
            50, // Quality threshold
            LocalDateTime.now().minusDays(30) // Not enriched in last 30 days
        ).page(0, batchSize).list();
        
        for (Customer customer : customersToEnrich) {
            enrichCustomer(customer)
                .thenAccept(result -> {
                    log.info("Enriched customer {}: {}", 
                        customer.getId(), result.getStatus());
                })
                .exceptionally(e -> {
                    log.error("Batch enrichment failed for customer {}: {}", 
                        customer.getId(), e.getMessage());
                    return null;
                });
        }
    }
}
```

### 2. Frontend AI Enrichment Panel

**Datei:** `frontend/src/features/customers/components/ai/AIEnrichmentPanel.tsx`

```typescript
// CLAUDE: AI Enrichment Panel mit Live-Vorschlägen
// Pfad: frontend/src/features/customers/components/ai/AIEnrichmentPanel.tsx

import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardActions,
  Typography,
  Button,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  Chip,
  LinearProgress,
  Alert,
  IconButton,
  Collapse,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Switch,
  FormControlLabel,
  Tooltip,
  Badge,
  Divider,
  Grid,
  useTheme
} from '@mui/material';
import {
  AutoFixHigh as AIIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Business as CompanyIcon,
  Person as PersonIcon,
  TrendingUp as RevenueIcon,
  Groups as EmployeeIcon,
  Category as IndustryIcon,
  Check as CheckIcon,
  Close as CloseIcon,
  Info as InfoIcon,
  Warning as WarningIcon,
  PlayArrow as StartIcon,
  Pause as PauseIcon,
  Refresh as RefreshIcon,
  Settings as SettingsIcon,
  Speed as SpeedIcon
} from '@mui/icons-material';
import { CircularProgressbar, buildStyles } from 'react-circular-progressbar';
import 'react-circular-progressbar/dist/styles.css';

interface AIEnrichmentPanelProps {
  customerId: string;
  onEnrichmentComplete?: (result: EnrichmentResult) => void;
}

export const AIEnrichmentPanel: React.FC<AIEnrichmentPanelProps> = ({
  customerId,
  onEnrichmentComplete
}) => {
  const [enrichmentResult, setEnrichmentResult] = useState<EnrichmentResult | null>(null);
  const [suggestions, setSuggestions] = useState<DataSuggestion[]>([]);
  const [isEnriching, setIsEnriching] = useState(false);
  const [autoEnrich, setAutoEnrich] = useState(false);
  const [selectedSuggestions, setSelectedSuggestions] = useState<Set<string>>(new Set());
  const [showDetails, setShowDetails] = useState(false);
  const [confidenceThreshold, setConfidenceThreshold] = useState(0.7);
  const theme = useTheme();
  
  useEffect(() => {
    loadEnrichmentStatus();
  }, [customerId]);
  
  useEffect(() => {
    if (autoEnrich && !isEnriching) {
      startEnrichment();
    }
  }, [autoEnrich]);
  
  const loadEnrichmentStatus = async () => {
    try {
      const response = await fetch(`/api/ai/enrichment/status/${customerId}`);
      const data = await response.json();
      setEnrichmentResult(data.lastResult);
      setSuggestions(data.suggestions);
    } catch (error) {
      console.error('Failed to load enrichment status:', error);
    }
  };
  
  const startEnrichment = async () => {
    setIsEnriching(true);
    try {
      const response = await fetch(`/api/ai/enrich/${customerId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          confidenceThreshold,
          selectedFields: Array.from(selectedSuggestions)
        })
      });
      
      const result = await response.json();
      setEnrichmentResult(result);
      onEnrichmentComplete?.(result);
      
      // Reload suggestions
      await loadEnrichmentStatus();
    } finally {
      setIsEnriching(false);
    }
  };
  
  const applySuggestion = async (suggestionId: string) => {
    try {
      await fetch(`/api/ai/enrichment/apply/${suggestionId}`, {
        method: 'POST'
      });
      
      // Remove from suggestions
      setSuggestions(suggestions.filter(s => s.id !== suggestionId));
    } catch (error) {
      console.error('Failed to apply suggestion:', error);
    }
  };
  
  const rejectSuggestion = async (suggestionId: string) => {
    try {
      await fetch(`/api/ai/enrichment/reject/${suggestionId}`, {
        method: 'POST'
      });
      
      // Remove from suggestions
      setSuggestions(suggestions.filter(s => s.id !== suggestionId));
    } catch (error) {
      console.error('Failed to reject suggestion:', error);
    }
  };
  
  const getFieldIcon = (field: string) => {
    switch (field) {
      case 'email': return <EmailIcon />;
      case 'phone': return <PhoneIcon />;
      case 'company': return <CompanyIcon />;
      case 'jobTitle': return <PersonIcon />;
      case 'revenue': return <RevenueIcon />;
      case 'employeeCount': return <EmployeeIcon />;
      case 'industry': return <IndustryIcon />;
      default: return <AIIcon />;
    }
  };
  
  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 0.9) return theme.palette.success.main;
    if (confidence >= 0.7) return theme.palette.warning.main;
    return theme.palette.error.main;
  };
  
  const formatFieldName = (field: string) => {
    const names: Record<string, string> = {
      email: 'E-Mail',
      phone: 'Telefon',
      jobTitle: 'Position',
      department: 'Abteilung',
      revenue: 'Umsatz',
      employeeCount: 'Mitarbeiter',
      industry: 'Branche',
      website: 'Website',
      linkedInUrl: 'LinkedIn'
    };
    return names[field] || field;
  };
  
  // Calculate overall data quality
  const dataQuality = enrichmentResult?.qualityScore || 0;
  
  return (
    <Card>
      <CardContent>
        {/* Header */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Box display="flex" alignItems="center" gap={2}>
            <AIIcon color="primary" sx={{ fontSize: 32 }} />
            <Box>
              <Typography variant="h6">
                KI Datenanreicherung
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Automatische Vervollständigung fehlender Daten
              </Typography>
            </Box>
          </Box>
          
          <Box display="flex" alignItems="center" gap={2}>
            {/* Data Quality Score */}
            <Box sx={{ width: 60, height: 60 }}>
              <CircularProgressbar
                value={dataQuality}
                text={`${Math.round(dataQuality)}%`}
                styles={buildStyles({
                  textSize: '24px',
                  pathColor: getConfidenceColor(dataQuality / 100),
                  textColor: theme.palette.text.primary
                })}
              />
            </Box>
            
            {/* Auto-Enrich Toggle */}
            <FormControlLabel
              control={
                <Switch
                  checked={autoEnrich}
                  onChange={(e) => setAutoEnrich(e.target.checked)}
                  disabled={isEnriching}
                />
              }
              label="Auto"
            />
            
            {/* Settings */}
            <IconButton onClick={() => setShowDetails(!showDetails)}>
              <SettingsIcon />
            </IconButton>
          </Box>
        </Box>
        
        {/* Settings Panel */}
        <Collapse in={showDetails}>
          <Box p={2} bgcolor="background.default" borderRadius={1} mb={2}>
            <Typography variant="subtitle2" gutterBottom>
              Anreicherungs-Einstellungen
            </Typography>
            
            <Box display="flex" alignItems="center" gap={2} mt={2}>
              <Typography variant="body2" sx={{ minWidth: 150 }}>
                Konfidenz-Schwelle:
              </Typography>
              <Slider
                value={confidenceThreshold}
                onChange={(_, value) => setConfidenceThreshold(value as number)}
                min={0.5}
                max={1}
                step={0.05}
                marks
                valueLabelDisplay="auto"
                valueLabelFormat={(v) => `${Math.round(v * 100)}%`}
                sx={{ flex: 1 }}
              />
              <Typography variant="body2" sx={{ minWidth: 50 }}>
                {Math.round(confidenceThreshold * 100)}%
              </Typography>
            </Box>
            
            <Alert severity="info" sx={{ mt: 2 }}>
              Höhere Schwellenwerte = weniger aber genauere Vorschläge
            </Alert>
          </Box>
        </Collapse>
        
        {/* Progress Bar */}
        {isEnriching && (
          <Box mb={3}>
            <Box display="flex" justifyContent="space-between" mb={1}>
              <Typography variant="body2">Anreicherung läuft...</Typography>
              <Typography variant="body2">
                <SpeedIcon sx={{ fontSize: 16, verticalAlign: 'middle' }} />
                ~15 Sek
              </Typography>
            </Box>
            <LinearProgress />
          </Box>
        )}
        
        {/* Suggestions List */}
        {suggestions.length > 0 && (
          <Box mb={3}>
            <Typography variant="subtitle2" gutterBottom>
              {suggestions.length} Daten-Vorschläge verfügbar
            </Typography>
            
            <List>
              {suggestions.slice(0, 5).map((suggestion) => (
                <ListItem key={suggestion.id}>
                  <ListItemIcon>
                    {getFieldIcon(suggestion.field)}
                  </ListItemIcon>
                  
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="body2">
                          {formatFieldName(suggestion.field)}:
                        </Typography>
                        <Typography variant="body2" fontWeight="bold">
                          {suggestion.value}
                        </Typography>
                        <Chip
                          size="small"
                          label={`${Math.round(suggestion.confidence * 100)}%`}
                          color={
                            suggestion.confidence >= 0.9 ? 'success' :
                            suggestion.confidence >= 0.7 ? 'warning' : 'error'
                          }
                        />
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="caption">
                          Quelle: {suggestion.source}
                        </Typography>
                        {suggestion.reason && (
                          <Typography variant="caption" display="block">
                            {suggestion.reason}
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                  
                  <ListItemSecondaryAction>
                    <Tooltip title="Übernehmen">
                      <IconButton
                        edge="end"
                        color="success"
                        onClick={() => applySuggestion(suggestion.id)}
                      >
                        <CheckIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Ablehnen">
                      <IconButton
                        edge="end"
                        color="error"
                        onClick={() => rejectSuggestion(suggestion.id)}
                      >
                        <CloseIcon />
                      </IconButton>
                    </Tooltip>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
            
            {suggestions.length > 5 && (
              <Button size="small" fullWidth>
                Alle {suggestions.length} Vorschläge anzeigen
              </Button>
            )}
          </Box>
        )}
        
        {/* Recent Enrichment Results */}
        {enrichmentResult && (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Letzte Anreicherung
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Gefundene Daten
                  </Typography>
                  <Typography variant="h6">
                    {enrichmentResult.fieldsEnriched}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={6}>
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Datenqualität
                  </Typography>
                  <Typography variant="h6">
                    +{Math.round(enrichmentResult.qualityImprovement)}%
                  </Typography>
                </Box>
              </Grid>
            </Grid>
            
            {/* Insights */}
            {enrichmentResult.insights && enrichmentResult.insights.length > 0 && (
              <Box mt={2}>
                <Typography variant="subtitle2" gutterBottom>
                  KI-Erkenntnisse
                </Typography>
                {enrichmentResult.insights.map((insight, idx) => (
                  <Alert
                    key={idx}
                    severity={insight.priority === 'high' ? 'warning' : 'info'}
                    sx={{ mb: 1 }}
                  >
                    {insight.message}
                  </Alert>
                ))}
              </Box>
            )}
          </Box>
        )}
        
        {/* No suggestions */}
        {!isEnriching && suggestions.length === 0 && (
          <Alert severity="success">
            Alle Daten sind vollständig! Datenqualität: {Math.round(dataQuality)}%
          </Alert>
        )}
      </CardContent>
      
      <CardActions>
        <Button
          variant="contained"
          startIcon={isEnriching ? <PauseIcon /> : <StartIcon />}
          onClick={isEnriching ? () => setIsEnriching(false) : startEnrichment}
          disabled={suggestions.length === 0}
          fullWidth
        >
          {isEnriching ? 'Anreicherung stoppen' : 'Jetzt anreichern'}
        </Button>
      </CardActions>
    </Card>
  );
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Backend AI Services (60 Min)
- [ ] AIEnrichmentService Core
- [ ] Company Data Sources Integration
- [ ] Email Finder Service
- [ ] ML Model Integration

### Phase 2: Prediction Models (45 Min)
- [ ] Revenue Prediction Model
- [ ] Employee Count Prediction
- [ ] Industry Classification
- [ ] Decision Level Prediction

### Phase 3: Frontend Components (45 Min)
- [ ] AIEnrichmentPanel Component
- [ ] Data Suggestions UI
- [ ] Confidence Indicators
- [ ] Batch Processing UI

### Phase 4: Data Quality (30 Min)
- [ ] Quality Score Calculation
- [ ] Validation Rules
- [ ] Conflict Resolution
- [ ] Audit Trail

### Phase 5: Automation (30 Min)
- [ ] Scheduled Enrichment Jobs
- [ ] Auto-Enrichment Rules
- [ ] API Rate Limiting
- [ ] Cost Management

## 🔗 INTEGRATION POINTS

1. **Contact Management** - Felder automatisch füllen
2. **Data Quality Dashboard** - Scores anzeigen
3. **Smart Suggestions** - Basierend auf AI-Daten
4. **Communication** - Email-Finder nutzen

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Zu niedrige Konfidenz-Schwelle**
   → Lösung: Mindestens 70% für automatische Übernahme

2. **API Kosten explodieren**
   → Lösung: Daily Limits & Batching

3. **Falsche Daten übernehmen**
   → Lösung: Menschliche Validierung bei kritischen Feldern

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 210 Minuten  
**Nächstes Dokument:** [→ Performance Optimization](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/PERFORMANCE_OPTIMIZATION.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**KI + Daten = Vertriebspower! 🤖✨**