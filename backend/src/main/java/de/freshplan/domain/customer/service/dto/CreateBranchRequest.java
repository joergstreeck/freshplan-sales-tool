package de.freshplan.domain.customer.service.dto;

import de.freshplan.domain.customer.entity.*;
import de.freshplan.domain.shared.BusinessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request DTO for creating a new branch (FILIALE) under a HEADQUARTER.
 *
 * <p>Sprint 2.1.7.7: Multi-Location Management - Extended DTO with Address and Contact
 *
 * <p>This DTO extends the basic customer creation with:
 *
 * <ul>
 *   <li>Nested AddressDto for primary location creation
 *   <li>Nested ContactDto for basic contact information
 * </ul>
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */
public record CreateBranchRequest(

    // ========== BASIC INFORMATION ==========
    @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must not exceed 255 characters")
        String companyName,
    @Size(max = 255, message = "Trading name must not exceed 255 characters") String tradingName,

    // ========== CLASSIFICATION ==========
    CustomerType customerType,
    BusinessType businessType,

    // ========== STATUS ==========
    CustomerStatus status,

    // ========== FINANCIAL ==========
    @DecimalMin(value = "0.0", message = "Expected annual volume must be positive")
        @Digits(integer = 10, fraction = 2, message = "Expected annual volume format is invalid")
        BigDecimal expectedAnnualVolume,

    // ========== ADDRESS (for Location creation) ==========
    @Valid AddressDto address,

    // ========== CONTACT INFO ==========
    @Valid ContactDto contact) {

  /**
   * Nested DTO for address data.
   *
   * <p>Used to create primary Location for the branch.
   */
  public record AddressDto(
      @Size(max = 255, message = "Street must not exceed 255 characters") String street,
      @Size(max = 20, message = "Postal code must not exceed 20 characters") String postalCode,
      @Size(max = 100, message = "City must not exceed 100 characters") String city,
      @Size(max = 2, message = "Country code must be 2 characters (ISO 3166-1 alpha-2)")
          String country) {}

  /**
   * Nested DTO for contact data.
   *
   * <p>Basic contact information for the branch.
   */
  public record ContactDto(
      @Size(max = 50, message = "Phone must not exceed 50 characters") String phone,
      @Email(message = "Invalid email format")
          @Size(max = 255, message = "Email must not exceed 255 characters")
          String email) {}

  /** Constructor with minimal required fields. */
  public CreateBranchRequest(String companyName) {
    this(
        companyName,
        null, // tradingName
        null, // customerType - defaults to UNTERNEHMEN
        null, // businessType
        null, // status - defaults to PROSPECT
        null, // expectedAnnualVolume
        null, // address
        null // contact
        );
  }

  /** Builder-style constructor for complex scenarios. */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String companyName;
    private String tradingName;
    private CustomerType customerType;
    private BusinessType businessType;
    private CustomerStatus status;
    private BigDecimal expectedAnnualVolume;
    private AddressDto address;
    private ContactDto contact;

    public Builder companyName(String companyName) {
      this.companyName = companyName;
      return this;
    }

    public Builder tradingName(String tradingName) {
      this.tradingName = tradingName;
      return this;
    }

    public Builder customerType(CustomerType customerType) {
      this.customerType = customerType;
      return this;
    }

    public Builder businessType(BusinessType businessType) {
      this.businessType = businessType;
      return this;
    }

    public Builder status(CustomerStatus status) {
      this.status = status;
      return this;
    }

    public Builder expectedAnnualVolume(BigDecimal expectedAnnualVolume) {
      this.expectedAnnualVolume = expectedAnnualVolume;
      return this;
    }

    public Builder address(AddressDto address) {
      this.address = address;
      return this;
    }

    public Builder address(String street, String postalCode, String city, String country) {
      this.address = new AddressDto(street, postalCode, city, country);
      return this;
    }

    public Builder contact(ContactDto contact) {
      this.contact = contact;
      return this;
    }

    public Builder contact(String phone, String email) {
      this.contact = new ContactDto(phone, email);
      return this;
    }

    public CreateBranchRequest build() {
      return new CreateBranchRequest(
          companyName,
          tradingName,
          customerType,
          businessType,
          status,
          expectedAnnualVolume,
          address,
          contact);
    }
  }
}
