# 🔍 Duplicate Detection - Intelligente Duplikat-Erkennung

**Phase:** 1 - Core Functionality  
**Priorität:** 🟡 WICHTIG - Datenqualität sichern  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/IMPORT_EXPORT.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COMMUNICATION_HISTORY.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Datenqualität
- Import-Prozesse
- CRM-Hygiene

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Duplicate Detection implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Duplicate Services
mkdir -p backend/src/main/java/de/freshplan/duplicate
touch backend/src/main/java/de/freshplan/duplicate/DuplicateDetectionService.java
touch backend/src/main/java/de/freshplan/duplicate/FuzzyMatcher.java
touch backend/src/main/java/de/freshplan/duplicate/MergeService.java
touch backend/src/main/java/de/freshplan/duplicate/SimilarityCalculator.java

# 2. Frontend Duplicate Components
mkdir -p frontend/src/features/customers/components/duplicate
touch frontend/src/features/customers/components/duplicate/DuplicateFinderDialog.tsx
touch frontend/src/features/customers/components/duplicate/MergeWizard.tsx
touch frontend/src/features/customers/components/duplicate/DuplicateComparison.tsx
touch frontend/src/features/customers/components/duplicate/SimilarityIndicator.tsx

# 3. Duplicate Algorithms
mkdir -p frontend/src/features/customers/services/duplicate
touch frontend/src/features/customers/services/duplicate/LevenshteinDistance.ts
touch frontend/src/features/customers/services/duplicate/PhoneticMatcher.ts

# 4. Tests
mkdir -p backend/src/test/java/de/freshplan/duplicate
touch backend/src/test/java/de/freshplan/duplicate/DuplicateDetectionTest.java
```

## 🎯 Das Problem: Duplikate zerstören Datenqualität

**Typische Duplikat-Quellen:**
- 👥 "Hans Müller" vs "H. Mueller" vs "Hans Müller GmbH"
- 📧 "hans@firma.de" vs "h.mueller@firma.de"
- 📞 "+49 89 12345" vs "089/123-45" vs "089 12345"
- 🏢 Gleiche Person in verschiedenen Abteilungen
- 🔄 Import ohne Duplikat-Check

## 💡 DIE LÖSUNG: Intelligente Fuzzy-Matching

### 1. Duplicate Detection Service

**Datei:** `backend/src/main/java/de/freshplan/duplicate/DuplicateDetectionService.java`

```java
// CLAUDE: Intelligente Duplikat-Erkennung mit Fuzzy Logic
// Pfad: backend/src/main/java/de/freshplan/duplicate/DuplicateDetectionService.java

package de.freshplan.duplicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.*;

