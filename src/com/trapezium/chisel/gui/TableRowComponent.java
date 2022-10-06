/*
 * @(#)TableRowComponent.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.Beans;
import java.io.*;
import java.net.URL;
import java.util.*;

/*  A TableRowComponent is made up of an array of column Components that are painted
 *  with the ObjectPainter static "paintCell" method.
 *
 *  A TableHeaderComponent extends TableRowComponent.  So all visuals for the
 *  ChiselFileTable are represented here.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */
public class TableRowComponent extends ChiselAWTPane implements ActionListener {

    final static int VARIABLE_WIDTH = -1;
    final static int DEFAULT_COLUMNS = 6;
    final static Insets DEFAULT_INSETS = new Insets(0,0,0,0);

    protected GlyphButton expandButton;

    private Component[] columns;
    private int[] colWidths;
    private boolean[] colVaries;
    private Container expansion;
    private Insets cellInsets;

    public TableRowComponent() {
        this(DEFAULT_COLUMNS, DEFAULT_INSETS);
    }

    public TableRowComponent(int numcolumns) {
        this(numcolumns, DEFAULT_INSETS);
    }

    public TableRowComponent(int numcolumns, Insets cellInsets) {
        super();
        this.cellInsets = (Insets) cellInsets.clone();
        columns = new Component[numcolumns];
        colWidths = new int[numcolumns];
        colVaries = new boolean[numcolumns];
        for (int i = 0; i < numcolumns; i++) {
            colWidths[i] = VARIABLE_WIDTH;
			columns[i] = null;
			colVaries[i] = false;
        }
        setBorder(LOWERED_BORDER);
		setLayout( null );

		expandButton = null;
		expansion = null;
    }

    /** the only action we are hooked into is the expand button */
   	public void actionPerformed( ActionEvent e ) {
       	invalidate();
       	getParent().getParent().getParent().validate();
    }

    public Insets getInsets() {
        return cellInsets;
    }

    public boolean isCollapsed() {
        return (expandButton == null || expandButton.getBooleanValue() == false);
    }

    public void setCollapsed(boolean collapsed) {
        if (collapsed == false) {
            setExpandible(true);
            expandButton.setBooleanValue(true);
        } else if (expandButton != null) {
            expandButton.setBooleanValue(false);
        }
        if (expansion != null) {
            expansion.setVisible(!collapsed);
        }
    }

    public boolean isExpandible() {
        return (expandButton != null);
    }

    public void setExpandible(boolean expandible) {
        if (expandible) {
            if (expandButton == null) {
                GlyphButton gb = new GlyphButton('+','-');
                gb.setInsets(1, 2, 1, 2);
                gb.setHilightDown(true);
                gb.setShape(LabelledImageButton.OVAL);
                gb.setSticky(true);
                gb.addActionListener(this);
                expandButton = gb;
                add(expandButton);
            }
        } else if (expandButton != null) {
            remove(expandButton);
            expandButton.setVisible(false);
            expandButton = null;
        }
    }

    public void setExpansion(Container expansion) {
        if (this.expansion != null) {
            remove(this.expansion);
        }
        this.expansion = expansion;
        if (expansion != null) {
            expansion.setVisible(!isCollapsed());
            add(expansion);
        }
    }

    public void addToExpansion(Component component) {
        if (expansion == null) {
            setExpansion(ComponentFactory.createSimplePane());
            expansion.setLayout(new BorderLayout());
        }
        expansion.add(component);
        Dimension pref = expansion.getPreferredSize();
        expansion.setSize(pref.width, pref.height);
    }

    public void removeFromExpansion(Component component) {
        if (expansion != null) {
            expansion.remove(component);
        }
    }

