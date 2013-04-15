# mod-management

A collection of modules providing management capabilities for vert.x

## Sub-projects

The management sub-projects can be used independently.  The expected normal use would be to have at least the Agent deployed in each application node.  Some sub-modules require other of the management modules to be deployed in the system.

### mod-management-agent

The Agent module polls the local JMX MBeanServer periodically (the period is configurable), converts the resulting data into JSON and publishes it to the Event Bus.  The agent also listens on two addresses for instructions that might include reconfiguration or an action, for example to deploy another verticle.

Included with the Agent module is a BlackBoxRecorder verticle that performs a similar operation, but instead of publishing to the Event Bus writes the JSON payload to disk.  NB The BBR is intended to be used for debugging instance behaviour and not for production monitoring.

See the module's README for the full deployment and runtime configuration options.

### mod-management-monitor

The Monitor module is responsible for observing a vert.x system and publishing meta-events, for example when a new member joins or leaves an existing system, when some monitored metric reaches a pre-determined value, or when a sequence of events is observed to meet a defined criteria.

### mod-management-logsink

The LogSink module listens to the Event Bus for data and writes it to a file.  This provides the opportunity to perform asynchronous logging via the Event Bus.  An application should configure a logger that publishes to the Event Bus rather than synchronously writing to disk.



