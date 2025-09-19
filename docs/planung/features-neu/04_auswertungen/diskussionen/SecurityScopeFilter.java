package com.freshplan.security;

import com.freshplan.reports.ScopeContext;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityScopeFilter implements ContainerRequestFilter {

  @Inject ScopeContext scope;

  @Override
  public void filter(ContainerRequestContext ctx) {
    // Pseudocode: Claims aus JWT auslesen (abhängig von Security-Framework)
    // Beispiel: territories = claim 'territories' (Array), chainId = claim 'chain_id'
    List<String> territories = List.of(); // TODO: extract from JWT
    String chainId = null;                // TODO: extract from JWT
    scope.setTerritories(territories);
    scope.setChainId(chainId);
  }
}
