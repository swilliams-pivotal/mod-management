package io.vertx.mods.management

import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.platform.Verticle

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory


@CompileStatic
abstract class ManagementVerticle extends Verticle {

  @Override
  def start() {
    super.start()
  }

  @Override
  def stop() {
    super.stop()
  }

}
