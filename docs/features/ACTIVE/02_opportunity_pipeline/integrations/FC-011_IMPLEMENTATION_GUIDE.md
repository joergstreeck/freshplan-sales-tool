# FC-011 Implementation Guide - Bonitätsprüfung & Vertragsmanagement

**Feature Code:** FC-011  
**Dokument:** Technische Implementierung  
**Stand:** 18.07.2025  

---

## 🏗️ ARCHITEKTUR ÜBERSICHT

```
Frontend (React)                    Backend (Quarkus)              External
    │                                    │                           │
    ├─ CreditCheckDialog                 ├─ CreditCheckService       ├─ Creditreform API
    ├─ ContractGenerator                 ├─ ContractService          ├─ PDF Generator
    ├─ CustomerContracts                 ├─ ContractRepository       └─ Email Service
    └─ OpportunityStageGate             └─ NotificationService
```

---

## 📦 BACKEND IMPLEMENTATION

### 1. Domain Entities

```java
// Contract.java
@Entity
@Table(name = "contracts")
public class Contract extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public ContractType type;
    
    @Column(nullable = false)
    public LocalDate startDate;
    
    @Column(nullable = false)
    public LocalDate endDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public ContractStatus status = ContractStatus.DRAFT;
    
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    public ContractTerms terms;
    
    @Column
    public String documentPath;
    
    @Column
    public boolean reminderSent = false;
    
    @Column
    public LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    public User createdBy;
}

// CreditCheck.java
@Entity
@Table(name = "credit_checks")
public class CreditCheck extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public CreditStatus status;
    
    @Column(nullable = false)
    public Integer score; // 0-100
    
    @Column(nullable = false)
    public BigDecimal creditLimit;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public RiskLevel riskLevel;
    
    @Column(columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    public Map<String, Object> details;
    
    @Column(nullable = false)
    public LocalDate validUntil;
    
    @Column(nullable = false)
    public LocalDateTime checkedAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "checked_by")
    public User checkedBy;
}
```

### 2. Service Layer

