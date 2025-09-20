package de.freshplan.help.service;

import de.freshplan.help.domain.HelpArticleDTO;
import de.freshplan.help.domain.SuggestionDTO;
import de.freshplan.help.domain.FollowUpPlanRequest;
import de.freshplan.help.domain.FollowUpPlanResponse;
import de.freshplan.help.domain.RoiQuickCheckRequest;
import de.freshplan.help.domain.RoiQuickCheckResponse;
import de.freshplan.help.repo.HelpRepository;
import de.freshplan.security.ScopeContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class HelpService {

  @Inject HelpRepository repo;
  @Inject ScopeContext scope;
  @Inject MeterRegistry meter;

  // Simple in-memory limiter per (user, session) with TTL
  private final Map<String, Entry> budget = new ConcurrentHashMap<>();
  private static class Entry { int used; long expireAt; }

  private int calcBudget(int sessionMinutes, int base, int perHour, int max){
    int extra = Math.floorDiv(Math.max(sessionMinutes,0), 60) * perHour;
    return Math.min(base + extra, max);
  }

  public List<HelpArticleDTO> menu(String module, String persona, String territory, int limit){
    long t0 = System.nanoTime();
    try {
      return repo.findMenu(module, persona, territory, limit);
    } finally {
      Timer.builder("help_menu_seconds").register(meter).record(Duration.ofNanos(System.nanoTime() - t0));
    }
  }

  public List<SuggestionDTO> suggest(String context, String module, String persona, String territory, int top, String sessionId, int sessionMinutes, double minConfidence, int base, int perHour, int max){
    String sid = (sessionId==null || sessionId.isBlank()) ? scope.getUserId()+":default" : scope.getUserId()+":"+sessionId;
    long now = System.currentTimeMillis();
    Entry e = budget.computeIfAbsent(sid, k -> { Entry ne = new Entry(); ne.used = 0; ne.expireAt = now + 8*60*60*1000L; return ne; });
    if (e.expireAt < now) { e.used = 0; e.expireAt = now + 8*60*60*1000L; }
    int allowed = calcBudget(sessionMinutes, base, perHour, max);
    if (e.used >= allowed){
      meter.counter("help_nudges_rate_limited_total").increment();
      throw new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.status(429)
        .entity(java.util.Map.of("type","https://freshplan.dev/problems/nudge_budget_exhausted","title","budget","status",429,"detail","Nudge budget exceeded")).build());
    }

    long t0 = System.nanoTime();
    try{
      var rows = repo.rawSuggest(context, module, persona, territory, top);
      List<SuggestionDTO> out = new java.util.ArrayList<>();
      for (Object[] r : rows){
        HelpArticleDTO d = new HelpArticleDTO();
        d.id = (java.util.UUID) r[0]; d.slug=(String) r[1]; d.module=(String) r[2]; d.kind=(String) r[3];
        d.title=(String) r[4]; d.summary=(String) r[5]; d.locale=(String) r[6]; d.territory=(String) r[7]; d.persona=(String) r[8];
        if (r[9] != null) d.keywords = java.util.Arrays.asList((String[]) r[9]);
        if (r[10] != null) { try { d.cta = new com.fasterxml.jackson.databind.ObjectMapper().readValue(r[10].toString(), de.freshplan.help.domain.HelpArticleDTO.Cta.class); } catch(Exception ignore){} }
        double score = ((Number) r[11]).doubleValue();
        if (score < minConfidence) continue;
        SuggestionDTO s = new SuggestionDTO(); s.article = d; s.confidence = Math.min(1.0, Math.max(0.0, score));
        out.add(s);
      }
      e.used++;
      meter.counter("help_nudges_shown_total").increment(out.size());
      return out;
    } finally {
      Timer.builder("help_suggest_seconds").register(meter).record(Duration.ofNanos(System.nanoTime() - t0));
    }
  }

  public FollowUpPlanResponse planFollowUp(FollowUpPlanRequest req, List<String> defaultOffsets){
    if (req == null || req.accountId == null) throw problem(400, "invalid_request", "accountId required");
    List<String> offs = (req.followupOffsets != null && !req.followupOffsets.isEmpty()) ? req.followupOffsets : defaultOffsets;
    FollowUpPlanResponse res = new FollowUpPlanResponse();
    // Integration point: call Activities API. For now, synthesize IDs and dates.
    java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
    for (String off : offs){
      java.time.Period p = java.time.Period.parse(off.replace("W","7D")); // simple PnD/PnW
      java.time.OffsetDateTime due = now.plusDays(p.getDays()).plusWeeks(p.getWeeks());
      FollowUpPlanResponse.ActivityRef ref = new FollowUpPlanResponse.ActivityRef();
      ref.id = java.util.UUID.randomUUID();
      ref.dueDate = due;
      ref.kind = "Produkttest-Follow-Up";
      res.createdActivities.add(ref);
    }
    return res;
  }

  public RoiQuickCheckResponse roiQuick(RoiQuickCheckRequest req){
    if (req == null || req.accountId == null) throw problem(400, "invalid_request", "accountId required");
    double monthlySavings = (req.hoursSavedPerDay * req.hourlyRate * req.workingDaysPerMonth) + req.wasteReductionPerMonth;
    // Assume a nominal enablement cost spread equals 3 months of savings (heuristic) to give a payback rough cut
    double payback = (monthlySavings * 3.0) / Math.max(monthlySavings, 1);
    RoiQuickCheckResponse r = new RoiQuickCheckResponse();
    r.monthlySavings = Math.round(monthlySavings * 100.0) / 100.0;
    r.paybackMonths = Math.round(payback * 10.0) / 10.0;
    r.usedCalculator = "INTERNAL";
    return r;
  }

  private javax.ws.rs.WebApplicationException problem(int status, String type, String detail){
    var body = new java.util.LinkedHashMap<String,Object>();
    body.put("type","https://freshplan.dev/problems/"+type);
    body.put("title",type); body.put("status",status); body.put("detail",detail);
    return new javax.ws.rs.WebApplicationException(javax.ws.rs.core.Response.status(status).entity(body).build());
  }
}
