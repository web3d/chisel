/*  Table
 *
 *  The Table organizes generic rows and columns and a header.
 */

package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

//import com.trapezium.factory.*;
import com.trapezium.chisel.gui.ComponentFactory;
import com.trapezium.chisel.gui.FontPool;


/** Generic table component
 */
public class Table extends ChiselPane {

    public TableRow createRow(int row, int columns, Insets insets, Object data) {
        return new TableRow(columns, insets);
    }

    public TableRow createHeader(int columns, Insets insets, Object data) {
        return new TableRow(columns, insets, true);
    }

    public final static int HEADER = -1;

    TableRow header;
    Vector rows;                // TableRow objects

    int visibleCols;            // number of columns visible
    int visibleRows;            // number of rows visible
    int leftCol;                // index of leftmost visible column
    int topRow;                 // index of topmost visible row

    int selectedRow;            // the currently selected row

    boolean collapsed = false;  // show only the header if true

    /** this ContainerListener implementation tracks content change (i.e.
        Components added or removed) which occurs in any row.
     */
    RowTracker rowTracker = new RowTracker();

    /** the width of vertical lines and height of horizontal lines between
        columns and rows */
    Dimension lineDim = new Dimension(1, 1);

    /** remember the cell insets for when we have to create new rows */
    private Insets cellInsets;

    /** the default insets in each cell */
    static final Insets defaultCellInsets = new Insets(0, 0, 0, 0);

    public Table(int numcolumns, int numrows) {
        this(numcolumns, numrows, defaultCellInsets, null);
    }

    public Table(int numcolumns, int numrows, Insets insets) {
        this(numcolumns, numrows, insets, null);
    }

    public Table(int numcolumns, int numrows, Insets insets, Object data) {
        super();

        Container container = getContainer();
        ComponentFactory.setPaneBorder(container, NO_BORDER);
        container.setBackground(DEFAULT_TABLECOLOR);

        visibleRows = numrows;
        visibleCols = numcolumns;
        leftCol = 0;
        topRow = 0;
        rows = new Vector();
        selectedRow = -1;

        cellInsets = (Insets) insets.clone();

        header = createHeader(visibleCols, cellInsets, data);
        if (header != null) {
            container.add(header.getComponent());
            header.getContainer().addContainerListener(rowTracker);
        }

        for (int i = 0; i < visibleRows; i++) {
            TableRow row = createRow(i, visibleCols, cellInsets, data);
            rows.addElement(row);
            container.add(row.getComponent());
            row.getContainer().addContainerListener(rowTracker);
        }
    }

    class RowTracker implements ContainerListener {
        public void componentAdded(ContainerEvent evt) {
            headerHeight = -1;
            rowHeight = -1;
            getContainer().invalidate();
        }
        public void componentRemoved(ContainerEvent evt) {
            headerHeight = -1;
            rowHeight = -1;
            getContainer().invalidate();
        }
    }


    public boolean isCollapsed() {
        return header != null && header.isCollapsed();
    }

    public void setCollapsed(boolean collapsed) {
        if (isCollapsed() != collapsed) {
            header.setCollapsed(collapsed);
            Component component = getComponent();
            component.invalidate();
            component.validate();
        }
    }

    public boolean isExpandible() {
        return header != null && header.isExpandible();
    }

    public void setExpandible(boolean expandible) {
        header.setExpandible(expandible);
    }


    public int getNumberRows() {
        return rows.size();
    }

    public void setNumberRows(int n, Object data) {
        if (rows.size() < n) {
            rowHeight = -1;
            int nrows = rows.size();
            Container container = getContainer();
            for (int r = nrows; r < n; r++) {
                TableRow row = createRow(r, visibleCols, cellInsets, data);
                rows.addElement(row);
                container.add(row.getComponent());
                row.getContainer().addContainerListener(rowTracker);
            }
        }
    }

    public TableRow getHeader() {
        return header;
    }

    public TableRow getRow(int row) {
        if (row >= 0 && row < rows.size()) {
            return (TableRow) rows.elementAt(row);
        } else {
            return null;
        }
    }


	protected Dimension getSizeForDims(int ncols, int nrows) {
        Insets insets = getContainer().getInsets();
        int width = ncols * getColumnWidth() + insets.left + insets.right;
        int height = getRowHeight(HEADER) + insets.top + insets.bottom;
        for (int i = 0; i < nrows; i++) {
            height += getRowHeight(i);
        }
        return new Dimension(width, height);
    }

    private transient int colWidth = -1;     // calculated width of column
    private transient int rowHeight = -1;    // calculated height of row
    private transient int headerHeight = -1; // calculated height of header
    protected int getColumnWidth() {
        if (colWidth < 0) {
            colWidth = 10 * getEmWidth(false) + cellInsets.left + cellInsets.right + lineDim.width;
        }
        return colWidth;
    }
    protected int getRowHeight(int row) {
        if (row == HEADER) {
            if (headerHeight <= 0) {
                if (header != null) {
                    headerHeight = header.getComponent().getPreferredSize().height;
                } else {
                    headerHeight = 0;
                }
            }
            return headerHeight;

        } else {
            /*****
            if (rowHeight <= 0) {
                rowHeight = 0;
                for (int i = 0; i < rows.size(); i++) {
                    TableRow row = (TableRow) rows.elementAt(i);
                    Dimension prefsize = row.getComponent().getPreferredSize();
                    if (rowHeight < prefsize.height) {
                        rowHeight = prefsize.height;
                    }
                }
            } ***/
            int height;
            if (row < rows.size()) {
                height = ((TableRow) rows.elementAt(row)).getComponent().getPreferredSize().height;
                if (rowHeight <= 0) {
                    rowHeight = height;
                }
            } else {
                height = rowHeight;
            }
            return height;
        }
    }

    public int getEmWidth(boolean header) {
        return getComponent().getFontMetrics((header ? FontPool.getHeaderFont(0) : FontPool.getFont(0))).charWidth('M');
    }

    public Dimension preferredLayoutSize(Container target) {
        int numrows = isCollapsed() ? 0 : getNumberRows();

        while (numrows > 0) {
            TableRow row = getRow(numrows - 1);
            if (!row.isEmpty()) {
                break;
            }
            numrows--;
        }
        return getSizeForDims(visibleCols, numrows);
    }

    public Dimension minimumLayoutSize(Container target) {
        return getSizeForDims(1, 0);
    }

    public void layoutContainer(Container target) {
        Dimension size = target.getSize();
        Insets insets = target.getInsets();

        int x = insets.left;
        int y = insets.top;

        int width = size.width - insets.left - insets.right;
        int headerheight = getRowHeight(HEADER);
        int yvisbottom = size.height - insets.bottom;

        if (header != null) {
            header.getComponent().reshape(x, y, width, headerheight);
        }

        int nrows = (collapsed ? 0 : rows.size());

        y += headerheight;

        for (int i = 0; i < nrows; i++) {
            TableRow row = (TableRow) rows.elementAt(i);
            int rowheight = getRowHeight(i);
            row.getComponent().reshape(x, y, width, rowheight);
            y += rowheight;
            //if (y >= yvisbottom) {
            //    break;
            //}
        }

    }


    /** dump the current document to the console */
    public void dump() {
    }
}

