package org.hibernate.service.jta.platform.spi;

import java.util.Map;

import org.hibernate.service.Service;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface JtaPlatformResolver extends Service {
	public JtaPlatform resolveJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry);
}
