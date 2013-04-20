package io.vertx.mods.management

import groovy.transform.CompileStatic;

import org.vertx.groovy.core.eventbus.Message
import org.vertx.java.core.Future
import org.vertx.groovy.platform.Verticle


@CompileStatic
abstract class SingleInstanceVerticle extends ManagementVerticle {

  String mapName
  boolean started = false

  @Override
  final def start(Future<Void> result) {
    start()

    this.mapName = prestart()

    Map provided = container.getConfig()

    def agent = vertx.sharedData.getMap(mapName)
    def random = new Random().nextLong()
    def uuidStr = new UUID(System.currentTimeMillis(), random).toString()

    agent.putIfAbsent('uid', provided['uid'] ?: uuidStr)
    if (uuidStr.equals(uuid())) { 
      started = true
      startMod(result)
    }
    else {
      result.setFailure(new IllegalStateException("Only 1 instance of '${this.getClass().getSimpleName()}' can be started"))
    }
  }

  abstract protected String prestart() throws Exception

  abstract protected void startMod(Future<Void> result) throws Exception

  @Override
  final def stop() {
    if (started) {
      super.stop()
      stopMod()
      vertx.sharedData.removeMap(mapName)
    }
  }

  abstract protected void stopMod()

  protected final String uuid() {
    def agent = vertx.sharedData.getMap(mapName)
    agent.get 'uid'
  }

}
