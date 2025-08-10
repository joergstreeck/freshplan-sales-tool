package de.freshplan.domain.search.service.dto;

/**
 * DTO for customer search results to avoid lazy loading issues.
 *
 * @since FC-005 PR4
 */
public class CustomerSearchDto {
  private String id;
  private String companyName;
  private String customerNumber;
  private String status;
  private String contactEmail;
  private String contactPhone;
  private Integer contactCount;

  // Constructors
  public CustomerSearchDto() {}

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

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getContactEmail() {
    return contactEmail;
  }

  public void setContactEmail(String contactEmail) {
    this.contactEmail = contactEmail;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public Integer getContactCount() {
    return contactCount;
  }

  public void setContactCount(Integer contactCount) {
    this.contactCount = contactCount;
  }
}
