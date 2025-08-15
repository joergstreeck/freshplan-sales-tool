package de.freshplan.test.config;

/**
 * Test Database Strategy für saubere, isolierte Tests.
 * 
 * Diese Klasse definiert die Strategie für Test-Datenbanken:
 * 
 * 1. ISOLATION: Jeder Test läuft in einer isolierten Umgebung
 * 2. STABILITÄT: Seed-Daten bleiben konstant
 * 3. PERFORMANCE: Wiederverwendung von Containern wo möglich
 * 
 * @author Claude
 * @since Phase 14.3 - Dauerhafte Test-Lösung
 */
public class TestDatabaseStrategy {
    
    /**
     * Strategie-Optionen für Test-Datenbanken
     */
    public enum Strategy {
        /**
         * ROLLBACK_AFTER_TEST (Empfohlen für Unit-Tests)
         * - Jeder Test startet mit sauberen Seed-Daten
         * - Alle Änderungen werden nach dem Test zurückgerollt
         * - Schnell und deterministisch
         */
        ROLLBACK_AFTER_TEST,
        
        /**
         * SEPARATE_SCHEMA (Empfohlen für Integration-Tests)
         * - Jeder Test bekommt sein eigenes Schema
         * - Parallele Test-Ausführung möglich
         * - Keine gegenseitige Beeinflussung
         */
        SEPARATE_SCHEMA,
        
        /**
         * IN_MEMORY (Schnellste Option)
         * - H2 In-Memory Database
         * - Für einfache Unit-Tests
         * - Keine PostgreSQL-Features
         */
        IN_MEMORY,
        
        /**
         * TESTCONTAINERS_ISOLATED (Aktuelle Lösung)
         * - Separater PostgreSQL Container
         * - Vollständige Isolation
         * - Realistische Test-Umgebung
         */
        TESTCONTAINERS_ISOLATED
    }
    
    // Standard-Strategie
    public static final Strategy DEFAULT = Strategy.TESTCONTAINERS_ISOLATED;
    
    /**
     * Seed-Daten Konfiguration
     */
    public static class SeedData {
        // Anzahl der stabilen Test-Kunden
        public static final int STABLE_CUSTOMER_COUNT = 50;
        
        // Prefix für stabile Test-Kunden
        public static final String STABLE_CUSTOMER_PREFIX = "[SEED]";
        
        // Diese Kunden werden NIEMALS gelöscht
        public static final String[] PROTECTED_CUSTOMER_NUMBERS = {
            "SEED-001", "SEED-002", "SEED-003", "SEED-004", "SEED-005",
            "SEED-006", "SEED-007", "SEED-008", "SEED-009", "SEED-010",
            // ... bis SEED-050
        };
    }
    
    /**
     * Test-Isolation Konfiguration
     */
    public static class Isolation {
        // Verwende Transactional Rollback wo möglich
        public static final boolean USE_TRANSACTIONAL_ROLLBACK = true;
        
        // Erstelle temporäre Schemas für parallele Tests
        public static final boolean USE_TEMP_SCHEMAS = false;
        
        // Cleanup-Strategie
        public static final CleanupStrategy CLEANUP = CleanupStrategy.ROLLBACK;
    }
    
    public enum CleanupStrategy {
        ROLLBACK,    // Transaction Rollback (schnell)
        TRUNCATE,    // Truncate Tables (mittel)
        DROP_CREATE  // Drop and Recreate (langsam)
    }
}