/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView.updateRow;

import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.JsonUtils;
import oracle.nosql.driver.values.MapValue;
import oracle.nosql.intellij.plugin.toolWindow.insertRow.RowGUI;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Field.Type;
import oracle.nosql.model.schema.Table;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map.Entry;

public class FormUpdateGUI {
    private JPanel mainPkPanel;
    private MapValue jsonMap;

    public FormUpdateGUI(Table table, String jString, String schem) {
        jsonMap = JsonUtils.createValueFromJson(jString, null).asMap();
        GridBagConstraints gridBagConstraintsPkPanel = new GridBagConstraints();
        for (int i = 0; i < table.getFieldCount(); i++) {
            RowGUI rowGUI = new RowGUI();
            gridBagConstraintsPkPanel.gridy = gridBagConstraintsPkPanel.gridy + 10;
            mainPkPanel.add(rowGUI.getRowPanel(), gridBagConstraintsPkPanel);
            Field field = table.getFieldAt(i);
            for (Component component : rowGUI.getRowPanel().getComponents()) {
                if (component instanceof JLabel) {
                    if (field.isPrimaryKey())
                        ((JLabel) component).setToolTipText("Read Only");
                    if (component.getName() != null && component.getName().equals("orLabel")) {
                        ((JLabel) component).setText("Or");
                    }
                    else
                        ((JLabel) component).setText(field.getName());
                } else if (component instanceof JTextField) {
                    if (field.isPrimaryKey()) {
                        ((JTextField) component).setToolTipText("Primary Key value cannot be updated");
                        component.setEnabled(false);
                        component.setName("PrimaryKeyTextField");
                    } else if (!field.isNullable()) {
                        component.setName("NullableKeyTextField");
                    } else
                        component.setName("ColumnKeyTextField");
                    if (field.getType() == Field.Type.BINARY || field.getType() == Field.Type.FIXED_BINARY) {
                        rowGUI.selectFileToUploadButton.setVisible(true);
                        rowGUI.rowTextField.setToolTipText("Must be a value of type: BINARY");
                        rowGUI.binaryFieldRow = true;
                        rowGUI.orLabel.setVisible(true);
                        fillData(rowGUI.rowTextField, rowGUI.selectFileToUploadButton);
                    } else {
                        rowGUI.selectFileToUploadButton.setVisible(false);
                        rowGUI.binaryFieldRow = false;
                    }

                    for (Entry<String, FieldValue> entry : jsonMap.entrySet()) {
                        if (field.getName().equals(entry.getKey())) {
                            String str = entry.getValue().toString();
                            if (!str.equals("null")) {
                                if (field.getType().equals(Field.Type.STRING) ||
                                        field.getType().equals(Type.TIMESTAMP) ||
                                        field.getType().equals(Type.BINARY) ||
                                        field.getType().equals(Type.FIXED_BINARY)) {
                                    ((JTextField) component).setText(str.substring(1, str.length() - 1));
                                } else
                                    ((JTextField) component).setText(str);
                            } else
                                ((JTextField) component).setText("");
                            break;
                        }
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
                                            ((JTextField) component).setToolTipText("GENERATED ALWAYS field cannot be updated");
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

    public void disableTF() {
    }
}
