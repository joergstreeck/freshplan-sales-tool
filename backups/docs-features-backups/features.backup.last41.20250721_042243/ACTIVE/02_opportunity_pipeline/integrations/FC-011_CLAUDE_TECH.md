# FC-011 Bonitätsprüfung & Vertragsmanagement - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Automatische Bonitätsprüfung + PDF-Vertragsgenerierung + Contract Management  
**Stack:** Quarkus + iText PDF + Mock Credit API + React Forms  
**Status:** 📋 Ready - Integration in M4 Pipeline  
**Dependencies:** M4 Opportunity Pipeline | Erweitert: FC-024 File Management  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [📄 PDF Patterns](#-pdf-generation-patterns)

**Core Purpose in 1 Line:** `Customer → Credit Check → Approved? → Generate PDF Contract → Track Expiry`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Credit Check Service in 5 Minuten
```java
// 1. Mock Credit Check Service (copy-paste ready)
@ApplicationScoped
@Named("mock-credit-service")
public class MockCreditCheckService implements CreditCheckService {
    
    @Override
    public CompletableFuture<CreditCheckResult> checkCredit(UUID customerId) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate API delay (1-3 seconds)
            simulateDelay(1000, 3000);
            
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
            
            // Simple scoring based on revenue
            BigDecimal score = calculateScore(customer);
            CreditStatus status = score.compareTo(BigDecimal.valueOf(50)) >= 0 
                ? CreditStatus.APPROVED 
                : CreditStatus.REJECTED;
            
            return CreditCheckResult.builder()
                .customerId(customerId)
                .status(status)
                .creditScore(score)
                .approvedCreditLimit(calculateCreditLimit(customer, score))
                .riskLevel(determineRiskLevel(score))
                .validUntil(LocalDateTime.now().plusMonths(12))
                .providerReference("MOCK-" + UUID.randomUUID().toString().substring(0, 8))
                .build();
        });
    }
    
    private BigDecimal calculateScore(Customer customer) {
        // Base score from annual revenue (0-70 points)
        BigDecimal revenueScore = customer.getAnnualRevenue()
            .divide(BigDecimal.valueOf(100000), 2, RoundingMode.HALF_UP)
            .multiply(BigDecimal.TEN)
            .min(BigDecimal.valueOf(70));
        
        // Industry bonus (0-20 points)
        BigDecimal industryBonus = getIndustryBonus(customer.getIndustry());
        
        // Random factor for realism (0-10 points)
        BigDecimal randomFactor = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(0, 11));
        
        return revenueScore.add(industryBonus).add(randomFactor);
    }
    
    private BigDecimal calculateCreditLimit(Customer customer, BigDecimal score) {
        // 20% of annual revenue * score factor
        BigDecimal baseLimit = customer.getAnnualRevenue()
            .multiply(BigDecimal.valueOf(0.2));
        
        BigDecimal scoreFactor = score.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return baseLimit.multiply(scoreFactor).setScale(0, RoundingMode.HALF_UP);
    }
}
```

### Recipe 2: PDF Contract Generator
```java
// 2. Contract PDF Generator Service (copy-paste ready)
@ApplicationScoped
public class ContractPdfGenerator {
    
    @Inject
    @Location("templates/contract-template.html")
    Template contractTemplate;
    
    public byte[] generateContract(ContractGenerationRequest request) {
        try {
            // Render HTML from Qute template
            String html = contractTemplate
                .data("customer", request.getCustomer())
                .data("opportunity", request.getOpportunity())
                .data("creditCheck", request.getCreditCheck())
                .data("contractNumber", generateContractNumber())
                .data("validUntil", LocalDate.now().plusYears(1))
                .render();
            
            // Convert HTML to PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            HtmlConverter.convertToPdf(html, outputStream, new ConverterProperties()
                .setBaseUri(getClass().getResource("/templates/").toString()));
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new ContractGenerationException("Failed to generate contract PDF", e);
        }
    }
    
    private String generateContractNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String sequence = String.format("%05d", getNextSequence());
        return String.format("VTR-%s-%s", year, sequence);
    }
}
```

### Recipe 3: Frontend Credit Check UI
```typescript
// 3. Credit Check Component (copy-paste ready)
export const CreditCheckDialog: React.FC<{
    customerId: string;
    onApproved: (result: CreditCheckResult) => void;
    onRejected: (result: CreditCheckResult) => void;
}> = ({ customerId, onApproved, onRejected }) => {
    const [checking, setChecking] = useState(false);
    const [result, setResult] = useState<CreditCheckResult | null>(null);
    
    const performCreditCheck = async () => {
        setChecking(true);
        try {
            const response = await creditCheckApi.checkCredit(customerId);
            setResult(response);
            
            if (response.status === 'APPROVED') {
                toast.success(`Bonitätsprüfung bestanden! Score: ${response.creditScore}`);
                onApproved(response);
            } else {
                toast.error(`Bonitätsprüfung fehlgeschlagen. Score: ${response.creditScore}`);
                onRejected(response);
            }
        } catch (error) {
            toast.error('Fehler bei der Bonitätsprüfung');
        } finally {
            setChecking(false);
        }
    };
    
    return (
        <Dialog open={true}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Bonitätsprüfung erforderlich</DialogTitle>
                    <DialogDescription>
                        Für den Übergang zu "Angebot" ist eine Bonitätsprüfung erforderlich.
                    </DialogDescription>
                </DialogHeader>
                
                {!result && (
                    <div className="py-6 text-center">
                        {checking ? (
                            <div className="space-y-4">
                                <Loader2 className="w-8 h-8 animate-spin mx-auto" />
                                <p>Bonitätsprüfung läuft...</p>
                            </div>
                        ) : (
                            <Button onClick={performCreditCheck}>
                                <CreditCard className="mr-2 h-4 w-4" />
                                Bonitätsprüfung starten
                            </Button>
                        )}
                    </div>
                )}
                
                {result && (
                    <CreditCheckResultDisplay result={result} />
                )}
            </DialogContent>
        </Dialog>
    );
};

// Result Display Component
const CreditCheckResultDisplay: React.FC<{ result: CreditCheckResult }> = ({ result }) => {
    const getRiskLevelColor = (level: string) => {
        const colors = {
            LOW: 'text-green-600',
            MEDIUM: 'text-yellow-600',
            HIGH: 'text-orange-600',
            CRITICAL: 'text-red-600'
        };
        return colors[level] || 'text-gray-600';
    };
    
    return (
        <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <Label>Status</Label>
                    <p className={cn(
                        "font-semibold",
                        result.status === 'APPROVED' ? 'text-green-600' : 'text-red-600'
                    )}>
                        {result.status === 'APPROVED' ? 'Genehmigt' : 'Abgelehnt'}
                    </p>
                </div>
                
                <div>
                    <Label>Kredit-Score</Label>
                    <p className="font-semibold">{result.creditScore}/100</p>
                </div>
                
                <div>
                    <Label>Risiko-Level</Label>
                    <p className={cn("font-semibold", getRiskLevelColor(result.riskLevel))}>
                        {result.riskLevel}
                    </p>
                </div>
                
                <div>
                    <Label>Kreditlimit</Label>
                    <p className="font-semibold">€{result.approvedCreditLimit.toLocaleString()}</p>
                </div>
            </div>
            
            {result.notes && result.notes.length > 0 && (
                <div>
                    <Label>Hinweise</Label>
                    <ul className="list-disc list-inside text-sm text-gray-600">
                        {result.notes.map((note, index) => (
                            <li key={index}>{note}</li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Credit Check Mock Test
```java
@QuarkusTest
class CreditCheckServiceTest {
    @Inject
    @Named("mock-credit-service")
    CreditCheckService creditCheckService;
    
    @Test
    void testApprovedCredit() {
        // Create test customer with good revenue
        Customer customer = Customer.builder()
            .id(UUID.randomUUID())
            .annualRevenue(BigDecimal.valueOf(1000000))
            .industry("Banking")
            .build();
        
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));
        
        // Perform credit check
        CreditCheckResult result = creditCheckService
            .checkCredit(customer.getId())
            .join();
        
        assertThat(result.getStatus()).isEqualTo(CreditStatus.APPROVED);
        assertThat(result.getCreditScore()).isGreaterThan(BigDecimal.valueOf(50));
        assertThat(result.getApprovedCreditLimit()).isGreaterThan(BigDecimal.ZERO);
    }
}
```

### Pattern 2: PDF Generation Test
```java
@QuarkusTest
class ContractPdfGeneratorTest {
    @Inject ContractPdfGenerator pdfGenerator;
    
