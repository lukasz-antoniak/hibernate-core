package org.hibernate.test.dirty;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.test.jdbc.Person;
import org.hibernate.test.onetomany.Node;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@TestForIssue(jiraKey = "HHH-5054")
public class EntityDirtyCheckTest extends BaseCoreFunctionalTestCase {
	private Person lukasz = null;
	private Node parentNode = null;
	private Node childNode = null;

	@Override
	protected void configure(Configuration configuration) {
		configuration.setProperty( "hibernate.show_sql", "true" );
		configuration.setProperty( "hibernate.format_sql", "true" );
	}

	@Override
	public String[] getMappings() {
		return new String[] { "jdbc/Mappings.hbm.xml", "onetomany/Node.hbm.xml" };
	}

	@Before
	public void initData() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();

		lukasz = new Person( "Lukasz", "Antoniak" );
		session.persist( lukasz );

		parentNode = new Node( 1, "Parent" );
		childNode = new Node( 2, "Child" );
		parentNode.getSubNodes().add( childNode );
		childNode.setParentNode( parentNode );
		session.persist( parentNode );
		session.persist( childNode );

		transaction.commit();
		session.close();
	}

	@After
	public void cleanData() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();

		parentNode = (Node) session.get( Node.class, parentNode.getId() );
		childNode = (Node) session.get( Node.class, childNode.getId() );
		session.delete( parentNode );
		session.delete( childNode );

		transaction.commit();
		session.close();
	}

	@Test
	public void testNewObject() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		Person kinga = new Person( "Kinga", "Mroz" );
		assertFalse( session.isDirty( kinga ) );
		session.persist( kinga );
		assertTrue( session.isDirty( kinga ) );
		transaction.commit();
		session.close();
	}

	@Test
	public void testModifyObject() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		lukasz = (Person) session.get( Person.class, lukasz.getId() );
		assertFalse( session.isDirty( lukasz ) );
		lukasz.setFirstName( "Robert" );
		assertTrue( session.isDirty( lukasz ) );
		session.update( lukasz );
		assertTrue( session.isDirty( lukasz ) );
		transaction.commit();
		session.close();
	}

	@Test
	public void testRemoveObject() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		lukasz = (Person) session.get( Person.class, lukasz.getId() );
		assertFalse( session.isDirty( lukasz ) );
		session.delete( lukasz );
		assertTrue( session.isDirty( lukasz ) );
		transaction.commit();
		session.close();
	}

	@Test
	public void testAddRelation() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		Node anotherNode = new Node( 3, "Node" );
		session.persist( anotherNode );
		transaction.commit();
		session.close();

		session = openSession();
		transaction = session.beginTransaction();
		parentNode = (Node) session.get( Node.class, parentNode.getId() );
		anotherNode = (Node) session.get( Node.class, anotherNode.getId() );
		assertFalse( session.isDirty( parentNode ));
		assertFalse( session.isDirty( anotherNode ));
		anotherNode.setParentNode( parentNode );
		parentNode.getSubNodes().add( anotherNode );
		assertTrue( session.isDirty( parentNode ));
		assertTrue( session.isDirty( anotherNode ));
		session.update( anotherNode );
		session.update( parentNode );
		assertTrue( session.isDirty( parentNode ) );
		assertTrue( session.isDirty( anotherNode ) );
		transaction.commit();
		session.close();

		session = openSession();
		transaction = session.beginTransaction();
		anotherNode = (Node) session.get( Node.class, anotherNode.getId() );
		session.delete( anotherNode );
		transaction.commit();
		session.close();
	}

	@Test
	public void testAddRelationOneSide() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		Node anotherNode = new Node( 3, "Node" );
		session.persist( anotherNode );
		transaction.commit();
		session.close();

		session = openSession();
		transaction = session.beginTransaction();
		parentNode = (Node) session.get( Node.class, parentNode.getId() );
		anotherNode = (Node) session.get( Node.class, anotherNode.getId() );
		assertFalse( session.isDirty( parentNode ));
		assertFalse( session.isDirty( anotherNode ));
		parentNode.getSubNodes().add( anotherNode );
		assertTrue( session.isDirty( parentNode ));
		assertFalse( session.isDirty( anotherNode ));
		session.update( parentNode );
		assertTrue( session.isDirty( parentNode ) );
		assertFalse( session.isDirty( anotherNode ) );
		transaction.commit();
		session.close();

		session = openSession();
		transaction = session.beginTransaction();
		anotherNode = (Node) session.get( Node.class, anotherNode.getId() );
		session.delete( anotherNode );
		transaction.commit();
		session.close();
	}

	@Test
	public void testRemoveRelation() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		parentNode = (Node) session.get( Node.class, parentNode.getId() );
		childNode = (Node) session.get( Node.class, childNode.getId() );
		assertFalse( session.isDirty( parentNode ));
		assertFalse( session.isDirty( childNode ));
		parentNode.getSubNodes().remove( childNode );
		childNode.setParentNode( null );
		assertTrue( session.isDirty( parentNode ));
		assertTrue( session.isDirty( childNode ));
		session.update( parentNode );
		session.update( childNode );
		assertTrue( session.isDirty( parentNode ) );
		assertTrue( session.isDirty( childNode ) );
		transaction.commit();
		session.close();
	}

	@Test
	public void testDetachedObject() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		assertFalse( session.isDirty( lukasz ));
		transaction.commit();
		session.close();
	}

	@Test
	public void testProxyObject() {
		Session session = openSession();
		Transaction transaction = session.beginTransaction();
		lukasz = (Person) session.load( Person.class, lukasz.getId() );
		Person lukaszCopy = (Person) session.get( Person.class, lukasz.getId() );
		lukaszCopy.setFirstName( "Copy" );
		session.update( lukaszCopy );
		assertTrue( session.isDirty( lukasz ));
		transaction.commit();
		session.close();
	}
}
