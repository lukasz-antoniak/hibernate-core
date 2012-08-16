package org.hibernate.service.jta.platform.spi;

import java.util.Collection;
import java.util.Map;

import org.hibernate.service.Service;
import org.hibernate.service.jta.platform.internal.AbstractJtaPlatform;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Contract for determining current JTA environment in runtime.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface JtaPlatformResolver extends Service {
	/**
	 * @param registry Service registry.
	 * @param configurationValues Configuration values.
	 * @param knownPlatforms Collection of known platforms shipped with library bundle.
	 * @return Currently used JTA Platform or {@code null} in case of unsuccessful resolution.
	 */
	public JtaPlatform resolveJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry,
										  Collection<AbstractJtaPlatform> knownPlatforms);
}
