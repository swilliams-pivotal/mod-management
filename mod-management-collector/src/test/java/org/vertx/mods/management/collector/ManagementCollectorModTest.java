package org.vertx.mods.management.collector;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.test.TestVerticle;
import org.vertx.java.test.VertxTestBase;
import org.vertx.java.test.junit.VertxJUnit4ClassRunner;

@RunWith(VertxJUnit4ClassRunner.class)
@TestVerticle(main="deployer.js")
public class ManagementCollectorModTest extends VertxTestBase {

  private Set<String> handlers = new HashSet<>();

  @Before
  public void setUp() throws Exception {
    // TODO setup mongo connection
    // send some metrics
  }

  @Test
  public final void test() {
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
  }

  @After
  public void tearDown() throws Exception {
    super.unregisterHandlers(handlers);
  }

}
