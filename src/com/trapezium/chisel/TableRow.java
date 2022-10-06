/*
 * @(#)TableRow.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.awt.*;

import com.trapezium.chisel.gui.LabelConstants;
import com.trapezium.chisel.gui.TableRowComponent;
import com.trapezium.chisel.gui.TableHeaderComponent;

/**
 *  A horizontal slice of a table. The TableRowComponent is the visual
 *  representation of the row.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 30 Jan 1998
 *
 *  @since           1.0
 */
public class TableRow extends ChiselPane implements LabelConstants {
    public TableRow(TableRowComponent rowComponent) {
        super(rowComponent);
    }
    public TableRow(int numColumns, Insets cellInsets) {
        this(numColumns, cellInsets, false);
    }
    public TableRow(int numColumns, Insets cellInsets, boolean header) {
        super(header ? new TableHeaderComponent(numColumns, cellInsets) : new TableRowComponent(numColumns, cellInsets));
    }

    public boolean isExpandible() {
		TableRowComponent row = (TableRowComponent)getComponent();
		return row.isExpandible();
    }

	public void setExpandible( boolean expandible ) {
		TableRowComponent row = (TableRowComponent)getComponent();
		row.setExpandible( expandible );
	}

	public void addToExpansion( Component comp ) {
		TableRowComponent row = (TableRowComponent)getComponent();
		row.addToExpansion( comp );
    }

	public void removeFromExpansion( Component comp ) {
		TableRowComponent row = (TableRowComponent)getComponent();
		row.removeFromExpansion( comp );
    }

	public boolean isCollapsed() {
		TableRowComponent row = (TableRowComponent)getComponent();
	    return row.isCollapsed();
	}

    public void setCollapsed(boolean collapsed) {
		TableRowComponent row = (TableRowComponent)getComponent();
		row.setCollapsed( collapsed );
    }

	public void setColumn( int i, Component a ) {
		TableRowComponent row = (TableRowComponent)getComponent();
		row.setColumn( i, a );
	}

	public void setColumnWidth( int i, int width ) {
		TableRowComponent row = (TableRowComponent)getComponent();
		row.setColumnWidth( i, width );
	}

	public Component getColumn(int column) {
		TableRowComponent row = (TableRowComponent)getComponent();
		return row.getColumn( column );
    }

    public boolean isEmpty() {
		TableRowComponent row = (TableRowComponent) getComponent();
		return row.isEmpty();
    }
}


