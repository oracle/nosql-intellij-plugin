/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import java.util.Iterator;

import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.FieldGroup;
import oracle.nosql.model.schema.SchemaBuilder;
import oracle.nosql.model.schema.Table;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableResult;
import java.util.List;

/**
 * A thin generalization of connection to data store. <br>
 * This interface provides a {@link #query(String) declarative query execution}
 * and {@link #getSchemaBuilder() building data model} for different
 * {@link IConnectionProfile flavors} of database e.g cloud service or
 * on-premise installation.
 * <p>
 * The interface provides declarative behavior using query rather than any
 * imperative operation (to avoid any uber-generalization). <br>
 * Moreover, the interface does not specify the query result type and returns
 * the result as an untyped iterator.
 * 
 * @author pinaki poddar
 *
 */
public interface IConnection {
    /**
     * Gets a string to describe this connection. It is recommend to format the
     * string in URI syntax.
     * 
     * @return a string to represent the connection, possibly in URI format.
     * never null.
     */
    String getConnectionString();

    /**
     * Gets a facility to build a datamodel.
     * 
     * @return a builder never null
     */
    SchemaBuilder getSchemaBuilder();

    /**
     * Gets the profile that created this connection.
     * 
     * @return a profile. never null.
     */
    IConnectionProfile<?> getProfile();

    /**
     * Executes a query. The result is returned as an iterator to account for
     * large result set. The iterated elements are untyped because on-premise
     * database and cloud service interfaces use different metaphor for data.
     * <br>
     * It is expected that the iterator would be lazy to account for large
     * result set. But not mandated in this interface.
     * 
     * @param query a query string as validated by the underlying store.
     * @return an untyped iterator.
     */
    Iterator<?> query(String query);

    /**
     * Unwraps the connection to a concrete implementation.
     * 
     * @param t a connection implementation class e.g. NoSQLHandle/KVStore
     * wrapped by this receiver
     * @param <T> the underlying unwrapped connection type
     * @return a <em>direct</em> interface to underlying store
     * @throws ClassCastException if this connection can not be unwrapped
     */
    <T> T unwrap(Class<T> t) throws ClassCastException;

    /**
     * Deletes the given field from database.
     * 
     * @param field
     * @throws Exception
     */
    void deleteField(Field field) throws Exception;

    /**
     * Deletes the given index from database.
     * 
     * @param fieldGroup - index as FiledGroup
     * @throws Exception
     */
    void deleteIndex(FieldGroup fieldGroup) throws Exception;

    String showSchema(Table table) throws Exception;

    /**
     * Fetches the table limits of the given table
     * @param table
     * @throws Exception
     */
    TableLimits getTableLimits(Table table) throws Exception;

    void deleteRow(Table table, String jString) throws Exception;

    /**
     * Drops the given table from database.
     * 
     * @param table
     * @throws Exception
     */
    void dropTable(Table table) throws Exception;

    /**
     * Inserts the given row into the given table.
     * 
     * @param table
     * @param jsonRow - row in JSON format.
     * @param isUpdate - row should be present already or not
     * @throws Exception
     */
    void insertFromJson(Table table, String jsonRow, boolean isUpdate) throws Exception;

    /**
     * Creates the new index with given columns in the given table.
     * 
     * @param table
     * @param indexName - name of the index.
     * @param colNames - list of column names.
     * @throws Exception
     */
    void createIndex(Table table, String indexName, String[] colNames)
            throws Exception;

    /**
     * Creates a new index with the given DDL statement
     *
     * @param createIdxDdl - DDL statement
     * @throws Exception
     */

    void createIndexUsingDdl(String createIdxDdl)
            throws Exception;


    /**
     * Fetches the current create table DDL
     * @param table
     * @return Current create table DDL
     * @throws Exception
     */
    String showTableDdl(Table table) throws Exception;

    /**
     * Fetches all the fields on which index is defined
     * @param table
     * @param index (current index)
     * @return array of fields on which index is defined
     * @throws Exception
     */
    String[] getIndexFields(Table table, String index) throws Exception;

    /**
     * Fetches the data for a particular key from the database
     * @param key
     * @param table
     * @return  MapValue which include data for that particular key
     * @throws Exception
     */
    MapValue getData(MapValue key,Table table) throws Exception;

    /**
     * Add a new column to the table.
     * 
     * @param table
     * @param flattenedColumn - syntax of the column
     * @throws Exception
     */
    void addNewColumn(Table table, String flattenedColumn) throws Exception;

    String fetchQueryPlan(String query) throws Exception;

    void ddlQuery(String query) throws Exception;

    void dmlQuery(String query) throws Exception;

    /**
     * Creates new table in the database.
     * 
     * @param tableName - name of the table.
     * @param query - create table query.
     * @param readKB - read limit in KBps.
     * @param writeKB - write limit in KBps.
     * @param storageGB - storage space in GB.
     * @throws Exception
     */
    void createTable(String tableName,
            String query,
            int readKB,
            int writeKB,
            int storageGB) throws Exception;

    /**
     * Creates a child table.
     * @param tableName
     * @param query
     * @throws Exception
     */
    void createChildTable(String tableName, String query) throws Exception;

    /**
     * Adds table replica
     * @param tableName
     * @param replica name - the replication region
     * @param readUnits - read limit
     * @param writeUnits - write limit
     * @throws Exception
     */
    boolean addReplica(String tableName, String replicaName,int readUnits, int writeUnits) throws Exception;

    /**
     * Drops table replica
     * @param tableName
     * @param replicas - list of the replication regions to be dropped
     * @throws Exception
     */
    boolean dropReplicas(String tableName, List<String> replicas) throws Exception;

    /**
     * Check if the table has been replicated or not
     * @param table
     * @throws Exception
     */
    boolean isReplicated(Table table) throws Exception;

    /**
     * Fetches the replicas of the table
     * @param table
     * @throws Exception
     */
    List<String> getReplicas(Table table) throws Exception;

    /**
     * Unfreezes the Schema of the table
     * @param tableName
     * @throws Exception
     */
    boolean unfreezeSchema(String tableName) throws Exception;

    /**
     * Freezes the Schema of the table
     * @param tableName
     * @throws Exception
     */
    boolean freezeSchema(String tableName) throws Exception;

    /**
     * Checks if the schema of the table is frozen or not
     * @param tableName
     * @throws Exception
     */
    boolean isFreezed(String tableName) throws Exception;

    /**
     * Returns the System result for the corresponding System request
     * @param query
     * @throws Exception
     */
    public String systemQuery(String query);

    public boolean setTableLimits(Table table,TableLimits tableLimits) throws Exception;
//    String getSDKVersion();
}