```java
// CreditCheckService.java
@ApplicationScoped
@Transactional
public class CreditCheckService {
    
    @Inject
    CreditCheckRepository creditCheckRepository;
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    ExternalCreditService externalService;
    
    @Inject
    NotificationService notificationService;
    
    @ConfigProperty(name = "credit.check.cache.days", defaultValue = "90")
    int cacheValidityDays;
    
    public CreditCheckResult performCreditCheck(UUID customerId, UUID userId) {
        Customer customer = customerRepository.findById(customerId);
        
        // Check cache first
        Optional<CreditCheck> cached = creditCheckRepository
            .findValidForCustomer(customerId, LocalDate.now());
            
        if (cached.isPresent()) {
            return mapToResult(cached.get());
        }
        
        // Perform new check
        CreditCheckResult result = externalService.checkCredit(customer);
        
        // Save to database
        CreditCheck check = new CreditCheck();
        check.customer = customer;
        check.status = result.getStatus();
        check.score = result.getScore();
        check.creditLimit = result.getCreditLimit();
        check.riskLevel = result.getRiskLevel();
        check.validUntil = LocalDate.now().plusDays(cacheValidityDays);
        check.checkedBy = User.findById(userId);
        check.details = result.getDetails();
        
        creditCheckRepository.persist(check);
        
        // Handle high risk
        if (check.riskLevel == RiskLevel.HIGH) {
            notificationService.notifyManager(
                "High risk customer: " + customer.name,
                NotificationType.CREDIT_RISK
            );
        }
        
        return result;
    }
    
    public boolean canCreateProposal(UUID customerId) {
        return creditCheckRepository
            .findValidForCustomer(customerId, LocalDate.now())
            .map(check -> check.status == CreditStatus.APPROVED)
            .orElse(false);
    }
}

// ContractService.java
@ApplicationScoped
@Transactional
public class ContractService {
    
    @Inject
    ContractRepository contractRepository;
    
    @Inject
    PDFGeneratorService pdfGenerator;
    
    @Inject
    EmailService emailService;
    
    @Inject
    Event<ContractCreatedEvent> contractCreated;
    
    public Contract createContract(ContractCreateRequest request, UUID userId) {
        Contract contract = new Contract();
        contract.customer = Customer.findById(request.getCustomerId());
        contract.type = request.getType();
        contract.startDate = request.getStartDate();
        contract.endDate = calculateEndDate(request);
        contract.terms = buildTerms(request, contract.customer);
        contract.createdBy = User.findById(userId);
        
        contractRepository.persist(contract);
        
        // Generate PDF asynchronously
        contractCreated.fireAsync(new ContractCreatedEvent(contract.id));
        
        return contract;
    }
    
    @Async
    void onContractCreated(@ObservesAsync ContractCreatedEvent event) {
        Contract contract = contractRepository.findById(event.getContractId());
        
        // Generate PDFs
        List<byte[]> documents = new ArrayList<>();
        
        // 1. Partnerschaftsvereinbarung
        documents.add(pdfGenerator.generateContract(contract));
        
        // 2. Rabattplan
        documents.add(pdfGenerator.generateDiscountPlan(contract.customer));
        
        // 3. AGBs
        documents.add(loadAGBs());
        
        // Merge PDFs
        byte[] merged = pdfGenerator.mergePDFs(documents);
        
        // Save to storage
        String path = storageService.store(merged, 
            "contracts/" + contract.id + ".pdf");
        
        contract.documentPath = path;
        contract.status = ContractStatus.READY;
    }
    
    public void sendContract(UUID contractId, EmailRequest request) {
        Contract contract = contractRepository.findById(contractId);
        
        EmailAttachment attachment = new EmailAttachment(
            "Vertrag_" + contract.customer.name + ".pdf",
            storageService.load(contract.documentPath)
        );
        
        emailService.send(
            request.getRecipient(),
            "Ihre Vertragsunterlagen",
            buildEmailBody(contract, request),
            Arrays.asList(attachment)
        );
    }
}
```

### 3. PDF Generation

```java
// PDFGeneratorService.java
@ApplicationScoped
public class PDFGeneratorService {
    
    @Inject
    TemplateEngine templateEngine;
    
    @ConfigProperty(name = "pdf.templates.path")
    String templatesPath;
    
    public byte[] generateContract(Contract contract) {
        // Load template
        String template = loadTemplate("partnership-agreement.html");
        
        // Prepare data
        Map<String, Object> data = new HashMap<>();
        data.put("customerName", contract.customer.name);
        data.put("customerNumber", contract.customer.customerNumber);
        data.put("address", formatAddress(contract.customer));
        data.put("startDate", germanDateFormat(contract.startDate));
        data.put("endDate", germanDateFormat(contract.endDate));
        data.put("creditLimit", formatCurrency(contract.terms.creditLimit));
        data.put("discountTier", contract.terms.discountTier);
        data.put("targetVolume", formatCurrency(contract.terms.targetVolume));
        
        // Special conditions
        if (contract.customer.isChainCustomer) {
            data.put("chainCustomerClause", loadClause("chain-customer"));
        }
        
        // Generate HTML
        String html = templateEngine.process(template, data);
        
        // Convert to PDF
        return htmlToPdf(html);
    }
    
    public byte[] generateDiscountPlan(Customer customer) {
        String template = loadTemplate("discount-plan.html");
        
        Map<String, Object> data = new HashMap<>();
        data.put("customerName", customer.name);
        data.put("baseDiscounts", getBaseDiscountTable());
        data.put("earlyBookingDiscounts", getEarlyBookingTable());
        data.put("pickupDiscount", "2%");
        data.put("applicableFrom", customer.revenue.compareTo(
            new BigDecimal("5000")) >= 0 ? "✓" : "✗");
        
        // Calculate examples
        data.put("examples", calculateDiscountExamples(customer));
        
        String html = templateEngine.process(template, data);
        return htmlToPdf(html);
    }
    
    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(output);
            return output.toByteArray();
        } catch (Exception e) {
            throw new PDFGenerationException("Failed to generate PDF", e);
        }
    }
}
```

