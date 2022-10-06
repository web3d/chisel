/*  ChiselAWTDividedPane
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;


public class ChiselAWTDividedPane extends ChiselAWTPane implements ActionListener {

    DividerBarComponent db;
    OrientationButton ob;

    public ChiselAWTDividedPane(int align, Component before, Component after) {
        super();
        setOpaque(false);
        setLayout(null);
        setBackground(Color.gray);
        ob = new OrientationButton();
        ob.addActionListener(this);
        add(ob);
        add(before);
        add(after);
        db = new DividerBarComponent(align, before, after);
        add(db);
    }

    public Dimension getMinimumSize() {
        Dimension dim = db.getMinimumSize();;
        Dimension minbefore = db.getComponentBefore().getMinimumSize();
        Dimension minafter = db.getComponentAfter().getMinimumSize();
        if (db.getAlign() == DividerBarComponent.HORIZONTAL) {
            dim.width += minbefore.width + minafter.width;
            dim.height = Math.max(dim.height, Math.max(minbefore.height, minafter.height));
        } else {
            dim.width = Math.max(dim.width, Math.max(minbefore.width, minafter.width));
            dim.height += minbefore.height + minafter.height;
        }
        return dim;
    }

    public Dimension getPreferredSize() {
        Dimension dim = db.getPreferredSize();
        Dimension prefbefore = db.getComponentBefore().getPreferredSize();
        Dimension prefafter = db.getComponentAfter().getPreferredSize();
        if (db.getAlign() == DividerBarComponent.HORIZONTAL) {
            dim.width += prefbefore.width + prefafter.width;
            dim.height = Math.max(dim.height, Math.max(prefbefore.height, prefafter.height));
        } else {
            dim.width = Math.max(dim.width, Math.max(prefbefore.width, prefafter.width));
            dim.height += prefbefore.height + prefafter.height;
        }
        return dim;
    }

    int yHoriz = -1;
    int xVert = -1;
    public void doLayout() {
        db.layoutContainer();
        Rectangle rect = db.getBounds();
        if (db.getAlign() == DividerBarComponent.HORIZONTAL) {
            // remember the x position in case we switch alignment
            xVert = rect.x + rect.width/2;
            rect.x -= 5;
            rect.width += 10;
            if (yHoriz < 0) {
                yHoriz = rect.height/2;
            }
            rect.height = rect.width;
            rect.y = yHoriz - rect.height/2;
        } else {
            // remember the y position in case we switch alignment
            yHoriz = rect.y + rect.height/2;
            rect.y -= 5;
            rect.height += 10;
            if (xVert < 0) {
                xVert = rect.width/2;
            }
            rect.width = rect.height;
            rect.x = xVert - rect.width/2;
        }
        ob.setBounds(rect);
        ob.setAlign(db.getAlign());
        super.doLayout();
    }

    /** rotate the pane from horizontal to vertical or vice versa */
   	public void actionPerformed( ActionEvent e ) {
   	    Component comp = db.getComponentBefore();
   	    Dimension totalsize = getSize();
   	    Dimension compsize = comp.getSize();
   	    if (db.getAlign() == DividerBarComponent.HORIZONTAL) {
       	    db.setAlign(DividerBarComponent.VERTICAL);
       	    compsize.height = yHoriz;  // totalsize.height * compsize.width / totalsize.width;
       	    compsize.width = totalsize.width;
        } else {
       	    db.setAlign(DividerBarComponent.HORIZONTAL);
       	    compsize.width = xVert; // totalsize.width * compsize.height / totalsize.height;
       	    compsize.height = totalsize.height;
        }
   	    comp.setSize(compsize);
   	    validate();
   	}
}

class OrientationButton extends LabelledImageButton implements DisplayConstants {

    int align = DividerBarComponent.HORIZONTAL;

    final static Color bg = new Color(204, 153, 51);
    public OrientationButton() {
        super( "", null );
        setBackground( bg /*DEFAULT_CONTROLCOLOR*/);
    }

    public void setAlign(int align) {
        this.align = align;
    }

	public void paint(Graphics g) {
 		Dimension size = getSize();
		int x = insets.left;
		int y = insets.top;
		int width = size.width - insets.left - insets.right;
		int height = size.height - insets.top - insets.bottom;

		Color bg = getBackground();
        Color hiliteColor = bg.brighter();
        Color liteShadowColor = bg.darker();
        Color shadowColor = liteShadowColor.darker();

        // bar indicating alignment if pushed
        g.setColor(shadowColor);
        int dxy = 4;
        int barx, bary, barwidth, barheight;
        if (align == DividerBarComponent.HORIZONTAL) {
            barx = x;
            bary = y + (height / 2) - (dxy / 2);
            barwidth = width;
            barheight = dxy;
            for (int xx = barx - dxy; xx < barx + barwidth - 1; xx += 2) {
                g.drawLine(xx + 1, bary, xx + dxy, bary + dxy - 1);
            }
        } else {
            barx = x + (width / 2) - (dxy / 2);
            bary = y;
            barwidth = dxy;
            barheight = height;
            for (int yy = bary - dxy; yy < bary + barheight - 1; yy += 2) {
                g.drawLine(barx, yy + 1, barx + dxy - 1, yy + dxy);
            }
        }

        // shrink the square we're drawing in
        x += 4;
        y += 4;
        width -= 8;
        height -= 8;

        g.setColor(bg);
        g.drawOval(x, y, width - 1, height - 1);
        g.drawOval(x, y + 1, width - 1, height - 2);
        g.drawOval(x, y, width - 1, height - 2);

        // button is drawn in the down state when any of the following are true:
        //
        //   -- the button is in a sticky down state
        //   -- the button is pressed and the mouse is within the bounds of the button
        //   -- the margins are symmetrical
        if (stickyDown || (pressed && contains(mousex, mousey)) || (margin.top == margin.bottom && margin.left == margin.right)) {
    		g.setColor(shadowColor);
    		g.drawOval(x, y, width - 1, height - 1);

        // button up
        } else {
    		g.setColor(shadowColor);
	    	g.drawOval(x + 1, y + 1, width - 2, height - 2);
    		g.setColor(hiliteColor);
	    	g.drawOval(x, y, width - 2, height - 2);
    		//g.setColor(liteShadowColor);
		    //g.drawLine(x + 1, y + 1, width - 2, height - 2);

            g.setColor(bg);
            g.drawOval(x + 1, y + 1, width - 3, height - 3);
        }

	}
}
