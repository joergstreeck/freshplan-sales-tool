package de.freshplan.domain.help.events;

import de.freshplan.domain.help.service.dto.HelpRequest;
import de.freshplan.domain.help.service.dto.UserStruggle;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event fired when help content is viewed by a user.
 *
 * <p>This event triggers side effects like view count updates and analytics tracking without
 * coupling the query operation to these write operations.
 *
 * <p>Part of Event-Driven CQRS architecture for Phase 12.2.
 */
public record HelpContentViewedEvent(
    UUID eventId,
    UUID helpContentId,
    String userId,
    String feature,
    LocalDateTime viewedAt,
    HelpRequest originalRequest,
    UserStruggle detectedStruggle,
    String userLevel,
    boolean isFirstTimeUser) {

  public static HelpContentViewedEvent create(
      UUID helpContentId, HelpRequest request, UserStruggle struggle) {

    return new HelpContentViewedEvent(
        UUID.randomUUID(), // eventId
        helpContentId, // helpContentId
        request.userId(), // userId
        request.feature(), // feature
        LocalDateTime.now(), // viewedAt
        request, // originalRequest
        struggle, // detectedStruggle
        request.userLevel(), // userLevel
        request.isFirstTime() // isFirstTimeUser
        );
  }

  /** Creates a minimal event for view count tracking only. */
  public static HelpContentViewedEvent forViewCount(UUID helpContentId, String userId) {
    return new HelpContentViewedEvent(
        UUID.randomUUID(),
        helpContentId,
        userId,
        "unknown",
        LocalDateTime.now(),
        null,
        null,
        "INTERMEDIATE",
        false);
  }
}
