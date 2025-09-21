package de.freshplan.admin.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.ForbiddenException;
import java.util.List;
import java.util.Set;
import de.freshplan.security.ScopeContext;

/** ABAC helper and RLS session setter (fail-closed). */
@ApplicationScoped
public class AdminSecurityService {

  @Inject EntityManager em;
  @Inject ScopeContext scope;

  /** Must be called once per request (e.g., in a ContainerRequestFilter) to enforce RLS. */
  public void establishRlsSession(){
    var tenant = scope.getTenantId() == null ? null : scope.getTenantId().toString();
    var territory = scope.getTerritory();
    var user = scope.getUserId() == null ? null : scope.getUserId().toString();
    var org = scope.getOrgId() == null ? null : scope.getOrgId().toString();
    em.createNativeQuery("SELECT set_config('app.tenant_id', :v, true)").setParameter("v", tenant).getSingleResult();
    em.createNativeQuery("SELECT set_config('app.territory', :v, true)").setParameter("v", territory).getSingleResult();
    em.createNativeQuery("SELECT set_config('app.user_id', :v, true)").setParameter("v", user).getSingleResult();
    em.createNativeQuery("SELECT set_config('app.org_id', :v, true)").setParameter("v", org).getSingleResult();
  }

  /** Basic ABAC enforcement for admin actions. */
  public void enforce(String action, String resourceType, String territory, String orgId){
    // Fail-closed defaults
    if (scope.getUserId() == null) throw new ForbiddenException("No user in scope");
    // Territory: if supplied, must be within user territories
    if (territory != null && !territory.isBlank()) {
      List<String> terrs = scope.getTerritories();
      if (terrs == null || !terrs.contains(territory)) {
        throw new ForbiddenException("Territory not allowed");
      }
    }
    // Org: if supplied, user must own/manage this org or be admin/security
    if (orgId != null && scope.getOrgId() != null && !scope.getOrgId().toString().equals(orgId)) {
      var roles = scope.getRoles();
      if (roles == null || !(roles.contains("admin") || roles.contains("security"))) {
        throw new ForbiddenException("Org not allowed");
      }
    }
  }
}
