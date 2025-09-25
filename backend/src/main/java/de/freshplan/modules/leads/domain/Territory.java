package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Territory configuration for currency, tax and business rules. Sprint 2.1: NO geographical
 * protection - territories are only for business configuration.
 */
@Entity
@Table(name = "territories")
public class Territory extends PanacheEntityBase {

  private static final Logger LOG = Logger.getLogger(Territory.class);

  @Id
  @Size(max = 10)
  @Column(name = "id", length = 10)
  public String id;

  @NotNull @Size(max = 100)
  @Column(name = "name", nullable = false, length = 100)
  public String name;

  @NotNull @Size(max = 2)
  @Column(name = "country_code", nullable = false, length = 2)
  public String countryCode;

  @NotNull @Size(max = 3)
  @Column(name = "currency_code", nullable = false, length = 3)
  public String currencyCode;

  @NotNull @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
  public BigDecimal taxRate;

  @NotNull @Size(max = 5)
  @Column(name = "language_code", nullable = false, length = 5)
  public String languageCode;

  @Column(name = "business_rules", columnDefinition = "jsonb")
  @Convert(converter = JsonObjectConverter.class)
  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  public JsonObject businessRules = new JsonObject();

  @Column(name = "active", nullable = false)
  public boolean active = true;

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt;

  // Simplified field names for easier access
  @Transient
  public String code() {
    return id;
  }

  @Transient
  public String currency() {
    return currencyCode;
  }

  // Helper methods
  public static Territory findByCode(String code) {
    return find("id", code).firstResult();
  }

  public static Territory findByCountryCode(String countryCode) {
    return find("countryCode", countryCode).firstResult();
  }

  /**
   * Returns the default territory (Germany) from database. WARNING: If no territory exists in DB,
   * returns a transient instance. Callers must ensure the instance is persisted if needed.
   *
   * @return Territory instance for Germany (may be transient if DB is empty)
   */
  public static Territory getDefault() {
    // Deutschland als Default Territory
    Territory defaultTerritory = findByCode("DE");
    if (defaultTerritory == null) {
      // Return transient territory wenn DB leer ist
      // Caller is responsible for persisting if needed
      // This avoids transaction issues in static context
      defaultTerritory = new Territory();
      defaultTerritory.id = "DE";
      defaultTerritory.name = "Deutschland";
      defaultTerritory.countryCode = "DE";
      defaultTerritory.currencyCode = "EUR";
      defaultTerritory.taxRate = new BigDecimal("19.00");
      defaultTerritory.languageCode = "de-DE";
      defaultTerritory.active = true;

      // Log warning to alert about transient instance
      LOG.warn(
          "Territory 'DE' not found in database, returning transient instance. "
              + "Ensure it's persisted before using with other entities.");
    }
    return defaultTerritory;
  }

  public boolean isGermany() {
    return "DE".equals(id);
  }

  public boolean isSwitzerland() {
    return "CH".equals(id);
  }

  /** Get formatted tax rate for display (e.g. "19.00%" for Germany). */
  public String getFormattedTaxRate() {
    return taxRate.setScale(2, java.math.RoundingMode.HALF_UP) + "%";
  }

  /** Get payment terms from business rules. */
  public Integer getPaymentTerms() {
    if (businessRules != null && businessRules.containsKey("payment_terms")) {
      return businessRules.getInteger("payment_terms");
    }
    return 30; // Default
  }

  @PrePersist
  protected void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
