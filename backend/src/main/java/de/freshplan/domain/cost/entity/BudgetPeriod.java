package de.freshplan.domain.cost.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Budget-Perioden für Cost Management
 */
public enum BudgetPeriod {
    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    /**
     * Berechnet den Start der aktuellen Periode
     */
    public LocalDateTime getPeriodStart(LocalDateTime now) {
        switch (this) {
            case HOURLY:
                return now.truncatedTo(ChronoUnit.HOURS);
            case DAILY:
                return now.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY:
                return now.truncatedTo(ChronoUnit.DAYS)
                    .minusDays(now.getDayOfWeek().getValue() - 1);
            case MONTHLY:
                return now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            case YEARLY:
                return now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
            default:
                return now.truncatedTo(ChronoUnit.DAYS);
        }
    }

    /**
     * Berechnet das Ende der aktuellen Periode
     */
    public LocalDateTime getPeriodEnd(LocalDateTime now) {
        switch (this) {
            case HOURLY:
                return now.truncatedTo(ChronoUnit.HOURS).plusHours(1);
            case DAILY:
                return now.truncatedTo(ChronoUnit.DAYS).plusDays(1);
            case WEEKLY:
                return getPeriodStart(now).plusWeeks(1);
            case MONTHLY:
                return getPeriodStart(now).plusMonths(1);
            case YEARLY:
                return getPeriodStart(now).plusYears(1);
            default:
                return now.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        }
    }

    /**
     * Benutzerfreundliche Bezeichnung
     */
    public String getDisplayName() {
        switch (this) {
            case HOURLY: return "Stündlich";
            case DAILY: return "Täglich";
            case WEEKLY: return "Wöchentlich";
            case MONTHLY: return "Monatlich";
            case YEARLY: return "Jährlich";
            default: return name();
        }
    }
}