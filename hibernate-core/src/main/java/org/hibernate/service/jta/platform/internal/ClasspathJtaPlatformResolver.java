package org.hibernate.service.jta.platform.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.spi.ServiceRegistryImplementor;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class ClasspathJtaPlatformResolver extends AbstractJtaPlatformResolver {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger( CoreMessageLogger.class, ClasspathJtaPlatformResolver.class.getName() );

	private final List<String> classpathEntries;

	public ClasspathJtaPlatformResolver() {
		final String classpath = System.getProperty( "java.class.path" );
		final String entrySeparator = System.getProperty( "path.separator" );
		final String fileSeparator = System.getProperty( "file.separator" );

		classpathEntries = new LinkedList<String>();
		for ( String entry : classpath.split( entrySeparator ) ) {
			classpathEntries.add( entry.substring( entry.lastIndexOf( fileSeparator ) + 1 ) );
		}
	}

	@Override
	protected JtaPlatform guessJtaPlatform(Map configurationValues, ServiceRegistryImplementor registry) {
		outer: for ( AbstractJtaPlatform platform : registeredPlatforms.values() ) {
			for ( Pattern jarNamePattern : platform.getCharacteristicJarArchivePatterns() ) {
				if ( !matchArchive( jarNamePattern ) ) {
					continue outer;
				}
			}
			System.out.println("LA: " + platform + "\n\n");
			return platform;
//			Allow regular expressions matching because if various JAR versions, example "btm-1.0-beta4.jar".
//			if ( classpathEntries.containsAll( platform.getCharacteristicJarArchivePatterns() ) ) {
//				return platform;
//			}
		}
		return null;
	}

	private boolean matchArchive(final Pattern pattern) {
		for ( String jar : classpathEntries ) {
			if ( pattern.matcher( jar ).matches() ) {
				return true;
			}
		}
		return false;
	}
}
