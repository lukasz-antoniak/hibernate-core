package org.hibernate.test.annotations.formula;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.testing.RequiresDialect;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@TestForIssue(jiraKey = "HHH-7525")
@RequiresDialect(H2Dialect.class)
public class FormulaExpressionTest extends BaseCoreFunctionalTestCase {
	private Person lukasz = null;

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Person.class };
	}

	@Before
	public void insertTestData() {
		Session session = openSession();
		session.getTransaction().begin();
		lukasz = new Person( "Lukasz", "Antoniak" );
		session.persist( lukasz );
		session.flush();
		session.refresh( lukasz ); // Populates @Formula property.
		session.getTransaction().commit();
		session.close();
	}

	@After
	public void cleanupTestData() {
		Session session = openSession();
		session.getTransaction().begin();
		session.delete( lukasz );
		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testSessionApi() {
		Session session = openSession();
		session.getTransaction().begin();
		Person person = (Person) session.get( Person.class, lukasz.getId() );
		Assert.assertEquals( lukasz, person );
		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testHqlQuery() {
		Session session = openSession();
		session.getTransaction().begin();
		Person person = (Person) session.createQuery( "from Person p where p.id = :id" )
				.setLong( "id", lukasz.getId() ).uniqueResult();
		Assert.assertEquals( lukasz, person );
		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testCriteriaApi() {
		Session session = openSession();
		session.getTransaction().begin();
		Person person = (Person) session.createCriteria( Person.class ).add( Restrictions.idEq( lukasz.getId() ) ).uniqueResult();
		Assert.assertEquals( lukasz, person );
		session.getTransaction().commit();
		session.close();
	}

	@Test
	public void testSqlQuery() {
		Session session = openSession();

		session.getTransaction().begin();
		List<Person> result = session.createSQLQuery( "SELECT p.*, FIRST_NAME || ' ' || LAST_NAME as fullName FROM people p WHERE p.id = :id" )
				.addEntity( Person.class ).setLong( "id", lukasz.getId() ).list();
		Assert.assertEquals( "@Formula expression inserted explicitly into SQL native query.", Arrays.asList( lukasz ), result );

		session.clear();

		// @Formula expression omitted in native SQL query by user.
		Person found = (Person) session.createSQLQuery( "SELECT p.* FROM people p WHERE p.id = :id" )
				.addEntity( Person.class ).setLong( "id", lukasz.getId() ).uniqueResult();
		Assert.assertEquals( lukasz.getId(), found.getId() );
		Assert.assertEquals( lukasz.getFirstName(), found.getFirstName() );
		Assert.assertEquals( lukasz.getLastName(), found.getLastName() );
		Assert.assertNull( found.getFullName() );
		session.getTransaction().commit();

		session.close();
	}
}
