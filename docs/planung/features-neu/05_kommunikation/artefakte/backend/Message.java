package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="communication_messages",
  indexes = { @Index(name="ix_comm_msgs_thread_time", columnList="thread_id, created_at DESC"),
              @Index(name="ix_comm_msgs_mime", columnList="mime_message_id") })
public class Message {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  @Column(name="thread_id", columnDefinition="uuid", nullable=false) public UUID threadId;
  @Column(nullable=false) public String territory;
  @Column(nullable=false) public String direction; // INBOUND|OUTBOUND
  @Column(nullable=false) public String status; // RECEIVED|SENT|QUEUED|BOUNCED
  public String subject;
  @Column(name="body_text", columnDefinition="text") public String bodyText;
  public String senderEmail;
  @Column(columnDefinition="jsonb") public String recipients = "[]";
  @Column(name="mime_message_id") public String mimeMessageId;
  @Column(name="in_reply_to") public String inReplyTo;
  @Column(name="created_at") public OffsetDateTime createdAt = OffsetDateTime.now();
  @Column(name="updated_at") public OffsetDateTime updatedAt = OffsetDateTime.now();
}
