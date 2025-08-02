package de.freshplan.domain.cost.service;

/** Exception für Budget-Überschreitungen */
public class BudgetExceededException extends RuntimeException {

  public BudgetExceededException(String message) {
    super(message);
  }

  public BudgetExceededException(String message, Throwable cause) {
    super(message, cause);
  }
}
