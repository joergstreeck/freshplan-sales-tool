package de.freshplan.modules.leads.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new lead.
 *
 * <p>Supports B2B-specific fields for gastronomy businesses.
 *
 * <p><b>Sprint 2.1.6 Phase 5+:</b> Added nested `contact` object for structured contact data
 * (ADR-007 Option C).
 *
 * <p><b>Sprint 2.1.5:</b> Added `activities` array for first contact documentation.
 */
public class LeadCreateRequest {

  /** Nested DTO for structured contact data (Sprint 2.1.6 Phase 5+ - ADR-007). */
  public static class ContactData {
    @Size(max = 100)
    public String firstName;

    @Size(max = 100)
    public String lastName;

    @Email(message = "Invalid email format")
    @Size(max = 255)
    public String email;

    @Size(max = 50)
    public String phone;
  }

  /**
   * Nested DTO for activity data (Sprint 2.1.5 - Pre-Claim Logic / First Contact Documentation).
   */
  public static class ActivityData {
    @Size(max = 100)
    public String activityType; // FIRST_CONTACT_DOCUMENTED, NOTE, etc.

    // Date-only field (no time) for first contact documentation
    // Frontend sends "yyyy-MM-dd" format from date input
    @com.fasterxml.jackson.annotation.JsonFormat(
        pattern = "yyyy-MM-dd",
        shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    public LocalDate performedAt;

    @Size(max = 1000)
    public String summary;

    public Boolean countsAsProgress;

    public Map<String, Object> metadata; // channel, notes, etc.
  }

  @NotNull(message = "Company name is required") @Size(min = 1, max = 255, message = "Company name must be between 1 and 255 characters")
  public String companyName;

  // Progressive Profiling (Sprint 2.1.5) - Stage: VORMERKUNG (0), REGISTRIERUNG (1), QUALIFIZIERT
  // (2)
  public Integer stage; // 0=VORMERKUNG, 1=REGISTRIERUNG, 2=QUALIFIZIERT

  // Sprint 2.1.6 Phase 5+: Structured contact data (NEW - preferred)
  @Valid public ContactData contact;

  // Legacy fields (for backward compatibility with old clients)
  @Deprecated
  @Size(max = 255)
  public String contactPerson;

  @Deprecated
  @Email(message = "Invalid email format")
  @Size(max = 255)
  public String email;

  @Deprecated
  @Size(max = 50)
  public String phone;

  @Size(max = 255)
  public String website;

  // Address fields
  @Size(max = 255)
  public String street;

  @Size(max = 20)
  public String postalCode;

  @Size(max = 100)
  public String city;

  @Size(min = 2, max = 2, message = "Country code must be 2 characters (e.g., DE, CH)")
  public String countryCode = "DE";

  // B2B-specific fields
  @Size(max = 100)
  public String businessType; // Restaurant/Hotel/Kantinen/Catering

  @Size(max = 20)
  public String kitchenSize; // small/medium/large

  public Integer employeeCount;

  public BigDecimal estimatedVolume;

  @Size(max = 50)
  public String industry;

  // Lead source tracking
  @Size(max = 100)
  public String source; // web/email/phone/event/partner

  @Size(max = 255)
  public String sourceCampaign;

  // Sprint 2.1.5: Activities for first contact documentation (MESSE/TELEFON requires
  // FIRST_CONTACT_DOCUMENTED)
  @Valid public List<ActivityData> activities;
}
