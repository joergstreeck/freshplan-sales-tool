package de.freshplan.domain.profile.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for creating a new profile.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
public class CreateProfileRequest {
    
    @NotBlank(message = "Customer ID is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", 
             message = "Customer ID must contain only uppercase letters, numbers and hyphens")
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("companyInfo")
    private CompanyInfo companyInfo;
    
    @JsonProperty("contactInfo")
    private ContactInfo contactInfo;
    
    @JsonProperty("financialInfo")
    private FinancialInfo financialInfo;
    
    @JsonProperty("notes")
    private String notes;
    
    // Nested DTOs
    
    public static class CompanyInfo {
        private String name;
        private String registrationNumber;
        private String vatNumber;
        private String industry;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRegistrationNumber() { 
            return registrationNumber; 
        }
        
        public void setRegistrationNumber(String registrationNumber) { 
            this.registrationNumber = registrationNumber; 
        }
        
        public String getVatNumber() { return vatNumber; }
        public void setVatNumber(String vatNumber) { this.vatNumber = vatNumber; }
        
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
    }
    
    public static class ContactInfo {
        private String primaryContact;
        private String email;
        private String phone;
        private String address;
        
        // Getters and Setters
        public String getPrimaryContact() { 
            return primaryContact; 
        }
        
        public void setPrimaryContact(String primaryContact) { 
            this.primaryContact = primaryContact; 
        }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
    
    public static class FinancialInfo {
        private String paymentTerms;
        private String creditLimit;
        private String taxExempt;
        
        // Getters and Setters
        public String getPaymentTerms() { 
            return paymentTerms; 
        }
        
        public void setPaymentTerms(String paymentTerms) { 
            this.paymentTerms = paymentTerms; 
        }
        
        public String getCreditLimit() { 
            return creditLimit; 
        }
        
        public void setCreditLimit(String creditLimit) { 
            this.creditLimit = creditLimit; 
        }
        
        public String getTaxExempt() { return taxExempt; }
        public void setTaxExempt(String taxExempt) { this.taxExempt = taxExempt; }
    }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final CreateProfileRequest request = new CreateProfileRequest();
        
        public Builder customerId(String customerId) {
            request.customerId = customerId;
            return this;
        }
        
        public Builder companyInfo(CompanyInfo companyInfo) {
            request.companyInfo = companyInfo;
            return this;
        }
        
        public Builder contactInfo(ContactInfo contactInfo) {
            request.contactInfo = contactInfo;
            return this;
        }
        
        public Builder financialInfo(FinancialInfo financialInfo) {
            request.financialInfo = financialInfo;
            return this;
        }
        
        public Builder notes(String notes) {
            request.notes = notes;
            return this;
        }
        
        public CreateProfileRequest build() {
            return request;
        }
    }
    
    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public CompanyInfo getCompanyInfo() { return companyInfo; }
    public void setCompanyInfo(CompanyInfo companyInfo) { 
        this.companyInfo = companyInfo; 
    }
    
    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfo contactInfo) { 
        this.contactInfo = contactInfo; 
    }
    
    public FinancialInfo getFinancialInfo() { return financialInfo; }
    public void setFinancialInfo(FinancialInfo financialInfo) { 
        this.financialInfo = financialInfo; 
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}