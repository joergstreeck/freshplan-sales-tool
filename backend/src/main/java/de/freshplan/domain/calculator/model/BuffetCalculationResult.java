package de.freshplan.domain.calculator.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Result of buffet calculation.
 * 
 * Contains net price, tax, and gross price for the buffet.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public final class BuffetCalculationResult {
    
    private final Integer persons;
    private final BuffetMenuType menuType;
    private final BigDecimal pricePerPerson;
    private final BigDecimal netTotal;
    private final BigDecimal taxAmount;
    private final BigDecimal grossTotal;
    private final double taxRate;
    
    /**
     * Creates a buffet calculation result.
     * 
     * @param persons number of persons
     * @param menuType buffet menu type
     * @param pricePerPerson price per person (net)
     * @param taxRate tax rate (e.g. 0.19 for 19%)
     */
    public BuffetCalculationResult(Integer persons, 
                                 BuffetMenuType menuType,
                                 BigDecimal pricePerPerson, 
                                 double taxRate) {
        this.persons = persons;
        this.menuType = menuType;
        this.pricePerPerson = pricePerPerson;
        this.taxRate = taxRate;
        
        // Calculate totals
        this.netTotal = pricePerPerson
                .multiply(BigDecimal.valueOf(persons))
                .setScale(2, RoundingMode.HALF_UP);
        
        this.taxAmount = netTotal
                .multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, RoundingMode.HALF_UP);
        
        this.grossTotal = netTotal
                .add(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public Integer getPersons() {
        return persons;
    }
    
    public BuffetMenuType getMenuType() {
        return menuType;
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
    
    @Override
    public String toString() {
        return String.format(
            "BuffetCalculationResult{persons=%d, menuType=%s, " +
            "netTotal=€%.2f, grossTotal=€%.2f}", 
            persons, menuType, netTotal, grossTotal
        );
    }
}