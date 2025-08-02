package de.freshplan.domain.help.entity;

/**
 * User Experience Level für zielgruppenspezifische Hilfe
 */
public enum UserLevel {
    
    /**
     * Neue User oder solche mit wenig Erfahrung
     * Benötigen ausführliche Erklärungen
     */
    BEGINNER,
    
    /**
     * User mit mittlerer Erfahrung
     * Verstehen Grundlagen, brauchen aber noch Hilfe bei erweiterten Features
     */
    INTERMEDIATE,
    
    /**
     * Erfahrene User
     * Brauchen nur kurze Hinweise oder erweiterte Tipps
     */
    EXPERT
}