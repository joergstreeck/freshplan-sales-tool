package de.freshplan.infrastructure.pg;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test-Alternative für PgNotifySender.
 * Sammelt alle gesendeten Notifications für Assertions.
 *
 * Sprint 2.1.1 P0 HOTFIX - Testbare Event-Pipeline
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class TestPgNotifySender implements PgNotifySender {

    public static record Send(String channel, String payload) {}

    private final List<Send> sent = new CopyOnWriteArrayList<>();

    @Override
    public void send(String channel, String payload) {
        sent.add(new Send(channel, payload));
    }

    /**
     * Gibt alle gesendeten Notifications zurück.
     */
    public List<Send> sent() {
        return sent;
    }

    /**
     * Leert die gesendeten Notifications.
     */
    public void clear() {
        sent.clear();
    }

    /**
     * Zählt wie viele Notifications gesendet wurden.
     */
    public int count() {
        return sent.size();
    }

    /**
     * Findet Notifications für einen bestimmten Channel.
     */
    public List<Send> sentToChannel(String channel) {
        return sent.stream()
            .filter(s -> s.channel().equals(channel))
            .toList();
    }
}