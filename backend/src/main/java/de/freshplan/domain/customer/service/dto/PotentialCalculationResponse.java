package de.freshplan.domain.customer.service.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for customer sales potential calculation.
 * Includes monthly/yearly potential and quick win opportunities.
 * 
 * @since 2.0.0
 */
public class PotentialCalculationResponse {
    
    private BigDecimal monthlyPotential;
    private BigDecimal yearlyPotential;
    private List<QuickWinDto> quickWins = new ArrayList<>();
    private String calculationNote;
    
    // Constructors
    public PotentialCalculationResponse() {}
    
    public PotentialCalculationResponse(
            BigDecimal monthlyPotential,
            BigDecimal yearlyPotential) {
        this.monthlyPotential = monthlyPotential;
        this.yearlyPotential = yearlyPotential;
    }
    
    // Builder methods
    public PotentialCalculationResponse withQuickWins(List<QuickWinDto> quickWins) {
        this.quickWins = quickWins;
        return this;
    }
    
    public PotentialCalculationResponse withCalculationNote(String note) {
        this.calculationNote = note;
        return this;
    }
    
    public PotentialCalculationResponse addQuickWin(QuickWinDto quickWin) {
        if (this.quickWins == null) {
            this.quickWins = new ArrayList<>();
        }
        this.quickWins.add(quickWin);
        return this;
    }
    
    // Helper methods
    public BigDecimal getTotalQuickWinPotential() {
        if (quickWins == null || quickWins.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return quickWins.stream()
                .map(QuickWinDto::getPotential)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public int getQuickWinCount() {
        return quickWins != null ? quickWins.size() : 0;
    }
    
    // Getters and Setters
    public BigDecimal getMonthlyPotential() {
        return monthlyPotential;
    }
    
    public void setMonthlyPotential(BigDecimal monthlyPotential) {
        this.monthlyPotential = monthlyPotential;
    }
    
    public BigDecimal getYearlyPotential() {
        return yearlyPotential;
    }
    
    public void setYearlyPotential(BigDecimal yearlyPotential) {
        this.yearlyPotential = yearlyPotential;
    }
    
    public List<QuickWinDto> getQuickWins() {
        return quickWins;
    }
    
    public void setQuickWins(List<QuickWinDto> quickWins) {
        this.quickWins = quickWins;
    }
    
    public String getCalculationNote() {
        return calculationNote;
    }
    
    public void setCalculationNote(String calculationNote) {
        this.calculationNote = calculationNote;
    }
    
    /**
     * Quick Win opportunity within the potential calculation.
     */
    public static class QuickWinDto {
        private String title;
        private BigDecimal potential;
        private String product;
        private String description;
        private Integer priority; // 1 = highest priority
        
        // Constructors
        public QuickWinDto() {}
        
        public QuickWinDto(String title, BigDecimal potential, String product) {
            this.title = title;
            this.potential = potential;
            this.product = product;
        }
        
        // Builder methods
        public QuickWinDto withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public QuickWinDto withPriority(Integer priority) {
            this.priority = priority;
            return this;
        }
        
        // Getters and Setters
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public BigDecimal getPotential() {
            return potential;
        }
        
        public void setPotential(BigDecimal potential) {
            this.potential = potential;
        }
        
        public String getProduct() {
            return product;
        }
        
        public void setProduct(String product) {
            this.product = product;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public Integer getPriority() {
            return priority;
        }
        
        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }
}