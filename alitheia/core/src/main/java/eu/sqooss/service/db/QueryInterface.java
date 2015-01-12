package eu.sqooss.service.db;

import java.util.List;
import java.util.Map;

public interface QueryInterface {

	/**
     * A generic query method to retrieve a single DAObject subclass using its identifier.
     * The return value is parameterized to the actual type of DAObject queried
     * so no downcast is needed.
     * @param daoClass the actual class of the DAObject. 
     * @param id the DAObject's identifier
     * @return the DAOObject if a match for the class and the identifier was found in the database,
     *          or null otherwise or if a database access error occured
     */
    public <T extends DAObject> T findObjectById(Class<T> daoClass, long id);

    /**
     * A generic query method to retrieve a single DAObject subclass using its identifier and
     * acquire a pessimistic row-level database lock on it.
     * This results in an SQL query with the form "SELECT ... FOR UPDATE".
     * You may use this method to ensure that no other session can modify the returned object
     * while the current session is active. This can help avoiding database-level deadlocks
     * when multiple sessions access and modify the same table in parallel.
     * The return value is parameterized to the actual type of DAObject queried
     * so no downcast is needed.
     * @param daoClass the actual class of the DAObject. 
     * @param id the DAObject's identifier
     * @return the DAOObject if a match for the class and the identifier was found in the database,
     *          or null otherwise or if a database access error occured
     */
    public <T extends DAObject> T findObjectByIdForUpdate(Class<T> daoClass, long id);

    /**
     * A generic query method to retrieve a list of DAObjects of a same subclass
     * matching a set of properties.
     * The returned list contains the objects matching <b>all</b> of the properties specified.
     * It is parameterized to the actual type of DAObject queried so no downcast is needed.
     * The map key should be the property name as a string, and the value should be a value
     * with a matching type for the property. For example, if a class has a String property
     * called name (ie. a getName()/setName() accessor pair), then you would use "name" as
     * the map key and a String object as the map value.
     * If any property in the map isn't valid (either an unknown name or a value of the wrong type)
     * the call will fail and an empty list will be returned.
     * It uses its own session.
     * 
     * @param daoClass the actual class of the DAObjects
     * @param properties a map of property name/value pairs corresponding to properties
     *          of the DAObject subclass
     * @return a list of DAObjects matching the class and the set of properties,
     *          possibly empty if no match was found in the database or if the properties map
     *          contains invalid entries or if a database access error occured
     */
    public <T extends DAObject> List<T> findObjectsByProperties(Class<T> daoClass,
                                                                Map<String,Object> properties );

    /**
     * A generic query method to retrieve a list of DAObjects of a same subclass
     * matching a set of properties and acquire a pessimistic row-level database lock
     * on each returned object in the list.
     * This results in an SQL query with the form "SELECT ... FOR UPDATE".
     * You may use this method to ensure that no other session can modify the returned objects
     * while the current session is active. This can help avoiding database-level deadlocks
     * when multiple sessions access and modify the same table in parallel.
     * The returned list contains the objects matching <b>all</b> of the properties specified.
     * It is parameterized to the actual type of DAObject queried so no downcast is needed.
     * The map key should be the property name as a string, and the value should be a value
     * with a matching type for the property. For example, if a class has a String property
     * called name (ie. a getName()/setName() accessor pair), then you would use "name" as
     * the map key and a String object as the map value.
     * If any property in the map isn't valid (either an unknown name or a value of the wrong type)
     * the call will fail and an empty list will be returned.
     * It uses its own session.
     * 
     * @param daoClass the actual class of the DAObjects
     * @param properties a map of property name/value pairs corresponding to properties
     *          of the DAObject subclass
     * @return a list of DAObjects matching the class and the set of properties,
     *          possibly empty if no match was found in the database or if the properties map
     *          contains invalid entries or if a database access error occured
     */
    public <T extends DAObject> List<T> findObjectsByPropertiesForUpdate(Class<T> daoClass,
                                                                Map<String,Object> properties );

    /**
     * Add a new record to the database, including all the associations the record may contain.
     * 
     * @param record the record to persist into the database
     * @return true if the record insertion succeeded, false otherwise
     */
    public boolean addRecord(DAObject record);
    
    /**
     * Add multiple new records to the database.
     * 
     * @param records the list of records to persist into the database
     * @return true if all the record insertions succeeded, false otherwise
     */
    public <T extends DAObject> boolean addRecords(List<T> records);

    /**
     * Delete an existing record from the database.
     *
     * @param record the record to remove from the database
     * @return true if the record deletion succeeded, false otherwise
     */
    public boolean deleteRecord(DAObject record);
    
    /**
     * Delete multiple existing records from the database.
     * 
     * @param records the list of records to remove from the database
     * @return true if all the record deletions succeeded, false otherwise
     */
    public <T extends DAObject> boolean deleteRecords(List<T> records);
	
}
