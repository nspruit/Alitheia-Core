package eu.sqooss.test.service.db;

import java.io.Serializable;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.mockito.Mockito;

import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.impl.service.db.HQLQueryInterfaceImpl;
import eu.sqooss.service.db.*;
import eu.sqooss.service.logging.Logger;

public final class InMemoryDatabase {

	private SessionFactory sessionFactory;
	private DBServiceImpl db;
	
	private HQLQueryInterfaceImpl hqlInterface;
	
	public InMemoryDatabase(Class<?>[] annotatedClasses) {
		setUpDatabase(annotatedClasses);
		
		db = new DBServiceImpl();
		db.prepareForTest(sessionFactory, true, Mockito.mock(Logger.class));
		
		hqlInterface = new HQLQueryInterfaceImpl(db.getSessionManager(), (DBSessionValidation)db.getSessionManager(),
				sessionFactory, Mockito.mock(Logger.class));
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
	
	public void close() {
		sessionFactory.close();
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public DBServiceImpl getDatabase() {
		return db;
	}
	
	public HQLQueryInterfaceImpl getHQLInterface() {
		return hqlInterface;
	}
	
	public void addTestObject(Object obj) {
		sessionFactory.getCurrentSession().save(obj);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getTestObject(Class<T> clazz, Serializable id) {
		return (T)sessionFactory.getCurrentSession().get(clazz, id);
	}

	public void startTransaction() {
		sessionFactory.getCurrentSession().beginTransaction();
	}
	
	public void stopTransaction() {
		if (sessionFactory.getCurrentSession().getTransaction() != null &&
				sessionFactory.getCurrentSession().getTransaction().isActive())
			sessionFactory.getCurrentSession().getTransaction().rollback();
	}
	
	public static InMemoryDatabase createDefault() {
		return new InMemoryDatabase(new Class<?>[] {
				Bug.class,
				BugStatus.class,
				BugReportMessage.class,
				BugSeverity.class,
				BugPriority.class,
				BugResolution.class,
				MailingList.class,
				MailMessage.class,
				MailingListThread.class,
				ProjectVersion.class,
				ProjectFile.class,
				ProjectFileState.class,
				Developer.class,
				DeveloperAlias.class,
				Directory.class,
				Tag.class,
				Branch.class,
				StoredProject.class,
				StoredProjectConfig.class,
				ConfigurationOption.class,
				ClusterNode.class,
				Plugin.class,
				PluginConfiguration.class,
				Metric.class,
				MetricType.class,
				StoredProjectMeasurement.class,
				ProjectVersionMeasurement.class,
				ProjectFileMeasurement.class,
				MailingListThreadMeasurement.class,
				MailMessageMeasurement.class,
				OhlohDeveloper.class,
				ProjectVersionParent.class,
				NameSpace.class,
				ExecutionUnit.class,
				EncapsulationUnit.class,
				NameSpaceMeasurement.class,
				ExecutionUnitMeasurement.class,
				EncapsulationUnitMeasurement.class
		});
	}
	
}
