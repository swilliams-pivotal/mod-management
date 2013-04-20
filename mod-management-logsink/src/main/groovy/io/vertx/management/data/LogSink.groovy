package io.vertx.management.data

import static org.vertx.management.Constants.*
import groovy.transform.CompileStatic
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.eventbus.Message
import org.vertx.groovy.core.file.AsyncFile
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.AsyncResult
import org.vertx.java.core.Future

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

  String fileName
  String perms
  String address = LOG_ADDRESS

  String defaultFormat = '%s'

  AsyncFile file


  @Override
  def start(Future<Void> result) {

    this.fileName = container.config['fileName']
    this.perms = container.config['perms'] ?: 'rw-r--r--'

    assert fileName != null

    if (container.config.containsKey('address')) {
      this.address = container.config['address']
    }
    if (container.config.containsKey('format')) {
      this.defaultFormat = container.config['format']
    }

    configure(result)
  }

  @Override
  def stop() {
    file?.flush()
    vertx.eventBus.unregisterHandler(address, this.&receiver) {
      file?.close()
    }
  }

  private void configure(Future<Void> result) {
    vertx.fileSystem.open(fileName, perms, false, true, true) { AsyncResult ar->
      if (ar.succeeded()) {
        assert ar.result() instanceof AsyncFile

        this.file = ar.result() as AsyncFile

        vertx.eventBus.registerLocalHandler(address, this.&receiver)
        result.setResult(null)
      }
      else {
        result.setFailure(new IOException("'${fileName}' could not be opened"))
      }
    }
  }

  def receiver(Message msg) {
    Map body = msg.body as Map

    def args = body['args'] as List
    String format = body['format'] ?: defaultFormat

    def buffer = new Buffer()
      .appendString(String.format(format, args.toArray()))

    file?.write(buffer)
  }

}
