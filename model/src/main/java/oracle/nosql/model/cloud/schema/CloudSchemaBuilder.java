/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.schema;

import oracle.nosql.driver.IndexNotFoundException;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.ops.GetTableRequest;
import oracle.nosql.driver.ops.ListTablesRequest;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.driver.ops.TableRequest;
import oracle.nosql.driver.ops.TableResult;
import oracle.nosql.driver.ops.TableResult.State;
import oracle.nosql.model.cloud.connection.CloudConnection;
import oracle.nosql.model.schema.Datamodel;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Index;
import oracle.nosql.model.schema.Schema;
import oracle.nosql.model.schema.SchemaBuilder;
import oracle.nosql.model.schema.Table;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A schema builder to build datamodel lazily from database schema information.
 * 
 * @author pinaki poddar
 *
 */
public class CloudSchemaBuilder extends AbstractInmemorySchemaBuilder
        implements
        SchemaBuilder {
    private final CloudConnection connection;
    final int MAX_WAIT_TIME = 5000;
    final int POLLING_INTERVAL = 100;

    public CloudSchemaBuilder(CloudConnection con) {
        if (con == null) {
            throw new IllegalArgumentException(
                    "can not create builder with null connection");
        }
        this.connection = con;
    }

    /**
     * Creates a new table with given DDL.
     * 
     * 
     * @param ddl a CREATE TABLE query
     * @param readKB the data limit on read request
     * @param writeKB the data limit on write request
     * @param storageGB the data limit on total data volume
     * @param cb a function that would be called when the database table
     * reaches target state.
     * @param targetState waits for given target state. If not null, this call
     * blocks until the database table reaches the target state.
     * 
     */
    void createTable(Table table,
            int readKB,
            int writeKB,
            int storageGB,
            State targetState) throws Exception {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        String ddl = table.getCreateDDL();
        TableRequest userTableReq =
                new TableRequest().setStatement(ddl).setTableLimits(
                        new TableLimits(readKB, writeKB, storageGB));
        TableResult result = handle.tableRequest(userTableReq);
        if (targetState != null) {
            waitForTableState(handle, result.getTableName(), targetState);
            switch (targetState) {
            case ACTIVE:
                GetTableRequest getTable = new GetTableRequest();
                getTable.setTableName(table.getName());
                result = handle.getTable(getTable);
                parse(result.getSchema(), table);
                break;
            case DROPPED:
                // getSchema().removeTable(result.getTableName());
                break;
            default:
                break;
            }
        }
    }

    public void
            createTable(Table table, int readKB, int writeKB, int storageGB)
                    throws Exception {
        createTable(table, readKB, writeKB, storageGB, State.ACTIVE);
    }

    public boolean createIndex(Index index) throws Exception {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        TableRequest tableReq =
                new TableRequest().setStatement(index.getCreateDDL());
        TableResult tableRes = handle.tableRequest(tableReq);
        waitForTableState(handle,
                tableRes.getTableName(),
                State.ACTIVE);
        return true;
    }

    public boolean deleteIndex(Index index) {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        TableRequest tableReq =
                new TableRequest().setStatement(index.getDropDDL());
        TableResult tableRes = handle.tableRequest(tableReq);
        waitForTableState(handle,
                tableRes.getTableName(),
                State.ACTIVE);
        return true;
    }

    public boolean createField(Field field) throws Exception {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        TableRequest tableReq =
                new TableRequest().setStatement(field.getCreateDDL());
        TableResult tableRes = handle.tableRequest(tableReq);
        waitForTableState(handle,
                tableRes.getTableName(),
                State.ACTIVE);
        return true;
    }

    public boolean deleteField(Field field) throws Exception {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        String dropIdxDdl = "alter table " +
                field.getTable().getName() +
                " (drop " +
                field.getName() +
                ")";
        TableRequest tableReq = new TableRequest();
        tableReq.setStatement(dropIdxDdl);
        TableResult tableRes = handle.tableRequest(tableReq);
        waitForTableState(handle,
                tableRes.getTableName(),
                State.ACTIVE);
        return true;
    }

    /**
     * Builds a datamodel of given name and with a schema of given name. The
     * schema is built lazily. The table information is not fetched other than
     * the names. The details of the the table is fetched only when a table is
     * {@link Schema#getTable(String) looked up} by name.
     */
    @Override
    public Datamodel build(String datamodelName, Object schemaName) {
        if (datamodelName == null) {
            throw new IllegalArgumentException(
                    "can not build datamodel with null name");
        }
        if (schemaName == null) {
            throw new IllegalArgumentException(
                    "can not build datamodel with null schema name");
        }
        Datamodel store = newDatamodel(datamodelName);
        Schema schema = newSchema(store, schemaName.toString(), true);
        schema.setSchemaBuilder(this);
        schema.recursiveRefresh();
        return store;
    }

    public String[] listTables() {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        ListTablesRequest request = new ListTablesRequest();
        String[] tableNames = handle.listTables(request).getTables();
        return tableNames;
    }

    void waitForTableState(NoSQLHandle handle,
            String tableName,
            State targetState) {
        int timeOut = 1000;
        int delayMs = 10;
        TableResult.waitForState(handle,
                tableName,
                targetState,
                timeOut,
                delayMs);
    }

    public boolean existsTable(String tableName) {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        GetTableRequest request = new GetTableRequest();
        request.setTableName(tableName);
        try {
            handle.getTable(request);
        } catch (IndexNotFoundException ex) {
            return false;
        }
        return true;
    }

    /**
     * Fetches details of given table from database.
     * 
     * @param table a table whose details to be refreshed from database
     * @return a table with fresh details
     * @throws IOException
     */
    public Table refresh(Table table) {
        NoSQLHandle handle = connection.unwrap(NoSQLHandle.class);
        GetTableRequest request = new GetTableRequest();
        request.setTableName(table.getName());
        TableResult result = handle.getTable(request);
        parse(result.getSchema(), table);
        return table;
    }

    public Schema refresh(Schema schema) {
        // Fetch all table names
        List<String> newTableNames = Arrays.asList(listTables());
        List<String> oldTableNames = schema.getTableNames();
        // Add table if it doesn't already exists
        for (String tableName : newTableNames) {
            if (!oldTableNames.contains(tableName)) {
                Table table = newTable(schema, tableName, true);
                schema.addTable(table, true);
            }
        }
        // Remove tables that don't exists any more
        for (String tableName : oldTableNames) {
            if (!newTableNames.contains(tableName)) {
                schema.removeTable(tableName);
            }
        }
        return schema;
    }

    @Override
    public Schema newSchema(String name) {
        return new SchemaImpl(name);
    }

    @Override
    public Table newTable(String name) {
        return new TableImpl(name);
    }
}
