/*
 * Copyright (C) 2019, 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */

package oracle.nosql.intellij.plugin.recordView.updateRow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;

public class DDLUpdateGUI {
    private JPanel ddlMainPanel;
    private RSyntaxTextArea textArea1;

    public DDLUpdateGUI(String jString) {
        String formatted = prettifyJson(jString);

        // Wrap the text area in a scroll pane
        RTextScrollPane scrollPane = new RTextScrollPane(textArea1);
        scrollPane.setFoldIndicatorEnabled(true);

        // Add scroll pane to the main panel instead of the raw text area
        ddlMainPanel.setLayout(new java.awt.BorderLayout());
        ddlMainPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        textArea1.setSyntaxEditingStyle("application/json");
        textArea1.setCodeFoldingEnabled(true);
        textArea1.setText(formatted);
    }

    private String prettifyJson(String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(raw, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return raw;
        }
    }

    public JComponent getDdlMainPanel() {
        return ddlMainPanel;
    }
}

