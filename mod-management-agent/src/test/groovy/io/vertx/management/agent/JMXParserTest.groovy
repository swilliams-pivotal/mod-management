package io.vertx.management.agent

import static org.junit.Assert.*
import io.vertx.management.agent.JMX;

import org.junit.Test

import groovy.json.JsonOutput


class JMXParserTest {
  
  @Test
  public void testCollectRuntime() {
    def beans = JMX.queryNames('java.lang:type=Runtime')
    beans.each { bean->
      assertEquals('java.lang:type=Runtime', bean)
    }

    def list = JMX.parseToList beans
    def beanName = list[0] as Map
    assertEquals(beanName.bean.systemProperties.'vertx.test.magicProperty', 'fooBar')
  }

  @Test
  public void testCollectCompilation() {
    def beans = JMX.queryNames('java.lang:type=Compilation')
    beans.each { bean->
      assertEquals('java.lang:type=Compilation', bean)
    }
  }

  @Test
  public void testCollectOperatingSystem() {
    def beans = JMX.queryNames('java.lang:type=OperatingSystem')
    beans.each { bean->
      assertEquals('java.lang:type=OperatingSystem', bean)
    }
  }

  @Test
  public void testCollectWildcard() {
    def beans = JMX.queryNames('java.lang:type=*')

    assertEquals('''java.lang:type=ClassLoading
java.lang:type=Compilation
java.lang:type=Memory
java.lang:type=OperatingSystem
java.lang:type=Runtime
java.lang:type=Threading''', beans.join('\n'))
  }

  @Test
  public void testCollectAndPrintVarious() {
    def beans = JMX.queryNames('java.lang:type=*', 'java.lang:type=*,*')
    def json = JMX.parseToList(beans)
    println JsonOutput.toJson(json)
  }

}
