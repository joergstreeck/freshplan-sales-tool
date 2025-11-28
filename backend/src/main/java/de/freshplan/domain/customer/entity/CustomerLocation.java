package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

/**
 * Customer location entity representing different locations/sites of a customer. Each location can
 * have multiple addresses (billing, shipping, etc.).
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@SuppressWarnings("PMD.CyclomaticComplexity") // Entity with many fields - inherent complexity
@Entity
@Table(
    name = "customer_locations",
    indexes = {
      @Index(name = "idx_location_customer", columnList = "customer_id"),
      @Index(name = "idx_location_main", columnList = "customer_id, is_main_location"),
      @Index(name = "idx_location_category", columnList = "category"),
      @Index(name = "idx_location_deleted", columnList = "is_deleted")
    })
public class CustomerLocation extends PanacheEntityBase {

  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(name = "location_name", nullable = false, length = 255)
  private String locationName;

  @Column(name = "location_code", length = 50)
  private String locationCode; // Internal code for this location

  @Enumerated(EnumType.STRING)
  @Column(name = "category", length = 30)
  private LocationCategory category;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  // Addresses - One location can have multiple addresses
  @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CustomerAddress> addresses = new ArrayList<>();

  // Operational Information
  @Column(name = "operating_hours", length = 500)
  private String operatingHours; // JSON or structured text

  @Column(name = "time_zone", length = 50)
  private String timeZone;

  @Column(name = "phone", length = 50)
  private String phone;

  @Column(name = "fax", length = 50)
  private String fax;

  @Column(name = "email", length = 255)
  private String email;

  // Business Information
  @Column(name = "tax_number", length = 50)
  private String taxNumber;

  @Column(name = "vat_number", length = 50)
  private String vatNumber;

  @Column(name = "commercial_register", length = 100)
  private String commercialRegister;

  // Status Flags
  @Column(name = "is_main_location", nullable = false)
  private Boolean isMainLocation = false;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "is_billing_location", nullable = false)
  private Boolean isBillingLocation = false;

  @Column(name = "is_shipping_location", nullable = false)
  private Boolean isShippingLocation = false;

  // Special Instructions
  @Column(name = "delivery_instructions", columnDefinition = "TEXT")
  private String deliveryInstructions;

  @Column(name = "access_instructions", columnDefinition = "TEXT")
  private String accessInstructions;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  // Sprint 2: Service Offerings and Location Details as JSON
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "service_offerings", columnDefinition = "jsonb")
  private Map<String, Object> serviceOfferings = new HashMap<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "location_details", columnDefinition = "jsonb")
  private Map<String, Object> locationDetails = new HashMap<>();

  // Soft Delete
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  // Audit Fields
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "created_by", nullable = false, updatable = false, length = 100)
  private String createdBy;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "updated_by", length = 100)
  private String updatedBy;

  // Lifecycle Methods
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (isDeleted == null) {
      isDeleted = false;
    }
    if (isActive == null) {
      isActive = true;
    }
    if (isMainLocation == null) {
      isMainLocation = false;
    }
    if (isBillingLocation == null) {
      isBillingLocation = false;
    }
    if (isShippingLocation == null) {
      isShippingLocation = false;
    }
    // Initialize Sprint 2 fields
    if (serviceOfferings == null) {
      serviceOfferings = new HashMap<>();
    }
    if (locationDetails == null) {
      locationDetails = new HashMap<>();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Business Methods

  /** Gets the primary billing address for this location. */
  public Optional<CustomerAddress> getPrimaryBillingAddress() {
    return addresses.stream()
        .filter(addr -> !Boolean.TRUE.equals(addr.getIsDeleted()))
        .filter(addr -> addr.getAddressType() == AddressType.BILLING)
        .filter(addr -> Boolean.TRUE.equals(addr.getIsPrimaryForType()))
        .findFirst();
  }

  /** Gets the primary shipping address for this location. */
  public Optional<CustomerAddress> getPrimaryShippingAddress() {
    return addresses.stream()
        .filter(addr -> !Boolean.TRUE.equals(addr.getIsDeleted()))
        .filter(addr -> addr.getAddressType() == AddressType.SHIPPING)
        .filter(addr -> Boolean.TRUE.equals(addr.getIsPrimaryForType()))
        .findFirst();
  }

  /** Gets all active addresses of a specific type. */
  public List<CustomerAddress> getAddressesByType(AddressType type) {
    return addresses.stream()
        .filter(addr -> !Boolean.TRUE.equals(addr.getIsDeleted()))
        .filter(addr -> addr.getAddressType() == type)
        .toList();
  }

  /** Gets all active addresses. */
  public List<CustomerAddress> getActiveAddresses() {
    return addresses.stream()
        .filter(addr -> !Boolean.TRUE.equals(addr.getIsDeleted()))
        .filter(addr -> Boolean.TRUE.equals(addr.getIsActive()))
        .toList();
  }

  /** Adds an address to this location. */
  public void addAddress(CustomerAddress address) {
    if (address != null) {
      address.setLocation(this);
      this.addresses.add(address);
    }
  }

  /** Removes an address from this location (soft delete). */
  public void removeAddress(CustomerAddress address, String deletedBy) {
    if (address != null && addresses.contains(address)) {
      address.setIsDeleted(true);
      address.setDeletedAt(LocalDateTime.now());
      address.setDeletedBy(deletedBy);
    }
  }

  /**
   * Sets this location as the main location. Note: Business logic should ensure only one main
   * location per customer.
   */
  public void setAsMainLocation(String updatedBy) {
    this.isMainLocation = true;
    this.updatedBy = updatedBy;
  }

  /** Removes main location status. */
  public void removeMainLocationStatus(String updatedBy) {
    this.isMainLocation = false;
    this.updatedBy = updatedBy;
  }

  /** Gets a display name for this location. */
  public String getDisplayName() {
    if (locationName != null && !locationName.isBlank()) {
      if (Boolean.TRUE.equals(isMainLocation)) {
        return locationName + " (Hauptstandort)";
      }
      return locationName;
    }
    return "Unbenannter Standort";
  }

  /** Checks if this location has any active addresses. */
  public boolean hasActiveAddresses() {
    return addresses.stream()
        .anyMatch(
            addr ->
                !Boolean.TRUE.equals(addr.getIsDeleted())
                    && Boolean.TRUE.equals(addr.getIsActive()));
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String locationName) {
    this.locationName = locationName;
  }

  public String getLocationCode() {
    return locationCode;
  }

  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode;
  }

  public LocationCategory getCategory() {
    return category;
  }

  public void setCategory(LocationCategory category) {
    this.category = category;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<CustomerAddress> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<CustomerAddress> addresses) {
    this.addresses = addresses;
  }

  public String getOperatingHours() {
    return operatingHours;
  }

  public void setOperatingHours(String operatingHours) {
    this.operatingHours = operatingHours;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getFax() {
    return fax;
  }

  public void setFax(String fax) {
    this.fax = fax;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTaxNumber() {
    return taxNumber;
  }

  public void setTaxNumber(String taxNumber) {
    this.taxNumber = taxNumber;
  }

  public String getVatNumber() {
    return vatNumber;
  }

  public void setVatNumber(String vatNumber) {
    this.vatNumber = vatNumber;
  }

  public String getCommercialRegister() {
    return commercialRegister;
  }

  public void setCommercialRegister(String commercialRegister) {
    this.commercialRegister = commercialRegister;
  }

  public Boolean getIsMainLocation() {
    return isMainLocation;
  }

  public void setIsMainLocation(Boolean isMainLocation) {
    this.isMainLocation = isMainLocation;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public Boolean getIsBillingLocation() {
    return isBillingLocation;
  }

  public void setIsBillingLocation(Boolean isBillingLocation) {
    this.isBillingLocation = isBillingLocation;
  }

  public Boolean getIsShippingLocation() {
    return isShippingLocation;
  }

  public void setIsShippingLocation(Boolean isShippingLocation) {
    this.isShippingLocation = isShippingLocation;
  }

  public String getDeliveryInstructions() {
    return deliveryInstructions;
  }

  public void setDeliveryInstructions(String deliveryInstructions) {
    this.deliveryInstructions = deliveryInstructions;
  }

  public String getAccessInstructions() {
    return accessInstructions;
  }

  public void setAccessInstructions(String accessInstructions) {
    this.accessInstructions = accessInstructions;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Boolean getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  public LocalDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  // Sprint 2 Getters/Setters
  public Map<String, Object> getServiceOfferings() {
    return serviceOfferings;
  }

  public void setServiceOfferings(Map<String, Object> serviceOfferings) {
    this.serviceOfferings = serviceOfferings;
  }

  public Map<String, Object> getLocationDetails() {
    return locationDetails;
  }

  public void setLocationDetails(Map<String, Object> locationDetails) {
    this.locationDetails = locationDetails;
  }

  @Override
  public String toString() {
    return "CustomerLocation{"
        + "id="
        + id
        + ", locationName='"
        + locationName
        + '\''
        + ", locationCode='"
        + locationCode
        + '\''
        + ", category="
        + category
        + ", isMainLocation="
        + isMainLocation
        + ", isActive="
        + isActive
        + '}';
  }
}
