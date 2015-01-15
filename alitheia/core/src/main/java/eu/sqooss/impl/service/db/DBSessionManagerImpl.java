package eu.sqooss.impl.service.db;

import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBSessionManager;
import eu.sqooss.service.logging.Logger;

public class DBSessionManagerImpl implements DBSessionManager {

	private AtomicBoolean isInitialised = null;
	private Logger logger = null;
    private SessionFactory sessionFactory = null;

	public DBSessionManagerImpl(SessionFactory factory, Logger logger, AtomicBoolean isInit) {
		this.sessionFactory = factory;
		this.logger = logger;
		this.isInitialised = isInit;
	}

	@Override
	public boolean startDBSession() {
		// Boot time check
		if (isInitialised.get() == false) {
			return false;
		}

		if (isDBSessionActive()) {
			logger.debug("startDBSession() - a session was already started for that thread");
			return true;
		}

		Session s = null;
		try {
			s = sessionFactory.getCurrentSession();
			// logger.debug("startDBSession: " + s + "[hashcode=" + s.hashCode()
			// + ",open=" + s.isOpen() + "]");
			s.beginTransaction();
		} catch (HibernateException e) {
			logger.error("startDBSession() - error while initializing session: "
					+ e.getMessage());
			if (s != null) {
				try {
					s.close();
				} catch (HibernateException e1) {
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean commitDBSession() {
		if ( !checkSession() )
            return false;
        
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            //logger.debug("commitDBSession: " + s + "[hashcode=" + s.hashCode() + ",open=" + s.isOpen() + "]");
            s.getTransaction().commit();
        } catch (HibernateException e) {
            logger.error("commitDBSession() - error while committing transaction: " + e.getMessage());
            if ( s != null ) {
                // The docs say to do so
                try {
                    s.getTransaction().rollback();
                } catch (HibernateException e1) {
                    try {
                        s.close();
                    } catch (HibernateException e2) {
                    }
                }
            }
            return false;
        }
        return true;
	}

	@Override
	public boolean rollbackDBSession() {
		if ( !checkSession() )
            return false;
        
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            s.getTransaction().rollback();
        } catch (HibernateException e) {
            logger.error("commitDBSession() - error while rolling back transaction: " + e.getMessage());
            if ( s != null ) {
                try {
                    s.close();
                } catch (HibernateException e1) {
                }
            }
            return false;
        }
        return true;
	}

	@Override
	public boolean flushDBSession() {
		if ( !checkSession() )
            return false;
        
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            s.flush();
            s.clear();
        } catch (HibernateException e) {
            logger.error("flushDBSession() - error while flushing session: " + e.getMessage());
            if ( s != null ) {
                try {
                    s.close();
                } catch (HibernateException e1) {
                }
            }
            return false;
        }
        return true;
	}

	@Override
	public boolean isDBSessionActive() {
		//Boot time check
        if(isInitialised.get() == false) {
            return false;
        }
        
        Session s = null;
        try {
            s = sessionFactory.getCurrentSession();
            return s.getTransaction() != null && s.getTransaction().isActive();
        } catch (HibernateException e) {
            logger.error("isDBSessionActive() - error while checking session status: " + e.getMessage());
            if ( s != null ) {
                try {
                    s.close();
                } catch (HibernateException e1) {
                }
            }
            return false;
        }
	}

	@Override
	public <T extends DAObject> T attachObjectToDBSession(T obj) {
		if( !checkSession() )
            return null;

        try {
            Session s = sessionFactory.getCurrentSession();
            if ( s.contains(obj)) {
                return obj;
            } else {
                return (T) s.merge(obj);
            }
        } catch (HibernateException e) {
//            TODO: logExceptionAndTerminateSession(e);
            return null;
        }
	}
	
	private boolean checkSession() {
        if ( !isDBSessionActive() ) {
            logger.warn("Trying to call a DBService method without an active session");
            try {
                throw new Exception("No active session.");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

}
