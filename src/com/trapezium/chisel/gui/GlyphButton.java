/*
 * @(#)GlyphButton.java
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


/** A button containing a glyph (see Glyph.java)
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.1, 29 Jan 1998
 *
 *  @since           1.0
 */
public class GlyphButton extends LabelledImageButton {


    /** if the glyph is set to any of the following values,
        a special symbol is drawn.  Otherwise the glyph is simply
        cast to a single character and drawn. */
    public static final int CLOSE = -1;
    public static final int CHECK = -2;     // checkmark
    public static final int UP = -3;        // up triangle
    public static final int DOWN = -4;      // down triangle
    public static final int DOT = -5;

    // these are in reverse so that RUN0 < RUN3
    public static final int RUN3 = -6;
    public static final int RUN2 = -7;
    public static final int RUN1 = -8;
    public static final int RUN0 = -9;

    // style-sensitive glyphs
    public static final int MINIMIZE = -10;
    public static final int MAXIMIZE = Integer.MAX_VALUE - 1;
    public static final int RESTORE = Integer.MAX_VALUE - 2;

    int glyph;
    int pressedGlyph;     // actually (pressed || stickyDown)

	public GlyphButton(int glyph) {
        this(glyph, glyph);
    }

    public GlyphButton(int glyph, int pressedGlyph) {
        super();
        this.glyph = glyph;
        this.pressedGlyph = pressedGlyph;
        setBackground(Color.lightGray);

        // by default, glyph buttons don't do rollover
        enableRollover(false);
	}

	public void setGlyph(int glyph) {
	    this.glyph = glyph;
	    repaint();
	}

	public void setGlyphs(int glyph, int pressedGlyph) {
	    this.glyph = glyph;
        this.pressedGlyph = pressedGlyph;
	    repaint();
	}

	/** draw the image and the label in a specified rectangle. */
	public void paint(Graphics g) {
		drawBorder(g);
        Dimension size = getSize();
        int x = margin.left + insets.left;
        int y = margin.top + insets.top;
        int width = size.width - margin.left - margin.right - insets.left - insets.right;
        int height = size.height - margin.top - margin.bottom - insets.top - insets.bottom;
		paint( g, x, y, width, height );
	}

	public void paint( Graphics g, Rectangle cellRect ) {
		paint( g, cellRect.x, cellRect.y, cellRect.width, cellRect.height );
	}

	void paint( Graphics g, int x, int y, int width, int height ) {
	    g.setColor((pressed || stickyDown) ? getForeground() : getForeground().brighter());
        int glyph = (pressed || stickyDown) ? pressedGlyph : this.glyph;

        switch (glyph) {
            case CLOSE:
                Glyph.drawClose(g, x, y, width, height);
                break;
            case CHECK:
                Glyph.drawCheck(g, x, y, width, height);
                break;
            case UP:
                Glyph.drawUp(g, x, y, width, height);
                break;
            case DOWN:
                Glyph.drawDown(g, x, y, width, height);
                break;
            case DOT:
                Glyph.drawDot(g, x, y, width, height);
                break;
            case RUN0:
            case RUN1:
            case RUN2:
            case RUN3:
                Glyph.drawRun(g, glyph - RUN0, x, y, width, height);
                break;
            case MINIMIZE:
                Glyph.drawMinimize(g, x, y, width, height);
                break;
            case MAXIMIZE:
                Glyph.drawMaximize(g, x, y, width, height);
                break;
            case RESTORE:
                Glyph.drawRestore(g, x, y, width, height);
                break;
            default:
                drawChar(g, (char) glyph, x, y, width, height);
                break;
        }
    }

    private static char[] cc = new char[1];
    public void drawChar(Graphics g, char c, int x, int y, int width, int height) {
		Font font = getFont();
		FontMetrics fm = getFontMetrics(font);
		int dx = fm.charWidth(c);
		int dy = fm.getHeight();

        g.setColor(getForeground());
        g.setFont(font);
		int a;

		a = align & TEXT_ALIGN_HORZ;
		if (a == TEXT_ALIGN_RIGHT) {
			x += width - dx;
		} else if (a == TEXT_ALIGN_CENTER) {
			x += (width - dx) / 2;
		}
		a = align & TEXT_ALIGN_VERT;
		if (a == TEXT_ALIGN_BOTTOM) {
			y += height - dy;
		} else if (a == TEXT_ALIGN_VCENTER) {
			y += (height - dy) / 2;
		}
		synchronized (cc) {
        	cc[0] = c;
        	g.drawChars(cc, 0, 1, x, y + fm.getAscent());
        }
	}

    /**
     * The minimum size of the button.
     */

    static final Dimension stdGlyphSize = new Dimension(7, 5);
    public Dimension getMinimumSize() {
		int dx = stdGlyphSize.width;
		int dy = stdGlyphSize.height;
		FontMetrics fm = null;
		Font font = getFont();
		if (label != null && font != null) {
			fm = getFontMetrics(font);
			dx = fm.charWidth('M');
			dy = fm.getHeight() - fm.getLeading();
		}
        return new Dimension(dx + margin.left + margin.right + insets.left + insets.right, dy + margin.top + margin.bottom + insets.top + insets.bottom);
    }

    public static Dimension getStandardSize() {
        return new Dimension(stdGlyphSize.width + stdMargin.left + stdMargin.right + 4, stdGlyphSize.height + stdMargin.top + stdMargin.bottom + 4);
    }
}

