package de.freshplan.domain.opportunity.service.dto;

/**
 * DTO for converting a won Opportunity to a Customer.
 *
 * <p>Used in Opportunity → Customer conversion workflow. All fields are optional - company name
 * defaults to opportunity name if not provided.
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.6.2 Phase 2
 */
public class ConvertToCustomerRequest {

  /** Company name (optional - uses opportunity name if null) */
  private String companyName;

  /** Street address for main location */
  private String street;

  /** Postal code */
  private String postalCode;

  /** City */
  private String city;

  /** Country (defaults to "Deutschland" if null) */
  private String country;

  /** Whether to auto-create contact from lead data (defaults to true) */
  private Boolean createContactFromLead = true;

  // Constructors

  public ConvertToCustomerRequest() {}

  // Getters and Setters

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Boolean getCreateContactFromLead() {
    return createContactFromLead;
  }

  public void setCreateContactFromLead(Boolean createContactFromLead) {
    this.createContactFromLead = createContactFromLead;
  }

  // Helper Methods

  /**
   * Checks if request contains any address data.
   *
   * @return true if street, postal code, or city is provided
   */
  public boolean hasAddressData() {
    return street != null || postalCode != null || city != null;
  }

  /**
   * Checks if contact should be auto-created from lead data.
   *
   * @return true if createContactFromLead is true (default)
   */
  public boolean createContactFromLead() {
    return createContactFromLead != null && createContactFromLead;
  }

  // Builder Pattern

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final ConvertToCustomerRequest request = new ConvertToCustomerRequest();

    public Builder companyName(String companyName) {
      request.companyName = companyName;
      return this;
    }

    public Builder street(String street) {
      request.street = street;
      return this;
    }

    public Builder postalCode(String postalCode) {
      request.postalCode = postalCode;
      return this;
    }

    public Builder city(String city) {
      request.city = city;
      return this;
    }

    public Builder country(String country) {
      request.country = country;
      return this;
    }

    public Builder createContactFromLead(Boolean createContactFromLead) {
      request.createContactFromLead = createContactFromLead;
      return this;
    }

    public ConvertToCustomerRequest build() {
      return request;
    }
  }

  @Override
  public String toString() {
    return "ConvertToCustomerRequest{"
        + "companyName='"
        + companyName
        + '\''
        + ", city='"
        + city
        + '\''
        + ", createContactFromLead="
        + createContactFromLead
        + '}';
  }
}
