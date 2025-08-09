package de.freshplan.audit.interceptor;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Annotation zur Markierung von Methoden oder Klassen, die automatisch auditiert werden sollen.
 *
 * <p>Verwendung: - Auf Service-Klassen für automatisches Tracking aller Methoden - Auf einzelnen
 * Methoden für gezieltes Tracking - Auf Repository-Klassen für Datenbank-Operationen
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Audited {

  /** Optionale Beschreibung für den Audit-Eintrag. */
  String value() default "";

  /**
   * Ob auch Lesezugriffe (VIEW) auditiert werden sollen. Default: false (nur schreibende Zugriffe)
   */
  boolean includeReads() default false;

  /** Ob sensible Daten (wie Passwörter) aus dem Audit-Log gefiltert werden sollen. Default: true */
  boolean filterSensitiveData() default true;
}
