/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

/**
 *Interface to subscribe and notify when Connection is changed by user.
 *
 * @author amsundar
 */
public interface ConnectionManagerListener extends EventListener {
    Topic<ConnectionManagerListener> TOPIC = Topic.create("Connections changed", ConnectionManagerListener.class);
    void connectionsChanged();
}
