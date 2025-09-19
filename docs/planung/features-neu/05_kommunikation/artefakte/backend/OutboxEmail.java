package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="outbox_emails",
  indexes = { @Index(name="ix_outbox_status_time", columnList="status, next_attempt_at"),
              @Index(name="ix_outbox_rate_bucket", columnList="rate_bucket") })
public class OutboxEmail {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  @Column(name="message_id", columnDefinition="uuid", nullable=false) public UUID messageId;
  @Column(name="rate_bucket") public String rateBucket;
  @Column(nullable=false) public String status = "PENDING"; // PENDING|SENDING|SENT|FAILED
  @Column(name="retry_count") public int retryCount = 0;
  @Column(name="next_attempt_at") public OffsetDateTime nextAttemptAt = OffsetDateTime.now();
  @Column(name="locked_by") public String lockedBy;
  @Column(name="locked_at") public OffsetDateTime lockedAt;
  @Column(name="last_error", columnDefinition="text") public String lastError;
  @Column(name="created_at") public OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") public OffsetDateTime updatedAt = OffsetDateTime.now();
}
