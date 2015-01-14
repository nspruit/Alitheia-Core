package eu.sqooss.test.service.db;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.test.service.db.objects.DBObject;

public class DBQueryTest {

	private static final String objNameA = "test-object-a";
	private static final String objNameB = "test-object-b";
	
	private static InMemoryDatabase db;
	
	@BeforeClass
	public static void setUp() {
		db = new InMemoryDatabase(new Class<?>[] { DBObject.class });
	}
	
	@AfterClass
	public static void tearDown() {
		db.close();
	}
	
	@Before
	public void beginTransaction() {
		db.getSessionFactory().getCurrentSession().beginTransaction();
	}
	
	@After
	public void closeTransaction() {
		db.getSessionFactory().getCurrentSession().getTransaction().rollback();
	}
	
	private DBObject construct(String name) {
		DBObject obj = new DBObject();
		obj.setName(name);
		return obj;
	}
	
	@Test
	public void testAddRecord() {
		DBObject obj = construct(objNameA);

		// Store the object and assert that it succeeded
		boolean result = db.getDatabase().addRecord(obj);
		assertTrue(result);

		// Retrieve 
		DBObject storedObj = (DBObject)db.getSessionFactory().getCurrentSession().get(DBObject.class, obj.getId());
		assertEquals(obj, storedObj);
	}
	
	@Test
	public void testAddRecord_multiple() {
		DBObject objA = construct(objNameA);
		DBObject objB = construct(objNameB);

		// Store both objects and assert that it succeeded
		boolean result = db.getDatabase().addRecord(objA);
		assertTrue(result);
		result = db.getDatabase().addRecord(objB);
		assertTrue(result);
		
		// Check that both objects have different IDs
		assertThat(objA.getId(), not(equalTo(objB.getId())));

		// Attempt to retrieve both objects
		DBObject storedObjA = (DBObject)db.getSessionFactory().getCurrentSession().get(DBObject.class, objA.getId());
		DBObject storedObjB = (DBObject)db.getSessionFactory().getCurrentSession().get(DBObject.class, objB.getId());
		assertEquals(objA, storedObjA);
		assertEquals(objB, storedObjB);
	}

}