    @Test
    void testContractGeneration() throws Exception {
        // Prepare test data
        ContractGenerationRequest request = ContractGenerationRequest.builder()
            .customer(createTestCustomer())
            .opportunity(createTestOpportunity())
            .creditCheck(createTestCreditResult())
            .build();
        
        // Generate PDF
        byte[] pdf = pdfGenerator.generateContract(request);
        
        // Verify PDF
        assertThat(pdf).isNotEmpty();
        
        // Load PDF and check content
        try (PDDocument document = PDDocument.load(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(2);
            
            // Extract text and verify key content
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            assertThat(text).contains("Vertrag Nr.");
            assertThat(text).contains(request.getCustomer().getName());
            assertThat(text).contains("Kreditlimit");
        }
    }
}
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit Opportunity Pipeline (M4)
```java
// Integration in OpportunityService
@ApplicationScoped
public class OpportunityService {
    @Inject CreditCheckService creditCheckService;
    @Inject ContractPdfGenerator contractGenerator;
    @Inject ContractRepository contractRepository;
    
    public OpportunityResponse moveToProposal(UUID opportunityId) {
        Opportunity opp = findById(opportunityId);
        
        // Check if credit check required
        if (!hasValidCreditCheck(opp.getCustomer())) {
            CreditCheckResult creditResult = creditCheckService
                .checkCredit(opp.getCustomer().getId())
                .join();
            
            if (creditResult.getStatus() != CreditStatus.APPROVED) {
                throw new BusinessException(
                    "Bonitätsprüfung fehlgeschlagen. Score: " + creditResult.getCreditScore()
                );
            }
            
            // Save credit check result
            saveCreditCheckResult(creditResult);
        }
        
        // Generate contract
        byte[] contractPdf = contractGenerator.generateContract(
            ContractGenerationRequest.builder()
                .customer(opp.getCustomer())
                .opportunity(opp)
                .creditCheck(getLatestCreditCheck(opp.getCustomer()))
                .build()
        );
        
        // Save contract
        Contract contract = Contract.builder()
            .opportunityId(opportunityId)
            .customerId(opp.getCustomer().getId())
            .contractNumber(generateContractNumber())
            .pdfContent(contractPdf)
            .validUntil(LocalDate.now().plusYears(1))
            .status(ContractStatus.DRAFT)
            .build();
        
        contractRepository.save(contract);
        
        // Update opportunity stage
        opp.setStage(Stage.PROPOSAL);
        opp.setContractId(contract.getId());
        
        return toResponse(opp);
    }
}
```

### Contract Management UI
```typescript
// Contract Management Component
export const ContractManager: React.FC<{ opportunityId: string }> = ({ opportunityId }) => {
    const { data: contract, isLoading } = useContract(opportunityId);
    const downloadContract = useDownloadContract();
    const sendContract = useSendContract();
    
    if (isLoading) return <Spinner />;
    if (!contract) return <NoContractMessage />;
    
    return (
        <Card>
            <CardHeader>
                <CardTitle>Vertrag {contract.contractNumber}</CardTitle>
                <Badge variant={getStatusVariant(contract.status)}>
                    {contract.status}
                </Badge>
            </CardHeader>
            <CardContent>
                <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <Label>Erstellt am</Label>
                            <p>{formatDate(contract.createdAt)}</p>
                        </div>
                        <div>
                            <Label>Gültig bis</Label>
                            <p className={cn(
                                isExpiringSoon(contract.validUntil) && "text-orange-600"
                            )}>
                                {formatDate(contract.validUntil)}
                            </p>
                        </div>
                    </div>
                    
                    <div className="flex gap-2">
                        <Button 
                            onClick={() => downloadContract.mutate(contract.id)}
                            variant="outline"
                        >
                            <Download className="mr-2 h-4 w-4" />
                            Download PDF
                        </Button>
                        
                        {contract.status === 'DRAFT' && (
                            <Button 
                                onClick={() => sendContract.mutate(contract.id)}
                                variant="default"
                            >
                                <Send className="mr-2 h-4 w-4" />
                                An Kunde senden
                            </Button>
                        )}
                    </div>
                </div>
            </CardContent>
        </Card>
    );
};
```

---

## 📄 PDF GENERATION PATTERNS

### HTML Template with Qute
```html
<!-- templates/contract-template.html -->
<!DOCTYPE html>
<html>
<head>
    <style>
        @page { size: A4; margin: 2cm; }
        body { font-family: Arial, sans-serif; line-height: 1.6; }
        .header { text-align: center; margin-bottom: 2em; }
        .contract-number { float: right; font-weight: bold; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 8px; text-align: left; }
        .footer { position: fixed; bottom: 0; width: 100%; text-align: center; }
    </style>
</head>
<body>
    <div class="header">
        <img src="logo.png" alt="FreshPlan" height="60">
        <h1>Vertrag</h1>
        <div class="contract-number">Nr. {contractNumber}</div>
    </div>
    
    <h2>Vertragsparteien</h2>
    <table>
        <tr>
            <td><strong>Auftraggeber:</strong></td>
            <td>
                {customer.name}<br>
                {customer.address}<br>
                {customer.zipCode} {customer.city}
            </td>
        </tr>
        <tr>
            <td><strong>Auftragnehmer:</strong></td>
            <td>
                FreshPlan GmbH<br>
                Musterstraße 123<br>
                12345 Berlin
            </td>
        </tr>
    </table>
    
    <h2>Bonitätsinformationen</h2>
    <table>
        <tr>
            <th>Kredit-Score</th>
            <td>{creditCheck.creditScore}/100</td>
        </tr>
        <tr>
            <th>Kreditlimit</th>
            <td>€{creditCheck.approvedCreditLimit}</td>
        </tr>
        <tr>
            <th>Risiko-Level</th>
            <td>{creditCheck.riskLevel}</td>
        </tr>
    </table>
    
    <div class="footer">
        <p>Seite <span class="pageNumber"></span> von <span class="totalPages"></span></p>
    </div>
</body>
</html>
```

### Dynamic Content Injection
```java
// Advanced PDF features
public byte[] generateContractWithAttachments(ContractRequest request) {
    PdfDocument pdfDoc = new PdfDocument(new PdfWriter(outputStream));
    Document document = new Document(pdfDoc);
    
    // Add header with logo
    Image logo = new Image(ImageDataFactory.create("logo.png"));
    logo.setWidth(100);
    document.add(logo);
    
    // Add dynamic table
    Table table = new Table(2);
    table.addCell("Kunde:");
    table.addCell(request.getCustomer().getName());
    table.addCell("Vertragswert:");
    table.addCell("€" + request.getOpportunity().getValue());
    document.add(table);
    
    // Add terms and conditions as attachment
    PdfFileSpec fileSpec = PdfFileSpec.createEmbeddedFileSpec(
        pdfDoc, 
        termsAndConditionsBytes, 
        "AGB.pdf", 
        "Allgemeine Geschäftsbedingungen", 
        null
    );
    pdfDoc.addFileAttachment("AGB", fileSpec);
    
    document.close();
    return outputStream.toByteArray();
}
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>💳 Real Credit API Integration</summary>

### Creditreform API Integration
```java
@ApplicationScoped
@Named("creditreform-service")
public class CreditreformService implements CreditCheckService {
    @ConfigProperty(name = "creditreform.api.key")
    String apiKey;
    