### 4. Scheduled Tasks

```java
// ContractExpiryScheduler.java
@ApplicationScoped
public class ContractExpiryScheduler {
    
    @Inject
    ContractRepository contractRepository;
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    TaskService taskService;
    
    @Scheduled(cron = "0 9 * * *") // Daily at 9 AM
    void checkExpiringContracts() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        
        List<Contract> expiring = contractRepository.list(
            "status = ?1 and endDate <= ?2 and reminderSent = false",
            ContractStatus.ACTIVE,
            thirtyDaysFromNow
        );
        
        for (Contract contract : expiring) {
            // Create notification
            notificationService.notify(
                contract.customer.salesRep,
                String.format("Vertrag läuft aus: %s (noch %d Tage)",
                    contract.customer.name,
                    ChronoUnit.DAYS.between(LocalDate.now(), contract.endDate)
                ),
                NotificationType.CONTRACT_EXPIRING,
                Map.of("contractId", contract.id)
            );
            
            // Create task
            taskService.createTask(
                TaskType.CONTRACT_RENEWAL,
                "Vertrag verlängern: " + contract.customer.name,
                contract.customer.salesRep,
                contract.endDate.minusDays(7), // Due 7 days before expiry
                Map.of(
                    "customerId", contract.customer.id,
                    "contractId", contract.id
                )
            );
            
            contract.reminderSent = true;
        }
    }
}
```

---

## 🎨 FRONTEND IMPLEMENTATION

### 1. Credit Check Component

```typescript
// CreditCheckDialog.tsx
import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  CircularProgress,
  Alert,
  Box,
  Typography,
  Chip
} from '@mui/material';
import { Security, Warning, CheckCircle } from '@mui/icons-material';
import { useCreditCheck } from '../hooks/useCreditCheck';

interface CreditCheckDialogProps {
  open: boolean;
  onClose: () => void;
  customerId: string;
  customerName: string;
  onSuccess: (result: CreditCheckResult) => void;
}

export const CreditCheckDialog: React.FC<CreditCheckDialogProps> = ({
  open,
  onClose,
  customerId,
  customerName,
  onSuccess
}) => {
  const { checkCredit, isLoading } = useCreditCheck();
  const [result, setResult] = useState<CreditCheckResult | null>(null);
  
  const handleCheck = async () => {
    const checkResult = await checkCredit(customerId);
    setResult(checkResult);
    
    if (checkResult.status === 'approved') {
      setTimeout(() => onSuccess(checkResult), 1500);
    }
  };
  
  const getRiskColor = (level: string) => {
    switch (level) {
      case 'low': return 'success';
      case 'medium': return 'warning';
      case 'high': return 'error';
      default: return 'default';
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <Security />
          Bonitätsprüfung
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Box py={2}>
          <Typography variant="body1" gutterBottom>
            Kunde: <strong>{customerName}</strong>
          </Typography>
          
          {!result && !isLoading && (
            <Alert severity="info" sx={{ mt: 2 }}>
              Die Bonitätsprüfung ist erforderlich, bevor ein Angebot 
              erstellt werden kann.
            </Alert>
          )}
          
          {isLoading && (
            <Box textAlign="center" py={4}>
              <CircularProgress />
              <Typography variant="body2" sx={{ mt: 2 }}>
                Prüfung läuft...
              </Typography>
            </Box>
          )}
          
          {result && (
            <Box mt={3}>
              <Alert 
                severity={result.status === 'approved' ? 'success' : 'error'}
                icon={result.status === 'approved' ? <CheckCircle /> : <Warning />}
              >
                <Typography variant="h6">
                  {result.status === 'approved' ? 'Bonität geprüft' : 'Prüfung fehlgeschlagen'}
                </Typography>
              </Alert>
              
              <Box mt={2} display="flex" flexDirection="column" gap={2}>
                <Box display="flex" justifyContent="space-between">
                  <Typography>Score:</Typography>
                  <Typography variant="h6">{result.score}/100</Typography>
                </Box>
                
                <Box display="flex" justifyContent="space-between">
                  <Typography>Kreditlimit:</Typography>
                  <Typography variant="h6">
                    {new Intl.NumberFormat('de-DE', { 
                      style: 'currency', 
                      currency: 'EUR' 
                    }).format(result.creditLimit)}
                  </Typography>
                </Box>
                
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Typography>Risiko:</Typography>
                  <Chip 
                    label={result.riskLevel.toUpperCase()} 
                    color={getRiskColor(result.riskLevel)}
                    size="small"
                  />
                </Box>
                
                <Typography variant="caption" color="text.secondary">
                  Gültig bis: {new Date(result.validUntil).toLocaleDateString('de-DE')}
                </Typography>
              </Box>
            </Box>
          )}
        </Box>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        {!result && (
          <Button 
            onClick={handleCheck} 
            variant="contained" 
            disabled={isLoading}
            startIcon={<Security />}
          >
            Prüfung starten
          </Button>
        )}
        {result?.status === 'rejected' && (
          <Button 
            onClick={() => alert('Manuelle Prüfung angefordert')}
            color="warning"
          >
            Manuell prüfen
          </Button>
        )}
        {result?.status === 'approved' && (
          <Button 
            onClick={() => onSuccess(result)} 
            variant="contained" 
            color="success"
          >
            Weiter
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
```

