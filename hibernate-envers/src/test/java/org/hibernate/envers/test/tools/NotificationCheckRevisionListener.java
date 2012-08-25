package org.hibernate.envers.test.tools;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import junit.framework.Assert;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class NotificationCheckRevisionListener implements EntityTrackingRevisionListener {
	private final static Queue<ChangeNotification> expectedNotifications = new LinkedList<ChangeNotification>();

	public static void expectNotification(Class entityClass, Serializable entityId, Object entity,
										  RevisionType revisionType) {
		expectedNotifications.offer( new ChangeNotification( entityClass, entityId, entity, revisionType ) );
	}

	public static void checkAllExpectedNotificationsProcessed() {
		Assert.assertTrue( expectedNotifications.isEmpty() );
	}

	@Override
	public void entityChanged(Class entityClass, String entityName, Serializable entityId, Object entity,
							  RevisionType revisionType, Object revisionEntity) {
		ChangeNotification notification = expectedNotifications.poll();
		if ( notification != null ) {
			Assert.assertEquals( notification.entityClass, entityClass );
			Assert.assertEquals( notification.entityName, entityName );
			Assert.assertEquals( notification.entityId, entityId );
			System.out.println("1: " + notification.entity.getClass().getName());
			System.out.println("2: " + entity.getClass().getName());
			Assert.assertEquals( notification.entity, entity );
			Assert.assertEquals( notification.revisionType, revisionType );
		}
		else {
			Assert.fail();
		}
	}

	@Override
	public void newRevision(Object revisionEntity) {
		Assert.assertTrue( !expectedNotifications.isEmpty() );
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
