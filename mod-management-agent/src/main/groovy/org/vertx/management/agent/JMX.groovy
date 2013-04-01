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

import java.beans.Introspector
import java.lang.management.ManagementFactory
import javax.management.MBeanAttributeInfo
import javax.management.MBeanServer
import javax.management.ObjectInstance
import javax.management.ObjectName
import javax.management.RuntimeMBeanException
import javax.management.openmbean.CompositeData
import javax.management.openmbean.CompositeDataSupport
import javax.management.openmbean.TabularDataSupport
import groovy.transform.CompileStatic


//@CompileStatic
class JMX {

  static MBeanServer server = ManagementFactory.platformMBeanServer

  static List queryNames(String... queries) {
    queryNames Arrays.asList(queries)
  }

  static List queryNames(List<String> queries) {
    def all = []
    for (String q : queries) {
      try {
        def name = new ObjectName(q)
        def names = server.queryNames(name, null)
        all.addAll names.collect { ObjectName on-> on.serializedNameString }
      }
      catch (Throwable t) {
        println "query: ${q} ${t.message}"
        t.printStackTrace()
      }
    }
    all
  }

  static List parseToList(beanNames) {
    def res = []
    for (String beanName : beanNames) {
      def map = [:]
      def objname = new ObjectName(beanName)
  
      if (server.isRegistered(objname)) {
        def mbeanInfo = server.getMBeanInfo(objname)
        def MBeanAttributeInfo[] attr = mbeanInfo.attributes
  
        for (MBeanAttributeInfo mai : attr) {
          def attrName = Introspector.decapitalize(mai.name)
          def attrValue = attribute(objname, mai.name)
          if ('objectName' != attrName) map.put attrName, attrValue
        }
      }
  
      res << [name:beanName, data:map]
    }
    res
  }

  static def attribute(ObjectName objname, String name) {

    def value
    try {
      value = server.getAttribute(objname, name)
      if ('ObjectName' == name) ((ObjectName) value).serializedNameString
      else mapTypes(value)
    }
    catch (RuntimeMBeanException e) {
      'unsupported'
    }
    catch (Exception e) {
      e.message
    }
  }

  private static Object mapTypes(value) {
    if (value instanceof CompositeData)
      compositeData value
    else if (value instanceof CompositeDataSupport)
      compositeDataSupport value
    else if (value instanceof TabularDataSupport)
      tabularDataSupport value
    else if (value?.class?.isArray())
      value
    else value
  }

  private static Map compositeData(type) {
    def data = (CompositeData) type
    def map = [:]
    data.compositeType.myNamesSet.each { String key->
      map.put key, mapTypes(data.get(key))
    }
    map
  }

  private static Map compositeDataSupport(type) {
    ((CompositeDataSupport) type).contents
  }

  private static Map tabularDataSupport(type) {
    def map = [:]
    def tds = (TabularDataSupport) type
    def keySet = (Set<Object[]>) tds.keySet()
    for (Object[] key :  keySet) {
      def cds = tds.get(key) as CompositeDataSupport
      map.put cds['key'], mapTypes(cds['value'])
    }
    map
  }

}
