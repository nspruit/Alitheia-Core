package eu.sqooss.test.service.db.objects;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.*;
import eu.sqooss.test.service.db.InMemoryDatabase;

public class ConfigurationOptionTest {

	private static DBService db;
	
	@BeforeClass
	public static void initializeDatabase() {
		db = InMemoryDatabase.createDefault().getDatabase();
		AlitheiaCore ac = mock(AlitheiaCore.class);
		
		when(ac.getDBService()).thenReturn(db);
		
		AlitheiaCore.setTestInstance(ac);
	}
	
	@AfterClass
	public static void tearDown() {
		db.shutDown();
	}
	
	@Before
	public void beginTransaction() {
		db.getSessionManager().startDBSession();
	}
	
	@After
	public void closeTransaction() {
		db.getSessionManager().rollbackDBSession();
	}
	
	@Test
	public void testGetValues_nonExistentProject() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		db.addRecord(opt);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> values = cfgOpt.getValues(new StoredProject());
		
		assertThat(values, is(empty()));
	}
	
	@Test
	public void testSetValues_singleProjectNoValues() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp = new StoredProject("sp1");
		db.addRecord(opt);
		db.addRecord(sp);
		
		// Add no values
		opt.setValues(sp, Collections.<String> emptyList(), false);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> values = cfgOpt.getValues(sp);
		
		assertThat(values, is(empty()));
	}
	
	@Test
	public void testSetValues_singleProjectSomeValues() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp = new StoredProject("sp1");
		db.addRecord(opt);
		db.addRecord(sp);
		
		// Add some values
		List<String> values = Arrays.asList("valueA", "valueB");
		opt.setValues(sp, values, false);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> readValues = cfgOpt.getValues(sp);
		
		assertThat(readValues, containsInAnyOrder(values.toArray(new String[2])));
	}
	
	@Test
	public void testSetValues_singleProjectMultipleTimes() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp = new StoredProject("sp1");
		db.addRecord(opt);
		db.addRecord(sp);
		
		// Add some values
		List<String> values = Arrays.asList("valueA", "valueB");
		opt.setValues(sp, values, false);
		// Add some more values
		List<String> moreValues = Arrays.asList("valueC", "valueD");
		opt.setValues(sp, moreValues, false);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> readValues = cfgOpt.getValues(sp);
		
		assertThat(readValues, containsInAnyOrder("valueA", "valueB", "valueC", "valueD"));
	}
	
	@Test
	public void testSetValues_singleProjectMultipleTimesWithOverlap() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp = new StoredProject("sp1");
		db.addRecord(opt);
		db.addRecord(sp);
		
		// Add some values
		List<String> values = Arrays.asList("valueA", "valueB", "valueD");
		opt.setValues(sp, values, false);
		// Add some more values
		List<String> moreValues = Arrays.asList("valueC", "valueD");
		opt.setValues(sp, moreValues, false);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> readValues = cfgOpt.getValues(sp);
		
		assertThat(readValues, containsInAnyOrder("valueA", "valueB", "valueC", "valueD"));
	}
	
	@Test
	public void testSetValues_multipleProjectsSomeValues() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp1 = new StoredProject("sp1");
		StoredProject sp2 = new StoredProject("sp2");
		db.addRecord(opt);
		db.addRecord(sp1);
		db.addRecord(sp2);
		
		// Add some values
		List<String> values1 = Arrays.asList("valueA", "valueD");
		List<String> values2 = Arrays.asList("valueB", "valueC", "valueE");
		opt.setValues(sp1, values1, false);
		opt.setValues(sp2, values2, false);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> readValues1 = cfgOpt.getValues(sp1);
		List<String> readValues2 = cfgOpt.getValues(sp2);
		
		assertThat(readValues1, containsInAnyOrder(values1.toArray(new String[2])));
		assertThat(readValues2, containsInAnyOrder(values2.toArray(new String[3])));
	}
	
	@Test
	public void testSetValues_overwriteNoNewValues() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp = new StoredProject("sp1");
		db.addRecord(opt);
		db.addRecord(sp);
		
		// Add some values
		List<String> values = Arrays.asList("valueA", "valueB");
		opt.setValues(sp, values, false);
		// Overwrite with no values
		opt.setValues(sp, Collections.<String>emptyList(), true);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> readValues = cfgOpt.getValues(sp);
		
		assertThat(readValues, is(empty()));
	}
	
	@Test
	public void testSetValues_overwriteSomeNewValues() {
		// Create test objects
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		StoredProject sp = new StoredProject("sp1");
		db.addRecord(opt);
		db.addRecord(sp);
		
		// Add some values
		List<String> values = Arrays.asList("valueA", "valueB", "valueC");
		opt.setValues(sp, values, false);
		// Overwrite with some new values
		List<String> newValues = Arrays.asList("valueD", "valueB", "valueE");
		opt.setValues(sp, newValues, true);
		
		// Read the test object from the database, and look up its values
		ConfigurationOption cfgOpt = db.findObjectById(ConfigurationOption.class, opt.getId());
		List<String> readValues = cfgOpt.getValues(sp);
		
		assertThat(readValues, containsInAnyOrder(newValues.toArray(new String[3])));
	}
	
	@Test
	public void testFromKey_notFound() {
		// Create test object
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		db.addRecord(opt);
		
		// Look up a non-existent key
		ConfigurationOption found = ConfigurationOption.fromKey("unknown-opt");
		
		assertNull(found);
	}
	
	@Test
	public void testFromKey_success() {
		// Create test object
		ConfigurationOption opt = new ConfigurationOption("opt", "A sample option");
		db.addRecord(opt);
		
		// Look up a non-existent key
		ConfigurationOption found = ConfigurationOption.fromKey("opt");
		
		assertNotNull(found);
		assertEquals(found.getId(), opt.getId());
		assertEquals(found.getDescription(), opt.getDescription());
	}
	
}
