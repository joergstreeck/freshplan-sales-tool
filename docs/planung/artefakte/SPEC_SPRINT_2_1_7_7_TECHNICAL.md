# Sprint 2.1.7.7 - Technical Specification

**Sprint-ID:** 2.1.7.7
**Created:** 2025-10-21
**Updated:** 2025-10-21 (Option A: UI bereits in Sprint 2.1.7.2 vorbereitet)
**Status:** 📋 PLANNING
**Owner:** TBD
**Estimated Effort:** 30h (reduziert von 36h durch Sprint 2.1.7.2 Vorbereitung)
**Dependencies:** Sprint 2.1.7.2 COMPLETE (UI-Vorbereitung!)

---

## 🎯 WHY NOW? Aktivierung statt Neubau!

**Sprint 2.1.7.2 hat bereits vorbereitet:**
- ✅ ConvertToCustomerDialog: hierarchyType UI existiert (FILIALE disabled)
- ✅ CustomerOnboardingWizard: hierarchyType Dropdown existiert
- ✅ Backend: hierarchyType wird bereits gespeichert

**Dieser Sprint aktiviert nur:**
- 🔓 FILIALE Option enabled (disabled entfernen)
- ➕ Parent-Selection Autocomplete hinzufügen (50 Zeilen Code)
- 🏢 BranchService + CreateBranchDialog (neu)
- 📊 HierarchyDashboard (neu)

**Aufwands-Reduktion:**
- Original-Planung: 36h (komplett neu)
- **Option A (JETZT): 30h** (-6h durch Vorbereitung!)
- UI-Grundlage existiert bereits → Nur Feature-Aktivierung

---

## 📋 INHALTSVERZEICHNIS

