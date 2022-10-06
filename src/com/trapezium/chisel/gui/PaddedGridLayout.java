/*
 * @(#)PaddedGridLayout.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 */
package com.trapezium.chisel.gui;

import java.awt.*;

/** a GridLayout with an independently adjustable margin */
public class PaddedGridLayout extends GridLayout {

    int hmargin;
    int vmargin;
    boolean hasbottommargin = true;

    public PaddedGridLayout() {
	    this(1, 0, 0, 0, 0, 0);
    }

    public PaddedGridLayout(int rows, int cols) {
    	this(rows, cols, 0, 0, 0, 0);
    }

    public PaddedGridLayout(int rows, int cols, int hgap, int vgap) {
    	this(rows, cols, hgap, vgap, 0, 0);
    }

    public PaddedGridLayout(int rows, int cols, int hgap, int vgap, int hmargin, int vmargin) {
    	super(rows, cols, hgap, vgap);
    	this.hmargin = hmargin;
    	this.vmargin = vmargin;
    }

    public PaddedGridLayout(int rows, int cols, int hgap, int vgap, int hmargin, int vmargin, boolean bottommargin) {
    	this(rows, cols, hgap, vgap, hmargin, vmargin);
    	this.hasbottommargin = bottommargin;
    }

	public int getHmargin() {
	    return hmargin;
	}

	public void setHmargin(int hmargin) {
	    this.hmargin = hmargin;
	}

	public int getVmargin() {
	    return vmargin;
	}

	public void setVmargin(int vmargin) {
	    this.vmargin = vmargin;
	}
	
	public boolean hasBottomMargin() {
	    return hasbottommargin;
	}
	
	public void setBottomMargin(boolean bottommargin) {
	    hasbottommargin = bottommargin;
	}
	
	public Dimension preferredLayoutSize(Container parent) {
        Dimension size = super.preferredLayoutSize(parent);
        size.width += 2 * hmargin;
        size.height += (hasbottommargin ? 2 : 1) * vmargin;
        return size;
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension size = super.minimumLayoutSize(parent);
        size.width += 2 * hmargin;
        size.height += (hasbottommargin ? 2 : 1) * vmargin;
        return size;
    }

    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
        	Insets insets = parent.getInsets();
        	int ncomponents = parent.getComponentCount();
        	int nrows = getRows();
        	int ncols = getColumns();
        	int hgap = getHgap();
        	int vgap = getVgap();

        	if (ncomponents == 0) {
        	    return;
        	}
        	if (nrows > 0) {
        	    ncols = (ncomponents + nrows - 1) / nrows;
        	} else {
        	    nrows = (ncomponents + ncols - 1) / ncols;
        	}

        	Dimension size = parent.getSize();
        	int width = size.width - (insets.left + insets.right) - 2 * hmargin;
        	int height = size.height - (insets.top + insets.bottom) - (hasbottommargin ? 2 : 1) * vmargin;
        	width = (width - (ncols - 1) * hgap) / ncols;
        	height = (height - (nrows - 1) * vgap) / nrows;

            int x = insets.left + hmargin;
            int y = insets.top + vmargin;
        	for (int c = 0; c < ncols; c++) {
        	    for (int r = 0; r < nrows; r++) {
        		    int i = r * ncols + c;
        		    if (i < ncomponents) {
        		        parent.getComponent(i).setBounds(x, y, width, height);
        		    }
        		    y += height + vgap;
        	    }
        	    x += width + hgap;
        	}
        }
    }
}
