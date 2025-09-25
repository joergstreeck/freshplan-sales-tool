package de.freshplan.modules.leads.constants;

/**
 * FreshFoodz Corporate Identity Constants. Sprint 2.1: Design System V2 compliance for all
 * lead-related UI components.
 */
public final class FreshFoodzCI {

  private FreshFoodzCI() {
    // Private constructor to prevent instantiation
  }

  /** FreshFoodz Primary Colors - DO NOT HARDCODE! */
  public static final String COLOR_PRIMARY = "#94C456"; // Primärgrün
  public static final String COLOR_SECONDARY = "#004F7B"; // Dunkelblau
  public static final String COLOR_WHITE = "#FFFFFF";
  public static final String COLOR_BLACK = "#000000";

  /** Accessibility-compliant hover variants */
  public static final String COLOR_PRIMARY_HOVER = "#7BA945";
  public static final String COLOR_SECONDARY_HOVER = "#003A5C";

  /** Typography Standards */
  public static final String FONT_HEADLINE = "Antonio"; // Headlines - Antonio Bold (PFLICHT)
  public static final String FONT_BODY = "Poppins"; // Body Text - Poppins Regular
  public static final int FONT_WEIGHT_BOLD = 700;
  public static final int FONT_WEIGHT_MEDIUM = 500;
  public static final int FONT_WEIGHT_REGULAR = 400;

  /** Lead Status Colors (aligned with CI) */
  public static final String STATUS_REGISTERED = COLOR_SECONDARY;
  public static final String STATUS_ACTIVE = COLOR_PRIMARY;
  public static final String STATUS_REMINDER = "#FFA500"; // Warning Orange
  public static final String STATUS_GRACE_PERIOD = "#FF6B6B"; // Critical Red
  public static final String STATUS_QUALIFIED = COLOR_PRIMARY_HOVER;
  public static final String STATUS_CONVERTED = "#28A745"; // Success Green
  public static final String STATUS_LOST = "#6C757D"; // Neutral Gray
  public static final String STATUS_EXPIRED = "#DC3545"; // Danger Red

  /** Email Template Defaults (for Campaign Management) */
  public static final String EMAIL_HEADER_BG = COLOR_PRIMARY;
  public static final String EMAIL_HEADER_TEXT = COLOR_WHITE;
  public static final String EMAIL_BODY_BG = COLOR_WHITE;
  public static final String EMAIL_BODY_TEXT = COLOR_BLACK;
  public static final String EMAIL_CTA_BG = COLOR_SECONDARY;
  public static final String EMAIL_CTA_TEXT = COLOR_WHITE;
}