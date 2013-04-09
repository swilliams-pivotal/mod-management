# mod-management-agent

The Management Agent module publishes JMX data for its local JVM, acts as a proxy for JMX operations and can deploy or undeploy verticles or modules in it's own node.

The Agent module can reconfigure itself to some extent at runtime.

At deployment time the Agent module registers two Event Bus handlers.  One address common to all nodes and a node specific address, so it can receive broadcast messages and point-to-point instructions.








