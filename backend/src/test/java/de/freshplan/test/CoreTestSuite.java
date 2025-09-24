package de.freshplan.test;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Test Suite for Core Tests - runs only tests tagged with @Tag("core")
 *
 * <p>Used in CI to run only the stable core tests
 */
@Suite
@SelectPackages({
  "de.freshplan.api",
  "de.freshplan.domain",
  "de.freshplan.greenpath",
  "de.freshplan.test"
})
@IncludeTags("core")
public class CoreTestSuite {
  // This class remains empty, it is used only as a holder for the above annotations
}
