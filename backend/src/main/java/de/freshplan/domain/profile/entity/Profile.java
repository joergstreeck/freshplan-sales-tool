package de.freshplan.domain.profile.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

/**
 * Profile entity representing a customer profile in the system.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
@Entity
@Table(name = "profiles")
public class Profile extends PanacheEntityBase {

  @Id @GeneratedValue @UuidGenerator private UUID id;

  @NotNull @Column(name = "customer_id", unique = true, nullable = false)
  private String customerId;

  @Column(name = "company_info", columnDefinition = "TEXT")
  private String companyInfo;

  @Column(name = "contact_info", columnDefinition = "TEXT")
  private String contactInfo;

  @Column(name = "financial_info", columnDefinition = "TEXT")
  private String financialInfo;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "created_by")
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Version
  @Column(name = "version")
  private Long version;

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getCompanyInfo() {
    return companyInfo;
  }

  public void setCompanyInfo(String companyInfo) {
    this.companyInfo = companyInfo;
  }

  public String getContactInfo() {
    return contactInfo;
  }

  public void setContactInfo(String contactInfo) {
    this.contactInfo = contactInfo;
  }

  public String getFinancialInfo() {
    return financialInfo;
  }

  public void setFinancialInfo(String financialInfo) {
    this.financialInfo = financialInfo;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
