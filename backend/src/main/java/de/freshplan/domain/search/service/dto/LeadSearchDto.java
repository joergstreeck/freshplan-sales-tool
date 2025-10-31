package de.freshplan.domain.search.service.dto;

/**
 * DTO for lead search results to avoid lazy loading issues.
 *
 * <p>Parallel structure to CustomerSearchDto for leads table.
 *
 * @since Lead Search Implementation
 */
public class LeadSearchDto {
  private String id; // Long converted to String
  private String companyName;
  private String status; // LeadStatus as String
  private String stage; // LeadStage as String
  private String email; // Lead has direct email field
  private String phone; // Lead has direct phone field
  private Integer contactCount;

  // Constructors
  public LeadSearchDto() {}

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStage() {
    return stage;
  }

  public void setStage(String stage) {
    this.stage = stage;
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

  public Integer getContactCount() {
    return contactCount;
  }

  public void setContactCount(Integer contactCount) {
    this.contactCount = contactCount;
  }
}
