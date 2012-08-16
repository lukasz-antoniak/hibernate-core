package org.hibernate.service.jta.platform.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jta.platform.spi.JtaPlatformResolver;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * Instantiates a chain of configured {@link JtaPlatformResolver} implementors.
 * By default only {@link ClasspathJtaPlatformResolver} is used.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class JtaPlatformResolverInitiator implements BasicServiceInitiator<JtaPlatformResolver> {
	public static final JtaPlatformResolverInitiator INSTANCE = new JtaPlatformResolverInitiator();
	private static final JtaPlatformResolver DEFAULT_RESOLVER = new ClasspathJtaPlatformResolver();

	@Override
	public JtaPlatformResolver initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		return new JtaPlatformResolverSet( determineResolvers( configurationValues, registry ) );
	}

	private List<JtaPlatformResolver> determineResolvers(Map configurationValues, ServiceRegistryImplementor registry) {
		final List<JtaPlatformResolver> resolvers = new ArrayList<JtaPlatformResolver>();

		final String resolverImplNames = (String) configurationValues.get( AvailableSettings.JTA_PLATFORM_RESOLVERS );

		if ( StringHelper.isNotEmpty( resolverImplNames ) ) {
			final ClassLoaderService classLoaderService = registry.getService( ClassLoaderService.class );
			for ( String resolverImplName : StringHelper.split( ", \n\r\f\t", resolverImplNames ) ) {
				try {
					resolvers.add( (JtaPlatformResolver) classLoaderService.classForName( resolverImplName ).newInstance() );
				}
				catch ( HibernateException e ) {
					throw e;
				}
				catch ( Exception e ) {
					throw new ServiceException(
							"Unable to instantiate named JTA platform resolver [" + resolverImplName + "]",
							e
					);
				}
			}
		}

		resolvers.add( DEFAULT_RESOLVER );
		return resolvers;
	}

	@Override
	public Class<JtaPlatformResolver> getServiceInitiated() {
		return JtaPlatformResolver.class;
	}
}
