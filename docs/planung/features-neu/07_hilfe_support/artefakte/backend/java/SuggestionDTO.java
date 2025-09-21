package de.freshplan.help.domain;

import com.freshfoodz.crm.help.operations.CARResponse;

public class SuggestionDTO {
  public HelpArticleDTO article;
  public double confidence;

  // Optional: Für Operations-Guided Responses
  public CARResponse carResponse;
}
