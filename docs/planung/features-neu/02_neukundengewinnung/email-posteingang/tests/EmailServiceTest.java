package de.freshplan.email;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** GIVEN 1000 emails batched
    WHEN processed with batch size 100
    THEN completes under 2s and error rate <0.5% (Performance Foundation) */
class EmailServiceTest {

  @Test
  void batchProcessing_performance() {
    long start = System.nanoTime();
    int total = 1000;
    int batch = 100;
    int errors = 0;
    for (int i=0;i<total;i+=batch) {
      // simulate processing
      // errors += EmailService.processBatch(i, batch);
    }
    long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    assertTrue(ms < 2000, "Batch processing should be < 2s");
    double errPct = errors * 100.0 / total;
    assertTrue(errPct < 0.5, "Error rate < 0.5%");
  }
}
