package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.service.dto.PotentialCalculationRequest;
import de.freshplan.domain.customer.service.dto.PotentialCalculationResponse;
import de.freshplan.domain.customer.service.dto.PotentialCalculationResponse.QuickWinDto;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculator service for customer sales potential.
 * Implements industry-specific calculations and quick win identification.
 * 
 * @since 2.0.0
 */
@ApplicationScoped
public class PotentialCalculator {
    
    private static final Logger log = LoggerFactory.getLogger(PotentialCalculator.class);
    
    // Base rates per meal/service
    private static final BigDecimal BREAKFAST_RATE = new BigDecimal("3.50");
    private static final BigDecimal BREAKFAST_WARM_RATE = new BigDecimal("5.00");
    private static final BigDecimal LUNCH_RATE = new BigDecimal("8.50");
    private static final BigDecimal DINNER_RATE = new BigDecimal("12.00");
    private static final BigDecimal ROOMSERVICE_RATE = new BigDecimal("15.00");
    
    // Working days per month
    private static final int WORKING_DAYS_MONTH = 22;
    private static final int MONTHS_PER_YEAR = 12;
    
    public PotentialCalculationResponse calculate(
            Customer customer,
            PotentialCalculationRequest request) {
        
        BigDecimal monthlyPotential = BigDecimal.ZERO;
        List<QuickWinDto> quickWins = new ArrayList<>();
        
        // Calculate based on industry
        switch (request.getIndustry().toLowerCase()) {
            case "hotel":
                monthlyPotential = calculateHotelPotential(request, quickWins);
                break;
            case "restaurant":
                monthlyPotential = calculateRestaurantPotential(request, quickWins);
                break;
            case "krankenhaus":
            case "hospital":
                monthlyPotential = calculateHospitalPotential(request, quickWins);
                break;
            case "schule":
            case "school":
                monthlyPotential = calculateSchoolPotential(request, quickWins);
                break;
            default:
                monthlyPotential = calculateGenericPotential(request, quickWins);
        }
        
        // Apply chain multiplier if applicable
        if (customer.getTotalLocationsEU() != null && customer.getTotalLocationsEU() > 1) {
            BigDecimal multiplier = new BigDecimal(customer.getTotalLocationsEU());
            monthlyPotential = monthlyPotential.multiply(multiplier);
            
            // Add chain-specific quick win
            quickWins.add(new QuickWinDto(
                "Kettenweite Standardisierung",
                monthlyPotential.multiply(new BigDecimal("0.15")), // 15% additional savings
                "Freshfoodz Kettenkonzept"
            ).withDescription("Standardisierte Prozesse über alle Standorte")
             .withPriority(1));
        }
        
        // Add pain point based quick wins
        if (customer.getPainPoints() != null) {
            addPainPointQuickWins(customer.getPainPoints(), quickWins, monthlyPotential);
        }
        
        BigDecimal yearlyPotential = monthlyPotential.multiply(
            new BigDecimal(MONTHS_PER_YEAR)
        );
        
        // Sort quick wins by priority
        quickWins.sort((a, b) -> {
            Integer aPrio = a.getPriority() != null ? a.getPriority() : 99;
            Integer bPrio = b.getPriority() != null ? b.getPriority() : 99;
            return aPrio.compareTo(bPrio);
        });
        
        // Keep only top 3 quick wins
        if (quickWins.size() > 3) {
            quickWins = quickWins.subList(0, 3);
        }
        
        return new PotentialCalculationResponse(monthlyPotential, yearlyPotential)
                .withQuickWins(quickWins)
                .withCalculationNote("Berechnung basiert auf Durchschnittswerten der Branche");
    }
    
    private BigDecimal calculateHotelPotential(
            PotentialCalculationRequest request, 
            List<QuickWinDto> quickWins) {
        
        BigDecimal potential = BigDecimal.ZERO;
        
        // Breakfast
        if (request.getBooleanValue("breakfast", false)) {
            Integer guests = request.getIntegerValue("breakfastGuestsPerDay", 50);
            BigDecimal breakfastPotential = BREAKFAST_RATE
                    .multiply(new BigDecimal(guests))
                    .multiply(new BigDecimal(WORKING_DAYS_MONTH));
            potential = potential.add(breakfastPotential);
            
            if (request.getBooleanValue("breakfastWarm", false)) {
                BigDecimal warmBonus = BREAKFAST_WARM_RATE.subtract(BREAKFAST_RATE)
                        .multiply(new BigDecimal(guests))
                        .multiply(new BigDecimal(WORKING_DAYS_MONTH));
                potential = potential.add(warmBonus);
                
                quickWins.add(new QuickWinDto(
                    "Warmes Frühstück optimieren",
                    warmBonus,
                    "Cook&Fresh® Frühstück"
                ).withPriority(2));
            }
        }
        
        // Lunch
        if (request.getBooleanValue("lunch", false)) {
            Integer covers = request.getIntegerValue("lunchCoversPerDay", 80);
            BigDecimal lunchPotential = LUNCH_RATE
                    .multiply(new BigDecimal(covers))
                    .multiply(new BigDecimal(WORKING_DAYS_MONTH));
            potential = potential.add(lunchPotential);
        }
        
        // Dinner  
        if (request.getBooleanValue("dinner", false)) {
            Integer covers = request.getIntegerValue("dinnerCoversPerDay", 60);
            BigDecimal dinnerPotential = DINNER_RATE
                    .multiply(new BigDecimal(covers))
                    .multiply(new BigDecimal(WORKING_DAYS_MONTH));
            potential = potential.add(dinnerPotential);
        }
        
        // Room Service
        if (request.getBooleanValue("roomService", false)) {
            Integer orders = request.getIntegerValue("roomServiceOrdersPerDay", 20);
            BigDecimal roomServicePotential = ROOMSERVICE_RATE
                    .multiply(new BigDecimal(orders))
                    .multiply(new BigDecimal(WORKING_DAYS_MONTH));
            potential = potential.add(roomServicePotential);
            
            quickWins.add(new QuickWinDto(
                "24/7 Room Service",
                roomServicePotential.multiply(new BigDecimal("0.3")),
                "Smart Kitchen Solutions"
            ).withDescription("Rund um die Uhr Verfügbarkeit ohne Nachtschicht")
             .withPriority(3));
        }
        
        return potential;
    }
    
