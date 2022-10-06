/*
	TextEdit.java
*/

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;


/** A lightweight label */
public class TextEdit extends TextLabel {

	public TextEdit( String label ) {
	    super(label);
        setMargin(0, 4, 0, 4);
	}

	public TextEdit( String label, Font font, int align ) {
	    super(label, font, align);
        setMargin(0, 4, 0, 4);
	}

    /** draw the image and the label in a specified rectangle. */
	public void paint(Graphics g) {
	    Dimension size = getSize();
        g.setColor(getBackground());
        g.fillRect(1, 1, size.width - 2, size.height - 2);

	    super.paint(g);

        g.setColor(getBackground());
        g.draw3DRect(0, 0, size.width - 1, size.height - 1, false);
	}


    /**
     * The preferred size of the button.
     */
    public Dimension getPreferredSize() {
		// get the label dimensions
		int labdx = 2;
		int labdy = 0;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null) {
			fm = getFontMetrics(font);
			labdx += Math.max(fm.stringWidth(label), fm.stringWidth("MMM"));
			labdy += fm.getHeight();
		}
        return new Dimension(labdx + margin.left + margin.right, labdy + margin.top + margin.bottom);
    }

    /**
     * The minimum size of the button.
     */
    public Dimension getMinimumSize() {
		// get the label dimensions
		int labdx = 0;
		int labdy = 0;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null) {
			fm = getFontMetrics(font);
			labdx = Math.max(fm.stringWidth(label), fm.stringWidth("M"));
			labdy = fm.getHeight();
		}
        return new Dimension(labdx + margin.left + margin.right, labdy + margin.top + margin.bottom);
    }
}

