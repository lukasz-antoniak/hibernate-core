package org.hibernate.envers.test.entities.reventity.trackmodifiedentities;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.envers.ModifiedEntityNames;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

/**
 * Sample revision entity that uses {@link ModifiedEntityNames} annotation.
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@Entity
@RevisionEntity
public class AnnotatedTrackingRevisionEntity {
    @Id
    @GeneratedValue
    @RevisionNumber
    private long customId;

    @RevisionTimestamp
    private long customTimestamp;

    @ElementCollection
    @JoinTable(name = "REVCHANGES", joinColumns = @JoinColumn(name = "REV"))
    @Column(name = "ENTITYNAME")
    @ModifiedEntityNames
    private Set<String> entityNames;

    public long getCustomId() {
        return customId;
    }

    public void setCustomId(long customId) {
        this.customId = customId;
    }

    public long getCustomTimestamp() {
        return customTimestamp;
    }

    public void setCustomTimestamp(long customTimestamp) {
        this.customTimestamp = customTimestamp;
    }

    public Set<String> getEntityNames() {
        return entityNames;
    }

    public void setEntityNames(Set<String> entityNames) {
        this.entityNames = entityNames;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnotatedTrackingRevisionEntity)) return false;

        AnnotatedTrackingRevisionEntity that = (AnnotatedTrackingRevisionEntity) o;

        if (customId != that.customId) return false;
        if (customTimestamp != that.customTimestamp) return false;
        if (entityNames != null ? !entityNames.equals(that.entityNames) : that.entityNames != null) return false;

        return true;
    }

    public int hashCode() {
        int result = (int) (customId ^ (customId >>> 32));
        result = 31 * result + (int) (customTimestamp ^ (customTimestamp >>> 32));
        result = 31 * result + (entityNames != null ? entityNames.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnnotatedTrackingRevisionEntity(customId = " + customId + ", customTimestamp = " + customTimestamp + ", entityNames=" + entityNames + ")";
    }
}
