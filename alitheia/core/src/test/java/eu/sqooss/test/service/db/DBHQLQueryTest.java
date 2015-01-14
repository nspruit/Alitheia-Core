package eu.sqooss.test.service.db;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.hibernate.QueryException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.test.service.db.objects.DBObject;

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
		
		// Select a non-existant object
		List<?> objs = db.getDatabase().doHQL("select o from DBObject o where o.name = 'unknown'");
		assertThat(objs, is(empty()));
	}
	
	@Test
	public void testDoHql_selectMultipleObjects() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		DBObject objC = new DBObject("other-object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		db.addTestObject(objC);
		
		// Select all objects with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getDatabase().doHQL("select o from DBObject o where o.name = 'object'");

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHql_withParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("other-object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select second object using a parameter
		Map<String, Object> params = new HashMap<>();
		params.put("nameparam", objB.getName());
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getDatabase().doHQL(
				"select o from DBObject o where o.name = :nameparam", params);
		
		assertThat(objs, contains(objB));
	}
	
	@Test(expected = QueryException.class)
	public void testDoHql_withMissingParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("other-object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Execute query without specifying all parameters
		db.getDatabase().doHQL("select o from DBObject o where o.name = :nameparam", new HashMap<String, Object>());
	}
	
	@Test
	public void testDoHql_withZeroLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		List<?> objs = db.getDatabase().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 0);

		assertThat(objs, is(empty()));
	}
	
	@Test
	public void testDoHql_withSmallLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getDatabase().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 1);

		assertThat(objs, hasSize(1));
		assertThat(objs.get(0), either(equalTo(objA)).or(equalTo(objB)));
	}
	
	@Test
	public void testDoHql_withLargeLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getDatabase().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 3);

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHql_withNegativeLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		db.addTestObject(objA);
		db.addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) db.getDatabase().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), -1);

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHql_withCollectionParameters() {
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
		List<DBObject> objs = (List<DBObject>) db.getDatabase().doHQL(
				"select o from DBObject o where o.name in (:namelist)", new HashMap<String, Object>(), params);
		
		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@SuppressWarnings("rawtypes")
	@Test(expected = QueryException.class)
	public void testDoHql_withMissingCollectionParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object-a");
		DBObject objB = new DBObject("object-b");
		DBObject objC = new DBObject("object-c");
		db.addTestObject(objA);
		db.addTestObject(objB);
		db.addTestObject(objC);
		
		// Select second object using a parameter
		db.getDatabase().doHQL("select o from DBObject o where o.name in (:namelist)",
				new HashMap<String, Object>(), new HashMap<String, Collection>());
	}
	
}