@ApplicationScoped
public class DuplicateDetectionService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    SimilarityCalculator similarityCalculator;
    
    @Inject
    PhoneticEncoder phoneticEncoder;
    
    // Threshold configurations
    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.85;
    private static final double MEDIUM_CONFIDENCE_THRESHOLD = 0.70;
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.55;
    
    /**
     * Find all duplicate groups in the database
     */
    public List<DuplicateGroup> findAllDuplicates() {
        List<Contact> allContacts = contactRepository.findAll();
        Map<String, DuplicateGroup> groups = new HashMap<>();
        Set<UUID> processed = new HashSet<>();
        
        for (int i = 0; i < allContacts.size(); i++) {
            Contact contact1 = allContacts.get(i);
            
            if (processed.contains(contact1.getId())) {
                continue;
            }
            
            List<DuplicateMatch> matches = new ArrayList<>();
            
            for (int j = i + 1; j < allContacts.size(); j++) {
                Contact contact2 = allContacts.get(j);
                
                if (processed.contains(contact2.getId())) {
                    continue;
                }
                
                DuplicateMatch match = compareContacts(contact1, contact2);
                
                if (match.getSimilarityScore() >= LOW_CONFIDENCE_THRESHOLD) {
                    matches.add(match);
                    processed.add(contact2.getId());
                }
            }
            
            if (!matches.isEmpty()) {
                processed.add(contact1.getId());
                
                DuplicateGroup group = new DuplicateGroup();
                group.setMaster(contact1);
                group.setDuplicates(matches);
                group.setConfidence(calculateGroupConfidence(matches));
                group.setSuggestedAction(suggestAction(group));
                
                groups.put(generateGroupKey(contact1), group);
            }
        }
        
        return new ArrayList<>(groups.values());
    }
    
    /**
     * Find duplicates for a specific contact
     */
    public List<DuplicateMatch> findDuplicatesForContact(Contact contact) {
        List<Contact> candidates = findCandidates(contact);
        List<DuplicateMatch> matches = new ArrayList<>();
        
        for (Contact candidate : candidates) {
            if (candidate.getId().equals(contact.getId())) {
                continue;
            }
            
            DuplicateMatch match = compareContacts(contact, candidate);
            
            if (match.getSimilarityScore() >= LOW_CONFIDENCE_THRESHOLD) {
                matches.add(match);
            }
        }
        
        // Sort by similarity score
        matches.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        
        return matches;
    }
    
    /**
     * Compare two contacts and calculate similarity
     */
    private DuplicateMatch compareContacts(Contact c1, Contact c2) {
        DuplicateMatch match = new DuplicateMatch();
        match.setContact1(c1);
        match.setContact2(c2);
        
        // Calculate individual field similarities
        double nameSimilarity = calculateNameSimilarity(c1, c2);
        double emailSimilarity = calculateEmailSimilarity(c1, c2);
        double phoneSimilarity = calculatePhoneSimilarity(c1, c2);
        double companySimilarity = calculateCompanySimilarity(c1, c2);
        double addressSimilarity = calculateAddressSimilarity(c1, c2);
        
        // Weighted average
        double totalScore = 0;
        double totalWeight = 0;
        
        if (nameSimilarity > 0) {
            totalScore += nameSimilarity * 0.35; // 35% weight for name
            totalWeight += 0.35;
        }
        
        if (emailSimilarity > 0) {
            totalScore += emailSimilarity * 0.25; // 25% weight for email
            totalWeight += 0.25;
        }
        
        if (phoneSimilarity > 0) {
            totalScore += phoneSimilarity * 0.20; // 20% weight for phone
            totalWeight += 0.20;
        }
        
        if (companySimilarity > 0) {
            totalScore += companySimilarity * 0.15; // 15% weight for company
            totalWeight += 0.15;
        }
        
        if (addressSimilarity > 0) {
            totalScore += addressSimilarity * 0.05; // 5% weight for address
            totalWeight += 0.05;
        }
        
        double finalScore = totalWeight > 0 ? totalScore / totalWeight : 0;
        match.setSimilarityScore(finalScore);
        
        // Set confidence level
        if (finalScore >= HIGH_CONFIDENCE_THRESHOLD) {
            match.setConfidence(ConfidenceLevel.HIGH);
        } else if (finalScore >= MEDIUM_CONFIDENCE_THRESHOLD) {
            match.setConfidence(ConfidenceLevel.MEDIUM);
        } else {
            match.setConfidence(ConfidenceLevel.LOW);
        }
        
        // Identify matching fields
        List<String> matchingFields = new ArrayList<>();
        if (nameSimilarity > 0.8) matchingFields.add("name");
        if (emailSimilarity > 0.9) matchingFields.add("email");
        if (phoneSimilarity > 0.9) matchingFields.add("phone");
        if (companySimilarity > 0.8) matchingFields.add("company");
        
        match.setMatchingFields(matchingFields);
        
        // Add detailed comparison
        match.setFieldComparisons(Map.of(
            "name", nameSimilarity,
            "email", emailSimilarity,
            "phone", phoneSimilarity,
            "company", companySimilarity,
            "address", addressSimilarity
        ));
        
        return match;
    }
    
    /**
     * Calculate name similarity using multiple algorithms
     */
    private double calculateNameSimilarity(Contact c1, Contact c2) {
        String name1 = normalizeNam (c1.getFirstName() + " " + c1.getLastName());
        String name2 = normalizeName(c2.getFirstName() + " " + c2.getLastName());
        
        // Try exact match first
        if (name1.equals(name2)) {
            return 1.0;
        }
        
        // Levenshtein distance
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(name1, name2);
        double levenshteinScore = 1.0 - (double) distance / Math.max(name1.length(), name2.length());
        
        // Jaro-Winkler similarity
        JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
        double jwScore = jaroWinkler.apply(name1, name2);
        
        // Phonetic matching (Soundex/Metaphone)
        String phonetic1 = phoneticEncoder.encode(name1);
        String phonetic2 = phoneticEncoder.encode(name2);
        double phoneticScore = phonetic1.equals(phonetic2) ? 1.0 : 0.0;
        
        // Check for initials
        double initialScore = checkInitials(c1, c2);
        
        // Weighted combination
        return (levenshteinScore * 0.3 + jwScore * 0.3 + phoneticScore * 0.2 + initialScore * 0.2);
    }
    
    /**
     * Calculate email similarity
     */
    private double calculateEmailSimilarity(Contact c1, Contact c2) {
        if (c1.getEmail() == null || c2.getEmail() == null) {
            return 0;
        }
        
        String email1 = c1.getEmail().toLowerCase().trim();
        String email2 = c2.getEmail().toLowerCase().trim();
        
        // Exact match
        if (email1.equals(email2)) {
            return 1.0;
        }
        
        // Split into local and domain parts
        String[] parts1 = email1.split("@");
        String[] parts2 = email2.split("@");
        
        if (parts1.length != 2 || parts2.length != 2) {
            return 0;
        }
        
        // Same domain?
        if (!parts1[1].equals(parts2[1])) {
            return 0;
        }
        
        // Similar local part?
        String local1 = parts1[0];
        String local2 = parts2[0];
        
        // Remove common variations
        local1 = local1.replaceAll("[._-]", "");
        local2 = local2.replaceAll("[._-]", "");
        
        if (local1.equals(local2)) {
            return 0.9; // High similarity
        }
        
        // Check if one is abbreviated
        if (local1.startsWith(local2) || local2.startsWith(local1)) {
            return 0.7;
        }
        
        // Levenshtein for local part
        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(local1, local2);
        
        if (distance <= 2) {
            return 0.6;
        }
        
        return 0;
    }
    
    /**
     * Calculate phone similarity with normalization
     */
    private double calculatePhoneSimilarity(Contact c1, Contact c2) {
        List<String> phones1 = getPhoneNumbers(c1);
        List<String> phones2 = getPhoneNumbers(c2);
        
        if (phones1.isEmpty() || phones2.isEmpty()) {
            return 0;
        }
        
        double maxSimilarity = 0;
        
        for (String phone1 : phones1) {
            String normalized1 = normalizePhone(phone1);
            
            for (String phone2 : phones2) {
                String normalized2 = normalizePhone(phone2);
                
                if (normalized1.equals(normalized2)) {
                    return 1.0;
                }
                
                // Check if one contains the other (extension)
                if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
                    maxSimilarity = Math.max(maxSimilarity, 0.8);
                }
                
                // Last 7 digits match (common for mobile)
                if (normalized1.length() >= 7 && normalized2.length() >= 7) {
                    String last1 = normalized1.substring(normalized1.length() - 7);
                    String last2 = normalized2.substring(normalized2.length() - 7);
                    
                    if (last1.equals(last2)) {
                        maxSimilarity = Math.max(maxSimilarity, 0.7);
                    }
                }
            }
        }
        
        return maxSimilarity;
    }
    
    /**
     * Find candidates using blocking/indexing for performance
     */
    private List<Contact> findCandidates(Contact contact) {
        Set<Contact> candidates = new HashSet<>();
        
        // Block by email domain
        if (contact.getEmail() != null) {
            String domain = contact.getEmail().split("@")[1];
            candidates.addAll(contactRepository.findByEmailDomain(domain));
        }
        
        // Block by phone area code
        if (contact.getPhone() != null) {
            String areaCode = extractAreaCode(contact.getPhone());
            if (areaCode != null) {
                candidates.addAll(contactRepository.findByPhoneAreaCode(areaCode));
            }
        }
        
        // Block by company
        if (contact.getCompany() != null) {
            candidates.addAll(contactRepository.findByCompanySimilar(contact.getCompany()));
        }
        
        // Block by phonetic name
        String phoneticName = phoneticEncoder.encode(
            contact.getFirstName() + " " + contact.getLastName()
        );
        candidates.addAll(contactRepository.findByPhoneticName(phoneticName));
        
        return new ArrayList<>(candidates);
    }
    
    /**
     * Normalize name for comparison
     */
    private String normalizeName(String name) {
        if (name == null) return "";
        
        return name
            .toLowerCase()
            .trim()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zäöüß\\s]", "")
            .replaceAll("ä", "ae")
            .replaceAll("ö", "oe")
            .replaceAll("ü", "ue")
            .replaceAll("ß", "ss");
    }
    
    /**
     * Normalize phone for comparison
     */
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        
        return phone
            .replaceAll("[^0-9+]", "")
            .replaceAll("^00", "+")
            .replaceAll("^0", "+49")
            .replaceAll("^\\+49", "")
            .replaceAll("^49", "");
    }
    
    private List<String> getPhoneNumbers(Contact contact) {
        List<String> phones = new ArrayList<>();
        if (contact.getPhone() != null) phones.add(contact.getPhone());
        if (contact.getMobile() != null) phones.add(contact.getMobile());
        return phones;
    }
}
```

### 2. Merge Wizard UI

**Datei:** `frontend/src/features/customers/components/duplicate/MergeWizard.tsx`

```typescript
// CLAUDE: Interaktiver Merge-Wizard für Duplikate
// Pfad: frontend/src/features/customers/components/duplicate/MergeWizard.tsx

