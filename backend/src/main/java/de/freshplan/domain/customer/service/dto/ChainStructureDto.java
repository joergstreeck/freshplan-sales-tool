package de.freshplan.domain.customer.service.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO for customer chain structure information. Represents the distribution of locations across
 * different regions.
 *
 * @since 2.0.0
 */
public class ChainStructureDto {

  @Min(value = 0, message = "Total locations EU must be non-negative")
  private Integer totalLocationsEU;

  @Min(value = 0, message = "Locations Germany must be non-negative")
  private Integer locationsGermany;

  @Min(value = 0, message = "Locations Austria must be non-negative")
  private Integer locationsAustria;

  @Min(value = 0, message = "Locations Switzerland must be non-negative")
  private Integer locationsSwitzerland;

  @Min(value = 0, message = "Locations Rest EU must be non-negative")
  private Integer locationsRestEU;

  // Constructors
  public ChainStructureDto() {}

  public ChainStructureDto(
      Integer totalLocationsEU,
      Integer locationsGermany,
      Integer locationsAustria,
      Integer locationsSwitzerland,
      Integer locationsRestEU) {
    this.totalLocationsEU = totalLocationsEU;
    this.locationsGermany = locationsGermany;
    this.locationsAustria = locationsAustria;
    this.locationsSwitzerland = locationsSwitzerland;
    this.locationsRestEU = locationsRestEU;
  }

  // Validation method
  public boolean isValid() {
    if (totalLocationsEU == null) {
      return true; // No validation if total not provided
    }

    int sum =
        (locationsGermany != null ? locationsGermany : 0)
            + (locationsAustria != null ? locationsAustria : 0)
            + (locationsSwitzerland != null ? locationsSwitzerland : 0)
            + (locationsRestEU != null ? locationsRestEU : 0);

    return sum <= totalLocationsEU;
  }

  // Getters and Setters
  public Integer getTotalLocationsEU() {
    return totalLocationsEU;
  }

  public void setTotalLocationsEU(Integer totalLocationsEU) {
    this.totalLocationsEU = totalLocationsEU;
  }

  public Integer getLocationsGermany() {
    return locationsGermany;
  }

  public void setLocationsGermany(Integer locationsGermany) {
    this.locationsGermany = locationsGermany;
  }

  public Integer getLocationsAustria() {
    return locationsAustria;
  }

  public void setLocationsAustria(Integer locationsAustria) {
    this.locationsAustria = locationsAustria;
  }

  public Integer getLocationsSwitzerland() {
    return locationsSwitzerland;
  }

  public void setLocationsSwitzerland(Integer locationsSwitzerland) {
    this.locationsSwitzerland = locationsSwitzerland;
  }

  public Integer getLocationsRestEU() {
    return locationsRestEU;
  }

  public void setLocationsRestEU(Integer locationsRestEU) {
    this.locationsRestEU = locationsRestEU;
  }
}
