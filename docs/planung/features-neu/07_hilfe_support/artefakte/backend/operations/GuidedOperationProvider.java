package com.freshfoodz.crm.help.operations;

/**
 * Interface für Guided Operation Provider im CAR-Strategy Help-System
 * Ermöglicht plugin-basierte Operations-Guides
 */
public interface GuidedOperationProvider {

    /**
     * Kategorie der Operation (z.B. "operations", "sales", "support")
     */
    String getOperationCategory();

    /**
     * Berechnet Confidence-Score für eine User-Anfrage
     * @param userQuery Die User-Anfrage
     * @return Confidence zwischen 0.0 und 1.0
     */
    double getConfidenceForQuery(String userQuery);

    /**
     * Behandelt eine User-Anfrage und gibt strukturierte Guidance zurück
     * @param userQuery Die User-Anfrage
     * @return CARResponse mit strukturierter Operations-Guidance
     */
    CARResponse handleQuery(String userQuery);
}