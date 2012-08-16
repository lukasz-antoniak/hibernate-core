package org.hibernate.service.jta.platform.internal;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.jta.platform.spi.JtaPlatformResolver;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Resolves current JTA platform based on known class lookup.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class ClasspathJtaPlatformResolver implements JtaPlatformResolver {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger( CoreMessageLogger.class, ClasspathJtaPlatformResolver.class.getName() );

	@Override
	public JtaPlatform resolveJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry,
										  Collection<AbstractJtaPlatform> knownPlatforms) {
		final ClassLoaderService classLoaderService = registry.getService( ClassLoaderService.class );
		for ( AbstractJtaPlatform platform : knownPlatforms ) {
			if ( platform.getCharacteristicClassNames() != null ) {
				for ( String className : platform.getCharacteristicClassNames() ) {
					// Treat known class names as alternatives.
					if ( isClassVisible( classLoaderService, className ) ) {
						LOG.debugf( "Guessing JTA platform: %s", platform.getClass().getName() );
						return platform;
					}
				}
			}
		}
		LOG.debugf( "Failed to guess current JTA platform" );
		return null;
	}

	private boolean isClassVisible(final ClassLoaderService classLoaderService, final String className) {
		try {
			classLoaderService.classForName( className );
			return true;
		}
		catch ( ClassLoadingException e ) {
			return false;
		}
	}
}
