package de.freshplan.governance.settings;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@ApplicationScoped
public class SettingsNotifyListener implements Runnable {

  @Inject DataSource ds;
  @Inject SettingsService service;

  private final Logger log = Logger.getLogger(SettingsNotifyListener.class);

  @PostConstruct
  void init(){
    Thread t = new Thread(this, "settings-notify-listener");
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void run(){
    while (true){
      try (Connection c = ds.getConnection()){
        PGConnection pg = c.unwrap(PGConnection.class);
        try (Statement st = c.createStatement()){
          st.execute("LISTEN settings_changed");
        }
        while (!Thread.currentThread().isInterrupted()){
          PGNotification[] notifications = pg.getNotifications(5000);
          if (notifications != null){
            for (PGNotification n : notifications){
              // MVP: clear L1 cache broadly; optional: parse payload and invalidate per-key
              service.clearAllLocalCaches();
            }
          }
        }
      } catch (Exception e){
        log.warn("LISTEN failed, retry in 3s", e);
        try { Thread.sleep(3000); } catch (InterruptedException ie){ Thread.currentThread().interrupt(); }
      }
    }
  }
}
