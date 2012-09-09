package org.hibernate.envers.test.tools;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import junit.framework.Assert;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.synchronization.CollectionChangeEvent;
import org.hibernate.envers.synchronization.EntityChangeEvent;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class NotificationCheckRevisionListener implements EntityTrackingRevisionListener {
	private final static Queue<ChangeNotification> expectedEntityNotifications = new LinkedList<ChangeNotification>();
	private final static Queue<ChangeNotification> expectedCollectionNotifications = new LinkedList<ChangeNotification>();

	public static void expectEntityNotification(Class entityClass, Serializable entityId, Object entity,
												RevisionType revisionType) {
		expectedEntityNotifications.offer( new ChangeNotification( entityClass, entityId, entity, revisionType ) );
	}

	public static void expectCollectionNotification(Class entityClass, Serializable entityId, Object entity,
													RevisionType revisionType) {
		expectedCollectionNotifications.offer( new ChangeNotification( entityClass, entityId, entity, revisionType ) );
	}

	public static void checkAllExpectedNotificationsProcessed() {
		Assert.assertTrue( expectedEntityNotifications.isEmpty() );
		Assert.assertTrue( expectedCollectionNotifications.isEmpty() );
	}

	@Override
	public void entityChanged(EntityChangeEvent event) {
		checkNotification(
				expectedEntityNotifications.poll(),
				event.getEntityClass(),
				event.getEntityName(),
				event.getEntityId(),
				event.getEntity(),
				event.getRevisionType()
		);
	}

	@Override
	public void collectionChanged(CollectionChangeEvent event) {
		checkNotification(
				expectedCollectionNotifications.poll(),
				event.getEntityClass(),
				event.getEntityName(),
				event.getEntityId(),
				event.getCollection(),
				event.getRevisionType()
		);
	}

	private void checkNotification(ChangeNotification expected, Class entityClass, String entityName,
								   Serializable entityId, Object entity, RevisionType revisionType) {
		if ( expected != null ) {
			Assert.assertEquals( expected.entityClass, entityClass );
			Assert.assertEquals( expected.entityName, entityName );
			Assert.assertEquals( expected.entityId, entityId );
			Assert.assertEquals( expected.entity, entity );
			Assert.assertEquals( expected.revisionType, revisionType );
		}
		else {
			Assert.fail();
		}
	}

	@Override
	public void newRevision(Object revisionEntity) {
		Assert.assertFalse( expectedEntityNotifications.isEmpty() && expectedCollectionNotifications.isEmpty() );
	}

	private static class ChangeNotification {
		private final Class entityClass;
		private final String entityName;
		private final Serializable entityId;
		private final Object entity;
		private final RevisionType revisionType;

		public ChangeNotification(Class entityClass, Serializable entityId, Object entity,
								  RevisionType revisionType) {
			this(entityClass, entityClass.getName(), entityId, entity, revisionType);
		}

		public ChangeNotification(Class entityClass, String entityName, Serializable entityId, Object entity,
								  RevisionType revisionType) {
			this.entityClass = entityClass;
			this.entityName = entityName;
			this.entityId = entityId;
			this.entity = entity;
			this.revisionType = revisionType;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof ChangeNotification) ) {
				return false;
			}

			ChangeNotification that = (ChangeNotification) o;

			if ( entity != null ? !entity.equals( that.entity ) : that.entity != null ) {
				return false;
			}
			if ( entityClass != null ? !entityClass.equals( that.entityClass ) : that.entityClass != null ) {
				return false;
			}
			if ( entityId != null ? !entityId.equals( that.entityId ) : that.entityId != null ) {
				return false;
			}
			if ( entityName != null ? !entityName.equals( that.entityName ) : that.entityName != null ) {
				return false;
			}
			if ( revisionType != that.revisionType ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = entityClass != null ? entityClass.hashCode() : 0;
			result = 31 * result + ( entityName != null ? entityName.hashCode() : 0 );
			result = 31 * result + ( entityId != null ? entityId.hashCode() : 0 );
			result = 31 * result + ( entity != null ? entity.hashCode() : 0 );
			result = 31 * result + ( revisionType != null ? revisionType.hashCode() : 0 );
			return result;
		}

		@Override
		public String toString() {
			return "ChangeNotification(entityClass = " + entityClass + ", entityName = " + entityName + ", entityId = " +
					entityId + ", entity = " + entity + ", revisionType = " + revisionType + ")";
		}

		public Class getEntityClass() {
			return entityClass;
		}

		public String getEntityName() {
			return entityName;
		}

		public Serializable getEntityId() {
			return entityId;
		}

		public Object getEntity() {
			return entity;
		}

		public RevisionType getRevisionType() {
			return revisionType;
		}
	}
}
