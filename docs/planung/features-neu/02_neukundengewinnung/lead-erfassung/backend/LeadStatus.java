package de.freshplan.leads.domain;

/**
 * Lead Status Enumeration
 * Foundation Standards: Clear business state definitions
 */
public enum LeadStatus {
    NEW,      // Neuer Lead, noch nicht bearbeitet
    ACTIVE,   // In Bearbeitung, aktive Kommunikation
    WON,      // Gewonnen, Kunde akquiriert
    LOST      // Verloren, Akquise gescheitert
}