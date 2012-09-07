package org.vertx.mods.management.api;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class ManagementRestApiMod extends BusModBase implements Handler<Message<JsonObject>> {

  @Override
  public void start() {
    super.start();

  }

  @Override
  public void stop() throws Exception {

    super.stop();
  }

  @Override
  public void handle(Message<JsonObject> event) {
    
  }

}