    @ConfigProperty(name = "creditreform.api.url")
    String apiUrl;
    
    @Inject
    @RestClient
    CreditreformClient client;
    
    @Override
    public CompletableFuture<CreditCheckResult> checkCredit(UUID customerId) {
        return CompletableFuture.supplyAsync(() -> {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow();
            
            CreditreformRequest request = CreditreformRequest.builder()
                .companyName(customer.getName())
                .taxId(customer.getTaxId())
                .address(customer.getAddress())
                .apiKey(apiKey)
                .build();
            
            CreditreformResponse response = client.checkCredit(request);
            
            return mapToCreditCheckResult(response, customerId);
        });
    }
}
```

</details>

<details>
<summary>📊 Contract Analytics</summary>

### Contract Performance Metrics
```java
@Path("/api/contracts/analytics")
public class ContractAnalyticsResource {
    
    @GET
    @Path("/expiring")
    public List<ExpiringContract> getExpiringContracts(
        @QueryParam("days") @DefaultValue("30") int days
    ) {
        LocalDate expiryThreshold = LocalDate.now().plusDays(days);
        
        return contractRepository.findExpiringBefore(expiryThreshold)
            .stream()
            .map(contract -> ExpiringContract.builder()
                .contractId(contract.getId())
                .contractNumber(contract.getContractNumber())
                .customerName(contract.getCustomer().getName())
                .expiryDate(contract.getValidUntil())
                .daysUntilExpiry(ChronoUnit.DAYS.between(LocalDate.now(), contract.getValidUntil()))
                .renewalProbability(calculateRenewalProbability(contract))
                .build()
            )
            .collect(Collectors.toList());
    }
}
```

</details>

---

**🎯 Nächster Schritt:** Mock Credit Service implementieren und in M4 Pipeline integrieren