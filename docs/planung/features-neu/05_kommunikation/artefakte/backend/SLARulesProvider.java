package de.freshplan.communication.sla;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.time.Duration;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;

@ApplicationScoped
public class SLARulesProvider {

  public static class Followup { public Duration offset = Duration.ZERO; public String template = ""; }
  public static class SampleDeliveredRule {
    public List<Followup> followups = new ArrayList<>();
    public Duration escalateIfNoReplyAfter = Duration.ofDays(10);
    public boolean requireMultiContact = true;
  }

  private SampleDeliveredRule sampleDelivered = new SampleDeliveredRule();

  @PostConstruct
  void load() {
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("sla-rules.yaml")) {
      if (is == null) return;
      Map<String,Object> y = new Yaml().load(is);
      Map<String,Object> rules = (Map<String,Object>) y.get("rules");
      Map<String,Object> sd = (Map<String,Object>) rules.get("sample_delivered");
      if (sd != null) {
        List<Map<String,Object>> fol = (List<Map<String,Object>>) sd.get("followups");
        if (fol != null) for (Map<String,Object> m : fol) {
          Followup f = new Followup();
          f.offset = Duration.parse((String)m.get("offset"));
          f.template = String.valueOf(m.getOrDefault("template",""));
          sampleDelivered.followups.add(f);
        }
        String esc = (String) sd.get("escalateIfNoReplyAfter");
        if (esc != null) sampleDelivered.escalateIfNoReplyAfter = Duration.parse(esc);
        Object req = sd.get("requireMultiContact"); if (req != null) sampleDelivered.requireMultiContact = (Boolean) req;
      }
    } catch (Exception ignore) {}
  }

  public SampleDeliveredRule sampleDelivered() { return sampleDelivered; }
}
