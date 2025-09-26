package de.freshplan.infrastructure.pg;

/**
 * Interface für PostgreSQL NOTIFY-Funktionalität.
 * Ermöglicht Abstraktion für Testbarkeit.
 *
 * Sprint 2.1.1 P0 HOTFIX - Testbare Event-Pipeline
 */
public interface PgNotifySender {
    /**
     * Sendet eine Notification via PostgreSQL NOTIFY.
     *
     * @param channel Der NOTIFY Channel
     * @param payload Die JSON Payload (max ~8KB)
     */
    void send(String channel, String payload);
}