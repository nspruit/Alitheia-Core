package eu.sqooss.impl.service.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBSessionManager;
import eu.sqooss.service.db.HQLQueryInterface;
import eu.sqooss.service.logging.Logger;

public class HQLQueryInterfaceImpl implements HQLQueryInterface {

	private SessionFactory sessionFactory;
	private DBSessionManager sessionManager;
	private DBSessionValidation sessionValidation;
	private Logger logger;
	
	public HQLQueryInterfaceImpl(DBSessionManager sessionManager, DBSessionValidation sessionValidation,
			SessionFactory sessionFactory, Logger logger) {
		this.sessionManager = sessionManager;
		this.sessionValidation = sessionValidation;
		this.sessionFactory = sessionFactory;
		this.logger = logger;
	}
	
	public <T extends DAObject> T findObjectById(Class<T> daoClass, long id) {
        return doFindObjectById(daoClass, id, false);
    }
    
    public <T extends DAObject> T findObjectByIdForUpdate(Class<T> daoClass, long id) {
        return doFindObjectById(daoClass, id, true);
    }

    @SuppressWarnings("unchecked")
    private <T extends DAObject> T doFindObjectById(Class<T> daoClass, long id, boolean useLock) {
        if ( !sessionValidation.checkSession() )
            return null;
        
        try {
            Session s = sessionFactory.getCurrentSession();
            return (T) (useLock ? s.get(daoClass, id, LockMode.UPGRADE) : s.get(daoClass, id));
        } catch (HibernateException e) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            return null;
        }
    }

    public <T extends DAObject> List<T> findObjectsByProperties(Class<T> daoClass, Map<String,Object> properties) {
        return doFindObjectsByProperties(daoClass, properties, false);
    }

    public <T extends DAObject> List<T> findObjectsByPropertiesForUpdate(Class<T> daoClass, Map<String,Object> properties) {
        return doFindObjectsByProperties(daoClass, properties, true);
    }

    @SuppressWarnings("unchecked")
    private <T extends DAObject> List<T> doFindObjectsByProperties(Class<T> daoClass, Map<String,Object> properties, boolean useLock) {
        if( !sessionValidation.checkSession() )
            return Collections.emptyList();

        // TODO maybe check that the properties are valid (e.g. with java.bean.PropertyDescriptor)

        Map<String,Object> parameterMap = new HashMap<String,Object>();
        StringBuffer whereClause = new StringBuffer();
        for (String key : properties.keySet()) {
            whereClause.append( whereClause.length() == 0 ? " where " : " and " );
            // We use "foo" as the name of the object
            whereClause.append("foo" + "." + key + "=:_" + key );
            parameterMap.put( "_" + key, properties.get(key) );
        }
        try {
            // We use "foo" as the name of the object
            return (List<T>) doHQL( "from " + daoClass.getName() + " as foo " + whereClause, parameterMap, useLock );
        } catch (QueryException e) {
            logger.warn("findObjectsByProperties(): invalid properties map. Restarting session...");
            // Automatically restart a session
            // (just be careful with preloaded DAOs that become detached)
            sessionManager.startDBSession();
            return Collections.emptyList();
        }
    }

    public boolean addRecord(DAObject record) {
        ArrayList<DAObject> tmpList = new ArrayList<DAObject>(1);
        tmpList.add(record);
        return addRecords(tmpList);
    }

    public boolean deleteRecord(DAObject record) {
        ArrayList<DAObject> tmpList = new ArrayList<DAObject>(1);
        tmpList.add(record);
        return deleteRecords(tmpList);
    }

    public <T extends DAObject> boolean addRecords(List<T> records) {
        if( !sessionValidation.checkSession() )
            return false;

        DAObject lastRecord = null;
        try {
            Session s = sessionFactory.getCurrentSession();
            for (DAObject record : records) {
                lastRecord = record;
                s.save(record);				
            }
            lastRecord = null;
            s.flush();
            return true;
        } catch (HibernateException e) {
            if (lastRecord != null) {
                logger.error("Failed to add object "
                        + "[" + lastRecord.getClass().getName() + ":" + lastRecord.getId() + "]"
                        + " to the database: " + e.getMessage());
            }
            sessionValidation.logExceptionAndTerminateSession(e);
            return false;
        }
    }

    public <T extends DAObject> boolean deleteRecords(List<T> records) {
        if( !sessionValidation.checkSession() )
            return false;

        DAObject lastRecord = null;
        try {
            Session s = sessionFactory.getCurrentSession();
            for (DAObject record : records) {
                lastRecord = record;
                s.delete(record);
            }
            lastRecord = null;
            s.flush();
            return true;
        } catch (HibernateException e) {
            if (lastRecord != null) {
                logger.error("Failed to remove object "
                        + "[" + lastRecord.getClass().getName() + ":" + lastRecord.getId() + "]"
                        + " from the database: " + e.getMessage());
            }
            sessionValidation.logExceptionAndTerminateSession(e);
            return false;
        }
    }
    
    public List<?> doHQL(String hql)
        throws QueryException {
        return doHQL(hql, null, null, false, -1, -1);
    }

    public List<?> doHQL(String hql, Map<String, Object> params) 
        throws QueryException {
        return doHQL(hql, params, null, false, -1, -1);
    }

    public List<?> doHQL(String hql, Map<String, Object> params, int limit) 
        throws QueryException {
        return doHQL(hql, params, null, false, 0, limit);
    }

    public List<?> doHQL(String hql, Map<String, Object> params, boolean lockForUpdate) 
        throws QueryException {
        return doHQL(hql, params, null, lockForUpdate, -1, -1);
    }

    public List<?> doHQL(String hql, Map<String, Object> params,
            Map<String, Collection> collectionParams) 
        throws QueryException {
        return doHQL(hql, params, collectionParams, false, -1, -1);
    }
   
    public List<?> doHQL(String hql, Map<String, Object> params,
            Map<String, Collection> collectionParams, boolean lockForUpdate, int start, int limit) 
        throws QueryException {
        if ( !sessionValidation.checkSession() ) {
            return Collections.emptyList();
        }
        try {
            Session s = sessionFactory.getCurrentSession();
            Query query = s.createQuery(hql);
            if (params != null) {
                for ( String param : params.keySet() ) {
                    query.setParameter(param, params.get(param));
                }
            }
            if (collectionParams != null) {
                for ( String param : collectionParams.keySet() ) {
                    query.setParameterList(param, collectionParams.get(param));
                }
            }
            if (lockForUpdate) {
                query.setLockMode("foo", LockMode.PESSIMISTIC_WRITE);
            }
            if ( start >= 0 && limit >= 0 ) {
                query.setFirstResult(start);
                query.setMaxResults(limit);
            }
            return query.list();
        } catch ( QueryException e ) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            throw e;
        } catch( HibernateException e ) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            return Collections.emptyList();
        } catch (ClassCastException e) {
            // Throw a QueryException instead of forwarding the ClassCastException
            // it's more explicit
            QueryException ebis = new QueryException("Invalid HQL query parameter type: "
                                                    + e.getMessage(), e);
            sessionValidation.logExceptionAndTerminateSession(ebis);
            throw ebis;
        }
        
    }
    
    public int executeUpdate(String hql, Map<String, Object> params) throws QueryException {
        if (!sessionValidation.checkSession()) {
            return -1;
        }
        
        try {
            Session s = sessionFactory.getCurrentSession();
            Query query = s.createQuery(hql);
            if (params != null) {
                for (String param : params.keySet()) {
                    query.setParameter(param, params.get(param));
                }
            }
            
            return query.executeUpdate();
            
        } catch (QueryException e) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            throw e;
        } catch (HibernateException e) {
        	sessionValidation.logExceptionAndTerminateSession(e);
            return -1;
        } catch (ClassCastException e) {
            // Throw a QueryException instead of forwarding the ClassCastException
            // it's more explicit
            QueryException ebis = new QueryException(
                    "Invalid HQL query parameter type: " + e.getMessage(), e);
            sessionValidation.logExceptionAndTerminateSession(ebis);
            throw ebis;
        }
    }
}