### 2. Contract Generator

```typescript
// ContractGenerator.tsx
import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Select,
  MenuItem,
  TextField,
  Checkbox,
  Box,
  Typography,
  Stack
} from '@mui/material';
import { Description, Email, Print, Preview } from '@mui/icons-material';
import { useContractGenerator } from '../hooks/useContractGenerator';

interface ContractGeneratorProps {
  open: boolean;
  onClose: () => void;
  customer: Customer;
  creditCheck: CreditCheckResult;
}

export const ContractGenerator: React.FC<ContractGeneratorProps> = ({
  open,
  onClose,
  customer,
  creditCheck
}) => {
  const { generateContract, sendContract, isGenerating } = useContractGenerator();
  
  const [contractData, setContractData] = useState({
    type: 'partnership',
    duration: 12,
    startDate: new Date().toISOString().split('T')[0],
    includeAGB: true,
    includeDiscountPlan: true,
    specialConditions: ''
  });
  
  const [preview, setPreview] = useState<string | null>(null);
  
  const handleGenerate = async () => {
    const result = await generateContract({
      customerId: customer.id,
      ...contractData,
      creditLimit: creditCheck.creditLimit,
      discountTier: calculateDiscountTier(customer.revenue)
    });
    
    setPreview(result.previewUrl);
  };
  
  const handleSend = async () => {
    await sendContract({
      contractId: preview!.contractId,
      recipient: customer.email,
      cc: currentUser.email
    });
    
    toast.success('Vertrag wurde per E-Mail versendet');
    onClose();
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <Description />
          Vertrag erstellen
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Stack spacing={3} sx={{ mt: 2 }}>
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Kunde: {customer.name} (#{customer.customerNumber})
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Kreditlimit: {formatCurrency(creditCheck.creditLimit)}
            </Typography>
          </Box>
          
          <FormControl component="fieldset">
            <FormLabel component="legend">Vertragstyp</FormLabel>
            <RadioGroup
              value={contractData.type}
              onChange={(e) => setContractData({
                ...contractData,
                type: e.target.value
              })}
            >
              <FormControlLabel 
                value="partnership" 
                control={<Radio />} 
                label="Partnerschaftsvereinbarung (mit Rabatten)" 
              />
              <FormControlLabel 
                value="standard" 
                control={<Radio />} 
                label="Standardvertrag" 
              />
            </RadioGroup>
          </FormControl>
          
          <Box display="flex" gap={2}>
            <FormControl fullWidth>
              <FormLabel>Laufzeit</FormLabel>
              <Select
                value={contractData.duration}
                onChange={(e) => setContractData({
                  ...contractData,
                  duration: Number(e.target.value)
                })}
              >
                <MenuItem value={6}>6 Monate</MenuItem>
                <MenuItem value={12}>12 Monate</MenuItem>
                <MenuItem value={24}>24 Monate</MenuItem>
              </Select>
            </FormControl>
            
            <TextField
              fullWidth
              label="Startdatum"
              type="date"
              value={contractData.startDate}
              onChange={(e) => setContractData({
                ...contractData,
                startDate: e.target.value
              })}
              InputLabelProps={{ shrink: true }}
            />
          </Box>
          
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Anlagen
            </Typography>
            <FormControlLabel
              control={
                <Checkbox 
                  checked={contractData.includeAGB}
                  onChange={(e) => setContractData({
                    ...contractData,
                    includeAGB: e.target.checked
                  })}
                />
              }
              label="AGBs anhängen"
            />
            <FormControlLabel
              control={
                <Checkbox 
                  checked={contractData.includeDiscountPlan}
                  onChange={(e) => setContractData({
                    ...contractData,
                    includeDiscountPlan: e.target.checked
                  })}
                />
              }
              label="Rabattplan anhängen"
            />
          </Box>
          
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Besondere Vereinbarungen (optional)"
            value={contractData.specialConditions}
            onChange={(e) => setContractData({
              ...contractData,
              specialConditions: e.target.value
            })}
          />
          
          {customer.isChainCustomer && (
            <Alert severity="info">
              <strong>Kettenkunde erkannt!</strong> Die Kettenkundenregelung 
              wird automatisch in den Vertrag aufgenommen.
            </Alert>
          )}
        </Stack>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        <Button
          onClick={handleGenerate}
          startIcon={<Preview />}
          disabled={isGenerating}
        >
          Vorschau
        </Button>
        {preview && (
          <>
            <Button
              onClick={() => window.open(preview, '_blank')}
              startIcon={<Print />}
            >
              Drucken
            </Button>
            <Button
              onClick={handleSend}
              variant="contained"
              startIcon={<Email />}
            >
              Per E-Mail senden
            </Button>
          </>
        )}
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Customer Contracts View

```typescript
// CustomerContracts.tsx
import React from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  Box,
  Typography,
  Chip,
  Button,
  ButtonGroup,
  Alert,
  AlertTitle,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Tooltip
} from '@mui/material';
import {
  Calculate,
  Email,
  Print,
  Download,
  Refresh,
  Warning,
  CheckCircle
} from '@mui/icons-material';
import { useCustomerContracts } from '../hooks/useCustomerContracts';
import { differenceInDays, format } from 'date-fns';
import { de } from 'date-fns/locale';

