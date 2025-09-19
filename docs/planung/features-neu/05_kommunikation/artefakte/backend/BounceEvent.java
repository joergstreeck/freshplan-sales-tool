package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="bounce_events")
public class BounceEvent {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  @Column(name="message_id", columnDefinition="uuid", nullable=false) public UUID messageId;
  @Column(nullable=false) public String severity; // HARD|SOFT
  @Column(name="smtp_code") public String smtpCode;
  public String reason;
  @Column(name="created_at") public OffsetDateTime createdAt = OffsetDateTime.now();
}
