package de.freshplan.cockpit.dto;

import java.time.OffsetDateTime;
import java.util.Map;

/** DTOs for Cockpit Summary APIs (Foundation Standards) */
public class CockpitDTO {

  public static class Summary {
    public String range;                       // 7d | 30d | 90d
    public double sampleSuccessRatePct;        // 0..100
    public double roiPipelineValue;            // €
    public double partnerSharePct;             // 0..100
    public Map<String, Double> channelMix;     // {DIRECT, PARTNER} in percent
    public int atRiskCustomers;
    public OffsetDateTime updatedAt;
  }
}
