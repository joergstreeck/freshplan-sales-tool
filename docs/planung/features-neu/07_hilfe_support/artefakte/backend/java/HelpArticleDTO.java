package de.freshplan.help.domain;

import java.util.List;
import java.util.UUID;

public class HelpArticleDTO {
  public UUID id;
  public String slug;
  public String module; // 01..07
  public String kind;   // FAQ|HowTo|Playbook|Video
  public String title;
  public String summary;
  public String locale;
  public String territory;
  public String persona; // CHEF|BUYER|GF|REP
  public List<String> keywords;
  public Cta cta;

  public static class Cta {
    public String type; // NONE|LINK|GUIDED_FOLLOWUP|GUIDED_ROI
    public String href;
  }
}
