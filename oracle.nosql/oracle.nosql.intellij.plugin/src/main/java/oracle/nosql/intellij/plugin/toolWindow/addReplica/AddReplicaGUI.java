/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.addReplica;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import oracle.nosql.driver.ops.TableLimits;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Table;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;
/**
 * Class for adding replica for the Global Active tables in the cloud
 * author @kunalgup
 */
public class AddReplicaGUI {
    private JPanel rootPanel;
    private JComboBox<String> replicationRegionComboBox;
    private JTextField readCapacityTextField;
    private JTextField writeCapacityTextField;
    private JTextField diskStorageTextField;
    private JPanel addReplicaPanel;
    private JButton addReplicaButton;
    private JFrame frame;
    private Project project;

    public AddReplicaGUI(Table table, List<String> replicaNames, Project project) {
        String tableName = table.getName();
        this.project = project;

        manageAvailableRegions(replicaNames);
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching table limits", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                IConnection con;
                try {
                    con = DBProject.getInstance(project).getConnection();
                    int initialReadUnits = 0;
                    int initialWriteUnits = 0;
                    int diskStorage = 0;
                    try {
                        TableLimits tableLimits = con.getTableLimits(table);
                        initialReadUnits = tableLimits.getReadUnits();
                        initialWriteUnits = tableLimits.getWriteUnits();
                        diskStorage = tableLimits.getStorageGB();
                        readCapacityTextField.setText(String.valueOf(initialReadUnits));
                        writeCapacityTextField.setText(String.valueOf(initialWriteUnits));
                        diskStorageTextField.setText(String.valueOf(diskStorage));
                        diskStorageTextField.setEditable(false);
                    } catch (Exception ex) {
                        throw new Exception("Cannot fetch table limits :" + ex.getMessage());
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to connect to the cloud connection : " + ex.getMessage());
                }
            }

            @Override
            public void onFinished() {
                createFrame();
            }
        });


        addReplicaButton.addActionListener(e -> {
            String replicatingRegion = (String) replicationRegionComboBox.getSelectedItem();
            int readUnits = Integer.parseInt(readCapacityTextField.getText());
            int writeUnits = Integer.parseInt(writeCapacityTextField.getText());

            assert replicatingRegion != null;
            if (validate(replicatingRegion, readUnits, writeUnits)) {
                frame.dispose();
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Adding regional replica to : " + replicatingRegion, false) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        IConnection con;
                        try {
                            con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                            try {
                                boolean status = con.addReplica(tableName, replicatingRegion, readUnits, writeUnits);
                                if (!status) {
                                    Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Cannot add regional replica to region : " + replicatingRegion, NotificationType.ERROR);
                                    Notifications.Bus.notify(notification, project);
                                }
                            } catch (Exception ex) {
                                Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Error adding regional replica to region " + replicatingRegion + " : " + ex.getMessage(), NotificationType.ERROR);
                                Notifications.Bus.notify(notification, project);
                            }
                        } catch (Exception ex) {
                            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                            Notifications.Bus.notify(notification, project);
                        }
                    }

                    @Override
                    public void onFinished() {
                        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Successfully added the regional replica to : \n" + replicatingRegion, NotificationType.INFORMATION);
                        Notifications.Bus.notify(notification, project);
                    }
                });
            }
        });
    }

    private void manageAvailableRegions(List<String> replicaNames) {
        Set<String> regionsPresent = new HashSet<>(replicaNames);
        List<String> regionsAvailable = new ArrayList<>();
        String[] regions = {"af-johannesburg-1", "ap-chuncheon-1", "ap-hyderabad-1", "ap-melbourne-1", "ap-mumbai-1",
                            "ap-osaka-1", "ap-seoul-1", "ap-sydney-1", "ap-tokyo-1", "ca-montreal-1", "ca-toronto-1",
                            "eu-frankfurt-1", "eu-marseille-1", "eu-paris-1", "me-abudhabi-1", "me-dubai-1",
                            "me-jeddah-1", "sa-santiago-1", "sa-saopaulo-1", "sa-valparaiso-1", "sa-vinhedo-1",
                            "uk-cardiff-1", "uk-london-1", "us-ashburn-1", "us-chicago-1", "us-saltlake-2",
                            "us-sanjose-1", "eu-amsterdam-1", "eu-stockholm-1", "sa-bogota-1", "eu-milan-1",
                            "eu-madrid-1", "il-jerusalem-1", "mx-queretaro-1", "mx-monterrey-1", "ap-singapore-1",
                            "eu-zurich-1","us-phoenix-1"};

        Arrays.sort(regions);
        ConnectionDataProviderService conService = ConnectionDataProviderService.getInstance(project);
        try {
            assert conService.getState() != null;
            String currentEndpoint = conService.getState().dict.get("/Cloud/Cloud/endpoint");
            for (String region : regions) {
                if (currentEndpoint.contains(region)) regionsPresent.add(region);
            }
        } catch (NullPointerException e) {
            throw new NullPointerException();
        }
        for (String region : regions) {
            if (!regionsPresent.contains(region)) regionsAvailable.add(region);
        }
        if (!regionsAvailable.isEmpty())
            replicationRegionComboBox.setModel(new DefaultComboBoxModel<>(regionsAvailable.toArray(new String[0])));
        else {
            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "No available region to add regional replica!", NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
        }


    }

    private void createFrame() {
        frame = new JFrame("Add Regional Replica");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private boolean validate(String replicatingRegion, int readUnits, int writeUnits) {

        if (replicatingRegion == null) return false;
        if (!(readUnits >= 1 && readUnits <= 40000)) return false;
        if (!(writeUnits >= 1 && writeUnits <= 20000)) return false;
        return true;
    }
}


