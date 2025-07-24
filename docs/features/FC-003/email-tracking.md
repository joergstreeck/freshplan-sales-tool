# FC-003: E-Mail Tracking & Analytics - Detailspezifikation

## Tracking-Architektur

### 1. E-Mail Open Tracking

```java
@ApplicationScoped
public class EmailTrackingService {
    
    private static final String TRACKING_PIXEL_PATH = "/api/email/track/open";
    
    public String injectTrackingPixel(String htmlBody, UUID messageId) {
        String trackingUrl = buildTrackingUrl(TRACKING_PIXEL_PATH, messageId);
        
        String trackingPixel = String.format(
            "<img src=\"%s\" width=\"1\" height=\"1\" " +
            "style=\"display:none;\" alt=\"\" />",
            trackingUrl
        );
        
        // Insert before closing body tag
        return htmlBody.replace("</body>", trackingPixel + "</body>");
    }
    
    private String buildTrackingUrl(String path, UUID messageId) {
        String token = generateSecureToken(messageId);
        return String.format("%s%s/%s", baseUrl, path, token);
    }
}

@Path("/api/email/track")
public class EmailTrackingResource {
    
    @GET
    @Path("/open/{token}")
    @Produces("image/gif")
    public Response trackOpen(@PathParam("token") String token) {
        try {
            UUID messageId = decodeToken(token);
            
            // Record open event
            trackingService.recordOpen(messageId, 
                extractClientInfo(httpRequest));
            
            // Return 1x1 transparent GIF
            return Response.ok(TRANSPARENT_GIF)
                .header("Cache-Control", "no-store, no-cache")
                .build();
                
        } catch (InvalidTokenException e) {
            return Response.status(404).build();
        }
    }
}
```

### 2. Link Click Tracking

```java
@ApplicationScoped
public class LinkTrackingService {
    
    public String processLinks(String htmlBody, UUID messageId) {
        Document doc = Jsoup.parse(htmlBody);
        
        // Track all links except unsubscribe
        doc.select("a[href]").forEach(link -> {
            String originalUrl = link.attr("href");
            
            if (!isExcludedUrl(originalUrl)) {
                String trackedUrl = createTrackedUrl(originalUrl, messageId);
                link.attr("href", trackedUrl);
            }
        });
        
        return doc.html();
    }
    
    private String createTrackedUrl(String originalUrl, UUID messageId) {
        String trackingId = generateTrackingId(messageId, originalUrl);
        
        return String.format("%s/api/email/track/click/%s?url=%s",
            baseUrl,
            trackingId,
            URLEncoder.encode(originalUrl, StandardCharsets.UTF_8)
        );
    }
}

@GET
@Path("/click/{trackingId}")
public Response trackClick(@PathParam("trackingId") String trackingId,
                          @QueryParam("url") String encodedUrl) {
    try {
        ClickInfo info = decodeTrackingId(trackingId);
        
        // Record click event
        trackingService.recordClick(info.messageId, 
            info.linkId,
            extractClientInfo(httpRequest));
        
        // Redirect to original URL
        String originalUrl = URLDecoder.decode(encodedUrl, 
            StandardCharsets.UTF_8);
        
        return Response.temporaryRedirect(URI.create(originalUrl))
            .build();
            
    } catch (Exception e) {
        return Response.status(404).build();
    }
}
```

### 3. Analytics Data Model

```java
@Entity
public class EmailTrackingEvent {
    @Id UUID id;
    UUID messageId;
    
    @Enumerated(EnumType.STRING)
    TrackingEventType type; // SENT, DELIVERED, OPENED, CLICKED, BOUNCED
    
    Instant occurredAt;
    
    // Client Information
    String ipAddress;
    String userAgent;
    String deviceType;
    String operatingSystem;
    String emailClient;
    
    // Click-specific data
    String clickedUrl;
    String linkId;
}

@Entity
public class EmailMetrics {
    @Id UUID messageId;
    
    // Delivery Metrics
    Instant sentAt;
    Instant deliveredAt;
    Instant firstOpenedAt;
    Instant lastOpenedAt;
    
    // Engagement Metrics
    Integer openCount = 0;
    Integer uniqueOpenCount = 0;
    Integer clickCount = 0;
    Integer uniqueClickCount = 0;
    
    // Conversion Metrics
    boolean replied = false;
    boolean converted = false; // Opportunity gewonnen
    BigDecimal attributedRevenue;
}
```

### 4. Real-time Analytics Dashboard

```typescript
// Analytics Dashboard Component
export const EmailAnalyticsDashboard: React.FC = () => {
    const { data: metrics } = useEmailMetrics();
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={3}>
                <MetricCard
                    title="Delivery Rate"
                    value={`${metrics.deliveryRate}%`}
                    trend={metrics.deliveryTrend}
                />
            </Grid>
            <Grid item xs={12} md={3}>
                <MetricCard
                    title="Open Rate"
                    value={`${metrics.openRate}%`}
                    benchmark={industryBenchmark.openRate}
                />
            </Grid>
            <Grid item xs={12} md={3}>
                <MetricCard
                    title="Click Rate"
                    value={`${metrics.clickRate}%`}
                    benchmark={industryBenchmark.clickRate}
                />
            </Grid>
            <Grid item xs={12} md={3}>
                <MetricCard
                    title="Reply Rate"
                    value={`${metrics.replyRate}%`}
                />
            </Grid>
            
            <Grid item xs={12}>
                <EmailPerformanceChart data={metrics.timeline} />
            </Grid>
            
            <Grid item xs={12} md={6}>
                <TopPerformingTemplates templates={metrics.topTemplates} />
            </Grid>
            
            <Grid item xs={12} md={6}>
                <EmailClientBreakdown clients={metrics.emailClients} />
            </Grid>
        </Grid>
    );
};
```

