package de.freshplan.domain.customer.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer address entity representing different addresses for a customer location.
 * Supports multiple address types (billing, shipping, etc.) per location.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(name = "customer_addresses", indexes = {
    @Index(name = "idx_address_location", columnList = "location_id"),
    @Index(name = "idx_address_type", columnList = "location_id, address_type"),
    @Index(name = "idx_address_postal", columnList = "postal_code"),
    @Index(name = "idx_address_city", columnList = "city"),
    @Index(name = "idx_address_deleted", columnList = "is_deleted")
})
public class CustomerAddress extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private CustomerLocation location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    private AddressType addressType;
    
    // Address Components
    @Column(name = "street", length = 255)
    private String street;
    
    @Column(name = "street_number", length = 20)
    private String streetNumber;
    
    @Column(name = "additional_line", length = 255)
    private String additionalLine;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    @Column(name = "state_province", length = 100)
    private String stateProvince;
    
    @Column(name = "country", nullable = false, length = 3)
    private String country = "DEU"; // ISO 3166-1 alpha-3 country code
    
    // Additional Information
    @Column(name = "po_box", length = 50)
    private String poBox;
    
    @Column(name = "care_of", length = 100)
    private String careOf; // "c/o" field
    
    @Column(name = "building_name", length = 100)
    private String buildingName;
    
    @Column(name = "floor_apartment", length = 50)
    private String floorApartment;
    
    // Geocoding Information
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "geocoded_at")
    private LocalDateTime geocodedAt;
    
    // Validation Status
    @Column(name = "is_validated", nullable = false)
    private Boolean isValidated = false;
    
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
    
    @Column(name = "validation_service", length = 50)
    private String validationService;
    
    // Status Flags
    @Column(name = "is_primary_for_type", nullable = false)
    private Boolean isPrimaryForType = false;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Special Delivery Instructions
    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;
    
    @Column(name = "access_instructions", columnDefinition = "TEXT")
    private String accessInstructions;
    
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
        if (isPrimaryForType == null) {
            isPrimaryForType = false;
        }
        if (isValidated == null) {
            isValidated = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business Methods
    
    /**
     * Gets the full formatted address as a single string.
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        
        if (street != null && !street.isBlank()) {
            sb.append(street);
            if (streetNumber != null && !streetNumber.isBlank()) {
                sb.append(" ").append(streetNumber);
            }
            sb.append("\n");
        }
        
        if (additionalLine != null && !additionalLine.isBlank()) {
            sb.append(additionalLine).append("\n");
        }
        
        if (poBox != null && !poBox.isBlank()) {
            sb.append("Postfach ").append(poBox).append("\n");
        }
        
        if (postalCode != null && !postalCode.isBlank()) {
            sb.append(postalCode).append(" ");
        }
        
        if (city != null && !city.isBlank()) {
            sb.append(city);
        }
        
        if (stateProvince != null && !stateProvince.isBlank()) {
            sb.append(", ").append(stateProvince);
        }
        
        if (country != null && !country.equals("DEU")) {
            sb.append("\n").append(country);
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Gets a single-line formatted address.
     */
    public String getFormattedAddressOneLine() {
        return getFormattedAddress().replace("\n", ", ");
    }
    
    /**
     * Checks if this address has geocoding information.
     */
    public boolean hasGeoLocation() {
        return latitude != null && longitude != null;
    }
    
    /**
     * Sets geocoding information.
     */
    public void setGeoLocation(Double latitude, Double longitude, String service) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.geocodedAt = LocalDateTime.now();
        this.validationService = service;
    }
    
    /**
     * Marks address as validated.
     */
    public void markAsValidated(String service) {
        this.isValidated = true;
        this.validatedAt = LocalDateTime.now();
        this.validationService = service;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CustomerLocation getLocation() {
        return location;
    }

    public void setLocation(CustomerLocation location) {
        this.location = location;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getAdditionalLine() {
        return additionalLine;
    }

    public void setAdditionalLine(String additionalLine) {
        this.additionalLine = additionalLine;
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

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getCareOf() {
        return careOf;
    }

    public void setCareOf(String careOf) {
        this.careOf = careOf;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getFloorApartment() {
        return floorApartment;
    }

    public void setFloorApartment(String floorApartment) {
        this.floorApartment = floorApartment;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getGeocodedAt() {
        return geocodedAt;
    }

    public void setGeocodedAt(LocalDateTime geocodedAt) {
        this.geocodedAt = geocodedAt;
    }

    public Boolean getIsValidated() {
        return isValidated;
    }

    public void setIsValidated(Boolean isValidated) {
        this.isValidated = isValidated;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getValidationService() {
        return validationService;
    }

    public void setValidationService(String validationService) {
        this.validationService = validationService;
    }

    public Boolean getIsPrimaryForType() {
        return isPrimaryForType;
    }

    public void setIsPrimaryForType(Boolean isPrimaryForType) {
        this.isPrimaryForType = isPrimaryForType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @Override
    public String toString() {
        return "CustomerAddress{" +
                "id=" + id +
                ", addressType=" + addressType +
                ", street='" + street + '\'' +
                ", streetNumber='" + streetNumber + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}