import React, { useState, useMemo } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stepper,
  Step,
  StepLabel,
  Box,
  Button,
  Typography,
  Grid,
  Card,
  CardContent,
  RadioGroup,
  FormControlLabel,
  Radio,
  Chip,
  Alert,
  Table,
  TableBody,
  TableRow,
  TableCell,
  IconButton,
  Tooltip,
  LinearProgress,
  Badge
} from '@mui/material';
import {
  Merge as MergeIcon,
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Business as BusinessIcon,
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  SwapHoriz as SwapIcon,
  ContentCopy as CopyIcon
} from '@mui/icons-material';
import { Contact, DuplicateMatch } from '../../types/contact.types';

interface MergeWizardProps {
  open: boolean;
  duplicates: DuplicateMatch[];
  onClose: () => void;
  onMerge: (master: Contact, mergedData: Partial<Contact>) => Promise<void>;
}

const steps = ['Wähle Master', 'Felder vergleichen', 'Vorschau', 'Bestätigung'];

export const MergeWizard: React.FC<MergeWizardProps> = ({
  open,
  duplicates,
  onClose,
  onMerge
}) => {
  const [activeStep, setActiveStep] = useState(0);
  const [masterId, setMasterId] = useState<string>('');
  const [fieldSelections, setFieldSelections] = useState<Map<string, string>>(new Map());
  const [merging, setMerging] = useState(false);
  
  const contacts = useMemo(() => {
    if (duplicates.length === 0) return [];
    return [duplicates[0].contact1, ...duplicates.map(d => d.contact2)];
  }, [duplicates]);
  
  const master = useMemo(() => {
    return contacts.find(c => c.id === masterId);
  }, [masterId, contacts]);
  
  const mergedContact = useMemo(() => {
    if (!master) return null;
    
    const merged = { ...master };
    
    fieldSelections.forEach((contactId, field) => {
      const source = contacts.find(c => c.id === contactId);
      if (source) {
        merged[field] = source[field];
      }
    });
    
    return merged;
  }, [master, fieldSelections, contacts]);
  
  // Field comparison data
  const fieldComparison = useMemo(() => {
    const fields = [
      { key: 'firstName', label: 'Vorname', icon: <PersonIcon /> },
      { key: 'lastName', label: 'Nachname', icon: <PersonIcon /> },
      { key: 'email', label: 'Email', icon: <EmailIcon /> },
      { key: 'phone', label: 'Telefon', icon: <PhoneIcon /> },
      { key: 'mobile', label: 'Mobil', icon: <PhoneIcon /> },
      { key: 'company', label: 'Firma', icon: <BusinessIcon /> },
      { key: 'position', label: 'Position', icon: <BusinessIcon /> },
      { key: 'department', label: 'Abteilung', icon: <BusinessIcon /> },
      { key: 'notes', label: 'Notizen', icon: <PersonIcon /> }
    ];
    
    return fields.map(field => ({
      ...field,
      values: contacts.map(c => ({
        contactId: c.id,
        value: c[field.key],
        isEmpty: !c[field.key],
        isSelected: fieldSelections.get(field.key) === c.id
      }))
    }));
  }, [contacts, fieldSelections]);
  
  const handleFieldSelection = (field: string, contactId: string) => {
    const newSelections = new Map(fieldSelections);
    newSelections.set(field, contactId);
    setFieldSelections(newSelections);
  };
  
  const autoSelectBestFields = () => {
    const newSelections = new Map<string, string>();
    
    fieldComparison.forEach(field => {
      // Find non-empty value with most information
      const bestValue = field.values
        .filter(v => !v.isEmpty)
        .sort((a, b) => {
          // Prefer master
          if (a.contactId === masterId) return -1;
          if (b.contactId === masterId) return 1;
          
          // Prefer longer values (more complete)
          const lengthA = String(a.value).length;
          const lengthB = String(b.value).length;
          return lengthB - lengthA;
        })[0];
      
      if (bestValue) {
        newSelections.set(field.key, bestValue.contactId);
      }
    });
    
    setFieldSelections(newSelections);
  };
  
  const renderStepContent = () => {
    switch (activeStep) {
      case 0: // Select Master
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Wähle den Hauptkontakt
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Der Hauptkontakt bleibt erhalten, die anderen werden gelöscht.
            </Typography>
            
            <RadioGroup
              value={masterId}
              onChange={(e) => setMasterId(e.target.value)}
            >
              <Grid container spacing={2}>
                {contacts.map((contact, index) => (
                  <Grid item xs={12} md={6} key={contact.id}>
                    <Card
                      sx={{
                        border: masterId === contact.id ? 2 : 1,
                        borderColor: masterId === contact.id ? 'primary.main' : 'divider',
                        cursor: 'pointer'
                      }}
                      onClick={() => setMasterId(contact.id)}
                    >
                      <CardContent>
                        <FormControlLabel
                          value={contact.id}
                          control={<Radio />}
                          label={
                            <Box>
                              <Typography variant="subtitle1">
                                {contact.firstName} {contact.lastName}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                {contact.company}
                              </Typography>
                              {contact.email && (
                                <Chip
                                  size="small"
                                  icon={<EmailIcon />}
                                  label={contact.email}
                                  sx={{ mt: 1, mr: 1 }}
                                />
                              )}
                              {contact.phone && (
                                <Chip
                                  size="small"
                                  icon={<PhoneIcon />}
                                  label={contact.phone}
                                  sx={{ mt: 1 }}
                                />
                              )}
                              
                              {index === 0 && (
                                <Chip
                                  label="Ältester Eintrag"
                                  color="info"
                                  size="small"
                                  sx={{ mt: 1, display: 'block' }}
                                />
                              )}
                              
                              {duplicates[index - 1] && (
                                <Box sx={{ mt: 2 }}>
                                  <Typography variant="caption" color="text.secondary">
                                    Ähnlichkeit: {Math.round(duplicates[index - 1].similarityScore * 100)}%
                                  </Typography>
                                  <LinearProgress
                                    variant="determinate"
                                    value={duplicates[index - 1].similarityScore * 100}
                                    sx={{ mt: 0.5 }}
                                  />
                                </Box>
                              )}
                            </Box>
                          }
                        />
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </RadioGroup>
          </Box>
        );
        
      case 1: // Field Comparison
        return (
          <Box>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
              <Typography variant="h6">
                Felder vergleichen und auswählen
              </Typography>
              <Button
                startIcon={<CheckIcon />}
                onClick={autoSelectBestFields}
              >
                Auto-Select Best
              </Button>
            </Box>
            
            <Table>
              <TableBody>
                {fieldComparison.map(field => (
                  <TableRow key={field.key}>
                    <TableCell sx={{ width: 150 }}>
                      <Box display="flex" alignItems="center" gap={1}>
                        {field.icon}
                        <Typography variant="body2">
                          {field.label}
                        </Typography>
                      </Box>
                    </TableCell>
                    
                    {field.values.map(value => (
                      <TableCell key={value.contactId}>
                        <Card
                          sx={{
                            p: 1,
                            cursor: value.isEmpty ? 'default' : 'pointer',
                            opacity: value.isEmpty ? 0.5 : 1,
                            border: value.isSelected ? 2 : 1,
                            borderColor: value.isSelected ? 'primary.main' : 'divider',
                            backgroundColor: value.isSelected ? 'action.selected' : 'transparent'
                          }}
                          onClick={() => !value.isEmpty && handleFieldSelection(field.key, value.contactId)}
                        >
                          {value.isEmpty ? (
                            <Typography variant="caption" color="text.secondary">
                              (leer)
                            </Typography>
                          ) : (
                            <Typography variant="body2">
                              {String(value.value)}
                            </Typography>
                          )}
                          
                          {value.contactId === masterId && (
                            <Chip label="Master" size="small" sx={{ mt: 0.5 }} />
                          )}
                        </Card>
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        );
        
      case 2: // Preview
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Vorschau des zusammengeführten Kontakts
            </Typography>
            
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Zusammengeführter Kontakt
                    </Typography>
                    
                    {mergedContact && (
                      <Box>
                        <Typography variant="h6">
                          {mergedContact.firstName} {mergedContact.lastName}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {mergedContact.company} - {mergedContact.position}
                        </Typography>
                        
                        <Box sx={{ mt: 2 }}>
                          {mergedContact.email && (
                            <Chip
                              icon={<EmailIcon />}
                              label={mergedContact.email}
                              sx={{ mb: 1, mr: 1 }}
                            />
                          )}
                          {mergedContact.phone && (
                            <Chip
                              icon={<PhoneIcon />}
                              label={mergedContact.phone}
                              sx={{ mb: 1 }}
                            />
                          )}
                        </Box>
                        
                        {mergedContact.notes && (
                          <Typography variant="body2" sx={{ mt: 2 }}>
                            {mergedContact.notes}
                          </Typography>
                        )}
                      </Box>
                    )}
                  </CardContent>
                </Card>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Card>
                  <CardContent>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                      Zu löschende Kontakte
                    </Typography>
                    
                    <List>
                      {contacts
                        .filter(c => c.id !== masterId)
                        .map(contact => (
                          <Box key={contact.id} sx={{ mb: 2 }}>
                            <Typography variant="body2" sx={{ textDecoration: 'line-through' }}>
                              {contact.firstName} {contact.lastName}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {contact.email}
                            </Typography>
                          </Box>
                        ))}
                    </List>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
            
            <Alert severity="warning" sx={{ mt: 3 }}>
              <strong>Wichtig:</strong> Die Zusammenführung kann nicht rückgängig gemacht werden.
              {contacts.length - 1} Kontakte werden gelöscht.
            </Alert>
          </Box>
        );
        
      case 3: // Confirmation
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Zusammenführung bestätigen
            </Typography>
            
            {merging ? (
              <Box sx={{ textAlign: 'center', py: 4 }}>
                <CircularProgress />
                <Typography variant="body2" sx={{ mt: 2 }}>
                  Kontakte werden zusammengeführt...
                </Typography>
              </Box>
            ) : (
              <Box>
                <Alert severity="info" sx={{ mb: 3 }}>
                  Die folgenden Aktionen werden durchgeführt:
                </Alert>
                
                <List>
                  <ListItem>
                    <ListItemIcon>
                      <CheckIcon color="success" />
                    </ListItemIcon>
                    <ListItemText
                      primary={`${master?.firstName} ${master?.lastName} wird als Hauptkontakt beibehalten`}
                    />
                  </ListItem>
                  
                  <ListItem>
                    <ListItemIcon>
                      <MergeIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary={`${fieldSelections.size} Felder werden aktualisiert`}
                    />
                  </ListItem>
                  
                  <ListItem>
                    <ListItemIcon>
                      <DeleteIcon color="error" />
                    </ListItemIcon>
                    <ListItemText
                      primary={`${contacts.length - 1} Duplikate werden gelöscht`}
                    />
                  </ListItem>
                </List>
              </Box>
            )}
          </Box>
        );
        
      default:
        return null;
    }
  };
  
  const handleNext = () => {
    if (activeStep === 0 && masterId) {
      autoSelectBestFields();
    }
    setActiveStep(activeStep + 1);
  };
  
  const handleBack = () => {
    setActiveStep(activeStep - 1);
  };
  
  const handleMerge = async () => {
    if (!master || !mergedContact) return;
    
    setMerging(true);
    try {
      await onMerge(master, mergedContact);
      onClose();
    } catch (error) {
      console.error('Merge failed:', error);
    } finally {
      setMerging(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={2}>
          <MergeIcon />
          <Typography variant="h6">
            Duplikate zusammenführen
          </Typography>
          <Chip label={`${contacts.length} Kontakte`} />
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map(label => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        <Box sx={{ minHeight: 400 }}>
          {renderStepContent()}
        </Box>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        <Button
          disabled={activeStep === 0}
          onClick={handleBack}
        >
          Zurück
        </Button>
        
        {activeStep === steps.length - 1 ? (
          <Button
            variant="contained"
            onClick={handleMerge}
            disabled={merging}
          >
            Zusammenführen
          </Button>
        ) : (
          <Button
            variant="contained"
            onClick={handleNext}
            disabled={activeStep === 0 && !masterId}
          >
            Weiter
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Similarity Algorithms

**Datei:** `frontend/src/features/customers/services/duplicate/LevenshteinDistance.ts`

```typescript
// CLAUDE: Levenshtein Distance für String-Ähnlichkeit
// Pfad: frontend/src/features/customers/services/duplicate/LevenshteinDistance.ts

export class StringSimilarity {
  
  /**
   * Calculate Levenshtein distance between two strings
   */
  static levenshteinDistance(str1: string, str2: string): number {
    const m = str1.length;
    const n = str2.length;
    
    // Create distance matrix
    const dp: number[][] = Array(m + 1)
      .fill(null)
      .map(() => Array(n + 1).fill(0));
    
    // Initialize first column and row
    for (let i = 0; i <= m; i++) {
      dp[i][0] = i;
    }
    for (let j = 0; j <= n; j++) {
      dp[0][j] = j;
    }
    
    // Fill the matrix
    for (let i = 1; i <= m; i++) {
      for (let j = 1; j <= n; j++) {
        if (str1[i - 1] === str2[j - 1]) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = Math.min(
            dp[i - 1][j] + 1,    // deletion
            dp[i][j - 1] + 1,    // insertion
            dp[i - 1][j - 1] + 1 // substitution
          );
        }
      }
    }
    
    return dp[m][n];
  }
  
  /**
   * Calculate similarity score (0-1) based on Levenshtein distance
   */
  static similarity(str1: string, str2: string): number {
    if (!str1 || !str2) return 0;
    if (str1 === str2) return 1;
    
    const distance = this.levenshteinDistance(str1, str2);
    const maxLength = Math.max(str1.length, str2.length);
    
    return 1 - (distance / maxLength);
  }
  
  /**
   * Jaro-Winkler similarity for better name matching
   */
  static jaroWinklerSimilarity(str1: string, str2: string): number {
    const jaro = this.jaroSimilarity(str1, str2);
    
    // Calculate common prefix length (up to 4 chars)
    let prefixLength = 0;
    for (let i = 0; i < Math.min(str1.length, str2.length, 4); i++) {
      if (str1[i] === str2[i]) {
        prefixLength++;
      } else {
        break;
      }
    }
    
    // Jaro-Winkler = Jaro + (prefix * p * (1 - Jaro))
    const p = 0.1; // Scaling factor
    return jaro + (prefixLength * p * (1 - jaro));
  }
  
  private static jaroSimilarity(str1: string, str2: string): number {
    if (str1 === str2) return 1;
    
    const len1 = str1.length;
    const len2 = str2.length;
    
    if (len1 === 0 || len2 === 0) return 0;
    
    const matchWindow = Math.floor(Math.max(len1, len2) / 2) - 1;
    const matches1 = new Array(len1).fill(false);
    const matches2 = new Array(len2).fill(false);
    
    let matches = 0;
    let transpositions = 0;
    
    // Find matches
    for (let i = 0; i < len1; i++) {
      const start = Math.max(0, i - matchWindow);
      const end = Math.min(i + matchWindow + 1, len2);
      
      for (let j = start; j < end; j++) {
        if (matches2[j] || str1[i] !== str2[j]) continue;
        
        matches1[i] = true;
        matches2[j] = true;
        matches++;
        break;
      }
    }
    
    if (matches === 0) return 0;
    
    // Count transpositions
    let k = 0;
    for (let i = 0; i < len1; i++) {
      if (!matches1[i]) continue;
      while (!matches2[k]) k++;
      if (str1[i] !== str2[k]) transpositions++;
      k++;
    }
    
    return (matches / len1 + matches / len2 + (matches - transpositions / 2) / matches) / 3;
  }
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Detection Algorithms (45 Min)
- [ ] Levenshtein Distance
- [ ] Jaro-Winkler Similarity
- [ ] Phonetic Matching
- [ ] Fuzzy Name Matching

### Phase 2: Backend Service (45 Min)
- [ ] DuplicateDetectionService
- [ ] Candidate Blocking
- [ ] Similarity Calculation
- [ ] Performance Optimization

### Phase 3: UI Components (45 Min)
- [ ] Duplicate Finder Dialog
- [ ] Merge Wizard
- [ ] Field Comparison View
- [ ] Preview & Confirmation

### Phase 4: Integration (30 Min)
- [ ] Auto-Detection on Import
- [ ] Background Scanning
- [ ] Merge History
- [ ] Undo Support

### Phase 5: Testing (30 Min)
- [ ] Algorithm Tests
- [ ] Performance Tests (10k+ contacts)
- [ ] Edge Cases
- [ ] UI Tests

## 🔗 INTEGRATION POINTS

1. **Import Process** - Auto-detect während Import
2. **Contact List** - Duplicate Badge anzeigen
3. **Audit Log** - Merge-Aktionen tracken
4. **Background Jobs** - Periodischer Scan

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **False Positives**
   → Lösung: Konfidenz-Level, manuelle Review

2. **Performance bei vielen Kontakten**
   → Lösung: Blocking, Indexing, Batch Processing

3. **Datenverlust beim Merge**
   → Lösung: Audit Trail, Soft Delete Option

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 195 Minuten  
**Nächstes Dokument:** [→ Communication History](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COMMUNICATION_HISTORY.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Saubere Daten = Erfolgreiche Beziehungen! 🔍✨**