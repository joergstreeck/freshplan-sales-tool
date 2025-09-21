package de.freshplan.admin.contract;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
public class RlsIsolationTest {

  @Inject EntityManager em;

  @Test
  void rlsBlocksCrossTenantRead(){
    // Session variables should be set by RlsSessionFilter; here we assert query returns zero rows for foreign tenant
    var cnt = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM admin_audit WHERE tenant_id <> current_setting('app.tenant_id')::uuid").getSingleResult()).intValue();
    assertEquals(0, cnt, "RLS should block cross-tenant reads");
  }
}
