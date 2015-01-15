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
import eu.sqooss.service.logging.Logger;


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
     * Get the logger used by the DB service, to log something DB specific. 
     * This is the prefered method for DAOs to log things.
     */
    public Logger logger();
    
    /**
     * Get the object responsible for all the session management operations
     * like starting, committing and rolling back a database transaction.
     * See {@link eu.sqooss.service.db.DBSessionManager}} for more details 
     * about the available operations.
     * @return the instance of the DBSessionManager
     */
    public DBSessionManager getSessionManager();
    
    /**
     * Returns a basic implementation of the QueryInterface used to perform
     * simple queries to the database, like adding, deleting and finding
     * records. For a complete list of the available operations, see
     * {@link eu.sqooss.service.db.QueryInterface}.
     * @return a basic QueryInterface instance
     */
    public QueryInterface getQueryInterface();
    
    /**
     * This function can be used to retrieve more extended implementations 
     * of the QueryInterface, for instance an HQLQueryInterface instance.
     * @param queryInterfaceType The class of the implementation to be used
     * as QueryInterface
     * @return an implementation of the QueryInterface interface
     */
    public <T> T getQueryInterface(Class<T> queryInterfaceType);
}

// vi: ai nosi sw=4 ts=4 expandtab
