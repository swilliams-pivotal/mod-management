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

import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.eventbus.EventBus
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.file.AsyncFile
import org.vertx.groovy.core.file.FileSystem
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.VoidResult;


/**
 * 
 * @author pidster
 *
 */
@CompileStatic
class BlackBoxRecorder extends Verticle {

  String address = 'vertx.management.metrics'

  AsyncFile file


  @Override
  def start(VoidResult startedResult) throws Exception {

    String perms = container.config['perms'] ?: 'rw-------'

    if (container.config.containsKey('address')) {
      this.address = container.config['address']
    }

    String fileDir = container.config['fileDir'] ?: System.getProperty('user.dir')
    assert fileDir != null

    def fileFormat = '%1$s/blackbox-%2$tY%2$tm%2$td-%2$tH%2$tM.json'
    def fileName = String.format(fileFormat, fileDir, new Date())

    vertx.fileSystem.open(fileName, perms, true, true, true, true) { AsyncResult ar->
      if (ar.succeeded()) {
        println "BlackBoxRecorder configured at: '${fileName}'"
        assert ar.result instanceof AsyncFile

        this.file = ar.result as AsyncFile
        /* 
         * FIXME this should only be local: vertx.eventBus.registerLocalHandler(address, this.&receiver) {
         * but it throws:
         * 
         * groovy.lang.MissingMethodException: No signature of method: 
         * org.vertx.java.core.eventbus.impl.DefaultEventBus.registerLocalHandler() 
         * is applicable for argument types: (java.lang.String, $Proxy7, $Proxy8) 
         * values: [vertx.management.metrics, org.vertx.groovy.core.eventbus.EventBus$_wrapHandler_closure1@45fa3e3e, ...] 
         * Possible solutions: registerLocalHandler(java.lang.String, org.vertx.java.core.Handler)
         */
        vertx.eventBus.registerHandler(address, this.&receiver) {
          println "BlackBoxRecorder listening for data on: '${address}'"
          startedResult.setResult()
        }
      }
      else {
        startedResult.setFailure(new IOException("BlackBoxRecorder '${fileName}' could not be opened"))
      }
    }
  }

  @Override
  def stop() throws Exception {
    vertx.eventBus.unregisterHandler(address, this.&receiver) {
      file?.close()
    }
  }

  private void receiver(Message msg) {
    def json = JsonOutput.toJson(msg.body as Map)
    Buffer buffer = new Buffer(json)
    file?.writeStream.writeBuffer(buffer)
  }

}
