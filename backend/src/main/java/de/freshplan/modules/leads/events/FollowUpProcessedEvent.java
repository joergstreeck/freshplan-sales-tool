package de.freshplan.modules.leads.events;

import java.time.LocalDateTime;

/**
 * Event für verarbeitete Follow-ups
 * Wird nach erfolgreicher T+3/T+7 Follow-up Verarbeitung gefeuert
 *
 * Integration mit Cockpit-Module für Real-time Dashboard Updates
 */
public class FollowUpProcessedEvent {

    private final int t3Count;
    private final int t7Count;
    private final LocalDateTime processedAt;

    public FollowUpProcessedEvent(int t3Count, int t7Count, LocalDateTime processedAt) {
        this.t3Count = t3Count;
        this.t7Count = t7Count;
        this.processedAt = processedAt;
    }

    public int getT3Count() {
        return t3Count;
    }

    public int getT7Count() {
        return t7Count;
    }

    public int getTotalCount() {
        return t3Count + t7Count;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    @Override
    public String toString() {
        return String.format("FollowUpProcessedEvent[t3=%d, t7=%d, at=%s]",
            t3Count, t7Count, processedAt);
    }
}