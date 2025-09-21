package de.freshplan.integration.sync;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileWriter;
import java.util.*;

public class KongDeckGenerator {

  @SuppressWarnings("unchecked")
  public String render(Map<String, String> settings, String kongAddr, String outFile) throws Exception {
    // Basiskonfiguration (Service + Route)
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("_format_version", "3.0");
    root.put("_transform", true);

    Map<String, Object> service = new LinkedHashMap<>();
    service.put("name", "api-backend");
    service.put("url", "http://backend:8080");

    Map<String, Object> route = new LinkedHashMap<>();
    route.put("name", "api-route");
    route.put("paths", List.of("/api"));
    route.put("methods", List.of("GET","POST","PUT","PATCH","DELETE"));
    route.put("strip_path", false);

    Map<String, Object> cors = new LinkedHashMap<>();
    cors.put("name", "cors");
    Map<String, Object> corsCfg = new LinkedHashMap<>();
    List<String> origins = parseStringList(settings.getOrDefault("gateway.cors.allowed.origins", "[]"));
    corsCfg.put("origins", origins);
    corsCfg.put("methods", List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    corsCfg.put("headers", List.of("Authorization","Content-Type","If-Match","If-None-Match","Idempotency-Key","X-Correlation-Id","X-Tenant-Id","X-Org-Id"));
    corsCfg.put("exposed_headers", List.of("ETag"));
    corsCfg.put("credentials", true);
    corsCfg.put("max_age", 86400);
    cors.put("config", corsCfg);

    Map<String, Object> rl = new LinkedHashMap<>();
    rl.put("name", "rate-limiting-advanced");
    Map<String, Object> rlCfg = new LinkedHashMap<>();
    rlCfg.put("identifier", "header");
    rlCfg.put("header_name", "x-tenant-id");
    rlCfg.put("limit", List.of(parseInt(settings.getOrDefault("gateway.rate.limit.rpm", "2000"))));
    rlCfg.put("window_size", List.of(60));
    rlCfg.put("sync_rate", -1);
    rlCfg.put("strategy", "redis");
    Map<String,Object> redis = new LinkedHashMap<>();
    redis.put("host", "redis");
    redis.put("port", 6379);
    rlCfg.put("redis", redis);
    rl.put("config", rlCfg);

    boolean authEnabled = Boolean.parseBoolean(settings.getOrDefault("gateway.auth.enabled","true"));
    Map<String, Object> oidc = null;
    if (authEnabled){
      oidc = new LinkedHashMap<>();
      oidc.put("name","openid-connect");
      Map<String,Object> oCfg = new LinkedHashMap<>();
      oCfg.put("issuer", "https://keycloak/auth/realms/freshplan");
      oCfg.put("client_id","freshplan-gateway");
      oCfg.put("scopes", List.of("openid","profile","email","roles"));
      oCfg.put("auth_methods", List.of("bearer"));
      oCfg.put("introspection_endpoint_auth_method","client_secret_post");
      oidc.put("config", oCfg);
    }

    List<Map<String,Object>> plugins = new ArrayList<>();
    plugins.add(cors);
    plugins.add(rl);
    if (oidc != null) plugins.add(oidc);

    Map<String, Object> routeWithPlugins = new LinkedHashMap<>(route);
    routeWithPlugins.put("plugins", plugins);

    Map<String, Object> serviceWithRoutes = new LinkedHashMap<>(service);
    serviceWithRoutes.put("routes", List.of(routeWithPlugins));

    root.put("services", List.of(serviceWithRoutes));

    DumperOptions opts = new DumperOptions();
    opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Representer repr = new Representer(opts);
    repr.getPropertyUtils().setSkipMissingProperties(true);
    Yaml yaml = new Yaml(repr, opts);

    try (FileWriter fw = new FileWriter(outFile)){
      yaml.dump(root, fw);
    }
    return outFile;
  }

  private static int parseInt(String s){
    try { return Integer.parseInt(s); } catch(Exception e){ return 2000; }
  }
  private static List<String> parseStringList(String json){
    if (json == null || json.isBlank()) return List.of();
    String t = json.trim();
    if (t.startsWith("[")) {
      // very naive parsing (expects '["a","b"]')
      t = t.substring(1, t.length()-1).trim();
      if (t.isEmpty()) return List.of();
      String[] parts = t.split(",");
      List<String> out = new ArrayList<>();
      for (String p : parts){
        out.add(p.trim().replaceAll("^"|"$", ""));
      }
      return out;
    }
    return List.of(t);
  }
}
