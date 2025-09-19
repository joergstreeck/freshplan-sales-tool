package de.freshplan.security;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import java.util.*;

@Provider
@Priority(Priorities.AUTHORIZATION)
@ApplicationScoped
public class SecurityScopeFilter implements ContainerRequestFilter {

  @Inject ScopeContext scope;
  @Inject JWTParser jwtParser;

  private static final String CLAIM_TERR = System.getProperty("app.security.claims.territories","territories");
  private static final String CLAIM_CHAIN = System.getProperty("app.security.claims.chain_id","chain_id");
  private static final String CLAIM_CHANNELS = System.getProperty("app.security.claims.channels","channels");
  private static final boolean ALLOW_HEADER_SCOPES = Boolean.parseBoolean(System.getProperty("app.security.allowHeaderScopesInDev","true"));

  @Override
  public void filter(ContainerRequestContext ctx) {
    List<String> terrs = new ArrayList<>();
    List<String> chans = new ArrayList<>();
    String chain = null;

    String auth = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      try {
        var jwt = jwtParser.parse(auth.substring(7));
        Object t = jwt.getClaim(CLAIM_TERR);
        if (t instanceof List<?> list) for (Object o : list) terrs.add(String.valueOf(o));
        else if (t instanceof String s && !s.isBlank()) terrs.addAll(Arrays.asList(s.split(",")));
        Object c = jwt.getClaim(CLAIM_CHAIN); if (c != null) chain = String.valueOf(c);
        Object ch = jwt.getClaim(CLAIM_CHANNELS);
        if (ch instanceof List<?> list) for (Object o : list) chans.add(String.valueOf(o));
        else if (ch instanceof String s && !s.isBlank()) chans.addAll(Arrays.asList(s.split(",")));
      } catch (ParseException e) {
        // ignore; may fallback in dev
      }
    }
    if (ALLOW_HEADER_SCOPES) {
      if (terrs.isEmpty()) {
        String h = ctx.getHeaderString("X-Territories"); if (h != null) terrs.addAll(Arrays.asList(h.split(",")));
      }
      if (chans.isEmpty()) {
        String h = ctx.getHeaderString("X-Channels"); if (h != null) chans.addAll(Arrays.asList(h.split(",")));
      }
      if (chain == null) chain = ctx.getHeaderString("X-Chain-Id");
    }

    scope.setTerritories(terrs);
    scope.setChannels(chans);
    scope.setChainId(chain);

    if (scope.getTerritories().isEmpty()) {
      throw new ForbiddenException("No authorized territories for user");
    }
  }
}
