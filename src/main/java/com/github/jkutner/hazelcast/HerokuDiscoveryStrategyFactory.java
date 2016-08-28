package com.github.jkutner.hazelcast;

import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import java.util.*;

/**
 * @author Joe Kutner on 8/28/16.
 *         Twitter: @codefinger
 */
public class HerokuDiscoveryStrategyFactory implements DiscoveryStrategyFactory {
  private final Collection<PropertyDefinition> propertyDefinitions;

  public HerokuDiscoveryStrategyFactory() {
    this.propertyDefinitions = Collections.unmodifiableCollection(
        new ArrayList<>()
    );
  }

  @Override
  public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
    return HerokuDiscoveryStrategy.class;
  }

  @Override
  public DiscoveryStrategy newDiscoveryStrategy(
      DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
    return new HerokuDiscoveryStrategy(logger, properties);
  }

  @Override
  public Collection<PropertyDefinition> getConfigurationProperties() {
    return propertyDefinitions;
  }
}
