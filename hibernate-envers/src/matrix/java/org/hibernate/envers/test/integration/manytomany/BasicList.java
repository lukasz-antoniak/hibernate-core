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
package org.hibernate.envers.test.integration.manytomany;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.persistence.EntityManager;

import org.junit.Test;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.envers.test.AbstractEntityTest;
import org.hibernate.envers.test.Priority;
import org.hibernate.envers.test.entities.manytomany.ListOwnedEntity;
import org.hibernate.envers.test.entities.manytomany.ListOwningEntity;
import org.hibernate.envers.test.tools.TestTools;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public class BasicList extends AbstractEntityTest {
    private Integer ed1_id;
    private Integer ed2_id;

    private Integer ing1_id;
    private Integer ing2_id;

    public void configure(Ejb3Configuration cfg) {
        cfg.addAnnotatedClass(ListOwningEntity.class);
        cfg.addAnnotatedClass(ListOwnedEntity.class);
    }

    @Test
    @Priority(10)
    public void initData() {
        EntityManager em = getEntityManager();

        ListOwnedEntity ed1 = new ListOwnedEntity(1, "data_ed_1");
        ListOwnedEntity ed2 = new ListOwnedEntity(2, "data_ed_2");

        ListOwningEntity ing1 = new ListOwningEntity(3, "data_ing_1");
        ListOwningEntity ing2 = new ListOwningEntity(4, "data_ing_2");

        // Revision 1
        em.getTransaction().begin();

        em.persist(ed1);
        em.persist(ed2);
        em.persist(ing1);
        em.persist(ing2);

        em.getTransaction().commit();

        // Revision 2

        em.getTransaction().begin();

        ing1 = em.find(ListOwningEntity.class, ing1.getId());
        ing2 = em.find(ListOwningEntity.class, ing2.getId());
        ed1 = em.find(ListOwnedEntity.class, ed1.getId());
        ed2 = em.find(ListOwnedEntity.class, ed2.getId());

        ing1.setReferences(new ArrayList<ListOwnedEntity>());
        ing1.getReferences().add(ed1);

        ing2.setReferences(new ArrayList<ListOwnedEntity>());
        ing2.getReferences().add(ed1);
        ing2.getReferences().add(ed2);

        em.getTransaction().commit();

        // Revision 3
        em.getTransaction().begin();

        ing1 = em.find(ListOwningEntity.class, ing1.getId());
        ed2 = em.find(ListOwnedEntity.class, ed2.getId());
        ed1 = em.find(ListOwnedEntity.class, ed1.getId());

        ing1.getReferences().add(ed2);

        em.getTransaction().commit();

        // Revision 4
        em.getTransaction().begin();

        ing1 = em.find(ListOwningEntity.class, ing1.getId());
        ed2 = em.find(ListOwnedEntity.class, ed2.getId());
        ed1 = em.find(ListOwnedEntity.class, ed1.getId());

        ing1.getReferences().remove(ed1);

        em.getTransaction().commit();

        // Revision 5
        em.getTransaction().begin();

        ing1 = em.find(ListOwningEntity.class, ing1.getId());

        ing1.setReferences(null);

        em.getTransaction().commit();

        //

        ed1_id = ed1.getId();
        ed2_id = ed2.getId();

        ing1_id = ing1.getId();
        ing2_id = ing2.getId();
    }

    @Test
    public void testRevisionsCounts() {
        assert Arrays.asList(1L, 2L, 4L).equals(getAuditReader().getRevisions(ListOwnedEntity.class, ed1_id));
        assert Arrays.asList(1L, 2L, 3L, 5L).equals(getAuditReader().getRevisions(ListOwnedEntity.class, ed2_id));

        assert Arrays.asList(1L, 2L, 3L, 4L, 5L).equals(getAuditReader().getRevisions(ListOwningEntity.class, ing1_id));
        assert Arrays.asList(1L, 2L).equals(getAuditReader().getRevisions(ListOwningEntity.class, ing2_id));
    }

    @Test
    public void testHistoryOfEdId1() {
        ListOwningEntity ing1 = getEntityManager().find(ListOwningEntity.class, ing1_id);
        ListOwningEntity ing2 = getEntityManager().find(ListOwningEntity.class, ing2_id);

        ListOwnedEntity rev1 = getAuditReader().find(ListOwnedEntity.class, ed1_id, 1L);
        ListOwnedEntity rev2 = getAuditReader().find(ListOwnedEntity.class, ed1_id, 2L);
        ListOwnedEntity rev3 = getAuditReader().find(ListOwnedEntity.class, ed1_id, 3L);
        ListOwnedEntity rev4 = getAuditReader().find(ListOwnedEntity.class, ed1_id, 4L);
        ListOwnedEntity rev5 = getAuditReader().find(ListOwnedEntity.class, ed1_id, 5L);

        assert rev1.getReferencing().equals(Collections.EMPTY_LIST);
        assert TestTools.checkList(rev2.getReferencing(), ing1, ing2);
        assert TestTools.checkList(rev3.getReferencing(), ing1, ing2);
        assert TestTools.checkList(rev4.getReferencing(), ing2);
        assert TestTools.checkList(rev5.getReferencing(), ing2);
    }

    @Test
    public void testHistoryOfEdId2() {
        ListOwningEntity ing1 = getEntityManager().find(ListOwningEntity.class, ing1_id);
        ListOwningEntity ing2 = getEntityManager().find(ListOwningEntity.class, ing2_id);

        ListOwnedEntity rev1 = getAuditReader().find(ListOwnedEntity.class, ed2_id, 1L);
        ListOwnedEntity rev2 = getAuditReader().find(ListOwnedEntity.class, ed2_id, 2L);
        ListOwnedEntity rev3 = getAuditReader().find(ListOwnedEntity.class, ed2_id, 3L);
        ListOwnedEntity rev4 = getAuditReader().find(ListOwnedEntity.class, ed2_id, 4L);
        ListOwnedEntity rev5 = getAuditReader().find(ListOwnedEntity.class, ed2_id, 5L);

        assert rev1.getReferencing().equals(Collections.EMPTY_LIST);
        assert TestTools.checkList(rev2.getReferencing(), ing2);
        assert TestTools.checkList(rev3.getReferencing(), ing1, ing2);
        assert TestTools.checkList(rev4.getReferencing(), ing1, ing2);
        assert TestTools.checkList(rev5.getReferencing(), ing2);
    }

    @Test
    public void testHistoryOfEdIng1() {
        ListOwnedEntity ed1 = getEntityManager().find(ListOwnedEntity.class, ed1_id);
        ListOwnedEntity ed2 = getEntityManager().find(ListOwnedEntity.class, ed2_id);

        ListOwningEntity rev1 = getAuditReader().find(ListOwningEntity.class, ing1_id, 1L);
        ListOwningEntity rev2 = getAuditReader().find(ListOwningEntity.class, ing1_id, 2L);
        ListOwningEntity rev3 = getAuditReader().find(ListOwningEntity.class, ing1_id, 3L);
        ListOwningEntity rev4 = getAuditReader().find(ListOwningEntity.class, ing1_id, 4L);
        ListOwningEntity rev5 = getAuditReader().find(ListOwningEntity.class, ing1_id, 5L);

        assert rev1.getReferences().equals(Collections.EMPTY_LIST);
        assert TestTools.checkList(rev2.getReferences(), ed1);
        assert TestTools.checkList(rev3.getReferences(), ed1, ed2);
        assert TestTools.checkList(rev4.getReferences(), ed2);
        assert rev5.getReferences().equals(Collections.EMPTY_LIST);
    }

    @Test
    public void testHistoryOfEdIng2() {
        ListOwnedEntity ed1 = getEntityManager().find(ListOwnedEntity.class, ed1_id);
        ListOwnedEntity ed2 = getEntityManager().find(ListOwnedEntity.class, ed2_id);

        ListOwningEntity rev1 = getAuditReader().find(ListOwningEntity.class, ing2_id, 1L);
        ListOwningEntity rev2 = getAuditReader().find(ListOwningEntity.class, ing2_id, 2L);
        ListOwningEntity rev3 = getAuditReader().find(ListOwningEntity.class, ing2_id, 3L);
        ListOwningEntity rev4 = getAuditReader().find(ListOwningEntity.class, ing2_id, 4L);
        ListOwningEntity rev5 = getAuditReader().find(ListOwningEntity.class, ing2_id, 5L);

        assert rev1.getReferences().equals(Collections.EMPTY_LIST);
        assert TestTools.checkList(rev2.getReferences(), ed1, ed2);
        assert TestTools.checkList(rev3.getReferences(), ed1, ed2);
        assert TestTools.checkList(rev4.getReferences(), ed1, ed2);
        assert TestTools.checkList(rev5.getReferences(), ed1, ed2);
    }
}