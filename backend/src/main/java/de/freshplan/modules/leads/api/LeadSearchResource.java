package de.freshplan.modules.leads.api;

import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.service.LeadFuzzySearchService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * REST API für Lead-Suche mit Fuzzy-Matching (pg_trgm).
 *
 * <p>Sprint 2.1.8 Phase 4: Advanced Search
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>GET /api/leads/search/fuzzy - Fuzzy-Suche mit pg_trgm
 *   <li>GET /api/leads/search/duplicates - Duplikat-Erkennung
 *   <li>GET /api/leads/search/similarity - Ähnlichkeitsberechnung
 * </ul>
 *
 * @since Sprint 2.1.8
 */
@Path("/api/leads/search")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
@RlsContext
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LeadSearchResource {

  private static final Logger LOG = Logger.getLogger(LeadSearchResource.class);

  @Inject LeadFuzzySearchService fuzzySearchService;

  @Context SecurityContext securityContext;

  @ConfigProperty(name = "app.dev.fallback-user-id", defaultValue = "dev-admin-001")
  String fallbackUserId;

  private String getCurrentUserId() {
    return securityContext.getUserPrincipal() != null
        ? securityContext.getUserPrincipal().getName()
        : fallbackUserId;
  }

  /**
   * Fuzzy-Suche für Leads.
   *
   * <p>Verwendet pg_trgm für Ähnlichkeitssuche. Findet auch bei Tippfehlern und Varianten.
   *
   * @param query Suchbegriff
   * @param limit Maximale Anzahl Ergebnisse (default: 20)
   * @param includeInactive Auch inaktive Leads einschließen (default: false)
   * @return Liste von Leads mit Ähnlichkeits-Score
   */
  @GET
  @Path("/fuzzy")
  public Response fuzzySearch(
      @QueryParam("q") String query,
      @QueryParam("limit") @DefaultValue("20") int limit,
      @QueryParam("includeInactive") @DefaultValue("false") boolean includeInactive) {

    String currentUserId = getCurrentUserId();
    LOG.infof("Fuzzy search by user %s: query='%s', limit=%d", currentUserId, query, limit);

    if (query == null || query.trim().isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Query parameter 'q' is required"))
          .build();
    }

    if (limit < 1 || limit > 100) {
      limit = 20;
    }

    try {
      List<Lead> leads = fuzzySearchService.fuzzySearch(query, limit, includeInactive);

      // Convert to DTOs
      List<LeadDTO> dtos = leads.stream().map(LeadDTO::from).collect(Collectors.toList());

      return Response.ok(
              Map.of(
                  "data",
                  dtos,
                  "total",
                  dtos.size(),
                  "query",
                  query,
                  "fuzzyEnabled",
                  fuzzySearchService.isPgTrgmAvailable()))
          .build();

    } catch (Exception e) {
      LOG.errorf(e, "Fuzzy search failed for query: %s", query);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Map.of("error", "Search failed: " + e.getMessage()))
          .build();
    }
  }

  /**
   * Duplikat-Suche für Import-Vorschau.
   *
   * <p>Findet potentielle Duplikate basierend auf Firmenname und/oder E-Mail.
   *
   * @param companyName Firmenname (required)
   * @param email E-Mail (optional, erhöht Genauigkeit)
   * @param limit Maximale Anzahl Ergebnisse (default: 5)
   * @return Liste von potentiellen Duplikaten
   */
  @GET
  @Path("/duplicates")
  public Response findDuplicates(
      @QueryParam("companyName") String companyName,
      @QueryParam("email") String email,
      @QueryParam("limit") @DefaultValue("5") int limit) {

    String currentUserId = getCurrentUserId();
    LOG.infof(
        "Duplicate search by user %s: company='%s', email='%s'", currentUserId, companyName, email);

    if (companyName == null || companyName.trim().isEmpty()) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Query parameter 'companyName' is required"))
          .build();
    }

    try {
      List<Lead> duplicates = fuzzySearchService.findPotentialDuplicates(companyName, email, limit);

      // Convert to DTOs with similarity info
      List<Map<String, Object>> results =
          duplicates.stream()
              .map(
                  lead -> {
                    double similarity =
                        fuzzySearchService.calculateSimilarity(companyName, lead.companyName);
                    return Map.of(
                        "lead", LeadDTO.from(lead),
                        "similarity", Math.round(similarity * 100),
                        "matchedFields", getMatchedFields(lead, companyName, email));
                  })
              .collect(Collectors.toList());

      return Response.ok(
              Map.of(
                  "duplicates", results,
                  "count", results.size(),
                  "searchCriteria",
                      Map.of("companyName", companyName, "email", email != null ? email : "")))
          .build();

    } catch (Exception e) {
      LOG.errorf(e, "Duplicate search failed");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Map.of("error", "Duplicate search failed: " + e.getMessage()))
          .build();
    }
  }

  /**
   * Berechnet die Ähnlichkeit zwischen zwei Strings.
   *
   * <p>Nützlich für Frontend-Validierung und Import-Vorschau.
   *
   * @param text1 Erster String
   * @param text2 Zweiter String
   * @return Ähnlichkeit (0-100%)
   */
  @GET
  @Path("/similarity")
  public Response calculateSimilarity(
      @QueryParam("text1") String text1, @QueryParam("text2") String text2) {

    if (text1 == null || text2 == null) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity(Map.of("error", "Both 'text1' and 'text2' are required"))
          .build();
    }

    try {
      double similarity = fuzzySearchService.calculateSimilarity(text1, text2);
      int percentage = (int) Math.round(similarity * 100);

      return Response.ok(
              Map.of(
                  "similarity", percentage,
                  "text1", text1,
                  "text2", text2,
                  "isMatch", percentage >= 50))
          .build();

    } catch (Exception e) {
      LOG.errorf(e, "Similarity calculation failed");
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(Map.of("error", "Similarity calculation failed"))
          .build();
    }
  }

  /**
   * Prüft ob pg_trgm Extension verfügbar ist.
   *
   * @return Status der Extension
   */
  @GET
  @Path("/status")
  public Response getSearchStatus() {
    boolean pgTrgmAvailable = fuzzySearchService.isPgTrgmAvailable();

    return Response.ok(
            Map.of(
                "pgTrgmEnabled", pgTrgmAvailable,
                "fuzzySearchAvailable", pgTrgmAvailable,
                "fallbackMode", !pgTrgmAvailable))
        .build();
  }

  /** Hilfsmethode: Ermittelt welche Felder beim Duplikat-Match übereinstimmen. */
  private List<String> getMatchedFields(Lead lead, String companyName, String email) {
    List<String> fields = new java.util.ArrayList<>();

    if (companyName != null
        && lead.companyName != null
        && fuzzySearchService.calculateSimilarity(companyName, lead.companyName) > 0.3) {
      fields.add("companyName");
    }

    if (email != null
        && lead.email != null
        && fuzzySearchService.calculateSimilarity(email, lead.email) > 0.5) {
      fields.add("email");
    }

    return fields;
  }
}
