/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.connection;

import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.kv.StoreAccessTokenProvider;
import oracle.nosql.driver.ops.*;
import oracle.nosql.model.cloud.schema.CloudSchemaBuilder;
import oracle.nosql.model.connection.AbstractConnection;
import oracle.nosql.model.connection.IConnectionProfile;
import oracle.nosql.model.profiletype.Cloudsim;
import oracle.nosql.model.profiletype.Onprem;
import oracle.nosql.model.profiletype.PublicCloud;
import oracle.nosql.driver.ops.TableResult.State;
import oracle.nosql.driver.ops.SystemResult;
import oracle.nosql.driver.ops.SystemRequest;
import oracle.nosql.driver.RequestTimeoutException;
import oracle.nosql.driver.NoSQLException;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.driver.ops.AddReplicaRequest;
import oracle.nosql.driver.ops.DropReplicaRequest;
import oracle.nosql.driver.OperationThrottlingException;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.model.schema.*;
import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;


/**
 * A connection as a thin wrapper over
 * <code>oracle.nosql.driver.NoSQLHandle</code>.
 */
public class CloudConnection extends AbstractConnection {
    private final NoSQLHandle handle;
    static final int WAIT_MILLIS  = 120000;
    static final int DELAY_MILLIS = 2000;
    static final int SLEEP_MILLIS = 15000;

