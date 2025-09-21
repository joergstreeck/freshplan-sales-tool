package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="participant_set")
public class ParticipantSet {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  public String fromEmail;
  @Column(columnDefinition="jsonb") public String toEmails = "[]";
  @Column(columnDefinition="jsonb") public String ccEmails = "[]";
  @Column(columnDefinition="jsonb") public String bccEmails = "[]";
}
