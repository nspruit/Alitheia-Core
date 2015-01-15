package eu.sqooss.test.service.db;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.QueryException;
import org.junit.Test;

import eu.sqooss.service.db.HQLQueryInterface;
import eu.sqooss.service.db.QueryInterface;

public abstract class HQLQueryInterfaceTest extends QueryInterfaceTest {

	protected abstract HQLQueryInterface getHQLQueryInterface();
	
	@Override
	protected QueryInterface getQueryInterface() {
		return getHQLQueryInterface();
	}
	
	@Test
	public void testDoHQL_selectNoObjects() {
		// Insert test object
		DBObject obj = new DBObject("object");
		getDB().addTestObject(obj);
		
		// Select a non-existent object
		List<?> objs = getHQLQueryInterface().doHQL("select o from DBObject o where o.name = 'unknown'");
		assertThat(objs, is(empty()));
	}
	
	@Test
	public void testDoHQL_selectMultipleObjects() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		DBObject objC = new DBObject("other-object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		getDB().addTestObject(objC);
		
		// Select all objects with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) getHQLQueryInterface().doHQL("select o from DBObject o where o.name = 'object'");

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHQL_withParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("other-object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Select second object using a parameter
		Map<String, Object> params = new HashMap<>();
		params.put("nameparam", objB.getName());
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) getHQLQueryInterface().doHQL(
				"select o from DBObject o where o.name = :nameparam", params);
		
		assertThat(objs, contains(objB));
	}
	
	@Test(expected = QueryException.class)
	public void testDoHQL_withMissingParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("other-object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Execute query without specifying all parameters
		getHQLQueryInterface().doHQL("select o from DBObject o where o.name = :nameparam", new HashMap<String, Object>());
	}
	
	@Test
	public void testDoHQL_withZeroLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Select one object with name "object"
		List<?> objs = getHQLQueryInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 0);

		assertThat(objs, is(empty()));
	}
	
	@Test
	public void testDoHQL_withSmallLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) getHQLQueryInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 1);

		assertThat(objs, hasSize(1));
		assertThat(objs.get(0), either(equalTo(objA)).or(equalTo(objB)));
	}
	
	@Test
	public void testDoHQL_withLargeLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) getHQLQueryInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), 3);

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHQL_withNegativeLimit() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Select one object with name "object"
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) getHQLQueryInterface().doHQL(
				"select o from DBObject o where o.name = 'object'", new HashMap<String, Object>(), -1);

		assertThat(objs, containsInAnyOrder(objA, objB));
	}
	
	@Test
	public void testDoHQL_withCollectionParameters() {
		// Insert test objects
		DBObject objA = new DBObject("object-a");
		DBObject objB = new DBObject("object-b");
		DBObject objC = new DBObject("object-c");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		getDB().addTestObject(objC);
		
		// Select second object using a parameter
		@SuppressWarnings("rawtypes")
		Map<String, Collection> params = new HashMap<>();
		params.put("namelist", Arrays.asList(objA.getName(), objB.getName()));
		@SuppressWarnings("unchecked")
		List<DBObject> objs = (List<DBObject>) getHQLQueryInterface().doHQL(
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
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		getDB().addTestObject(objC);
		
		// Select second object using a parameter
		getHQLQueryInterface().doHQL("select o from DBObject o where o.name in (:namelist)",
				new HashMap<String, Object>(), new HashMap<String, Collection>());
	}
	
	@Test(expected = QueryException.class)
	public void testExecuteUpdate_missingParameter() {
		// Insert test object
		DBObject objA = new DBObject("object");
		getDB().addTestObject(objA);
		
		// Update the name of the test object
		getHQLQueryInterface().executeUpdate("update DBObject set object_name = :newname where object_id = :obj_id", null);
	}
	
	@Test
	public void testExecuteUpdate_oneObject() {
		// Insert test objects
		DBObject objA = new DBObject("object");
		DBObject objB = new DBObject("object");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		
		// Update the name of the test object
		Map<String, Object> params = new HashMap<>();
		params.put("newname", "changed");
		params.put("obj_id", objA.getId());
		int rows = getHQLQueryInterface().executeUpdate("update DBObject set name = :newname where id = :obj_id", params);
		// Commit the change
		getDB().getDatabase().getSessionManager().commitDBSession();
		
		try {
			// Start a new session to read the changes
			getDB().getDatabase().getSessionManager().startDBSession();
			
			assertThat(rows, equalTo(1));
			assertThat(getDB().getTestObject(DBObject.class, objA.getId()).getName(), equalTo("changed"));
		} catch (Exception e) {
			throw e;
		} finally {
			getDB().getDatabase().getSessionManager().startDBSession();
			getHQLQueryInterface().executeUpdate("delete from DBObject", null);
			getDB().getDatabase().getSessionManager().commitDBSession();
		}
	}
	
	@Test
	public void testExecuteUpdate_multipleObjects() {
		// Insert test objects
		DBObject objA = new DBObject("object-a");
		DBObject objB = new DBObject("object-b");
		DBObject objC = new DBObject("object-c");
		getDB().addTestObject(objA);
		getDB().addTestObject(objB);
		getDB().addTestObject(objC);
		
		// Update the name of the test object
		Map<String, Object> params = new HashMap<>();
		params.put("newname", "changed");
		int rows = getHQLQueryInterface().executeUpdate("update DBObject set name = :newname", params);
		// Commit the change
		getDB().getDatabase().getSessionManager().commitDBSession();
		
		try {
			// Start a new session to read the changes
			getDB().getDatabase().getSessionManager().startDBSession();
			
			assertThat(rows, equalTo(3));
			assertThat(getDB().getTestObject(DBObject.class, objA.getId()).getName(), equalTo("changed"));
			assertThat(getDB().getTestObject(DBObject.class, objB.getId()).getName(), equalTo("changed"));
			assertThat(getDB().getTestObject(DBObject.class, objC.getId()).getName(), equalTo("changed"));
		} catch (Exception e) {
			throw e;
		} finally {
			getDB().getDatabase().getSessionManager().startDBSession();
			getHQLQueryInterface().executeUpdate("delete from DBObject", null);
			getDB().getDatabase().getSessionManager().commitDBSession();
		}
	}

}
