package org.hibernate.envers.test.integration.cascade;

import org.hibernate.cfg.Environment;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.test.AbstractEntityTest;
import org.hibernate.envers.test.Priority;
import org.hibernate.testing.TestForIssue;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChildEntityCascadeTest extends AbstractEntityTest {
    @Override
    public void configure(Ejb3Configuration cfg) {
        cfg.addAnnotatedClass(ClassA.class);
        cfg.addAnnotatedClass(ClassB.class);
        cfg.setProperty(Environment.SHOW_SQL, "true");
        cfg.setProperty(Environment.FORMAT_SQL, "true");
    }

    @Test
    @TestForIssue(jiraKey="HHH-6275")
    public void testCascadeAll() {
        EntityManager em = getEntityManager();

        // Revision 1
        em.getTransaction().begin();
        final ClassA classA = new ClassA();
        classA.setObjectId(UUID.randomUUID().toString());
        final ClassB classB = new ClassB();
        classB.setName("test");
        classB.setValue("value");
        classA.add(classB);
        em.persist(classA);
        em.getTransaction().commit();

        assertAuditCountFor(classB, 1);

        // No audit data shall be persisted during the following transaction
        em.getTransaction().begin();
        ClassA childOfClassA = new ClassA();
        childOfClassA.setObjectId(UUID.randomUUID().toString());
        classA.getChildren().add(childOfClassA);
        em.merge(classA);
        em.getTransaction().commit();

        assertAuditCountFor(classB, 1);
    }

    private void assertAuditCountFor(final ClassB classB, final int count) {
        AuditReader reader = getAuditReader();
        List<Object> rawResults = reader.createQuery().forRevisionsOfEntity(ClassB.class, false, true)
                                        .add(AuditEntity.property("originalId.id").in(Collections.singleton(classB.getId())))
                                        .getResultList();
        Assert.assertEquals(count, rawResults.size());
    }
}
