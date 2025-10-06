package de.freshplan.modules.leads.api.admin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request for converting Lead → Customer.
 *
 * <p>Sprint 2.1.6 - User Story 2 (Bestandsleads-Migration Phase 2)
 */
public class LeadConvertRequest {

  /** Optional custom customer number (auto-generated if not provided). */
  @Size(max = 20)
  public String customerNumber;

  /** Optional notes about the conversion reason. */
  @Size(max = 500)
  public String conversionNotes;

  /**
   * Optional flag to mark Lead as "converted" instead of deleting it. Default: true (soft-delete).
   */
  @NotNull public Boolean keepLeadRecord = true;
}
