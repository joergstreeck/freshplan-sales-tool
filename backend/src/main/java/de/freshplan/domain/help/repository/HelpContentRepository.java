package de.freshplan.domain.help.repository;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository für Help Content Operations
 * 
 * Bietet optimierte Queries für verschiedene Help-Szenarien:
 * - Feature-spezifische Hilfe
 * - User-Level angepasste Inhalte
 * - Analytics und Tracking
 */
@ApplicationScoped
public class HelpContentRepository implements PanacheRepositoryBase<HelpContent, UUID> {

    /**
     * Findet alle aktiven Hilfe-Inhalte für ein Feature
     */
    public List<HelpContent> findActiveByFeature(String feature) {
        return list("feature = ?1 AND isActive = true ORDER BY priority ASC, createdAt DESC", 
                   feature);
    }

    /**
     * Findet Hilfe-Inhalte für einen spezifischen Typ und Feature
     */
    public List<HelpContent> findByFeatureAndType(String feature, HelpType helpType) {
        return list("feature = ?1 AND helpType = ?2 AND isActive = true ORDER BY priority ASC", 
                   feature, helpType);
    }

    /**
     * Findet den besten Tooltip für ein Feature basierend auf User-Level
     */
    public Optional<HelpContent> findBestTooltip(String feature, String userLevel, List<String> userRoles) {
        List<HelpContent> tooltips = list(
            "feature = ?1 AND helpType = ?2 AND isActive = true " +
            "AND targetUserLevel = ?3 ORDER BY priority ASC, helpfulCount DESC",
            feature, HelpType.TOOLTIP, userLevel.toUpperCase()
        );

        // Filter nach Rollen wenn vorhanden
        return tooltips.stream()
                .filter(content -> content.targetRoles == null || content.targetRoles.isEmpty() ||
                                  userRoles.stream().anyMatch(content.targetRoles::contains))
                .findFirst();
    }

    /**
     * Findet User-spezifische Hilfe-Inhalte
     */
    public List<HelpContent> findForUserLevel(String feature, String userLevel, List<String> userRoles) {
        // Query für User Level Matching
        String levelQuery = switch (userLevel.toUpperCase()) {
            case "BEGINNER" -> "targetUserLevel = 'BEGINNER'";
            case "INTERMEDIATE" -> "targetUserLevel IN ('BEGINNER', 'INTERMEDIATE')";
            case "EXPERT" -> "targetUserLevel IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')";
            default -> "targetUserLevel = 'BEGINNER'";
        };

        List<HelpContent> contents = list(
            "feature = ?1 AND isActive = true AND " + levelQuery + " ORDER BY priority ASC",
            feature
        );

        // Filter nach Rollen
        return contents.stream()
                .filter(content -> content.targetRoles == null || content.targetRoles.isEmpty() ||
                                  userRoles.stream().anyMatch(content.targetRoles::contains))
                .toList();
    }

    /**
     * Findet die am häufigsten nachgefragten Hilfe-Themen
     */
    public List<HelpContent> findMostRequested(int limit) {
        return find("isActive = true ORDER BY viewCount DESC")
               .page(0, limit)
               .list();
    }

    /**
     * Findet die hilfreichsten Inhalte basierend auf Feedback
     */
    public List<HelpContent> findMostHelpful(int limit) {
        return find(
            "isActive = true AND (helpfulCount + notHelpfulCount) >= 5 " +
            "ORDER BY (CAST(helpfulCount AS double) / (helpfulCount + notHelpfulCount)) DESC, viewCount DESC"
        ).page(0, limit).list();
    }

    /**
     * Findet Inhalte die Verbesserung brauchen (niedrige Hilfreichkeits-Rate)
     */
    public List<HelpContent> findNeedingImprovement(int limit) {
        return find(
            "isActive = true AND (helpfulCount + notHelpfulCount) >= 10 " +
            "AND (CAST(helpfulCount AS double) / (helpfulCount + notHelpfulCount)) < 0.6 " +
            "ORDER BY viewCount DESC"
        ).page(0, limit).list();
    }

    /**
     * Sucht in Hilfe-Inhalten nach Suchbegriff
     */
    public List<HelpContent> searchContent(String searchTerm, String userLevel, List<String> userRoles) {
        String search = "%" + searchTerm.toLowerCase() + "%";
        
        List<HelpContent> results = list(
            "isActive = true AND " +
            "(LOWER(title) LIKE ?1 OR LOWER(shortContent) LIKE ?1 OR " +
            "LOWER(mediumContent) LIKE ?1 OR LOWER(detailedContent) LIKE ?1) " +
            "ORDER BY priority ASC, viewCount DESC",
            search
        );

        // Filter nach User Level und Rollen
        return results.stream()
                .filter(content -> content.isApplicableForUser(userLevel, userRoles))
                .toList();
    }

    /**
     * Statistik-Queries für Analytics
     */
    public long getTotalViews() {
        Long result = find("SELECT SUM(viewCount) FROM HelpContent WHERE isActive = true")
               .project(Long.class)
               .singleResult();
        return result != null ? result : 0L;
    }

    public long getTotalFeedback() {
        Long result = find("SELECT SUM(helpfulCount + notHelpfulCount) FROM HelpContent WHERE isActive = true")
               .project(Long.class)
               .singleResult();
        return result != null ? result : 0L;
    }

    public double getOverallHelpfulnessRate() {
        List<Object[]> result = find(
            "SELECT SUM(helpfulCount), SUM(notHelpfulCount) FROM HelpContent " +
            "WHERE isActive = true AND (helpfulCount + notHelpfulCount) > 0"
        ).project(Object[].class).list();
        
        if (result.isEmpty()) return 0.0;
        
        Object[] row = result.get(0);
        Long helpful = (Long) row[0];
        Long notHelpful = (Long) row[1];
        
        if (helpful == null || notHelpful == null || (helpful + notHelpful) == 0) {
            return 0.0;
        }
        
        return (double) helpful / (helpful + notHelpful) * 100.0;
    }

    /**
     * Feature-Coverage: Welche Features haben Hilfe-Inhalte?
     */
    public List<String> getFeaturesWithHelp() {
        return find("SELECT DISTINCT feature FROM HelpContent WHERE isActive = true ORDER BY feature")
               .project(String.class)
               .list();
    }

    /**
     * Gaps: Features ohne bestimmte Hilfe-Typen
     */
    public List<String> getFeaturesWithoutType(HelpType helpType) {
        List<String> allFeatures = getFeaturesWithHelp();
        List<String> featuresWithType = find(
            "SELECT DISTINCT feature FROM HelpContent WHERE isActive = true AND helpType = ?1",
            helpType
        ).project(String.class).list();

        return allFeatures.stream()
                .filter(feature -> !featuresWithType.contains(feature))
                .toList();
    }
}