    /**
     * Creates a connection with handle to underlying database.
     * 
     * @param profile a handle to cloud service.
     */
    CloudConnection(IConnectionProfile<?> profile) {
        super(profile);
        NoSQLHandleConfig config = null;
        if(CloudConnectionProfile.class.isInstance(profile)) {
	        URL url = (URL) profile.getProperty(Cloudsim.PROPERTY_URL.getName());
	        String tenantId = (String) profile
	                .getProperty(Cloudsim.PROPERTY_TENANT.getName());
	        config = new NoSQLHandleConfig(url);
	        config.setAuthorizationProvider(
                    new ExampleAccessTokenProvider(tenantId));
        } else if (PublicCloudConnectionProfile.class.isInstance(profile)) {
            // Resetting the System property cloud connection if a user previously selected Onprem connection using SSL.
            System.setProperty("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStorePassword", "");
            SignatureProvider ap;
            String endpoint = (String) profile.getProperty(PublicCloud.PROPERTY_ENDPOINT.getName());
            String tenantId = (String) profile.getProperty(PublicCloud.PROPERTY_TENANTID.getName());
            String userId = (String) profile.getProperty(PublicCloud.PROPERTY_USERID.getName());
            String fingerprint = (String) profile.getProperty(PublicCloud.PROPERTY_FINGEPRINT.getName());
            String privateKey = (String) profile.getProperty(PublicCloud.PROPERTY_PRIVATEKEY.getName());
            String passphrase = (String) profile.getProperty(PublicCloud.PROPERTY_PASSPHRASE.getName());
            String compartment = (String) profile.getProperty(PublicCloud.PROPERTY_COMPARTMENT.getName());

    		config = new NoSQLHandleConfig(endpoint);

            ap = new SignatureProvider(tenantId,userId,fingerprint,new File(privateKey),passphrase.toCharArray());

    		config.setAuthorizationProvider(ap);

    		if(compartment != null && !compartment.trim().isEmpty()) {
                config.setDefaultCompartment(compartment);
            }
    		config.setRequestTimeout(15000);
        } else if (OnpremConnectionProfile.class.isInstance(profile)) {
            URL proxyURL = (URL) profile.getProperty(Onprem.PROPERTY_URL.getName());
            config = new NoSQLHandleConfig(proxyURL.toString());
            StoreAccessTokenProvider sap;
            String security = (String) profile
                    .getProperty(Onprem.SECURITY.getName());
            String namespace = (String) profile.getProperty(Onprem.PROPERTY_NAMESPACE.getName());
            if(security != null && security.equals("SSL")){
                String username = (String) profile
                        .getProperty(Onprem.USER_NAME.getName());
                String password = (String) profile
                        .getProperty(Onprem.PASSWORD.getName());
                String trustStore = (String) profile
                        .getProperty(Onprem.TRUST_STORE.getName());
                String passphrase = (String) profile
                        .getProperty(Onprem.TS_PASSPHRASE.getName());
                System.setProperty("javax.net.ssl.trustStore", trustStore);
                System.setProperty("javax.net.ssl.trustStorePassword", passphrase);
                sap = new StoreAccessTokenProvider(
                        username, password.toCharArray());
            } else {
                sap = new StoreAccessTokenProvider();
            }
            config.setAuthorizationProvider(sap);
            if(namespace != null && !namespace.trim().isEmpty()) {
                config.setDefaultNamespace(namespace);
            }
        }
        handle = NoSQLHandleFactory.createNoSQLHandle(config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> t) {
        if (NoSQLHandle.class.isAssignableFrom(t)) {
            return (T) handle;
        }
        throw new RuntimeException("can not unwrap " + this + " to " + t);
    }

    @Override
    public Iterator<?> query(String query) {
        QueryRequest request = new QueryRequest();
        request.setStatement(query);
        return new QueryIterator(handle, request);
    }

    @Override
    public String fetchQueryPlan(String query) {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        PrepareRequest pr = new PrepareRequest().setStatement(query).setGetQueryPlan(true);
        PrepareResult pres = nosqlHdl.prepare(pr);
        String qp = pres.getPreparedStatement().getQueryPlan();
        return qp;
    }

    @Override
    public String getConnectionString() {
        return getProfile().getConnectionString();
    }

    @Override
    public SchemaBuilder getSchemaBuilder() {
        return new CloudSchemaBuilder(this);
    }

    @Override
    public void deleteField(Field field) throws Exception {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        TableRequest tableReq;
        TableResult tableRes;
        String dropIdxDdl = "alter table " +
                field.getOwner().getName() +
                " (drop " +
                field.getName() +
                ")";
        tableReq = new TableRequest().setStatement(dropIdxDdl);
        tableRes = nosqlHdl.tableRequest(tableReq);
        tableRes = TableResult.waitForState(nosqlHdl,
                tableRes.getTableName(),
                TableResult.State.ACTIVE,
                5000,
                100);
    }

    @Override
    public void deleteIndex(FieldGroup fieldGroup) throws Exception {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        TableRequest tableReq;
        TableResult tableRes;
        String dropIdxDdl = "drop index if exists " +
                fieldGroup.getName() +
                " on " +
                fieldGroup.getTable().getName();
        tableReq = new TableRequest().setStatement(dropIdxDdl);
        tableRes = nosqlHdl.tableRequest(tableReq);
        tableRes = TableResult.waitForState(nosqlHdl,
                tableRes.getTableName(),
                TableResult.State.ACTIVE,
                5000,
                100);
    }

    @Override
    public void deleteRow(Table table, String jString) throws Exception {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        DeleteRequest delRequest = new DeleteRequest()
                .setKeyFromJson(jString, null)
                .setTableName(table.getName());
        DeleteResult del = nosqlHdl.delete(delRequest);
    }

    @Override
    public void dropTable(Table table) throws Exception {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        TableRequest tableReq;
        String dropTblDdl = "drop table if exists " + table.getName();
        tableReq = new TableRequest().setStatement(dropTblDdl);
        nosqlHdl.doTableRequest(tableReq,
                    60000, /* wait up to 60 sec */
                    1000); /* poll once per second */
    }

    @Override
    public String showSchema(Table table) throws Exception {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetTableRequest getTableRequest = new GetTableRequest().setTableName(table.getName());
        TableResult tableResult = nosqlHdl.getTable(getTableRequest);
        return tableResult.getSchema();
    }

    @Override
    public void insertFromJson(Table table, String jsonRow, boolean isUpdate) throws Exception {
        PutRequest putReq = new PutRequest().setValueFromJson(jsonRow, null).setTableName(table.getName());
        if (isUpdate) {
            putReq.setOption(PutRequest.Option.IfPresent);
        } else {
            putReq.setOption(PutRequest.Option.IfAbsent);
        }
        NoSQLHandle handle = unwrap(NoSQLHandle.class);
        PutResult putRes = handle.put(putReq);
        if (putRes.getVersion() == null) {
            throw new Exception("Insertion failed for :" + jsonRow);
        }
    }
    public MapValue getData(MapValue key,Table table)throws Exception{
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetRequest getReq = new GetRequest()
                .setKey(key)
                .setTableName(table.getName());
        GetResult getRes = nosqlHdl.get(getReq);
        MapValue mVal = getRes.getValue();
        if(getRes.getVersion()==null){
            throw new Exception("Fetch data failed!");
        }
        return mVal;
    }

    @Override
    public void createIndex(Table table, String indexName, String colNames[]) throws Exception {
        String createIdxDdl="";
        try {
            NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
            TableRequest tableReq;
            TableResult tableRes;
            String columns = "";
            int len = colNames.length;
            for (int i = 0; i < len - 1; i++) {
                columns += colNames[i] + ",";
            }
            columns += colNames[len - 1];
            createIdxDdl = "create index " + indexName + " on " + table.getName() + " (" + columns + ")";
            tableReq = new TableRequest().setStatement(createIdxDdl);
            tableRes = nosqlHdl.tableRequest(tableReq);
            tableRes.waitForCompletion(nosqlHdl, 120000, 1000);
            final State actTableState = tableRes.getTableState();
            final State expTableState = State.ACTIVE;

            if (actTableState != expTableState) {
                throw new IllegalStateException("Execution failed, statement=" + createIdxDdl +
                        ", expected table state: " + expTableState + ", actual table state: " + actTableState);
            }
        } catch (RequestTimeoutException rte) {
            final String msg = "Timeout in creating index=" + createIdxDdl + ", timeoutMs=" + 120000;
            throw new IllegalStateException(msg, rte);
        } catch (NoSQLException nse) {
            final String msg = "Failed to create index=" + createIdxDdl + ", error=" + nse;
            throw new IllegalStateException(msg, nse);
        }
    }

    @Override
    public boolean addReplica(String tableName, String replicaName,
                               int readUnits, int writeUnits) throws Exception {
        boolean status = false;

        try {
            NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);

                AddReplicaRequest addReplicaReq = new AddReplicaRequest()
                        .setTableName(tableName)
                        .setReplicaName(replicaName);

                if(readUnits > 0) {
                    addReplicaReq.setReadUnits(readUnits);
                }

                if(writeUnits > 0) {
                    addReplicaReq.setWriteUnits(writeUnits);
                }

                TableResult tableRes = nosqlHdl.addReplica(addReplicaReq);

                /*
                 * Wait for the table to become active.
                 * Table request is asynchronous. It's necessary to wait for an
                 * expected state to know when the operation has completed.
                 */
                tableRes.waitForCompletion(nosqlHdl,
                        WAIT_MILLIS,
                        DELAY_MILLIS);
                status = true;
                System.out.println("Successfully added replica " + replicaName);

        } catch (Exception e) {
            status = false;
            throw new Exception("Exception during addReplica: " + e.getMessage());
        }

        return status;
    }

