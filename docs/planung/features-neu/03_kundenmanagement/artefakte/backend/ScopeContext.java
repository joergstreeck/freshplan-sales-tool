package de.freshplan.security;

import jakarta.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequestScoped
public class ScopeContext {
  private final List<String> territories = new ArrayList<>();
  private String chainId;
  public List<String> getTerritories() { return Collections.unmodifiableList(territories); }
  public void setTerritories(List<String> ts) { territories.clear(); if (ts!=null) territories.addAll(ts); }
  public String getChainId() { return chainId; }
  public void setChainId(String id) { this.chainId = id; }
}
