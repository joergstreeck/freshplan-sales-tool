package de.freshplan.domain.calculator.service.dto;

/** Response DTO for calculator discount calculations. */
public class CalculatorResponse {

  // Input parameters (echo back)
  private Double orderValue;
  private Integer leadTime;
  private Boolean pickup;
  private Boolean chain;

  // Calculated discounts
  private Double baseDiscount;
  private Double earlyDiscount;
  private Double pickupDiscount;
  private Double chainDiscount;
  private Double totalDiscount;

  // Calculated amounts
  private Double discountAmount;
  private Double savingsAmount;
  private Double finalPrice;

  // Builder pattern for easier construction
  public static Builder builder() {
    return new Builder();
  }

  // Getters
  public Double getOrderValue() {
    return orderValue;
  }

  public Integer getLeadTime() {
    return leadTime;
  }

  public Boolean getPickup() {
    return pickup;
  }

  public Boolean getChain() {
    return chain;
  }

  public Double getBaseDiscount() {
    return baseDiscount;
  }

  public Double getEarlyDiscount() {
    return earlyDiscount;
  }

  public Double getPickupDiscount() {
    return pickupDiscount;
  }

  public Double getChainDiscount() {
    return chainDiscount;
  }

  public Double getTotalDiscount() {
    return totalDiscount;
  }

  public Double getDiscountAmount() {
    return discountAmount;
  }

  public Double getSavingsAmount() {
    return savingsAmount;
  }

  public Double getFinalPrice() {
    return finalPrice;
  }

  // Builder class
  public static class Builder {
    private final CalculatorResponse response = new CalculatorResponse();

    public Builder orderValue(Double orderValue) {
      response.orderValue = orderValue;
      return this;
    }

    public Builder leadTime(Integer leadTime) {
      response.leadTime = leadTime;
      return this;
    }

    public Builder pickup(Boolean pickup) {
      response.pickup = pickup;
      return this;
    }

    public Builder chain(Boolean chain) {
      response.chain = chain;
      return this;
    }

    public Builder baseDiscount(Double baseDiscount) {
      response.baseDiscount = baseDiscount;
      return this;
    }

    public Builder earlyDiscount(Double earlyDiscount) {
      response.earlyDiscount = earlyDiscount;
      return this;
    }

    public Builder pickupDiscount(Double pickupDiscount) {
      response.pickupDiscount = pickupDiscount;
      return this;
    }

    public Builder chainDiscount(Double chainDiscount) {
      response.chainDiscount = chainDiscount;
      return this;
    }

    public Builder totalDiscount(Double totalDiscount) {
      response.totalDiscount = totalDiscount;
      return this;
    }

    public Builder discountAmount(Double discountAmount) {
      response.discountAmount = discountAmount;
      return this;
    }

    public Builder savingsAmount(Double savingsAmount) {
      response.savingsAmount = savingsAmount;
      return this;
    }

    public Builder finalPrice(Double finalPrice) {
      response.finalPrice = finalPrice;
      return this;
    }

    public CalculatorResponse build() {
      return response;
    }
  }
}
