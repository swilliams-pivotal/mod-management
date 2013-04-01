package org.vertx.management.data

import groovy.transform.CompileStatic
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.file.AsyncFile
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.VoidResult;

/**
 * Deployment configuration is (in JavaScript), as follows:
 * 
 * <pre>
 * {@code
 *   var config = {
 *     fileName: '',  // Required
 *     perms: 'rw-r--r--', // Optional (default)
 *     address: 'vertx.management.log', // Optional (default)
 *     format: '%s', // Optional (default)
 *   }
 * }
 * </pre>
 *
 * Runtime usage is as follows:
 * 
 * <pre>
 * {@code
 *   var json = {
 *     args: ['some log message'], // Required
 *     format: '%s' // Optional (default)
 *   }
 *   eventBus.send(address, json)
 * }
 * </pre>
 *
 * @author pidster
 *
 */
@CompileStatic
class LogSink extends Verticle {

  String address = 'vertx.management.log'

  String defaultFormat = '%s'

  AsyncFile file


  @Override
  def start(VoidResult startedResult) throws Exception {

    String fileName = container.config['fileName']
    String perms = container.config['perms'] ?: 'rw-r--r--'

    assert fileName != null

    if (container.config.containsKey('address')) {
      this.address = container.config['address']
    }

    if (container.config.containsKey('format')) {
      this.defaultFormat = container.config['format']
    }

    vertx.fileSystem.open(fileName, perms, false, true, true) { AsyncResult ar->
      if (ar.succeeded()) {
        assert ar.result instanceof AsyncFile

        this.file = ar.result as AsyncFile
        // FIXME local causes error: vertx.eventBus.registerLocalHandler(address, this.&receiver) {
        vertx.eventBus.registerHandler(address, this.&receiver) {
          startedResult.setResult()
        }
      }
      else {
        startedResult.setFailure(new IOException("'${fileName}' could not be opened"))
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
    Map body = msg.body as Map

    def args = body['args'] as List
    String format = body['format'] ?: defaultFormat

    def buffer = new Buffer()
      .appendString(String.format(format, args.toArray()))

    file?.writeStream.writeBuffer(buffer)
  }

}
