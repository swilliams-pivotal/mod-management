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

import java.util.Map;

import groovy.json.JsonOutput
//import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode;

import org.vertx.groovy.core.eventbus.Message
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Future
import io.vertx.mods.management.SingleInstanceVerticle


/**
 * @author swilliams
 *
 */
@CompileStatic
class Monitor extends SingleInstanceVerticle {

  private static final String MANAGEMENT_MONITOR_MAP = 'management.monitor'
  private static final String MANAGEMENT_MEMBER_MAP = 'management.members'
  private static final String MANAGEMENT_TSTAMP_MAP = 'management.tstamps'


  Map config = [
    period: 5000L,
    periodicId: 0,
    lastCheck: 0,
    sequenceId: 0,
    missedReports: 2,
    agents: AGENTS_ADDRESS,
    metrics: METRICS_ADDRESS
  ]

  @Override
  protected String prestart() throws Exception {
    Map provided = container.getConfig()

    // apply only the things we already know about
    // configure against a known default config
    config.each { key, val->
      if (provided.containsKey(key))
        config[key] = provided[key]
    }

    MANAGEMENT_MONITOR_MAP
  }

  @Override
  protected void startMod(Future<Void> result) throws Exception {

    vertx.eventBus.registerHandler(config['metrics'] as String, this.&receiver) { AsyncResult arm->
      if (!arm.succeeded()) result.setFailure(arm.cause())

      vertx.eventBus.registerHandler(config['agents'] as String, this.&receiver) { AsyncResult ars->
        if (!ars.succeeded()) result.setFailure(ars.cause())
        config['periodicId'] = vertx.setPeriodic config['period'] as long, this.&monitor
        result.setResult(null)
      }
    }
  }

  @Override
  protected void stopMod() {

    vertx.cancelTimer config['periodicId'] as long
    vertx.eventBus.unregisterHandler(config['metrics'] as String, this.&receiver)
    vertx.eventBus.unregisterHandler(config['agents'] as String, this.&receiver)

    // do this anyway?
    vertx.sharedData.removeMap(MANAGEMENT_MEMBER_MAP)
    vertx.sharedData.removeMap(MANAGEMENT_TSTAMP_MAP)
    vertx.sharedData.removeMap(MANAGEMENT_MONITOR_MAP)
  }


  private void monitor(long id) {
    config['sequenceId'] = (config['sequenceId'] as long) + 1
    config['lastCheck'] = System.currentTimeMillis()

    def tstamps = vertx.sharedData.getMap(MANAGEMENT_TSTAMP_MAP)
    def members = vertx.sharedData.getMap(MANAGEMENT_MEMBER_MAP)

    def keySet = tstamps.keySet()

    for (String memberId : keySet) {
      long lastSeen = tstamps.get(memberId) as long
      long mperiod = members.get(memberId) as long
      long diff = (config['lastCheck'] as long) - lastSeen
      long mprd = mperiod * 1000
      String message = null

      if (diff > (mprd * (config['missedReports'] as long))) {
        message = 'disappeared'
      }
      else if (diff > mprd) {
        message = 'slacking'
      }

      if (message) {
        println "Oh Noes! '$memberId' is $message!"
        vertx.eventBus.publish(STATUS_ADDRESS, [id: memberId, status: message])
      }
    }
  }

  private void receiver(Message msg) {
    def body = msg.body as Map
    String sender = body['sender']
    long tstamp = body['tstamp'] as long

    def members = vertx.sharedData.getMap(MANAGEMENT_MEMBER_MAP)

    if (!members.containsKey(sender)) {
      def address = "${AGENTS_ADDRESS}.${body['sender']}"
      def test = [type:'config', tstamp: System.currentTimeMillis()]

      // send a test message and publish an announcement on reply
      vertx.eventBus.send(address, test) { Message reply->
        def rbody = reply.body as Map
        members.put(sender, rbody['period'])
        vertx.eventBus.publish(STATUS_ADDRESS, [id: sender, status: 'appeared'])
      }
    }

    def tstamps = vertx.sharedData.getMap(MANAGEMENT_TSTAMP_MAP)
    tstamps.put(sender, tstamp)

    // is it a shutdown message?
    if (body.containsKey('type') && body['type'] == 'shutdown') {
      body.put 'status', body['type']
      vertx.eventBus.publish(STATUS_ADDRESS, body)
    }
    // is it a metrics message?
    else if (body.containsKey('metrics')) {
      analyseMetrics(body)
    }
  }

  @CompileStatic(TypeCheckingMode.SKIP)
  private void analyseMetrics(Map body) {

    def metrics = body['metrics'] as Map

    println JsonOutput.prettyPrint(JsonOutput.toJson(metrics))

    long maxHeap = metrics.'java.lang:type=Memory'.heapMemoryUsage.max
    long usedHeap = metrics.'java.lang:type=Memory'.heapMemoryUsage.used
    long maxNonHeap = metrics.'java.lang:type=Memory'.heapMemoryUsage.max
    long usedNonHeap = metrics.'java.lang:type=Memory'.heapMemoryUsage.used

    println "maxHeap: $maxHeap"
    println "usedHeap: $usedHeap"
    println "maxNonHeap: $maxNonHeap"
    println "usedNonHeap: $usedNonHeap"

  }

  private String uid() {
    def agent = vertx.sharedData.getMap(MANAGEMENT_MONITOR_MAP)
    agent.get 'uid'
  }
}
