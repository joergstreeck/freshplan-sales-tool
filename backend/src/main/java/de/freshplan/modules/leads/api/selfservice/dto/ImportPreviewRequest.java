package de.freshplan.modules.leads.api.selfservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request für Schritt 2/3: Mapping + Preview
 *
 * @since Sprint 2.1.8
 */
public record ImportPreviewRequest(
    @NotNull @NotEmpty Map<String, String> mapping // Datei-Spalte → Lead-Feld
    ) {}