interface CustomerContractsProps {
  customerId: string;
}

export const CustomerContracts: React.FC<CustomerContractsProps> = ({
  customerId
}) => {
  const { contracts, loading, refresh } = useCustomerContracts(customerId);
  
  const isExpiringSoon = (contract: Contract) => {
    const daysUntilExpiry = differenceInDays(
      new Date(contract.endDate), 
      new Date()
    );
    return daysUntilExpiry <= 30 && daysUntilExpiry > 0;
  };
  
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active': return 'success';
      case 'expired': return 'error';
      case 'draft': return 'default';
      default: return 'warning';
    }
  };
  
  if (loading) return <CircularProgress />;
  
  return (
    <Card>
      <CardHeader 
        title="Verträge & Vereinbarungen"
        action={
          <IconButton onClick={refresh}>
            <Refresh />
          </IconButton>
        }
      />
      <CardContent>
        {contracts.length === 0 ? (
          <Alert severity="info">
            Keine Verträge vorhanden
          </Alert>
        ) : (
          <List>
            {contracts.map((contract) => {
              const daysUntilExpiry = differenceInDays(
                new Date(contract.endDate),
                new Date()
              );
              
              return (
                <ListItem key={contract.id} divider>
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="subtitle1">
                          {contract.type === 'partnership' 
                            ? 'Partnerschaftsvereinbarung' 
                            : 'Standardvertrag'}
                        </Typography>
                        <Chip 
                          label={contract.status} 
                          size="small"
                          color={getStatusColor(contract.status)}
                        />
                      </Box>
                    }
                    secondary={
                      <>
                        <Typography variant="body2">
                          Gültig: {format(new Date(contract.startDate), 'dd.MM.yyyy', { locale: de })}
                          {' - '}
                          {format(new Date(contract.endDate), 'dd.MM.yyyy', { locale: de })}
                        </Typography>
                        {contract.terms.creditLimit && (
                          <Typography variant="caption" color="text.secondary">
                            Kreditlimit: {formatCurrency(contract.terms.creditLimit)}
                          </Typography>
                        )}
                      </>
                    }
                  />
                  
                  <ListItemSecondaryAction>
                    <Box display="flex" flexDirection="column" alignItems="flex-end" gap={1}>
                      {isExpiringSoon(contract) && (
                        <Alert severity="warning" sx={{ mb: 1 }}>
                          <AlertTitle sx={{ fontSize: '0.875rem' }}>
                            Läuft in {daysUntilExpiry} Tagen aus!
                          </AlertTitle>
                          <Button 
                            size="small" 
                            color="warning"
                            onClick={() => handleRenew(contract)}
                          >
                            Verlängern
                          </Button>
                        </Alert>
                      )}
                      
                      <ButtonGroup size="small">
                        <Tooltip title="Neues Angebot erstellen">
                          <Button
                            startIcon={<Calculate />}
                            onClick={() => handleCreateProposal(contract)}
                          >
                            Angebot
                          </Button>
                        </Tooltip>
                        
                        <Tooltip title="Per E-Mail senden">
                          <IconButton onClick={() => handleEmail(contract)}>
                            <Email />
                          </IconButton>
                        </Tooltip>
                        
                        <Tooltip title="Herunterladen">
                          <IconButton onClick={() => handleDownload(contract)}>
                            <Download />
                          </IconButton>
                        </Tooltip>
                        
                        <Tooltip title="Drucken">
                          <IconButton onClick={() => handlePrint(contract)}>
                            <Print />
                          </IconButton>
                        </Tooltip>
                      </ButtonGroup>
                    </Box>
                  </ListItemSecondaryAction>
                </ListItem>
              );
            })}
          </List>
        )}
      </CardContent>
    </Card>
  );
};
```

### 4. Integration in Opportunity Pipeline

```typescript
// OpportunityStageGate.tsx
import React, { useState } from 'react';
import { Alert, AlertTitle, Button, Box } from '@mui/material';
import { Security, Description } from '@mui/icons-material';
import { CreditCheckDialog } from './CreditCheckDialog';
import { ContractGenerator } from './ContractGenerator';

