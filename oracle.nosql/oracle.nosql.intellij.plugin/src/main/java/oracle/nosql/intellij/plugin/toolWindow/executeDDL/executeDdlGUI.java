/*
* Copyright (C) 2024, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.toolWindow.executeDDL;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.DatabaseBrowserManager;
import oracle.nosql.model.connection.IConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * Class for executing System DDLs
 * author @kunalgup
 */
public class executeDdlGUI {
    private JPanel rootPanel;
    private JPanel ddlPanel;
    private JPanel resultPanel;
    private JPanel buttonPanel;
    private JButton closeButton;
    private JTextArea ddlTextArea;
    private JTextArea resultTextArea;
    private JButton executeButton;
    private JPanel ddlButtonPanel;
    private JButton clearButton;
    private JButton copyButton;
    private JPanel copyButtonPanel;
    private JButton copyDdlButton;
    private JFrame frame;

    public executeDdlGUI(Project project) {
        ddlTextArea.setToolTipText("Enter the DDL command");
        createFrame();
        executeButton.addActionListener(e -> {
            final String query = ddlTextArea.getText();
            ProgressManager.getInstance().run(new Task.Backgroundable(project, "Executing DDL", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IConnection con;
                    try {
                        con = DBProject.getInstance(project).getConnection();
                        try {
                            String result = con.systemQuery(query);
                            if (result != null) resultTextArea.setText(result.toString());
                            else {
                                resultTextArea.setText("Executed successfully!");
                                DatabaseBrowserManager.getInstance(project).getToolWindowForm().refresh();
                            }
                        } catch (Exception ex) {
                            String error = "Error executing DDL : " + ex.getMessage();
                            resultTextArea.setText(error);
                        }
                    } catch (Exception ex) {
                        String error = "Error executing DDL : " + ex.getMessage();
                        resultTextArea.setText(error);
                    }
                }
            });
        });
        closeButton.addActionListener(e -> frame.dispose());
        clearButton.addActionListener(e -> {
            ddlTextArea.setText("");
            resultTextArea.setText("");
        });
        copyButton.addActionListener(e2 -> {
            String result = resultTextArea.getText();
            if (result != null && !result.isEmpty()) {
                StringSelection stringSelection = new StringSelection(result);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
            Color originalColor = copyButton.getForeground();
            if (result != null && !result.isEmpty()) copyButton.setForeground(JBColor.green);
            else copyButton.setForeground(JBColor.red);
            Timer timer = new Timer(750, e3 -> copyButton.setForeground(originalColor));
            timer.setRepeats(false);
            timer.start();
        });
        copyDdlButton.addActionListener(e3 -> {
            String result = ddlTextArea.getText();
            if (result != null && !result.isEmpty()) {
                StringSelection stringSelection = new StringSelection(result);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
            Color originalColor = copyDdlButton.getForeground();
            if (result != null && !result.isEmpty()) copyDdlButton.setForeground(JBColor.green);
            else copyDdlButton.setForeground(JBColor.red);
            Timer timer = new Timer(750, e4 -> copyDdlButton.setForeground(originalColor));
            timer.setRepeats(false);
            timer.start();
        });
    }

    private void createFrame() {
        frame = new JFrame("DDL");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
