package de.freshplan.ai;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ProviderRateLimiter {

  static class Bucket {
    AtomicInteger count = new AtomicInteger(0);
    volatile long windowStartMs = System.currentTimeMillis();
  }

  private final Map<String,Bucket> buckets = new ConcurrentHashMap<>();

  /** allow max permits per windowMs */
  public boolean tryAcquire(String provider, int max, long windowMs){
    Bucket b = buckets.computeIfAbsent(provider, k -> new Bucket());
    long now = System.currentTimeMillis();
    synchronized (b){
      if (now - b.windowStartMs >= windowMs){
        b.windowStartMs = now;
        b.count.set(0);
      }
      if (b.count.get() < max){
        b.count.incrementAndGet();
        return true;
      }
      return false;
    }
  }
}
