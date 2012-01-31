package org.hibernate.envers.test.integration.hhh6544;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.envers.test.AbstractEntityTest;
import org.hibernate.envers.test.Priority;
import org.hibernate.envers.test.tools.TestTools;
import org.hibernate.testing.TestForIssue;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.Arrays;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@TestForIssue(jiraKey = "HHH-6544")
public class HHH6544Test extends AbstractEntityTest {
    private int userId;
    private int destinyId;
    private int securityGroup1Id;
    private int securityGroup2Id;

    public void configure(Ejb3Configuration cfg) {
        cfg.addAnnotatedClass(User.class);
        cfg.addAnnotatedClass(Destiny.class);
        cfg.addAnnotatedClass(SecurityGroup.class);
    }

    @Test
    @Priority(10)
    public void initData() {
        EntityManager em = getEntityManager();

        // Revision 1
        em.getTransaction().begin();
        User user = new User("Lukasz");
        Destiny destiny = new Destiny("Unknown");
        SecurityGroup securityGroup1 = new SecurityGroup("Administrator");
        user.getDestinies().add(destiny);
        user.getSecurityGroups().add(securityGroup1);
        em.persist(destiny);
        em.persist(securityGroup1);
        em.persist(user);
        em.getTransaction().commit();

        userId = user.getId();
        destinyId = destiny.getId();
        securityGroup1Id = securityGroup1.getId();

        // Revision 2
        em.getTransaction().begin();
        user = em.find(User.class, user.getId());
        SecurityGroup securityGroup2 = new SecurityGroup("User");
        user.getSecurityGroups().add(securityGroup2);
        em.persist(securityGroup2);
        em.merge(user);
        em.getTransaction().commit();

        securityGroup2Id = securityGroup2.getId();

        // Revision 3
        em.getTransaction().begin();
        user = em.find(User.class, user.getId());
        user.setName("Kinga");
        em.merge(user);
        em.getTransaction().commit();

        // Revision 4
        em.getTransaction().begin();
        securityGroup1 = em.find(SecurityGroup.class, securityGroup1.getId());
        user = em.find(User.class, user.getId());
        user.getDestinies().clear();
        user.getSecurityGroups().remove(securityGroup1);
        em.merge(user);
        em.getTransaction().commit();
    }

    @Test
    public void testRevisionsCounts() {
        Assert.assertEquals(Arrays.asList(1, 2, 3, 4), getAuditReader().getRevisions(User.class, userId));
        Assert.assertEquals(Arrays.asList(1, 4), getAuditReader().getRevisions(Destiny.class, destinyId));
        Assert.assertEquals(Arrays.asList(1), getAuditReader().getRevisions(SecurityGroup.class, securityGroup1Id));
        Assert.assertEquals(Arrays.asList(2), getAuditReader().getRevisions(SecurityGroup.class, securityGroup2Id));
    }

    @Test
    public void testHistoryOfUser() {
        User user = new User("Lukasz", userId);
        Destiny destiny = new Destiny("Unknown", destinyId);
        SecurityGroup securityGroup1 = new SecurityGroup("Administrator", securityGroup1Id);
        SecurityGroup securityGroup2 = new SecurityGroup("User", securityGroup2Id);

        User ver1 = getAuditReader().find(User.class, userId, 1);

        Assert.assertEquals(user, ver1);
        Assert.assertEquals(TestTools.makeSet(destiny), ver1.getDestinies());
        Assert.assertEquals(TestTools.makeSet(securityGroup1), ver1.getSecurityGroups());

        User ver2 = getAuditReader().find(User.class, userId, 2);

        Assert.assertEquals(user, ver2);
        Assert.assertEquals(TestTools.makeSet(destiny), ver2.getDestinies());
        Assert.assertEquals(TestTools.makeSet(securityGroup1, securityGroup2), ver2.getSecurityGroups());

        user.setName("Kinga");

        User ver3 = getAuditReader().find(User.class, userId, 3);

        Assert.assertEquals(user, ver3);

        User ver4 = getAuditReader().find(User.class, userId, 4);

        Assert.assertEquals(user, ver4);
        Assert.assertTrue(ver4.getDestinies().isEmpty());
        Assert.assertEquals(TestTools.makeSet(securityGroup2), ver4.getSecurityGroups());
    }
}
