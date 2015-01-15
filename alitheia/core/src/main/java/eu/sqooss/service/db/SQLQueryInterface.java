package eu.sqooss.service.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hibernate.QueryException;

public interface SQLQueryInterface {

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
	
}
