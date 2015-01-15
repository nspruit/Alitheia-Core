/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - 2010 - Organization for Free and Open Source Software,  
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

package eu.sqooss.service.db;

import eu.sqooss.core.AlitheiaCoreService;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.logging.Logger;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.QueryException;


/**
 * This is the service providing access to the Alitheia Database,
 * including project metadata, user management, metrics data...
 * 
 * The API includes methods for retrieving data access objects (DAO) by id or by properties,
 * and adding/deleting records in the database, and general-purpose querying methods
 * for lower-level database access.
 * Access and manipulation of the data is done directly through the DAOs in an object-oriented way.
 * 
 * All access to the DB service has to be done in the context of a session. You can see the session
 * as the connection to the database and the transaction for that connection.
 * The method startDBSession() initialises a session, while commitDBSession() and rollbackDBSession()
 * end the session, by committing or cancelling the changes respectively.
 * You can also query the current state of the session with the method isDBSessionActive().
 * 
 * All the methods in this interface are thread-safe, which means you can call these methods on the
 * same DBService object from different threads without needed to protect the access to the object.
 * Furthermore, each session is handled within the context of a thread. So if two different threads
 * have code that call startDBSession(), they will each start their own, and whatever they do during
 * the session will be isolated from the other. (ie. no DAO sharing, no changes visible accross threads...)
 * 
 * No exceptions are thrown by methods in this service. Notification of success or failure is achieved
 * through return values. Exception handling for the actual db access and Hibernate is all handled
 * internally, and all resources are guaranteed to be released properly if an error occurs.
 * All errors are automatically logged into Alitheia's log file. (see Logger service)
 * 
 * nb: the package eu.sqooss.service.db contains all the Alitheia predefined DAOs that are used by
 * the platform, but it is also possible to add your own DAOs in metrics installed by Alitheia.
 * See the implementation of the developer contribution metric 
 * (under metrics/contrib) for an example how to achieve that.
 * 
 * 
 * @author Romain Pokrzywka
 *
 */
public interface DBService extends AlitheiaCoreService {

    /**
     * Execute a complete SQL query to the database.
     * This allows low-level manipulation of the database contents outside of the DAO types.
     * To limit risks of SQL injection exploits, please do not execute queries like
     * <code>"SELECT * FROM " + tableName</code>.
     * If you need dynamic SQL queries, please use the overload with the params argument.
     * 
     * @param sql the sql query string
     * @return a list of records. If the query contains multiple columns,
     *          the results are returned in an instance of Object[]
     * @throws SQLException if the query is invalid or a database access error occurs
     * 
     * @see doSQL(String sql, Map<String, Object> params)
     * @deprecated
     */
    @Deprecated
    public List<?> doSQL(String sql)
        throws SQLException;
    
    /**
     * Execute a parameterized SQL query to the database.
     * This allows low-level manipulation of the database contents outside of the DAO types.
     * 
     * @param sql the sql query string
     * @param params the map of parameters to be substituted in the SQL query
     * @return a list of records. If the query contains multiple columns,
     *          the results are returned in an instance of Object[]
     * @throws SQLException if the query is invalid or a database access error occurs
     * @throws QueryException if some parameters are missing
     * @deprecated
     */
    @Deprecated
    public List<?> doSQL(String sql, Map<String, Object> params)
        throws SQLException, QueryException;
        
    /**
     * Execute a named stored procedure. Stored procedures in general should be
     * avoided as much as possible as they harm portability and this is why
     * this method is marked as deprecated. For the same reason this method only
     * returns an integer and not a cursor, as one should expect.
     * In some cases however, e.g. when doing large batch updates or when 
     * moving large volumes, stored procedures can speed up things. To maintain
     * portability, there must always be an alternative execution path that 
     * does not involve calling a stored procedure. 
     * 
     * @param sql The name of the procedure to call (case sensitive)
     * @param arglist Names for the stored procedure arguments, order must be the
     * same as in the stored procedure itself.
     * @param params The map of parameters to be substituted in the SQL query
     * @return The number of rows affected by the execution of the procedure.
     * @throws SQLException if the stored procedure execution fails for some reason.
     * @throws QueryException if some parameters are missing
     * @deprecated
     */
    @Deprecated
    public int callProcedure(String procName, List<String> arglist, Map<String, Object> params)
    	throws SQLException, QueryException;
    
    
    
    /**
     * Get the logger used by the DB service, to log something DB specific. 
     * This is the prefered method for DAOs to log things.
     */
    public Logger logger();
    
    
    
    public DBSessionManager getSessionManager();
    
    public QueryInterface getQueryInterface();
    
    public <T> T getQueryInterface(Class<T> queryInterfaceType);
}

// vi: ai nosi sw=4 ts=4 expandtab
