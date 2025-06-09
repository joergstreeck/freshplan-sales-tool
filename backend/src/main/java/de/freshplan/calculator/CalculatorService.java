package de.freshplan.calculator;

import de.freshplan.calculator.dto.CalculatorRequest;
import de.freshplan.calculator.dto.CalculatorResponse;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for discount calculations based on FreshPlan business rules.
 * 
 * Implements the official FreshPlan discount system as documented in
 * "Anlage 1 FreshPlan Rabattsystem für Endkunden".
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
public class CalculatorService {
    
    /**
     * Calculate discount based on FreshPlan business rules.
     */
    public CalculatorResponse calculate(CalculatorRequest request) {
        double orderValue = request.getOrderValue();
        int leadTime = request.getLeadTime();
        boolean pickup = request.isPickup();
        
        // 1. Basisrabatt nach Bestellwert
        double baseDiscount = calculateBaseDiscount(orderValue);
        
        // 2. Frühbucherrabatt nach Vorlaufzeit
        double earlyDiscount = calculateEarlyBookingDiscount(leadTime);
        
        // 3. Abholrabatt (nur ab 5.000 EUR)
        double pickupDiscount = 0.0;
        if (pickup && orderValue >= 5000) {
            pickupDiscount = 2.0;
        }
        
        // 4. Gesamtrabatt (kumuliert)
        double totalDiscount = baseDiscount + earlyDiscount + pickupDiscount;
        
        // 5. Beträge berechnen
        double discountAmount = orderValue * (totalDiscount / 100);
        double finalPrice = orderValue - discountAmount;
        
        return CalculatorResponse.builder()
            .orderValue(orderValue)
            .leadTime(leadTime)
            .pickup(pickup)
            .baseDiscount(baseDiscount)
            .earlyDiscount(earlyDiscount)
            .pickupDiscount(pickupDiscount)
            .totalDiscount(totalDiscount)
            .discountAmount(discountAmount)
            .finalPrice(finalPrice)
            .build();
    }
    
    /**
     * Basisrabatt je Einzelbestellung (netto).
     * 
     * Quelle: Anlage 1, Abschnitt 1.1
     */
    private double calculateBaseDiscount(double orderValue) {
        if (orderValue >= 75000) {
            return 10.0;
        } else if (orderValue >= 50000) {
            return 9.0;
        } else if (orderValue >= 30000) {
            return 8.0;
        } else if (orderValue >= 15000) {
            return 6.0;
        } else if (orderValue >= 5000) {
            return 3.0;
        } else {
            return 0.0;
        }
    }
    
    /**
     * Frühbucherrabatt (zusätzlich).
     * 
     * Quelle: Anlage 1, Abschnitt 1.2
     * - 10-14 Tage: 1% Rabatt
     * - 15-29 Tage: 2% Rabatt  
     * - 30-44 Tage: 3% Rabatt
     */
    private double calculateEarlyBookingDiscount(int leadTime) {
        if (leadTime >= 30) {
            return 3.0;
        } else if (leadTime >= 15) {
            return 2.0;
        } else if (leadTime >= 10) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}