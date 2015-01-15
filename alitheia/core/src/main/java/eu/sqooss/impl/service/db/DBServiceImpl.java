/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 - 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package eu.sqooss.impl.service.db;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.osgi.framework.BundleContext;

import eu.sqooss.core.AlitheiaCoreService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.DBSessionManager;
import eu.sqooss.service.db.DBSessionValidation;
import eu.sqooss.service.db.HQLQueryInterface;
import eu.sqooss.service.db.QueryInterface;
import eu.sqooss.service.db.QueryInterfaceFactory;
import eu.sqooss.service.db.SQLQueryInterface;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.util.URIUtills;

/**
 * Implementation of the Database service, using Hibernate's Thread-based session handling
 * 
 * 
 * @author Romain Pokrzywka, Georgios Gousios
 * 
 */
public class DBServiceImpl implements DBService, AlitheiaCoreService {

    private static DBService instance;
    
    public static Map<String, String> drivers = new HashMap<String, String>();
    
    static {
        drivers.put("mysql", "com.mysql.jdbc.Driver");
        drivers.put("hsqldb", "org.hsqldb.jdbcDriver");
        drivers.put("postgres", "org.postgresql.Driver");
        drivers.put("h2", "org.h2.Driver");
    }
    
    public static Map<String, String> connString = new HashMap<String, String>();
    
    static {
        connString.put("mysql", "jdbc:mysql://<HOST>/<SCHEMA>?useUnicode=true&amp;connectionCollation=utf8_general_ci&amp;characterSetResults=utf8");
        connString.put("hsqldb", "jdbc:hsqldb:file:<SCHEMA>");
        connString.put("postgres", "jdbc:postgresql://<HOST>/<SCHEMA>");
        connString.put("h2", "jdbc:h2:<SCHEMA>");
    }

    public static Map<String, String> hbmDialects = new HashMap<String, String>();
    
    static {
        hbmDialects.put("mysql", "org.hibernate.dialect.MySQLInnoDBDialect");
        hbmDialects.put("hsqldb", "org.hibernate.dialect.HSQLDialect");
        hbmDialects.put("postgres", "org.hibernate.dialect.PostgreSQLDialect");
        hbmDialects.put("h2", "org.h2.Driver");
    }

    public static Map<String, String> conPools = new HashMap<String, String>();
    
    static {
        conPools.put("default", "org.hibernate.connection.DriverManagerConnectionProvider");
        conPools.put("c3p0", "org.hibernate.connection.C3P0ConnectionProvider");
    }
    
    private static final String DB = "eu.sqooss.db";
    private static final String DB_HOST = "eu.sqooss.db.host";
    private static final String DB_SCHEMA = "eu.sqooss.db.schema";
    private static final String DB_USERNAME = "eu.sqooss.db.user";
    private static final String DB_PASSWORD = "eu.sqooss.db.passwd";
    private static final String DB_CONPOOL = "eu.sqooss.db.conpool";
    
    private Logger logger = null;
    private SessionFactory sessionFactory = null;
    private BundleContext bc = null;
    private AtomicBoolean isInitialised = new AtomicBoolean(false);
    private Properties conProp = new Properties();
    private DBSessionManager sessionManager = null;
    private DBSessionValidation sessionValidation = null;
    
    private Map<Class<? extends QueryInterface>, Class<? extends QueryInterfaceFactory<?>>>
			queryInterfaceFactories = new HashMap<>();
    private Map<Class<? extends QueryInterface>, QueryInterface> queryInterfaces = new HashMap<>();
    
