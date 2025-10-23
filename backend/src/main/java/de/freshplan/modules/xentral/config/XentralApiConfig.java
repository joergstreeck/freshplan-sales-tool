package de.freshplan.modules.xentral.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configuration for Xentral API integration.
 *
 * <p>Maps application.properties xentral.* settings to a type-safe configuration object.
 *
 * @author FreshPlan Team
 * @since 2.1.7.2
 */
@ConfigMapping(prefix = "xentral")
public interface XentralApiConfig {

  /**
   * Base URL of the Xentral API.
   *
   * @return Base URL (e.g., "https://example.xentral.biz")
   */
  @WithName("base-url")
  @WithDefault("https://example.xentral.biz")
  String baseUrl();

  /**
   * API token for Xentral authentication.
   *
   * @return API token
   */
  @WithName("api-token")
  @WithDefault("")
  String token();

  /**
   * Mock mode flag - if true, uses MockXentralApiClient instead of real API.
   *
   * @return true if mock mode is enabled
   */
  @WithName("mock-mode")
  @WithDefault("true")
  boolean mockMode();
}
