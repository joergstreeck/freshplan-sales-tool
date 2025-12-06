package de.freshplan.modules.leads.api.selfservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request für Schritt 4: Import ausführen
 *
 * @since Sprint 2.1.8
 */
public record ImportExecuteRequest(
    @NotNull @NotEmpty Map<String, String> mapping,
    DuplicateAction duplicateAction,
    String source, // z.B. "MESSE_FRANKFURT_2025"
    boolean ignoreErrors // Fehlerhafe Zeilen überspringen
    ) {

  public enum DuplicateAction {
    SKIP, // Überspringen (default)
    CREATE // Trotzdem anlegen
  }

  public ImportExecuteRequest {
    if (duplicateAction == null) {
      duplicateAction = DuplicateAction.SKIP;
    }
  }
}
