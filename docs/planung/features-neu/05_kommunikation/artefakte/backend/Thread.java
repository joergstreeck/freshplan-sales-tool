package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="communication_threads",
  indexes = {
    @Index(name="ix_comm_threads_customer", columnList="customer_id"),
    @Index(name="ix_comm_threads_territory_last", columnList="territory, last_message_at DESC")
  }
)
public class Thread {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  @Column(name="customer_id", columnDefinition="uuid", nullable=false) public UUID customerId;
  @Column(nullable=false) public String territory;
  @Column(nullable=false) public String channel = "EMAIL";
  @Column(nullable=false) public String subject;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="participant_set_id") public ParticipantSet participantSet;
  @Column(name="last_message_at") public OffsetDateTime lastMessageAt = OffsetDateTime.now();
  @Column(name="unread_count") public int unreadCount = 0;
  @Version public int version;
  @Column(name="created_at") public OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") public OffsetDateTime updatedAt = OffsetDateTime.now();
}
