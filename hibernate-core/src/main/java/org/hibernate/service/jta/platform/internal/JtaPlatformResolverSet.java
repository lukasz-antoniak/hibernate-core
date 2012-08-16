package org.hibernate.service.jta.platform.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.jta.platform.spi.JtaPlatformResolver;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Tries to guess current JTA platform using provided list of {@link JtaPlatformResolver} implementors.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class JtaPlatformResolverSet implements JtaPlatformResolver {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger( CoreMessageLogger.class, JtaPlatformResolverSet.class.getName() );

	private List<JtaPlatformResolver> resolvers;

	public JtaPlatformResolverSet() {
		this( new ArrayList<JtaPlatformResolver>() );
	}

	public JtaPlatformResolverSet(List<JtaPlatformResolver> resolvers) {
		this.resolvers = resolvers;
	}

	public JtaPlatformResolverSet(JtaPlatformResolver... resolvers) {
		this( Arrays.asList( resolvers ) );
	}

	@Override
	public JtaPlatform resolveJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry, Collection<AbstractJtaPlatform> knownPlatforms) {
		for ( JtaPlatformResolver resolver : resolvers ) {
			try {
				JtaPlatform platform = resolver.resolveJtaPlatform( configurationValues, registry, knownPlatforms );
				if ( platform != null ) {
					return platform;
				}
			}
			catch ( Exception e ) {
				LOG.exceptionInSubResolver( e.getMessage() );
			}
		}
		return null;
	}

	/**
	 * Add a resolver at the end of the underlying resolver list.  The resolver added by this method is at lower
	 * priority than any other existing resolvers.
	 *
	 * @param resolver The resolver to add.
	 */
	public void addResolver(JtaPlatformResolver resolver) {
		resolvers.add( resolver );
	}

	/**
	 * Add a resolver at the beginning of the underlying resolver list.  The resolver added by this method is at higher
	 * priority than any other existing resolvers.
	 *
	 * @param resolver The resolver to add.
	 */
	public void addResolverAtFirst(JtaPlatformResolver resolver) {
		resolvers.add( 0, resolver );
	}
}
