package org.hibernate.service.jta.platform.internal;

import java.util.Collection;

/**
 * Interface used to distinguish runtime environment based on the known class lookup.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface ClasspathRecognizable {
	/**
	 * @return List of characteristic fully qualified class names.
	 */
	public Collection<String> getCharacteristicClassNames();
}
