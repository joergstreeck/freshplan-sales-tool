package de.freshplan.domain.cost.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Budget Limits Entity - Konfigurierbare Budget-Limits für verschiedene Services und Features
 *
 * <p>Ermöglicht flexible Budget-Kontrolle auf Service- und Feature-Ebene
 */
@Entity
@Table(name = "budget_limits")
public class BudgetLimit extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public UUID id;

  @Column(nullable = false, length = 50)
  public String scope; // "global", "service", "feature", "user"

  @Column(name = "scope_value", length = 100)
  public String scopeValue; // "openai", "smart-suggestions", "user123", etc.

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  public BudgetPeriod period;

  @Column(name = "limit_amount", nullable = false, precision = 10, scale = 2)
  public BigDecimal limitAmount;

  @Column(name = "alert_threshold", nullable = false, precision = 3, scale = 2)
  public BigDecimal alertThreshold; // 0.0 - 1.0 (z.B. 0.8 = 80%)

  @Column(name = "hard_stop_threshold", nullable = false, precision = 3, scale = 2)
  public BigDecimal hardStopThreshold; // 0.0 - 1.0 (z.B. 0.95 = 95%)

  @Column(nullable = false)
  public boolean active;

  @Column(name = "created_at")
  public LocalDateTime createdAt;

  @Column(name = "updated_at")
  public LocalDateTime updatedAt;

  @Column(length = 200)
  public String description; // Beschreibung des Limits

  // Konstruktoren
  public BudgetLimit() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.active = true;
    this.alertThreshold = BigDecimal.valueOf(0.8);
    this.hardStopThreshold = BigDecimal.valueOf(0.95);
  }

  public BudgetLimit(String scope, String scopeValue, BudgetPeriod period, BigDecimal limitAmount) {
    this();
    this.scope = scope;
    this.scopeValue = scopeValue;
    this.period = period;
    this.limitAmount = limitAmount;
  }

  // Factory Methods
  public static BudgetLimit createGlobalDailyLimit(BigDecimal amount) {
    return new BudgetLimit("global", null, BudgetPeriod.DAILY, amount);
  }

  public static BudgetLimit createServiceLimit(
      String service, BudgetPeriod period, BigDecimal amount) {
    return new BudgetLimit("service", service, period, amount);
  }

  public static BudgetLimit createFeatureLimit(
      String feature, BudgetPeriod period, BigDecimal amount) {
    return new BudgetLimit("feature", feature, period, amount);
  }

  // Business Methods
  public BigDecimal getAlertAmount() {
    return limitAmount.multiply(alertThreshold);
  }

  public BigDecimal getHardStopAmount() {
    return limitAmount.multiply(hardStopThreshold);
  }

  public boolean shouldAlert(BigDecimal currentUsage) {
    return currentUsage.compareTo(getAlertAmount()) >= 0;
  }

  public boolean shouldHardStop(BigDecimal currentUsage) {
    return currentUsage.compareTo(getHardStopAmount()) >= 0;
  }

  public String getDisplayName() {
    if ("global".equals(scope)) {
      return "Globales Budget";
    } else if ("service".equals(scope)) {
      return "Service: " + scopeValue;
    } else if ("feature".equals(scope)) {
      return "Feature: " + scopeValue;
    } else if ("user".equals(scope)) {
      return "User: " + scopeValue;
    }
    return scope + ": " + scopeValue;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return String.format(
        "BudgetLimit{scope='%s', scopeValue='%s', period=%s, limit=%s}",
        scope, scopeValue, period, limitAmount);
  }
}
