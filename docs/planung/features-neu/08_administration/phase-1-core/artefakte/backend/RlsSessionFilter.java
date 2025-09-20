package de.freshplan.admin.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/** Sets DB session variables each request so RLS policies take effect. */
@Provider
public class RlsSessionFilter implements ContainerRequestFilter {

  @Inject AdminSecurityService sec;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    sec.establishRlsSession();
  }
}