**0️⃣ [UI-Aktivierung](#0️⃣-ui-aktivierung-sprint-2172-vorbereitung-nutzen)** (Sprint 2.1.7.2 → 2.1.7.7)
   - [0.1 ConvertDialog FILIALE aktivieren](#01-convertdialog-filiale-aktivieren)
   - [0.2 Parent-Selection Autocomplete](#02-parent-selection-autocomplete)

**1️⃣-7️⃣ Neue Features:**
1. [Backend: Branch Service](#1️⃣-backend-branch-service)
2. [Backend: Address-Matching Service](#2️⃣-backend-address-matching-service)
3. [Backend: Hierarchy Metrics Service](#3️⃣-backend-hierarchy-metrics-service)
4. [Frontend: Create Branch Dialog](#4️⃣-frontend-create-branch-dialog)
5. [Frontend: Hierarchy Dashboard](#5️⃣-frontend-hierarchy-dashboard)
6. [Frontend: Hierarchy Tree View](#6️⃣-frontend-hierarchy-tree-view)
7. [CustomerDetailPage Integration](#7️⃣-customerdetailpage-integration)

**8️⃣-10 Optional/Testing:**
8. [Migration: Opportunity Location Link (Optional)](#8️⃣-migration-opportunity-location-link-optional)
9. [API Endpoints](#9️⃣-api-endpoints)
10. [Test Specifications](#🔟-test-specifications)

---

## 0️⃣ UI-Aktivierung (Sprint 2.1.7.2 Vorbereitung nutzen)

**Aufwand dieser Section: 1h** (statt 6h - UI existiert bereits!)

### **0.1 ConvertDialog FILIALE aktivieren**

**Datei:** `frontend/src/features/opportunity/components/ConvertToCustomerDialog.tsx`

**Status:** 📋 ENHANCEMENT (Sprint 2.1.7.2 vorbereitet!)

**Was existiert bereits (Sprint 2.1.7.2):**
```tsx
// ✅ hierarchyType State existiert
const [hierarchyType, setHierarchyType] = useState<HierarchyType>('STANDALONE');

// ✅ Dropdown existiert
<Select value={hierarchyType} onChange={...}>
  <MenuItem value="STANDALONE">Einzelbetrieb</MenuItem>
  <MenuItem value="HEADQUARTER">Zentrale/Hauptbetrieb</MenuItem>
  <MenuItem value="FILIALE" disabled> {/* ← Nur disabled entfernen! */}
    Filiale (gehört zu Zentrale)
  </MenuItem>
</Select>
```

**Was wir JETZT machen (5 min!):**
```tsx
// VORHER (Sprint 2.1.7.2):
<MenuItem value="FILIALE" disabled>
  Filiale (gehört zu Zentrale)
</MenuItem>

// NACHHER (Sprint 2.1.7.7):
<MenuItem value="FILIALE"> {/* ← disabled entfernt! */}
  Filiale (gehört zu Zentrale)
</MenuItem>
```

**Aufwand:** 5 Minuten (1 Zeile ändern!)

---

### **0.2 Parent-Selection Autocomplete**

**Datei:** `frontend/src/features/opportunity/components/ConvertToCustomerDialog.tsx`

**Status:** 📋 ENHANCEMENT

**Implementierung:**
```tsx
// State für Parent-Selection (Sprint 2.1.7.2 vorbereitet!)
const [parentCustomer, setParentCustomer] = useState<Customer | null>(null);

// Fetch HEADQUARTER customers
const { data: headquarterCustomers } = useQuery(
  ['customers-headquarter'],
  () => httpClient.get('/api/customers?hierarchyType=HEADQUARTER')
);

return (
  <Dialog open={open} onClose={onClose}>
    <DialogContent>
      {/* Existing: hierarchyType Dropdown */}
      {/* ... */}

      {/* NEU: Parent-Selection (nur wenn FILIALE gewählt) */}
      {hierarchyType === 'FILIALE' && (
        <Autocomplete
          options={headquarterCustomers || []}
          getOptionLabel={(option) => option.companyName}
          renderInput={(params) => (
            <TextField
              {...params}
              label="Gehört zu (Hauptbetrieb)"
              required
              helperText="Wählen Sie den Hauptbetrieb aus, zu dem diese Filiale gehört"
            />
          )}
          renderOption={(props, option) => (
            <Box component="li" {...props}>
              <Stack>
                <Typography variant="body1">{option.companyName}</Typography>
                <Typography variant="caption" color="text.secondary">
                  {option.city} • {option.businessType}
                </Typography>
              </Stack>
            </Box>
          )}
          onChange={(event, value) => setParentCustomer(value)}
          value={parentCustomer}
          sx={{ mb: 2 }}
        />
      )}

      {/* Info-Alert aktualisieren (Sprint 2.1.7.2 → 2.1.7.7) */}
      {hierarchyType === 'FILIALE' && !parentCustomer && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Bitte wählen Sie den Hauptbetrieb aus.
        </Alert>
      )}

      {/* Button disabled-Logik aktualisieren */}
      <Button
        variant="contained"
        onClick={handleConvert}
        disabled={
          !companyName ||
          (hierarchyType === 'FILIALE' && !parentCustomer) // ← NEU!
        }
      >
        Customer anlegen
      </Button>
    </DialogContent>
  </Dialog>
);
```

**Backend Integration:**
```java
// OpportunityService.convertToCustomer() erweitern

if (request.hierarchyType() == CustomerHierarchyType.FILIALE) {
    // Jetzt aktiv (Sprint 2.1.7.2 war disabled)
    Customer parent = customerRepository.findById(request.parentCustomerId());
    customer.setParentCustomer(parent);
    customer.setXentralCustomerId(parent.getXentralCustomerId()); // Gleiche!
}
```

**Tests:**
- Component Test: Parent-Selection shows when FILIALE selected
- Component Test: Only HEADQUARTER customers in dropdown
- Integration Test: FILIALE customer created with parent_id

**Aufwand:** 1h (50 Zeilen Code + Tests)

---

**Section 0 Total: 1h** (statt 6h - Vorbereitung spart 5h!)

---

## 1️⃣ Backend: Branch Service

### **BranchService.java**

```java
package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerHierarchyType;
import de.freshplan.domain.customer.entity.CustomerLocation;
import de.freshplan.domain.customer.entity.CustomerAddress;
import de.freshplan.domain.customer.entity.AddressType;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;

@ApplicationScoped
public class BranchService {

  @Inject CustomerRepository customerRepository;

  /**
   * Creates a new branch (child customer) for a parent customer.
   *
   * @param parentId ID of the parent customer (HEADQUARTER)
   * @param request Branch creation request
   * @return Created branch customer
   */
  @Transactional
  public Customer createBranch(UUID parentId, CreateBranchRequest request) {
    // Validate parent
    Customer parent = customerRepository.findByIdOptional(parentId)
      .orElseThrow(() -> new BusinessException("Parent customer not found"));

    if (parent.getHierarchyType() != CustomerHierarchyType.HEADQUARTER) {
      throw new BusinessException("Only HEADQUARTER customers can have branches");
    }

    // Create branch customer
    Customer branch = new Customer();
    branch.setCompanyName(request.branchName);
    branch.setParentCustomer(parent);
    branch.setHierarchyType(CustomerHierarchyType.FILIALE);
    branch.setXentralCustomerId(parent.getXentralCustomerId()); // ← Same as parent!
    branch.setStatus(CustomerStatus.PROSPECT);
    branch.setCreatedBy(request.createdBy);

    // Copy parent business fields
    branch.setBusinessType(parent.getBusinessType());
    branch.setIsChain(true); // Branch implies chain

    // Create main location with address
    CustomerLocation location = new CustomerLocation();
    location.setLocationName(request.branchName);
    location.setIsMainLocation(true);
    location.setIsShippingLocation(true);
    location.setCreatedBy(request.createdBy);
    branch.addLocation(location);

    // Create address for location
    CustomerAddress address = new CustomerAddress();
    address.setAddressType(AddressType.SHIPPING);
    address.setStreet(request.address.street);
    address.setZipCode(request.address.zipCode);
    address.setCity(request.address.city);
    address.setCountry(request.address.country);
    address.setIsPrimaryForType(true);
    address.setCreatedBy(request.createdBy);
    location.addAddress(address);

    // Persist
    customerRepository.persist(branch);

    return branch;
  }

  /**
   * DTO for branch creation request.
   */
  public record CreateBranchRequest(
    String branchName,
    AddressRequest address,
    String createdBy
  ) {}

  public record AddressRequest(
    String street,
    String zipCode,
    String city,
    String country
  ) {}
}
```

---

## 2️⃣ Backend: Address-Matching Service

### **XentralAddressMatcher.java**

```java
package de.freshplan.integration.xentral.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerLocation;
import de.freshplan.domain.customer.entity.CustomerAddress;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class XentralAddressMatcher {

  private static final double SIMILARITY_THRESHOLD = 0.80; // 80% match required
  private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

  /**
   * Matches a Xentral delivery address to a FreshPlan customer (branch).
   *
   * <p>Algorithm:
   * 1. Find all children with same xentral_customer_id
   * 2. Fuzzy-match delivery address to each child's main shipping address
   * 3. Return best match (≥80% similarity)
   * 4. Fallback: Return parent if no match
   *
   * @param parent Parent customer (HEADQUARTER)
   * @param xentralDeliveryAddress Delivery address from Xentral invoice/order
   * @return Matched customer (branch) or parent (fallback)
   */
  public Customer matchDeliveryAddress(Customer parent, String xentralDeliveryAddress) {
    // Normalize input
    String normalizedInput = normalizeAddress(xentralDeliveryAddress);

    // Get all children (branches)
    List<Customer> branches = parent.getChildCustomers().stream()
      .filter(c -> c.getXentralCustomerId() != null)
      .filter(c -> c.getXentralCustomerId().equals(parent.getXentralCustomerId()))
      .toList();

    double bestSimilarity = 0.0;
    Customer bestMatch = null;

    // Fuzzy-match against each branch
    for (Customer branch : branches) {
      Optional<CustomerLocation> mainLocation = branch.getMainLocation();
      if (mainLocation.isEmpty()) continue;

      Optional<CustomerAddress> shippingAddress = mainLocation.get().getPrimaryShippingAddress();
      if (shippingAddress.isEmpty()) continue;

      String branchAddress = buildFullAddress(shippingAddress.get());
      double similarity = calculateSimilarity(normalizedInput, branchAddress);

      if (similarity > bestSimilarity) {
        bestSimilarity = similarity;
        bestMatch = branch;
      }
    }

    // Check threshold
    if (bestSimilarity >= SIMILARITY_THRESHOLD && bestMatch != null) {
      log.debug("Address matched: {} → {} ({}% similarity)",
        xentralDeliveryAddress, bestMatch.getCompanyName(), (int)(bestSimilarity * 100));
      return bestMatch;
    }

    // Fallback: Parent
    log.warn("No address match found for '{}' (best: {}%), using parent",
      xentralDeliveryAddress, (int)(bestSimilarity * 100));
    return parent;
  }

  /**
   * Calculates similarity between two addresses using Levenshtein distance.
   *
   * @return Similarity score (0.0 = no match, 1.0 = perfect match)
   */
  private double calculateSimilarity(String address1, String address2) {
    int maxLength = Math.max(address1.length(), address2.length());
    if (maxLength == 0) return 1.0;

    int distance = levenshteinDistance.apply(address1, address2);
    return 1.0 - ((double) distance / maxLength);
  }

  /**
   * Builds full address string from CustomerAddress entity.
   */
  private String buildFullAddress(CustomerAddress address) {
    return normalizeAddress(String.join(", ",
      address.getStreet() != null ? address.getStreet() : "",
      address.getZipCode() != null ? address.getZipCode() : "",
      address.getCity() != null ? address.getCity() : ""
    ));
  }

  /**
   * Normalizes address for matching (lowercase, trim, remove special chars).
   */
  private String normalizeAddress(String address) {
    return address
      .toLowerCase()
      .trim()
      .replaceAll("[^a-z0-9\\s]", "")  // Remove special chars
      .replaceAll("\\s+", " ");         // Normalize whitespace
  }
}
```

**Dependencies (pom.xml):**
```xml
<!-- Apache Commons Text for Levenshtein Distance -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-text</artifactId>
  <version>1.11.0</version>
</dependency>
```

---

## 3️⃣ Backend: Hierarchy Metrics Service

### **HierarchyMetricsService.java**

```java
package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerHierarchyType;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.opportunity.entity.OpportunityStage;
import de.freshplan.domain.opportunity.repository.OpportunityRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HierarchyMetricsService {

  @Inject CustomerRepository customerRepository;
  @Inject OpportunityRepository opportunityRepository;

  /**
   * Calculates hierarchy metrics for a parent customer (roll-up).
   *
   * <p>Includes:
   * - Total revenue (sum of all branches)
   * - Average revenue per branch
   * - Total open opportunities
   * - Revenue per branch (with percentage)
   *
   * @param parentId ID of parent customer (HEADQUARTER)
   * @return Hierarchy metrics with branch details
   */
  public HierarchyMetrics getHierarchyMetrics(UUID parentId) {
    Customer parent = customerRepository.findByIdOptional(parentId)
      .orElseThrow(() -> new BusinessException("Customer not found"));

    if (parent.getHierarchyType() != CustomerHierarchyType.HEADQUARTER) {
      throw new BusinessException("Only HEADQUARTER customers have hierarchy metrics");
    }

    List<Customer> branches = parent.getChildCustomers();

    // Calculate total revenue
    BigDecimal totalRevenue = BigDecimal.ZERO;
    List<BranchRevenueDetail> branchDetails = new ArrayList<>();

    for (Customer branch : branches) {
      BigDecimal branchRevenue = branch.getActualAnnualVolume() != null
        ? branch.getActualAnnualVolume()
        : BigDecimal.ZERO;

      totalRevenue = totalRevenue.add(branchRevenue);

      int openOpportunities = opportunityRepository.countByCustomerAndStageNot(
        branch,
        OpportunityStage.CLOSED_WON
      );

      branchDetails.add(new BranchRevenueDetail(
        branch.getId(),
        branch.getCompanyName(),
        getCity(branch),
        getCountry(branch),
        branchRevenue,
        null, // percentage - calculate later
        openOpportunities,
        branch.getStatus()
      ));
    }

    // Calculate percentages
    BigDecimal finalTotalRevenue = totalRevenue;
    branchDetails = branchDetails.stream()
      .map(detail -> new BranchRevenueDetail(
        detail.branchId,
        detail.branchName,
        detail.city,
        detail.country,
        detail.revenue,
        calculatePercentage(detail.revenue, finalTotalRevenue),
        detail.openOpportunities,
        detail.status
      ))
      .toList();

    // Average revenue
    BigDecimal averageRevenue = branches.isEmpty()
      ? BigDecimal.ZERO
      : totalRevenue.divide(BigDecimal.valueOf(branches.size()), 2, RoundingMode.HALF_UP);

    // Total opportunities
    int totalOpenOpportunities = branchDetails.stream()
      .mapToInt(BranchRevenueDetail::openOpportunities)
      .sum();

    return new HierarchyMetrics(
      totalRevenue,
      averageRevenue,
      branches.size(),
      totalOpenOpportunities,
      branchDetails
    );
  }

  private BigDecimal calculatePercentage(BigDecimal value, BigDecimal total) {
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return value
      .divide(total, 4, RoundingMode.HALF_UP)
      .multiply(BigDecimal.valueOf(100))
      .setScale(1, RoundingMode.HALF_UP);
  }

  private String getCity(Customer customer) {
    return customer.getMainLocation()
      .flatMap(CustomerLocation::getPrimaryShippingAddress)
      .map(CustomerAddress::getCity)
      .orElse("N/A");
  }

  private String getCountry(Customer customer) {
    return customer.getMainLocation()
      .flatMap(CustomerLocation::getPrimaryShippingAddress)
      .map(CustomerAddress::getCountry)
      .orElse("DE");
  }

  /**
   * Response DTOs
   */
  public record HierarchyMetrics(
    BigDecimal totalRevenue,
    BigDecimal averageRevenue,
    int branchCount,
    int totalOpenOpportunities,
    List<BranchRevenueDetail> branches
  ) {}

  public record BranchRevenueDetail(
    UUID branchId,
    String branchName,
    String city,
    String country,
    BigDecimal revenue,
    BigDecimal percentage,
    int openOpportunities,
    CustomerStatus status
  ) {}
}
```

---

## 4️⃣ Frontend: Create Branch Dialog

### **CreateBranchDialog.tsx**

```tsx
import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Alert,
  Stack,
  Grid,
} from '@mui/material';
import { useCreateBranch } from '../api/branchApi';
import { Customer } from '../types/Customer';

interface CreateBranchDialogProps {
  open: boolean;
  onClose: () => void;
  parentCustomer: Customer;
}

export const CreateBranchDialog: React.FC<CreateBranchDialogProps> = ({
  open,
  onClose,
  parentCustomer,
}) => {
  const [branchName, setBranchName] = useState('');
  const [street, setStreet] = useState('');
  const [zipCode, setZipCode] = useState('');
  const [city, setCity] = useState('');
  const [country, setCountry] = useState('DE');

  const createBranchMutation = useCreateBranch();

  const handleSubmit = async () => {
    try {
      await createBranchMutation.mutateAsync({
        parentId: parentCustomer.id,
        branchName,
        address: {
          street,
          zipCode,
          city,
          country,
        },
      });

      // Success
      onClose();
      // Reset form
      setBranchName('');
      setStreet('');
      setZipCode('');
      setCity('');
      setCountry('DE');
    } catch (error) {
      console.error('Failed to create branch:', error);
    }
  };

  const isValid = branchName.trim() !== '' &&
                  street.trim() !== '' &&
                  zipCode.trim() !== '' &&
                  city.trim() !== '';

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Neue Filiale anlegen</DialogTitle>
      <DialogContent>
        <Stack spacing={3} sx={{ mt: 2 }}>
          {/* Info Alert */}
          <Alert severity="info">
            <strong>Parent-Organisation:</strong> {parentCustomer.companyName}
            <br />
            <strong>Xentral-Kundennummer:</strong> {parentCustomer.xentralCustomerId || 'N/A'}
            <br />
            <small>
              Die Filiale erhält die gleiche Xentral-Kundennummer wie die Zentrale.
              Unterscheidung erfolgt über die Lieferadresse.
            </small>
          </Alert>

          {/* Branch Name */}
          <TextField
            label="Filialname *"
            value={branchName}
            onChange={(e) => setBranchName(e.target.value)}
            placeholder="z.B. NH Hotel Berlin Alexanderplatz"
            fullWidth
            helperText="Name der Filiale (wird als Customer-Name verwendet)"
          />

          {/* Address */}
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <TextField
                label="Straße & Hausnummer *"
                value={street}
                onChange={(e) => setStreet(e.target.value)}
                placeholder="z.B. Alexanderplatz 1"
                fullWidth
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                label="PLZ *"
                value={zipCode}
                onChange={(e) => setZipCode(e.target.value)}
                placeholder="10178"
                fullWidth
              />
            </Grid>

            <Grid item xs={12} md={5}>
              <TextField
                label="Stadt *"
                value={city}
                onChange={(e) => setCity(e.target.value)}
                placeholder="Berlin"
                fullWidth
              />
            </Grid>

            <Grid item xs={12} md={3}>
              <TextField
                label="Land"
                value={country}
                onChange={(e) => setCountry(e.target.value)}
                placeholder="DE"
                fullWidth
              />
            </Grid>
          </Grid>

          {/* Xentral Matching Info */}
          <Alert severity="warning">
            <strong>Wichtig:</strong> Die Adresse wird für Xentral-Umsatz-Matching verwendet.
            Stellen Sie sicher, dass sie mit der Lieferadresse in Xentral übereinstimmt.
          </Alert>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={!isValid || createBranchMutation.isLoading}
        >
          {createBranchMutation.isLoading ? 'Wird erstellt...' : 'Filiale anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

## 5️⃣ Frontend: Hierarchy Dashboard

### **HierarchyDashboard.tsx**

```tsx
import React from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  Grid,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Typography,
  Chip,
  IconButton,
  Button,
  Alert,
  Stack,
  Box,
} from '@mui/material';
import { OpenInNew as OpenInNewIcon, Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useHierarchyMetrics } from '../api/hierarchyApi';
import { Customer } from '../types/Customer';
import { MetricCard } from '../components/MetricCard';
import { formatCurrency } from '../utils/formatters';

interface HierarchyDashboardProps {
  parent: Customer;
  onCreateBranch: () => void;
}

export const HierarchyDashboard: React.FC<HierarchyDashboardProps> = ({
  parent,
  onCreateBranch,
}) => {
  const navigate = useNavigate();
  const { data: metrics, isLoading } = useHierarchyMetrics(parent.id);

  if (isLoading) {
    return <div>Lädt Standort-Daten...</div>;
  }

  if (!metrics) {
    return null;
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'AKTIV':
        return 'success';
      case 'PROSPECT':
        return 'info';
      case 'RISIKO':
        return 'warning';
      default:
        return 'default';
    }
  };

  return (
    <Stack spacing={3}>
      {/* Header with Actions */}
      <Box display="flex" justifyContent="space-between" alignItems="center">
        <Typography variant="h5">Standort-Übersicht</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={onCreateBranch}
        >
          Neue Filiale anlegen
        </Button>
      </Box>

      {/* Gesamt-Metriken */}
      <Grid container spacing={2}>
        <Grid item xs={12} md={3}>
          <MetricCard
            title="Gesamt-Umsatz (Konzern)"
            value={formatCurrency(metrics.totalRevenue)}
            subtitle={`${metrics.branchCount} Standorte`}
            color="primary"
          />
        </Grid>

        <Grid item xs={12} md={3}>
          <MetricCard
            title="Durchschnitt pro Standort"
            value={formatCurrency(metrics.averageRevenue)}
            subtitle="12 Monate"
            color="info"
          />
        </Grid>

        <Grid item xs={12} md={3}>
          <MetricCard
            title="Standorte"
            value={metrics.branchCount}
            subtitle="Filialen"
            color="success"
          />
        </Grid>

        <Grid item xs={12} md={3}>
          <MetricCard
            title="Opportunities"
            value={metrics.totalOpenOpportunities}
            subtitle="Alle Standorte"
            color="warning"
          />
        </Grid>
      </Grid>

      {/* Alert wenn keine Standorte */}
      {metrics.branchCount === 0 && (
        <Alert severity="info">
          Noch keine Filialen angelegt.
          Klicken Sie auf "Neue Filiale anlegen" um zu starten.
        </Alert>
      )}

      {/* Standort-Tabelle */}
      {metrics.branchCount > 0 && (
        <Card>
          <CardHeader title="Standort-Details (sortiert nach Umsatz)" />
          <CardContent>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Standort</TableCell>
                  <TableCell align="right">Umsatz (12 Monate)</TableCell>
                  <TableCell align="right">Anteil</TableCell>
                  <TableCell align="right">Opportunities</TableCell>
                  <TableCell align="center">Status</TableCell>
                  <TableCell>Aktionen</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {metrics.branches
                  .sort((a, b) => Number(b.revenue) - Number(a.revenue))
                  .map((branch) => (
                    <TableRow
                      key={branch.branchId}
                      hover
                      onClick={() => navigate(`/customers/${branch.branchId}`)}
                      sx={{ cursor: 'pointer' }}
                    >
                      <TableCell>
                        <Stack>
                          <Typography variant="body1" fontWeight="bold">
                            {branch.branchName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {branch.city}, {branch.country}
                          </Typography>
                        </Stack>
                      </TableCell>

                      <TableCell align="right">
                        <Typography variant="h6">
                          {formatCurrency(branch.revenue)}
                        </Typography>
                      </TableCell>

                      <TableCell align="right">
                        <Typography variant="body2" color="text.secondary">
                          {branch.percentage}%
                        </Typography>
                      </TableCell>

                      <TableCell align="right">
                        <Chip
                          label={`${branch.openOpportunities} offen`}
                          size="small"
                          color={branch.openOpportunities > 0 ? 'primary' : 'default'}
                        />
                      </TableCell>

                      <TableCell align="center">
                        <Chip
                          label={branch.status}
                          color={getStatusColor(branch.status)}
                          size="small"
                        />
                      </TableCell>

                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            navigate(`/customers/${branch.branchId}`);
                          }}
                        >
                          <OpenInNewIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </Stack>
  );
};
```

---

## 6️⃣ Frontend: Hierarchy Tree View

### **HierarchyTreeView.tsx**

```tsx
import React from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  Box,
  Typography,
  Chip,
  Stack,
} from '@mui/material';
import { TreeView, TreeItem } from '@mui/x-tree-view';
import {
  ExpandMore as ExpandMoreIcon,
  ChevronRight as ChevronRightIcon,
  Business as BusinessIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { Customer } from '../types/Customer';
import { formatCurrency } from '../utils/formatters';

interface HierarchyTreeViewProps {
  rootCustomer: Customer;
}

export const HierarchyTreeView: React.FC<HierarchyTreeViewProps> = ({
  rootCustomer,
}) => {
  const navigate = useNavigate();

  return (
    <Card>
      <CardHeader title="Konzern-Hierarchie" />
      <CardContent>
        <TreeView
          defaultCollapseIcon={<ExpandMoreIcon />}
          defaultExpandIcon={<ChevronRightIcon />}
          defaultExpanded={[rootCustomer.id]}
        >
          {/* Root Level (Parent) */}
          <TreeItem
            nodeId={rootCustomer.id}
            label={
              <HierarchyNodeLabel
                customer={rootCustomer}
                type="HEADQUARTER"
                onClick={() => navigate(`/customers/${rootCustomer.id}`)}
              />
            }
          >
            {/* Children (Branches) */}
            {rootCustomer.childCustomers?.map((child) => (
              <TreeItem
                key={child.id}
                nodeId={child.id}
                label={
                  <HierarchyNodeLabel
                    customer={child}
                    type="FILIALE"
                    onClick={() => navigate(`/customers/${child.id}`)}
                  />
                }
              />
            ))}
          </TreeItem>
        </TreeView>
      </CardContent>
    </Card>
  );
};

interface HierarchyNodeLabelProps {
  customer: Customer;
  type: 'HEADQUARTER' | 'FILIALE';
  onClick: () => void;
}

const HierarchyNodeLabel: React.FC<HierarchyNodeLabelProps> = ({
  customer,
  type,
  onClick,
}) => {
  return (
    <Box
      display="flex"
      alignItems="center"
      gap={2}
      onClick={(e) => {
        e.stopPropagation();
        onClick();
      }}
      sx={{
        cursor: 'pointer',
        py: 1,
        '&:hover': {
          bgcolor: 'action.hover',
        },
      }}
    >
      <BusinessIcon color={type === 'HEADQUARTER' ? 'primary' : 'action'} />
      <Typography fontWeight={type === 'HEADQUARTER' ? 'bold' : 'normal'}>
        {customer.companyName}
      </Typography>
      {customer.actualAnnualVolume && (
        <Chip
          label={formatCurrency(customer.actualAnnualVolume)}
          size="small"
        />
      )}
      <Chip label={type} size="small" variant="outlined" />
    </Box>
  );
};
```

---

## 7️⃣ CustomerDetailPage Integration

### **User-Workflow: Filial-Management**

**Wo wird CreateBranchDialog aufgerufen?**

```
CustomerDetailPage (Hauptbetrieb)
  ↓
Tab "Filialen" (NEU - nur wenn hierarchyType = HEADQUARTER)
  ↓
HierarchyDashboard Component
  ↓
Button "Neue Filiale anlegen"
  ↓
CreateBranchDialog opens
```

### **CustomerDetailPage.tsx (Erweiterung)**

**NEU: Tab "Filialen" hinzufügen**

```tsx
// CustomerDetailPage.tsx (bestehende Datei erweitern)

import React, { useState } from 'react';
import { Tabs, Tab, Box } from '@mui/material';
import { HierarchyDashboard } from '../features/customers/components/HierarchyDashboard';
import { CreateBranchDialog } from '../features/customers/components/CreateBranchDialog';
import { useCustomer } from '../features/customers/hooks/useCustomer';

export const CustomerDetailPage: React.FC = () => {
  const { customerId } = useParams<{ customerId: string }>();
  const { data: customer } = useCustomer(customerId);
  const [currentTab, setCurrentTab] = useState(0);
  const [branchDialogOpen, setBranchDialogOpen] = useState(false);

  if (!customer) return <LoadingScreen />;

  // Nur Filialen-Tab zeigen wenn HEADQUARTER
  const showBranchesTab = customer.hierarchyType === 'HEADQUARTER';

  return (
    <Box>
      {/* Header mit Customer-Info */}
      <CustomerHeader customer={customer} />

      {/* Tabs */}
      <Tabs value={currentTab} onChange={(e, v) => setCurrentTab(v)}>
        <Tab label="Übersicht" />
        <Tab label="Opportunities" />
        <Tab label="Kontakte" />
        {showBranchesTab && <Tab label="Filialen" />}
        <Tab label="Dokumente" />
      </Tabs>

      {/* Tab Content */}
      <Box sx={{ py: 3 }}>
        {currentTab === 0 && <CustomerOverview customer={customer} />}
        {currentTab === 1 && <CustomerOpportunities customerId={customer.id} />}
        {currentTab === 2 && <CustomerContacts customerId={customer.id} />}

        {/* NEU: Filialen-Tab */}
        {showBranchesTab && currentTab === 3 && (
          <HierarchyDashboard
            parent={customer}
            onCreateBranch={() => setBranchDialogOpen(true)}
          />
        )}

        {currentTab === (showBranchesTab ? 4 : 3) && (
          <CustomerDocuments customerId={customer.id} />
        )}
      </Box>

      {/* CreateBranchDialog */}
      <CreateBranchDialog
        open={branchDialogOpen}
        onClose={() => setBranchDialogOpen(false)}
        parentCustomer={customer}
      />
    </Box>
  );
};
```

### **Conditional Rendering (Wichtig!)**

**Tab "Filialen" nur zeigen wenn:**
- `customer.hierarchyType === 'HEADQUARTER'`
- ODER `customer.isChain === true`
- ODER `customer.totalLocationsEU > 1`

**Für STANDALONE/FILIALE Customers:**
- ❌ Kein Tab "Filialen"
- Tab-Reihenfolge: Übersicht, Opportunities, Kontakte, Dokumente

**Für FILIALE Customers (Child):**
- ✅ Link zu Parent anzeigen: "Teil von: [NH Hotels Deutschland GmbH]"
- ❌ Kein CreateBranch Button (nur Parent kann Branches anlegen)

### **Code-Änderungen:**

**Dateien:**
1. `frontend/src/pages/CustomerDetailPage.tsx` (erweitern)
2. `frontend/src/features/customers/components/HierarchyDashboard.tsx` (NEU)
3. `frontend/src/features/customers/components/CreateBranchDialog.tsx` (NEU)

**Tests:**
- `CustomerDetailPage.test.tsx` erweitern (Tab-Visibility, Conditional Rendering)

---

## 8️⃣ Migration: Opportunity Location Link (Optional)

**NOTE:** Migration V10036 ist **OPTIONAL** und nur nötig wenn Option B (Locations) gewählt wird. Bei Option A (Separate Customers) wird diese Migration **NICHT** benötigt!

### **V10036__opportunity_location_link.sql**

```sql
-- Sprint 2.1.7.7: Opportunity → Location Link (OPTIONAL - nur wenn Option B)
-- Migration V10036
-- NOTE: Bei Option A (Separate Customers) wird diese Migration NICHT ausgeführt!

-- Nur wenn Location-basiertes Modell gewählt wird:
-- ALTER TABLE opportunities
-- ADD COLUMN location_id UUID REFERENCES customer_locations(id);

-- CREATE INDEX idx_opportunity_location ON opportunities(location_id);

-- COMMENT:
-- Bei Option A (CRM Best Practice) ist location_id NICHT nötig!
-- Opportunity.customer_id → Customer (Filiale) direkt
```

---

## 9️⃣ API Endpoints

### **CustomerResource.java (erweitert)**

```java
@Path("/api/customers")
public class CustomerResource {

  @Inject BranchService branchService;
  @Inject HierarchyMetricsService hierarchyMetricsService;

  /**
   * Create a new branch for a parent customer.
   *
   * POST /api/customers/{id}/branches
   */
  @POST
  @Path("/{id}/branches")
  @Transactional
  public Response createBranch(
      @PathParam("id") UUID parentId,
      CreateBranchRequest request
  ) {
    Customer branch = branchService.createBranch(parentId, request);
    return Response.status(Response.Status.CREATED)
      .entity(CustomerResponse.from(branch))
      .build();
  }

  /**
   * Get hierarchy metrics for a parent customer.
   *
   * GET /api/customers/{id}/hierarchy-metrics
   */
  @GET
  @Path("/{id}/hierarchy-metrics")
  public Response getHierarchyMetrics(@PathParam("id") UUID parentId) {
    HierarchyMetrics metrics = hierarchyMetricsService.getHierarchyMetrics(parentId);
    return Response.ok(metrics).build();
  }

  /**
   * List customers with optional hierarchy filter.
   *
   * GET /api/customers?hierarchyType=HEADQUARTER
   */
  @GET
  public Response listCustomers(
      @QueryParam("hierarchyType") CustomerHierarchyType hierarchyType
  ) {
    List<Customer> customers;

    if (hierarchyType != null) {
      customers = customerRepository.findByHierarchyType(hierarchyType);
    } else {
      customers = customerRepository.listAll();
    }

    return Response.ok(customers.stream()
      .map(CustomerResponse::from)
      .toList()
    ).build();
  }
}
```

---

## 🔟 Test Specifications

### **Backend Tests (29 Tests)**

**BranchServiceTest.java (8 Tests):**
```java
@QuarkusTest
public class BranchServiceTest {

  @Inject BranchService branchService;
  @Inject CustomerRepository customerRepository;

  @Test
  @Transactional
  void testCreateBranch_Success() {
    // Given: Parent HEADQUARTER
    Customer parent = createHeadquarter();

    // When: Create branch
    CreateBranchRequest request = new CreateBranchRequest(
      "NH Hotel Berlin",
      new AddressRequest("Alexanderplatz 1", "10178", "Berlin", "DE"),
      "test-user"
    );
    Customer branch = branchService.createBranch(parent.getId(), request);

    // Then
    assertThat(branch).isNotNull();
    assertThat(branch.getHierarchyType()).isEqualTo(CustomerHierarchyType.FILIALE);
    assertThat(branch.getParentCustomer()).isEqualTo(parent);
    assertThat(branch.getXentralCustomerId()).isEqualTo(parent.getXentralCustomerId());
    assertThat(branch.getStatus()).isEqualTo(CustomerStatus.PROSPECT);
  }

  @Test
  void testCreateBranch_ParentNotFound_ThrowsException() {
    // When/Then
    assertThatThrownBy(() ->
      branchService.createBranch(UUID.randomUUID(), validRequest())
    ).isInstanceOf(BusinessException.class)
     .hasMessageContaining("Parent customer not found");
  }

  @Test
  @Transactional
  void testCreateBranch_ParentNotHeadquarter_ThrowsException() {
    // Given: Parent is FILIALE (not HEADQUARTER)
    Customer filiale = createFiliale();

    // When/Then
    assertThatThrownBy(() ->
      branchService.createBranch(filiale.getId(), validRequest())
    ).isInstanceOf(BusinessException.class)
     .hasMessageContaining("Only HEADQUARTER customers can have branches");
  }

  // ... 5 more tests
}
```

**XentralAddressMatcherTest.java (10 Tests):**
```java
@QuarkusTest
public class XentralAddressMatcherTest {

  @Inject XentralAddressMatcher addressMatcher;

  @Test
  void testMatchDeliveryAddress_ExactMatch() {
    // Given: Branch with address "Alexanderplatz 1, 10178 Berlin"
    Customer parent = createParentWithBranch("Alexanderplatz 1", "10178", "Berlin");
    String xentralAddress = "Alexanderplatz 1, 10178 Berlin";

    // When
    Customer matched = addressMatcher.matchDeliveryAddress(parent, xentralAddress);

    // Then: Should match branch (not parent)
    assertThat(matched.getHierarchyType()).isEqualTo(CustomerHierarchyType.FILIALE);
    assertThat(matched.getCompanyName()).contains("Berlin");
  }

  @Test
  void testMatchDeliveryAddress_FuzzyMatch_80Percent() {
    // Given: Branch "Alexanderplatz 1" vs. Xentral "Alexanderplatz 1a"
    Customer parent = createParentWithBranch("Alexanderplatz 1", "10178", "Berlin");
    String xentralAddress = "Alexanderplatz 1a, 10178 Berlin";

    // When
    Customer matched = addressMatcher.matchDeliveryAddress(parent, xentralAddress);

    // Then: Should still match (>80% similarity)
    assertThat(matched.getHierarchyType()).isEqualTo(CustomerHierarchyType.FILIALE);
  }

  @Test
  void testMatchDeliveryAddress_NoMatch_Fallback() {
    // Given: Branch "Alexanderplatz 1" vs. Xentral "Stachus 10" (different!)
    Customer parent = createParentWithBranch("Alexanderplatz 1", "10178", "Berlin");
    String xentralAddress = "Stachus 10, 80331 München";

    // When
    Customer matched = addressMatcher.matchDeliveryAddress(parent, xentralAddress);

    // Then: Should fallback to parent
    assertThat(matched.getHierarchyType()).isEqualTo(CustomerHierarchyType.HEADQUARTER);
    assertThat(matched).isEqualTo(parent);
  }

  // ... 7 more tests (normalization, multiple branches, edge cases)
}
```

**HierarchyMetricsServiceTest.java (6 Tests):**
```java
@QuarkusTest
public class HierarchyMetricsServiceTest {

  @Inject HierarchyMetricsService hierarchyMetricsService;

  @Test
  @Transactional
  void testGetHierarchyMetrics_CalculatesRollUp() {
    // Given: Parent with 3 branches (revenues: 100k, 80k, 50k)
    Customer parent = createParentWithBranches(
      branch("Berlin", 100000),
      branch("München", 80000),
      branch("Hamburg", 50000)
    );

    // When
    HierarchyMetrics metrics = hierarchyMetricsService.getHierarchyMetrics(parent.getId());

    // Then
    assertThat(metrics.totalRevenue()).isEqualByComparingTo("230000");
    assertThat(metrics.averageRevenue()).isEqualByComparingTo("76666.67");
    assertThat(metrics.branchCount()).isEqualTo(3);
  }

  @Test
  @Transactional
  void testGetHierarchyMetrics_CalculatesPercentages() {
    // Given: Parent with 2 branches (100k, 50k)
    Customer parent = createParentWithBranches(
      branch("Berlin", 100000),
      branch("München", 50000)
    );

    // When
    HierarchyMetrics metrics = hierarchyMetricsService.getHierarchyMetrics(parent.getId());

    // Then
    BranchRevenueDetail berlin = metrics.branches().get(0);
    BranchRevenueDetail munich = metrics.branches().get(1);

    assertThat(berlin.percentage()).isEqualByComparingTo("66.7");
    assertThat(munich.percentage()).isEqualByComparingTo("33.3");
  }

  // ... 4 more tests
}
```

### **Frontend Tests (13 Tests)**

**CreateBranchDialog.test.tsx (5 Tests):**
```typescript
describe('CreateBranchDialog', () => {
  it('renders with parent info', () => {
    const parent = mockHeadquarter();
    render(<CreateBranchDialog open={true} parentCustomer={parent} />);

    expect(screen.getByText(parent.companyName)).toBeInTheDocument();
    expect(screen.getByText(parent.xentralCustomerId)).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    render(<CreateBranchDialog open={true} parentCustomer={mockHeadquarter()} />);

    const submitButton = screen.getByText('Filiale anlegen');
    expect(submitButton).toBeDisabled();

    // Fill required fields
    await userEvent.type(screen.getByLabelText('Filialname *'), 'NH Hotel Berlin');
    await userEvent.type(screen.getByLabelText('Straße & Hausnummer *'), 'Alexanderplatz 1');
    await userEvent.type(screen.getByLabelText('PLZ *'), '10178');
    await userEvent.type(screen.getByLabelText('Stadt *'), 'Berlin');

    expect(submitButton).toBeEnabled();
  });

  it('calls API on submit', async () => {
    const { createBranch } = mockBranchApi();
    render(<CreateBranchDialog open={true} parentCustomer={mockHeadquarter()} />);

    // Fill and submit
    await fillBranchForm();
    await userEvent.click(screen.getByText('Filiale anlegen'));

    expect(createBranch).toHaveBeenCalledWith({
      parentId: expect.any(String),
      branchName: 'NH Hotel Berlin',
      address: {
        street: 'Alexanderplatz 1',
        zipCode: '10178',
        city: 'Berlin',
        country: 'DE',
      },
    });
  });

  // ... 2 more tests
});
```

**HierarchyDashboard.test.tsx (5 Tests):**
```typescript
describe('HierarchyDashboard', () => {
  it('displays roll-up metrics', () => {
    const metrics = mockHierarchyMetrics({
      totalRevenue: 230000,
      averageRevenue: 76666.67,
      branchCount: 3,
      totalOpenOpportunities: 12,
    });

    render(<HierarchyDashboard parent={mockHeadquarter()} />);

    expect(screen.getByText('230.000 €')).toBeInTheDocument();
    expect(screen.getByText('76.667 €')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('12')).toBeInTheDocument();
  });

  it('displays branches sorted by revenue', () => {
    const metrics = mockHierarchyMetrics({
      branches: [
        { branchName: 'Berlin', revenue: 100000 },
        { branchName: 'München', revenue: 80000 },
        { branchName: 'Hamburg', revenue: 50000 },
      ],
    });

    render(<HierarchyDashboard parent={mockHeadquarter()} />);

    const rows = screen.getAllByRole('row');
    expect(rows[1]).toHaveTextContent('Berlin');  // Highest revenue first
    expect(rows[2]).toHaveTextContent('München');
    expect(rows[3]).toHaveTextContent('Hamburg');
  });

  // ... 3 more tests
});
```

**HierarchyTreeView.test.tsx (3 Tests):**
```typescript
describe('HierarchyTreeView', () => {
  it('renders tree with parent and children', () => {
    const parent = mockHeadquarter({
      childCustomers: [
        mockBranch({ companyName: 'Filiale Berlin' }),
        mockBranch({ companyName: 'Filiale München' }),
      ],
    });

    render(<HierarchyTreeView rootCustomer={parent} />);

    expect(screen.getByText(parent.companyName)).toBeInTheDocument();
    expect(screen.getByText('Filiale Berlin')).toBeInTheDocument();
    expect(screen.getByText('Filiale München')).toBeInTheDocument();
  });

  it('navigates to customer on click', async () => {
    const { navigate } = mockRouter();
    const parent = mockHeadquarter({
      childCustomers: [mockBranch({ id: 'branch-1', companyName: 'Filiale Berlin' })],
    });

    render(<HierarchyTreeView rootCustomer={parent} />);

    await userEvent.click(screen.getByText('Filiale Berlin'));

    expect(navigate).toHaveBeenCalledWith('/customers/branch-1');
  });

  // ... 1 more test
});
```

---

## 🔗 RELATED DOCUMENTATION

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_7_DESIGN_DECISIONS.md`

**Trigger Document:**
→ `/docs/planung/TRIGGER_SPRINT_2_1_7_7.md`

**Related Sprints:**
- Sprint 2.1.7.4: Customer Status Architecture (hierarchyType Foundation)
- Sprint 2.1.7.2: Xentral Integration (Address-Matching nutzt Xentral-Daten)

---

**✅ TECHNICAL SPECIFICATION STATUS: 📋 COMPLETE**

**Letzte Aktualisierung:** 2025-10-21 (Initial Creation - vollständige Code-Beispiele)
