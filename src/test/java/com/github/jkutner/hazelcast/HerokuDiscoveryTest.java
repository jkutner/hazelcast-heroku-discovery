package com.github.jkutner.hazelcast;

import com.hazelcast.config.*;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Joe Kutner on 8/28/16.
 *         Twitter: @codefinger
 */
public class HerokuDiscoveryTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(HerokuDiscoveryTest.class);

  @Rule
  public final EnvironmentVariables environmentVariables
      = new EnvironmentVariables();

  @Test
  public void discoveryProviderTest() throws Exception {
    environmentVariables.set("HEROKU_DNS_FORMATION_NAME", "localhost");

    DiscoveryNode local = new SimpleDiscoveryNode(new Address("127.0.0.1", 1010));
    DiscoveryStrategyFactory factory = new HerokuDiscoveryStrategyFactory();
    DiscoveryStrategy provider = factory.newDiscoveryStrategy(local, null, new HashMap<>());

    provider.start();

    Iterable<DiscoveryNode> nodes = provider.discoverNodes();
    Assert.assertNotNull(nodes);
    Assert.assertTrue("Empty DiscoveryNode list", nodes.iterator().hasNext());

    for (DiscoveryNode node : nodes) {
      LOGGER.info("Node -> {}", node.getPublicAddress());
    }
  }

  @Test
  public void hazelcastConfigurationTest() throws Exception {
    Config config = loadConfig("test-hazelcast-discovery-dns.xml");
    DiscoveryConfig discovery = config.getNetworkConfig().getJoin().getDiscoveryConfig();
    Collection<DiscoveryStrategyConfig> discoveryConfs = discovery.getDiscoveryStrategyConfigs();

    Assert.assertFalse("No DiscoveryStrategy configured", discoveryConfs.isEmpty());
    Assert.assertEquals(1, discoveryConfs.size());

    DiscoveryStrategyConfig discoveryConf = discoveryConfs.iterator().next();

    Assert.assertEquals(HerokuDiscoveryStrategy.class.getName(), discoveryConf.getClassName());
  }

  private Config loadConfig(String fileName) throws IOException {

    try (InputStream in = HerokuDiscoveryTest.class.getClassLoader().getResourceAsStream(fileName)) {
      Config config = new XmlConfigBuilder(in).build();

      InterfacesConfig interfaces = config.getNetworkConfig().getInterfaces();
      interfaces.clear();
      interfaces.setEnabled(true);
      interfaces.addInterface("127.0.0.1");

      return config;
    }
  }
}
