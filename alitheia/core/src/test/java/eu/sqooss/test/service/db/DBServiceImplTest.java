package eu.sqooss.test.service.db;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.service.logging.Logger;

public class DBServiceImplTest {

	private DBServiceImpl db;
	private Logger l;
	
	@Before
	public void setUp() {
		db = new DBServiceImpl();
		l = mock(Logger.class);
	}
	
	@Test
	public void testStartDBSessionUninitialised() {
		SessionFactory s = mock(SessionFactory.class);
		db.prepareForTest(s, false, l);
		
		boolean result = db.startDBSession();
		
		assertFalse(result);
		verifyNoMoreInteractions(s);
	}
	
	@Test
	public void testStartDBSessionTransactionFailure() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		when(ss.beginTransaction()).thenThrow(new HibernateException("Failed to start transaction"));
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.startDBSession();
		
		assertFalse(result);
		verify(ss, times(1)).beginTransaction();
	}

}
