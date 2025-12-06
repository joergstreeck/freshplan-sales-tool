package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Fuzzy Search Service für Leads mit pg_trgm.
 *
 * <p>Sprint 2.1.8 Phase 4: Advanced Search
 *
 * <p>Verwendet PostgreSQL pg_trgm Extension für:
 *
 * <ul>
 *   <li>Similarity-basierte Suche (Tippfehler-tolerant)
 *   <li>Trigram-basierte Ähnlichkeitsberechnung
 *   <li>Ranking nach Relevanz
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@ApplicationScoped
public class LeadFuzzySearchService {

  private static final Logger LOG = Logger.getLogger(LeadFuzzySearchService.class);

  /** Minimale Ähnlichkeit für Treffer (0.0 - 1.0). Default: 0.3 (30% Ähnlichkeit) */
  private static final double MIN_SIMILARITY = 0.3;

  /** Minimale Ähnlichkeit für "fuzzy" Suche (toleranter). */
  private static final double MIN_SIMILARITY_FUZZY = 0.2;

  @Inject EntityManager em;

  /**
   * Sucht Leads mit Fuzzy-Matching (pg_trgm similarity).
   *
   * <p>Sucht in: company_name, contact_person, email, city
   *
   * <p>Sortiert nach Ähnlichkeit (höchste zuerst).
   *
   * @param searchTerm Suchbegriff
   * @param limit Maximale Anzahl Ergebnisse
   * @param includeInactive Auch inaktive Leads einschließen
   * @return Liste von Leads, sortiert nach Ähnlichkeit
   */
  @SuppressWarnings("unchecked")
  public List<Lead> fuzzySearch(String searchTerm, int limit, boolean includeInactive) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      return Collections.emptyList();
    }

    String term = searchTerm.trim().toLowerCase();
    LOG.debugf(
        "Fuzzy search for: '%s', limit: %d, includeInactive: %b", term, limit, includeInactive);

    // Native Query mit pg_trgm similarity
    String sql =
        """
        SELECT l.*,
               GREATEST(
                 similarity(LOWER(l.company_name), :term),
                 similarity(LOWER(COALESCE(l.contact_person, '')), :term),
                 similarity(LOWER(COALESCE(l.email, '')), :term),
                 similarity(LOWER(COALESCE(l.city, '')), :term)
               ) AS sim_score
        FROM leads l
        WHERE (
            similarity(LOWER(l.company_name), :term) > :minSim
            OR similarity(LOWER(COALESCE(l.contact_person, '')), :term) > :minSim
            OR similarity(LOWER(COALESCE(l.email, '')), :term) > :minSim
            OR similarity(LOWER(COALESCE(l.city, '')), :term) > :minSim
        )
        """
            + (includeInactive ? "" : " AND l.status NOT IN ('EXPIRED', 'DELETED', 'LOST')")
            + """
        ORDER BY sim_score DESC
        LIMIT :limit
        """;

    Query query = em.createNativeQuery(sql, Lead.class);
    query.setParameter("term", term);
    query.setParameter("minSim", MIN_SIMILARITY);
    query.setParameter("limit", limit);

    try {
      List<Lead> results = query.getResultList();
      LOG.debugf("Fuzzy search found %d results", results.size());
      return results;
    } catch (Exception e) {
      LOG.errorf(e, "Fuzzy search failed for term: %s", term);
      // Fallback to simple LIKE search
      return fallbackSearch(term, limit, includeInactive);
    }
  }

  /**
   * Sucht potentielle Duplikate für einen neuen Lead.
   *
   * <p>Verwendet strengere Similarity-Schwelle für Duplikat-Erkennung.
   *
   * @param companyName Firmenname
   * @param email E-Mail (optional)
   * @param limit Maximale Anzahl Ergebnisse
   * @return Liste von potentiellen Duplikaten
   */
  @SuppressWarnings("unchecked")
  public List<Lead> findPotentialDuplicates(String companyName, String email, int limit) {
    if (companyName == null || companyName.trim().isEmpty()) {
      return Collections.emptyList();
    }

    String term = companyName.trim().toLowerCase();
    String emailTerm = email != null ? email.trim().toLowerCase() : "";

    LOG.debugf("Finding duplicates for: company='%s', email='%s'", term, emailTerm);

    // Höhere Similarity-Schwelle für Duplikate (0.5 = 50%)
    double dupSimilarity = 0.5;

    String sql =
        """
        SELECT l.*,
               GREATEST(
                 similarity(LOWER(l.company_name), :companyTerm),
                 CASE WHEN :emailTerm != '' THEN similarity(LOWER(COALESCE(l.email, '')), :emailTerm) ELSE 0 END
               ) AS sim_score
        FROM leads l
        WHERE (
            similarity(LOWER(l.company_name), :companyTerm) > :minSim
            OR (:emailTerm != '' AND similarity(LOWER(COALESCE(l.email, '')), :emailTerm) > :minSim)
        )
        AND l.status NOT IN ('DELETED')
        ORDER BY sim_score DESC
        LIMIT :limit
        """;

    Query query = em.createNativeQuery(sql, Lead.class);
    query.setParameter("companyTerm", term);
    query.setParameter("emailTerm", emailTerm);
    query.setParameter("minSim", dupSimilarity);
    query.setParameter("limit", limit);

    try {
      List<Lead> results = query.getResultList();
      LOG.debugf("Found %d potential duplicates", results.size());
      return results;
    } catch (Exception e) {
      LOG.errorf(e, "Duplicate search failed");
      return Collections.emptyList();
    }
  }

  /**
   * Berechnet die Ähnlichkeit zwischen zwei Strings.
   *
   * <p>Wrapper für pg_trgm similarity() Funktion.
   *
   * @param text1 Erster String
   * @param text2 Zweiter String
   * @return Ähnlichkeit (0.0 - 1.0)
   */
  public double calculateSimilarity(String text1, String text2) {
    if (text1 == null || text2 == null) {
      return 0.0;
    }

    String sql = "SELECT similarity(:text1, :text2)";
    Query query = em.createNativeQuery(sql);
    query.setParameter("text1", text1.toLowerCase());
    query.setParameter("text2", text2.toLowerCase());

    try {
      Number result = (Number) query.getSingleResult();
      return result != null ? result.doubleValue() : 0.0;
    } catch (Exception e) {
      LOG.warnf("Similarity calculation failed: %s", e.getMessage());
      return 0.0;
    }
  }

  /**
   * Fallback-Suche mit LIKE wenn pg_trgm nicht verfügbar.
   *
   * @param term Suchbegriff
   * @param limit Limit
   * @param includeInactive Inaktive einschließen
   * @return Lead-Liste
   */
  private List<Lead> fallbackSearch(String term, int limit, boolean includeInactive) {
    LOG.warn("Using fallback LIKE search (pg_trgm not available)");

    String pattern = "%" + term + "%";
    String query =
        "lower(companyName) LIKE ?1 "
            + "OR lower(contactPerson) LIKE ?1 "
            + "OR lower(email) LIKE ?1 "
            + "OR lower(city) LIKE ?1";

    if (!includeInactive) {
      query += " AND status NOT IN (?2, ?3, ?4)";
      return Lead.find(query, pattern, LeadStatus.EXPIRED, LeadStatus.DELETED, LeadStatus.LOST)
          .page(0, limit)
          .list();
    }

    return Lead.find(query, pattern).page(0, limit).list();
  }

  /**
   * Prüft ob pg_trgm Extension verfügbar ist.
   *
   * @return true wenn pg_trgm aktiviert
   */
  public boolean isPgTrgmAvailable() {
    try {
      String sql = "SELECT 1 FROM pg_extension WHERE extname = 'pg_trgm'";
      Query query = em.createNativeQuery(sql);
      return !query.getResultList().isEmpty();
    } catch (Exception e) {
      LOG.warn("Could not check pg_trgm availability: " + e.getMessage());
      return false;
    }
  }
}
