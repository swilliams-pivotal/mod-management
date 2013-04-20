package io.vertx.management.data

import static org.vertx.management.Constants.*

import groovy.transform.CompileStatic

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Future


@CompileStatic
class MongoDataBridge extends Verticle {

  private static String MONGO_MODULE = 'io.vertx~mod-mongo-persistor~2.0.0-SNAPSHOT'
  private static String MONGO_ADDRESS = 'vertx.management.mongo'
  private static String METRICS_COLLECTION = 'vertx.agent.metrics'
  private static String STATUS_COLLECTION = 'vertx.agent.status'

  private String metricsAddress
  private String metricsCollection

  private String statusAddress
  private String statusCollection

  @Override
  def start(Future<Void> future) {

    this.statusAddress = container.config['status'] ?: STATUS_ADDRESS
    this.metricsAddress = container.config['metrics'] ?: METRICS_ADDRESS

    Map<String, Object> mongoConfig = [:]
    mongoConfig['address'] = MONGO_ADDRESS
    mongoConfig['host'] = 'localhost'
    mongoConfig['port'] = 27000
    mongoConfig['db_name'] = 'vertx'
    mongoConfig['fake'] = false
    mongoConfig['statusCollection'] = STATUS_COLLECTION
    mongoConfig['metricsCollection'] = METRICS_COLLECTION

    if (container.config['mongo'])
      mongoConfig.putAll container.config['mongo'] as Map

    this.statusCollection = mongoConfig['statusCollection'] ?: STATUS_COLLECTION
    this.metricsCollection = mongoConfig['metricsCollection'] ?: METRICS_COLLECTION
    int instances = mongoConfig['instances'] as int ?: 2

    container.deployModule(MONGO_MODULE, mongoConfig, instances) { did->
      vertx.eventBus.registerHandler(statusAddress, this.&statusMetrics) {
        vertx.eventBus.registerHandler(metricsAddress, this.&receiveMetrics) {
          start()
          future.setResult(null)
        }
      }
    }
  }

  @Override
  def stop() {
    vertx.eventBus.unregisterHandler(metricsAddress, this.&receiveMetrics) {
      vertx.eventBus.unregisterHandler(statusAddress, this.&receiveStatus) {
        // container.undeployModule(deploymentId)
      }
    }
  }


  private void receiveStatus(Message msg) {
    def body = msg.body as Map
    def json = [
      action: 'save',
      collection: statusCollection,
      document: body
    ]
    vertx.eventBus.send(MONGO_ADDRESS, json) { reply->
      msg.reply(reply)
    }
  }

  private void receiveMetrics(Message msg) {
    def body = msg.body as Map
    def json = [
      action: 'save',
      collection: metricsCollection,
      document: body
    ]
    vertx.eventBus.send(MONGO_ADDRESS, json) { reply->
      msg.reply(reply)
    }
  }

}
