package de.freshplan.domain.customer.service.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for customer location information.
 *
 * @since 2.0.0
 */
public class LocationDto {

  private UUID id;
  private UUID customerId;
  private String locationName;
  private String locationCode;

  // Address
  private String street;
  private String houseNumber;
  private String postalCode;
  private String city;

  // Contact
  private String phone;
  private String email;

  // Status
  private Boolean isMainLocation;
  private Boolean isActive;

  // Sprint 2 fields
  private Map<String, Object> serviceOfferings;
  private Map<String, Object> locationDetails;

  // Metadata
  private LocalDateTime createdAt;
  private String createdBy;
  private LocalDateTime updatedAt;
  private String updatedBy;

  // Constructors
  public LocationDto() {}

  // Builder pattern
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private LocationDto dto = new LocationDto();

    public Builder id(UUID id) {
      dto.id = id;
      return this;
    }

    public Builder customerId(UUID customerId) {
      dto.customerId = customerId;
      return this;
    }

    public Builder locationName(String locationName) {
      dto.locationName = locationName;
      return this;
    }

    public Builder address(String street, String houseNumber, String postalCode, String city) {
      dto.street = street;
      dto.houseNumber = houseNumber;
      dto.postalCode = postalCode;
      dto.city = city;
      return this;
    }

    public Builder contact(String phone, String email) {
      dto.phone = phone;
      dto.email = email;
      return this;
    }

    public Builder isMainLocation(Boolean isMainLocation) {
      dto.isMainLocation = isMainLocation;
      return this;
    }

    public Builder isActive(Boolean isActive) {
      dto.isActive = isActive;
      return this;
    }

    public Builder serviceOfferings(Map<String, Object> serviceOfferings) {
      dto.serviceOfferings = serviceOfferings;
      return this;
    }

    public Builder locationDetails(Map<String, Object> locationDetails) {
      dto.locationDetails = locationDetails;
      return this;
    }

    public LocationDto build() {
      return dto;
    }
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
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

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getHouseNumber() {
    return houseNumber;
  }

  public void setHouseNumber(String houseNumber) {
    this.houseNumber = houseNumber;
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

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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
}
