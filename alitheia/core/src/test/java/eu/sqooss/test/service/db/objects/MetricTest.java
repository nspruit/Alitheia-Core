package eu.sqooss.test.service.db.objects;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.db.NameSpace;
import eu.sqooss.service.db.NameSpaceMeasurement;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.test.service.db.InMemoryDatabase;

public class MetricTest {

	private static DBService db;

	private static final String metric_mnemonic = "test-metric";
	private static final String test_project_name = "test-project";
	private static final String metric_type_string = "NAMESPACE";
	private static final String test_measurement_result = "test-result";

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
		db.startDBSession();
	}

	@After
	public void closeTransaction() {
		db.rollbackDBSession();
	}
	
	@Test
	public void testIsEvaluated(){
		// Create necessary objects
		Metric metr = new Metric();
		MetricType type = new MetricType();
		StoredProject project = new StoredProject(test_project_name);
		ProjectVersion version = new ProjectVersion(project);
		NameSpaceMeasurement meas = new NameSpaceMeasurement();
		NameSpace namespace = new NameSpace();
		
		// Set required properties of the objects
		type.setType(metric_type_string);
		metr.setMetricType(type);
		metr.setMnemonic(metric_mnemonic);
		namespace.setChangeVersion(version);
		meas.setMetric(metr);
		meas.setNamespace(namespace);
		meas.setResult(test_measurement_result);
		
		// Add the objects to the database
		db.addRecord(type);
		db.addRecord(metr);
		db.addRecord(project);
		db.addRecord(version);
		db.addRecord(namespace);
		db.addRecord(meas);
		
		// Call the method and check its result
		boolean result = metr.isEvaluated(project);
		assertTrue(result);
	}
	
	@Test
	public void testIsEvaluated_not_evaluated(){
		Metric metr = new Metric();
		MetricType type = new MetricType();
		type.setType(metric_type_string);
		metr.setMetricType(type);
		StoredProject project = new StoredProject(test_project_name);
		db.addRecord(type);
		db.addRecord(metr);
		db.addRecord(project);
		
		boolean result = metr.isEvaluated(project);
		assertFalse(result);
	}
	
	@Test
	public void testGetMetricByMnemonic_no_results(){
		// Add the metric to the database
		Metric obj = new Metric();
		db.addRecord(obj);
		
		// Retrieve metric based on mnemonic and check properties
		Metric result = Metric.getMetricByMnemonic(metric_mnemonic);
		assertNull(result);
	}
	
	@Test
	public void testGetMetricByMnemonic_single_result(){
		// Add the metric to the database
		Metric obj = new Metric();
		obj.setMnemonic(metric_mnemonic);
		db.addRecord(obj);
		
		// Retrieve metric based on mnemonic and check properties
		Metric result = Metric.getMetricByMnemonic(metric_mnemonic);
		assertNotNull(result);
		assertEquals(obj.getId(), result.getId());
		assertEquals(obj.getMnemonic(), result.getMnemonic());
		assertEquals(metric_mnemonic, result.getMnemonic());
	}
	
	@Test
	public void testGetMetricByMnemonic_multiple_results(){
		// Add the metrics to the database
		Metric obj1 = new Metric();
		Metric obj2 = new Metric();
		obj1.setMnemonic(metric_mnemonic);
		obj2.setMnemonic(metric_mnemonic);
		db.addRecord(obj1);
		db.addRecord(obj2);
		
		// Retrieve metric based on mnemonic and check properties
		Metric result = Metric.getMetricByMnemonic(metric_mnemonic);
		assertNotNull(result);
		assertEquals(metric_mnemonic, result.getMnemonic());
		assertThat(result, isOneOf(obj1,obj2));
	}
	
	@Test
	public void testGetAllMetrics_no_metrics(){
		// Retrieve all metrics and check properties
		List<Metric> results = Metric.getAllMetrics();
		assertThat(results, is(empty()));
	}
	
	@Test
	public void testGetAllMetrics_single_metric(){
		// Add the metric to the database
		Metric obj = new Metric();
		db.addRecord(obj);
		
		// Retrieve all metrics and check properties
		List<Metric> results = Metric.getAllMetrics();
		assertThat(results, is(not(empty())));
		assertEquals(1, results.size());
		assertEquals(obj, results.get(0));
	}
	
	@Test
	public void testGetAllMetrics_multiple_metrics(){
		// Add the metrics to the database
		Metric obj1 = new Metric();
		Metric obj2 = new Metric();
		db.addRecord(obj1);
		db.addRecord(obj2);
		
		// Retrieve all metrics and check properties
		List<Metric> results = Metric.getAllMetrics();
		assertThat(results, is(not(empty())));
		assertEquals(2, results.size());
		assertThat(results, contains(obj1,obj2));
	}
}