    @Override
    public boolean dropReplicas(String tableName, List<String> replicas) throws Exception {
        boolean status = false;
        int noOfReplica = replicas.size();

        try {
            NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
            for(int i=0; i<noOfReplica; i++) {
                String replicaName = replicas.get(i);
                DropReplicaRequest dropReplicaReq = new DropReplicaRequest()
                        .setTableName(tableName)
                        .setReplicaName(replicaName);

                TableResult tableRes = nosqlHdl.dropReplica(dropReplicaReq);

                /*
                 * Wait for the table to become active.
                 * Table request is asynchronous. It's necessary to wait for an
                 * expected state to know when the operation has completed.
                 */
                tableRes.waitForCompletion(nosqlHdl,
                        WAIT_MILLIS,
                        DELAY_MILLIS);
                status = true;
                System.out.println("Successfully dropped replica " + replicaName);
            }
        } catch (Exception e) {
            status = false;
            throw new Exception("Exception during dropReplicas: " + e.getMessage());
        }

        return status;
    }

    @Override
    public boolean unfreezeSchema(String tableName) throws Exception {
        boolean status = false;
        String ddl = "alter table " + tableName + " unfreeze schema";
        try {
            NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
            TableResult res = tableOperation("unfreeze schema", tableName, nosqlHdl, ddl, null, true);
            status = true;
        } catch (Exception e) {
            throw new Exception("Exception during unfreezing schema for " + tableName + " : " + e.getMessage());
        }
        return status;
    }
    @Override
    public boolean freezeSchema(String tableName) throws Exception {
        boolean status = false;
        String ddl = "alter table " + tableName + " freeze schema";
        try {
            NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
            TableResult res = tableOperation("freeze schema", tableName, nosqlHdl, ddl, null, true);
            status = true;
        } catch (Exception e) {
            throw new Exception("Exception during freezing schema for " + tableName + " : " + e.getMessage());
        }
        return status;
    }

