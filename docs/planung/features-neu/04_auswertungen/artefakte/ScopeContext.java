package com.freshplan.reports;

import jakarta.enterprise.context.RequestScoped;
import java.util.List;

@RequestScoped
public class ScopeContext {
  private List<String> territories = List.of();
  private String chainId;

  public List<String> getTerritories() { return territories; }
  public void setTerritories(List<String> territories) { this.territories = territories; }

  public String getChainId() { return chainId; }
  public void setChainId(String chainId) { this.chainId = chainId; }
}