interface OpportunityStageGateProps {
  opportunity: Opportunity;
  onStageComplete: () => void;
}

export const OpportunityStageGate: React.FC<OpportunityStageGateProps> = ({
  opportunity,
  onStageComplete
}) => {
  const [showCreditCheck, setShowCreditCheck] = useState(false);
  const [showContractGen, setShowContractGen] = useState(false);
  const [creditCheckResult, setCreditCheckResult] = useState<CreditCheckResult | null>(null);
  
  // Stage: Qualified → Credit Check required
  if (opportunity.stage === 'qualified' && !opportunity.creditCheckDone) {
    return (
      <>
        <Alert severity="warning">
          <AlertTitle>Bonitätsprüfung erforderlich</AlertTitle>
          Bevor Sie fortfahren können, muss die Bonität des Kunden geprüft werden.
          <Box mt={1}>
            <Button
              variant="contained"
              color="warning"
              startIcon={<Security />}
              onClick={() => setShowCreditCheck(true)}
            >
              Bonitätsprüfung starten
            </Button>
          </Box>
        </Alert>
        
        <CreditCheckDialog
          open={showCreditCheck}
          onClose={() => setShowCreditCheck(false)}
          customerId={opportunity.customer.id}
          customerName={opportunity.customer.name}
          onSuccess={(result) => {
            setCreditCheckResult(result);
            setShowCreditCheck(false);
            
            // Auto-open contract generator for approved customers
            if (result.status === 'approved' && !opportunity.hasActiveContract) {
              setShowContractGen(true);
            } else {
              onStageComplete();
            }
          }}
        />
        
        {creditCheckResult && (
          <ContractGenerator
            open={showContractGen}
            onClose={() => {
              setShowContractGen(false);
              onStageComplete();
            }}
            customer={opportunity.customer}
            creditCheck={creditCheckResult}
          />
        )}
      </>
    );
  }
  
  // Stage: Proposal → Valid contract required
  if (opportunity.stage === 'proposal' && !opportunity.hasValidContract) {
    return (
      <Alert severity="info">
        <AlertTitle>Vertrag erforderlich</AlertTitle>
        Der Kunde benötigt einen gültigen Vertrag für die Angebotserstellung.
        <Box mt={1}>
          <Button
            variant="contained"
            startIcon={<Description />}
            onClick={() => setShowContractGen(true)}
          >
            Vertrag erstellen
          </Button>
        </Box>
      </Alert>
    );
  }
  
  return null;
};
```

---

## 🔗 API ENDPOINTS

```typescript
// Credit Check API
POST   /api/credit-checks
GET    /api/credit-checks/customer/{customerId}/latest
GET    /api/credit-checks/{id}

