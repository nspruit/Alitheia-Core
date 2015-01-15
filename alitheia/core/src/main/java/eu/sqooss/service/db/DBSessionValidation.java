package eu.sqooss.service.db;

import java.sql.SQLException;

public interface DBSessionValidation {

	/**
     * Returns the state of the work session for the current thread, while logging
     * any access to an inactive session. Should be called whenever a session is
     * required to be active.
     * 
     * @return true if a session was started and is still active,
     *         false otherwise
     */
    public boolean checkSession();
    
    /**
     * 
     * @param e an exception triggering the termination of the active session
     */
    public void logExceptionAndTerminateSession(Exception e);
    
    /**
     * 
     * @param e the exception to be logged
     */
    public void logSQLException(SQLException e);
	
}
