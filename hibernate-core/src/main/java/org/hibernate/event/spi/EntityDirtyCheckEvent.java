package org.hibernate.event.spi;

/**
 * Defines class for the entity-dirty-checking event.
 *
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class EntityDirtyCheckEvent extends FlushEvent {
	private boolean dirty;
	private final Object entity;

	public EntityDirtyCheckEvent(EventSource source, Object entity) {
		super(source);
		this.entity = entity;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public Object getEntity() {
		return entity;
	}
}