package eu.sqooss.service.db;

import org.hibernate.SessionFactory;

public interface QueryInterfaceFactory<T extends QueryInterface> {

	/**
	 * Constructs a new QueryInterface of type T, with references to internal
	 * database structures.
	 * @param dbService the database service
	 * @param sessionFactory the Hibernate session factory
	 * @param sessionValidation the database error handling interface
	 * @return a new QueryInterface
	 */
	public T build(DBService dbService, SessionFactory sessionFactory,
			DBSessionValidation sessionValidation);
	
}
