package org.vertx.mods.management.api;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

public class ManagementRestApiMod extends BusModBase implements Handler<Message<JsonObject>> {

  @Override
  public void start() {

    super.start();

    eb.registerHandler("vertx.management.api.control", this);

    int port = getMandatoryIntConfig("port");
    String host = getOptionalStringConfig("host", "localhost");

    RouteMatcher router = new RouteMatcher();
    router.getWithRegEx("/management/nodes", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest event) {
        // ask for all nodes
        event.response.end();
      }
    });

    router.getWithRegEx("/management/nodes/:node", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest event) {
        // ask for named node
        String node = event.params().get("node");
        event.response.end(node);
      }
    });

    router.getWithRegEx("/management/stats", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest event) {
        // ask for list of stats
        event.response.end();
      }
    });

    router.getWithRegEx("/management/nodes/:node/stats", new Handler<HttpServerRequest>() {
      @Override
      public void handle(HttpServerRequest event) {
        // ask for stats from named node
        String node = event.params().get("node");
        event.response.end(node);
      }
    });

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, host);
  }

  @Override
  public void stop() throws Exception {
    super.stop();
  }

  @Override
  public void handle(Message<JsonObject> event) {
    event.reply(event.body);
  }

}
