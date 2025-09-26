package de.freshplan.infrastructure.pg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.sql.PreparedStatement;
import org.hibernate.Session;

/**
 * Produktions-Implementierung für PostgreSQL NOTIFY via Hibernate.
 *
 * <p>Sprint 2.1.1 P0 HOTFIX - Testbare Event-Pipeline
 */
@ApplicationScoped
public class HibernatePgNotifySender implements PgNotifySender {

  @Inject EntityManager entityManager;

  @Override
  public void send(String channel, String payload) {
    Session session = entityManager.unwrap(Session.class);
    session.doWork(
        connection -> {
          try (PreparedStatement ps = connection.prepareStatement("SELECT pg_notify(?, ?)")) {
            ps.setString(1, channel);
            ps.setString(2, payload);
            ps.execute();
          }
        });
  }
}
