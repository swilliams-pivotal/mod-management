/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

load('vertx.js')

var eb = vertx.eventBus;

var mongoAddress = 'vertx.management.persistor'
var metricsAddress = 'vertx.management.metrics';

var mongo = {
  "address": mongoAddress,
  "host": "127.0.0.1",
  "port": 27000
  "db_name": "metrics"
}

var persistor = function(msg) {
  var payload = {
    "action": "save",
    "collection": msg.type,
    "document": msg.data
  }
  eb.send(metricsAddress, payload)
}

vertx.deployModule('vertx.mongodb-persistor-v1.0', mongo, 1, function() {
  eb.registerHandler(metricsAddress, handler)
});

function vertxStop() {
  eb.unregisterHandler(metricsAddress, handler)
}
