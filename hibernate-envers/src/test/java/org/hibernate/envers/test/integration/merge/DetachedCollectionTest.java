package org.hibernate.envers.test.integration.merge;

import java.util.Arrays;

import junit.framework.Assert;
import org.junit.Test;

import org.hibernate.Session;
import org.hibernate.envers.test.BaseEnversFunctionalTestCase;
import org.hibernate.envers.test.Priority;
import org.hibernate.testing.TestForIssue;

/**
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
@TestForIssue( jiraKey = "HHH-7928" )
public class DetachedCollectionTest extends BaseEnversFunctionalTestCase {
	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Character.class, Alias.class };
	}

	@Test
	@Priority(10)
	public void initData() {
		Session session = openSession();

		// Revision 1 - persisting unassociated entities.
		session.getTransaction().begin();
		Character paulo = new Character( 1, "Paulo Atreides" );
		Alias alias1 = new Alias( 1, "Paul Muad'Dib" );
		Alias alias2 = new Alias( 2, "Usul" );
		session.save( paulo );
		session.save( alias1 );
		session.save( alias2 );
		session.getTransaction().commit();

		session.close();
		session = openSession();

		// Revision 2 - creating associations.
		paulo.associateAlias( alias1 );
		paulo.associateAlias( alias2 );
		session.getTransaction().begin();
		session.merge( alias1 );
		session.flush();
		session.merge( alias2 );
		session.flush();
		session.getTransaction().commit();

		session.close();
	}

	@Test
	public void testRevisionsCount() {
		Assert.assertEquals( Arrays.asList( 1, 2 ), getAuditReader().getRevisions( Character.class, 1 ) );
		Assert.assertEquals( Arrays.asList( 1, 2 ), getAuditReader().getRevisions( Alias.class, 1 ) );
		Assert.assertEquals( Arrays.asList( 1, 2 ), getAuditReader().getRevisions( Alias.class, 2 ) );
	}

	@Test
	public void testHistoryOfCharacter() {
		Character paulo = new Character( 1, "Paulo Atreides" );
		Alias alias1 = new Alias( 1, "Paul Muad'Dib" );
		Alias alias2 = new Alias( 2, "Usul" );

		Character ver1 = getAuditReader().find( Character.class, 1, 1 );
		Assert.assertEquals( paulo, ver1 );
		Assert.assertEquals( 0, ver1.getAliases().size() );

		paulo.associateAlias( alias1 );
		paulo.associateAlias( alias2 );

		Character ver2 = getAuditReader().find( Character.class, 1, 2 );
		Assert.assertEquals( paulo, ver2 );
		Assert.assertEquals( 2, ver2.getAliases().size() );
		Assert.assertEquals( Arrays.asList( alias1, alias2 ), ver2.getAliases() );
	}

	@Test
	public void testHistoryOfAlias() {
		Character paulo = new Character( 1, "Paulo Atreides" );
		Alias alias1 = new Alias( 1, "Paul Muad'Dib" );

		Alias ver1 = getAuditReader().find( Alias.class, 1, 1 );
		Assert.assertEquals( alias1, ver1 );
		Assert.assertEquals( 0, ver1.getCharacters().size() );

		paulo.associateAlias( alias1 );

		Alias ver2 = getAuditReader().find( Alias.class, 1, 2 );
		Assert.assertEquals( alias1, ver2 );
		Assert.assertEquals( 1, ver2.getCharacters().size() );
		Assert.assertEquals( Arrays.asList( paulo ), ver2.getCharacters() );
	}
}
