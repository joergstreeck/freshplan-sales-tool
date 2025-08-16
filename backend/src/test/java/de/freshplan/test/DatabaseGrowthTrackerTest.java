package de.freshplan.test;

import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import java.util.*;

/**
 * Database Growth Tracker - Zeigt welche Tests Daten hinterlassen
 * 
 * Dieser Test läuft zwischen anderen Tests und protokolliert Änderungen
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseGrowthTrackerTest {
    
    @Inject CustomerRepository customerRepository;
    
    private static final Map<String, Long> checkpoints = new HashMap<>();
    private static final List<String> growthLog = new ArrayList<>();
    
    @Test
    @Order(10)
    void checkpoint_01_initial() {
        captureState("01_INITIAL");
    }
    
    @Test
    @Order(100)
    void checkpoint_02_afterBasicTests() {
        captureState("02_AFTER_BASIC_TESTS");
    }
    
    @Test
    @Order(500)
    void checkpoint_03_midway() {
        captureState("03_MIDWAY");
    }
    
    @Test
    @Order(1000)
    void checkpoint_04_afterCQRSTests() {
        captureState("04_AFTER_CQRS_TESTS");
    }
    
    @Test
    @Order(9999)
    void checkpoint_99_final() {
        captureState("99_FINAL");
        
        // Print summary
        System.out.println("\n" + "=".repeat(100));
        System.out.println("=== DATABASE GROWTH SUMMARY ===");
        System.out.println("=".repeat(100));
        
        Long initial = checkpoints.get("01_INITIAL");
        Long finalCount = checkpoints.get("99_FINAL");
        
        if (initial != null && finalCount != null) {
            long growth = finalCount - initial;
            System.out.printf("TOTAL GROWTH: %d customers added during test run%n", growth);
            
            if (growth > 0) {
                System.out.println("\n### GROWTH TIMELINE ###");
                growthLog.forEach(System.out::println);
                
                System.out.println("\n### PROBLEM AREAS ###");
                checkpoints.forEach((checkpoint, count) -> {
                    if (initial != null && count > initial) {
                        System.out.printf("%s: +%d customers%n", checkpoint, count - initial);
                    }
                });
            }
        }
        
        System.out.println("=".repeat(100) + "\n");
    }
    
    private void captureState(String checkpoint) {
        long count = customerRepository.count();
        Long previousCount = checkpoints.isEmpty() ? null : 
            checkpoints.values().stream().reduce((first, second) -> second).orElse(null);
        
        checkpoints.put(checkpoint, count);
        
        String message = String.format("[%s] Customers: %d", checkpoint, count);
        if (previousCount != null && count != previousCount) {
            long diff = count - previousCount;
            message += String.format(" (CHANGE: %+d)", diff);
            
            // Log significant changes
            if (Math.abs(diff) > 0) {
                growthLog.add(String.format("%s: %d → %d (%+d)", 
                    checkpoint, previousCount, count, diff));
            }
        }
        
        System.out.println(message);
    }
}