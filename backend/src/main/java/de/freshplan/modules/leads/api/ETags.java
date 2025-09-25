package de.freshplan.modules.leads.api;

/**
 * ETag generation utilities for optimistic concurrency control.
 * Generates weak ETags based on entity ID and version.
 */
public final class ETags {

  private ETags() {
    // Utility class
  }

  /**
   * Generate weak ETag for a Lead entity.
   * Format: W/"lead-{id}-{version}"
   *
   * @param id the lead ID
   * @param version the entity version
   * @return weak ETag string
   */
  public static String weakLead(long id, long version) {
    return "W/\"lead-%d-%d\"".formatted(id, version);
  }

  /**
   * Generate weak ETag for any entity type.
   * Format: W/"{type}-{id}-{version}"
   *
   * @param type the entity type name
   * @param id the entity ID
   * @param version the entity version
   * @return weak ETag string
   */
  public static String weak(String type, long id, long version) {
    return "W/\"%s-%d-%d\"".formatted(type.toLowerCase(), id, version);
  }

  /**
   * Parse ETag to extract version.
   * Returns -1 if ETag is invalid or cannot be parsed.
   *
   * @param etag the ETag string
   * @return version number or -1 if invalid
   */
  public static long parseVersion(String etag) {
    if (etag == null || !etag.startsWith("W/\"") || !etag.endsWith("\"")) {
      return -1;
    }

    try {
      String content = etag.substring(3, etag.length() - 1); // Remove W/" and trailing "
      String[] parts = content.split("-");
      if (parts.length >= 3) {
        return Long.parseLong(parts[parts.length - 1]); // Last part is version
      }
    } catch (NumberFormatException e) {
      // Invalid format
    }
    return -1;
  }
}