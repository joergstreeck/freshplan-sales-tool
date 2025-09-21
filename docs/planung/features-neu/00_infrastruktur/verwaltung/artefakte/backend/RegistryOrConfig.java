package de.freshplan.governance.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class RegistryOrConfig {

  @Inject SettingsService service;
  @Inject ObjectMapper mapper;
  @Inject MeterRegistry metrics;
  @Inject JsonWebToken jwt;

  private String moduleLabel = "core";

  public Optional<JsonNode> getJson(String key){
    var p = SettingsPrincipal.fromJwt(jwt);
    var res = service.getEffective(p, Set.of(key));
    if (res.payload().has(key)){
      metrics.counter("settings_lookup_source", "module", moduleLabel, "source","registry").increment();
      return Optional.of(res.payload().get(key));
    }
    String raw = ConfigProvider.getConfig().getOptionalValue(key, String.class).orElse(null);
    if (raw != null){
      metrics.counter("settings_lookup_source", "module", moduleLabel, "source","config").increment();
      try { return Optional.of(mapper.readTree(raw)); } catch (Exception e){ return Optional.empty(); }
    }
    return Optional.empty();
  }

  public Optional<String> getString(String key){
    return getJson(key).map(JsonNode::asText);
  }
}
