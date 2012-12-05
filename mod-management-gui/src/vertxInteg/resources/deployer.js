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

var mongo = {
  "address": "vertx.metrics.data",
  "host": "127.0.0.1",
  "port": 27017,
  "db_name": "metrics_db"
}

var collector = {
  "mongodb": "metrics_db"
}

var config = {
  "mongodb": "metrics_db"
}

vertx.deployModule('vertx.mongo-persistor-v1.2', mongo, 1, function(id1) {
  console.log('deployed vertx.mongo-persistor-v1.2 with id: ' + id1)

  vertx.deployModule('vertx.management-collector-v1.0', collector, 1, function(id2) {
    console.log('deployed vertx.management-collector-v1.0 with id: ' + id2)

    vertx.deployModule('vertx.management-gui-v1.0', config, 1, function(id3) {
      console.log('deployed vertx.management-gui-v1.0 with id: ' + id3)
    });
  });
});

function vertxStop() {
  //
}
