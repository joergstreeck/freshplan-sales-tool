package de.freshplan.domain.search.service.dto;

/**
 * DTO for lead contact search results to avoid lazy loading issues.
 *
 * <p>Parallel structure to ContactSearchDto for lead_contacts table.
 *
 * <p>Note: lead_contacts has 100% parity with customer_contacts (ADR-007 Option C).
 *
 * @since Lead Search Implementation
 */
public class LeadContactSearchDto {
  private String id; // Long converted to String
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private String position;
  private String leadId; // Foreign key to leads table
  private String customerId; // Alias for leadId (for frontend compatibility)
  private String leadName; // Company name from lead
  private Boolean isPrimary;

  // Constructors
  public LeadContactSearchDto() {}

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getLeadId() {
    return leadId;
  }

  public void setLeadId(String leadId) {
    this.leadId = leadId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getLeadName() {
    return leadName;
  }

  public void setLeadName(String leadName) {
    this.leadName = leadName;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public void setIsPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }
}