    private TableResult tableOperation(String op, String tableName, NoSQLHandle handle, String ddl, TableLimits limits, boolean wait) throws Exception {
        final int maxRetries = 13;
        String info = (ddl != null ? "ddl=" + ddl : "tableName=" + tableName);
        if (limits != null) {
            info += ", limits=" + limits;
        }

        TableResult tres = null;
        TableRequest tableRequest = new TableRequest().setStatement(ddl).setTableLimits(limits).setTimeout(15000);

        if (ddl == null) {
            tableRequest.setTableName(tableName);
        }

        System.out.println(op);

        System.out.println(info);
        int retry = 0;
        while (retry < maxRetries) {
            try {
                tres = handle.tableRequest(tableRequest);
                break;
            } catch (OperationThrottlingException ex) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                System.out.println("[retry=" + (++retry) + "] TableRequest: " + op);
            }
        }

        if (!wait) {
            return tres;
        }

        if (tres != null && tres.getTableName() != null) {
            tres.waitForCompletion(handle, WAIT_MILLIS, DELAY_MILLIS);
        }

        return tres;
    }

    @Override
    public void createIndexUsingDdl(String createIdxDdl) throws Exception {
        try {
            NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
            TableRequest tableReq;
            TableResult tableRes;
            tableReq = new TableRequest().setStatement(createIdxDdl);
            tableRes = nosqlHdl.tableRequest(tableReq);
            tableRes.waitForCompletion(nosqlHdl, 120000, 1000);
            final State actTableState = tableRes.getTableState();
            final State expTableState = State.ACTIVE;

            if (actTableState != expTableState) {
                throw new IllegalStateException("Execution failed, statement=" + createIdxDdl +
                        ", expected table state: " + expTableState + ", actual table state: " + actTableState);
            }
        } catch (RequestTimeoutException rte) {
            final String msg = "Timeout in creating index=" + createIdxDdl + ", timeoutMs=" + 120000;
            throw new IllegalStateException(msg, rte);
        } catch (NoSQLException nse) {
            final String msg = "Failed to create index=" + createIdxDdl + ", error=" + nse;
            throw new IllegalStateException(msg, nse);
        }
    }
    @Override
    public String showTableDdl(Table table) throws Exception
    {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetTableRequest getTableRequest = new GetTableRequest().setTableName(table.getName());
        TableResult tableResult = nosqlHdl.getTable(getTableRequest);
        return tableResult.getDdl();
    }

    public String[] getIndexFields(Table table, String index){
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetIndexesRequest request = new GetIndexesRequest().setIndexName(index).setTableName(table.getName());
        GetIndexesResult result = nosqlHdl.getIndexes(request);
        return result.getIndexes()[0].getFieldNames();
    }

    @Override
    public TableLimits getTableLimits(Table table) throws Exception{
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetTableRequest getTableRequest = new GetTableRequest().setTableName(table.getName());
        TableResult tableResult = nosqlHdl.getTable(getTableRequest);
        return tableResult.getTableLimits();
    }
    @Override
    public boolean setTableLimits(Table table,TableLimits tableLimits) throws Exception{
        NoSQLHandle handle = unwrap(NoSQLHandle.class);
        TableRequest userTableReq =
                new TableRequest().setTableName(table.getName()).setTableLimits(tableLimits);
        handle.doTableRequest(userTableReq,
                60000, /* wait up to 60 sec */
                1000); /* poll once per second */
        return true;
    }

    @Override
    public boolean isReplicated(Table table) throws Exception{
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetTableRequest getTableRequest = new GetTableRequest().setTableName(table.getName());
        TableResult tableResult = nosqlHdl.getTable(getTableRequest);
        return tableResult.isReplicated();
    }
    @Override
    public boolean isFreezed(String tableName) throws Exception{
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetTableRequest getTableRequest = new GetTableRequest().setTableName(tableName);
        TableResult tableResult = nosqlHdl.getTable(getTableRequest);
        return tableResult.isFrozen();
    }
    @Override
    public List<String> getReplicas(Table table) throws Exception{
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        GetTableRequest getTableRequest = new GetTableRequest().setTableName(table.getName());
        TableResult tableResult = nosqlHdl.getTable(getTableRequest);
        TableResult.Replica[] replicas =  tableResult.getReplicas();
        List<String> replicasName = new ArrayList<>();
        if(replicas!=null) {
            for (int i = 0; i < replicas.length; i++) {
                replicasName.add(replicas[i].getReplicaName());
            }
        }
        return replicasName;
    }

    @Override
    public void addNewColumn(Table table, String flattenedColumn)
            throws Exception {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        TableRequest tableReq;
        TableResult tableRes;
        String addNewColumnDdl = "alter table " +
                table.getName() +
                " (add " +
                flattenedColumn +
                ")";
        tableReq = new TableRequest().setStatement(addNewColumnDdl);
        tableRes = nosqlHdl.tableRequest(tableReq);
        tableRes = TableResult.waitForState(nosqlHdl,
                tableRes.getTableName(),
                TableResult.State.ACTIVE,
                5000,
                100);
    }
    @Override
    public String systemQuery(String query){
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        SystemRequest systemReq;
        SystemResult systemRes;
        systemReq = new SystemRequest().setStatement(query.toCharArray());
        systemRes=nosqlHdl.systemRequest(systemReq);
        systemRes.waitForCompletion(nosqlHdl,
                5000,
                100);
        return systemRes.getResultString();
    }

    @Override
    public void ddlQuery(String query) {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        TableRequest tableReq;
        TableResult tableRes;
        tableReq = new TableRequest().setStatement(query);
        tableRes = nosqlHdl.tableRequest(tableReq);
        tableRes = TableResult.waitForState(nosqlHdl,
                tableRes.getTableName(),
                TableResult.State.ACTIVE,
                5000,
                100);
    }

    @Override
    public void dmlQuery(String query) {
        NoSQLHandle nosqlHdl = unwrap(NoSQLHandle.class);
        QueryRequest queryReq;
        QueryResult queryRes;
        queryReq = new QueryRequest().setStatement(query);
        queryRes = nosqlHdl.query(queryReq);
    }

    @Override
    public void createTable(String tableName,
            String query,
            int readKB,
            int writeKB,
            int storageGB) throws Exception {
        NoSQLHandle handle = unwrap(NoSQLHandle.class);
        TableRequest userTableReq =
                new TableRequest().setStatement(query).setTableLimits(
                        new TableLimits(readKB, writeKB, storageGB));
        handle.doTableRequest(userTableReq,
                    60000, /* wait up to 60 sec */
                    1000); /* poll once per second */
    }
    public void createChildTable(String tableName,String query) throws Exception{
        NoSQLHandle handle = unwrap(NoSQLHandle.class);
        TableRequest userTableReq =
                new TableRequest().setStatement(query);
        handle.doTableRequest(userTableReq,
                60000, /* wait up to 60 sec */
                1000); /* poll once per second */
    }

    @Override
    public String getSDKVersion(){
        return NoSQLHandleConfig.class.getPackage().getImplementationVersion();
    }
}
