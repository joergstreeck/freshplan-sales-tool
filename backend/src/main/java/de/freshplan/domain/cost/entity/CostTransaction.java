package de.freshplan.domain.cost.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cost Transaction Entity - Tracks individual API/service costs
 *
 * <p>Entspricht der Cost Management Architektur für präzise Kostenverfolgung von externen Services
 * wie OpenAI, Anthropic, etc.
 */
@Entity
@Table(name = "cost_transactions")
public class CostTransaction extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public UUID id;

  @Column(nullable = false, length = 50)
  public String service; // "openai", "anthropic", "local", "rules"

  @Column(nullable = false, length = 100)
  public String feature; // "smart-suggestions", "data-analysis", etc.

  @Column(nullable = false, length = 50)
  public String model; // "gpt-4", "gpt-3.5-turbo", "claude-3", etc.

  @Column(name = "estimated_cost", nullable = false, precision = 10, scale = 4)
  public BigDecimal estimatedCost; // Vorab-Schätzung

  @Column(name = "actual_cost", precision = 10, scale = 4)
  public BigDecimal actualCost; // Tatsächliche Kosten

  @Column(name = "tokens_used")
  public Integer tokensUsed; // Token-Verbrauch für API-Calls

  @Column(name = "tokens_estimated")
  public Integer tokensEstimated; // Geschätzte Tokens

  @Column(name = "start_time", nullable = false)
  public LocalDateTime startTime;

  @Column(name = "end_time")
  public LocalDateTime endTime;

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  public TransactionStatus status;

  @Column(name = "user_id", length = 100)
  public String userId; // Welcher User hat die Anfrage gestellt

  @Column(name = "request_context", length = 200)
  public String requestContext; // Zusätzlicher Kontext für Debugging

  @Column(name = "error_message", columnDefinition = "TEXT")
  public String errorMessage; // Falls Transaktion fehlschlägt

  @Column(name = "created_at")
  public LocalDateTime createdAt;

  @Column(name = "updated_at")
  public LocalDateTime updatedAt;

  // Konstruktoren
  public CostTransaction() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.status = TransactionStatus.STARTED;
  }

  public CostTransaction(String service, String feature, String model, BigDecimal estimatedCost) {
    this();
    this.service = service;
    this.feature = feature;
    this.model = model;
    this.estimatedCost = estimatedCost;
    this.startTime = LocalDateTime.now();
  }

  // Factory Methods
  public static CostTransaction startTransaction(
      String service, String feature, String model, BigDecimal estimatedCost) {
    return new CostTransaction(service, feature, model, estimatedCost);
  }

  // Business Methods
  public void complete(BigDecimal actualCost, Integer tokensUsed) {
    this.actualCost = actualCost;
    this.tokensUsed = tokensUsed;
    this.endTime = LocalDateTime.now();
    this.status = TransactionStatus.COMPLETED;
    this.updatedAt = LocalDateTime.now();
  }

  public void fail(String errorMessage) {
    this.errorMessage = errorMessage;
    this.endTime = LocalDateTime.now();
    this.status = TransactionStatus.FAILED;
    this.updatedAt = LocalDateTime.now();
  }

  public BigDecimal getCostDifference() {
    if (actualCost == null || estimatedCost == null) {
      return BigDecimal.ZERO;
    }
    return actualCost.subtract(estimatedCost);
  }

  public boolean isOverBudget() {
    return getCostDifference().compareTo(BigDecimal.ZERO) > 0;
  }

  public Long getDurationMs() {
    if (startTime == null || endTime == null) {
      return null;
    }
    return java.time.Duration.between(startTime, endTime).toMillis();
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  @Override
  public String toString() {
    return String.format(
        "CostTransaction{id=%s, service='%s', feature='%s', model='%s', estimatedCost=%s, actualCost=%s, status=%s}",
        id, service, feature, model, estimatedCost, actualCost, status);
  }
}
