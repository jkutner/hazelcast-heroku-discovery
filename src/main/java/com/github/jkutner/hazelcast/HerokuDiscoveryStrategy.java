package com.github.jkutner.hazelcast;

import com.hazelcast.config.NetworkConfig;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;

import java.util.*;

import java.net.InetAddress;

/**
 * @author Joe Kutner on 8/28/16.
 *         Twitter: @codefinger
 */
public class HerokuDiscoveryStrategy extends AbstractDiscoveryStrategy {
  private static final ILogger LOGGER = Logger.getLogger(HerokuDiscoveryStrategy.class);

  private final String serviceName;

  public HerokuDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
    super(logger, properties);
    this.serviceName = System.getenv("HEROKU_DNS_FORMATION_NAME");

    String mergeDelay = System.getProperty("heroku.hazelcast.merge.first.run.delay.seconds");
    System.setProperty("hazelcast.merge.first.run.delay.seconds", mergeDelay == null ? "20" : mergeDelay);

    // TODO parse /etc/heroku/space-topology.json instead,
    // but that should go in a separate library
    System.setProperty("networkaddress.cache.ttl", "20");
    System.setProperty("networkaddress.cache.negative.ttl", "0");
  }

  @Override
  public Iterable<DiscoveryNode> discoverNodes() {
    List<DiscoveryNode> servers = new ArrayList<>();

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
        LOGGER.warning("Could not find serviceName '" + serviceName + "'");
      }
    } catch (Exception e) {
      if (LOGGER.isFinestEnabled()) {
        LOGGER.warning(e);
      }

      LOGGER.warning("DNS lookup for serviceDns '" + serviceName + "' failed");
      return Collections.emptyList();
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
