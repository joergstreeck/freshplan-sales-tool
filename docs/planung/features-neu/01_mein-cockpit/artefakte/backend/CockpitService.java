package de.freshplan.cockpit.service;

import de.freshplan.cockpit.dto.CockpitDTO;
import de.freshplan.cockpit.repo.CockpitRepository;
import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class CockpitService {

  @Inject CockpitRepository repo;
  @Inject ScopeContext scope;

  public CockpitDTO.Summary getSummary(String range, String territory, String channelsCsv) {
    List<String> channels = new ArrayList<>();
    if (channelsCsv != null && !channelsCsv.isBlank()) {
      for (String c : channelsCsv.split(",")) channels.add(c.trim().toUpperCase());
    }
    if (!scope.getChannels().isEmpty()) {
      channels.retainAll(scope.getChannels());
    }
    Map<String,Object> data = repo.fetchSummary(range, territory, channels);
    CockpitDTO.Summary s = new CockpitDTO.Summary();
    s.range = range;
    s.sampleSuccessRatePct = (double) data.get("sampleSuccessRatePct");
    s.roiPipelineValue = (double) data.get("roiPipelineValue");
    s.partnerSharePct = (double) data.get("partnerSharePct");
    @SuppressWarnings("unchecked")
    Map<String,Double> mix = (Map<String,Double>) data.get("channelMix");
    s.channelMix = mix;
    s.atRiskCustomers = (int) data.getOrDefault("atRiskCustomers", 0);
    s.updatedAt = OffsetDateTime.now();
    return s;
  }

  public Map<String,Object> filters() {
    return repo.fetchFilters();
  }
}
