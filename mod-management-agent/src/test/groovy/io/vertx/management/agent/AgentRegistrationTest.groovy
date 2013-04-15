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


class AgentRegistrationTest extends TestVerticle {

  @Test
  public void testDeploySingleInstance() {
    def results = []
    vertx.eventBus().registerHandler(AGENTS_ADDRESS, { Message msg->
      results.add(msg.body)
    } as Handler, { res->
      container.deployWorkerVerticle('groovy:io.vertx.management.agent.ManagementAgent', new JsonObject(), 1, false, { did->
        vertx.setTimer(2000, { id->
          assertEquals(1, results.size())
          testComplete()
        } as Handler)
      } as Handler)
    } as AsyncResultHandler)
  }

  @Test
  public void testDeployMultipleInstances() {
    def results = []
    vertx.eventBus().registerHandler(AGENTS_ADDRESS, { Message msg->
      results.add(msg.body)
    } as Handler, { res->
      container.deployWorkerVerticle('groovy:io.vertx.management.agent.ManagementAgent', new JsonObject(), 5, false, { did->
        vertx.setTimer(2000, { id->
          assertEquals(1, results.size())
          testComplete()
        } as Handler)
      } as Handler)
    } as AsyncResultHandler)
  }

  @Test
  public void testBroadcastAddress() {
    def results = []
    vertx.eventBus().registerHandler(AGENTS_ADDRESS, { Message msg->
      results.add(msg.body)
    } as Handler, { res->
      container.deployWorkerVerticle('groovy:io.vertx.management.agent.ManagementAgent', new JsonObject(), 1, false, { did->
        vertx.setTimer(2000, { id->
          assertEquals(1, results.size())
          testComplete()
        } as Handler)
      } as Handler)
    } as AsyncResultHandler)
  }

  @Test
  public void testUniqueAddress() {
    def results = []
    vertx.eventBus().registerHandler(AGENTS_ADDRESS, { Message msg->
      results.add(msg.body)
    } as Handler, { res->
      container.deployWorkerVerticle('groovy:io.vertx.management.agent.ManagementAgent', new JsonObject(), 1, false, { did->
        vertx.setTimer(2000, { id->
          assertEquals(1, results.size())
          testComplete()
        } as Handler)
      } as Handler)
    } as AsyncResultHandler)
  }

}
