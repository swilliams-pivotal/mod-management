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
package org.vertx.management.agent

import groovy.transform.CompileStatic
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.VoidResult;

import com.sun.corba.se.impl.orb.ORBConfiguratorImpl.ConfigParser;


/**
 * @author pidster
 *
 */
@CompileStatic
class ManagementAgent extends Verticle {

  static def BBR_CLASS = BlackBoxRecorder.class.getName()

  long periodicID = 0

  List allowed = ['reregister','checkBean','']

  String uid

  String listener

  Map config = [
    delay: 2000,
    registry: 'vertx.management.registry',
    agents: 'vertx.management.agents',
    metrics: 'vertx.management.metrics',
    beans: ['java.lang:type=Memory', 'java.lang:type=Threading', 'io.vertx.*:type=*']
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

    // fixed at startup time, we don't want to reconfigure these
    this.uid = provided['uid'] ?: UUID.randomUUID().toString()
    this.listener = String.format('%s.%s', config['agents'], this.uid)

    // deploy BlackBoxRecorder if configured
    if (provided['enable-bbr']) {
      def bbr_config = [:]
      bbr_config['address'] = config['metrics']
      // bbr_config['fileDir'] = fileDir

      container.deployVerticle('groovy:' + BBR_CLASS, bbr_config, 1) { id->
        start()
        startedResult.setResult()
      }
    }

    configure()
    register()
  }

  @Override
  def stop() throws Exception {
    deregister()
    deconfigure()
  }

  private Map data(Map json = [:]) {
    if (!json.containsKey('sender')) json['sender'] = this.uid
    if (!json.containsKey('time')) json['time'] = System.currentTimeMillis()
    json
  }

  /**
   * 
   * @param address
   * @param json
   */
  private void status(Map json, String address = config['agents']) {
    vertx.eventBus.publish address, data(json)
  }

  private void configure() {
    vertx.eventBus.registerHandler(this.listener, this.&receiver) { res->
      long period = config['delay'] as long
      this.periodicID = vertx.setPeriodic period, this.&collectAndPublish
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
  }


  private void register() {
    def queries = ['java.lang:type=*','java.lang:type=*,*']
    def names = JMX.queryNames queries
    def json = [
      type: 'register',
      data: JMX.parseToList(names)
    ]
    status json, config['registry'] as String
  }

  private void deregister() {
    def json = [
      type: 'deregister'
    ]
    status json, config['registry'] as String
  }


  private void receiver(Message message) {
    def body = message.body as Map
    commands(body['command'] as String)
        .call(body['args'])
  }

  private Closure commands(String name) {
    def cmd = allowed.contains(name) ? this.&"$name" : { args->
      println "Unknown command ${name} with ${args}" }
  }


  private void checkBean(String name, String address = config['metrics']) {

    def names = JMX.queryNames name
    def beans = JMX.parseToList names
    def json = [:]
    json['data'] = beans
    status json, address
  }


  private void deployVerticle(Map command) {
    String verticleName = command['verticle']
    Map config = command['config'] as Map ?: [:]
    int instances = command['instances'] as int ?: 1
    container.deployVerticle(verticleName, config, instances) { String did->
      deployments.put verticleName, did
      def json = [
        verticle: verticleName,
        status: 'ok'
      ]
      status json
    }
  }

  private void deployWorkerVerticle(Map command) {
    String verticleName = command['verticle']
    Map config = command['config'] as Map ?: [:]
    int instances = command['instances'] as int ?: 1
    container.deployVerticle(verticleName, config, instances) { String did->
      deployments.put verticleName, did
      def json = [
        verticle: verticleName,
        status: 'ok'
      ]
      status json
    }
  }

  private void undeploy(Map command) {
    String deploymentId = deployments.get(command['name'])
    container.undeployVerticle(deploymentId) {
      // undeployed
      def json = [
        command: command['name'],
        status: 'ok'
      ]
      status json
    }
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
    def json = ['data': beans]
    vertx.eventBus.publish config['metrics'] as String, data(json)
  }

}