/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.icons.AllIcons;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import oracle.nosql.intellij.plugin.common.ConnectionDataProviderService;
import oracle.nosql.intellij.plugin.common.DBProject;
import oracle.nosql.intellij.plugin.common.NoSQLKeywords;
import oracle.nosql.intellij.plugin.common.OracleNoSqlBundle;
import oracle.nosql.model.connection.IConnection;
import oracle.nosql.model.schema.Field;
import oracle.nosql.model.schema.Table;
import oracle.nosql.model.table.ui.TablePageCache;
import org.fife.ui.rsyntaxtextarea.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.fife.ui.rtextarea.RTextScrollPane;
import org.json.JSONObject;

import static java.awt.Adjustable.HORIZONTAL;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

/**
 * GUI for query field and Table.
 *
 * @author amsundar, kunalgup
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "HardCodedStringLiteral", "SpellCheckingInspection"})
class TableView {
    private static final String TABLE_RESULT_CARD = "Table_Result";
    private static final String ERROR_RESULT_CARD = "Error_Result";
    private static final String PREVIOUS_COMMANDS = "---Previous Commands---";
    private static final String ERROR = "oracle.nosql.tableview.query.syntax.error";
    private JPanel topComponent;
    private RSyntaxTextArea queryField;
    private JPopupMenu suggestionPopup;
    private JTextField autoCompleteField;
    private JComboBox<String> previousCommandBox;
    private JButton executeButton;
    private JButton queryPlanButton;
    private JButton queryDownloadButton;
    private JPanel resultPanel;
    private JPanel errorResultPanel;
    private DatabaseTable myTable;
    private NavigationBar navBar;
    private final CardLayout myLayout = new CardLayout();
    private final Project project;
    private final DataBaseVirtualFile file;
    private TablePageCache pageCache;
    private DataBaseTableModel myModel;
    private Set<String> previousCommandsSet;
    private List<String> columnSuggestions;
    private List<String> keywordSuggestions;
    private List<String> triggerList;
    private boolean isJsonCollection;

    TableView(Project project, DataBaseVirtualFile file) {
        this.file = file;
        this.project = project;
        this.pageCache = null;
        previousCommandsSet = new HashSet<>();

        setupUI();

        IConnection connection;

        try { // checks if the table is json collection
            connection = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();

            String schemaJson = connection.showSchema(file.getTable());
            String result = connection.showSchema(file.getTable());
            JSONObject jsonObject = new JSONObject(result);
            isJsonCollection = jsonObject.has("jsonCollection");
        } catch (Exception ex) {
            Notification notification = new Notification(
                    "Oracle NOSQL", "Oracle NoSql explorer",
                    OracleNoSqlBundle
                            .message(
                                    "oracle.nosql.toolWindow.connection.get.error") +
                            ex.getMessage(),
                    NotificationType.ERROR);
            Notifications.Bus.notify(notification, project);
        }

        executeButton.addActionListener(e -> {
            executeButton.setEnabled(false);
            previousCommandBox.setSelectedItem(PREVIOUS_COMMANDS);
            executeQuery();
        });
        previousCommandBox.addActionListener(e -> {
            if (!previousCommandBox.getSelectedItem().equals(PREVIOUS_COMMANDS))
                queryField.setText(previousCommandBox.getSelectedItem().toString());
        });
        queryPlanButton.addActionListener(e -> {
            queryPlanButton.setEnabled(false);
            showQueryplan();
        });
        queryDownloadButton.addActionListener(e -> {
            queryDownloadButton.setEnabled(false);
            downloadQueryResult();
        });
        navBar.addNextListener(e -> nextPage());
        navBar.addPrevListener(e -> prevPage());

        setupSuggestions();
        queryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestionsPopup();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestionsPopup();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestionsPopup();
            }
        });

        queryField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (suggestionPopup.isVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB) {
                        e.consume();
                        if (suggestionPopup.isVisible()) {
                            MenuElement[] path = MenuSelectionManager.defaultManager().getSelectedPath();
                            if (path.length > 0) {
                                MenuElement menuElement = path[path.length - 1];
                                if (menuElement instanceof JMenuItem) {
                                    JMenuItem menuItem = (JMenuItem) menuElement;
                                    menuItem.doClick();
                                }
                            }
                        }
                    }
                }
            }
        });

        // Rebind TAB key to ensure it doesn't transfer focus
        InputMap im = queryField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = queryField.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
    }

    public void setupUI() {
        JPanel tableResultPanel;
        JPanel queryComponent;
        JBScrollPane jBScrollPane1;
        topComponent = new JPanel();
        topComponent.setLayout(new BorderLayout(4, 3));

        //textarea with execute button for query
        queryComponent = new JPanel();
        queryComponent.setLayout(new GridLayoutManager(6, 4, JBUI.emptyInsets(), -1, -1));
        topComponent.add(queryComponent, BorderLayout.NORTH);

        queryField = new RSyntaxTextArea();
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/nosql", "oracle.nosql.intellij.plugin.recordView.NoSQLTokenMaker");
        queryField.setSyntaxEditingStyle("text/nosql");

        String initialQuery = "SELECT * FROM " + file.getTable().getName();
        String formatted = new SQLFormatter().format(initialQuery);
        queryField.setText(formatted);

        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        Color bgColor = scheme.getDefaultBackground();
        if (isDarkTheme()) {
            // Apply dark theme to the queryField
            try {
                Theme theme = Theme.load(getClass().getResourceAsStream(
                        "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                theme.apply(queryField);
            } catch (IOException ioe) { // Never happens
                ioe.printStackTrace();
            }
        }
        queryField.setBackground(bgColor);
        queryField.setHighlightCurrentLine(false);

        JPopupMenu rightClickPopup = queryField.getPopupMenu();
        rightClickPopup.removeAll();
        JMenuItem formatSQLMenuItem = new JMenuItem("Prettify SQL");
        customizeMenuItem(formatSQLMenuItem);
        formatSQLMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = queryField.getText();
                String formatted = new SQLFormatter().format(text);
                queryField.setText(formatted);
            }
        });
        rightClickPopup.add(formatSQLMenuItem);

        RTextScrollPane queryScrollPane = new RTextScrollPane(queryField);
        queryScrollPane.setViewportView(queryField);
        queryScrollPane.setLineNumbersEnabled(false);
        queryComponent.add(queryScrollPane, new GridConstraints(0, 0, 4, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 200), null, 0, false));
        executeButton = new JButton();
        executeButton.setText("Execute");
        executeButton.setIcon(AllIcons.Actions.Execute);
        queryComponent.add(executeButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        queryPlanButton = new JButton();

        previousCommandBox = new ComboBox<>();
        previousCommandBox.addItem(PREVIOUS_COMMANDS);
        previousCommandBox.setSelectedItem(PREVIOUS_COMMANDS);
        previousCommandBox.setPrototypeDisplayValue("select * from ");
        addHorizontalScrollBar(previousCommandBox);
        queryComponent.add(previousCommandBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

        queryPlanButton.setText("Show Query Plan");
        queryPlanButton.setIcon(AllIcons.Actions.Show);
        queryComponent.add(queryPlanButton, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        //result of the query which is card layout
        queryDownloadButton = new JButton();
        queryDownloadButton.setText("Download Query Result");
        queryDownloadButton.setIcon(AllIcons.Actions.Download);
        queryComponent.add(queryDownloadButton, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        resultPanel = new JPanel(myLayout);

        tableResultPanel = new JPanel();
        tableResultPanel.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        jBScrollPane1 = new JBScrollPane();
        tableResultPanel.add(jBScrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        myModel = new DataBaseTableModel(null);
        myTable = new DatabaseTable(project, myModel);
        myTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jBScrollPane1.setViewportView(myTable);
        resultPanel.add(tableResultPanel, TABLE_RESULT_CARD);

        errorResultPanel = new JPanel(new BorderLayout());
        resultPanel.add(errorResultPanel, ERROR_RESULT_CARD);

        topComponent.add(resultPanel, BorderLayout.CENTER);

        //navigation bar
        navBar = new NavigationBar();
        topComponent.add(navBar, BorderLayout.SOUTH);
    }

    /**
     * @param input
     * @return Method to get SQL auto-complete suggestions on the provided input string
     */
    private List<String> getSQLSuggestions(String input) {
        if (input.isEmpty()) return Collections.emptyList();
        if (input.charAt(input.length() - 1) == ' ') return Collections.emptyList();

        int caretPosition = queryField.getCaretPosition();
        String temp = queryField.getText().substring(0, caretPosition);
        String[] words = temp.split("\\s+");
        String lastWord = words[words.length - 1];

        int dotIndex = lastWord.lastIndexOf(".");
        List<String> suggestionItems;
        if (dotIndex != -1) {
            lastWord = lastWord.substring(dotIndex+1);
            suggestionItems = columnSuggestions;
        } else {
            String secondLastWord = words.length > 1 ? words[words.length - 2] : "";
            boolean suggestionToggler = false;
            for (String triggerWord : triggerList) {
                if (triggerWord.equalsIgnoreCase(secondLastWord)) {
                    suggestionToggler = true;
                    break;
                }
            }
            suggestionItems = suggestionToggler ? columnSuggestions : keywordSuggestions;
        }

        if (lastWord.isEmpty() || suggestionItems.contains(lastWord)) return Collections.emptyList();
        ArrayList<String> suggestions = new ArrayList<>();
        for (String item : suggestionItems) {
            if (item.toLowerCase().startsWith(lastWord.toLowerCase())) {
                suggestions.add(item);
            }
        }
        return suggestions;
    }

    /**
     * Initializes suggestionItems list, with column names and SQL keywords
     */
    private void setupSuggestions() {
        suggestionPopup = new JPopupMenu();
        Table table = file.getTable();
        List<Field> fields = table.getFields();
        keywordSuggestions = new ArrayList<>();
        columnSuggestions = new ArrayList<>();
        triggerList = new ArrayList<>();

        columnSuggestions.add(file.getTable().getName());
        for (Field field : fields) {
            columnSuggestions.add(field.getName());
            triggerList.add(field.getName() + ",");
        }
        keywordSuggestions.addAll(Arrays.asList(NoSQLKeywords.RESERVED_WORDS));

        triggerList.addAll(Arrays.asList(NoSQLKeywords.TRIGGER_WORDS));
        triggerList.add(",");
    }

    /**
     * Renders and updates the auto-complete popup, based on the text in queryField
     */
    private void updateSuggestionsPopup() {
        SwingUtilities.invokeLater(() -> {
            String input = queryField.getText();
            List<String> suggestionsList = getSQLSuggestions(input);
            if (!input.isEmpty() && !suggestionsList.isEmpty()) {
                // Clear existing items only if the suggestions have changed
                JMenuItem firstItem = null;
                if (!isPopupShowingSuggestions(suggestionsList)) {
                    suggestionPopup.removeAll();
                    for (int i = 0; i < suggestionsList.size(); i++) {
                        String suggestion = suggestionsList.get(i);
                        JMenuItem item = new JMenuItem(suggestion);
                        item.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                selectSuggestion(suggestion);
                            }
                        });
                        customizeMenuItem(item);
                        suggestionPopup.add(item);
                        if (i == 0) {
                            firstItem = item;
                        }
                    }
                    setSuggestionPopupPosition();
                    MenuElement[] elements = new MenuElement[]{suggestionPopup, firstItem};
                    SwingUtilities.invokeLater(() -> {
                        MenuSelectionManager.defaultManager().setSelectedPath(elements);
                    });

                }
                suggestionPopup.setVisible(true);
                queryField.requestFocus();
            } else {
                suggestionPopup.removeAll();
                if (suggestionPopup.isVisible()) {
                    suggestionPopup.setVisible(false);
                }
            }
        });
    }

    private static void customizeMenuItem(JMenuItem menuItem) {
        menuItem.setUI(new javax.swing.plaf.basic.BasicMenuItemUI() {
            @Override
            protected void paintBackground(Graphics g, JMenuItem menuItem, Color bgColor) {
                if (menuItem.isArmed() || menuItem.isSelected()) {
                    g.setColor(new Color(0, 91, 187)); // Blue focus color
                    g.fillRect(0, 0, menuItem.getWidth(), menuItem.getHeight());
                    menuItem.setForeground(Color.WHITE);
                } else {
                    EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
                    menuItem.setBackground(scheme.getDefaultBackground());
                    if (isDarkTheme()) {
                        menuItem.setForeground(Color.LIGHT_GRAY);
                    } else {
                        menuItem.setForeground(Color.DARK_GRAY);
                    }
                }
            }
        });
    }

    /**
     * @param suggestion Method to select a suggestion menu item, when it is clicked
     */
    private void selectSuggestion(String suggestion) {
        int caretPos = queryField.getCaretPosition();
        String beforeCaret = queryField.getText().substring(0, caretPos);
        String afterCaret = "";
        if (caretPos + 1 < queryField.getText().length()) {
            afterCaret = queryField.getText().substring(caretPos);
        }
        int lastSpace = beforeCaret.lastIndexOf(" ");
        int lastEndline = beforeCaret.lastIndexOf("\n");
        int lastTab = beforeCaret.lastIndexOf("\t");
        int lastDot = beforeCaret.lastIndexOf(".");

        int lastDelimiter = Math.max(lastSpace, Math.max(lastEndline, Math.max(lastTab, lastDot)));

        String beforeDelimiter = beforeCaret.substring(0, lastDelimiter + 1);
        String updatedText = beforeDelimiter + suggestion + afterCaret;
        queryField.setText(updatedText);
        queryField.setCaretPosition(lastDelimiter + suggestion.length() + 1);
    }

    /**
     * Method to set position of suggestion Popup, based on caret position
     */
    private void setSuggestionPopupPosition() {
        try {
            Rectangle2D caretRectangle = queryField.modelToView2D(queryField.getCaretPosition());
            int x = (int) caretRectangle.getX();
            int y = (int) caretRectangle.getY();
            suggestionPopup.show(queryField, x, y + queryField.getFontMetrics(queryField.getFont()).getHeight());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param suggestionsList Method to check if the popup is already showing the current suggestions
     */
    private boolean isPopupShowingSuggestions(List<String> suggestionsList) {
        if (suggestionPopup.getComponentCount() != suggestionsList.size()) {
            return false;
        }
        for (int i = 0; i < suggestionsList.size(); i++) {
            if (!((JMenuItem) suggestionPopup.getComponent(i)).getText().equals(suggestionsList.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method to add scroll bars in the previous command dropdown.
     */
    private void addHorizontalScrollBar(JComboBox<String> box) {
        if (box.getItemCount() == 0) return;
        Object comp = box.getUI().getAccessibleChild(box, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        JPopupMenu popup = (JPopupMenu) comp;
        JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
        scrollPane.setHorizontalScrollBar(new JScrollBar(HORIZONTAL));
        scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private void showQueryplan() {
        final String query = queryField.getText();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Fetching Query Plan", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                IConnection con;
                try {
                    con = DBProject.getInstance(Objects.requireNonNull(project)).getConnection();
                    String queryPlan = con.fetchQueryPlan(query);
                    SwingUtilities.invokeLater(() -> {
                        JTextArea textArea = new JTextArea(queryPlan);
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        textArea.setEditable(false);
                        textArea.setLineWrap(true);
                        textArea.setWrapStyleWord(true);
                        scrollPane.setPreferredSize(new Dimension(600, 500));
                        JOptionPane.showMessageDialog(null, scrollPane, "Query Execution Plan", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    showErrorResult(OracleNoSqlBundle.message(ERROR, ex.getMessage()));
                    queryPlanButton.setEnabled(true);
                    return;
                }
                queryPlanButton.setEnabled(true);
            }
        });
    }

    private void executeQuery() {
        final String query = queryField.getText();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Executing Query", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                IConnection con;
                try {
                    con = DBProject.getInstance(project).getConnection();
                    Iterator<?> result;
                    try {
                        result = con.query(query);
                    } catch (Exception ex) {
                        showErrorResult(OracleNoSqlBundle.message(ERROR, ex.getMessage()));
                        return;
                    }
                    setResult(result, query);
                } catch (Exception ex) {
                    showErrorResult(OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage());
                }
            }
        });
    }

    /**
     * Method to add a successfull query run in the history of previous commands
     */
    private void setPreviousCommand(String query) {

        if (!previousCommandsSet.contains(query)) {
            previousCommandBox.insertItemAt(query, 1);
            previousCommandsSet.add(query);
        } else {
            int i;
            for (i = 1; i <= 20; i++) {
                String str = previousCommandBox.getItemAt(i);
                if (str.equals(query)) {
                    break;
                }
            }
            previousCommandBox.removeItemAt(i);
            previousCommandBox.insertItemAt(query, 1);
        }
        if (previousCommandsSet.size() > 20) {
            String extra = previousCommandBox.getItemAt(previousCommandBox.getItemCount() - 1);
            previousCommandsSet.remove(extra);
            previousCommandBox.removeItemAt(previousCommandBox.getItemCount() - 1);
        }
    }

    /**
     * Method to download the query result is JSON format
     */
    private void downloadQueryResult() {
        final String query = queryField.getText();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading query result", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                Iterator<?> result;
                try {
                    IConnection con;
                    con = DBProject.getInstance(project).getConnection();
                    try {
                        result = con.query(query);
                    } catch (Exception ex) {
                        showErrorResult(OracleNoSqlBundle.message(ERROR, ex.getMessage()));
                        return;
                    }
                    try {
                        SwingUtilities.invokeLater(() -> startDownloadUtil(result));
                    } catch (Exception ex) {
                        showErrorResult("Error downloading the query result: " + ex.getMessage());
                    }
                    queryDownloadButton.setEnabled(true);
                } catch (Exception ex) {
                    showErrorResult(OracleNoSqlBundle.message("oracle.nosql.toolWindow.connection.get.error") + ex.getMessage());
                }
            }
        });
    }

    /**
     * Method that displays a dialog to user to save the JSON file to the
     * desired location
     */
    private void startDownloadUtil(Iterator<?> result) {
        String home = System.getProperty("user.home");
        String fileSeparator = File.separator;
        String pathname = home + fileSeparator + "Downloads" + fileSeparator + "queryResult.json";
        JFileChooser jFileChooser = new JFileChooser() {
            @Override
            public void approveSelection() {
                File selectedFile = getSelectedFile();
                if (selectedFile.exists() && getDialogType() == SAVE_DIALOG) {
                    int option = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (option) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                        default:
                            throw new IllegalStateException("Unexpected value: " + option);
                    }
                }
                super.approveSelection();
            }
        };

        jFileChooser.setSelectedFile(new File(pathname));
        int returnVal = jFileChooser.showSaveDialog(jFileChooser.getParent());
        File selectedFile = jFileChooser.getSelectedFile();

        downloadResult(result, returnVal, selectedFile, project);
    }

    private void downloadResult(Iterator<?> result, int returnVal, File selectedFile, Project project) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading query result", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    FileWriter fileWriter;
                    try {
                        fileWriter = new FileWriter(selectedFile);
                    } catch (IOException e) {
                        showErrorResult("Error downloading the query result: " + e.getMessage());
                        return;
                    }
                    try (BufferedWriter writer = new BufferedWriter(fileWriter, 8192)) {
                        int batchSize = 100;
                        writer.write("[");
                        int count = 0;
                        while (result.hasNext()) {
                            String currRecord = "";
                            try {
                                currRecord = result.next().toString();
                            } catch (Exception ex) {
                                showErrorResult("Error downloading the query result: " + ex.getMessage());
                                return;
                            }
                            writer.write(currRecord);
                            if (result.hasNext()) writer.write(",");
                            else writer.write("]");
                            count++;
                            if (count % batchSize == 0 || !result.hasNext()) {
                                writer.flush();
                                indicator.setText("Downloaded " + count + " rows... ");
                            }
                        }
                        writer.flush();
                        downloadComplete(result, returnVal, selectedFile);
                    } catch (Exception e) {
                        showErrorResult("Download Failed:<br/>Error downloading the query result: " + e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Method to display the notification in the IDE
     */
    private void downloadComplete(Iterator<?> result, int returnVal, File selectedFile) {
        if (!result.hasNext() && returnVal == JFileChooser.APPROVE_OPTION) {
            Notification notification = new Notification("Oracle NOSQL", "Oracle NoSql explorer", "Query result downloaded successfully: \n", NotificationType.INFORMATION);
            notification.addAction(new NotificationAction(selectedFile.getName()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(selectedFile);
                        } else showErrorResult("Error opening file!");
                    } catch (SecurityException | IOException ex) {
                        showErrorResult("Error opening file!<br/>" + ex.getMessage());
                    }
                }
            });
            Notifications.Bus.notify(notification, project);
        }
    }

    //run only in EDT
    private void showErrorResult(String errorMsg) {
        ApplicationManager.getApplication().invokeLater(() -> {
            errorResultPanel.removeAll();
            JLabel label = new JLabel(errorMsg, SwingConstants.LEFT);
            errorResultPanel.add(label, BorderLayout.NORTH);
            errorResultPanel.validate();
            errorResultPanel.repaint();
            myLayout.show(resultPanel, ERROR_RESULT_CARD);
            executeButton.setEnabled(true);
            queryPlanButton.setEnabled(true);
            queryDownloadButton.setEnabled(true);
        });

    }

    private void setResult(Iterator<?> result, String query) {
        try {
            pageCache =  DBProject.getInstance(project).getConnectionProfile().getTablePageCacheInstance(result, file.getTable());
            String pageSizeAsString = ConnectionDataProviderService.getInstance(project).getValue(ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE);
            if (pageSizeAsString == null) {
                pageSizeAsString = "20";
                ConnectionDataProviderService.getInstance(project).putValue(ConnectionDataProviderService.KEY_SHOW_TABLE_PAGE_SIZE, pageSizeAsString);
            }
            int pageSize = Integer.parseInt(pageSizeAsString);

            pageCache.setPageSize(pageSize);
        } catch (Exception ex) {
            showErrorResult("Error getting Connection Profile" + ex.getMessage());
            return;
        }
        navBar.setPageCache(pageCache);

        try {
            pageCache.nextPage(isJsonCollection);
            if (!pageCache.getPages().isEmpty())
                setPreviousCommand(query);
        } catch (Exception ex) {
            showErrorResult(OracleNoSqlBundle.message(ERROR, ex.getMessage()));
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            updateTable();
            navBar.updateButtons();
            executeButton.setEnabled(true);
        });
    }

    /**
     * Method that hardcodes the text to "Download Binary Object" in the UI table.
     */
    private void setBinaryRenderer() {
        Table table = file.getTable();
        for (int i = 0; i < table.getFieldCount(); i++) {
            String name = table.getFields().get(i).getName();
            if (table.getFields().get(i).getType().equals(Field.Type.BINARY) || table.getFields().get(i).getType().equals(Field.Type.FIXED_BINARY)) {
                myTable.getColumnModel().getColumn(i).setCellRenderer(new HardCodedTextRenderer());
            }

        }
    }

    public class HardCodedTextRenderer extends JEditorPane implements TableCellRenderer {
        public HardCodedTextRenderer() {
            setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
            setEditable(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Set the text for the cell
            String val = Objects.requireNonNull(table.getValueAt(row, column)).toString();
            if (val != null && !val.trim().isEmpty()) {
                setText("<html><body><u><font color='#1E90FF'>Download binary object</font></u></body></html>");
            } else
                setText("null");
            return this;
        }
    }

    private void updateTable() {
        if (pageCache.getColumnHeaders().isEmpty()) {
            showErrorResult("0 row(s) returned");
        } else {
            myModel.setPageCache(pageCache);
            setBinaryRenderer();
            myLayout.show(resultPanel, TABLE_RESULT_CARD);
        }
    }

    JComponent getComponent() {
        return topComponent;
    }

    private void prevPage() {
        pageCache.prevPage();
        updateTable();
    }

    private void nextPage() {
        try {
            pageCache.nextPage(isJsonCollection);
        } catch (Exception ex) {
            showErrorResult("Please check the query:<br/>" + ex.getMessage());
        }
        updateTable();
    }

    private static boolean isDarkTheme() {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        Color backgroundColor = scheme.getColor(EditorColors.CARET_ROW_COLOR);

        if (backgroundColor == null) {
            // Fallback if CARET_ROW_COLOR is not set
            backgroundColor = scheme.getDefaultBackground();
        }

        return isColorDark(backgroundColor);
    }

    /**
     * @param color
     * @return Method to determine if the color is dark based on a luminance calculation or a predefined threshold
     */
    private static boolean isColorDark(Color color) {
        int brightness = (int) Math.sqrt(
                color.getRed() * color.getRed() * 0.241 +
                        color.getGreen() * color.getGreen() * 0.691 +
                        color.getBlue() * color.getBlue() * 0.068);

        return brightness < 130; // Threshold for determining darkness
    }
}

