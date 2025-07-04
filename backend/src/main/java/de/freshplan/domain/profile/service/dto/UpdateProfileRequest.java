package de.freshplan.domain.profile.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for updating an existing profile.
 * All fields are optional - only provided fields will be updated.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class UpdateProfileRequest {
    
    @JsonProperty("companyInfo")
    private CreateProfileRequest.CompanyInfo companyInfo;
    
    @JsonProperty("contactInfo")
    private CreateProfileRequest.ContactInfo contactInfo;
    
    @JsonProperty("financialInfo")
    private CreateProfileRequest.FinancialInfo financialInfo;
    
    @JsonProperty("notes")
    private String notes;
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UpdateProfileRequest request = new UpdateProfileRequest();
        
        public Builder companyInfo(CreateProfileRequest.CompanyInfo companyInfo) {
            request.companyInfo = companyInfo;
            return this;
        }
        
        public Builder contactInfo(CreateProfileRequest.ContactInfo contactInfo) {
            request.contactInfo = contactInfo;
            return this;
        }
        
        public Builder financialInfo(
                CreateProfileRequest.FinancialInfo financialInfo) {
            request.financialInfo = financialInfo;
            return this;
        }
        
        public Builder notes(String notes) {
            request.notes = notes;
            return this;
        }
        
        public UpdateProfileRequest build() {
            return request;
        }
    }
    
    // Getters and Setters
    public CreateProfileRequest.CompanyInfo getCompanyInfo() { 
        return companyInfo; 
    }
    
    public void setCompanyInfo(CreateProfileRequest.CompanyInfo companyInfo) { 
        this.companyInfo = companyInfo; 
    }
    
    public CreateProfileRequest.ContactInfo getContactInfo() { 
        return contactInfo; 
    }
    
    public void setContactInfo(CreateProfileRequest.ContactInfo contactInfo) { 
        this.contactInfo = contactInfo; 
    }
    
    public CreateProfileRequest.FinancialInfo getFinancialInfo() { 
        return financialInfo; 
    }
    
    public void setFinancialInfo(
            CreateProfileRequest.FinancialInfo financialInfo) { 
        this.financialInfo = financialInfo; 
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}