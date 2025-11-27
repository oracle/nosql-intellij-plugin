/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.settings;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.*;
import oracle.nosql.model.connection.ConnectionFactory;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.connection.IConnectionProfileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AllConnectionsSettingGUI {

    private JPanel rootPanel;
    private JPanel profileTypeAndConnectionsPanel;
    private JComboBox<String> profileTypeComboBox;
    private JComboBox<String> connectionsComboBox;
    private JPanel subPanel;
    private JButton modifyButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JPanel buttonPanel;
    private JPanel profileTypePanel;
    private JPanel connectionsPanel;
    private JPanel addConnectionPanel;
    private JButton addConnectionButton;
    private JButton addButton;
    private JButton closeButton;
    private JButton modifyCloseButton;
    private boolean isModified;
    private Map<String, Set<String>> typeToNamesMap;
    private Map<String, String[]> nameAndUidToPairMap;
    private MultipleConnectionsDataProviderService mConService;
    private Project project;

    public AllConnectionsSettingGUI(MultipleConnectionsDataProviderService service, Project project) {
        isModified = false;
        mConService = service;
        this.project = project;
        saveButton.setVisible(false);
        addButton.setVisible(false);
        closeButton.setVisible(false);
        modifyCloseButton.setVisible(false);
        if(connectionsComboBox.getItemCount()==0){
            modifyButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        else{
            modifyButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
        IConnectionProfileType[] profileTypes = ConnectionFactory.getProfileTypes();
        if (profileTypes.length == 0) {
            throw new IllegalStateException("No registered profile types.");
        }
        String[] profileNames = new String[profileTypes.length];
        for (int i = 0; i < profileTypes.length; i++) {
            profileNames[i] = profileTypes[i].getName();
        }

        if (service.getState() == null) throw new AssertionError();

        typeToNamesMap = new HashMap<>();
        nameAndUidToPairMap = new HashMap<>();
        getTypeToNamesMap(service);
        profileTypeComboBox.setModel(new DefaultComboBoxModel<>(profileNames));
        assert profileTypeComboBox != null;
        profileTypeComboBox.setSelectedIndex(0);
        updateConnections(profileTypeComboBox.getItemAt(0));

        addConnectionButton.addActionListener(e -> {
            addButton.setVisible(true);
            closeButton.setVisible(true);
            addConnectionPanel.setVisible(true);
            AddConnectionGUI addConnectionGUI = new AddConnectionGUI(profileTypeComboBox, project);
            addConnectionPanel.add(addConnectionGUI.createPanel(service));
            String profileType = Objects.requireNonNull(profileTypeComboBox.getSelectedItem()).toString();

            if (addButton.getActionListeners().length > 0) {
                addButton.removeActionListener(addButton.getActionListeners()[0]);
            }
            addButton.addActionListener(e1 -> {
                ConnectionDataProviderService.State newState;
                try {
                    newState = addConnectionGUI.apply();
                } catch (Exception ex) {
                    notifyMsg("Error saving details : " + ex.getMessage());
                    return;
                }
                if(newState!=null) {
                    ConnectionDataProviderService.getInstance(project).loadState(newState);
                    DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                }
                getTypeToNamesMap(service);
                updateConnections(profileType);
                addButton.setVisible(false);
                closeButton.setVisible(false);
                removePanel(addConnectionPanel);
                String msg = "Connection Added Successfully : " + addConnectionGUI.getConnectionNameAndUrl();
                notifyMsg(msg);
            });
            closeButton.addActionListener(e2 -> {
                addConnectionPanel.removeAll();
                addConnectionPanel.setVisible(false);
                closeButton.setVisible(false);
                addButton.setVisible(false);
            });
        });
        assert profileTypeComboBox != null;
        profileTypeComboBox.addItemListener(e -> {
            String selectedType = e.getItem().toString();
            updateConnections(selectedType);
        });
        connectionsComboBox.addActionListener(e->{
            if(connectionsComboBox.getItemCount()==0){
                modifyButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
            else{
                modifyButton.setEnabled(true);
                deleteButton.setEnabled(true);
            }
        });

        deleteButton.addActionListener(e -> {
            if (confirmDeletion()) {
                String currentConnectionUrl = getCurrentConnection();
                String connectionNameAndUrl = Objects.requireNonNull(connectionsComboBox.getSelectedItem()).toString();
                String name = nameAndUidToPairMap.get(connectionNameAndUrl)[0];
                String uid = nameAndUidToPairMap.get(connectionNameAndUrl)[1];
                service.getState().uidToTypeMap.remove(uid);
                service.getState().dict.remove(uid);
                service.getState().nameToUidMap.remove(name);
                String profileType = Objects.requireNonNull(profileTypeComboBox.getSelectedItem()).toString();
                typeToNamesMap.get(profileType).remove(name);
                subPanel.removeAll();
                updateConnections(profileType);
                if (currentConnectionUrl != null && currentConnectionUrl.equals(uid)) {
                    if(connectionsComboBox.getSelectedItem()!=null) {
                        String newConnection = connectionsComboBox.getSelectedItem().toString();
                        if (newConnection != null) {
                            String newUid = nameAndUidToPairMap.get(newConnection)[1];
                            ConnectionDataProviderService.State state = service.getState().dict.get(newUid);
                            ConnectionDataProviderService.getInstance(project).loadState(state);
                        }
                    }
                    else{
                        if(!service.getState().dict.isEmpty()){
                            Set<String> keys= service.getState().dict.keySet();
                            for(String key :keys){
                                ConnectionDataProviderService.State state = service.getState().dict.get(key);
                                ConnectionDataProviderService.getInstance(project).loadState(state);
                                break;
                            }
                        }
                    }
                }
                String msg = "Connection deleted successfully : " + connectionNameAndUrl;
                saveButton.setVisible(false);
                modifyCloseButton.setVisible(false);
                DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                notifyMsg(msg);
            }
        });

        modifyButton.addActionListener(e -> {
            subPanel.removeAll();
            saveButton.setVisible(true);
            modifyCloseButton.setVisible(true);
            subPanel.setVisible(true);
            String connection = Objects.requireNonNull(connectionsComboBox.getSelectedItem()).toString();
            String name = nameAndUidToPairMap.get(connection)[0];
            String uid = nameAndUidToPairMap.get(connection)[1];
            String currentConnectionUrl = getCurrentConnection();
            ConnectionDataProviderService conService = new ConnectionDataProviderService();
            conService.loadState(service.getState().dict.get(uid));
            String profileType = Objects.requireNonNull(profileTypeComboBox.getSelectedItem()).toString();
            ConnectionDetailsGUI cGUI = new ConnectionDetailsGUI(profileType, conService, service);
            isModified = cGUI.isModified();
            String conType = service.getConType(uid);
            JComponent connectionDetailsPanel = cGUI.getRootPanel();
            subPanel.add(connectionDetailsPanel);

            if (saveButton.getActionListeners().length > 0) {
                saveButton.removeActionListener(saveButton.getActionListeners()[0]);
            }
            saveButton.addActionListener(e1 -> {
                ConnectionDataProviderService.State newState;
                try {
                    service.getState().dict.remove(uid);
                    service.getState().uidToTypeMap.remove(uid);
                    service.getState().nameToUidMap.remove(name);
                    typeToNamesMap.get(profileType).remove(name);
                    newState = cGUI.apply();
                    typeToNamesMap.get(profileType).add(cGUI.getConnectionName());

                } catch (Exception ex) {
                    notifyMsg("Error saving details : " + ex.getMessage());
                    service.getState().dict.put(uid, conService.getState());
                    service.getState().uidToTypeMap.put(uid, conType);
                    service.getState().nameToUidMap.put(name, uid);
                    typeToNamesMap.get(profileType).add(name);
                    return;
                }

                getTypeToNamesMap(service);
                updateConnections(profileType);
                if (currentConnectionUrl != null && currentConnectionUrl.equals(uid)) {
                    ConnectionDataProviderService.getInstance(project).loadState(newState);
                }
                try {
                    IConnection con = DBProject.getInstance(project).getConnection();
                } catch (Exception ex) {
                    Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage(), NotificationType.ERROR);
                    Notifications.Bus.notify(notification, project);
                    return;
                }

                String msg = "Connection modified successfully : " + connection;
                DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                notifyMsg(msg);
                saveButton.setVisible(false);
                modifyCloseButton.setVisible(false);
                removePanel(subPanel);

            });
            modifyCloseButton.addActionListener(e2 -> {
                subPanel.removeAll();
                subPanel.setVisible(false);
                saveButton.setVisible(false);
                modifyCloseButton.setVisible(false);
            });
        });
    }

    private boolean confirmDeletion() {
        String confirmMsg = "Are you sure you want to delete the connection? ";
        Object[] msg = {confirmMsg};

        int result = JOptionPane.showConfirmDialog(null, msg, "DELETE CONNECTION", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, AllIcons.General.BalloonWarning);

        if (result == JOptionPane.YES_OPTION) return true;
        return false;
    }

    private String getCurrentConnection() {
        ConnectionDataProviderService conService = ConnectionDataProviderService.getInstance(project);
        ConnectionDataProviderService.State stateC = conService.getState();

        MultipleConnectionsDataProviderService mconService = MultipleConnectionsDataProviderService.getInstance(project);
        MultipleConnectionsDataProviderService.State stateM = mconService.getState();
        if (stateM != null && stateC != null) {
            for (Map.Entry<String, ConnectionDataProviderService.State> e : stateM.dict.entrySet()) {
                String connection = e.getKey();
                ConnectionDataProviderService.State state = e.getValue();
                if (stateC.dict.equals(state.dict)) return connection;
            }
        }
        return null;
    }

    private void notifyMsg(String msg) {
        Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", msg, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification, project);
    }

    private void removePanel(Container container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            container.remove(component);
        }

        container.revalidate();
        container.repaint();
    }


    public void getTypeToNamesMap(MultipleConnectionsDataProviderService service) {
        typeToNamesMap.clear();
        try {
            if (service.getState() == null) throw new AssertionError();
            for (Map.Entry<String, String> e : service.getState().nameToUidMap.entrySet()) {
                String name = e.getKey();
                String uid = e.getValue();
                String type = service.getState().uidToTypeMap.get(uid);
                if (!typeToNamesMap.containsKey(type)) typeToNamesMap.put(type, new HashSet<>());
                typeToNamesMap.get(type).add(name);
            }
        } catch (NullPointerException e) {
            throw new NullPointerException();
        }
    }

    public void updateConnections(String selectedType) {
        if (connectionsComboBox != null) connectionsComboBox.removeAllItems();
        if (!typeToNamesMap.isEmpty()) {
            Set<String> connectionsNames = typeToNamesMap.get(selectedType);
            Set<String> nameAndUid = new HashSet<>();
            Map<String, String> nameToUidMap = mConService.getNameToUidMap();
            if (connectionsNames != null && !connectionsNames.isEmpty()) {
                for (String name : connectionsNames) {
                    String uid = nameToUidMap.get(name);
                    String temp = name + " : " + uid;
                    nameAndUid.add(temp);
                    nameAndUidToPairMap.put(temp, new String[]{name, uid});
                }
                connectionsComboBox.removeAllItems();
                List<String> nameAndUrlList = new ArrayList<>(nameAndUid);
                String[] connections = nameAndUrlList.toArray(new String[nameAndUrlList.size()]);
                connectionsComboBox.setModel(new DefaultComboBoxModel<>(connections));
            }
        }
    }

    public void apply() throws ConfigurationException {
        try {
            String connection = Objects.requireNonNull(connectionsComboBox.getSelectedItem()).toString();
            assert mConService.getState() != null;
            ConnectionDataProviderService.State state = mConService.getState().dict.get(connection);
            ConnectionDataProviderService.getInstance(project).loadState(state);
            DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getHelpTopic() {
        String connectionType = Objects.requireNonNull(profileTypeComboBox.getSelectedItem()).toString().toUpperCase();
        if (connectionType.equals("CLOUDSIM")) {
            return MyHelpProvider.CLOUDSIM_CONNECTION_HELP_ID;
        } else if (connectionType.equals("CLOUD")) {
            return MyHelpProvider.CLOUD_CONNECTION_HELP_ID;
        } else if (connectionType.equals("ONPREM")) {
            return MyHelpProvider.ONPREM_CONNECTION_HELP_ID;
        } else {
            return "";
        }
    }

    public JComponent createPanel() {
        return rootPanel;
    }
}