package de.freshplan.security;

import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import java.util.*;

/**
 * ABAC-Secured Request Filter for Territory-based Access Control
 *
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Implementation
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE REST Standards
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 *
 * This filter provides territory-based access control via JWT claims
 * and follows Foundation Standards for ABAC security patterns.
 *
 * @author Security Team
 * @version 1.1
 * @since 2025-09-19
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
@ApplicationScoped
public class SecurityScopeFilter implements ContainerRequestFilter {

  @Inject ScopeContext scope;
  @Inject JsonWebToken jwt;

  private static final String CLAIM_TERR = System.getProperty("app.security.claims.territories", "territories");
  private static final String CLAIM_CHAIN = System.getProperty("app.security.claims.chain_id", "chain_id");
  private static final boolean ALLOW_HEADER_SCOPES = Boolean.parseBoolean(
      System.getProperty("app.security.allowHeaderScopesInDev", "true")
  );

  @Override
  public void filter(ContainerRequestContext ctx) {
    List<String> territories = new ArrayList<>();
    String chainId = null;

    // 1) Try JWT from Quarkus OIDC context
    try {
      if (jwt != null && jwt.getClaimNames() != null) {
        Object terrClaim = jwt.getClaim(CLAIM_TERR);
        if (terrClaim instanceof List<?> list) {
          for (Object o : list) { territories.add(String.valueOf(o)); }
        } else if (terrClaim instanceof String s) {
          // comma-separated fallback
          territories.addAll(Arrays.asList(s.split(",")));
        }
        Object chainClaim = jwt.getClaim(CLAIM_CHAIN);
        if (chainClaim != null) chainId = String.valueOf(chainClaim);
      }
    } catch (Exception e) {
      // ignore invalid token, fall back to headers if allowed
    }

    // 2) Dev/Test fallback via headers (for integration tests)
    if (ALLOW_HEADER_SCOPES) {
      String headerT = ctx.getHeaderString("X-Territories");
      if (headerT != null && territories.isEmpty()) {
        territories.addAll(Arrays.asList(headerT.split(",")));
      }
      String headerC = ctx.getHeaderString("X-Chain-Id");
      if (headerC != null && chainId == null) chainId = headerC;
    }

    scope.setTerritories(territories);
    scope.setChainId(chainId);
  }
}
