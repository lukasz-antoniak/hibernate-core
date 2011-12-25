/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.test.integration.onetomany.detached;

import java.util.Arrays;
import javax.persistence.EntityManager;

import org.junit.Test;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.envers.test.AbstractEntityTest;
import org.hibernate.envers.test.Priority;
import org.hibernate.envers.test.entities.onetomany.detached.ListJoinColumnBidirectionalRefEdEntity;
import org.hibernate.envers.test.entities.onetomany.detached.ListJoinColumnBidirectionalRefIngEntity;

import static org.hibernate.envers.test.tools.TestTools.checkList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for a "fake" bidirectional mapping where one side uses @OneToMany+@JoinColumn (and thus owns the relatin),
 * and the other uses a @ManyToOne(insertable=false, updatable=false).
 * @author Adam Warski (adam at warski dot org)
 */
public class JoinColumnBidirectionalList extends AbstractEntityTest {
    private Integer ed1_id;
    private Integer ed2_id;

    private Integer ing1_id;
    private Integer ing2_id;

    public void configure(Ejb3Configuration cfg) {
        cfg.addAnnotatedClass(ListJoinColumnBidirectionalRefIngEntity.class);
        cfg.addAnnotatedClass(ListJoinColumnBidirectionalRefEdEntity.class);
    }

    @Test
    @Priority(10)
    public void createData() {
        EntityManager em = getEntityManager();

        ListJoinColumnBidirectionalRefEdEntity ed1 = new ListJoinColumnBidirectionalRefEdEntity("ed1", null);
        ListJoinColumnBidirectionalRefEdEntity ed2 = new ListJoinColumnBidirectionalRefEdEntity("ed2", null);

        ListJoinColumnBidirectionalRefIngEntity ing1 = new ListJoinColumnBidirectionalRefIngEntity("coll1", ed1);
        ListJoinColumnBidirectionalRefIngEntity ing2 = new ListJoinColumnBidirectionalRefIngEntity("coll1", ed2);

        // Revision 1 (ing1: ed1, ing2: ed2)
        em.getTransaction().begin();

        em.persist(ed1);
        em.persist(ed2);
        em.persist(ing1);
        em.persist(ing2);

        em.getTransaction().commit();

        // Revision 2 (ing1: ed1, ed2)
        em.getTransaction().begin();

        ing1 = em.find(ListJoinColumnBidirectionalRefIngEntity.class, ing1.getId());
        ing2 = em.find(ListJoinColumnBidirectionalRefIngEntity.class, ing2.getId());
        ed1 = em.find(ListJoinColumnBidirectionalRefEdEntity.class, ed1.getId());
        ed2 = em.find(ListJoinColumnBidirectionalRefEdEntity.class, ed2.getId());

        ing2.getReferences().remove(ed2);
        ing1.getReferences().add(ed2);

        em.getTransaction().commit();
        em.clear();

        // No revision - no changes
        em.getTransaction().begin();

        ing1 = em.find(ListJoinColumnBidirectionalRefIngEntity.class, ing1.getId());
        ing2 = em.find(ListJoinColumnBidirectionalRefIngEntity.class, ing2.getId());
        ed1 = em.find(ListJoinColumnBidirectionalRefEdEntity.class, ed1.getId());
        ed2 = em.find(ListJoinColumnBidirectionalRefEdEntity.class, ed2.getId());

        ed2.setOwner(ing2);

        em.getTransaction().commit();
        em.clear();

        // Revision 3 (ing1: ed1, ed2)
        em.getTransaction().begin();

        ed1 = em.find(ListJoinColumnBidirectionalRefEdEntity.class, ed1.getId());

        ed1.setData("ed1 bis");
        // Shouldn't get written
        ed1.setOwner(ing2);

        em.getTransaction().commit();
        em.clear();

        // Revision 4 (ing2: ed1, ed2)
        em.getTransaction().begin();

        ing1 = em.find(ListJoinColumnBidirectionalRefIngEntity.class, ing1.getId());
        ing2 = em.find(ListJoinColumnBidirectionalRefIngEntity.class, ing2.getId());

        ing1.getReferences().clear();
        ing2.getReferences().add(ed1);
        ing2.getReferences().add(ed2);

        em.getTransaction().commit();
        em.clear();

        //

        ing1_id = ing1.getId();
        ing2_id = ing2.getId();

        ed1_id = ed1.getId();
        ed2_id = ed2.getId();
    }

    @Test
    public void testRevisionsCounts() {
        assertEquals(Arrays.asList(1L, 2L, 4L), getAuditReader().getRevisions(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id));
        assertEquals(Arrays.asList(1L, 2L, 4L), getAuditReader().getRevisions(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id));

        assertEquals(Arrays.asList(1L, 3L, 4L), getAuditReader().getRevisions(ListJoinColumnBidirectionalRefEdEntity.class, ed1_id));
        assertEquals(Arrays.asList(1L, 2L, 4L), getAuditReader().getRevisions(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id));
    }

