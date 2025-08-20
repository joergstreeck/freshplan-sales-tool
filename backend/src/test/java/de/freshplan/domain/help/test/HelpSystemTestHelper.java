package de.freshplan.domain.help.test;

import de.freshplan.domain.help.entity.HelpContent;
import de.freshplan.domain.help.repository.HelpContentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Test Helper Service für Phase 12.4 Integration Tests
 *
 * <p>Dieser Service löst das CDI Context Problem bei Awaitility Tests. Awaitility läuft in
 * separaten Threads ohne CDI Request Context. Mit @ActivateRequestContext stellen wir sicher, dass
 * Repository-Zugriffe funktionieren.
 *
 * @since Phase 12.4 CQRS Testing
 */
@ApplicationScoped
public class HelpSystemTestHelper {

  @Inject HelpContentRepository helpRepository;

  /** Findet HelpContent mit aktivem Request Context. Wird von Awaitility Assertions aufgerufen. */
  @ActivateRequestContext
  public Optional<HelpContent> findHelpContentById(UUID id) {
    return helpRepository.findByIdOptional(id);
  }

  /** Zählt alle HelpContent Einträge mit aktivem Request Context. */
  @ActivateRequestContext
  public long countAllHelpContent() {
    return helpRepository.count();
  }

  /** Prüft ob HelpContent existiert mit aktivem Request Context. */
  @ActivateRequestContext
  public boolean helpContentExists(UUID id) {
    return helpRepository.findByIdOptional(id).isPresent();
  }
}
