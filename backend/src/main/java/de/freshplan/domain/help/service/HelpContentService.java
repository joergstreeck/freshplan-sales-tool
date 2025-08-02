package de.freshplan.domain.help.service;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.entity.HelpType;
import de.freshplan.domain.help.entity.UserLevel;
import de.freshplan.domain.help.repository.HelpContentRepository;
import de.freshplan.domain.help.service.dto.HelpAnalytics;
import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.HelpResponse;
import de.freshplan.domain.help.service.dto.UserStruggle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service für Help Content Management
 * 
 * Zentrale Business Logic für:
 * - Kontextuelle Hilfe-Bereitstellung
 * - User Struggle Detection
 * - Hilfe-Analytics und Feedback
 * - Adaptive Help Delivery
 */
@ApplicationScoped
@Transactional
public class HelpContentService {

    private static final Logger LOG = LoggerFactory.getLogger(HelpContentService.class);

    @Inject
    HelpContentRepository helpRepository;

    @Inject
    UserStruggleDetectionService struggleDetectionService;

    @Inject
    HelpAnalyticsService analyticsService;

    /**
     * Holt die beste Hilfe für eine Feature-Anfrage
     */
    public HelpResponse getHelpForFeature(HelpRequest request) {
        LOG.debug("Getting help for feature: {} (user: {}, level: {})", 
                 request.feature(), request.userId(), request.userLevel());

        // 1. Standard Hilfe-Inhalte laden
        List<HelpContent> contents = helpRepository.findForUserLevel(
            request.feature(), 
            request.userLevel(), 
            request.userRoles()
        );

        // 2. Struggle Detection - proaktive Hilfe?
        UserStruggle struggle = struggleDetectionService.detectStruggle(
            request.userId(), 
            request.feature(),
            request.context()
        );

        // 3. Adaptive Content Selection
        HelpContent selectedContent = selectBestContent(contents, request, struggle);

        if (selectedContent != null) {
            // View tracking
            selectedContent.incrementViewCount();
            helpRepository.persist(selectedContent);

            // Analytics
            analyticsService.trackHelpRequest(request, selectedContent, struggle);

            return buildHelpResponse(selectedContent, struggle, request);
        }

        LOG.warn("No help content found for feature: {} (user level: {})", 
                request.feature(), request.userLevel());
        
        return HelpResponse.empty(request.feature());
    }

    /**
     * Wählt den besten Hilfe-Inhalt basierend auf Kontext aus
     */
    private HelpContent selectBestContent(List<HelpContent> contents, HelpRequest request, UserStruggle struggle) {
        if (contents.isEmpty()) {
            return null;
        }

        // Bei Struggle: Passende proaktive Hilfe bevorzugen
        if (struggle.isDetected()) {
            Optional<HelpContent> proactiveHelp = contents.stream()
                .filter(c -> c.helpType == HelpType.PROACTIVE || c.helpType == HelpType.CORRECTION)
                .findFirst();
            
            if (proactiveHelp.isPresent()) {
                LOG.info("Providing proactive help for struggle: {} (feature: {})", 
                        struggle.getType(), request.feature());
                return proactiveHelp.get();
            }
        }

        // Erste Nutzung: First-Time Hilfe
        if (request.isFirstTime()) {
            Optional<HelpContent> firstTimeHelp = contents.stream()
                .filter(c -> c.helpType == HelpType.FIRST_TIME || c.helpType == HelpType.TOUR)
                .findFirst();
            
            if (firstTimeHelp.isPresent()) {
                return firstTimeHelp.get();
            }
        }

        // Spezifischer Typ angefragt
        if (request.preferredType() != null) {
            Optional<HelpContent> typeSpecific = contents.stream()
                .filter(c -> c.helpType == request.preferredType())
                .findFirst();
                
            if (typeSpecific.isPresent()) {
                return typeSpecific.get();
            }
        }

        // Default: Besten verfügbaren Content (nach Priority und Helpfulness)
        return contents.stream()
                .filter(c -> c.helpType == HelpType.TOOLTIP || c.helpType == HelpType.TUTORIAL)
                .min((a, b) -> {
                    // Erst nach Priority, dann nach Hilfreichkeit
                    int priorityCompare = Integer.compare(a.priority, b.priority);
                    if (priorityCompare != 0) return priorityCompare;
                    
                    return Double.compare(b.getHelpfulnessRate(), a.getHelpfulnessRate());
                })
                .orElse(contents.get(0));
    }

    /**
     * Baut die Help Response auf
     */
    private HelpResponse buildHelpResponse(HelpContent content, UserStruggle struggle, HelpRequest request) {
        HelpResponse.Builder builder = HelpResponse.builder()
            .id(content.id)
            .feature(content.feature)
            .title(content.title)
            .type(content.helpType)
            .priority(content.priority);

        // Content basierend auf User Level und Preference
        String selectedContent = selectContentByLevel(content, request.userLevel());
        builder.content(selectedContent);

        // Video URL falls vorhanden
        if (content.videoUrl != null && !content.videoUrl.isEmpty()) {
            builder.videoUrl(content.videoUrl);
        }

        // Interaction Data (für Tours, etc.)
        if (content.interactionData != null) {
            builder.interactionData(content.interactionData);
        }

        // Struggle-spezifische Anpassungen
        if (struggle.isDetected()) {
            builder.struggleDetected(true)
                   .struggleType(struggle.getType().toString())
                   .suggestionLevel(struggle.getSeverity());
        }

        // Analytics Daten
        builder.viewCount(content.viewCount)
               .helpfulnessRate(content.getHelpfulnessRate());

        return builder.build();
    }

