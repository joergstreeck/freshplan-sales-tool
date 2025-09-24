package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Territory configuration for currency, tax and business rules. Sprint 2.1: NO geographical
 * protection - territories are only for business configuration.
 */
@Entity
@Table(name = "territories")
public class Territory extends PanacheEntityBase {

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
  public JsonObject businessRules = new JsonObject();

  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt = LocalDateTime.now();

  // Helper methods
  public static Territory findByCode(String code) {
    return find("id", code).firstResult();
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
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
