package com.freshplan.leads;

import java.util.Map;

/** Lead-Scoring (B2B-Food):
 * score = w1(sample_status) + w2(roi_bucket) + w3(decision_makers) + w4(season_window) + w5(bounce_penalty)
 * Normiert auf 0..100
 */
public class LeadScoringService {

  public int score(Map<String,Object> lead) {
    int score = 0;
    // sample_status
    String sample = (String) lead.getOrDefault("sample_status","NONE");
    score += switch(sample) { case "FEEDBACK_SUCCESS" -> 35; case "DELIVERED" -> 20; default -> 0; };

    // roi_bucket
    String roi = (String) lead.getOrDefault("roi_bucket","LOW");
    score += switch(roi) { case "HIGH" -> 30; case "MID" -> 20; default -> 10; };

    // decision_makers
    int dm = (int) lead.getOrDefault("decision_maker_count", 1);
    boolean aligned = (boolean) lead.getOrDefault("has_exec_alignment", false);
    score += Math.min(15, dm * 3) + (aligned ? 10 : 0);

    // seasonal window
    boolean inWindow = (boolean) lead.getOrDefault("in_season_window", false);
    score += inWindow ? 5 : 0;

    // bounce penalty
    boolean hardBounce = (boolean) lead.getOrDefault("hard_bounce", false);
    if (hardBounce) score -= 20;

    return Math.max(0, Math.min(100, score));
  }
}