    private BigDecimal calculateRestaurantPotential(
            PotentialCalculationRequest request,
            List<QuickWinDto> quickWins) {
        
        Integer coversPerDay = request.getIntegerValue("averageCoversPerDay", 100);
        BigDecimal avgSpend = new BigDecimal(
            request.getIntegerValue("averageSpendPerCover", 25)
        );
        
        // Base calculation: 20% food cost savings potential
        BigDecimal monthlyRevenue = avgSpend
                .multiply(new BigDecimal(coversPerDay))
                .multiply(new BigDecimal(WORKING_DAYS_MONTH));
        
        BigDecimal potential = monthlyRevenue.multiply(new BigDecimal("0.20"));
        
        quickWins.add(new QuickWinDto(
            "Wareneinsatz optimieren",
            potential.multiply(new BigDecimal("0.5")),
            "Freshfoodz Vollsortiment"
        ).withPriority(1));
        
        return potential;
    }
    
    private BigDecimal calculateHospitalPotential(
            PotentialCalculationRequest request,
            List<QuickWinDto> quickWins) {
        
        Integer beds = request.getIntegerValue("numberOfBeds", 200);
        BigDecimal mealsPerBedDay = new BigDecimal("3");
        BigDecimal mealCost = new BigDecimal("7.50");
        
        BigDecimal monthlyPotential = mealCost
                .multiply(mealsPerBedDay)
                .multiply(new BigDecimal(beds))
                .multiply(new BigDecimal(30)); // All days in hospital
        
        // 25% savings potential in healthcare
        monthlyPotential = monthlyPotential.multiply(new BigDecimal("0.25"));
        
        quickWins.add(new QuickWinDto(
            "Diätverpflegung vereinfachen",
            monthlyPotential.multiply(new BigDecimal("0.4")),
            "MediCare Menülinie"
        ).withDescription("Alle Kostformen aus einer Hand")
         .withPriority(1));
        
        return monthlyPotential;
    }
    
    private BigDecimal calculateSchoolPotential(
            PotentialCalculationRequest request,
            List<QuickWinDto> quickWins) {
        
        Integer students = request.getIntegerValue("numberOfStudents", 500);
        BigDecimal mealPrice = new BigDecimal("4.50");
        BigDecimal participationRate = new BigDecimal("0.6"); // 60% participation
        
        BigDecimal monthlyPotential = mealPrice
                .multiply(new BigDecimal(students))
                .multiply(participationRate)
                .multiply(new BigDecimal(WORKING_DAYS_MONTH));
        
        // 30% savings potential in schools
        monthlyPotential = monthlyPotential.multiply(new BigDecimal("0.30"));
        
        quickWins.add(new QuickWinDto(
            "Bio-Komponenten integrieren",
            monthlyPotential.multiply(new BigDecimal("0.3")),
            "School Fresh Bio"
        ).withDescription("Förderfähige Bio-Produkte")
         .withPriority(2));
        
        return monthlyPotential;
    }
    
    private BigDecimal calculateGenericPotential(
            PotentialCalculationRequest request,
            List<QuickWinDto> quickWins) {
        
        // Generic calculation based on estimated food budget
        Integer monthlyBudget = request.getIntegerValue("estimatedMonthlyFoodBudget", 10000);
        
        // 20% generic savings potential
        return new BigDecimal(monthlyBudget).multiply(new BigDecimal("0.20"));
    }
    
    private void addPainPointQuickWins(
            List<String> painPoints,
            List<QuickWinDto> quickWins,
            BigDecimal basePotential) {
        
        for (String painPoint : painPoints) {
            switch (painPoint) {
                case "personalMangel":
                case "staffingIssues":
                    quickWins.add(new QuickWinDto(
                        "Personalmangel kompensieren",
                        basePotential.multiply(new BigDecimal("0.2")),
                        "Ready-to-Serve Lösungen"
                    ).withDescription("Weniger Fachpersonal nötig")
                     .withPriority(1));
                    break;
                    
                case "qualitySchwankungen":
                case "qualityFluctuations":
                    quickWins.add(new QuickWinDto(
                        "Konstante Qualität sichern",
                        basePotential.multiply(new BigDecimal("0.15")),
                        "Standardisierte Rezepturen"
                    ).withDescription("Gleichbleibende Qualität garantiert")
                     .withPriority(2));
                    break;
                    
                case "lebensmittelAbfall":
                case "foodWaste":
                    quickWins.add(new QuickWinDto(
                        "Food Waste reduzieren",
                        basePotential.multiply(new BigDecimal("0.1")),
                        "Portionsgerechte Verpackungen"
                    ).withDescription("Bis zu 30% weniger Abfall")
                     .withPriority(3));
                    break;
            }
        }
    }
}