    private boolean getJDBCConnection() {
        String driver = conProp.getProperty("hibernate.connection.driver_class");
        try {
            Driver d = (Driver)Class.forName(driver).newInstance();
            DriverManager.registerDriver(d);
            logger.info("Created instance of JDBC driver " + driver);
        } catch (InstantiationException e) {
            logger.error("Unable to instantiate the JDBC driver " + driver
                    + " : " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            logger.error("Unable to find JDBC driver " + driver);
            return false;
        } catch (IllegalAccessException e) {
            logger.error("Not allowed to access the JDBC driver " + driver);
            return false;
        } catch (SQLException e) {
            logger.error("Failed to register driver " + driver);
            sessionValidation.logSQLException(e);
            return false;
        }
        
        try {
            Connection c = DriverManager.getConnection(
                    conProp.getProperty("hibernate.connection.url"),
                    conProp.getProperty("hibernate.connection.username"),
                    conProp.getProperty("hibernate.connection.password"));
            c.setAutoCommit(false);
            c.close();
            return true;
        } catch (SQLException e) {
            logger.error("Unable to connect to DB URL " +
                    conProp.getProperty("hibernate.connection.url"));
            sessionValidation.logSQLException(e);
            return false;
        }
    }

    private boolean initHibernate(URL configFileURL) {
        
        logger.info("Initializing Hibernate with URL <" + configFileURL + ">");
        if (configFileURL == null) {
            logger.warn("Ignoring null URL.");
            return false;
        }
        try {
            Configuration c = new AnnotationConfiguration().configure(configFileURL); 
            // c now holds the configuration from hibernate.cfg.xml, need
            // to override some of those properties.            
            for(Object s : conProp.keySet()) {
                c.setProperty(s.toString(), conProp.getProperty(s.toString()));
            }
            
			// Get the list of eu.sqo-oss.metrics.* jars and add them to the
			// config
			String osgiInst = System.getProperty("osgi.install.area");
			List<String> dirsToSearch = new ArrayList<String>();
			if (osgiInst != null) {
				dirsToSearch.add(osgiInst);
				dirsToSearch.add(osgiInst + "/..");
				dirsToSearch.add(osgiInst + "/../bundles");
			} else {
				logger.warn("couln't resolve OSGi install property to a " +
						"directory on disk :" + osgiInst + ". Custom DAOs " +
						"from metrics bundles won't be initialized.");
			}
			
			List<String> inited = new ArrayList<String>();
			
            for (String dir : dirsToSearch) {

            	URI uri = URIUtills.toURI(dir);
            	if (uri == null) {
            		logger.warn("Error constructing URI for path: " + dir);
            	}

            	File searchDir = new File(uri);

            	logger.debug("Searching for plug-ins in " + searchDir.getCanonicalPath());
                
                if ( searchDir.exists() && searchDir.isDirectory() ) {
                    File[] metricsJars = searchDir.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.startsWith("eu.sqooss.metrics")  && name.endsWith(".jar");
                        }
                    });
                    for( File jarFile: metricsJars ) {
                        logger.debug("found metric bundle \"" + jarFile.getName() + "\", examining for custom DAOs");
                        
                        if (inited.contains(jarFile.getName())) {
                        	logger.debug("Skipping already initialised plug-in " + jarFile.getName());
                        	continue;
                        }
                        
                        c.addJar(jarFile);
                        inited.add(jarFile.getName());
                    }
                } 
            }
            sessionFactory = c.buildSessionFactory();
            
            DBSessionManagerImpl ssm = new DBSessionManagerImpl(sessionFactory, logger, isInitialised);
            sessionManager = ssm;
            sessionValidation = ssm;
            
