/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

public abstract class AbstractConnection implements IConnection {
    private final IConnectionProfile<?> profile;
    private int maxRetry = 10;
    private int timeoutMs = 5 * 1000;

    protected AbstractConnection(IConnectionProfile<?> profile) {
        this.profile = profile;
    }

    @Override
    public IConnectionProfile<?> getProfile() {
        return profile;
    }

    /**
     * Gets maximum number of retires when an operation fails.
     * 
     * @return a positive number.
     */
    public int getMaxRetry() {
        return maxRetry;
    }

    /**
     * Sets maximum number of retires when an operation fails.
     * 
     * @param maxRetry a non-zero positive number.
     * 
     * @return the same connection
     */
    public AbstractConnection setMaxRetry(int maxRetry) {
        if (maxRetry < 0) {
            throw new IllegalArgumentException("invalid max rerty " +
                    maxRetry +
                    " must be greater than or equal to 0");
        }
        this.maxRetry = maxRetry;
        return this;
    }

    /**
     * Gets maximum timeout in millisecond.
     * 
     * @return a non-zero positive number.
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Sets maximum timeout in millisecond.
     * 
     * @param timeoutMs a non-zero positive number.
     * @return the same connection
     */
    public AbstractConnection setTimeoutMs(int timeoutMs) {
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("invalid timeout " +
                    timeoutMs +
                    " must be greater than 0");
        }
        this.timeoutMs = timeoutMs;
        return this;
    }

    @Override
    public String getConnectionString() {
        return "in-memory";
    }
}
