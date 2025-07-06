package de.freshplan.domain.customer.service.dto.timeline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a quick note.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class CreateNoteRequest {

  @NotBlank(message = "Note content is required")
  @Size(max = 5000)
  private String note;

  @NotBlank(message = "Performed by is required")
  @Size(max = 100)
  private String performedBy;

  // Constructors
  public CreateNoteRequest() {}

  public CreateNoteRequest(String note, String performedBy) {
    this.note = note;
    this.performedBy = performedBy;
  }

  // Getters and Setters
  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getPerformedBy() {
    return performedBy;
  }

  public void setPerformedBy(String performedBy) {
    this.performedBy = performedBy;
  }
}
