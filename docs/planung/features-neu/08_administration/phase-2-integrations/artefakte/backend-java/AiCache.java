package de.freshplan.ai;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AiCache {
  static class Entry {
    AiRouterInterceptor.AiResponse resp;
    long expiresAt;
  }
  private final Map<String, Entry> store = new ConcurrentHashMap<>();

  public AiRouterInterceptor.AiResponse get(String key){
    Entry e = store.get(key);
    if (e == null) return null;
    if (System.currentTimeMillis() > e.expiresAt){
      store.remove(key);
      return null;
    }
    return e.resp;
    }

  public void put(String key, AiRouterInterceptor.AiResponse resp, Duration ttl){
    Entry e = new Entry();
    e.resp = resp;
    e.expiresAt = System.currentTimeMillis() + ttl.toMillis();
    store.put(key, e);
  }
}
