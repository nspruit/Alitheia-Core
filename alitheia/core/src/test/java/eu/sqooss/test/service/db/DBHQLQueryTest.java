package eu.sqooss.test.service.db;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.QueryException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DBHQLQueryTest {

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
		db.startTransaction();
	}
	
	@After
	public void closeTransaction() {
		db.stopTransaction();
	}
	
	@Test
	public void testDoHQL_selectNoObjects() {
		// Insert test object
		DBObject obj = new DBObject("object");
		db.addTestObject(obj);
		
		// Select a non-existent object
		List<?> objs = db.getHQLInterface().doHQL("select o from DBObject o where o.name = 'unknown'");
		assertThat(objs, is(empty()));
	}
	
	@Test
	public void testDoHQL_selectMultipleObjects() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		DBObject objC = new DBObject("other-object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		db.addTestObject(objC);
		
		// Select all objects with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getHQLInterface().doHQL("select o from DBObject o where o.name = 'object'");

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHQL_withParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("other-object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select second object using a parameter
		Map<String, Object> params = new HashMap<>();
		params.put("nameparam", objB.getName());
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getHQLInterface().doHQL(
				"select o from DBObject o where o.name = :nameparam", params);
		
		assertThat(objs, contains(objB));
	}
	
	@Test(expected = QueryException.class)
	public void testDoHQL_withMissingParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("other-object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Execute query without specifying all parameters
		db.getHQLInterface().doHQL("select o from DBObject o where o.name = :nameparam", new HashMap<String, Object>());
	}
	
	@Test
	public void testDoHQL_withZeroLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		List<?> objs = db.getHQLInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 0);

		assertThat(objs, is(empty()));
	}
	
	@Test
	public void testDoHQL_withSmallLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getHQLInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 1);

		assertThat(objs, hasSize(1));
		assertThat(objs.get(0), either(equalTo(objA)).or(equalTo(objB)));
	}
	
	@Test
	public void testDoHQL_withLargeLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getHQLInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 3);

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHQL_withNegativeLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getHQLInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), -1);

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHQL_withCollectionParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object-a");
		DBObject objB = new DBObject("object-b");
		DBObject objC = new DBObject("object-c");
		db.addTestObject(objA);
		db.addTestObject(objB);
		db.addTestObject(objC);
		
		// Select second object using a parameter
		@SuppressWarnings("rawtypes")
		Map<String, Collection> params = new HashMap<>();
		params.put("namelist", Arrays.asList(objA.getName(), objB.getName()));
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getHQLInterface().doHQL(
				"select o from DBObject o where o.name in (:namelist)", new HashMap<String, Object>(), params);
		
		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@SuppressWarnings("rawtypes")
	@Test(expected = QueryException.class)
	public void testDoHQL_withMissingCollectionParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object-a");
		DBObject objB = new DBObject("object-b");
		DBObject objC = new DBObject("object-c");
		db.addTestObject(objA);
		db.addTestObject(objB);
		db.addTestObject(objC);
		
		// Select second object using a parameter
		db.getHQLInterface().doHQL("select o from DBObject o where o.name in (:namelist)",
				new HashMap<String, Object>(), new HashMap<String, Collection>());
	}
	
	@Test(expected = QueryException.class)
	public void testExecuteUpdate_missingParameter() {
		// Insert test object
		DBObject objA = new DBObject("object");
		db.addTestObject(objA);
		
		// Update the name of the test object
		db.getHQLInterface().executeUpdate("update DBObject set object_name = :newname where object_id = :obj_id", null);
	}
	
	@Test
	public void testExecuteUpdate_oneObject() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Update the name of the test object
		Map<String, Object> params = new HashMap<>();
		params.put("newname", "changed");
		params.put("obj_id", objA.getId());
		int rows = db.getHQLInterface().executeUpdate("update DBObject set name = :newname where id = :obj_id", params);
		// Commit the change
		db.getDatabase().getSessionManager().commitDBSession();
		
		try {
			// Start a new session to read the changes
			db.getDatabase().getSessionManager().startDBSession();
			
			assertThat(rows, equalTo(1));
			assertThat(db.getTestObject(DBObject.class, objA.getId()).getName(), equalTo("changed"));
		} catch (Exception e) {
			throw e;
		} finally {
			db.getDatabase().getSessionManager().startDBSession();
			db.getHQLInterface().executeUpdate("delete from DBObject", null);
			db.getDatabase().getSessionManager().commitDBSession();
		}
	}
	
	@Test
	public void testExecuteUpdate_multipleObjects() {
		// Insert test objects
		DBObject objA = new DBObject("object-a");
		DBObject objB = new DBObject("object-b");
		DBObject objC = new DBObject("object-c");
		db.addTestObject(objA);
		db.addTestObject(objB);
		db.addTestObject(objC);
		
		// Update the name of the test object
		Map<String, Object> params = new HashMap<>();
		params.put("newname", "changed");
		int rows = db.getHQLInterface().executeUpdate("update DBObject set name = :newname", params);
		// Commit the change
		db.getDatabase().getSessionManager().commitDBSession();
		
		try {
			// Start a new session to read the changes
			db.getDatabase().getSessionManager().startDBSession();
			
			assertThat(rows, equalTo(3));
			assertThat(db.getTestObject(DBObject.class, objA.getId()).getName(), equalTo("changed"));
			assertThat(db.getTestObject(DBObject.class, objB.getId()).getName(), equalTo("changed"));
			assertThat(db.getTestObject(DBObject.class, objC.getId()).getName(), equalTo("changed"));
		} catch (Exception e) {
			throw e;
		} finally {
			db.getDatabase().getSessionManager().startDBSession();
			db.getHQLInterface().executeUpdate("delete from DBObject", null);
			db.getDatabase().getSessionManager().commitDBSession();
		}
	}
	
}
