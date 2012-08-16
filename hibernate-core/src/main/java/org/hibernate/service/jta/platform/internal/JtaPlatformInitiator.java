/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.service.jta.platform.internal;

import java.util.LinkedHashMap;
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
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * Standard initiator for the standard {@link org.hibernate.service.jta.platform.spi.JtaPlatform}
 *
 * @author Steve Ebersole
 */
public class JtaPlatformInitiator implements BasicServiceInitiator<JtaPlatform> {
	public static final JtaPlatformInitiator INSTANCE = new JtaPlatformInitiator();

	private static final CoreMessageLogger LOG = Logger.getMessageLogger(CoreMessageLogger.class, JtaPlatformInitiator.class.getName());

	// Legacy transaction lookup managers map to JTA platform.
	protected final Map<String, AbstractJtaPlatform> registeredPlatforms;

	public JtaPlatformInitiator() {
		registeredPlatforms = new LinkedHashMap<String, AbstractJtaPlatform>();
		registeredPlatforms.put( "org.hibernate.transaction.BESTransactionManagerLookup", new BorlandEnterpriseServerJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.BTMTransactionManagerLookup", new BitronixJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.JBossTransactionManagerLookup", new JBossAppServerJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.JBossTSStandaloneTransactionManagerLookup", new JBossStandAloneJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.JOnASTransactionManagerLookup", new JOnASJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.JOTMTransactionManagerLookup", new JOTMJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.JRun4TransactionManagerLookup", new JRun4JtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.OC4JTransactionManagerLookup", new OC4JJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.OrionTransactionManagerLookup", new OrionJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.ResinTransactionManagerLookup", new ResinJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.SunONETransactionManagerLookup", new SunOneJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.WeblogicTransactionManagerLookup", new WeblogicJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.WebSphereTransactionManagerLookup", new WebSphereJtaPlatform() );
		registeredPlatforms.put( "org.hibernate.transaction.WebSphereExtendedJTATransactionLookup", new WebSphereExtendedJtaPlatform() );
	}

	@Override
	public Class<JtaPlatform> getServiceInitiated() {
		return JtaPlatform.class;
	}

	@Override
	@SuppressWarnings( {"unchecked"})
	public JtaPlatform initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
		// Retrieving configured JTA platform.
		Object platform = getConfiguredPlatform( configurationValues, registry );
		if ( platform != null ) {
			return registry.getService( ConfigurationService.class ).cast( JtaPlatform.class, platform );
		}

		// Trying to guess if not configured.
		JtaPlatform guessedPlatform = registry.getService( JtaPlatformResolver.class )
				.resolveJtaPlatform( configurationValues, registry, registeredPlatforms.values() );
		if ( guessedPlatform != null ) {
			return guessedPlatform;
		}

		return new NoJtaPlatform();
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
}
