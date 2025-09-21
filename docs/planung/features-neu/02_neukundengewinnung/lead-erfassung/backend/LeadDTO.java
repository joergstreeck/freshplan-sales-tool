package de.freshplan.leads.dto;

import de.freshplan.leads.domain.LeadEntity;
import de.freshplan.leads.domain.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.*;

public class LeadDTO {
  public static class CreateRequest {
    @NotBlank @Size(max=200) public String name;
    @NotBlank @Pattern(regexp="^[A-Z]{2,5}$") public String territory; // e.g., BER, NRW
    public LeadStatus status = LeadStatus.NEW;
  }

  public static class Item {
    public UUID id; public String name; public String territory; public LeadStatus status; public OffsetDateTime createdAt;
    public static Item from(LeadEntity e) {
      Item i = new Item();
      i.id = e.getId(); i.name = e.getName(); i.territory = e.getTerritory(); i.status = e.getStatus(); i.createdAt = e.getCreatedAt();
      return i;
    }
  }

  public static class Page {
    public List<Item> items = new ArrayList<>();
    public String nextCursor;
  }
}
