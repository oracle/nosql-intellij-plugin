/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.viewReplicas;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Class for viewing replicas of the Global Active tables in the cloud
 * author @kunalgup
 */
public class ViewReplicaGUI {
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private List<String> replicasNames;
    private JFrame frame;

    public ViewReplicaGUI(Table table, Project project) {
        getReplicaNames(table, project);
        closeButton.addActionListener(e -> {
            frame.dispose();
        });
    }

    private void createFrame() {
        frame = new JFrame("View Regional Replicas");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void getReplicaNames(Table table, Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching Regional Replicas", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                IConnection con;
                try {
                    con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                    try {
                        replicasNames = con.getReplicas(table);
                    } catch (Exception ex) {
                        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error fetching table replicas : " + ex.getMessage(), NotificationType.ERROR);
                        Notifications.Bus.notify(notification, project);
                    }
                } catch (Exception ex) {
                    Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                    Notifications.Bus.notify(notification, project);
                }

            }

            @Override
            public void onFinished() {
                if (replicasNames == null || replicasNames.isEmpty()) {
                    Notification notification = new Notification(
                            "Oracle NOSQL", "Oracle NoSql explorer",
                            "No regional replicas found for table : " + table.getName(),
                            NotificationType.ERROR);
                    Notifications.Bus.notify(notification, project);
                } else {
                    mainPanel.add(createPanel(table));
                    createFrame();
                }
            }
        });
    }

    private JPanel createPanel(Table table) {
        int i = 3;
        JPanel panel = new JPanel();
        panel.setLayout(new FormLayout(
                "fill:d:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow", // Column constraints
                "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow," + // Row constraints up to row 17
                        "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," +
                        "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," +
                        "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," +
                        "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," +
                        "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow," + // Row 18
                        "center:max(d;4px):noGrow" // Row 19
        ));
        CellConstraints cc = new CellConstraints();
        final Spacer spacer1 = new Spacer();
        panel.add(spacer1, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        int count = 1;
        for (String replicaName : replicasNames) {
            JLabel label = new JLabel("Replica-" + (count++) + " : ");
            Font boldFont = new Font(label.getFont().getName(), Font.BOLD, 15);
            label.setFont(boldFont);
            JTextField field = new JTextField();
            field.setPreferredSize(new Dimension(350, 30));
            field.setText(replicaName);
            field.setEditable(false);
            panel.add(label, cc.xy(1, i, CellConstraints.LEFT, CellConstraints.FILL));
            panel.add(field, cc.xy(3, i, CellConstraints.FILL, CellConstraints.FILL));
            i += 2;
        }
        return panel;
    }

}
