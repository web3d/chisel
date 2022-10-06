/*  Sizer
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;

//import com.trapezium.chisel.*;


public class Sizer extends Mover {

    final static int cornerWidth = 16;
    final static int cornerHeight = 16;

    public final static int WEST = Cursor.W_RESIZE_CURSOR;
    public final static int NW = Cursor.NW_RESIZE_CURSOR;
    public final static int NORTH = Cursor.N_RESIZE_CURSOR;
    public final static int NE = Cursor.NE_RESIZE_CURSOR;
    public final static int EAST = Cursor.E_RESIZE_CURSOR;
    public final static int SE = Cursor.SE_RESIZE_CURSOR;
    public final static int SOUTH = Cursor.S_RESIZE_CURSOR;
    public final static int SW = Cursor.SW_RESIZE_CURSOR;
    public final static int NONE = Cursor.DEFAULT_CURSOR;

    int direction = NONE;

    private Cursor oldCursor;

    public Sizer() {
        super();
    }

    public Sizer(Component component) {
        super(component);
    }

    public Sizer(Component component, boolean trackParent) {
        super(component, trackParent);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void mouseMoved(MouseEvent evt) {
        //System.out.print(" Sizer mouseMoved --");
        Component target = getTarget();
        int x = evt.getX();
        int y = evt.getY();
        synchronized (target) {
            if (trackAncestor > 0) {
                Point pt = target.getLocation();
                x += pt.x;
                y += pt.y;
            }

            Cursor cursor = target.getCursor();
            int cursortype = (cursor == null ? NONE : cursor.getType());
            int sizeType = getSizeType(x, y);
            if (sizeType != cursortype) {
                Cursor newCursor = Cursor.getPredefinedCursor(sizeType);
                target.setCursor(newCursor != null ? newCursor : Cursor.getDefaultCursor());
            }
        }
    }

    public void mouseEntered(MouseEvent evt) {
        //System.out.print(" Sizer mouseEntered -[]-");
        Component target = getTarget();
        synchronized (target) {
            int x = evt.getX();
            int y = evt.getY();
            if (trackAncestor > 0) {
                Point pt = target.getLocation();
                x += pt.x;
                y += pt.y;
            }
            int cursortype = getSizeType(x, y);
            if (cursortype != NONE) {
                oldCursor = target.getCursor();
                Cursor newCursor = Cursor.getPredefinedCursor(cursortype);
                target.setCursor(newCursor != null ? newCursor : Cursor.getDefaultCursor());
            }
        }
    }

    public void mouseExited(MouseEvent evt) {
        //System.out.print(" Sizer mouseExited -X-");
        Component target = getTarget();
        synchronized (target) {
            target.setCursor(oldCursor != null ? oldCursor : Cursor.getDefaultCursor());
        }
    }

    public void mousePressed(MouseEvent evt) {
        super.mousePressed(evt);
        Component target = getTarget();
        int x = evt.getX();
        int y = evt.getY();
        if (trackAncestor > 0) {
            Point pt = target.getLocation();
            x += pt.x;
            y += pt.y;
        }
        setDirection(getSizeType(x, y));
    }

    public void mouseReleased(MouseEvent evt) {
        super.mousePressed(evt);
        if (direction != NONE) {
            setDirection(NONE);
            Component target = getTarget();
            int x = evt.getX();
            int y = evt.getY();
            if (trackAncestor > 0) {
                Point pt = target.getLocation();
                x += pt.x;
                y += pt.y;
            }
            int cursortype = getSizeType(x, y);
            Cursor newCursor = null;
            if (cursortype != NONE) {
                newCursor = Cursor.getPredefinedCursor(cursortype);
            } else {
                newCursor = oldCursor;
            }
            if (newCursor == null) {
                newCursor = Cursor.getDefaultCursor();
            }
            target.setCursor(newCursor);
        }
    }

    public void mouseDragged(MouseEvent evt) {
        if (direction == NONE) {
            return;
        }
        int x = evt.getX();
        int y = evt.getY();
        if (x != anchorx || y != anchory) {
            doResize(x, y);
        }
    }

    protected int getSizeType(int x, int y) {

        Dimension size = getTarget().getSize();

        boolean sizeWest = false;
        boolean sizeNorth = false;
        boolean sizeEast = false;
        boolean sizeSouth = false;
        if (x < cornerWidth) {
            sizeWest = true;
        }
        if (y < cornerHeight) {
            sizeNorth = true;
        }
        if (x >= size.width - cornerWidth) {
            sizeEast = true;
        }
        if (y >= size.height - cornerHeight) {
            sizeSouth = true;
        }

        // check for corneroverlap
        if (sizeWest && sizeEast) {
            sizeWest = false;
            sizeEast = false;
        }
        if (sizeNorth && sizeSouth) {
            sizeNorth = false;
            sizeSouth = false;
        }

        // return the code for the particular combination of
        // directions being sized
        if (sizeWest) {
            if (sizeNorth) {
                return NW;
            } else if (sizeSouth) {
                return SW;
            } else {
                return WEST;
            }
        } else if (sizeEast) {
            if (sizeNorth) {
                return NE;
            } else if (sizeSouth) {
                return SE;
            } else {
                return EAST;
            }
        } else if (sizeNorth) {
            return NORTH;
        } else if (sizeSouth) {
            return SOUTH;
        }
        return NONE;
    }

    public void doResize(int x, int y) {

        if (direction == NONE) {
            return;
        }

        Component target = getTarget();
        synchronized (target) {
            Rectangle box = target.getBounds();

            int xnew = box.x;
            int ynew = box.y;
            int wnew = box.width;
            int hnew = box.height;

            boolean sizeWest = (direction == WEST || direction == NW || direction == SW);
            boolean sizeNorth = (direction == NORTH || direction == NW || direction == NE);
            boolean sizeEast = (direction == EAST || direction == NE || direction == SE);
            boolean sizeSouth = (direction == SOUTH || direction == SE || direction == SW);

            int dx = x - anchorx;
            int dy = y - anchory;

            if (sizeWest || sizeEast) {
                if (sizeWest) {
                    xnew += dx;
                    wnew -= dx;
                } else {
                    wnew += dx;
                    anchorx += dx;
                }
            }
            if (sizeNorth || sizeSouth) {
                if (sizeNorth) {
                    ynew += dy;
                    hnew -= dy;
                } else {
                    hnew += dy;
                    anchory += dy;
                }
            }

            Dimension mindim = target.getMinimumSize();
            int minWidth = mindim.width;
            int minHeight = mindim.height;

            if (wnew < minWidth) {
                if (sizeWest) {
                    xnew += minWidth - wnew;
                }
                wnew = minWidth;
            }
            if (hnew < minHeight) {
                if (sizeNorth) {
                    ynew += minHeight - hnew;
                }
                hnew = minHeight;
            }

            if (box.x != xnew || box.y != ynew || box.width != wnew || box.height != hnew) {
                target.setBounds(xnew, ynew, wnew, hnew);
                target.validate();
                //target.getParent().repaint(Math.min(box.x, xnew), Math.min(box.y, ynew), Math.max(box.width, wnew), Math.max(box.height, hnew));
            }
        }
    }
}

