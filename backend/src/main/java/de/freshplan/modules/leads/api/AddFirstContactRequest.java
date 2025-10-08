package de.freshplan.modules.leads.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for adding first contact to a Pre-Claim lead (Sprint 2.1.6 Phase 5).
 *
 * <p>This endpoint transitions a lead from VORMERKUNG (Stage 0) to REGISTRIERUNG (Stage 1) by:
 *
 * <ul>
 *   <li>Setting contact person details (name, email, phone)
 *   <li>Creating a FIRST_CONTACT_DOCUMENTED activity
 *   <li>Activating lead protection (registeredAt = NOW())
 *   <li>Transitioning stage: VORMERKUNG → REGISTRIERUNG
 * </ul>
 *
 * @since 2.1.6 Phase 5
 */
public class AddFirstContactRequest {

  /** Contact person name (required). */
  @NotNull(message = "Contact person name is required")
  @Size(min = 2, max = 255, message = "Contact person name must be between 2 and 255 characters")
  public String contactPerson;

  /** Contact email (optional, but recommended). */
  @Email(message = "Invalid email format")
  @Size(max = 255, message = "Email must be max 255 characters")
  public String email;

  /** Contact phone (optional, but recommended). */
  @Size(max = 50, message = "Phone must be max 50 characters")
  public String phone;

  /**
   * Date/time of first contact (optional, defaults to NOW()).
   *
   * <p>If provided, must not be in the future.
   */
  public LocalDateTime contactDate;

  /** Notes about the first contact (optional). */
  @Size(max = 1000, message = "Notes must be max 1000 characters")
  public String notes;
}
