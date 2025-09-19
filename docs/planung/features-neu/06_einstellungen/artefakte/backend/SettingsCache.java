package de.freshplan.settings.cache;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.freshplan.settings.service.SettingsMergeEngine;
import de.freshplan.settings.repo.SettingsRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** TTL cache invalidated by Postgres LISTEN/NOTIFY (channel: settings_changed). */
@ApplicationScoped
public class SettingsCache {
  static class Entry { ObjectNode blob; String etag; Instant ts; }
  private final Map<String, Entry> cache = new ConcurrentHashMap<>();
  private final long ttlSeconds = Long.getLong("settings.cache.ttl.seconds", 60);

  @Inject SettingsMergeEngine engine;
  @Inject SettingsRepository repo;
  @Inject DataSource ds;

  @PostConstruct void init(){ new Thread(this::listenLoop, "settings-listener").start(); }

  private void listenLoop(){
    try (Connection c = ds.getConnection(); Statement st = c.createStatement()){
      st.execute("LISTEN settings_changed");
      while (true){
        Thread.sleep(1000L);
        var pg = c.unwrap(org.postgresql.PGConnection.class);
        var notes = pg.getNotifications();
        if (notes != null) for (var n : notes){
          String sig = n.getParameter();
          if (sig==null || sig.isBlank()) cache.clear(); else cache.remove(sig);
        }
      }
    } catch (Exception e){ /* restart */ new Thread(this::listenLoop).start(); }
  }

  public SettingsMergeEngine.Result getOrCompute(SettingsMergeEngine.Scope s){
    String key = SettingsMergeEngine.computeScopeSig(s);
    Entry e = cache.get(key);
    if (e != null && Duration.between(e.ts, Instant.now()).getSeconds() < ttlSeconds){
      return new SettingsMergeEngine.Result(e.blob, e.etag, java.time.OffsetDateTime.now());
    }
    var res = engine.computeEffective(s);
    Entry ne = new Entry(); ne.blob = res.blob(); ne.etag = res.etag(); ne.ts = Instant.now();
    cache.put(key, ne);
    return res;
  }
}
