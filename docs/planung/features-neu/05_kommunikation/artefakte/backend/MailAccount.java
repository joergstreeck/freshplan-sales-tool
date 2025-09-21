package de.freshplan.communication.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="mail_account")
public class MailAccount {
  @Id @Column(columnDefinition="uuid") public UUID id = UUID.randomUUID();
  @Column(nullable=false, unique=true) public String email;
  public String displayName;
  public boolean dkimEnabled;
  public String territory;
}
