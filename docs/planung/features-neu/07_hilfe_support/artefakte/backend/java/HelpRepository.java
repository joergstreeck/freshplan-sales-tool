package de.freshplan.help.repo;

import de.freshplan.help.domain.HelpArticleDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class HelpRepository {

  @Inject EntityManager em;

  private HelpArticleDTO map(Object[] r){
    HelpArticleDTO d = new HelpArticleDTO();
    d.id = (java.util.UUID) r[0];
    d.slug = (String) r[1];
    d.module = (String) r[2];
    d.kind = (String) r[3];
    d.title = (String) r[4];
    d.summary = (String) r[5];
    d.locale = (String) r[6];
    d.territory = (String) r[7];
    d.persona = (String) r[8];
    if (r[9] != null) d.keywords = java.util.Arrays.asList((String[]) r[9]);
    if (r[10] != null) {
      try { d.cta = new com.fasterxml.jackson.databind.ObjectMapper().readValue(r[10].toString(), HelpArticleDTO.Cta.class); }
      catch (Exception ignore) {}
    }
    return d;
  }

  public List<HelpArticleDTO> findMenu(String module, String persona, String territory, int limit){
    String sql =
      "SELECT id, slug, module, kind, title, summary, locale, territory, persona, keywords, cta " +
      "FROM help_article " +
      "WHERE is_published = true " +
      "  AND (:module IS NULL OR module = :module) " +
      "  AND (:persona IS NULL OR persona = CAST(:persona AS persona_enum)) " +
      "  AND (:territory IS NULL OR territory IS NULL OR territory = :territory) " +
      "ORDER BY updated_at DESC LIMIT :limit";
    Query q = em.createNativeQuery(sql)
      .setParameter("module", module)
      .setParameter("persona", persona)
      .setParameter("territory", territory)
      .setParameter("limit", limit);
    @SuppressWarnings("unchecked") List<Object[]> rows = q.getResultList();
    return rows.stream().map(this::map).collect(Collectors.toList());
  }

  public List<Object[]> rawSuggest(String context, String module, String persona, String territory, int top){
    // simple ranking: context tokens vs keywords + title ILIKE
    String sql =
      "WITH ctx AS (SELECT regexp_split_to_table(:ctx, '[:/ ,]') AS tok) " +
      "SELECT a.id, a.slug, a.module, a.kind, a.title, a.summary, a.locale, a.territory, a.persona, a.keywords, a.cta, " +
      "  ( " +
      "    COALESCE((SELECT count(*) FROM ctx WHERE tok <> '' AND tok = ANY(a.keywords)),0) * 0.6 + " +
      "    CASE WHEN a.title ILIKE '%'||split_part(:ctx,'::',1)||'%' THEN 0.2 ELSE 0 END + " +
      "    CASE WHEN a.module = :module THEN 0.2 ELSE 0 END " +
      "  ) AS score " +
      "FROM help_article a " +
      "WHERE is_published = true " +
      "  AND (:module IS NULL OR a.module = :module) " +
      "  AND (:persona IS NULL OR a.persona = CAST(:persona AS persona_enum)) " +
      "  AND (:territory IS NULL OR a.territory IS NULL OR a.territory = :territory) " +
      "ORDER BY score DESC, updated_at DESC LIMIT :top";
    Query q = em.createNativeQuery(sql)
      .setParameter("ctx", context)
      .setParameter("module", module)
      .setParameter("persona", persona)
      .setParameter("territory", territory)
      .setParameter("top", top);
    @SuppressWarnings("unchecked") List<Object[]> rows = q.getResultList();
    return rows;
  }

  public void insertEvent(UUID userId, UUID tenantId, String territory, String module, String context, String topic, String type, Double score, UUID articleId, String detailsJson){
    String sql =
      "INSERT INTO help_event (user_id, tenant_id, territory, module, context, topic, event_type, score, article_id, details) " +
      "VALUES (:uid, :tid, :terr, :mod, :ctx, :topic, :type, :score, :aid, CAST(:details AS jsonb))";
    em.createNativeQuery(sql)
      .setParameter("uid", userId)
      .setParameter("tid", tenantId)
      .setParameter("terr", territory)
      .setParameter("mod", module)
      .setParameter("ctx", context)
      .setParameter("topic", topic)
      .setParameter("type", type)
      .setParameter("score", score)
      .setParameter("aid", articleId)
      .setParameter("details", detailsJson)
      .executeUpdate();
  }
}
