/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.dropReplicas;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Class for dropping replicas for the Global Active tables in the cloud
 * author @kunalgup
 */
public class DropReplicasGUI {
    private JPanel rootPanel;
    private JPanel buttonPanel;
    private JButton dropReplicasButton;
    private JPanel subPanel;
    private JButton closeButton;
    private JFrame frame;
    public static List<String> replicasNames;
    FormGUI formGUI;

    public DropReplicasGUI(Table table, List<String> replicas, Project project) {
        formGUI = new FormGUI();
        replicasNames = new ArrayList<>(replicas);

        dropReplicasButton.addActionListener(e -> {
            String confirmMsg = "Are you sure you want to drop the regional replicas? ";
            Object[] msg = {confirmMsg};

            int result = JOptionPane.showConfirmDialog(null, msg, "DROP REGIONAL REPLICAS", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, AllIcons.General.BalloonWarning);

            if (result == JOptionPane.YES_OPTION) {
                frame.dispose();
                Set<String> droppingRegionsSet = getRegions();
                List<String> droppingRegionsList = new ArrayList<>(droppingRegionsSet);
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Dropping Regional Replicas", false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                            try {
                                con.dropReplicas(table.getName(), droppingRegionsList);
                            } catch (Exception ex) {
                                Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error dropping regional replicas : " + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, project);
                            }
                        } catch (Exception ex) {
                            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
                    }

                    @Override
                    public void onFinished() {
                        StringBuilder dropped = new StringBuilder();
                        for (String region : droppingRegionsList) {
                            dropped.append(region + "\n");
                        }
                        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Successfully dropped the regional replicas : \n" + dropped, NotificationType.INFORMATION);
                        Notifications.Bus.notify(notification, project);
                    }
                });
            }
        });

        closeButton.addActionListener(e -> frame.dispose());
        createFrame();

    }

    private Set<String> getRegions() {
        Set<String> regions = new HashSet<>();
        for (Component c : formGUI.getReplicasPanel().getComponents()) {
            if (c instanceof JPanel) {
                for (Component d : ((JPanel) c).getComponents()) {
                    if (d instanceof JComboBox<?>) {
                        String region = Objects.requireNonNull(((JComboBox<?>) d).getSelectedItem()).toString();
                        regions.add(region);
                    }
                }
            }
        }
        return regions;
    }

    private void createFrame() {
        frame = new JFrame("Drop Regional Replicas");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this.createPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public JComponent createPanel() {
        subPanel.add(createReplicasPanel());
        rootPanel.setPreferredSize(new Dimension(500, 450));
        return rootPanel;
    }

    private JPanel createReplicasPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraintsColumnPanel = new GridBagConstraints();

        ReplicaGUI replicaGUI = new ReplicaGUI();
        gridBagConstraintsColumnPanel.gridy = gridBagConstraintsColumnPanel.gridy + 10;
        formGUI.getReplicasPanel().add(replicaGUI.getReplicaPanel(), gridBagConstraintsColumnPanel);
        replicaGUI.setRemoveButton((JPanel) formGUI.getReplicasPanel());

        // Action Listener for Add Column Button
        formGUI.getAddButton().addActionListener(e -> {
            ReplicaGUI replicaGUI1 = new ReplicaGUI();
            replicaGUI1.setRemoveButton((JPanel) formGUI.getReplicasPanel());
            gridBagConstraintsColumnPanel.gridy = gridBagConstraintsColumnPanel.gridy + 10;
            formGUI.getReplicasPanel().add(replicaGUI1.getReplicaPanel(), gridBagConstraintsColumnPanel);
            formGUI.getReplicasPanel().updateUI();
        });

        panel.add(formGUI.getRootPanel());
        return panel;
    }
}
