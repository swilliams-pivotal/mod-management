package org.vertx.mods.management.agent;

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
public class ManagementAgentModTest extends VertxTestBase {

  private Set<String> handlers = new HashSet<>();

  @Before
  public void setUp() throws Exception {

    String handler = super.registerHandler("vertx.management.metrics", new Handler<Message<JsonObject>>() {
      @Override
      public void handle(Message<JsonObject> event) {
        System.out.println(event.body.encode());
      }
    });

    handlers.add(handler);
  }

  @Test
  public final void test() {
    try {
      Thread.sleep(10000L);
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
  }

  @After
  public void tearDown() throws Exception {
    //
    super.unregisterHandlers(handlers);
  }

}
