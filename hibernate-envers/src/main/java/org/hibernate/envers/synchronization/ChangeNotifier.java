package org.hibernate.envers.synchronization;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.revisioninfo.RevisionInfoGenerator;
import org.hibernate.envers.synchronization.work.AuditWorkUnit;
import org.hibernate.envers.synchronization.work.PersistentCollectionChangeWorkUnit;
import org.hibernate.envers.tools.Tools;

/**
 * Notifies {@link RevisionInfoGenerator} about changes made in the current revision.
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class ChangeNotifier {
    private final RevisionInfoGenerator revisionInfoGenerator;
    private final SessionImplementor sessionImplementor;

    public ChangeNotifier(RevisionInfoGenerator revisionInfoGenerator, SessionImplementor sessionImplementor) {
        this.revisionInfoGenerator = revisionInfoGenerator;
        this.sessionImplementor = sessionImplementor;
    }

    /**
     * Notifies {@link RevisionInfoGenerator} about changes made in the current revision. Provides information
     * about modified entity class, entity name and its id, as well as {@link RevisionType} and revision log entity.
     * @param session Active session.
     * @param currentRevisionData Revision log entity.
     * @param vwu Performed work unit.
     */
    public void notifyRevisionGenerator(Session session, Object currentRevisionData, AuditWorkUnit vwu) {
        Serializable entityId = vwu.getEntityId();
        Class entityClass = Tools.getEntityClass(sessionImplementor, session, vwu.getEntityName());
        if (vwu instanceof PersistentCollectionChangeWorkUnit) {
            if (entityId instanceof PersistentCollectionChangeWorkUnit.PersistentCollectionChangeWorkUnitId) {
                // Notify about a change in collection owner entity.
                entityId = ((PersistentCollectionChangeWorkUnit.PersistentCollectionChangeWorkUnitId) entityId).getOwnerId();
            }
            revisionInfoGenerator.collectionChanged(new CollectionChangeEvent(entityClass, vwu.getEntityName(), entityId, vwu.getEntity(),
                                                                              vwu.getRevisionType(), currentRevisionData));
        } else {
            revisionInfoGenerator.entityChanged(new EntityChangeEvent(entityClass, vwu.getEntityName(), entityId, vwu.getEntity(),
                                                                      vwu.getRevisionType(), currentRevisionData));
        }
    }
}
