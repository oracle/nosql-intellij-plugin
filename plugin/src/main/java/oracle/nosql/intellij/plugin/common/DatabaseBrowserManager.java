/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import oracle.nosql.intellij.plugin.toolWindow.NoSQLToolWindow;

public class DatabaseBrowserManager {
    private final Project project;
    private NoSQLToolWindow toolWindowForm;

    private DatabaseBrowserManager(Project project) {
        this.project = project;
        MessageBusConnection connection = project.getMessageBus().connect();
        ConnectionManagerListener connectionManagerListener = new ConnectionManagerListener() {
            /**
             * This will be called when user changes connection in GUI.
             */
            @Override
            public void connectionsChanged() {
                if (toolWindowForm != null) {
                    toolWindowForm.refresh();
                }
            }
        };
        connection.subscribe(ConnectionManagerListener.TOPIC,
                connectionManagerListener);
    }

    public synchronized NoSQLToolWindow getToolWindowForm() {
        if (toolWindowForm == null) {
            toolWindowForm = new NoSQLToolWindow(project);
        }
        return toolWindowForm;
    }

    public static DatabaseBrowserManager getInstance(Project project) {
        return project.getService(DatabaseBrowserManager.class);
    }


}
