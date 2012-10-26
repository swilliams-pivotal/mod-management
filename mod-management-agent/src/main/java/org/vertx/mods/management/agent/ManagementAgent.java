package org.vertx.mods.management.agent;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;


public class ManagementAgent implements Handler<Long> {

  private static final String VERTX_METRICS_ADDRESS = "vertx.management.metrics";

  private RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

  private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

//  private List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();

  private List<GarbageCollectorMXBean> collectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

  private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

  private boolean paused = false;

  private JsonObject base;

  private EventBus eventBus;

  public ManagementAgent(EventBus eventBus, JsonObject base) {
    this.eventBus = eventBus;
    this.base = base;
  }

  @Override
  public void handle(Long event) {
    sample();
  }

  public void pause(Message<JsonObject> event) {
    this.paused = true;
    event.reply();
  }

  public void resume(Message<JsonObject> event) {
    this.paused = false;
    event.reply();
  }

  public boolean isPaused() {
    return paused;
  }

  private void sample() {
    if (paused) {
      return;
    }

    JsonObject data = new JsonObject();

    JsonObject threads = new JsonObject();
    threads.putNumber("daemonThreadCount", threadMXBean.getDaemonThreadCount());
    threads.putNumber("threadCount", threadMXBean.getThreadCount());
    threads.putNumber("peakThreadCount", threadMXBean.getPeakThreadCount());
    threads.putNumber("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());
    data.putObject("threads", threads);

    JsonObject memory = new JsonObject();
    memory.putNumber("free", Runtime.getRuntime().freeMemory());
    memory.putNumber("max", Runtime.getRuntime().maxMemory());
    memory.putNumber("total", Runtime.getRuntime().totalMemory());
    memory.putObject("heap", usageJson(memoryMXBean.getHeapMemoryUsage()));
    memory.putObject("nonHeap", usageJson(memoryMXBean.getNonHeapMemoryUsage()));
    data.putObject("memory", memory);

    JsonObject gc = new JsonObject();
    for (GarbageCollectorMXBean gcMXBean : collectorMXBeans) {
      gc.putString("name", gcMXBean.getName());
      gc.putNumber("count", gcMXBean.getCollectionCount());
      gc.putNumber("time", gcMXBean.getCollectionTime());
    }
    data.putObject("gc", gc);

    JsonObject sample = base.copy()
      .putNumber("timestamp", System.currentTimeMillis())
      .putNumber("uptime", runtimeMXBean.getUptime())
      .putString("status", "sample")
      .putObject("data", data);

    eventBus.publish(VERTX_METRICS_ADDRESS, sample);
  }

  private JsonObject usageJson(MemoryUsage memoryUsage) {
    JsonObject json = new JsonObject();

    json.putNumber("init", memoryUsage.getInit());
    json.putNumber("used", memoryUsage.getUsed());
    json.putNumber("committed", memoryUsage.getCommitted());
    json.putNumber("max", memoryUsage.getMax());

    return json;
  }

}
