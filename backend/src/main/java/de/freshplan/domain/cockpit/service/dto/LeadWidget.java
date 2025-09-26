package de.freshplan.domain.cockpit.service.dto;

import de.freshplan.modules.leads.service.LeadService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Widget für Lead-Management Statistiken und Metriken. Integriert Lead-Statistiken,
 * Follow-up Metriken und Real-time Updates.
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - Dashboard Widget Integration
 */
public class LeadWidget {

  // Core Lead Statistiken
  private LeadService.LeadStatistics leadStats;

  // Follow-up Metriken
  private List<FollowUpMetric> recentFollowUps;
  private int pendingT3Count;
  private int pendingT7Count;
  private int completedT3Today;
  private int completedT7Today;

  // Performance Metriken
  private double t3ResponseRate;
  private double t7ResponseRate;
  private double averageResponseTime;

  // Status Distribution
  private StatusDistribution statusDistribution;

  // Recent Activities
  private List<RecentLeadActivity> recentActivities;

  // Widget Metadata
  private LocalDateTime lastUpdated;
  private boolean realTimeEnabled;

  // Konstruktoren
  public LeadWidget() {
    this.lastUpdated = LocalDateTime.now();
    this.realTimeEnabled = true;
  }

  // Inner Classes für strukturierte Daten

  /** Follow-up Metrik für einzelne Follow-up Aktionen. */
  public static class FollowUpMetric {
    private String leadId;
    private String companyName;
    private String followUpType;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    private boolean success;
    private String responseStatus;

    // Getters und Setters
    public String getLeadId() {
      return leadId;
    }

    public void setLeadId(String leadId) {
      this.leadId = leadId;
    }

    public String getCompanyName() {
      return companyName;
    }

    public void setCompanyName(String companyName) {
      this.companyName = companyName;
    }

    public String getFollowUpType() {
      return followUpType;
    }

    public void setFollowUpType(String followUpType) {
      this.followUpType = followUpType;
    }

    public LocalDateTime getScheduledAt() {
      return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
      this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getCompletedAt() {
      return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
      this.completedAt = completedAt;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getResponseStatus() {
      return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
      this.responseStatus = responseStatus;
    }
  }

  /** Status-Verteilung für Lead-Pipeline Visualisierung. */
  public static class StatusDistribution {
    private int newLeads;
    private int qualified;
    private int contacted;
    private int negotiating;
    private int won;
    private int lost;
    private int total;

    public int getNewLeads() {
      return newLeads;
    }

    public void setNewLeads(int newLeads) {
      this.newLeads = newLeads;
    }

    public int getQualified() {
      return qualified;
    }

    public void setQualified(int qualified) {
      this.qualified = qualified;
    }

    public int getContacted() {
      return contacted;
    }

    public void setContacted(int contacted) {
      this.contacted = contacted;
    }

    public int getNegotiating() {
      return negotiating;
    }

    public void setNegotiating(int negotiating) {
      this.negotiating = negotiating;
    }

    public int getWon() {
      return won;
    }

    public void setWon(int won) {
      this.won = won;
    }

    public int getLost() {
      return lost;
    }

    public void setLost(int lost) {
      this.lost = lost;
    }

    public int getTotal() {
      return total;
    }

    public void setTotal(int total) {
      this.total = total;
    }

    public double getConversionRate() {
      return total > 0 ? (double) won / total * 100 : 0;
    }
  }

  /** Aktuelle Lead-Aktivitäten für Activity Feed. */
  public static class RecentLeadActivity {
    private String leadId;
    private String companyName;
    private String activityType;
    private String description;
    private LocalDateTime occurredAt;
    private String userId;

    public String getLeadId() {
      return leadId;
    }

    public void setLeadId(String leadId) {
      this.leadId = leadId;
    }

    public String getCompanyName() {
      return companyName;
    }

    public void setCompanyName(String companyName) {
      this.companyName = companyName;
    }

    public String getActivityType() {
      return activityType;
    }

    public void setActivityType(String activityType) {
      this.activityType = activityType;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public LocalDateTime getOccurredAt() {
      return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
      this.occurredAt = occurredAt;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }

  // Main Getters und Setters

  public LeadService.LeadStatistics getLeadStats() {
    return leadStats;
  }

  public void setLeadStats(LeadService.LeadStatistics leadStats) {
    this.leadStats = leadStats;
  }

  public List<FollowUpMetric> getRecentFollowUps() {
    return recentFollowUps;
  }

  public void setRecentFollowUps(List<FollowUpMetric> recentFollowUps) {
    this.recentFollowUps = recentFollowUps;
  }

  public int getPendingT3Count() {
    return pendingT3Count;
  }

  public void setPendingT3Count(int pendingT3Count) {
    this.pendingT3Count = pendingT3Count;
  }

  public int getPendingT7Count() {
    return pendingT7Count;
  }

  public void setPendingT7Count(int pendingT7Count) {
    this.pendingT7Count = pendingT7Count;
  }

  public int getCompletedT3Today() {
    return completedT3Today;
  }

  public void setCompletedT3Today(int completedT3Today) {
    this.completedT3Today = completedT3Today;
  }

  public int getCompletedT7Today() {
    return completedT7Today;
  }

  public void setCompletedT7Today(int completedT7Today) {
    this.completedT7Today = completedT7Today;
  }

  public double getT3ResponseRate() {
    return t3ResponseRate;
  }

  public void setT3ResponseRate(double t3ResponseRate) {
    this.t3ResponseRate = t3ResponseRate;
  }

  public double getT7ResponseRate() {
    return t7ResponseRate;
  }

  public void setT7ResponseRate(double t7ResponseRate) {
    this.t7ResponseRate = t7ResponseRate;
  }

  public double getAverageResponseTime() {
    return averageResponseTime;
  }

  public void setAverageResponseTime(double averageResponseTime) {
    this.averageResponseTime = averageResponseTime;
  }

  public StatusDistribution getStatusDistribution() {
    return statusDistribution;
  }

  public void setStatusDistribution(StatusDistribution statusDistribution) {
    this.statusDistribution = statusDistribution;
  }

  public List<RecentLeadActivity> getRecentActivities() {
    return recentActivities;
  }

  public void setRecentActivities(List<RecentLeadActivity> recentActivities) {
    this.recentActivities = recentActivities;
  }

  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public boolean isRealTimeEnabled() {
    return realTimeEnabled;
  }

  public void setRealTimeEnabled(boolean realTimeEnabled) {
    this.realTimeEnabled = realTimeEnabled;
  }

  /** Berechnet Health Score für Lead-Pipeline. */
  public double getPipelineHealthScore() {
    if (statusDistribution == null) return 0;

    double score = 0;
    // Gewichtete Berechnung basierend auf Status-Verteilung
    score += statusDistribution.getQualified() * 0.3;
    score += statusDistribution.getContacted() * 0.2;
    score += statusDistribution.getNegotiating() * 0.4;
    score += statusDistribution.getConversionRate() * 0.1;

    return Math.min(100, score);
  }
}
