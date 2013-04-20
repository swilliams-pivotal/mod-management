/*
 * Copyright 2013 the original author or authors.
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
package io.vertx.management.api

import groovy.transform.CompileStatic

import groovy.json.JsonOutput
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.Future


@CompileStatic
class ManagementAPI extends Verticle {

  HttpServer server

  @Override
  def start(Future<Void> result) {

    int port = container['port'] as int ?: 8081
    String host = container['host'] ?: '0.0.0.0'

    def rm = new RouteMatcher()
    rm.get '/nodes/', this.&nodes
    rm.get '/nodes/status', this.&nodeStatus
    rm.get '/nodes/:group', this.&nodeGroup
    rm.get '/nodes/:group/:id', this.&nodeGroupId
    rm.get '/nodes/:group/:id/status', this.&nodeGroupIdStatus
    rm.get '/nodes/:group/:id/memory', this.&nodeGroupIdMemory

    this.server = vertx.createHttpServer()
        .requestHandler(rm.asClosure())
        .listen(port, host) {
          result.setResult(null)
        }
  }

  @Override
  def stop() {
    server?.close()
  }

  private void nodes(HttpServerRequest req) {
    def map = [:]
    req.response.end JsonOutput.toJson(map)
  }

  private void nodeStatus(HttpServerRequest req) {
    def map = [:]
    req.response.end JsonOutput.toJson(map)
  }

  private void nodeGroup(HttpServerRequest req) {
    def map = [:]
    req.response.end JsonOutput.toJson(map)
  }

  private void nodeGroupId(HttpServerRequest req) {
    def map = [:]
    req.response.end JsonOutput.toJson(map)
  }

  private void nodeGroupIdStatus(HttpServerRequest req) {
    def map = [:]
    req.response.end JsonOutput.toJson(map)
  }

  private void nodeGroupIdMemory(HttpServerRequest req) {
    def map = [:]
    req.response.end JsonOutput.toJson(map)
  }

}
