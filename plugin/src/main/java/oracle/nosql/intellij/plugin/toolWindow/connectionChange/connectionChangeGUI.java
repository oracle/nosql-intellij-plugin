/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.connectionChange;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.MultipleConnectionsDataProviderService.State;

import javax.swing.*;
import java.util.Map;
import java.util.Objects;

/**
 * Class for toggling between multiple available connections in the NoSQL tool window
 * author @kunalgup
 */
public class connectionChangeGUI {
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JComboBox<String> connectionsComboBox;
    private JPanel buttonPanel;
    private JButton okButton;
    private JButton closeButton;
    private JFrame frame;

    public connectionChangeGUI(String connections[], Map<String, String[]> nameAndUrlToPair, State state, Project project) {
        connectionsComboBox.setModel(new DefaultComboBoxModel<>(connections));
        if (connections.length == 0) {
            String error = "No added connections!";
            notifyMsg(error, project);
            return;
        }
        createFrame();
        okButton.addActionListener(e -> {
            String currentConnection = Objects.requireNonNull(connectionsComboBox.getSelectedItem()).toString();

            if (currentConnection != null) {
                ConnectionDataProviderService.State connectionDataProviderService = state.dict.get(nameAndUrlToPair.get(currentConnection)[1]);
                ConnectionDataProviderService.getInstance(project).loadState(connectionDataProviderService);
                DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

                FileEditor[] fileEditors = fileEditorManager.getAllEditors();
                for (FileEditor fileEditor : fileEditors) {
                    fileEditorManager.closeFile(fileEditor.getFile());
                }
                frame.dispose();
            }
        });
        closeButton.addActionListener(e -> {
            frame.dispose();
        });


    }

    private void notifyMsg(String msg, Project project) {
        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", msg, NotificationType.ERROR);
        Notifications.Bus.notify(notification, project);
    }

    private void createFrame() {
        frame = new JFrame("Change Connection");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
