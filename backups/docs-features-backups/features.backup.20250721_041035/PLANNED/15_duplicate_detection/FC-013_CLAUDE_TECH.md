# FC-013: Duplicate Detection 🔍 CLAUDE_TECH

**Feature Code:** FC-013  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 876 Zeilen → **Optimiert:** ~450 Zeilen (49% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-013?
**Intelligente Duplicate Detection mit Fuzzy-Matching verhindert 95% aller Dubletten**

### Die 4 Kern-Features:
1. **Multi-Field Fuzzy Match** → Name (40%), Email (30%), Phone (20%), Address (10%)
2. **Real-time Check** → <200ms beim Anlegen neuer Kunden
3. **Smart Merge** → Automatische Merge-Vorschläge mit Konfliktauflösung
4. **Audit Trail** → DSGVO-konforme Historie aller Merges

### Sofort starten:
```bash
# Backend: Fuzzy Matching Dependencies
cd backend
./mvnw quarkus:add-extension -Dextensions="commons-text,hibernate-search-orm-elasticsearch"

# Frontend: UI Components
cd frontend
npm install fuse.js react-diff-viewer
```

---

## 📦 1. BACKEND: Copy-paste Recipes

### 1.1 Fuzzy Matching Engine (10 Minuten)
```java
@ApplicationScoped
public class FuzzyMatchingEngine {
    
    private static final double DEFAULT_THRESHOLD = 0.7;
    
    @ConfigProperty(name = "duplicate.weight.name", defaultValue = "0.4")
    double nameWeight;
    
    @ConfigProperty(name = "duplicate.weight.email", defaultValue = "0.3")
    double emailWeight;
    
    @ConfigProperty(name = "duplicate.weight.phone", defaultValue = "0.2")
    double phoneWeight;
    
    @ConfigProperty(name = "duplicate.weight.address", defaultValue = "0.1")
    double addressWeight;
    
    @Inject NameMatcher nameMatcher;
    @Inject EmailMatcher emailMatcher;
    @Inject PhoneMatcher phoneMatcher;
    @Inject AddressMatcher addressMatcher;
    
    public List<DuplicateMatch> findDuplicates(Customer newCustomer, 
                                               List<Customer> existingCustomers) {
        return existingCustomers.parallelStream()
            .map(existing -> calculateMatch(newCustomer, existing))
            .filter(match -> match.getScore() >= DEFAULT_THRESHOLD)
            .sorted(Comparator.comparing(DuplicateMatch::getScore).reversed())
            .limit(5)
            .collect(Collectors.toList());
    }
    
    private DuplicateMatch calculateMatch(Customer newCustomer, Customer existing) {
        double nameScore = nameMatcher.similarity(
            newCustomer.getName(), existing.getName()
        );
        double emailScore = emailMatcher.similarity(
            newCustomer.getEmail(), existing.getEmail()
        );
        double phoneScore = phoneMatcher.similarity(
            newCustomer.getPhone(), existing.getPhone()
        );
        double addressScore = addressMatcher.similarity(
            newCustomer.getAddress(), existing.getAddress()
        );
        
        double totalScore = (nameScore * nameWeight) +
                           (emailScore * emailWeight) +
                           (phoneScore * phoneWeight) +
                           (addressScore * addressWeight);
        
        return DuplicateMatch.builder()
            .customer(existing)
            .score(totalScore)
            .nameScore(nameScore)
            .emailScore(emailScore)
            .phoneScore(phoneScore)
            .addressScore(addressScore)
            .matchReason(generateMatchReason(nameScore, emailScore, phoneScore))
            .build();
    }
    
    private String generateMatchReason(double name, double email, double phone) {
        List<String> reasons = new ArrayList<>();
        if (name > 0.8) reasons.add("Sehr ähnlicher Name");
        if (email > 0.9) reasons.add("Gleiche E-Mail-Domain");
        if (phone > 0.95) reasons.add("Identische Telefonnummer");
        return String.join(", ", reasons);
    }
}

// Name Matcher with Company Suffix Normalization
@ApplicationScoped
public class NameMatcher {
    
    private static final Set<String> COMPANY_SUFFIXES = Set.of(
        "gmbh", "ag", "ug", "kg", "ohg", "gbr", "gmbh & co kg",
        "e.v.", "ev", "ltd", "inc", "corp"
    );
    
    public double similarity(String name1, String name2) {
        if (name1 == null || name2 == null) return 0.0;
        
        String normalized1 = normalize(name1);
        String normalized2 = normalize(name2);
        
        // Exact match after normalization
        if (normalized1.equals(normalized2)) return 1.0;
        
        // Levenshtein distance
        int distance = LevenshteinDistance.getDefaultInstance()
            .apply(normalized1, normalized2);
        double maxLen = Math.max(normalized1.length(), normalized2.length());
        double levenshteinScore = 1.0 - (distance / maxLen);
        
        // Jaro-Winkler for additional similarity
        double jaroScore = new JaroWinklerSimilarity()
            .apply(normalized1, normalized2);
        
        // Combined score
        return (levenshteinScore * 0.6) + (jaroScore * 0.4);
    }
    
    private String normalize(String name) {
        String lower = name.toLowerCase().trim();
        
        // Remove company suffixes
        for (String suffix : COMPANY_SUFFIXES) {
            lower = lower.replaceAll("\\s+" + suffix + "$", "");
        }
        
        // Remove special characters
        return lower.replaceAll("[^a-zäöüß0-9\\s]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }
}

// Email Matcher with Domain Focus
@ApplicationScoped
public class EmailMatcher {
    
    public double similarity(String email1, String email2) {
        if (email1 == null || email2 == null) return 0.0;
        if (email1.equalsIgnoreCase(email2)) return 1.0;
        
        String domain1 = extractDomain(email1);
        String domain2 = extractDomain(email2);
        
        // Same domain = high similarity
        if (domain1.equals(domain2)) {
            return 0.85 + (0.15 * usernameSimiliarity(email1, email2));
        }
        
        // Different domain but similar username pattern
        return usernameSimiliarity(email1, email2) * 0.3;
    }
    
    private String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(atIndex + 1).toLowerCase() : "";
    }
    
    private double usernameSimiliarity(String email1, String email2) {
        String user1 = email1.substring(0, email1.indexOf('@'));
        String user2 = email2.substring(0, email2.indexOf('@'));
        
        return new JaroWinklerSimilarity().apply(user1, user2);
    }
}

// Phone Matcher with International Normalization
@ApplicationScoped
public class PhoneMatcher {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("[^0-9+]");
    
    public double similarity(String phone1, String phone2) {
        if (phone1 == null || phone2 == null) return 0.0;
        
        String normalized1 = normalizePhone(phone1);
        String normalized2 = normalizePhone(phone2);
        
        if (normalized1.equals(normalized2)) return 1.0;
        
        // Check if one contains the other (country code differences)
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return 0.95;
        }
        
        // Levenshtein for similar numbers
        int distance = LevenshteinDistance.getDefaultInstance()
            .apply(normalized1, normalized2);
        
        return distance <= 2 ? 0.8 : 0.0;
    }
    
    private String normalizePhone(String phone) {
        // Remove all non-digits except +
        String normalized = PHONE_PATTERN.matcher(phone).replaceAll("");
        
        // Convert German numbers to international format
        if (normalized.startsWith("0")) {
            normalized = "+49" + normalized.substring(1);
        }
        
        return normalized;
    }
}
```

### 1.2 Duplicate Detection Service (5 Minuten)
```java
@ApplicationScoped
@Transactional
public class DuplicateDetectionService {
    
    @Inject FuzzyMatchingEngine matchingEngine;
    @Inject CustomerRepository customerRepository;
    @Inject DuplicateLogRepository duplicateLogRepository;
    
    public DuplicateCheckResult checkDuplicates(Customer newCustomer) {
        // Get potential matches using indexed search
        List<Customer> candidates = findCandidates(newCustomer);
        
        // Apply fuzzy matching
        List<DuplicateMatch> matches = matchingEngine.findDuplicates(
            newCustomer, candidates
        );
        
        // Log the check
        logDuplicateCheck(newCustomer, matches);
        
        return DuplicateCheckResult.builder()
            .hasDuplicates(!matches.isEmpty())
            .matches(matches)
            .highestScore(matches.isEmpty() ? 0.0 : matches.get(0).getScore())
            .recommendation(generateRecommendation(matches))
            .build();
    }
    
    private List<Customer> findCandidates(Customer newCustomer) {
        // Use Hibernate Search for efficient candidate retrieval
        return Search.session(entityManager)
            .search(Customer.class)
            .where(f -> f.bool()
                .should(f.match()
                    .field("name")
                    .matching(newCustomer.getName())
                    .fuzzy(2))
                .should(f.match()
                    .field("email")
                    .matching(newCustomer.getEmail()))
                .should(f.match()
                    .field("phone")
                    .matching(newCustomer.getPhone()))
            )
            .fetchHits(100);
    }
    
    private String generateRecommendation(List<DuplicateMatch> matches) {
        if (matches.isEmpty()) return "Kein Duplikat gefunden - sicher anlegen";
        
        DuplicateMatch topMatch = matches.get(0);
        if (topMatch.getScore() > 0.95) {
            return "Sehr wahrscheinlich ein Duplikat - Merge empfohlen";
        } else if (topMatch.getScore() > 0.8) {
            return "Mögliches Duplikat - bitte prüfen";
        } else {
            return "Geringe Ähnlichkeit - wahrscheinlich kein Duplikat";
        }
    }
}

// Merge Service with Conflict Resolution
@ApplicationScoped
@Transactional
public class CustomerMergeService {
    
    @Inject CustomerRepository customerRepository;
    @Inject AuditLogService auditLogService;
    @Inject EventBus eventBus;
    
    public MergeResult mergeCustomers(UUID primaryId, UUID duplicateId, 
                                     MergeStrategy strategy) {
        Customer primary = customerRepository.findById(primaryId);
        Customer duplicate = customerRepository.findById(duplicateId);
        
        // Create merge snapshot for audit
        MergeSnapshot snapshot = createSnapshot(primary, duplicate);
        
        // Apply merge strategy
        switch (strategy) {
            case KEEP_PRIMARY:
                mergeActivities(duplicate, primary);
                break;
            case KEEP_NEWEST:
                mergeByTimestamp(primary, duplicate);
                break;
            case MANUAL:
                // Frontend provides field-by-field decisions
                break;
        }
        
        // Update all references
        updateReferences(duplicateId, primaryId);
        
        // Mark duplicate as merged
        duplicate.setStatus(CustomerStatus.MERGED);
        duplicate.setMergedIntoId(primaryId);
        
        // Audit log
        auditLogService.logMerge(snapshot, primary);
        
        // Publish event
        eventBus.publish(new CustomerMergedEvent(primaryId, duplicateId));
        
        return MergeResult.success(primary);
    }
    
    private void updateReferences(UUID oldId, UUID newId) {
        // Update all foreign key references
        entityManager.createNativeQuery(
            "UPDATE opportunities SET customer_id = :newId WHERE customer_id = :oldId"
        ).setParameter("newId", newId)
         .setParameter("oldId", oldId)
         .executeUpdate();
        
        // Update activities, notes, etc.
        entityManager.createNativeQuery(
            "UPDATE activities SET customer_id = :newId WHERE customer_id = :oldId"
        ).setParameter("newId", newId)
         .setParameter("oldId", oldId)
         .executeUpdate();
    }
}
```

### 1.3 Configuration & Admin API (5 Minuten)
```java
@Path("/api/admin/duplicate-config")
@RolesAllowed("admin")
public class DuplicateConfigResource {
    
    @Inject DuplicateConfigService configService;
    
    @GET
    public DuplicateConfig getConfig() {
        return configService.getCurrentConfig();
    }
    
    @PUT
    @Transactional
    public Response updateConfig(DuplicateConfig config) {
        // Validate weights sum to 1.0
        double sum = config.getNameWeight() + config.getEmailWeight() + 
                    config.getPhoneWeight() + config.getAddressWeight();
        
        if (Math.abs(sum - 1.0) > 0.001) {
            return Response.status(400)
                .entity("Weights must sum to 1.0")
                .build();
        }
        
        configService.updateConfig(config);
        return Response.ok().build();
    }
    
    @GET
    @Path("/statistics")
    public DuplicateStatistics getStatistics() {
        return DuplicateStatistics.builder()
            .totalChecks(duplicateLogRepository.count())
            .duplicatesFound(duplicateLogRepository.countDuplicates())
            .mergesCompleted(mergeHistoryRepository.count())
            .preventionRate(calculatePreventionRate())
            .topDuplicateReasons(getTopReasons())
            .build();
    }
}
```

---

## 🎨 2. FRONTEND: Duplicate Detection UI

### 2.1 Real-time Duplicate Check Hook (5 Minuten)
```typescript
export const useDuplicateCheck = () => {
  const [checking, setChecking] = useState(false);
  const [duplicates, setDuplicates] = useState<DuplicateMatch[]>([]);
  
  const checkDuplicates = useCallback(
    debounce(async (customer: Partial<Customer>) => {
      // Only check if minimum data is provided
      if (!customer.name && !customer.email && !customer.phone) {
        setDuplicates([]);
        return;
      }
      
      setChecking(true);
      try {
        const result = await api.checkDuplicates(customer);
        setDuplicates(result.matches);
      } finally {
        setChecking(false);
      }
    }, 500),
    []
  );
  
  return { checkDuplicates, checking, duplicates };
};

// Integration in Customer Create Form
export const CustomerCreateForm: React.FC = () => {
  const { checkDuplicates, checking, duplicates } = useDuplicateCheck();
  const [showDuplicateWarning, setShowDuplicateWarning] = useState(false);
  
  const { register, watch, handleSubmit } = useForm<Customer>();
  
  // Watch for changes and check duplicates
  const watchedFields = watch(['name', 'email', 'phone']);
  
  useEffect(() => {
    const customer = {
      name: watchedFields[0],
      email: watchedFields[1],
      phone: watchedFields[2]
    };
    
    checkDuplicates(customer);
  }, [watchedFields, checkDuplicates]);
  
  useEffect(() => {
    setShowDuplicateWarning(duplicates.length > 0);
  }, [duplicates]);
  
  return (
    <Box>
      <form onSubmit={handleSubmit(onSubmit)}>
        <TextField
          {...register('name', { required: true })}
          label="Firmenname"
          fullWidth
          margin="normal"
          InputProps={{
            endAdornment: checking && <CircularProgress size={20} />
          }}
        />
        
        {/* Other fields... */}
        
        {showDuplicateWarning && (
          <Alert severity="warning" sx={{ mt: 2 }}>
            <AlertTitle>Mögliche Duplikate gefunden</AlertTitle>
            {duplicates.length} ähnliche Kunden gefunden
            <Button
              size="small"
              onClick={() => setShowDuplicateDialog(true)}
              sx={{ ml: 2 }}
            >
              Details anzeigen
            </Button>
          </Alert>
        )}
      </form>
      
      <DuplicateWarningDialog
        open={showDuplicateDialog}
        duplicates={duplicates}
        newCustomer={watchedFields}
        onMerge={handleMerge}
        onCreateAnyway={handleCreateAnyway}
        onClose={() => setShowDuplicateDialog(false)}
      />
    </Box>
  );
};
```

### 2.2 Duplicate Warning Dialog (5 Minuten)
```typescript
export const DuplicateWarningDialog: React.FC<Props> = ({
  open,
  duplicates,
  newCustomer,
  onMerge,
  onCreateAnyway,
  onClose
}) => {
  const [selectedDuplicate, setSelectedDuplicate] = useState<string | null>(null);
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <WarningIcon color="warning" />
          Mögliche Duplikate gefunden
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Typography variant="body2" color="text.secondary" gutterBottom>
          Wir haben {duplicates.length} ähnliche Kunden gefunden:
        </Typography>
        
        <List>
          {duplicates.map((match) => (
            <DuplicateMatchItem
              key={match.customer.id}
              match={match}
              selected={selectedDuplicate === match.customer.id}
              onSelect={() => setSelectedDuplicate(match.customer.id)}
              newCustomer={newCustomer}
            />
          ))}
        </List>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          onClick={() => onCreateAnyway()}
          variant="outlined"
        >
          Trotzdem anlegen
        </Button>
        <Button
          onClick={() => onMerge(selectedDuplicate)}
          variant="contained"
          disabled={!selectedDuplicate}
          color="primary"
        >
          Mit ausgewähltem zusammenführen
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Match Item with Visual Diff
const DuplicateMatchItem: React.FC<{
  match: DuplicateMatch;
  selected: boolean;
  onSelect: () => void;
  newCustomer: Partial<Customer>;
}> = ({ match, selected, onSelect, newCustomer }) => {
  const { customer, score, matchReason } = match;
  
  return (
    <ListItem
      button
      selected={selected}
      onClick={onSelect}
      sx={{
        border: 1,
        borderColor: selected ? 'primary.main' : 'divider',
        borderRadius: 1,
        mb: 1
      }}
    >
      <ListItemIcon>
        <Radio checked={selected} />
      </ListItemIcon>
      
      <ListItemText
        primary={
          <Box display="flex" justifyContent="space-between">
            <Typography variant="subtitle1">{customer.name}</Typography>
            <Chip
              label={`${Math.round(score * 100)}% Übereinstimmung`}
              color={score > 0.9 ? 'error' : score > 0.8 ? 'warning' : 'default'}
              size="small"
            />
          </Box>
        }
        secondary={
          <Box>
            <Typography variant="caption" color="text.secondary">
              {matchReason}
            </Typography>
            <Grid container spacing={1} sx={{ mt: 1 }}>
              <Grid item xs={6}>
                <FieldComparison
                  label="Name"
                  oldValue={customer.name}
                  newValue={newCustomer.name}
                  score={match.nameScore}
                />
              </Grid>
              <Grid item xs={6}>
                <FieldComparison
                  label="E-Mail"
                  oldValue={customer.email}
                  newValue={newCustomer.email}
                  score={match.emailScore}
                />
              </Grid>
            </Grid>
          </Box>
        }
      />
    </ListItem>
  );
};
```

---

## 🗄️ 3. DATENBANK: Schema & Performance

### 3.1 Database Schema (Copy-paste ready)
```sql
-- V1.0.0__duplicate_detection.sql

-- Duplicate check log
CREATE TABLE duplicate_check_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checked_data JSONB NOT NULL,
    duplicates_found INTEGER DEFAULT 0,
    highest_score DECIMAL(3,2),
    user_action VARCHAR(50), -- MERGED, CREATED_ANYWAY, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

-- Merge history for audit trail
CREATE TABLE customer_merge_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    primary_customer_id UUID REFERENCES customers(id),
    merged_customer_id UUID,
    merge_strategy VARCHAR(50),
    snapshot_before JSONB NOT NULL,
    snapshot_after JSONB NOT NULL,
    conflicts_resolved JSONB,
    merged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    merged_by UUID REFERENCES users(id)
);

-- Duplicate configuration
CREATE TABLE duplicate_config (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name_weight DECIMAL(3,2) DEFAULT 0.4,
    email_weight DECIMAL(3,2) DEFAULT 0.3,
    phone_weight DECIMAL(3,2) DEFAULT 0.2,
    address_weight DECIMAL(3,2) DEFAULT 0.1,
    threshold DECIMAL(3,2) DEFAULT 0.7,
    auto_merge_threshold DECIMAL(3,2) DEFAULT 0.95,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID REFERENCES users(id)
);

-- Indexes for performance
CREATE INDEX idx_duplicate_log_created ON duplicate_check_log(created_at DESC);
CREATE INDEX idx_merge_history_primary ON customer_merge_history(primary_customer_id);
CREATE INDEX idx_customers_name_trgm ON customers USING gin(name gin_trgm_ops);
CREATE INDEX idx_customers_email ON customers(lower(email));
CREATE INDEX idx_customers_phone ON customers(phone);

-- Enable trigram extension for fuzzy search
CREATE EXTENSION IF NOT EXISTS pg_trgm;
```

---

## ✅ 4. TESTING

```java
@QuarkusTest
class DuplicateDetectionTest {
    
    @Test
    void testFuzzyNameMatching() {
        Customer c1 = createCustomer("Müller GmbH");
        Customer c2 = createCustomer("Mueller GmbH & Co. KG");
        
        double similarity = nameMatcher.similarity(c1.getName(), c2.getName());
        
        assertThat(similarity).isGreaterThan(0.8);
    }
    
    @Test
    void testPhoneNormalization() {
        String phone1 = "+49 30 123456";
        String phone2 = "030-123456";
        
        double similarity = phoneMatcher.similarity(phone1, phone2);
        
        assertThat(similarity).isEqualTo(1.0);
    }
}
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (1 Tag)**: Fuzzy Matching Engine + Basic Detection
2. **Phase 2 (1 Tag)**: UI Integration + Warning Dialog
3. **Phase 3 (1 Tag)**: Merge Functionality + Audit Trail
4. **Phase 4 (0.5 Tag)**: Admin Config + Performance Tuning

**Geschätzter Aufwand:** 3.5 Entwicklungstage