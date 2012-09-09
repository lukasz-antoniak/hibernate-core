package org.hibernate.envers;

import org.hibernate.envers.synchronization.CollectionChangeEvent;
import org.hibernate.envers.synchronization.EntityChangeEvent;

/**
 * Extension of standard {@link RevisionListener} that notifies whenever an entity instance or persistent collection
 * has been added, modified or removed within current revision boundaries.
 * @see RevisionListener
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public interface EntityTrackingRevisionListener extends RevisionListener {
    /**
     * Called after audited entity data has been persisted.
     * @param event Entity change event.
     */
    void entityChanged(EntityChangeEvent event);

    /**
     * Called after persistent collection has changed.
     * @param event Persistent collection change event.
     */
    void collectionChanged(CollectionChangeEvent event);
}
