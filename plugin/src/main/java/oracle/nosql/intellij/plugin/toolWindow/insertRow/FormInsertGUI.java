/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.insertRow;

import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.JsonUtils;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Table;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map.Entry;

public class FormInsertGUI {
    private JPanel mainPkPanel;

    public boolean sch;

    public FormInsertGUI(Table table, String schem) {
        GridBagConstraints gridBagConstraintsPkPanel = new GridBagConstraints();
        for (int i = 0; i < table.getFieldCount(); i++) {
            RowGUI rowGUI = new RowGUI();
            gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
            mainPkPanel.add(rowGUI.getRowPanel(), gridBagConstraintsPkPanel);
            Field field = table.getFieldAt(i);
            for (Component component : rowGUI.getRowPanel().getComponents()) {
                if (component instanceof JLabel) {
                    if (component.getName() != null && component.getName().equals("orLabel")) {
                        ((JLabel) component).setText("Or");
                    } else
                        ((JLabel) component).setText(field.getName());
                } else if (component instanceof JTextField) {
                    if (field.isPrimaryKey()) {
                        component.setName("PrimaryKeyTextField");
                    } else if (!field.isNullable()) {
                        component.setName("NullableKeyTextField");
                    } else
                        component.setName("ColumnKeyTextField");
                    if (field.getType() == Field.Type.BINARY || field.getType() == Field.Type.FIXED_BINARY) {
                        rowGUI.selectFileToUploadButton.setVisible(true);
                        rowGUI.rowTextField.setToolTipText("Must be a Base64 encoded data");
                        rowGUI.binaryFieldRow = true;
                        rowGUI.orLabel.setVisible(true);
                        fillData(rowGUI.rowTextField, rowGUI.selectFileToUploadButton);
                    } else {
                        rowGUI.selectFileToUploadButton.setVisible(false);
                        rowGUI.binaryFieldRow = false;
                    }
                    if (field.isDefault()) {
                        ((JTextField) component).setText(field.getDefault());
                    }

                    MapValue mp = JsonUtils.createValueFromJson(schem, null).asMap();
                    String text1 = "\"" + field.getName() + "\"";
                    for (Entry<String, FieldValue> entry : mp.entrySet()) {
                        if ("identity".equals(entry.getKey())) {
                            MapValue jsonMp = JsonUtils.createValueFromJson(String.valueOf(entry.getValue()), null).asMap();
                            for (Entry<String, FieldValue> entr : jsonMp.entrySet()) {
                                if ("name".equals(entr.getKey()) && String.valueOf(entr.getValue()).equals(text1)) {
                                    for (Entry<String, FieldValue> ent : jsonMp.entrySet()) {
                                        if ("always".equals(ent.getKey()) && String.valueOf(ent.getValue()).equals("true")) {
                                            component.setEnabled(false);
                                            ((JTextField) component).setToolTipText("GENERATED ALWAYS field cannot be inserted");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void fillData(JTextField rowTextField, JButton selectFileToUploadButton) {
        selectFileToUploadButton.addActionListener(e -> chooseFile(rowTextField));
    }

    private void chooseFile(JTextField rowTextField) {
        String memoryText = "";
        if (rowTextField.getText() != null) memoryText = rowTextField.getText();
        rowTextField.setText(new String(""));
        rowTextField.setEditable(false);
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Binary files (.bin)", "bin");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(mainPkPanel);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                byte[] encodedBytes = Base64.getEncoder().encode(fileBytes);
                rowTextField.setText(new String(encodedBytes));
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainPkPanel, "Error reading file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (result == JFileChooser.CANCEL_OPTION) {
            rowTextField.setText(memoryText);
            rowTextField.setEditable(true);
        }
    }

    public JComponent getMainPkPanel() {
        return mainPkPanel;
    }

    public void setFormInsert(Component jt) {
        jt.setEnabled(false);
    }

    public void setSchemaJSON(boolean sch) {
        this.sch = sch;
    }

}
