package com.github.jkutner.hazelcast;

import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.properties.PropertyTypeConverter;
import com.hazelcast.config.properties.SimplePropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;

import java.net.InetAddress;
import java.util.*;

/**
 * @author Joe Kutner on 8/28/16.
 *         Twitter: @codefinger
 */
public class HerokuDiscoveryStrategy extends AbstractDiscoveryStrategy {
  private static final ILogger LOGGER = Logger.getLogger(HerokuDiscoveryStrategy.class);

  private final Collection<String> serviceNames;

  public HerokuDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
    super(logger, properties);

    String serviceNamesProp = getOrNull(new SimplePropertyDefinition("serviceNames", PropertyTypeConverter.STRING));
    if (serviceNamesProp == null) {
      String formationName = System.getenv("HEROKU_DNS_FORMATION_NAME");
      if (formationName == null) {
        throw new IllegalArgumentException("You must enable Heroku DNS Service Discovery for this Hazelcast plugin to work!");
      } else {
        this.serviceNames = Collections.unmodifiableCollection(Arrays.asList(formationName));
      }
    } else {
      List<String> serviceNamesList = new ArrayList<>();
      for (String serviceName : serviceNamesProp.split(";")) {
        String appName = System.getenv("HEROKU_DNS_APP_NAME");
        if (appName == null) {
          throw new IllegalArgumentException("You must enable Heroku DNS Service Discovery for this Hazelcast plugin to work!");
        } else {
          serviceNamesList.add(serviceName + "." + appName);
        }
      }
      this.serviceNames = Collections.unmodifiableCollection(serviceNamesList);
    }

    String mergeDelay = getOrNull(new SimplePropertyDefinition("mergeDelay", PropertyTypeConverter.STRING));
    System.setProperty("hazelcast.merge.first.run.delay.seconds", mergeDelay == null ? "20" : mergeDelay);

    // TODO parse /etc/heroku/space-topology.json instead,
    // but that should go in a separate library
    System.setProperty("networkaddress.cache.ttl", "20");
    System.setProperty("networkaddress.cache.negative.ttl", "0");
  }

  @Override
  public Iterable<DiscoveryNode> discoverNodes() {
    List<DiscoveryNode> servers = new ArrayList<>();

    for (String serviceName : this.serviceNames) {
      try {
        InetAddress[] hosts = InetAddress.getAllByName(serviceName);

        for (InetAddress host : hosts) {
          Address address = ipToAddress(host.getHostAddress());
          if (LOGGER.isFinestEnabled()) {
            LOGGER.finest("Found node ip-address is: " + address);
          }

          servers.add(new SimpleDiscoveryNode(address));
        }

        if (servers.isEmpty()) {
          LOGGER.warning("Could not find any service for service '" + serviceName + "'");
        }
      } catch (Exception e) {
        if (LOGGER.isFinestEnabled()) {
          LOGGER.warning(e);
        }

        LOGGER.warning("DNS lookup for service '" + serviceName + "' failed");
      }
    }

    return servers;
  }

  @Override
  public void destroy() {
  }

  private Address ipToAddress(String ip) throws Exception {
    return new Address(ip, NetworkConfig.DEFAULT_PORT);
  }
}
