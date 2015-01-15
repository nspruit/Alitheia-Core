package eu.sqooss.impl.service.db;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.DBSessionManager;
import eu.sqooss.service.db.DBSessionValidation;
import eu.sqooss.service.db.QueryInterface;
import eu.sqooss.service.db.QueryInterfaceFactory;
import eu.sqooss.service.db.SQLQueryInterface;

public class SQLQueryInterfaceImpl implements SQLQueryInterface {
	
	private DBService dbService;
	private QueryInterface qi;
	private DBSessionManager sessionManager;
	private DBSessionValidation sessionValidation;
	private SessionFactory sessionFactory;
	
	public SQLQueryInterfaceImpl(DBService dbService, DBSessionValidation sesVal,
			SessionFactory sesFac) {
		this.sessionManager = dbService.getSessionManager();
		this.sessionValidation = sesVal;
		this.sessionFactory = sesFac;
		this.dbService = dbService;
		this.qi = null;
	}

	@Override
	@Deprecated
	public List<?> doSQL(String sql) throws SQLException {
		return doSQL(sql, null);
	}

	@Override
	@Deprecated
	public List<?> doSQL(String sql, Map<String, Object> params)
			throws SQLException, QueryException {
		boolean autoSession = !sessionManager.isDBSessionActive();
        try {
            Session s = sessionFactory.getCurrentSession();
            if (autoSession) {
                s.beginTransaction();
            }
            Query query = s.createSQLQuery(sql);
            if ( params != null ) {
                for ( String param : params.keySet() ) {
                    query.setParameter(param, params.get(param));
                }
            }
            List<?> result = query.list();
            if (autoSession) {
                s.getTransaction().commit();
            }
            return result;
        } catch ( JDBCException e ) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            throw e.getSQLException();
        } catch ( QueryException e ) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            throw e;
        } catch( HibernateException e ) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            return Collections.emptyList();
        }
	}

	@Override
	@Deprecated
	public int callProcedure(String procName, List<String> args,
			Map<String, Object> params) throws SQLException, QueryException {
		boolean autoSession = !sessionManager.isDBSessionActive();
		StringBuilder sql = new StringBuilder("call " + procName + "(");
		
		for (String arg : args) {
			sql.append(":").append(arg).append(",");
		}
		sql.deleteCharAt(sql.lastIndexOf(",")).append(")");
		
		try {
			Session s = sessionFactory.getCurrentSession();
			if (autoSession) {
				s.beginTransaction();
			}
			Query query = s.createSQLQuery(sql.toString());
			if (params != null) {
				for (String param : params.keySet()) {
					query.setParameter(param, params.get(param));
				}
			}
			int result = query.executeUpdate();
			if (autoSession) {
				s.getTransaction().commit();
			}
			return result;
		} catch (JDBCException e) {
			sessionValidation.logExceptionAndTerminateSession(e);
			throw e.getSQLException();
		} catch (QueryException e) {
			sessionValidation.logExceptionAndTerminateSession(e);
			throw e;
		} catch (HibernateException e) {
			sessionValidation.logExceptionAndTerminateSession(e);
			throw e;
		}
	}

	@Override
	public <T extends DAObject> T findObjectById(Class<T> daoClass, long id) {
		return getQI().findObjectById(daoClass, id);
	}

	@Override
	public <T extends DAObject> T findObjectByIdForUpdate(Class<T> daoClass,
			long id) {
		return getQI().findObjectByIdForUpdate(daoClass, id);
	}

	@Override
	public <T extends DAObject> List<T> findObjectsByProperties(
			Class<T> daoClass, Map<String, Object> properties) {
		return getQI().findObjectsByProperties(daoClass, properties);
	}

	@Override
	public <T extends DAObject> List<T> findObjectsByPropertiesForUpdate(
			Class<T> daoClass, Map<String, Object> properties) {
		return getQI().findObjectsByPropertiesForUpdate(daoClass, properties);
	}

	@Override
	public boolean addRecord(DAObject record) {
		return getQI().addRecord(record);
	}

	@Override
	public <T extends DAObject> boolean addRecords(List<T> records) {
		return getQI().addRecords(records);
	}

	@Override
	public boolean deleteRecord(DAObject record) {
		return getQI().deleteRecord(record);
	}

	@Override
	public <T extends DAObject> boolean deleteRecords(List<T> records) {
		return getQI().deleteRecords(records);
	}
	
	private QueryInterface getQI() {
		if (qi == null)
			qi = dbService.getQueryInterface();
		return qi;
	}

	public static class Factory implements QueryInterfaceFactory<SQLQueryInterface> {

		@Override
		public SQLQueryInterface build(DBService dbService,	SessionFactory sessionFactory,
				DBSessionValidation sessionValidation) {
			return new SQLQueryInterfaceImpl(dbService, sessionValidation, sessionFactory);
		}
    }
}
