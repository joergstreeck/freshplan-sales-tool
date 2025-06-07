package de.freshplan.domain.calculator.service.dto;

import de.freshplan.domain.calculator.model.BuffetMenuType;

import java.math.BigDecimal;

/**
 * REST API response DTO for buffet calculation result.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class BuffetQuoteResponse {
    
    private final Integer persons;
    private final BuffetMenuType menuType;
    private final String menuDisplayName;
    private final BigDecimal pricePerPerson;
    private final BigDecimal netTotal;
    private final BigDecimal taxAmount;
    private final BigDecimal grossTotal;
    private final double taxRate;
    
    public BuffetQuoteResponse(Integer persons,
                             BuffetMenuType menuType,
                             String menuDisplayName, 
                             BigDecimal pricePerPerson,
                             BigDecimal netTotal,
                             BigDecimal taxAmount,
                             BigDecimal grossTotal,
                             double taxRate) {
        this.persons = persons;
        this.menuType = menuType;
        this.menuDisplayName = menuDisplayName;
        this.pricePerPerson = pricePerPerson;
        this.netTotal = netTotal;
        this.taxAmount = taxAmount;
        this.grossTotal = grossTotal;
        this.taxRate = taxRate;
    }
    
    public Integer getPersons() {
        return persons;
    }
    
    public BuffetMenuType getMenuType() {
        return menuType;
    }
    
    public String getMenuDisplayName() {
        return menuDisplayName;
    }
    
    public BigDecimal getPricePerPerson() {
        return pricePerPerson;
    }
    
    public BigDecimal getNetTotal() {
        return netTotal;
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    
    public BigDecimal getGrossTotal() {
        return grossTotal;
    }
    
    public double getTaxRate() {
        return taxRate;
    }
    
    /**
     * Builder for BuffetQuoteResponse.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Integer persons;
        private BuffetMenuType menuType;
        private String menuDisplayName;
        private BigDecimal pricePerPerson;
        private BigDecimal netTotal;
        private BigDecimal taxAmount;
        private BigDecimal grossTotal;
        private double taxRate;
        
        public Builder persons(Integer persons) {
            this.persons = persons;
            return this;
        }
        
        public Builder menuType(BuffetMenuType menuType) {
            this.menuType = menuType;
            return this;
        }
        
        public Builder menuDisplayName(String menuDisplayName) {
            this.menuDisplayName = menuDisplayName;
            return this;
        }
        
        public Builder pricePerPerson(BigDecimal pricePerPerson) {
            this.pricePerPerson = pricePerPerson;
            return this;
        }
        
        public Builder netTotal(BigDecimal netTotal) {
            this.netTotal = netTotal;
            return this;
        }
        
        public Builder taxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }
        
        public Builder grossTotal(BigDecimal grossTotal) {
            this.grossTotal = grossTotal;
            return this;
        }
        
        public Builder taxRate(double taxRate) {
            this.taxRate = taxRate;
            return this;
        }
        
        public BuffetQuoteResponse build() {
            return new BuffetQuoteResponse(
                persons, menuType, menuDisplayName, pricePerPerson,
                netTotal, taxAmount, grossTotal, taxRate
            );
        }
    }
}