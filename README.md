# Hazelcast Heroku Discovery

This [Hazelcast](https://hazelcast.org/) Discovery Plugin
provides the possibility to lookup IP addresses of other members by resolving those requests against
the Heroku DNS Discovery in [Heroku Private Spaces](https://devcenter.heroku.com/articles/private-spaces).

## Usage

A minimal `hazelcast.xml` looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.7.xsd"
           xmlns="http://www.hazelcast.com/schema/config"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <properties>
    <property name="hazelcast.discovery.enabled">true</property>
  </properties>
  <network>
    <join>
      <!-- deactivate normal discovery -->
      <multicast enabled="false"/>
      <tcp-ip enabled="false" />

      <!-- activate the Heroku DNS Discovery -->
      <discovery-strategies>
        <discovery-strategy
          enabled="true"
          class="com.github.jkutner.hazelcast.HerokuDiscoveryStrategy">
        </discovery-strategy>
      </discovery-strategies>
    </join>
  </network>
</hazelcast>
```

## Hazelcast Discovery SPI

Hazelcast, since 3.6, offers a, so called, Discovery SPI to integrate external discovery mechanisms into the system. For more information please rely on the official documentation, available [here](http://docs.hazelcast.org/docs/3.6-EA2/manual/html-single/index.html#discovery-spi).

## Configuration

No configuration is required. The plugin will detect the service name from the `$HEROKU_DNS_FORMATION_NAME` environment variable set by Heroku.
