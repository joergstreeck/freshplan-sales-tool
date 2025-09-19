package de.freshplan.security;

import jakarta.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ABAC Security Context for Territory-based Access Control
 *
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Implementation Pattern
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE Context Management
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - Request-scoped Performance
 *
 * This context provides request-scoped territory and chain access control
 * for ABAC (Attribute-Based Access Control) security enforcement.
 *
 * @author Security Team
 * @version 1.1
 * @since 2025-09-19
 */
@RequestScoped
public class ScopeContext {

  private List<String> territories = new ArrayList<>();
  private String chainId;

  public List<String> getTerritories() {
    return Collections.unmodifiableList(territories);
  }
  public void setTerritories(List<String> ts) {
    this.territories.clear();
    if (ts != null) this.territories.addAll(ts);
  }
  public String getChainId() { return chainId; }
  public void setChainId(String chainId) { this.chainId = chainId; }
}
