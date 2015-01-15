package eu.sqooss.service.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.QueryException;

public interface HQLQueryInterface {

	/**
     * Execute a complete HQL query to the database.
     * To limit risks of HQL injection exploits, please do not execute queries like
     * <code>"FROM " + objectClass</code>.
     * If you need dynamic HQL queries, please use the overload with the params argument.
     * 
     * @param hql the HQL query string
     * @return a list of {@link DAObject}, fetched with a read access lock in the database.
     *         If the query contains multiple columns,
     *         the results are returned in an instance of Object[].
     *         If the query is invalid or a database access error occurs,
     *         an empty list will be returned.
     *           
     * @throws QueryException if the query is invalid
     * 
     * @see doHQL(String, Map<String, Object>)
     */
    public List<?> doHQL(String hql)
        throws QueryException;
    
    /**
     * Execute a parameterized HQL query to the database.
     *
     * @param hql the HQL query string
     * @param params the map of parameters to be substituted in the HQL query
     * @return a list of {@link DAObject}, fetched with a read access lock in the database.
     *         If the query contains multiple columns,
     *         the results are returned in an instance of Object[].
     *         If the query is invalid or a database access error occurs,
     *         an empty list will be returned.
     *           
     * @throws QueryException if the query is invalid or if params contains invalid entries
     * 
     * @see doHQL(String, Map<String, Object>, Map<String,Collection>)
     */
    public List<?> doHQL(String hql, Map<String, Object> params)
        throws QueryException;

    /**
     * Execute a parameterized HQL query to the database.
     *
     * @param hql the HQL query string
     * @param params the map of parameters to be substituted in the HQL query
     * @param limit only retrieve the first n rows
     * @return a list of {@link DAObject}, fetched with a read access lock in the database.
     *         If the query contains multiple columns,
     *         the results are returned in an instance of Object[]
     *         If the query is invalid or a database access error occurs,
     *         an empty list will be returned.
     *           
     * @throws QueryException if the query is invalid or if params contains invalid entries
     * 
     * @see doHQL(String, Map<String, Object>, Map<String,Collection>)
     */
    public List<?> doHQL(String hql, Map<String, Object> params, int limit)
        throws QueryException;

    /**
     * Execute a parameterized HQL query to the database. The table whose rows
     * should be returned and locked must be aliased as 'foo' for the lock
     * mode to work, for example:
     * <pre>
     * <tt>select foo from Developer as foo, StoredProject sp where foo.project=sp...</tt>
     * </pre>
     * @param hql the HQL query string
     * @param params the map of parameters to be substituted in the HQL query
     * @param lockForUpdate if true, the generated SQL query will use a "SELECT ... FOR UPDATE"
     *        statement. Otherwise, a normal "SELECT" will be used. Only one table can be 
     *        locked per query. 
     * @return a list of {@link DAObject}, with a corresponding lock in the database.
     *         If the query contains multiple columns,
     *         the results are returned in an instance of Object[]
     *         If the query is invalid or a database access error occurs,
     *         an empty list will be returned.
     *           
     * @throws QueryException if the query is invalid or if params contains invalid entries
     * 
     * @see doHQL(String, Map<String, Object>, Map<String,Collection>)
     */
    public List<?> doHQL(String hql, Map<String, Object> params, boolean lockForUpdate)
        throws QueryException;

    /**
     * Execute a parameterized HQL query to the database.
     *
     * @param hql HQL query string
     * @param params the map of parameters to be substituted in the HQL query
     * @return a list of {@link DAObject}, fetched with a read access lock in the database.
     *         If the query contains multiple columns,
     *         the results are returned in an instance of Object[]
     *         If the query is invalid or a database access error occurs,
     *         an empty list will be returned.
     *           
     * @throws QueryException if the query is invalid or if params or collectionParams
     *                          contain invalid entries
     */
    public List<?> doHQL(String hql, Map<String, Object> params,
                          Map<String, Collection> collectionParams)
        throws QueryException;

    /**
     * Execute a parameterized HQL query to the database.
     * HQL is very similar to SQL, but differs in a variety of important ways.
     * See the hibernate documentation at
     * http://www.hibernate.org/hib_docs/reference/en/html/queryhql.html
     * for details. As a rule, you do not write 'SELECT *' but only
     * 'FROM <ClassName>' (note: not the table name, the @em class).
     *
     * The query string may contain named parameters, for which values
     * will be substituted from the params and lparams arguments to this
     * method. For parameters that expect a single datum, put the mapping
     * from name to an object in the params argument. List-based parameters
     * (for instance the allowable values in a "IN ( foo, ... )" clause)
     * may be placed in the lparams argument. Either may be null if there
     * are no paramaters of that kind.
     *
     * @param hql HQL query string
     * @param params the map of parameters to be substituted in the HQL query
     * @param lockForUpdate if true, the generated SQL query will use a "SELECT ... FOR UPDATE"
     *        statement. Otherwise, a normal "SELECT" will be used
     * @param start fetch results starting at the specified row
     * @param limit only retrieve the specified number of rows
     * @return a list of {@link DAObject}, with a corresponding lock in the database.
     *         If the query contains multiple columns,
     *         the results are returned in an instance of Object[]
     *         If the query is invalid or a database access error occurs,
     *         an empty list will be returned.
     *           
     * @throws QueryException if the query is invalid or if params or collectionParams
     *                          contain invalid entries
     */
    public List<?> doHQL(String hql,
                         Map<String, Object> params,
                         Map<String, Collection> collectionParams,
                         boolean lockForUpdate,
                         int start, int limit
                         )
        throws QueryException;
    
    /**
     * Executes a DML-type query. The query forms that HQL supports 
     * are the following:
     * <ul>
     *  <li>INSERT INTO ... SELECT ... </li>
     *  <li>UPDATE ... SET ... WHERE....</li>
     *  <li>DELETE ... WHERE</li>
     * </ul> 
     * 
     * @param hql The HQL statement to execute 
     * @param params the map of parameters to be substituted in the HQL query
     * @return The number of rows updated or deleted or -1 in case of error
     */
    public int executeUpdate(String hql, Map<String, Object> params);
	
}
