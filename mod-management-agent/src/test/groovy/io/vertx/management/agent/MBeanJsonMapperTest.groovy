package io.vertx.management.agent

import org.junit.Test
import org.vertx.java.core.Handler
import org.vertx.java.core.json.JsonObject
import org.vertx.testtools.TestVerticle
import org.vertx.testtools.VertxAssert

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import groovy.json.JsonOutput


class MBeanJsonMapperTest extends TestVerticle {

  @Test
  public void testFoo() {
//    container.deployWorkerVerticle('groovy:io.vertx.management.agent.ManagementAgent', new JsonObject(), 5, true, { did->
      VertxAssert.testComplete()
//    } as Handler)
  }

}
