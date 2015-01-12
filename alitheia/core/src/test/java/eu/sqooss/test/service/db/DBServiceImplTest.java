package eu.sqooss.test.service.db;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.impl.service.db.HQLQueryInterfaceImpl;
import eu.sqooss.impl.service.db.SQLQueryInterfaceImpl;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.DBSessionValidation;
import eu.sqooss.service.db.HQLQueryInterface;
import eu.sqooss.service.db.QueryInterface;
import eu.sqooss.service.db.QueryInterfaceFactory;
import eu.sqooss.service.db.SQLQueryInterface;

public class DBServiceImplTest {
	
	private static InMemoryDatabase db;
	private static DBServiceImpl dbs;
	
	@BeforeClass
	public static void setUp() {
		db = new InMemoryDatabase(new Class<?>[] { DBObject.class });
		dbs = db.getDatabase();
	}
	
	@AfterClass
	public static void tearDown() {
		db.close();
	}
	
	@Test
	public void testGetSessionManager_notNull() {
		assertNotNull(dbs.getSessionManager());
	}
	
	@Test
	public void testGetQueryInterface_withoutParameter() {
		assertThat(dbs.getQueryInterface(), instanceOf(HQLQueryInterfaceImpl.class));
	}
	
	@Test
	public void testGetQueryInterface_queryInterface() {
		assertThat(dbs.getQueryInterface(QueryInterface.class), instanceOf(HQLQueryInterfaceImpl.class));
	}
	
	@Test
	public void testGetQueryInterface_hqlQueryInterface() {
		assertThat(dbs.getQueryInterface(HQLQueryInterface.class), instanceOf(HQLQueryInterfaceImpl.class));
	}
	
	@Test
	public void testGetQueryInterface_sqlQueryInterface() {
		assertThat(dbs.getQueryInterface(SQLQueryInterface.class), instanceOf(SQLQueryInterfaceImpl.class));
	}
	
	@Test
	public void testGetQueryInterface_unknownInterface() {
		assertNull(dbs.getQueryInterface(UnknownQueryInterface.class));
	}
	
	@Test
	public void testRegisterQueryInterface() {
		dbs.registerQueryInterface(NewQueryInterface.class, NewQueryInterfaceFactory.class);
		
		assertThat(dbs.getQueryInterface(NewQueryInterface.class), instanceOf(NewQueryInterface.class));
	}
	
	private static interface UnknownQueryInterface extends QueryInterface { }
	
	public static interface NewQueryInterface extends QueryInterface { }
	
	public static class NewQueryInterfaceFactory implements QueryInterfaceFactory<NewQueryInterface> {

		@Override
		public NewQueryInterface build(DBService dbService, SessionFactory sessionFactory,
				DBSessionValidation sessionValidation) {
			return mock(NewQueryInterface.class);
		}
		
	}
	
}
