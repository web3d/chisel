/*  ConstrainedMover
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;

public class ConstrainedMover extends Mover {

    public final static int EASTWEST = Cursor.W_RESIZE_CURSOR;
    public final static int NWSE = Cursor.NW_RESIZE_CURSOR;
    public final static int NORTHSOUTH = Cursor.N_RESIZE_CURSOR;
    public final static int NESW = Cursor.NE_RESIZE_CURSOR;
    public final static int NONE = Cursor.DEFAULT_CURSOR;

    int direction = NONE;
    boolean constrainedToParent = true;
    Insets insets = new Insets(0, 0, 0, 0);

    public ConstrainedMover() {
        super();
    }

    public ConstrainedMover(Component component) {
        super(component);
    }

    public ConstrainedMover(Component component, boolean trackParent) {
        super(component, trackParent);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setConstrainedToParent(boolean constrained) {
        constrainedToParent = constrained;
    }

    public boolean isConstrainedToParent() {
        return constrainedToParent;
    }

    public void setInsets(Insets insets) {
        this.insets.left = insets.left;
        this.insets.top = insets.top;
        this.insets.right = insets.right;
        this.insets.bottom = insets.bottom;
    }

    public void mouseDragged(MouseEvent evt) {
        if (direction == NONE) {
            return;
        }
        int x = evt.getX();
        int y = evt.getY();
        if (x != anchorx || y != anchory) {
            doMove(x, y);
        }
    }


    public void doMove(int x, int y) {

        if (direction == NONE) {
            return;
        }

        Component target = getTarget();
        synchronized (target) {
            Point pt = target.getLocation();

            int xnew = pt.x;
            int ynew = pt.y;

            boolean moveHoriz = (direction == EASTWEST || direction == NWSE || direction == NESW);
            boolean moveVert = (direction == NORTHSOUTH || direction == NWSE || direction == NESW);

            int dx = x - anchorx;
            int dy = y - anchory;

            if (moveHoriz) {
                xnew += dx;
            }
            if (moveVert) {
                ynew += dy;
            }

            if (isConstrainedToParent()) {
                Dimension size = target.getSize();
                Dimension parentsize = target.getParent().getSize();

                // in both horizontal and vertical checks, check <= edge
                // rather than < edge to prevent oscillation when the
                // component is bigger than parent

                if (moveHoriz) {
                    if (xnew <= insets.left) {
                        xnew = insets.left;
                    } else if (xnew + size.width > parentsize.width - insets.right) {
                        xnew = parentsize.width - insets.right - size.width;
                    }
                }

                if (moveVert) {
                    if (ynew <= insets.top) {
                        ynew = insets.top;
                    } else if (ynew + size.height > parentsize.height - insets.bottom) {
                        ynew = parentsize.height - insets.bottom - size.height;
                    }
                }
            }

            if (pt.x != xnew || pt.y != ynew) {
                target.setLocation(xnew, ynew);
                Dimension size = target.getSize();
                target.getParent().repaint(Math.min(pt.x, xnew), Math.min(pt.y, ynew), size.width + Math.max(xnew - pt.x, pt.x - xnew), size.height + Math.max(ynew - pt.y, pt.y - ynew));
            }
        }
    }
}

