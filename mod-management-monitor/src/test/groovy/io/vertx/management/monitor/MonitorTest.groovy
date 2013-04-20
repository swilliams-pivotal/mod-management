/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.management.monitor

import static org.vertx.management.Constants.*
import static org.vertx.testtools.VertxAssert.*

import java.util.concurrent.LinkedBlockingQueue
import org.junit.Test
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.Message
import org.vertx.java.core.json.JsonObject
import org.vertx.testtools.TestVerticle
import org.vertx.testtools.VertxAssert



/**
 * @author swilliams
 *
 */
class MonitorTest extends TestVerticle {

  @Test
  public void testDeployAgent() {

    def mconfig = new JsonObject()
    container.deployVerticle('groovy:io.vertx.management.monitor.Monitor', mconfig, 1, { idm->
      assertNotNull(idm)

      def config = new JsonObject()
      container.deployModule('io.vertx~management-agent~1.0.0-SNAPSHOT', config, 1, { ida->
        assertNotNull(ida)
        testComplete()

        

      } as Handler)
    } as Handler)
  }

  @Test
  public void testDeployAgentAndMonitorStatus() {

    def queue = new LinkedBlockingQueue()

    def mconfig = new JsonObject()
    vertx.eventBus().registerHandler(STATUS_ADDRESS, { Message msg->
      println "status msg: ${msg.body}"
      queue.offer(msg.body)
    } as Handler)

    container.deployVerticle('groovy:io.vertx.management.monitor.Monitor', mconfig, 1, { idm->
      assertNotNull(idm)

      def config = new JsonObject()
      config.putNumber('period', 5)
      container.deployModule('io.vertx~management-agent~1.0.0-SNAPSHOT', config, 1, { ida->
        assertNotNull(ida)
        vertx.setTimer(10000, { id->
          assertEquals(1, queue.size())
          testComplete()
        } as Handler)
      } as Handler)
    } as Handler)
  }

}
