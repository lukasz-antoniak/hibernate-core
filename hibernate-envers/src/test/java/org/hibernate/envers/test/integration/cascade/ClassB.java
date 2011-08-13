package org.hibernate.envers.test.integration.cascade;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Entity
@Table(name = "CLASS_B")
@Audited
public class ClassB {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "VALUE")
    private String value;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLASS_REF_ID")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ClassA belongsTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ClassA getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(ClassA belongsTo) {
        this.belongsTo = belongsTo;
    }
}