package de.freshplan.integration.sync;

import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Db {
  private static final Logger log = LoggerFactory.getLogger(Db.class);
  private final String url, user, pass;

  public Db(String url, String user, String pass){
    this.url = url; this.user = user; this.pass = pass;
  }

  public Connection open() throws SQLException {
    Connection c = DriverManager.getConnection(url, user, pass);
    try (Statement st = c.createStatement()){
      st.execute("SELECT set_config('app.user_id', '00000000-0000-0000-0000-000000000000', true)");
      st.execute("SELECT set_config('app.roles', 'admin', true)");
      st.execute("SELECT set_config('app.tenant_id', null, true)");
      st.execute("SELECT set_config('app.org_id', null, true)");
    }
    return c;
  }

  public Map<String, String> loadEffectiveGatewaySettings() throws SQLException {
    String sql = "SELECT key, value::text FROM settings_effective WHERE key LIKE 'gateway.%' AND user_id IS NULL";
    Map<String, String> out = new HashMap<>();
    try (Connection c = open();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()){
      while (rs.next()){
        out.put(rs.getString(1), rs.getString(2));
      }
    }
    return out;
  }
}
