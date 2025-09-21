package de.freshplan.cockpit.repo;

import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;

@ApplicationScoped
public class CockpitRepository {
  @Inject EntityManager em;
  @Inject ScopeContext scope;

  @SuppressWarnings("unchecked")
  public Map<String,Object> fetchSummary(String range, String territory, List<String> channels) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT COALESCE(avg(sample_success_rate_pct),0), COALESCE(sum(roi_pipeline_value),0), COALESCE(avg(partner_share_pct),0) ");
    sql.append("FROM cockpit_kpi_daily WHERE day >= (CURRENT_DATE - CASE WHEN :range='7d' THEN 7 WHEN :range='30d' THEN 30 ELSE 90 END) ");
    Map<String,Object> params = new HashMap<>();
    params.put("range", range);

    if (territory != null && !territory.isBlank()) {
      sql.append("AND territory = :territory ");
      params.put("territory", territory);
    }
    if (!scope.getTerritories().isEmpty()) {
      sql.append("AND territory = ANY(:scoped) ");
      params.put("scoped", scope.getTerritories().toArray(new String[0]));
    }
    if (channels != null && !channels.isEmpty()) {
      sql.append("AND channel = ANY(:channels) ");
      params.put("channels", channels.toArray(new String[0]));
    } else if (!scope.getChannels().isEmpty()) {
      sql.append("AND channel = ANY(:channelsScoped) ");
      params.put("channelsScoped", scope.getChannels().toArray(new String[0]));
    }

    Query q = em.createNativeQuery(sql.toString());
    for (var e : params.entrySet()) q.setParameter(e.getKey(), e.getValue());
    Object[] row = (Object[]) q.getSingleResult();

    // Channel mix
    String mixSql = "SELECT channel, COALESCE(sum(value_pct),0) FROM cockpit_channel_mix_daily WHERE day >= (CURRENT_DATE - CASE WHEN :range='7d' THEN 7 WHEN :range='30d' THEN 30 ELSE 90 END) "
                  + (territory!=null && !territory.isBlank()? "AND territory=:territory " : "")
                  + (!scope.getTerritories().isEmpty()? "AND territory = ANY(:scoped) " : "")
                  + (channels!=null && !channels.isEmpty()? "AND channel = ANY(:cmChannels) " : (!scope.getChannels().isEmpty()? "AND channel = ANY(:cmChannelsScoped) " : ""))
                  + "GROUP BY channel";
    Query mq = em.createNativeQuery(mixSql);
    mq.setParameter("range", range);
    if (params.containsKey("territory")) mq.setParameter("territory", params.get("territory"));
    if (params.containsKey("scoped")) mq.setParameter("scoped", params.get("scoped"));
    if (channels!=null && !channels.isEmpty()) mq.setParameter("cmChannels", params.get("channels"));
    else if (!scope.getChannels().isEmpty()) mq.setParameter("cmChannelsScoped", params.get("channelsScoped"));

    Map<String,Double> channelMix = new LinkedHashMap<>();
    channelMix.put("DIRECT", 0d); channelMix.put("PARTNER", 0d);
    List<Object[]> rows = mq.getResultList();
    for (Object[] r : rows) {
      channelMix.put(String.valueOf(r[0]), ((Number)r[1]).doubleValue());
    }

    Map<String,Object> result = new LinkedHashMap<>();
    result.put("sampleSuccessRatePct", ((Number)row[0]).doubleValue());
    result.put("roiPipelineValue", ((Number)row[1]).doubleValue());
    result.put("partnerSharePct", ((Number)row[2]).doubleValue());
    result.put("channelMix", channelMix);
    result.put("atRiskCustomers", 0);
    return result;
  }

  public Map<String,Object> fetchFilters() {
    Map<String,Object> f = new LinkedHashMap<>();
    f.put("territories", scope.getTerritories());
    f.put("channels", scope.getChannels().isEmpty()? List.of("DIRECT","PARTNER") : scope.getChannels());
    return f;
  }
}