    @Test
    public void testHistoryOfIng1() {
        ListJoinColumnBidirectionalRefEdEntity ed1_fromRev1 = new ListJoinColumnBidirectionalRefEdEntity(ed1_id, "ed1", null);
        ListJoinColumnBidirectionalRefEdEntity ed1_fromRev3 = new ListJoinColumnBidirectionalRefEdEntity(ed1_id, "ed1 bis", null);
        ListJoinColumnBidirectionalRefEdEntity ed2 = getEntityManager().find(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id);

        ListJoinColumnBidirectionalRefIngEntity rev1 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id, 1L);
        ListJoinColumnBidirectionalRefIngEntity rev2 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id, 2L);
        ListJoinColumnBidirectionalRefIngEntity rev3 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id, 3L);
        ListJoinColumnBidirectionalRefIngEntity rev4 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id, 4L);

        assertTrue(checkList(rev1.getReferences(), ed1_fromRev1));
        assertTrue(checkList(rev2.getReferences(), ed1_fromRev1, ed2));
        assertTrue(checkList(rev3.getReferences(), ed1_fromRev3, ed2));
        assertTrue(checkList(rev4.getReferences()));
    }

    @Test
    public void testHistoryOfIng2() {
        ListJoinColumnBidirectionalRefEdEntity ed1 = getEntityManager().find(ListJoinColumnBidirectionalRefEdEntity.class, ed1_id);
        ListJoinColumnBidirectionalRefEdEntity ed2 = getEntityManager().find(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id);

        ListJoinColumnBidirectionalRefIngEntity rev1 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id, 1l);
        ListJoinColumnBidirectionalRefIngEntity rev2 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id, 2L);
        ListJoinColumnBidirectionalRefIngEntity rev3 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id, 3L);
        ListJoinColumnBidirectionalRefIngEntity rev4 = getAuditReader().find(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id, 4L);

        assertTrue(checkList(rev1.getReferences(), ed2));
        assertTrue(checkList(rev2.getReferences()));
        assertTrue(checkList(rev3.getReferences()));
        assertTrue(checkList(rev4.getReferences(), ed1, ed2));
    }

    @Test
    public void testHistoryOfEd1() {
        ListJoinColumnBidirectionalRefIngEntity ing1 = getEntityManager().find(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id);
        ListJoinColumnBidirectionalRefIngEntity ing2 = getEntityManager().find(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id);

        ListJoinColumnBidirectionalRefEdEntity rev1 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed1_id, 1L);
        ListJoinColumnBidirectionalRefEdEntity rev2 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed1_id, 2L);
        ListJoinColumnBidirectionalRefEdEntity rev3 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed1_id, 3L);
        ListJoinColumnBidirectionalRefEdEntity rev4 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed1_id, 4L);

        assertTrue(rev1.getOwner().equals(ing1));
        assertTrue(rev2.getOwner().equals(ing1));
        assertTrue(rev3.getOwner().equals(ing1));
        assertTrue(rev4.getOwner().equals(ing2));

        assertEquals(rev1.getData(), "ed1");
        assertEquals(rev2.getData(), "ed1");
        assertEquals(rev3.getData(), "ed1 bis");
        assertEquals(rev4.getData(), "ed1 bis");
    }

    @Test
    public void testHistoryOfEd2() {
        ListJoinColumnBidirectionalRefIngEntity ing1 = getEntityManager().find(ListJoinColumnBidirectionalRefIngEntity.class, ing1_id);
        ListJoinColumnBidirectionalRefIngEntity ing2 = getEntityManager().find(ListJoinColumnBidirectionalRefIngEntity.class, ing2_id);

        ListJoinColumnBidirectionalRefEdEntity rev1 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id, 1L);
        ListJoinColumnBidirectionalRefEdEntity rev2 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id, 2L);
        ListJoinColumnBidirectionalRefEdEntity rev3 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id, 3L);
        ListJoinColumnBidirectionalRefEdEntity rev4 = getAuditReader().find(ListJoinColumnBidirectionalRefEdEntity.class, ed2_id, 4L);

        assertTrue(rev1.getOwner().equals(ing2));
        assertTrue(rev2.getOwner().equals(ing1));
        assertTrue(rev3.getOwner().equals(ing1));
        assertTrue(rev4.getOwner().equals(ing2));

        assertEquals(rev1.getData(), "ed2");
        assertEquals(rev2.getData(), "ed2");
        assertEquals(rev3.getData(), "ed2");
        assertEquals(rev4.getData(), "ed2");
    }
}
