package eu.sqooss.test.service.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.mockito.Mockito;

import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.service.logging.Logger;

public final class InMemoryDatabase {

	private SessionFactory sessionFactory;
	private DBServiceImpl db;
	
	public InMemoryDatabase(Class<?>[] annotatedClasses) {
		setUpDatabase(annotatedClasses);
		
		db = new DBServiceImpl();
		db.prepareForTest(sessionFactory, true, Mockito.mock(Logger.class));
	}
	
	public void close() {
		sessionFactory.close();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public DBServiceImpl getDatabase() {
		return db;
	}
	
	private void setUpDatabase(Class<?>[] annotatedClasses) {
		// setup the session factory
		AnnotationConfiguration configuration = new AnnotationConfiguration();
		for (Class<?> annotatedClass : annotatedClasses)
			configuration.addAnnotatedClass(annotatedClass);
		configuration.setProperty("hibernate.dialect",
				"org.hibernate.dialect.H2Dialect");
		configuration.setProperty("hibernate.connection.driver_class",
				"org.h2.Driver");
		configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:");
		configuration.setProperty("hibernate.hbm2ddl.auto", "create");
		configuration.setProperty("hibernate.current_session_context_class", "thread");
		 
		sessionFactory = configuration.buildSessionFactory();
	}
	
}
