package eu.sqooss.test.service.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.test.service.db.objects.DBObject;

public class DBQueryTest {

	private static final String objNameA = "test-object-a";
	private static final String objNameB = "test-object-b";
	private static final String objNameC = "test-object-c";
	private static final String objNameD = "test-object-d";
	
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

	@Test
	public void testFindObjectById(){
		DBObject objC = construct(objNameC);
		
		// Store object and verify it succeeded
		boolean result = db.getDatabase().addRecord(objC);
		assertTrue(result);
		
		// Check whether the findObjectById function returns the correct object
		DBObject storedObjC = (DBObject)db.getDatabase().findObjectById(DBObject.class, objC.getId());
		assertEquals(objC, storedObjC);
	}
	
	@Test
	public void testFindObjectByIdForUpdate(){
		DBObject objD = construct(objNameD);
		
		// Store object and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		
		// Check whether the findObjectByIdForUpdate function returns the correct object
		DBObject storedObjD = (DBObject)db.getDatabase().findObjectByIdForUpdate(DBObject.class, objD.getId());
		assertEquals(objD, storedObjD);
		
		// Check whether the object has at least the UPGRADE lock => mode is not UPDRAGE as expected?
//		LockMode mode = db.getSessionFactory().getCurrentSession().getCurrentLockMode(storedObjD);
//		assertEquals(LockMode.UPGRADE,mode);
	}
	
	@Test
	public void testFindObjectsByProperties_single_property(){
		DBObject objD = construct(objNameD);
		
		// Store object and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByProperties function returns a list with only objD in it
		List<DBObject> res = db.getDatabase().findObjectsByProperties(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByProperties_multiple_properties(){
		DBObject objD = construct(objNameD);
		
		// Store object and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("id", objD.getId());
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByProperties function returns a list with only objD in it
		List<DBObject> res = db.getDatabase().findObjectsByProperties(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByProperties_multiple_results(){
		DBObject objD = construct(objNameD);
		DBObject objD2 = construct(objNameD);
		
		// Store objects and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		result = db.getDatabase().addRecord(objD2);
		assertTrue(result);
		
		// Create a map with the common name property of objD and objD2
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByProperties function returns a list with only objD and objD2 in it
		List<DBObject> res = db.getDatabase().findObjectsByProperties(DBObject.class, properties);
		assertEquals(2, res.size());
		assertTrue(res.contains(objD));
		assertTrue(res.contains(objD2));
	}
	
	@Test
	public void testFindObjectsByPropertiesForUpdate_single_property(){
		DBObject objD = construct(objNameD);
		
		// Store object and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByPropertiesForUpdate function returns a list with only objD in it
		List<DBObject> res = db.getDatabase().findObjectsByPropertiesForUpdate(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByPropertiesForUpdate_multiple_properties(){
		DBObject objD = construct(objNameD);
		
		// Store object and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("id", objD.getId());
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByPropertiesForUpdate function returns a list with only objD in it
		List<DBObject> res = db.getDatabase().findObjectsByPropertiesForUpdate(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByPropertiesForUpdate_multiple_results(){
		DBObject objD = construct(objNameD);
		DBObject objD2 = construct(objNameD);
		
		// Store objects and verify it succeeded
		boolean result = db.getDatabase().addRecord(objD);
		assertTrue(result);
		result = db.getDatabase().addRecord(objD2);
		assertTrue(result);
		
		// Create a map with the common name property of objD and objD2
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByPropertiesForUpdate function returns a list with only objD and objD2 in it
		List<DBObject> res = db.getDatabase().findObjectsByPropertiesForUpdate(DBObject.class, properties);
		assertEquals(2, res.size());
		assertTrue(res.contains(objD));
		assertTrue(res.contains(objD2));
	}
}
