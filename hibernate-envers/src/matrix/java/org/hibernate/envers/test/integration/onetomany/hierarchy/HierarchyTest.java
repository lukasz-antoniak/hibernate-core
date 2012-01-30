package org.hibernate.envers.test.integration.onetomany.hierarchy;

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
@TestForIssue(jiraKey = "HHH-6661")
public class HierarchyTest extends AbstractEntityTest {
    private Long parentId = null;
    private Long child1Id = null;
    private Long child2Id = null;

    public void configure(Ejb3Configuration cfg) {
        cfg.addAnnotatedClass(Node.class);
    }

    @Test
    @Priority(10)
    public void initData() {
        EntityManager em = getEntityManager();

        // Revision 1
        em.getTransaction().begin();
        Node parent = new Node("parent", (Node)null);
        Node child1 = new Node("child1", parent);
        Node child2 = new Node("child2", parent);
        parent.getChildren().add(child1); 
        parent.getChildren().add(child2);
        em.persist(parent);
        em.persist(child1);
        em.persist(child2);
        em.getTransaction().commit();

        parentId = parent.getId();
        child1Id = child1.getId();
        child2Id = child2.getId();

        // Revision 2
        em.getTransaction().begin();
        parent = em.find(Node.class, parent.getId());
        parent.getChildren().get(0).setData("child1 modified");
        em.getTransaction().commit();

        // Revision 3
        em.getTransaction().begin();
        child2 = em.find(Node.class, child2.getId());
        em.remove(child2);
        em.getTransaction().commit();
    }

    @Test
	public void testRevisionsCounts() {
        Assert.assertEquals(Arrays.asList(1, 3), getAuditReader().getRevisions(Node.class, parentId));
        Assert.assertEquals(Arrays.asList(1, 2), getAuditReader().getRevisions(Node.class, child1Id));
        Assert.assertEquals(Arrays.asList(1, 3), getAuditReader().getRevisions(Node.class, child2Id));
    }

    @Test
    public void testHistoryOfParentNode() {
        Node parent = new Node("parent", parentId);
        Node child1 = new Node("child1", child1Id);
        Node child2 = new Node("child2", child2Id);

        Node ver1 = getAuditReader().find(Node.class, parentId, 1);
        Assert.assertEquals(parent, ver1);
        Assert.assertTrue(TestTools.checkList(ver1.getChildren(), child1, child2));

        child1.setData("child1 modified");

        Node ver2 = getAuditReader().find(Node.class, parentId, 2);
        Assert.assertEquals(parent, ver2);
        Assert.assertTrue(TestTools.checkList(ver2.getChildren(), child1, child2));

        Node ver3 = getAuditReader().find(Node.class, parentId, 3);
        Assert.assertEquals(parent, ver3);
        Assert.assertTrue(TestTools.checkList(ver3.getChildren(), child1));
    }

    @Test
    public void testHistoryOfChild1Node() {
        Node parent = new Node("parent", parentId);
        Node child1 = new Node("child1", child1Id);

        Node ver1 = getAuditReader().find(Node.class, child1Id, 1);
        Assert.assertEquals(child1, ver1);
        Assert.assertEquals(parent.getId(), ver1.getParent().getId());
        Assert.assertEquals(parent.getData(), ver1.getParent().getData());

        child1.setData("child1 modified");

        Node ver2 = getAuditReader().find(Node.class, child1Id, 2);
        Assert.assertEquals(child1, ver2);
        Assert.assertEquals(parent.getId(), ver2.getParent().getId());
        Assert.assertEquals(parent.getData(), ver2.getParent().getData());
    }
}