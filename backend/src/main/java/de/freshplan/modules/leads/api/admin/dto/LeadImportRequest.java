package de.freshplan.modules.leads.api.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Bestandsleads-Migration Import Request (Modul 08 - Admin).
 *
 * <p>Batch-Import für historische Leads mit max. 1000 Leads/Batch. Dry-Run Mode PFLICHT für
 * Validierung vor Real-Import.
 *
 * <p>Sprint 2.1.6 - User Story 1
 */
public class LeadImportRequest {

  @NotNull(message = "dryRun flag is required") public Boolean dryRun;

  @NotEmpty(message = "leads list cannot be empty")
  @Size(max = 1000, message = "Maximum 1000 leads per batch")
  @Valid
  public List<LeadImportData> leads;

  /** Request hash for idempotency (auto-generated from leads data). */
  public String requestHash;

  public static class LeadImportData {

    // Company Information
    @NotNull(message = "companyName is required") @Size(max = 255, message = "companyName max 255 characters")
    public String companyName;

    @Size(max = 255)
    public String contactPerson;

    @Size(max = 255)
    public String email;

    @Size(max = 50)
    public String phone;

    @Size(max = 255)
    public String website;

    // Address
    @Size(max = 255)
    public String street;

    @Size(max = 20)
    public String postalCode;

    @Size(max = 100)
    public String city;

    @Size(max = 2)
    public String countryCode;

    // Business Details
    @Size(max = 100)
    public String businessType;

    @Size(max = 20)
    public String kitchenSize;

    public Integer employeeCount;

    public java.math.BigDecimal estimatedVolume;

    // Historical Dates (KRITISCH!)
    @NotNull(message = "registeredAt is required for Bestandsleads") public java.time.LocalDateTime registeredAt;

    /** User ID des ursprünglichen Owners (falls bekannt). */
    @Size(max = 50)
    public String ownerUserId;

    /** Territory Code (z.B. "DE-BY", "CH-ZH"). */
    @Size(max = 10)
    public String territoryCode;

    // Activities
    @Valid public List<ActivityImportData> activities;

    // Metadata
    @Size(max = 100)
    public String source; // "BESTAND_IMPORT", "MANUAL_IMPORT"

    @Size(max = 255)
    public String sourceCampaign;

    /** Importgrund für Audit-Trail. */
    @NotNull(message = "importReason is required") @Size(max = 250)
    public String importReason;
  }

  public static class ActivityImportData {

    @NotNull(message = "activityType is required") @Size(max = 50)
    public String activityType;

    @Size(max = 500)
    public String summary;

    @NotNull(message = "activityDate is required") public java.time.LocalDateTime activityDate;

    /** MUSS explizit gesetzt werden (NICHT automatisch berechnen!). */
    @NotNull(message = "countsAsProgress must be set explicitly") public Boolean countsAsProgress;

    @Size(max = 50)
    public String performedBy;
  }
}
