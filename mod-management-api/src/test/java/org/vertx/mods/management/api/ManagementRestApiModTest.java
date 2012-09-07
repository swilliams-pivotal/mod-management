package org.vertx.mods.management.api;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.test.TestVerticle;
import org.vertx.java.test.junit.VertxJUnit4ClassRunner;

@RunWith(VertxJUnit4ClassRunner.class)
@TestVerticle(main="deployer.js")
public class ManagementRestApiModTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public final void test() {
    fail("Not yet implemented"); // TODO
  }

  @After
  public void tearDown() throws Exception {
  }

}
