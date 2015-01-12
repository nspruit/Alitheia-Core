package eu.sqooss.test.service.db;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.service.logging.Logger;

public class DBTransactionTest {

	private DBServiceImpl db;
	private Logger l;
	
	@Before
	public void setUp() {
		db = new DBServiceImpl();
		l = mock(Logger.class);
	}
	
	@Test
	public void testStartDBSession_uninitialised() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		db.prepareForTest(s, false, l);
		
		boolean result = db.startDBSession();
		
		assertFalse(result);
		verifyNoMoreInteractions(s);
	}
	
	@Test
	public void testStartDBSession_beginTransactionFailure() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		when(ss.beginTransaction()).thenThrow(new HibernateException("Failed to start transaction"));
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.startDBSession();
		
		assertFalse(result);
		verify(ss).beginTransaction();
	}
	
	@Test
	public void testStartDBSession_success() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.startDBSession();
		
		assertTrue(result);
		verify(ss).beginTransaction();
	}
	
	@Test
	public void testIsDBSessionActive_uninitialised() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		db.prepareForTest(s, false, l);
		
		boolean result = db.isDBSessionActive();
		
		assertFalse(result);
		verifyNoMoreInteractions(s);
	}
	
	@Test
	public void testIsDBSessionActive_noTransaction() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.isDBSessionActive();
		
		assertFalse(result);
	}
	
	@Test
	public void testIsDBSessionActive_transactionException() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		when(ss.getTransaction()).thenThrow(new HibernateException("Failed to get transaction"));
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.isDBSessionActive();
		
		assertFalse(result);
		verify(ss).close();
	}
	
	@Test
	public void testIsDBSessionActive_transactionInactive() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		// Create an inactive transaction
		Transaction t = mock(Transaction.class);
		when(ss.getTransaction()).thenReturn(t);
		when(t.isActive()).thenReturn(false);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.isDBSessionActive();
		
		assertFalse(result);
	}
	
	@Test
	public void testIsDBSessionActive_true() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		// Create an active transaction
		Transaction t = mock(Transaction.class);
		when(ss.getTransaction()).thenReturn(t);
		when(t.isActive()).thenReturn(true);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.isDBSessionActive();
		
		assertTrue(result);
	}
	
	@Test
	public void testCommitDBSession_withoutActiveSession() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.commitDBSession();
		
		assertFalse(result);
	}

	@Test
	public void testCommitDBSession_commitException() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		// Prepare active transaction
		Transaction t = mock(Transaction.class);
		when(ss.getTransaction()).thenReturn(t);
		when(t.isActive()).thenReturn(true);
		doThrow(new HibernateException("Failed to commit transaction")).when(t).commit();
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.commitDBSession();
		
		assertFalse(result);
		verify(t).commit();
		verify(t).rollback();
	}
	
	@Test
	public void testCommitDBSession_success() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		// Prepare active transaction
		Transaction t = mock(Transaction.class);
		when(ss.getTransaction()).thenReturn(t);
		when(t.isActive()).thenReturn(true);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.commitDBSession();
		
		assertTrue(result);
		verify(t).commit();
		verify(t, atLeastOnce()).isActive();
		verifyNoMoreInteractions(t);
	}

	@Test
	public void testRollbackDBSession_withoutActiveSession() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.rollbackDBSession();
		
		assertFalse(result);
	}

	@Test
	public void testRollbackDBSession_rollbackException() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		// Prepare active transaction
		Transaction t = mock(Transaction.class);
		when(ss.getTransaction()).thenReturn(t);
		when(t.isActive()).thenReturn(true);
		doThrow(new HibernateException("Failed to commit transaction")).when(t).rollback();
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.rollbackDBSession();
		
		assertFalse(result);
		verify(t).rollback();
		verify(t, never()).commit();
	}
	
	@Test
	public void testRollbackDBSession_success() {
		// Prepare session
		SessionFactory s = mock(SessionFactory.class);
		Session ss = mock(Session.class);
		when(s.getCurrentSession()).thenReturn(ss);
		
		// Prepare active transaction
		Transaction t = mock(Transaction.class);
		when(ss.getTransaction()).thenReturn(t);
		when(t.isActive()).thenReturn(true);
		
		db.prepareForTest(s, true, l);
		
		boolean result = db.rollbackDBSession();
		
		assertTrue(result);
		verify(t).rollback();
		verify(t, never()).commit();
	}
	
}
