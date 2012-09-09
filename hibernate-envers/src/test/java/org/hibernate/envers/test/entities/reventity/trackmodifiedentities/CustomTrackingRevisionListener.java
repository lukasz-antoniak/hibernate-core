package org.hibernate.envers.test.entities.reventity.trackmodifiedentities;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.synchronization.CollectionChangeEvent;
import org.hibernate.envers.synchronization.EntityChangeEvent;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class CustomTrackingRevisionListener implements EntityTrackingRevisionListener {
    @Override
    public void entityChanged(EntityChangeEvent event) {
        String type = event.getEntityClass().getName();
        ((CustomTrackingRevisionEntity) event.getRevisionEntity()).addModifiedEntityType(type);
    }

    @Override
    public void collectionChanged(CollectionChangeEvent event) {
    }

    @Override
    public void newRevision(Object revisionEntity) {
    }
}
