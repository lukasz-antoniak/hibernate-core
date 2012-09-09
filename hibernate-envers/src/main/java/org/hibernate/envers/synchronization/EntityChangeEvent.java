package org.hibernate.envers.synchronization;

import java.io.Serializable;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionType;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class EntityChangeEvent implements Serializable {
    private final Class entityClass;
    private final String entityName;
    private final Serializable entityId;
    private final Object entity;
    private final RevisionType revisionType;
    private final Object revisionEntity;

    public EntityChangeEvent(Class entityClass, String entityName, Serializable entityId, Object entity,
                             RevisionType revisionType, Object revisionEntity) {
        this.entityClass = entityClass;
        this.entityName = entityName;
        this.entityId = entityId;
        this.entity = entity;
        this.revisionType = revisionType;
        this.revisionEntity = revisionEntity;
    }

    /**
     * @return Audited entity class.
     */
    public Class getEntityClass() {
        return entityClass;
    }

    /**
     * @return Name of the audited entity. May be useful when Java class is mapped multiple times,
     *         potentially to different tables.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @return Identifier of modified entity.
     */
    public Serializable getEntityId() {
        return entityId;
    }

    /**
     * @return The actual entity that has changed.
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * @return Modification type (addition, update or removal).
     */
    public RevisionType getRevisionType() {
        return revisionType;
    }

    /**
     * @return An instance of the entity annotated with {@link RevisionEntity}.
     */
    public Object getRevisionEntity() {
        return revisionEntity;
    }
}