    /**
     * Wählt passenden Content für User Level
     */
    private String selectContentByLevel(HelpContent content, String userLevel) {
        UserLevel level = UserLevel.valueOf(userLevel.toUpperCase());
        
        return switch (level) {
            case BEGINNER -> {
                // Beginners bekommen detailed content wenn verfügbar
                if (content.detailedContent != null && !content.detailedContent.isEmpty()) {
                    yield content.detailedContent;
                }
                if (content.mediumContent != null && !content.mediumContent.isEmpty()) {
                    yield content.mediumContent;
                }
                yield content.shortContent;
            }
            case INTERMEDIATE -> {
                // Intermediate users bekommen medium content
                if (content.mediumContent != null && !content.mediumContent.isEmpty()) {
                    yield content.mediumContent;
                }
                if (content.shortContent != null && !content.shortContent.isEmpty()) {
                    yield content.shortContent;
                }
                yield content.detailedContent;
            }
            case EXPERT -> {
                // Experts bekommen kurze, prägnante Hilfe
                if (content.shortContent != null && !content.shortContent.isEmpty()) {
                    yield content.shortContent;
                }
                yield content.mediumContent;
            }
        };
    }

    /**
     * Registriert User Feedback für Hilfe-Inhalt
     */
    @Transactional
    public void recordFeedback(UUID helpId, String userId, boolean helpful, 
                              Integer timeSpent, String comment) {
        LOG.debug("Recording feedback for help {}: helpful={}, timeSpent={}", 
                 helpId, helpful, timeSpent);

        Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
        if (contentOpt.isEmpty()) {
            LOG.warn("Help content not found for feedback: {}", helpId);
            return;
        }

        HelpContent content = contentOpt.get();
        content.recordFeedback(helpful, timeSpent);
        helpRepository.persist(content);

        // Analytics
        analyticsService.trackFeedback(helpId, userId, helpful, timeSpent, comment);

        LOG.info("Feedback recorded for help '{}': {}% helpful ({} total)", 
                content.title, content.getHelpfulnessRate(), 
                content.helpfulCount + content.notHelpfulCount);
    }

    /**
     * Sucht in Hilfe-Inhalten
     */
    public List<HelpResponse> searchHelp(String searchTerm, String userLevel, List<String> userRoles) {
        LOG.debug("Searching help content for: '{}' (level: {})", searchTerm, userLevel);

        List<HelpContent> results = helpRepository.searchContent(searchTerm, userLevel, userRoles);
        
        return results.stream()
                .map(content -> HelpResponse.builder()
                    .id(content.id)
                    .feature(content.feature)
                    .title(content.title)
                    .type(content.helpType)
                    .content(selectContentByLevel(content, userLevel))
                    .videoUrl(content.videoUrl)
                    .helpfulnessRate(content.getHelpfulnessRate())
                    .viewCount(content.viewCount)
                    .build())
                .toList();
    }

    /**
     * Holt Analytics für Help System
     */
    public HelpAnalytics getAnalytics() {
        return analyticsService.getOverallAnalytics();
    }

    /**
     * Erstellt oder aktualisiert Hilfe-Inhalt
     */
    @Transactional
    public HelpContent createOrUpdateHelpContent(
            String feature, HelpType type, String title,
            String shortContent, String mediumContent, String detailedContent,
            UserLevel userLevel, List<String> roles, String createdBy) {

        LOG.info("Creating help content: {} - {} ({})", feature, title, type);

        HelpContent content = new HelpContent();
        content.feature = feature;
        content.helpType = type;
        content.title = title;
        content.shortContent = shortContent;
        content.mediumContent = mediumContent;
        content.detailedContent = detailedContent;
        content.targetUserLevel = userLevel;
        content.targetRoles = roles;
        content.createdBy = createdBy;
        content.updatedBy = createdBy;
        content.createdAt = LocalDateTime.now();
        content.updatedAt = LocalDateTime.now();

        helpRepository.persist(content);
        return content;
    }

    /**
     * Aktiviert/Deaktiviert Hilfe-Inhalt
     */
    @Transactional
    public void toggleHelpContent(UUID helpId, boolean active, String updatedBy) {
        Optional<HelpContent> contentOpt = helpRepository.findByIdOptional(helpId);
        if (contentOpt.isEmpty()) {
            throw new IllegalArgumentException("Help content not found: " + helpId);
        }

        HelpContent content = contentOpt.get();
        content.isActive = active;
        content.updatedBy = updatedBy;
        content.updatedAt = LocalDateTime.now();

        helpRepository.persist(content);
        
        LOG.info("Help content '{}' {}: {}", content.title, 
                active ? "activated" : "deactivated", helpId);
    }

    /**
     * Holt Feature Coverage Report
     */
    public List<String> getFeatureCoverageGaps() {
        List<String> allFeatures = helpRepository.getFeaturesWithHelp();
        List<String> tooltipGaps = helpRepository.getFeaturesWithoutType(HelpType.TOOLTIP);
        List<String> tourGaps = helpRepository.getFeaturesWithoutType(HelpType.TOUR);

        LOG.debug("Help coverage: {} features total, {} without tooltips, {} without tours", 
                 allFeatures.size(), tooltipGaps.size(), tourGaps.size());

        return tourGaps; // Tours sind kritischer als Tooltips
    }
}