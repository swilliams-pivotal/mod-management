package io.vertx.management.agent

import static org.vertx.management.Constants.*
import static org.vertx.testtools.VertxAssert.*
import org.junit.Test
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject
import org.vertx.testtools.TestVerticle
import org.vertx.testtools.VertxAssert

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import groovy.json.JsonOutput


class AgentPublishingTest extends TestVerticle {

  @Test
  public void testMetricPublishing() {
    def results = []
    vertx.eventBus().registerHandler(METRICS_ADDRESS, { Message msg->
      results.add(msg.body)
    } as Handler, { res->
      def config = new JsonObject()
      config.putNumber('period', 1)
      container.deployWorkerVerticle('groovy:io.vertx.management.agent.ManagementAgent', config, 1, false, { did->
        println 'Waiting for 5s for metric data... '
        vertx.setTimer(5500, { id->
          assertEquals(5, results.size())
          testComplete()
        } as Handler)
      } as Handler)
    } as AsyncResultHandler)
  }

}
