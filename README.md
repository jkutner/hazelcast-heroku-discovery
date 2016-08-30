# Hazelcast Heroku Discovery [![Build Status](https://travis-ci.org/jkutner/hazelcast-heroku-discovery.svg?branch=master)](https://travis-ci.org/jkutner/hazelcast-heroku-discovery)

This [Hazelcast](https://hazelcast.org/) Discovery Plugin
will lookup IP addresses of other members by resolving service names against
the Heroku DNS Discovery in [Heroku Private Spaces](https://devcenter.heroku.com/articles/private-spaces).

## Usage

In Maven:

```xml
<dependency>
  <groupId>com.github.jkutner</groupId>
  <artifactId>hazelcast-heroku-discovery</artifactId>
  <version>3.7.0</version>
</dependency>
```

In Gradle:

```groovy
dependencies {
    compile('com.github.jkutner:hazelcast-heroku-discovery:3.7.0')
}
```

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

Hazelcast, since 3.6, offers a, so called, Discovery SPI to integrate external discovery mechanisms into the system.
For more information please rely on the official documentation, available [here](http://docs.hazelcast.org/docs/3.6-EA2/manual/html-single/index.html#discovery-spi).

## Configuration

No configuration is required by default. The plugin will detect the service name from the `$HEROKU_DNS_FORMATION_NAME` environment variable set by Heroku.

If you want to configure the `hazelcast.merge.first.run.delay.seconds`, set the following in your `hazelcast.xml`:

```xml
<discovery-strategy
  enabled="true"
  class="com.github.jkutner.hazelcast.HerokuDiscoveryStrategy">
    <properties>
      <property name="mergeDelay">30</property>
    </properties>
</discovery-strategy>
```

If you want to configure the service names (i.e. the process types) that will be discovered, you can set a `;`
delimited list like this:

```xml
<discovery-strategy
  enabled="true"
  class="com.github.jkutner.hazelcast.HerokuDiscoveryStrategy">
    <properties>
      <property name="serviceNames">web;worker;job</property>
    </properties>
</discovery-strategy>
```

## License

MIT
