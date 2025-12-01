/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.recordView;

import com.intellij.icons.AllIcons;
import oracle.nosql.model.table.ui.TablePageCache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

/**
 * Navigation bar provides the UI for page navigation, with functionality for
 * move next, move back and shows page number. (This class needs many
 * modifications yet, there will be design changes here)
 *
 * @author amsundar
 */
class NavigationBar extends JPanel {
    private final JLabel pageIndicator;
    private final JButton next;
    private final  JButton prev;
    private TablePageCache pageCache;
    private int currentPageNumber;

    NavigationBar() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        prev = new JButton(AllIcons.Actions.Back);
        prev.setEnabled(false);
        //prev.setBorderPainted(false);
        prev.setBorder(BorderFactory.createEmptyBorder());
        //prev.setMargin(new Insets(0, 0, 0, 0));
        //prev.setContentAreaFilled(false);
        this.add(prev);
        pageIndicator = new JLabel();
        this.add(pageIndicator);
        next = new JButton(AllIcons.Actions.Forward);
        next.setEnabled(false);
        next.setBorder(BorderFactory.createEmptyBorder());
        this.add(next);
        currentPageNumber=1;
    }
    void setPageCache(TablePageCache pageCache) {
        this.pageCache = pageCache;
        setPageNumber(1);
    }
    private void setPageNumber(@SuppressWarnings("SameParameterValue") int num) {
        currentPageNumber = num;
    }
    void addNextListener(ActionListener listener) {
        next.addActionListener(e -> {
            listener.actionPerformed(e);
            ++currentPageNumber;
            updateButtons();
        });
    }

    public void addPrevListener(ActionListener listener) {
        prev.addActionListener(e -> {
            listener.actionPerformed(e);
            --currentPageNumber;
            updateButtons();
        });
    }
    public void updateButtons() {
        if (pageCache != null) {
            boolean hasNextPage;
            try {
                hasNextPage = pageCache.hasNextPage();
            } catch (Exception ex) {
                hasNextPage = false;
            }
            if (hasNextPage) {
                next.setEnabled(true);
            } else {
                next.setEnabled(false);
            }
            if (pageCache.hasPrevPage()) {
                prev.setEnabled(true);
            } else {
                prev.setEnabled(false);
            }
            pageIndicator.setText("" + currentPageNumber); //$NON-NLS-1$
        }
    }
}
