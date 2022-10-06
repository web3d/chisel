/*
	TextLabel.java
*/

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;


/** A lightweight label */
public class TextLabel extends Component implements LabelConstants {

	protected String label = null;
	protected int align;
    protected Insets margin = new Insets(0, 0, 0, 0);

	public TextLabel( String label ) {
	    this.label = label;
	    setFont(FontPool.getLabelFont());
	    align = TEXT_ALIGN_LEFT | TEXT_ALIGN_VCENTER;
	}

	public TextLabel( String label, Font font, int align ) {
	    this.label = label;
	    setFont(font);
	    this.align = align;
	}

	public void setAlign(int align) {
	    this.align = align;
	}

	public void setMargin(int top, int left, int bottom, int right) {
	    margin.top = top;
	    margin.left = left;
	    margin.bottom = bottom;
	    margin.right = right;
	}

	public String getText() {
		return label;
	}

	public void setText( String s ) {
		label = s;
		repaint();
	}

	/** draw the image and the label in a specified rectangle. */
	public void paint(Graphics g) {
		Dimension size = getSize();
        Rectangle bounds = new Rectangle(margin.left, margin.top, size.width - margin.left - margin.right, size.height - margin.top - margin.bottom);

		// get the label dimensions
		int labdx = 0;
		int labdy = 0;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null) {
			fm = getFontMetrics(font);
			labdx = fm.stringWidth(label);
			labdy = fm.getHeight();
		}

		Font originalFont = g.getFont();
		g.setFont( font );
        g.setColor(getForeground());
		int x, y;
		int a;
		if (labdx > 0) {
			a = align & TEXT_ALIGN_HORZ;
			if (a == TEXT_ALIGN_LEFT) {
				x = bounds.x;
			} else if (a == TEXT_ALIGN_RIGHT) {
				x = bounds.x + bounds.width - labdx;
			} else { 	// TEXT_ALIGN_CENTER
				x = bounds.x + (bounds.width - labdx) / 2;
			}
			a = align & TEXT_ALIGN_VERT;
			if (a == TEXT_ALIGN_TOP) {
				y = bounds.y;
			} else if (a == TEXT_ALIGN_BOTTOM) {
				y = bounds.y + bounds.height - labdy;
            } else if (a == TEXT_ALIGN_VCENTERASCENT) {
				y = bounds.y + (bounds.height - labdy + fm.getDescent()) / 2;
			} else { 	// TEXT_ALIGN_VCENTER
				y = bounds.y + (bounds.height - labdy) / 2;
			}
			g.drawString(label, x, y + fm.getAscent());
		}
		g.setFont( originalFont );
	}

    /**
    * The preferred size of the button.
    */
    public Dimension getPreferredSize() {
        Dimension dim = getMinimumSize();
        dim.width += 2;
        dim.height += 2;
        return dim;
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
            // the fontmetrics seem to be off sometimes so add a fudge factor
			labdx += fm.stringWidth(label) + (label.length() > 0 ? 16 : 0);
			labdy += fm.getHeight() + fm.getLeading();
		}
        return new Dimension(labdx + margin.left + margin.right, labdy + margin.top + margin.bottom);
    }
}

