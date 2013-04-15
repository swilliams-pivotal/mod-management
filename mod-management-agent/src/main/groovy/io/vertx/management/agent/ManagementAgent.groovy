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
package io.vertx.management.agent

import static org.vertx.management.Constants.*

import java.lang.management.ManagementFactory;
import java.util.concurrent.locks.ReentrantLock

import groovy.transform.CompileStatic
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.VoidResult;


/**
 * @author pidster
 *
 */
@CompileStatic
class ManagementAgent extends Verticle {

  private static final String MANAGEMENT_AGENT_MAP = 'management.agent'
  static def BBR_CLASS = BlackBoxRecorder.name

  long periodicID = 0

  List allowed = [
    'test', 'config', 'ping', 'pong', 'reregister', 'reconfigure', 'checkBean',
    'listDeployments', 'deployModule', 'undeployModule',
    'deployVerticle', 'deployWorkerVerticle', 'undeployVerticle'
  ]

  String listener
  boolean started = false

  Map config = [
    period: 10,
    group: 'default',
    agents: AGENTS_ADDRESS,
    metrics: METRICS_ADDRESS,
    beans: [
      ManagementFactory.MEMORY_MXBEAN_NAME,
      ManagementFactory.THREAD_MXBEAN_NAME,
      'io.vertx.*:type=*'
    ]
  ]

  Map deployments = [:]

  @Override
  def start(VoidResult startedResult) throws Exception {
    Map provided = container.getConfig()

    // apply only the things we already know about
    config.each { key, val->
      if (provided.containsKey(key)) {
        config[key] = provided[key]
      }
    }

    def agent = vertx.sharedData.getMap(MANAGEMENT_AGENT_MAP)
    def random = new Random().nextLong()
    def uuid = new UUID(System.currentTimeMillis(), random).toString()

    agent.putIfAbsent('uid', provided['uid'] ?: uuid)
    if (uid() == uuid) started = true

    if (started) {
      // deploy BlackBoxRecorder if configured
      if (provided['enable-bbr']) {
        def bbr_config = [:]
        bbr_config['address'] = config['metrics']

        container.deployVerticle("groovy:${BBR_CLASS}", bbr_config, 1) { id->
          println "Deployed BlackBoxRecorder: $id"
        }
      }

      this.listener = String.format('%s.%s', config['agents'], uid())

      configure()
      started()
    }

    startedResult.setResult()
  }

  @Override
  def stop() throws Exception {
    if (started) {
      shutdown()
      deconfigure()
      vertx.sharedData.removeMap(MANAGEMENT_AGENT_MAP)
    }
  }

  private Map data(Map json = [:]) {
    if (!json.containsKey('sender')) json['sender'] = uid()
    if (!json.containsKey('tstamp')) json['tstamp'] = System.currentTimeMillis()
    json
  }

  /**
   * 
   * @param address
   * @param json
   */
  private void publish(Map json, String address = config['metrics']) {
    // println "sending ${data(json)} to '${address}'"
    vertx.eventBus.publish address, data(json)
  }

  private void configure() {
    // register a unique listener and a broadcast listener
    vertx.eventBus.registerHandler(this.listener, this.&receiver) { AsyncResult arb->
      if (arb.succeeded()) {
        vertx.eventBus.registerHandler(config['agents'] as String, this.&receiver) { AsyncResult ara->
          if (ara.succeeded()) {
            long period = config['period'] as long
            this.periodicID = vertx.setPeriodic period * 1000, this.&collectAndPublish
          }
        }
      }
    }
  }

  private void reconfigure(Map config) {
    this.config.putAll config
    deconfigure()
    configure()
  }

  private void deconfigure() {
    if (periodicID) vertx.cancelTimer(periodicID)
    vertx.eventBus.unregisterHandler this.listener, this.&receiver
    vertx.eventBus.unregisterHandler config['agents'] as String, this.&receiver
  }

  private void started() {
    def json = [
      type: 'started'
    ]
    publish json, config['agents'] as String
  }

