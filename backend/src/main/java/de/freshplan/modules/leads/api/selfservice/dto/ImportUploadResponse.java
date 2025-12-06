package de.freshplan.modules.leads.api.selfservice.dto;

import java.util.List;
import java.util.Map;

/**
 * Response für Schritt 1: File Upload
 *
 * @since Sprint 2.1.8
 */
public record ImportUploadResponse(
    String uploadId,
    List<String> columns,
    Map<String, String> suggestedMapping,
    int rowCount,
    String fileType,
    String charset,
    List<LeadFieldInfo> availableFields) {

  public record LeadFieldInfo(String key, String label, boolean required) {}
}
