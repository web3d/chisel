/*
    SelectOperation.java
*/

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;

/** an abstract selection operation */
public class SelectOperation extends Rectangle {

    Component target;
    Insets insets;
    int xAnchor;
    int yAnchor;
    final static Color selColor = Color.white;
    boolean show;


    public SelectOperation(Component target, int x, int y) {
        super(target.getBounds());
        this.target = target;
        insets = null;
        xAnchor = x;
        yAnchor = y;
        show = false;
    }


    public Component getTarget() {
        return target;
    }

    Graphics getGraphics() {
        Graphics g = target.getGraphics();
        Point cpt = getContainerOffset();
        Point tpt = getLocation();
        g.translate(cpt.x + tpt.x, cpt.y + tpt.y);
        return g;
    }

    Point getContainerOffset() {
        Point pt = new Point(0, 0);
        Container c = target.getParent();
        while (c != null) {
            Point cpt = c.getLocation();
            pt.x += cpt.x;
            pt.y += cpt.y;
            c = c.getParent();
        }
        return pt;
    }

    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    // Note: the reason both repaint() and draw() check the show flag is
    // to avoid the getGraphics() call in repaint() but still handle
    // cases in which draw is called directly


    public void repaint() {

        if (!show) {
            return;
        }

        Graphics g = getGraphics();
        draw(g);

        // should we be cleaning up here?  I don't know.
        g.dispose();
        g = null;
    }


    public void draw(Graphics g) {

        if (!show) {
            return;
        }

        g.setColor(Color.black);
        g.setXORMode(selColor);
        int thick = 4; //((box instanceof WindowWidget && ((WindowWidget)box).hasSizeBox()) ? 4 : 1);
        Point pt = target.getLocation();
        int xx = x - pt.x;
        int yy = y - pt.y;
        int ww = width;
        int hh = height;
        if (insets != null) {
            xx += insets.left;
            yy += insets.top;
            ww -= insets.left + insets.right;
            hh -= insets.top + insets.bottom;
        }
        for (int i = 0; i < thick; i++) {
            g.drawRect(xx + i, yy + i, ww - 2 * i - 1, hh - 2 * i - 1);
        }
        g.setPaintMode();
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.sync();
    }


    synchronized public void move(int x, int y) {
        Point pt = target.getLocation();
        int xx = pt.x + (x - xAnchor);
        int yy = pt.y + (y - yAnchor);
        if (this.x != xx || this.y != yy) {
            Graphics g = getGraphics();
            draw(g);
            this.x = xx;
            this.y = yy;
            show = true;
            draw(g);
            g.dispose();
            g = null;
        }
    }

    public void act(int x, int y) {
        Rectangle box = target.getBounds();
        int xx = this.x;
        int yy = this.y;
        if (xx != box.x || yy != box.y || width != box.width || height != box.height) {
            Container p = target.getParent();
            //if (p != null && p instanceof Workspace) {
            //    Dimension size = p.size();
            //    xx = Math.min(xx, size.width - 6);
            //    yy = Math.min(yy, size.height - 6);
            //}
            xx = Math.max(xx, -box.width + 6);
            yy = Math.max(yy, -6);

            target.setBounds(xx, yy, width, height);
        }
    }

    public void clear() {
        if (!show) {
            return;
        }
        Graphics g = getGraphics();
        draw(g);
        g.dispose();
        g = null;
    }
}

