package org.hibernate.envers.test.integration.cascade;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CLASS_A")
public class ClassA {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "OBJECT_REF_ID")
    private String objectId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "belongsTo", fetch = FetchType.EAGER)
    private Set<ClassB> dataAssociatedWithA = new HashSet<ClassB>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<ClassA> children = new HashSet<ClassA>();

    @ManyToOne
    @JoinColumn(name = "PARENT_REF")
    private ClassA parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Set<ClassB> getDataAssociatedWithA() {
        return dataAssociatedWithA;
    }

    public void setDataAssociatedWithA(Set<ClassB> dataAssociatedWithA) {
        this.dataAssociatedWithA = dataAssociatedWithA;
    }

    public Set<ClassA> getChildren() {
        return children;
    }

    public void setChildren(Set<ClassA> children) {
        this.children = children;
    }

    public ClassA getParent() {
        return parent;
    }

    public void setParent(ClassA parent) {
        this.parent = parent;
    }

    public void add(ClassB classB) {
        classB.setBelongsTo(this);
        dataAssociatedWithA.add(classB);
    }
}