            if (sessionFactory == null)
                return false;
        } catch (Throwable e) {
            logger.error("Failed to initialize Hibernate: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void preloadFactories() {
    	registerQueryInterface(QueryInterface.class, HQLQueryInterfaceImpl.Factory.class);
    	registerQueryInterface(HQLQueryInterface.class, HQLQueryInterfaceImpl.Factory.class);
    	registerQueryInterface(SQLQueryInterface.class, SQLQueryInterfaceImpl.Factory.class);
    }
    
    public DBServiceImpl() { }
    
    public DBServiceImpl(Properties p, URL configFileURL, Logger l) { 
        this.conProp = p;
        this.logger = l;
        initHibernate(configFileURL);
        preloadFactories();
        isInitialised.compareAndSet(false, true);
        instance = this;
    }
    
    public static DBService getInstance() {
        if (instance == null)
            instance = new DBServiceImpl();
        return instance;
    }
    
    public Logger logger() {
        return this.logger;
    } 

    @Override
    public boolean startUp() {
        String db  = bc.getProperty(DB).toLowerCase();
        String cs = connString.get(db);
        cs = cs.replaceAll("<HOST>", bc.getProperty(DB_HOST));
        cs = cs.replaceAll("<SCHEMA>", bc.getProperty(DB_SCHEMA));
            
        conProp.setProperty("hibernate.connection.driver_class",  drivers.get(db));
        conProp.setProperty("hibernate.connection.url", cs);
        conProp.setProperty("hibernate.connection.username", bc.getProperty(DB_USERNAME));
        conProp.setProperty("hibernate.connection.password", bc.getProperty(DB_PASSWORD));
        conProp.setProperty("hibernate.connection.dialect",  hbmDialects.get(db));
        conProp.setProperty("hibernate.connection.provider_class", conPools.get(bc.getProperty(DB_CONPOOL)));
        
        if (!getJDBCConnection()) {
            logger.error("DB service got no JDBC connectors.");
            return false;
        }
        
        if(!initHibernate(bc.getBundle().getResource("hibernate.cfg.xml")))
            return false;
        
        isInitialised.compareAndSet(false, true);
        return true; 
    }

    @Override
    public void shutDown() {
    	logger.info("Shutting down database service");
    	sessionFactory.close();
    }

	@Override
	public void setInitParams(BundleContext bc, Logger l) {
		this.bc = bc;
        this.logger = l;
	}
	
	@Override
	public DBSessionManager getSessionManager() {
		return sessionManager;
	}
	
	@Override
	public QueryInterface getQueryInterface() {
		return getQueryInterface(QueryInterface.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends QueryInterface> T getQueryInterface(Class<T> queryInterfaceType) {
		if (!queryInterfaces.containsKey(queryInterfaceType)) {
			if (!queryInterfaceFactories.containsKey(queryInterfaceType)) {
				logger.error("Requesting QueryInterface of unknown type: " + queryInterfaceType.getName());
				return null;
			}
			
			try {
				// Lazy initialization of QueryInterfaces by using a factory pattern
				QueryInterfaceFactory<T> factory = (QueryInterfaceFactory<T>)
						queryInterfaceFactories.get(queryInterfaceType).newInstance();
				T qi = factory.build(this, sessionFactory, sessionValidation);
				
				queryInterfaces.put(queryInterfaceType, qi);
			} catch (InstantiationException | IllegalAccessException e) {
				logger.error("Failed to instantiate QueryInterface: ", e);
				e.printStackTrace();
				return null;
			}
		}
		
		return (T) queryInterfaces.get(queryInterfaceType);
	}
	
	@Override
	public <T extends QueryInterface> void registerQueryInterface(Class<T> queryInterfaceType,
			Class<? extends QueryInterfaceFactory<? extends T>> factoryType) {
		// Override any existing interface
		if (queryInterfaces.containsKey(queryInterfaceType))
			queryInterfaces.remove(queryInterfaceType);
		
		queryInterfaceFactories.put(queryInterfaceType, factoryType);
	}
	
	/**
	 * Prepare the DB service for testing by injecting a custom SessionFactory.
	 * Note: this should NOT be used in production code.
	 * 
	 * @param s a custom SessionFactory object
	 * @param setInitialised true if the DBServiceImpl should be considered initialised
	 * @param l the logger to use for the DB service
	 */
	public void prepareForTest(SessionFactory s, boolean setInitialised, Logger l) {
		this.sessionFactory = s;
		this.logger = l;
		
		DBSessionManagerImpl ssm = new DBSessionManagerImpl(sessionFactory, logger, isInitialised);
		this.sessionManager = ssm;
		this.sessionValidation = ssm;
		preloadFactories();
		
		isInitialised.set(setInitialised);
	}
}

//vi: ai nosi sw=4 ts=4 expandtab
