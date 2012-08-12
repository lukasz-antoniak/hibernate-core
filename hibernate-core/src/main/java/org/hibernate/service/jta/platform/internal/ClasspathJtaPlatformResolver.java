package org.hibernate.service.jta.platform.internal;

import java.util.Map;

import org.jboss.logging.Logger;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class ClasspathJtaPlatformResolver extends AbstractJtaPlatformResolver {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger( CoreMessageLogger.class, ClasspathJtaPlatformResolver.class.getName() );

	@Override
	protected JtaPlatform guessJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry) {
		for ( AbstractJtaPlatform platform : registeredPlatforms.values() ) {
			for ( String className : platform.getCharacteristicClassNames() ) {
				if ( isClassVisible( registry, className ) ) {
					LOG.debugf( "Guessing JTA platform %s", platform.getClass().getName() );
					return platform;
				}
			}
		}
		LOG.debugf( "Failed to guess current JTA platform" );
		return null;
	}

	private boolean isClassVisible(final ServiceRegistryImplementor registry, final String className) {
		try {
			registry.getService( ClassLoaderService.class ).classForName( className );
			return true;
		}
		catch ( ClassLoadingException e ) {
			return false;
		}
	}
}
