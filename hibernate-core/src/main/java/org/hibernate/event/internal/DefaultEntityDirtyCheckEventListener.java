package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.event.spi.EntityDirtyCheckEvent;
import org.hibernate.event.spi.EntityDirtyCheckEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * Defines the default entity-dirty-check event listener used by Hibernate for checking dirtiness of entities
 * in response to generated entity-dirty-check events.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class DefaultEntityDirtyCheckEventListener extends AbstractFlushingEventListener
		implements EntityDirtyCheckEventListener {
	/**
	 * Handle the given entity-dirty-check event.
	 *
	 * @param event The entity-dirty-check event to be handled.
	 * @throws HibernateException
	 */
	@Override
	public void onEntityDirtyCheck(EntityDirtyCheckEvent event) throws HibernateException {
		int oldSize = event.getSession().getActionQueue().numberOfCollectionRemovals();
		try {
			EventSource source = event.getSession();
			final Object entity = event.getEntity();
			boolean dirty = false;

			flushEverythingToExecutions( event );

			if ( entity instanceof HibernateProxy ) {
				LazyInitializer li = ( (HibernateProxy) entity ).getHibernateLazyInitializer();
				dirty = source.getActionQueue()
						.hasAnyQueuedActionsForEntity( li.getEntityName(), li.getIdentifier() );
			}
			else {
				EntityEntry entityEntry = source.getPersistenceContext().getEntry( entity );
				// If entityEntry is null, the entity has not been persisted
				dirty = entityEntry != null && source.getActionQueue()
						.hasAnyQueuedActionsForEntity( entityEntry.getEntityName(), entityEntry.getId() );
			}
			event.setDirty( dirty );
		}
		finally {
			event.getSession().getActionQueue().clearFromFlushNeededCheck( oldSize );
		}
	}
}
