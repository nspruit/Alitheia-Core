package eu.sqooss.service.db;

/**
 * Database service entity that manages the thread-based database sessions. 
 */
public interface DBSessionManager {

	/**
     * Starts a new work session with the DBService for the current thread.
     * This method should be called before any other method from DBService, to ensure all resources
     * are properly set up and ready, such as database connection, active transaction...
     * Only one session per thread can be active at a time, so calling startDBSession with a
     * previously started session in the same thread has no effect and will assume usage
     * of the existing session. (e.g. if a previous session was not closed properly)
     * 
     * This method is thread-safe, and it creates session for a specific thread only.
     * It will also start a database transaction, ensuring that only the current thread has access
     * to the database for the duration of the session, therefore simplifying concurrency issues.
     * 
     * @return true if the session was correctly started ;
     *         false if a session was already started for this thread, 
     *          or if the session couldn't be started
     */
    public boolean startDBSession();
    
    /**
     * Commits the changes made in the current work session into the database and closes the session,
     * also releasing the transaction lock on the database.
     * 
     * This method is thread-safe, and it will always close the current session (if any)
     * and release any lock on the database, even if an error occurs.
     * 
     * @return true if the commit was successful and the session correctly closed,
     *         false if there was no active session or if an error occured.
     */
    public boolean commitDBSession();
    
    /**
     * Closes the current work session without committing the changes into the database,
     * also releasing the transaction lock on the database.
     * 
     * Note that any DAOs loaded and modified during the session will NOT be reset to
     * their state at load-time. In other words, modifications to the DAOs are NOT cancelled,
     * however these modifications will not be persisted in the database.
     * 
     * This method is thread-safe, and it will always close the current session (if any)
     * and release any lock on the database, even if an error occurs.
     * 
     * @return true if the session was correctly closed,
     *         false if there was no active session or if an error occured.
     */
    public boolean rollbackDBSession();
    
    /**
     * Flush the current changes in the session to the database and clears the session cache.
     * Note that the transaction isn't committed though, so changes will only be visible
     * to the current session.
     * @return true if the session was correctly flushed,
     *         false if there was no active session or if an error occured.
     */
    public boolean flushDBSession();
    
    /**
     * Returns the state of the work session for the current thread.
     * @return true if a session was started and is still active,
     *         false otherwise
     */
    public boolean isDBSessionActive();
    
    /**
     * Attach a disconnected object to the current Session. If the corresponding
     * row exists, then the returned object will merge the persistent and 
     * the disconnected object fields. Preference will be given to the field
     * values of the detached object. If the detached object contains 
     * references to other DAOs, the attach operation will cascade.
     * 
     * WARNING : the attached DAO is the returned object, NOT the one you passed as argument !
     *  
     * @param obj the object to connect
     * @return the connected instance of the object
     */
    public <T extends DAObject> T attachObjectToDBSession(T obj);
	
}
