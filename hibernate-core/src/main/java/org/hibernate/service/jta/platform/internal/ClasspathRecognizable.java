package org.hibernate.service.jta.platform.internal;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Interface used to guess current JTA platform based on JAR archives present in the classpath.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface ClasspathRecognizable {
	/**
	 * @return Collection of JAR archive name patterns required to match current runtime environment.
	 */
	public Collection<Pattern> getCharacteristicJarArchivePatterns();
}
