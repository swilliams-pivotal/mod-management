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

import groovy.json.JsonOutput
import groovy.transform.CompileStatic;

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.VoidResult


@CompileStatic
class Monitor extends Verticle {

  private static final String MANAGEMENT_MONITOR_MAP = 'management.monitor'
  private static final String MANAGEMENT_MEMBER_MAP = 'management.members'
  private static final String MANAGEMENT_TSTAMP_MAP = 'management.tstamps'

  boolean started = false
  long periodicId
  long lastCheck
  long sequenceId = 0
  long missedReports = 2

  @Override
  def start(VoidResult result) throws Exception {
    def agent = vertx.sharedData.getMap(MANAGEMENT_MONITOR_MAP)
    def random = new Random().nextLong()
    def uuid = new UUID(System.currentTimeMillis(), random).toString()

    agent.putIfAbsent('uid', uuid)
    if (uid() == uuid) started = true

    if (started) vertx.eventBus.registerHandler(METRICS_ADDRESS, this.&receiver) { AsyncResult arm->
      if (!arm.succeeded()) result.setFailure(arm.exception)

      vertx.eventBus.registerHandler(AGENTS_ADDRESS, this.&receiver) { AsyncResult ars->
        if (!ars.succeeded()) result.setFailure(ars.exception)

        long period = 5000L
        this.periodicId = vertx.setPeriodic period, this.&monitor
        result.setResult()
      }
    }
  }

  @Override
  def stop() throws Exception {
    if (started) vertx.eventBus.unregisterHandler(METRICS_ADDRESS, this.&receiver) { arm->
      vertx.eventBus.unregisterHandler(AGENTS_ADDRESS, this.&receiver) { ars->
        vertx.cancelTimer periodicId
        vertx.sharedData.removeMap(MANAGEMENT_MEMBER_MAP)
        vertx.sharedData.removeMap(MANAGEMENT_MONITOR_MAP)
        vertx.sharedData.removeMap(MANAGEMENT_TSTAMP_MAP)
      }
    }
  }

  private void monitor(long id) {
    sequenceId++
    this.lastCheck = System.currentTimeMillis()
//    def json = [
//      command: 'ping',
//      args: [
//        tstamp: lastCheck,
//        sequence: sequenceId
//      ]
//    ]

    def tstamps = vertx.sharedData.getMap(MANAGEMENT_TSTAMP_MAP)
    def members = vertx.sharedData.getMap(MANAGEMENT_MEMBER_MAP)

    def keySet = tstamps.keySet()

    for (String memberId : keySet) {
      long lastSeen = tstamps.get(memberId) as long
      long period = members.get(memberId) as long
      def message = 'ok'
      if ((lastCheck - lastSeen) > (period*missedReports)) {
        message = 'disappeared'
      }
      else if ((lastCheck - lastSeen) > period) {
        message = 'slacking'
      }

      println "Oh Noes! '$memberId' is $message!"
      vertx.eventBus.publish(STATUS_ADDRESS, [id: memberId, status: message])
    }
  }

  def receiver(Message msg) {
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

    // is it a metrics message?
    if (body.containsKey('metrics')) {
      analyseMetrics(body)
    }
  }

  private void analyseMetrics(Map body) {
    println 'metrics message!'
  }

  private String uid() {
    def agent = vertx.sharedData.getMap(MANAGEMENT_MONITOR_MAP)
    agent.get 'uid'
  }
}