### 5. Template Performance Analytics

```java
@ApplicationScoped
public class TemplateAnalyticsService {
    
    public TemplatePerformance calculatePerformance(UUID templateId, 
                                                    TimeRange range) {
        List<EmailMetrics> metrics = metricsRepository
            .findByTemplateIdAndDateRange(templateId, range);
            
        return TemplatePerformance.builder()
            .templateId(templateId)
            .sentCount(metrics.size())
            .avgOpenRate(calculateAverage(metrics, EmailMetrics::getOpenRate))
            .avgClickRate(calculateAverage(metrics, EmailMetrics::getClickRate))
            .avgReplyRate(calculateAverage(metrics, EmailMetrics::isReplied))
            .conversionRate(calculateConversionRate(metrics))
            .revenue(calculateAttributedRevenue(metrics))
            .bestSendTime(findOptimalSendTime(metrics))
            .build();
    }
    
    private SendTimeRecommendation findOptimalSendTime(
            List<EmailMetrics> metrics) {
        // Analyse nach Wochentag und Uhrzeit
        Map<DayOfWeek, Map<Integer, Double>> openRatesByTime = 
            metrics.stream()
                .collect(Collectors.groupingBy(
                    m -> m.getSentAt().atZone(ZoneId.of("Europe/Berlin"))
                        .getDayOfWeek(),
                    Collectors.groupingBy(
                        m -> m.getSentAt().atZone(ZoneId.of("Europe/Berlin"))
                            .getHour(),
                        Collectors.averagingDouble(m -> m.getOpenRate())
                    )
                ));
                
        return analyzeOptimalTime(openRatesByTime);
    }
}
```

### 6. Engagement Scoring

```java
@ApplicationScoped
public class EmailEngagementScorer {
    
    public EngagementScore calculateScore(UUID customerId) {
        List<EmailTrackingEvent> events = trackingRepository
            .findByCustomerId(customerId, Duration.ofDays(90));
            
        double openScore = calculateOpenScore(events);
        double clickScore = calculateClickScore(events);
        double replyScore = calculateReplyScore(events);
        double recencyScore = calculateRecencyScore(events);
        
        double totalScore = (openScore * 0.2 + 
                            clickScore * 0.3 + 
                            replyScore * 0.4 + 
                            recencyScore * 0.1);
                            
        return EngagementScore.builder()
            .customerId(customerId)
            .score(totalScore)
            .level(determineLevel(totalScore))
            .lastCalculated(Instant.now())
            .build();
    }
}
```

### 7. Bounce & Complaint Handling

```java
@ApplicationScoped
public class BounceHandler {
    
    @ConsumeEvent("email.bounce")
    public void handleBounce(BounceEvent event) {
        EmailBounce bounce = EmailBounce.builder()
            .messageId(event.getMessageId())
            .bounceType(event.getType()) // HARD, SOFT
            .reason(event.getReason())
            .occurredAt(event.getTimestamp())
            .build();
            
        bounceRepository.persist(bounce);
        
        if (event.getType() == BounceType.HARD) {
            // Mark email as invalid
            markEmailInvalid(event.getRecipient());
            
            // Update customer record
            updateCustomerEmailStatus(event.getCustomerId(), 
                EmailStatus.INVALID);
        }
    }
}
```

### 8. GDPR Compliance & Privacy

```java
@ApplicationScoped
public class PrivacyCompliantTracking {
    
    public boolean shouldTrack(UUID customerId) {
        // Check tracking consent
        CustomerPrivacySettings settings = 
            privacyRepository.findByCustomerId(customerId);
            
        return settings != null && 
               settings.isEmailTrackingAllowed() &&
               !settings.isOptedOut();
    }
    
    @Scheduled(every = "24h")
    public void cleanupOldTrackingData() {
        // GDPR: Lösche Tracking-Daten älter als 2 Jahre
        Instant cutoffDate = Instant.now().minus(730, ChronoUnit.DAYS);
        
        trackingRepository.deleteOlderThan(cutoffDate);
    }
}
```

### 9. Export & Reporting

```java
@Path("/api/email/analytics/export")
public class AnalyticsExportResource {
    
    @GET
    @Produces("text/csv")
    public Response exportCampaignMetrics(
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("templateId") UUID templateId) {
        
        TimeRange range = TimeRange.of(
            LocalDate.parse(from),
            LocalDate.parse(to)
        );
        
        List<EmailMetrics> metrics = metricsService
            .getMetricsByTemplateAndRange(templateId, range);
            
        String csv = CsvExporter.export(metrics, EmailMetrics.class);
        
        return Response.ok(csv)
            .header("Content-Disposition", 
                "attachment; filename=email-metrics.csv")
            .build();
    }
}
```