package org.hibernate.service.jta.platform.internal;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.jndi.JndiHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;
import org.hibernate.service.jta.platform.spi.JtaPlatformResolver;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public abstract class AbstractJtaPlatformResolver implements JtaPlatformResolver {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(CoreMessageLogger.class, AbstractJtaPlatformResolver.class.getName() );

	protected final Map<String, AbstractJtaPlatform> registeredPlatforms;

	protected AbstractJtaPlatformResolver() {
		registeredPlatforms = new HashMap<String, AbstractJtaPlatform>();
		registeredPlatforms.put( "org.hibernate.transaction.BTMTransactionManagerLookup", new BitronixJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.WeblogicTransactionManagerLookup", new WeblogicJtaPlatform() );
	}

	@Override
	public JtaPlatform resolveJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry) {
		Object platform = getConfiguredPlatform( configurationValues, registry );
		if (platform != null) {
			return registry.getService( ConfigurationService.class ).cast( JtaPlatform.class, platform );
		}
		return guessJtaPlatform( configurationValues, registry );
	}

	private Object getConfiguredPlatform(Map configurationValues, ServiceRegistryImplementor registry) {
		Object platform = configurationValues.get( AvailableSettings.JTA_PLATFORM );
		if ( platform == null ) {
			final String transactionManagerLookupImplName = (String) configurationValues.get( Environment.TRANSACTION_MANAGER_STRATEGY );
			if ( transactionManagerLookupImplName != null ) {
				LOG.deprecatedTransactionManagerStrategy(TransactionManagerLookup.class.getName(),
						Environment.TRANSACTION_MANAGER_STRATEGY,
						JtaPlatform.class.getName(),
						AvailableSettings.JTA_PLATFORM);
				platform = mapLegacyClasses( transactionManagerLookupImplName, configurationValues, registry );
				LOG.debugf("Mapped legacy class %s -> %s", transactionManagerLookupImplName, platform);
			}
		}
		return platform;
	}

	private JtaPlatform mapLegacyClasses(String tmlImplName, Map configurationValues, ServiceRegistryImplementor registry) {
		if ( registeredPlatforms.containsKey( tmlImplName ) ) {
			return registeredPlatforms.get( tmlImplName );
		}

		try {
			TransactionManagerLookup lookup = (TransactionManagerLookup) registry.getService( ClassLoaderService.class )
					.classForName( tmlImplName )
					.newInstance();
			return new TransactionManagerLookupBridge( lookup, JndiHelper.extractJndiProperties( configurationValues ) );
		}
		catch ( Exception e ) {
			throw new JtaPlatformException(
					"Unable to build " + TransactionManagerLookupBridge.class.getName() + " from specified " +
							TransactionManagerLookup.class.getName() + " implementation: " + tmlImplName
			);
		}
	}

	protected abstract JtaPlatform guessJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry);
}
