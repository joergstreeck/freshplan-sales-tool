package de.freshplan.test;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

/**
 * Basis-Klasse für alle Integrationstests mit Testcontainers.
 * 
 * Diese Klasse stellt sicher, dass:
 * - Eine echte PostgreSQL-Datenbank via Testcontainers läuft
 * - Flyway-Migrationen ausgeführt werden
 * 
 * WICHTIG: Jeder Test sollte mit @TestTransaction annotiert sein,
 * damit die Datenbank automatisch sauber bleibt!
 */
public abstract class BaseIntegrationTest {
    
    @Inject
    protected EntityManager entityManager;
    
    /**
     * Hilfsmethode um zu prüfen ob Stammdaten vorhanden sind
     */
    protected long countContactRoles() {
        return (Long) entityManager
            .createNativeQuery("SELECT COUNT(*) FROM contact_roles")
            .getSingleResult();
    }
}