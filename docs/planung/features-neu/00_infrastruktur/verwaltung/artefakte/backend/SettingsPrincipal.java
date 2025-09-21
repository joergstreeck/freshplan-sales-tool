package de.freshplan.governance.settings;

import org.eclipse.microprofile.jwt.JsonWebToken;
import java.util.*;
import java.util.stream.Collectors;

public record SettingsPrincipal(
    java.util.UUID userId,
    java.util.Optional<java.util.UUID> tenantId,
    java.util.Optional<java.util.UUID> orgId,
    java.util.Set<String> roles
) {
  public static SettingsPrincipal fromJwt(JsonWebToken jwt){
    java.util.UUID u = java.util.Optional.ofNullable(jwt.getSubject()).map(java.util.UUID::fromString).orElseThrow();
    java.util.Optional<java.util.UUID> tenant = java.util.Optional.ofNullable((String) jwt.getClaim("tenant_id")).map(java.util.UUID::fromString);
    java.util.Optional<java.util.UUID> org    = java.util.Optional.ofNullable((String) jwt.getClaim("org_id")).map(java.util.UUID::fromString);
    java.util.Set<String> roles = java.util.Optional.ofNullable((java.util.Collection<String>) jwt.getClaim("roles"))
        .map(coll -> coll.stream().collect(java.util.stream.Collectors.toSet()))
        .orElseGet(java.util.HashSet::new);
    return new SettingsPrincipal(u, tenant, org, roles);
  }

  public String cacheKeyFor(java.util.Set<String> keys){
    return (tenantId.map(java.util.UUID::toString).orElse("-")+"|"+orgId.map(java.util.UUID::toString).orElse("-")+"|"+userId+"|"+String.join(",", new java.util.TreeSet<>(keys)));
  }
}