    public boolean isEmpty() {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != null) {
                return false;
            }
        }
        return true;
    }

    public void setColumnVaries(int column, boolean flag) {
        colVaries[column] = flag;
    }

    public int getNumColumns() {
        return columns.length;
    }

    public Component getColumn(int column) {
        if (column >= 0 && column < columns.length) {
            return columns[column];
        } else {
            return null;
        }
    }

    public void setColumn(int column, Object cellObject) {
    	if (Beans.isInstanceOf(cellObject, Component.class)) {
            setColumn(column, (Component)Beans.getInstanceOf(cellObject, Component.class));
    	} else {
            setColumn(column, new TextLabel(cellObject.getClass().getName()));
    	}
    }

    public void setColumn(int column, Component cellEntry) {
        if (column >= getNumColumns()) {
            setNumColumns(column + 1);
        }

        // remove the last one if any
        if (columns[column] != null) {
            remove(columns[column]);
        }

        columns[column] = cellEntry;

		// add the cell entry to the layout
		if (cellEntry != null) {
    		add( cellEntry );
        }
    }

    /** set the width of a column. */
    public void setColumnWidth(int column, int width) {
        if (column >= getNumColumns()) {
            setNumColumns(column + 1);
        }
        colWidths[column] = width;
    }

    /** set the number of columns in this row */
    public synchronized void setNumColumns(int numcolumns) {
        if (numcolumns != columns.length) {
            int copylen = Math.min(numcolumns, columns.length);
            Component[] newcolumns = new Component[numcolumns];
            System.arraycopy(columns, 0, newcolumns, 0, copylen);
            columns = newcolumns;

            int[] newcolwidths = new int[numcolumns];
            System.arraycopy(colWidths, 0, newcolwidths, 0, copylen);
            if (newcolwidths.length > copylen) {
                for (int i = copylen; i < newcolwidths.length; i++) {
                    newcolwidths[i] = VARIABLE_WIDTH;
                }
            }
            colWidths = newcolwidths;

            boolean[] newcolvaries = new boolean[numcolumns];
            System.arraycopy(colVaries, 0, newcolvaries, 0, copylen);
            if (newcolvaries.length > copylen) {
                for (int i = copylen; i < newcolvaries.length; i++) {
                    newcolvaries[i] = false;
                }
            }
            colVaries = newcolvaries;
        }
    }

    /** compute the width of a variable-width column (i.e. whose width is set
        to VARIABLE_WIDTH). The width is rounded up to guarantee that the entire
        row is occupied by columns. */
    int getVariableColumnWidth() {
        int sumfixed = 0;
        int numvariable = 0;
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] != VARIABLE_WIDTH) {
                sumfixed += colWidths[i];
            }
            if (colVaries[i] || colWidths[i] == VARIABLE_WIDTH) {
                numvariable++;
            }
        }
        int totalwidth = getSize().width;
        // if the component hasn't been sized yet, pick a reasonable value
        if (totalwidth == 0) {
            totalwidth = sumfixed + numvariable * 32;
        }

        if (expandButton != null) {
            totalwidth -= expandButton.getPreferredSize().width + 2;
        } else {
            totalwidth -= extra + 2;
        }

        if (sumfixed < totalwidth && numvariable > 0) {
            return (totalwidth - sumfixed + numvariable - 1) / numvariable;

        } else {
            return 0;
        }
    }

    /** override the color this gui component is initialized to and use the
        parent's color */
    public Color getForeground() {
        return getParent().getForeground();
    }
    public Color getBackground() {
        return getParent().getBackground();
    }

    /** overriden to keep expand button the same color */
    public void paint(Graphics g) {
        if (expandButton != null) {
            expandButton.setForeground(getForeground());
            expandButton.setBackground(getBackground());
        }
        super.paint(g);
    }

    public Dimension getPreferredSize() {
        Dimension prefsize = new Dimension(0, 0);

        if (expandButton != null) {
            prefsize.width += expandButton.getPreferredSize().width + 2;
        } else {
            prefsize.width += extra + 2;
        }

        int vcolwidth = getVariableColumnWidth();
        for (int i = 0; i < columns.length; i++) {
            int cellwidth = colWidths[i];
            if (cellwidth != VARIABLE_WIDTH) {
                prefsize.width += cellwidth;
            }
            Component column = columns[i];
            Dimension cellpref;
            if (column == null) {
                cellpref = new Dimension(cellInsets.left + cellInsets.right, cellInsets.top + cellInsets.bottom);
            } else {
                cellpref = column.getPreferredSize();
                if (!(column instanceof TableCellComponent)) {
                    cellpref.height += cellInsets.top + cellInsets.bottom;
                    cellpref.width += cellInsets.left + cellInsets.right;
                }
            }
            if (prefsize.height < cellpref.height) {
                prefsize.height = cellpref.height;
            }
            if (cellwidth == VARIABLE_WIDTH) {
                prefsize.width += cellpref.width;
            }

        }
        if (!isCollapsed() && expansion != null) {
            Dimension exppref = expansion.getPreferredSize();
            if (prefsize.width < exppref.width) {
                prefsize.width = exppref.width;
            }
            prefsize.height += exppref.height;
        }
        return prefsize;
    }

// this is how someone who doesn't understand the code gets
// columns to align when there is no expansion button (see extra
// used below) -- put expansion button size in a static, then
// just add it to cellx when there is no expansion button
// ..works, but kind of stupid.
static int extra = GlyphButton.getStandardSize().width; //0;
    public void doLayout() {
        int vcolwidth = getVariableColumnWidth();
        Dimension size = getSize();


        // the cell rectangle (x and width get set in the loop below)
        Rectangle cellRect = new Rectangle(0, 0, 0, size.height);
        if (!isCollapsed() && expansion != null) {
            cellRect.height -= expansion.getSize().height;
        }

        int cellx = 0;
        int cellwidth = 0;

        if (expandButton != null) {
            cellx = 2;
            Dimension expsize = expandButton.getPreferredSize();
            int y = cellRect.y + (cellRect.height - expsize.height) / 2;
            expandButton.setBounds(cellx, y, expsize.width, expsize.height);
            cellx += expsize.width;
            //extra = expsize.width;
        } else {
            cellx += extra + 2;
        }

        for (int i = 0; i < columns.length; i++) {
            if (cellx >= size.width) {
                break;
            }
            cellwidth = colWidths[i];
            if (cellwidth == VARIABLE_WIDTH) {
                cellwidth = vcolwidth;
            } else if (colVaries[i]) {
                cellwidth += vcolwidth;
            }
            cellRect.x = cellx;
            cellRect.width = cellwidth;
            Component column = columns[i];
            if (column != null) {
                if (column instanceof TableCellComponent) {
            		column.setBounds( cellRect );
                } else {
            		column.setBounds( cellRect.x + cellInsets.left, cellRect.y + cellInsets.top, cellRect.width - cellInsets.left - cellInsets.right, cellRect.height - cellInsets.top - cellInsets.bottom );
                }
            }
            cellx += cellwidth;
        }
        if (expansion != null) {
            int y = cellRect.y + cellRect.height;
            expansion.setBounds( 0, y, size.width, expansion.getSize().height);
            expansion.setVisible(!isCollapsed());
        }
    }
}