  private void shutdown() {
    def json = [
      type: 'shutdown'
    ]
    publish json, config['agents'] as String
  }

  def receiver(Message message) {
    def body = message.body as Map
    String cmd = body['type']
    if (allowed.contains(cmd)) {
      invokeMethod(cmd, message)
    }
  }

  private void ping(Message msg) {
    def command = msg.body as Map
    String address = command['address']
    vertx.eventBus.send(address, System.currentTimeMillis())
  }

  private void pong(Message msg) {
    long timestamp = msg.body as long
    msg.reply(System.currentTimeMillis() - timestamp)
  }

  private void test(Message msg) {
    def body = msg.body as Map
    long timestamp = body['tstamp'] as long
    body.put('diff', System.currentTimeMillis() - timestamp)
    msg.reply(body)
  }

  private void config(Message msg) {
    msg.reply(config)
  }

  private void checkBean(Message msg) {
    def command = msg.body as Map
    String name = command['name']
    String address = command['address'] ?: config['metrics']

    def names = JMX.queryNames name
    def beans = JMX.parseToList names
    def json = [:]
    json['data'] = beans
    publish json, address
  }


  private void deployModule(Message msg) {
    def command = msg.body as Map
    String moduleName = command['module']
    Map config = command['config'] as Map ?: [:]
    int instances = command['instances'] as int ?: 1

    container.deployModule(moduleName, config, instances) { String did->
      deployments.put moduleName, did
      def json = [
        verticle: moduleName,
        deployment: did,
        status: 'deployed'
      ]
      publish json
    }
  }

  private void undeployModule(Message msg) {
    def command = msg.body as Map
    String moduleName = command['module']
    if (moduleName.startsWith('io.vertx~management-agent~')) return
    String deploymentId = deployments.get('deployment')

    container.undeployModule(deploymentId) {
      // undeployed
      def json = [
        verticle: moduleName,
        deployment: deploymentId,
        status: 'undeployed'
      ]
      publish json
    }
  }

  private void deployVerticle(Message msg) {
    def command = msg.body as Map
    String verticleName = command['verticle']
    Map config = command['config'] as Map ?: [:]
    int instances = command['instances'] as int ?: 1

    container.deployVerticle(verticleName, config, instances) { String did->
      deployments.put verticleName, did
      def json = [
        verticle: verticleName,
        deployment: did,
        status: 'deployed'
      ]
      publish json
    }
  }

  private void deployWorkerVerticle(Message msg) {
    def command = msg.body as Map
    String verticleName = command['verticle']
    Map config = command['config'] as Map ?: [:]
    int instances = command['instances'] as int ?: 1

    container.deployVerticle(verticleName, config, instances) { String did->
      deployments.put verticleName, did
      def json = [
        verticle: verticleName,
        deployment: did,
        status: 'deployed'
      ]
      publish json
    }
  }

  private void undeployVerticle(Message msg) {
    def command = msg.body as Map
    String verticleName = command['verticle']
    String deploymentId = deployments.get('deployment')

    container.undeployVerticle(deploymentId) {
      // undeployed
      def json = [
        verticle: verticleName,
        deployment: deploymentId,
        status: 'undeployed'
      ]
      publish json
    }
  }

  private void listDeployments(Message msg) {
    def command = msg.body as Map
    def json = [
      deployments: deployments
    ]
    publish json
  }


  /**
   * Query the configured JMX MBeans and return a map.
   * The method runs on a scheduled timer call
   *
   * @param id current timer instance id
   */
  private void collectAndPublish(long id) {
    def names = JMX.queryNames config['beans'] as List
    def beans = JMX.parseToList names
    def json = ['metrics': beans]
    vertx.eventBus.publish config['metrics'] as String, data(json)
  }

  private String uid() {
    def agent = vertx.sharedData.getMap(MANAGEMENT_AGENT_MAP)
    agent.get 'uid'
  }

}
