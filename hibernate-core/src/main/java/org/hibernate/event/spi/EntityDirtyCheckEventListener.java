package org.hibernate.event.spi;

import java.io.Serializable;

import org.hibernate.HibernateException;

/**
 * Defines the contract for handling of entity-dirty-check events.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface EntityDirtyCheckEventListener extends Serializable {
	/**
	 * Handle the given entity-dirty-check event.
	 *
	 * @param event The entity-dirty-check event to be handled.
	 * @throws HibernateException
	 */
	public void onEntityDirtyCheck(EntityDirtyCheckEvent event) throws HibernateException;
}