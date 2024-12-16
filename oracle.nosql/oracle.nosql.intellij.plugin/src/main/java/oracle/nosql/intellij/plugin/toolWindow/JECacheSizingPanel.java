/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the dialog window for JE Cache Size calculator
 *
 * @author lsatpal
 */
public class JECacheSizingPanel extends JPanel {
    private final Project project;
    private final JFrame frame;
    private final DefaultTableModel tableModel;
    private final JPanel indexPanel;
    private final JTextField tableIdField;
    private final JTextField primaryKeySizeField;
    private final JTextField rowSizeField;
    private final JTextField numberOfRowsField;
    private int indexCounter = 0;
    private final JPanel clearButtonPanel;

    private final List<Integer> lookup;
    private final List<String> lookup2;

    private static class TableDetailRow {
        public int primaryKeySize, rowSize;
        public long rowCount;
        public String tableId;
        public List<Integer> indexSizes;

        TableDetailRow(String tableId, int primaryKeySize, int rowSize, long rowCount, List<Integer> indexSizes) {
            this.tableId = tableId;
            this.primaryKeySize = primaryKeySize;
            this.rowSize = rowSize;
            this.rowCount = rowCount;
            this.indexSizes = indexSizes;
        }
    }

    public JECacheSizingPanel(Project project) {
        this.project = project;

        frame = new JFrame("JE Cache Size Calculator");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Initialize lookup
        lookup = new ArrayList<>();
        lookup2 = new ArrayList<>();

        frame.setLocationRelativeTo(null);

        // Top panel with table
        JPanel topPanel = new JPanel(new BorderLayout());
        String[] columnNames = {"Table ID", "Primary Key size", "Row size", "Number of Rows", "Index sizes"};

        tableModel = new DefaultTableModel(new Object[0][columnNames.length], columnNames);

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        JButton clearAllButton = new JButton("Clear");
        clearAllButton.setIcon(AllIcons.Actions.Refresh);
        clearAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClear();
            }
        });

        clearButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearButtonPanel.add(clearAllButton);
        clearButtonPanel.setVisible(false);
        topPanel.add(clearButtonPanel, BorderLayout.SOUTH);

        // Bottom panel with form
        JPanel bottomPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        bottomPanel.add(new JLabel("Table ID:"), gbc);

        gbc.gridx = 1;
        tableIdField = new JTextField(20);
        tableIdField.setText("1");
        bottomPanel.add(tableIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        bottomPanel.add(new JLabel("Primary Key size (in Bytes):"), gbc);

        gbc.gridx = 1;
        primaryKeySizeField = new JTextField(20);
        bottomPanel.add(primaryKeySizeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        bottomPanel.add(new JLabel("Row size (in Bytes):"), gbc);

        gbc.gridx = 1;
        rowSizeField = new JTextField(20);
        bottomPanel.add(rowSizeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        bottomPanel.add(new JLabel("Number of Rows:"), gbc);


        gbc.gridx = 1;
        numberOfRowsField = new JTextField(20);
        bottomPanel.add(numberOfRowsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        bottomPanel.add(new JLabel("Index details:"), gbc);

        gbc.gridx = 1;
        JButton addIndexButton = new JButton("Add Index sizes");
        addIndexButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addIndexButton.setPreferredSize(new Dimension(150, 25));
        bottomPanel.add(addIndexButton, gbc);

        // Panel to hold index labels and text fields
        indexPanel = new JPanel();
        indexPanel.setLayout(new BoxLayout(indexPanel, BoxLayout.Y_AXIS));
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        bottomPanel.add(indexPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton addTableButton = new JButton("Add Table");
        addTableButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(addTableButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton calculateButton = new JButton("CALCULATE");
        calculateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(calculateButton, gbc);

        JScrollPane bottomScrollPane = new JScrollPane(bottomPanel);
        bottomScrollPane.setPreferredSize(new Dimension(frame.getWidth(), 400));

        // Add panels to frame
        frame.add(topPanel, BorderLayout.CENTER);
        frame.add(bottomScrollPane, BorderLayout.SOUTH);

        // Button action listeners
        addTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddTable();
            }
        });

        addIndexButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddIndex();
            }
        });

        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCalculate();
            }
        });

        frame.setVisible(true);
    }

    private void onAddTable() {
        String tableId = tableIdField.getText();
        String primaryKeySize = primaryKeySizeField.getText();
        String rowSize = rowSizeField.getText();
        String numberOfRows = numberOfRowsField.getText();

        List<Integer> indexSizes = new ArrayList<>();
        try {
            for (Component item : indexPanel.getComponents()) {
                JPanel panel = (JPanel) item;
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JTextField) {
                        indexSizes.add(Integer.parseInt(((JTextField) comp).getText()));
                    }
                }
            }
        } catch (NumberFormatException | ClassCastException e) {
            return;
        }

        StringBuilder indexSizesString = new StringBuilder();
        for (int i = 0; i < indexSizes.size(); i++) {
            indexSizesString.append(indexSizes.get(i).toString());
            if (i < indexSizes.size() - 1) {
                indexSizesString.append(", ");
            }
        }

        Object[] newRow = {tableId, primaryKeySize, rowSize, numberOfRows, indexSizesString.toString()};
        tableModel.addRow(newRow);
        clearButtonPanel.setVisible(true);

        tableIdField.setText(String.valueOf(tableModel.getRowCount() + 1));
        primaryKeySizeField.setText("");
        rowSizeField.setText("");
        numberOfRowsField.setText("");
        indexPanel.removeAll();
        indexCounter = 0;
    }

    private void onCalculate() {
        List<TableDetailRow> rows = new ArrayList<>();
        try {
            rows = getAllRows();
            if (lookup.isEmpty() || lookup2.isEmpty()) {
                setSizingLookup();
            }
        } catch (Exception ex) {
            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", OracleNoSqlBundle.message("oracle.nosql.toolWindow.sizing.error") + ex.getMessage(), NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
            return;
        }

        double cache = 0.0, storage = 0.0;
        for (TableDetailRow currentTable : rows) {
            double ind = 0;
            double indi = 0;
            for (int j = 0; j < currentTable.indexSizes.size(); j++) {
                String str = lookup2.get((currentTable.indexSizes.get(j) - 1) * 1024 + currentTable.primaryKeySize - 1);
                String[] values = str.split(",");
                ind += Integer.parseInt(values[2]) * (double) currentTable.rowCount / (100L * 1024 * 1024 * 1024);
                indi += (currentTable.primaryKeySize + currentTable.indexSizes.get(j)) * (double) currentTable.rowCount / (1024L * 1024 * 1024);
            }
            cache += (lookup.get(currentTable.primaryKeySize - 1) * (double) currentTable.rowCount) / (100L * 1024 * 1024 * 1024) + ind;
            storage += ((currentTable.primaryKeySize + currentTable.rowSize) * (double) currentTable.rowCount) / (1024L * 1024 * 1024) + indi;
        }
        double shards = Math.ceil(cache / 22.4);
        double formattedStorage = (storage * 2.5) / 1024;
        String message = String.format("JE Cache Size: %.4f GB\nNumber of Shards: %.0f\nStorage Size: %.4f TB", cache, shards, formattedStorage);
        JOptionPane.showMessageDialog(frame, message);
    }

    private void onClear() {
        tableModel.setRowCount(0);
        tableIdField.setText("1");
        primaryKeySizeField.setText("");
        rowSizeField.setText("");
        numberOfRowsField.setText("");
        indexPanel.removeAll();
        clearButtonPanel.setVisible(false);
        indexCounter = 0;
    }

    private void onAddIndex() {
        JPanel indexEntryPanel = new JPanel(new BorderLayout());
        JLabel indexLabel = new JLabel("Index " + indexCounter + " size: ");
        JLabel deleteIcon = new JLabel(AllIcons.Actions.DeleteTag);
        JTextField indexField = new JTextField(10);
        indexEntryPanel.add(indexLabel, BorderLayout.WEST);
        indexEntryPanel.add(indexField, BorderLayout.CENTER);
        indexEntryPanel.add(deleteIcon, BorderLayout.EAST);
        deleteIcon.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        deleteIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                indexPanel.remove(indexEntryPanel);
                indexPanel.revalidate();
                indexPanel.repaint();
                indexCounter--;
                int i = 0;
                for (Component item : indexPanel.getComponents()) {
                    JPanel panel = (JPanel) item;
                    JLabel label = (JLabel) panel.getComponent(0);
                    label.setText("Index " + i + " size: ");
                    i++;
                }
            }
        });

        indexPanel.add(indexEntryPanel);
        indexPanel.revalidate();
        indexPanel.repaint();
        indexCounter++;
    }

    private List<TableDetailRow> getAllRows() throws NumberFormatException, ClassCastException {
        List<TableDetailRow> rows = new ArrayList<>();
        int rowCount = tableModel.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            try {
                String tableId = (String) tableModel.getValueAt(i, 0);
                int primaryKeySize = Integer.parseInt((String) tableModel.getValueAt(i, 1));
                int rowSize = Integer.parseInt((String) tableModel.getValueAt(i, 2));
                long numberOfRows = Long.parseLong((String) tableModel.getValueAt(i, 3));
                String indexes = (String) tableModel.getValueAt(i, 4);
                String[] temp = indexes.split(", ");
                List<Integer> indexSizes = new ArrayList<>();
                for (String item : temp) {
                    if (!item.isEmpty()) {
                        indexSizes.add(Integer.parseInt(item));
                    }
                }
                rows.add(new TableDetailRow(tableId, primaryKeySize, rowSize, numberOfRows, indexSizes));
            } catch (NumberFormatException | ClassCastException e) {
                throw e;
            }
        }
        return rows;
    }

    private void setSizingLookup() throws Exception {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sizing-lookup.txt");
            List<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines().toList();
            int breakIndex = lines.lastIndexOf("IDENTIFIER");
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i < breakIndex) {
                    lookup.add(Integer.parseInt(line));
                } else if (i > breakIndex) {
                    lookup2.add(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}



