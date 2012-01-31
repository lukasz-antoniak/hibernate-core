package org.hibernate.envers.test.integration.hhh6544;

import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lukasz Antoniak (lukasz.antoniak@oracle.com) Oracle iTech Poland
 */
@Entity
@Audited
public class User implements Serializable {
    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Destiny> destinies = new HashSet<Destiny>();

    @ManyToMany
    @JoinTable(joinColumns = @JoinColumn(name = "USER_ID"),
               inverseJoinColumns = @JoinColumn(name = "SECURITYGROUPS_ID"),
               uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID", "SECURITYGROUPS_ID"}))
    private Set<SecurityGroup> securityGroups = new HashSet<SecurityGroup>();

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public User(String name, int id) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "User(id = " + id + ", name = " + name + ")";
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Destiny> getDestinies() {
        return destinies;
    }

    public void setDestinies(Set<Destiny> destinies) {
        this.destinies = destinies;
    }

    public Set<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }
}