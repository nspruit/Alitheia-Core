package eu.sqooss.service.db;

import org.hibernate.SessionFactory;

public interface QueryInterfaceFactory<T extends QueryInterface> {

	public T build(DBService dbService, SessionFactory sessionFactory,
			DBSessionValidation sessionValidation);
	
}
