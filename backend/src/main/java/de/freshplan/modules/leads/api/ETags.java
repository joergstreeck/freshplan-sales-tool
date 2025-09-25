package de.freshplan.modules.leads.api;

import jakarta.ws.rs.core.EntityTag;

/**
 * ETag generation utilities for optimistic concurrency control. Uses STRONG ETags for If-Match (per
 * HTTP spec) and WEAK ETags for If-None-Match.
 */
public final class ETags {

  private ETags() {
    // Utility class
  }

  /**
   * Generate STRONG ETag for a Lead entity. Format: "lead-{id}-{version}" (no W/ prefix) Used with
   * If-Match for optimistic locking per HTTP spec.
   *
   * @param id the lead ID
   * @param version the entity version
   * @return strong EntityTag for JAX-RS
   */
  public static EntityTag strongLead(long id, long version) {
    // Strong ETag (weak=false) required for If-Match per HTTP spec
    return new EntityTag("lead-%d-%d".formatted(id, version), false);
  }

  /**
   * Generate WEAK ETag for list results. Used with If-None-Match for caching.
   *
   * @param hashCode hash of the result set
   * @return weak EntityTag for JAX-RS
   */
  public static EntityTag weakList(int hashCode) {
    // Weak ETag for list results (If-None-Match only)
    return new EntityTag("list-%d".formatted(hashCode), true);
  }

  /**
   * Convert EntityTag to string representation. Handles both strong and weak ETags correctly.
   *
   * @param tag the EntityTag
   * @return string representation with proper quotes
   */
  public static String toString(EntityTag tag) {
    if (tag.isWeak()) {
      return "W/\"%s\"".formatted(tag.getValue());
    }
    return "\"%s\"".formatted(tag.getValue());
  }

  /**
   * Parse string ETag to EntityTag object. Handles both strong and weak ETags.
   *
   * @param etag the ETag string
   * @return EntityTag object or null if invalid
   */
  public static EntityTag parse(String etag) {
    if (etag == null) return null;

    boolean weak = etag.startsWith("W/");
    String value = etag;

    if (weak) {
      value = etag.substring(2); // Remove W/ prefix
    }

    // Remove surrounding quotes
    if (value.startsWith("\"") && value.endsWith("\"")) {
      value = value.substring(1, value.length() - 1);
    }

    return new EntityTag(value, weak);
  }

  /**
   * Legacy method - kept for backward compatibility during migration.
   *
   * @deprecated Use strongLead() instead
   */
  @Deprecated
  public static String weakLead(long id, long version) {
    return "W/\"lead-%d-%d\"".formatted(id, version);
  }
}
