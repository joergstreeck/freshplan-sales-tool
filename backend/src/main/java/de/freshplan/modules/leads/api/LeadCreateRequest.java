package de.freshplan.modules.leads.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Request DTO for creating a new lead. Supports B2B-specific fields for gastronomy businesses. */
public class LeadCreateRequest {

  @NotNull(message = "Company name is required") @Size(min = 1, max = 255, message = "Company name must be between 1 and 255 characters")
  public String companyName;

  @Size(max = 255)
  public String contactPerson;

  @Email(message = "Invalid email format")
  @Size(max = 255)
  public String email;

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
}
