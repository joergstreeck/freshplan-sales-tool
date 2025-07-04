package de.freshplan.domain.profile.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for profile responses.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class ProfileResponse {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("companyInfo")
    private CreateProfileRequest.CompanyInfo companyInfo;
    
    @JsonProperty("contactInfo")
    private CreateProfileRequest.ContactInfo contactInfo;
    
    @JsonProperty("financialInfo")
    private CreateProfileRequest.FinancialInfo financialInfo;
    
    @JsonProperty("notes")
    private String notes;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("createdBy")
    private String createdBy;
    
    @JsonProperty("updatedBy")
    private String updatedBy;
    
    @JsonProperty("version")
    private Long version;
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ProfileResponse response = new ProfileResponse();
        
        public Builder id(UUID id) {
            response.id = id;
            return this;
        }
        
        public Builder customerId(String customerId) {
            response.customerId = customerId;
            return this;
        }
        
        public Builder companyInfo(CreateProfileRequest.CompanyInfo companyInfo) {
            response.companyInfo = companyInfo;
            return this;
        }
        
        public Builder contactInfo(CreateProfileRequest.ContactInfo contactInfo) {
            response.contactInfo = contactInfo;
            return this;
        }
        
        public Builder financialInfo(
                CreateProfileRequest.FinancialInfo financialInfo) {
            response.financialInfo = financialInfo;
            return this;
        }
        
        public Builder notes(String notes) {
            response.notes = notes;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            response.createdBy = createdBy;
            return this;
        }
        
        public Builder updatedBy(String updatedBy) {
            response.updatedBy = updatedBy;
            return this;
        }
        
        public Builder version(Long version) {
            response.version = version;
            return this;
        }
        
        public ProfileResponse build() {
            return response;
        }
    }
    
    // Getters
    public UUID getId() { return id; }
    public String getCustomerId() { return customerId; }
    public CreateProfileRequest.CompanyInfo getCompanyInfo() { 
        return companyInfo; 
    }
    public CreateProfileRequest.ContactInfo getContactInfo() { 
        return contactInfo; 
    }
    public CreateProfileRequest.FinancialInfo getFinancialInfo() { 
        return financialInfo; 
    }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public Long getVersion() { return version; }
}