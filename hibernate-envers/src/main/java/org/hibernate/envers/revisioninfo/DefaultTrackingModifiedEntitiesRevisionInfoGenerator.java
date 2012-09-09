package org.hibernate.envers.revisioninfo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.envers.DefaultTrackingModifiedEntitiesRevisionEntity;
import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionListener;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.entities.PropertyData;
import org.hibernate.envers.synchronization.CollectionChangeEvent;
import org.hibernate.envers.synchronization.EntityChangeEvent;
import org.hibernate.envers.tools.reflection.ReflectionTools;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;

/**
 * Automatically adds entity names, that have been changed during current revision, to revision entity.
 * @see ModifiedEntityNames
 * @see DefaultTrackingModifiedEntitiesRevisionEntity
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class DefaultTrackingModifiedEntitiesRevisionInfoGenerator extends DefaultRevisionInfoGenerator {
    private final Setter modifiedEntityNamesSetter;
    private final Getter modifiedEntityNamesGetter;

    public DefaultTrackingModifiedEntitiesRevisionInfoGenerator(String revisionInfoEntityName, Class<?> revisionInfoClass,
                                                                Class<? extends RevisionListener> listenerClass,
                                                                PropertyData revisionInfoTimestampData, boolean timestampAsDate,
                                                                PropertyData modifiedEntityNamesData) {
        super(revisionInfoEntityName, revisionInfoClass, listenerClass, revisionInfoTimestampData, timestampAsDate);
        modifiedEntityNamesSetter = ReflectionTools.getSetter(revisionInfoClass, modifiedEntityNamesData);
        modifiedEntityNamesGetter = ReflectionTools.getGetter(revisionInfoClass, modifiedEntityNamesData);
    }

    @Override
    public void entityChanged(EntityChangeEvent event) {
        super.entityChanged(event);
        setModifiedEntityName(event.getRevisionEntity(), event.getEntityName());
    }

    @Override
    public void collectionChanged(CollectionChangeEvent event) {
        super.collectionChanged(event);
        setModifiedEntityName(event.getRevisionEntity(), event.getEntityName());
    }

    @SuppressWarnings({"unchecked"})
    private void setModifiedEntityName(Object revisionEntity, String entityName) {
        Set<String> modifiedEntityNames = (Set<String>) modifiedEntityNamesGetter.get(revisionEntity);
        if (modifiedEntityNames == null) {
            modifiedEntityNames = new HashSet<String>();
            modifiedEntityNamesSetter.set(revisionEntity, modifiedEntityNames, null);
        }
        modifiedEntityNames.add(entityName);
    }
}
