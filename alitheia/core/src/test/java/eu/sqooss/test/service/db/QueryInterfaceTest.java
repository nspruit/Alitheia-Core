package eu.sqooss.test.service.db;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.service.db.QueryInterface;

public abstract class QueryInterfaceTest {

	private static final String objNameA = "test-object-a";
	private static final String objNameB = "test-object-b";
	private static final String objNameC = "test-object-c";
	private static final String objNameD = "test-object-d";
	
	private static InMemoryDatabase db;
	
	protected InMemoryDatabase getDB() {
		return db;
	}
	
	protected abstract QueryInterface getQueryInterface();
	
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
		db.startTransaction();
	}
	
	@After
	public void closeTransaction() {
		db.stopTransaction();
	}
	
	@Test
	public void testAddRecord() {
		DBObject obj = new DBObject(objNameA);

		// Store the object and assert that it succeeded
		boolean result = getQueryInterface().addRecord(obj);
		assertTrue(result);

		// Retrieve 
		DBObject storedObj = db.getTestObject(DBObject.class, obj.getId());
		assertEquals(obj, storedObj);
	}
	
	@Test
	public void testAddRecord_no_active_session() {
		DBObject obj = new DBObject(objNameA);
		
		// Make sure there is no active transaction
		closeTransaction(); 

		// Store the object and assert that it succeeded
		boolean result = getQueryInterface().addRecord(obj);
		assertFalse(result);
		
		beginTransaction(); //necessary for closeTransaction() to succeed
	}
	
	@Test
	public void testAddRecord_multiple() {
		DBObject objA = new DBObject(objNameA);
		DBObject objB = new DBObject(objNameB);

		// Store both objects and assert that it succeeded
		boolean result = getQueryInterface().addRecord(objA);
		assertTrue(result);
		result = getQueryInterface().addRecord(objB);
		assertTrue(result);
		
		// Check that both objects have different IDs
		assertThat(objA.getId(), not(equalTo(objB.getId())));

		// Attempt to retrieve both objects
		DBObject storedObjA = db.getTestObject(DBObject.class, objA.getId());
		DBObject storedObjB = db.getTestObject(DBObject.class, objB.getId());
		assertEquals(objA, storedObjA);
		assertEquals(objB, storedObjB);
	}
	
	@Test
	public void testDeleteRecord() {
		DBObject obj = new DBObject(objNameA);

		// Store the object and assert that it succeeded
		boolean result = getQueryInterface().addRecord(obj);
		assertTrue(result);

		// Retrieve the object
		DBObject storedObj = db.getTestObject(DBObject.class, obj.getId());
		assertEquals(obj, storedObj);
		
		// Delete the record
		result = getQueryInterface().deleteRecord(obj);
		assertTrue(result);
		
		// Check whether it is actually deleted
		storedObj = db.getTestObject(DBObject.class, obj.getId());
		assertNull(storedObj);
	}
	
	@Test
	public void testDeleteRecord_no_active_session() {
		DBObject obj = new DBObject(objNameA);

		// Store the object and assert that it succeeded
		boolean result = getQueryInterface().addRecord(obj);
		assertTrue(result);

		// Retrieve the object
		DBObject storedObj = db.getTestObject(DBObject.class, obj.getId());
		assertEquals(obj, storedObj);
		
		// Make sure there is no active transaction
		closeTransaction(); 
				
		// Delete the record
		result = getQueryInterface().deleteRecord(obj);
		assertFalse(result);
		
		beginTransaction(); //necessary for closeTransaction() to succeed
	}
	
	@Test
	public void testDeleteRecord_multiple_adds_one_delete() {
		DBObject objA = new DBObject(objNameA);
		DBObject objB = new DBObject(objNameB);
		
		// Store both objects and assert that it succeeded
		boolean result = getQueryInterface().addRecord(objA);
		assertTrue(result);
		result = getQueryInterface().addRecord(objB);
		assertTrue(result);

		// Attempt to retrieve both objects
		DBObject storedObjA = db.getTestObject(DBObject.class, objA.getId());
		DBObject storedObjB = db.getTestObject(DBObject.class, objB.getId());
		assertEquals(objA, storedObjA);
		assertEquals(objB, storedObjB);
		
		// Delete the record for objA
		result = getQueryInterface().deleteRecord(objA);
		assertTrue(result);
		
		// Check whether only objA is deleted
		storedObjA = db.getTestObject(DBObject.class, objA.getId());
		assertNull(storedObjA);
		storedObjB = db.getTestObject(DBObject.class, objB.getId());
		assertEquals(objB, storedObjB);
	}

	@Test
	public void testFindObjectById_success(){
		DBObject objC = new DBObject(objNameC);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objC);
		assertTrue(result);
		
		// Check whether the findObjectById function returns the correct object
		DBObject storedObjC = (DBObject)getQueryInterface().findObjectById(DBObject.class, objC.getId());
		assertEquals(objC, storedObjC);
	}
	
	@Test
	public void testFindObjectById_no_active_session(){
		DBObject objC = new DBObject(objNameC);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objC);
		assertTrue(result);
		
		// Make sure there is no active transaction
		closeTransaction(); 
		
		// Check whether the findObjectById function returns null
		DBObject storedObjC = (DBObject)getQueryInterface().findObjectById(DBObject.class, objC.getId());
		assertNull(storedObjC);
		
		beginTransaction(); //necessary for closeTransaction() to succeed
	}
	
	@Test
	public void testFindObjectByIdForUpdate(){
		DBObject objD = new DBObject(objNameD);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		
		// Check whether the findObjectByIdForUpdate function returns the correct object
		DBObject storedObjD = (DBObject)getQueryInterface().findObjectByIdForUpdate(DBObject.class, objD.getId());
		assertEquals(objD, storedObjD);
	}
	
	@Test
	public void testFindObjectsByProperties_single_property(){
		DBObject objD = new DBObject(objNameD);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByProperties function returns a list with only objD in it
		List<DBObject> res = getQueryInterface().findObjectsByProperties(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByProperties_no_active_session(){
		DBObject objD = new DBObject(objNameD);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		
		// Make sure there is no active transaction
		closeTransaction(); 
				
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);		
		
		// Check whether the findObjectsByProperties function returns the empty list
		List<DBObject> res = getQueryInterface().findObjectsByProperties(DBObject.class, properties);
		assertEquals(Collections.emptyList(),res);
		
		beginTransaction(); //necessary for closeTransaction() to succeed
	}
	
	@Test
	public void testFindObjectsByProperties_multiple_properties(){
		DBObject objD = new DBObject(objNameD);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("id", objD.getId());
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByProperties function returns a list with only objD in it
		List<DBObject> res = getQueryInterface().findObjectsByProperties(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByProperties_multiple_results(){
		DBObject objD = new DBObject(objNameD);
		DBObject objD2 = new DBObject(objNameD);
		
		// Store objects and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		result = getQueryInterface().addRecord(objD2);
		assertTrue(result);
		
		// Create a map with the common name property of objD and objD2
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByProperties function returns a list with only objD and objD2 in it
		List<DBObject> res = getQueryInterface().findObjectsByProperties(DBObject.class, properties);
		assertEquals(2, res.size());
		assertTrue(res.contains(objD));
		assertTrue(res.contains(objD2));
	}
	
	@Test
	public void testFindObjectsByPropertiesForUpdate_single_property(){
		DBObject objD = new DBObject(objNameD);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByPropertiesForUpdate function returns a list with only objD in it
		List<DBObject> res = getQueryInterface().findObjectsByPropertiesForUpdate(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByPropertiesForUpdate_multiple_properties(){
		DBObject objD = new DBObject(objNameD);
		
		// Store object and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		
		// Create a map with properties of objD
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("id", objD.getId());
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByPropertiesForUpdate function returns a list with only objD in it
		List<DBObject> res = getQueryInterface().findObjectsByPropertiesForUpdate(DBObject.class, properties);
		assertEquals(1, res.size());
		assertEquals(objD, res.get(0));
	}
	
	@Test
	public void testFindObjectsByPropertiesForUpdate_multiple_results(){
		DBObject objD = new DBObject(objNameD);
		DBObject objD2 = new DBObject(objNameD);
		
		// Store objects and verify it succeeded
		boolean result = getQueryInterface().addRecord(objD);
		assertTrue(result);
		result = getQueryInterface().addRecord(objD2);
		assertTrue(result);
		
		// Create a map with the common name property of objD and objD2
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("name", objNameD);
		
		// Check whether the findObjectsByPropertiesForUpdate function returns a list with only objD and objD2 in it
		List<DBObject> res = getQueryInterface().findObjectsByPropertiesForUpdate(DBObject.class, properties);
		assertEquals(2, res.size());
		assertTrue(res.contains(objD));
		assertTrue(res.contains(objD2));
	}
	
}