// Contract API  
POST   /api/contracts
GET    /api/contracts/customer/{customerId}
GET    /api/contracts/{id}
PATCH  /api/contracts/{id}
POST   /api/contracts/{id}/send
GET    /api/contracts/{id}/download
POST   /api/contracts/{id}/renew

// Contract Templates
GET    /api/contracts/templates
POST   /api/contracts/preview
```

---

## 📊 DATABASE SCHEMA

```sql
-- Credit Checks
CREATE TABLE credit_checks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    status VARCHAR(20) NOT NULL,
    score INTEGER NOT NULL CHECK (score >= 0 AND score <= 100),
    credit_limit DECIMAL(15,2) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    details JSONB,
    valid_until DATE NOT NULL,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checked_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Contracts
CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    terms JSONB NOT NULL,
    document_path VARCHAR(500),
    reminder_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_credit_checks_customer ON credit_checks(customer_id);
CREATE INDEX idx_credit_checks_valid ON credit_checks(valid_until);
CREATE INDEX idx_contracts_customer ON contracts(customer_id);
CREATE INDEX idx_contracts_expiry ON contracts(end_date);
CREATE INDEX idx_contracts_status ON contracts(status);
```

---

## 🧪 TESTING

```typescript
// CreditCheckService.test.ts
describe('CreditCheckService', () => {
  it('should use cached result if valid', async () => {
    const customerId = 'test-customer-id';
    const cachedCheck = createCreditCheck({
      validUntil: addDays(new Date(), 30)
    });
    
    mockRepository.findValidForCustomer.mockResolvedValue(cachedCheck);
    
    const result = await service.performCreditCheck(customerId, userId);
    
    expect(externalService.checkCredit).not.toHaveBeenCalled();
    expect(result).toEqual(mapToResult(cachedCheck));
  });
  
  it('should notify manager for high risk customers', async () => {
    const result = createCreditCheckResult({ riskLevel: 'high' });
    externalService.checkCredit.mockResolvedValue(result);
    
    await service.performCreditCheck(customerId, userId);
    
    expect(notificationService.notifyManager).toHaveBeenCalledWith(
      expect.stringContaining('High risk'),
      NotificationType.CREDIT_RISK
    );
  });
});
```

---

**Entscheidungen:** [FC-011_DECISION_LOG.md](./FC-011_DECISION_LOG.md)