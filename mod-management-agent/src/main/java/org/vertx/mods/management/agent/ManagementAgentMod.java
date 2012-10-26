package org.vertx.mods.management.agent;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class ManagementAgentMod extends BusModBase implements Handler<Message<JsonObject>> {

  private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

  private Set<Long> timers = new HashSet<Long>();

  private String managementAddress;

  private long periodicTimerId;

  private String handlerId;

  private JsonObject base;

  private ManagementAgent agent;

  @Override
  public void start() {
    super.start();

    int period = getOptionalIntConfig("period", 5000);
    String uuid = getOptionalStringConfig("uuid", UUID.randomUUID().toString());

    this.base = new JsonObject()
      .putString("uuid", uuid)
      .putNumber("processors", Runtime.getRuntime().availableProcessors())
      .putString("name", runtimeMXBean.getName());

    this.agent = new ManagementAgent(eb, base);
    this.managementAddress = super.getOptionalStringConfig("address", "vertx.management.agent.control");
    this.periodicTimerId = vertx.setPeriodic(period, agent);
    this.timers.add(periodicTimerId);

    final CountDownLatch latch = new CountDownLatch(1);
    this.handlerId = eb.registerHandler(managementAddress + "." + uuid, this, new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> event) {
        latch.countDown();
      }
    });

    try {
      latch.await(5000L, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      logger.error("", e);
    }

    JsonObject started = base.copy()
      .putNumber("timestamp", System.currentTimeMillis())
      .putString("status", "started");

    eb.publish(managementAddress, started);
  }

  @Override
  public void stop() throws Exception {

    for (long timer : timers) {
      vertx.cancelTimer(timer);
    }

    JsonObject stapped = base.copy()
      .putNumber("timestamp", System.currentTimeMillis())
      .putString("status", "stapped");
    eb.publish(managementAddress, stapped);

    eb.unregisterHandler(handlerId);

    super.stop();
  }

  @Override
  public void handle(Message<JsonObject> event) {
    JsonObject payload = event.body;
    String type = payload.getString("type", "none");

    if ("pause".equalsIgnoreCase(type)) {
      agent.pause(event);
    }
    else if ("resume".equalsIgnoreCase(type)) {
      agent.resume(event);
    }
    else if ("configure".equalsIgnoreCase(type)) {
      configure(event);
    }
    else if ("deployModule".equalsIgnoreCase(type)) {
      deployModule(event);
    }
    else if ("undeployModule".equalsIgnoreCase(type)) {
      undeployModule(event);
    }
    else if ("deployVerticle".equalsIgnoreCase(type)) {
      deployVerticle(event);
    }
    else if ("deployWorkerVerticle".equalsIgnoreCase(type)) {
      deployWorkerVerticle(event);
    }
    else if ("undeployVerticle".equalsIgnoreCase(type)) {
      undeployVerticle(event);
    }
    else {
      event.reply(new JsonObject().putString("status", "command not found"));
    }
  }

  private void configure(Message<JsonObject> event) {
    long period = event.body.getLong("period");
    vertx.cancelTimer(periodicTimerId);
    this.periodicTimerId = vertx.setPeriodic(period, agent);
    event.reply();
  }

  private void deployModule(final Message<JsonObject> event) {
    JsonObject module = event.body.getObject("module");

    String moduleName = module.getString("name");
    JsonObject config = module.getObject("config");
    int instances = module.getInteger("instances");

    container.deployModule(moduleName, config, instances, new Handler<String>() {
      @Override
      public void handle(String id) {
        event.reply();
      }});
  }

  private void undeployModule(final Message<JsonObject> event) {
    JsonObject module = event.body.getObject("module");
    String id = module.getString("id");

    container.undeployModule(id, new Handler<Void>() {
      @Override
      public void handle(Void id) {
        event.reply();
      }});
  }

  private void deployVerticle(final Message<JsonObject> event) {
    JsonObject module = event.body.getObject("module");

    String main = module.getString("main");
    JsonObject config = module.getObject("config");
    int instances = module.getInteger("instances");

    container.deployVerticle(main, config, instances, new Handler<String>() {
      @Override
      public void handle(String id) {
        event.reply();
      }});
  }

  private void deployWorkerVerticle(final Message<JsonObject> event) {
    JsonObject module = event.body.getObject("module");

    String main = module.getString("main");
    JsonObject config = module.getObject("config");
    int instances = module.getInteger("instances");

    container.deployWorkerVerticle(main, config, instances, new Handler<String>() {
      @Override
      public void handle(String id) {
        event.reply();
      }});
  }

  private void undeployVerticle(final Message<JsonObject> event) {
    JsonObject module = event.body.getObject("module");
    String id = module.getString("id");

    container.undeployVerticle(id, new Handler<Void>() {
      @Override
      public void handle(Void id) {
        event.reply();
      }});
  }

}
