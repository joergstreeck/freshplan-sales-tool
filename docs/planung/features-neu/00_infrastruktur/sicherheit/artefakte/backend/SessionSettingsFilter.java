package de.freshplan.security;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.hibernate.Session;
import io.quarkus.security.identity.SecurityIdentity;

/**
 * SessionSettingsFilter (Enhanced)
 *
 * Ziel: Keycloak-Claims sicher und performant als PostgreSQL-Session-Settings setzen.
 * - Pool-safe: Settings werden auf der **tatsächlich verwendeten** JDBC-Connection gesetzt
 *   via Hibernate Session#doWork innerhalb einer Transaktion.
 * - Performance: O(1) Statements, Arrays als CSV (serverseitig mit string_to_array genutzt).
 *
 * Verwendung: Annotiere Service/Resource-Klassen mit @ApplySessionSettings;
 * der Interceptor setzt Settings vor dem ersten Statement im Tx.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SessionSettingsFilter implements ContainerRequestFilter {

  @Inject SecurityIdentity identity;
  @Inject SessionSettingsContext ctx;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    // Claims robust extrahieren
    String sub = safeString(identity.getPrincipal().getName());
    String org = safeAttr(identity, "org_id");
    String territory = safeAttr(identity, "territory");
    String scopesCsv = joinCsv(identity.getAttribute("scopes"));
    String contactRolesCsv = joinCsv(identity.getAttribute("contact_roles"));
    String rolesCsv = joinCsv(identity.getRoles()); // quarkus roles

    // In Request-Kontext ablegen (wird vom Interceptor auf die DB-Connection angewandt)
    ctx.setUserId(sub);
    ctx.setOrgId(org);
    ctx.setTerritory(territory);
    ctx.setScopesCsv(scopesCsv);
    ctx.setContactRolesCsv(contactRolesCsv);
    ctx.setRolesCsv(rolesCsv);
  }

  private static String safeAttr(SecurityIdentity id, String key) {
    Object v = id.getAttribute(key);
    return v == null ? "" : String.valueOf(v);
  }
  private static String safeString(String v) { return v == null ? "" : v; }

  @SuppressWarnings("unchecked")
  private static String joinCsv(Object claim) {
    if (claim == null) return "";
    if (claim instanceof String s) return s;
    if (claim instanceof String[] arr) return String.join(",", arr);
    if (claim instanceof List<?> list) return String.join(",", list.stream().map(String::valueOf).toList());
    return String.valueOf(claim);
  }
}

// ===== Request-Scoped Context =====
@RequestScoped
class SessionSettingsContext {
  private String userId, orgId, territory, scopesCsv, contactRolesCsv, rolesCsv;
  public String getUserId() { return userId; }
  public String getOrgId() { return orgId; }
  public String getTerritory() { return territory; }
  public String getScopesCsv() { return scopesCsv; }
  public String getContactRolesCsv() { return contactRolesCsv; }
  public String getRolesCsv() { return rolesCsv; }
  public void setUserId(String v){ this.userId=v; }
  public void setOrgId(String v){ this.orgId=v; }
  public void setTerritory(String v){ this.territory=v; }
  public void setScopesCsv(String v){ this.scopesCsv=v; }
  public void setContactRolesCsv(String v){ this.contactRolesCsv=v; }
  public void setRolesCsv(String v){ this.rolesCsv=v; }
}

// ===== Interceptor Binding =====
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@InterceptorBinding
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@interface ApplySessionSettings {}

// ===== Interceptor (pool-safe, Tx-bound) =====
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;

@Interceptor
@ApplySessionSettings
@Priority(Interceptor.Priority.APPLICATION)
class SessionSettingsInterceptor {

  @Inject EntityManager em;
  @Inject SessionSettingsContext ctx;

  @AroundInvoke
  @Transactional // stellt sicher, dass eine einzige JDBC-Connection verwendet wird
  public Object around(InvocationContext ic) throws Exception {
    apply(em, ctx);
    return ic.proceed();
  }

  static void apply(EntityManager em, SessionSettingsContext ctx) {
    em.unwrap(Session.class).doWork(conn -> setLocals(conn, ctx));
  }

  private static void setLocals(Connection c, SessionSettingsContext ctx) throws SQLException {
    // SET LOCAL ist transaktionsgebunden; PreparedStatement schützt vor Injection
    try (PreparedStatement ps1 = c.prepareStatement("SET LOCAL app.user_id = ?");
         PreparedStatement ps2 = c.prepareStatement("SET LOCAL app.org_id = ?");
         PreparedStatement ps3 = c.prepareStatement("SET LOCAL app.territory = ?");
         PreparedStatement ps4 = c.prepareStatement("SET LOCAL app.scopes = ?");
         PreparedStatement ps5 = c.prepareStatement("SET LOCAL app.contact_roles = ?");
         PreparedStatement ps6 = c.prepareStatement("SET LOCAL app.roles = ?")) {

      ps1.setString(1, ctx.getUserId());          ps1.execute();
      ps2.setString(1, ctx.getOrgId());           ps2.execute();
      ps3.setString(1, ctx.getTerritory());       ps3.execute();
      ps4.setString(1, nullToEmpty(ctx.getScopesCsv()));        ps4.execute();
      ps5.setString(1, nullToEmpty(ctx.getContactRolesCsv()));  ps5.execute();
      ps6.setString(1, nullToEmpty(ctx.getRolesCsv()));         ps6.execute();
    }
  }

  private static String nullToEmpty(String s){ return s==null ? "" : s; }
}
