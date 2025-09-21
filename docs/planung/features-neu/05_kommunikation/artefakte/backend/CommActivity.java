package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="comm_activity",
  indexes = @Index(name="ix_comm_activity_customer_time", columnList="customer_id, occurred_at DESC"))
public class CommActivity {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  @Column(name="customer_id", columnDefinition="uuid", nullable=false) public UUID customerId;
  @Column(nullable=false) public String territory;
  @Column(nullable=false) public String kind; // CALL|MEETING|FOLLOW_UP
  @Column(name="occurred_at", nullable=false) public OffsetDateTime occurredAt;
  @Column(nullable=false, columnDefinition="text") public String summary;
  @Column(columnDefinition="jsonb") public String participants = "[]";
  @Column(name="created_at") public OffsetDateTime createdAt = OffsetDateTime.now();
}
