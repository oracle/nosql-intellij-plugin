/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.cloud.connection;

import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.model.util.LazyIteratorChain;

import java.util.Iterator;

/**
 * An iterator of query result. The results are fetched lazily from underlying
 * query engine. <br>
 * A database query may return large number of results and hence query result
 * set is accessed by iterator rather than as a collection. However, the total
 * size of the result set is not known a priori. <br>
 * The results are fetched in batch. The batch size is determined by the
 * request.
 * 
 * @author pinaki poddar
 *
 */
class QueryIterator extends LazyIteratorChain<MapValue> {
    private final NoSQLHandle con;
    private final QueryRequest request;

    /**
     * Creates an iterator from given connection and request.
     * 
     * @param con a connection. must not be null.
     * @param request a request. must not be null.
     */
    public QueryIterator(NoSQLHandle con, QueryRequest request) {
        if (con == null) {
            throw new IllegalArgumentException(
                    "can not query with null connection");
        }
        if (request == null) {
            throw new IllegalArgumentException(
                    "can not query with null request");
        }
        this.con = con;
        this.request = request;
    }

    /**
     * Gets the next batch of results as an iterator. The iteration is
     * terminated by returning null when iterationCount is not the first one
     * and continuation key is null.
     */
    @Override
    protected Iterator<MapValue> nextIterator(int iterationCount) {
        if(request.isDone() && iterationCount > 0) {
            return null;
        }
        // fetch from database
        QueryResult result = con.query(request);
        return result.getResults().iterator();
    }
}
