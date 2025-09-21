package de.freshplan.integration.sync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncJob implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(SyncJob.class);

  private final Db db;
  private final KongDeckGenerator gen;
  private final DeckCli deck;
  private final AuditLog audit;
  private final boolean dryRun;
  private final int intervalSec;

  public SyncJob(Db db, KongDeckGenerator gen, DeckCli deck, AuditLog audit, boolean dryRun, int intervalSec){
    this.db = db; this.gen = gen; this.deck = deck; this.audit = audit; this.dryRun = dryRun; this.intervalSec = intervalSec;
  }

  @Override
  public void run(){
    while(true){
      try {
        Map<String,String> settings = db.loadEffectiveGatewaySettings();
        String tmp = Files.createTempFile("kong-", ".yaml").toString();
        gen.render(settings, System.getenv().getOrDefault("KONG_ADDR","http://kong:8001"), tmp);
        String diff = deck.diff(tmp);
        Map<String,Object> detail = new HashMap<>();
        detail.put("diff_size", diff.length());
        detail.put("dry_run", dryRun);
        audit.write("deck.diff", true, detail);
        log.info("deck diff:
{}", diff);
        if (!dryRun && diff != null && diff.trim().length() > 0 && !diff.contains("No changes detected")){
          String out = deck.sync(tmp);
          audit.write("deck.sync", true, Map.of("output", out.length()+"b"));
          log.info("deck sync:
{}", out);
        }
      } catch(Exception e){
        audit.write("sync.error", false, Map.of("error", e.getMessage()));
        log.error("Sync error", e);
      }
      try { TimeUnit.SECONDS.sleep(intervalSec); } catch (InterruptedException ie){ Thread.currentThread().interrupt(); break; }
    }
  }

  public static void main(String[] args){
    String url = System.getenv().getOrDefault("DB_JDBC_URL","jdbc:postgresql://localhost:5432/freshplan");
    String user = System.getenv().getOrDefault("DB_USERNAME","app_admin");
    String pass = System.getenv().getOrDefault("DB_PASSWORD","secret");
    String deckBin = System.getenv().getOrDefault("DECK_BIN","deck");
    String kongAddr = System.getenv().getOrDefault("KONG_ADDR","http://kong:8001");
    boolean dry = Boolean.parseBoolean(System.getenv().getOrDefault("SYNC_DRY_RUN","true"));
    int interval = Integer.parseInt(System.getenv().getOrDefault("SYNC_INTERVAL_SEC","300"));
    String auditPath = System.getenv().getOrDefault("AUDIT_LOG","./audit/settings-gateway-sync.jsonl");

    Db db = new Db(url, user, pass);
    KongDeckGenerator gen = new KongDeckGenerator();
    DeckCli deck = new DeckCli(deckBin, kongAddr);
    AuditLog audit = new AuditLog(auditPath);

    new SyncJob(db, gen, deck, audit, dry, interval).run();
  }
}
