package de.freshplan.security;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityContractTests
 *  - JUnit 5 + Testcontainers (PostgreSQL)
 *  - Prüft 5 B2B‑Szenarien mit RLS v2
 */
@Testcontainers
public class SecurityContractTests {

  @Container
  static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:15-alpine")
          .withUsername("test").withPassword("test").withDatabaseName("testdb");

  static Connection admin;

  @BeforeAll
  static void init() throws Exception {
    PG.start();
    admin = DriverManager.getConnection(PG.getJdbcUrl(), PG.getUsername(), PG.getPassword());
    admin.createStatement().execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
    // Minimal Schema + RLS v2
    execSqlResource(admin, "/sql/rls_v2.sql");
    seedData();
  }

  @AfterAll
  static void done() throws Exception {
    if (admin!=null) admin.close();
    PG.stop();
  }

  // === Tests ===

  @Test
  void gfFullAccess_owner_can_update_lead() throws Exception {
    try (Connection c = pgUser("DE","freshfoodz","GF")) {
      var leadId = UUID.fromString("00000000-0000-0000-0000-0000000000de");
      // owner user is 1111...de
      c.createStatement().execute("UPDATE leads SET status='QUALIFIED' WHERE id='"+leadId+"'");
      // success if no exception; verify row visible
      var rs = c.createStatement().executeQuery("SELECT status FROM leads WHERE id='"+leadId+"'");
      assertTrue(rs.next());
      assertEquals("QUALIFIED", rs.getString(1));
    }
  }

  @Test
  void buyer_sees_commercial_not_product() throws Exception {
    try (Connection c = pgUser("DE","freshfoodz","BUYER")) {
      var q1 = c.createStatement().executeQuery("SELECT count(*) FROM lead_notes WHERE category='COMMERCIAL'");
      assertTrue(q1.next()); assertTrue(q1.getInt(1) > 0);
      var q2 = c.createStatement().executeQuery("SELECT count(*) FROM lead_notes WHERE category='PRODUCT'");
      assertTrue(q2.next()); assertEquals(0, q2.getInt(1));
    }
  }

  @Test
  void chef_sees_product_not_commercial() throws Exception {
    try (Connection c = pgUser("DE","freshfoodz","CHEF")) {
      var q1 = c.createStatement().executeQuery("SELECT count(*) FROM lead_notes WHERE category='PRODUCT'");
      assertTrue(q1.next()); assertTrue(q1.getInt(1) > 0);
      var q2 = c.createStatement().executeQuery("SELECT count(*) FROM lead_notes WHERE category='COMMERCIAL'");
      assertTrue(q2.next()); assertEquals(0, q2.getInt(1));
    }
  }

  @Test
  void territory_isolation_de_user_cannot_see_ch_lead() throws Exception {
    try (Connection c = pgUser("DE","freshfoodz","GF")) {
      var rs = c.createStatement().executeQuery("SELECT count(*) FROM leads WHERE territory='CH'");
      assertTrue(rs.next()); assertEquals(0, rs.getInt(1));
    }
  }

  @Test
  void collaborator_can_read_manager_override_can_update() throws Exception {
    var leadId = UUID.fromString("00000000-0000-0000-0000-0000000000de");
    // collaborator (not owner) can read activities
    try (Connection c = pgUser("DE","freshfoodz","BUYER", new String[]{"activity:read"})) {
      var rs = c.createStatement().executeQuery("SELECT count(*) FROM activities WHERE lead_id='"+leadId+"'");
      assertTrue(rs.next()); assertTrue(rs.getInt(1) > 0);
      // but cannot update lead
      assertThrows(SQLException.class, () -> c.createStatement().execute("UPDATE leads SET status='HOT' WHERE id='"+leadId+"'"));
    }
    // manager override can update
    try (Connection c = pgUser("DE","freshfoodz","GF", new String[]{"lead:override"})) {
      c.createStatement().execute("UPDATE leads SET status='HOT' WHERE id='"+leadId+"'");
      var rs = c.createStatement().executeQuery("SELECT status FROM leads WHERE id='"+leadId+"'");
      assertTrue(rs.next()); assertEquals("HOT", rs.getString(1));
    }
  }

  // === Helpers ===
  static void seedData() throws Exception {
    // Leads DE/CH
    var leadDe = UUID.fromString("00000000-0000-0000-0000-0000000000de");
    var leadCh = UUID.fromString("00000000-0000-0000-0000-0000000000c1");
    admin.createStatement().execute("INSERT INTO leads(id,org_id,territory,status) VALUES " +
            "('"+leadDe+"','freshfoodz','DE','WARM'),('"+leadCh+"','freshfoodz','CH','WARM')");
    // Owner assignment (user 1111...de owns DE lead)
    admin.createStatement().execute("INSERT INTO user_lead_assignments(lead_id,user_id,territory) VALUES " +
            "('"+leadDe+"','11111111-1111-1111-1111-1111111111de','DE')");
    // Collaborator (user 2222...de) on DE lead
    admin.createStatement().execute("INSERT INTO lead_collaborators(lead_id,user_id) VALUES " +
            "('"+leadDe+"','22222222-2222-2222-2222-2222222222de')");
    // Activities
    admin.createStatement().execute("INSERT INTO activities(id,lead_id,kind) VALUES " +
            "('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1','"+leadDe+"','QUALIFIED_CALL')");
    // Notes
    admin.createStatement().execute("INSERT INTO lead_notes(id,lead_id,author_user_id,visibility,category,body) VALUES " +
            "('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1','"+leadDe+"','11111111-1111-1111-1111-1111111111de','ACCOUNT_TEAM','COMMERCIAL','Pricing terms')," +
            "('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2','"+leadDe+"','11111111-1111-1111-1111-1111111111de','ACCOUNT_TEAM','PRODUCT','Taste feedback')");
  }

  static Connection pgUser(String territory, String org, String contactRole) throws Exception {
    return pgUser(territory, org, contactRole, new String[] {"lead:read","lead:edit","activity:read","note:read"});
  }
  static Connection pgUser(String territory, String org, String contactRole, String[] scopes) throws Exception {
    Connection c = DriverManager.getConnection(PG.getJdbcUrl(), PG.getUsername(), PG.getPassword());
    c.setAutoCommit(false);
    try (var st = c.createStatement()) {
      st.execute("SET LOCAL app.user_id = '" + userFor(territory) + "'");
      st.execute("SET LOCAL app.org_id = '" + org + "'");
      st.execute("SET LOCAL app.territory = '" + territory + "'");
      st.execute("SET LOCAL app.scopes = '" + String.join(",", scopes) + "'");
      st.execute("SET LOCAL app.contact_roles = '" + contactRole + "'");
    }
    return c;
  }

  static String userFor(String territory) {
    return "DE".equals(territory) ? "11111111-1111-1111-1111-1111111111de" : "33333333-3333-3333-3333-3333333333ch";
  }

  static void execSqlResource(Connection c, String res) throws Exception {
    try (var is = SecurityContractTests.class.getResourceAsStream(res)) {
      if (is == null) throw new IllegalStateException("Missing resource: " + res);
      var sql = new String(is.readAllBytes());
      for (String stmt : sql.split(";\\s*\\n")) {
        var s = stmt.trim();
        if (s.isEmpty()) continue;
        c.createStatement().execute(s);
      }
    }
  }
}
