/*
 *  DividerBarComponent.java
 */

package com.trapezium.chisel.gui;

import java.awt.event.*;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Frame;
import java.awt.Container;


/** An adjustable divider between two sizeable objects */
public class DividerBarComponent extends Component {

    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;

    public final static int minHeight = 4;
    public final static int minWidth = 4;

    public int align;
    public Component before;        // top or left
    public Component after;         // bottom or right
    public DividerBarComponent next;  // if there are more than two objects


    final static int gap = 0;
    final static Color BARCOLOR = Color.gray;

    // if the user starts dragging the divider, these come into play
    int anchorx;            // starting x in a drag operation
    int anchory;            // starting y in a drag operation

    Cursor oldcursor;
    boolean mousedown = false;
    boolean mousein = false;

    int mousex = 0;
    int mousey = 0;



    public DividerBarComponent(int align, DividerBarComponent prev, Component before, Component after) {
        this(align, before, after);
        if (prev != null) {
            prev.next = this;
        }
    }


    public DividerBarComponent(int align, Component before, Component after) {
        if ((align != VERTICAL && align != HORIZONTAL) || before == null || after == null) {
            System.err.println("DividerBar(" + align + "," + before + "," + after);
            throw new IllegalArgumentException();
        }

        this.align = align;
        this.before = before;
        this.after = after;
        this.next = null;

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        addMouseMotionListener(new MouseTracker());
        //enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        if (align != this.align) {
            this.align = align;
            layoutContainer();
        }
    }


    /** return the component above or to the left of this component */
    public Component getComponentBefore() {
        return before;
    }

    public void setComponentBefore(Component before) {
        if (before != this.before) {
            this.before = before;
            layoutContainer();
        }
    }

    /** return the component below or to the right of this component */
    public Component getComponentAfter() {
        return after;
    }

    public void setComponentAfter(Component after) {
        if (after != this.after) {
            this.after = after;
            layoutContainer();
        }
    }

    public Dimension minimumSize() {
        return new Dimension(minWidth, minHeight);
    }

    public Dimension preferredSize() {
        Container parent = getParent();
        Dimension size = parent.getSize();
        int dx = ((align == HORIZONTAL || parent == null || size.width < minWidth) ? minWidth : size.width);
        int dy = ((align == VERTICAL || parent == null || size.height < minHeight) ? minHeight : size.height);

        return new Dimension(dx, dy);
    }

    public void paint(Graphics g) {
        Dimension size = getSize();
        g.setColor(BARCOLOR);
        g.fill3DRect(0, 0, size.width, size.height, true);
    }


    public boolean mouseEnter(MouseEvent evt, int x, int y) {

        //System.out.print("-> db mouseEnter <-");
        mousein = true;
        oldcursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(align == HORIZONTAL ? Cursor.E_RESIZE_CURSOR : Cursor.N_RESIZE_CURSOR));
        return true;
    }

    public boolean mouseExit(MouseEvent evt, int x, int y) {

        //System.out.print("-< db mouseExit >-");
        mousein = false;

        if (!mousedown) {
            setCursor(oldcursor);
        }
        return true;
    }

    public boolean mouseDown(MouseEvent evt, int x, int y) {

        //System.out.print("-V db mouseDown V-");
        mousedown = true;

        anchorx = x;
        anchory = y;

        return true;
    }

    public boolean mouseUp(MouseEvent evt, int x, int y) {

        //System.out.print("-^ db mouseUp ^-");
        mousedown = false;

        if (before == null || after == null) {
            return false;
        }
        Rectangle beforeBounds = before.getBounds(); // starting bounds of first pane
        Rectangle afterBounds = after.getBounds();   // starting bounds of second pane

        synchronized (this) {

            if (align == VERTICAL && y != anchory) {

                int dy = y - anchory;
                if (dy < 0) {
                    Dimension bmin = before.minimumSize();
                    if (beforeBounds.height + dy < bmin.height) {
                        dy = bmin.height - beforeBounds.height;
                    }
                } else if (dy > 0) {
                    Dimension amin = after.minimumSize();
                    if (afterBounds.height - dy < amin.height) {
                        dy = afterBounds.height - amin.height;
                    }
                }
                if (dy != 0) {
                    before.setBounds(beforeBounds.x, beforeBounds.y, beforeBounds.width, beforeBounds.height + dy);
                    after.setBounds(afterBounds.x, afterBounds.y + dy, afterBounds.width, afterBounds.height - dy);
                    setLocation(beforeBounds.x, beforeBounds.y + beforeBounds.height + dy);
                    getParent().validate();
                }

            } else if (align == HORIZONTAL && x != anchorx) {

                int dx = x - anchorx;
                if (dx < 0) {
                    Dimension bmin = before.minimumSize();
                    if (beforeBounds.width + dx < bmin.width) {
                        dx = bmin.width - beforeBounds.width;
                    }
                } else if (dx > 0) {
                    Dimension amin = after.minimumSize();
                    if (afterBounds.width - dx < amin.width) {
                        dx = afterBounds.width - amin.width;
                    }
                }
                if (dx != 0) {
                    before.setBounds(beforeBounds.x, beforeBounds.y, beforeBounds.width + dx, beforeBounds.height);
                    after.setBounds(afterBounds.x + dx, afterBounds.y, afterBounds.width - dx, afterBounds.height);
                    setLocation(beforeBounds.x + beforeBounds.width + dx, beforeBounds.y);
                    getParent().validate();
                }
            }
        }
        if (!mousein) {
            setCursor(oldcursor);
        }

        return true;
    }

    class MouseTracker extends MouseMotionAdapter {
        public MouseTracker() {
            super();
        }

        public void mouseDragged(MouseEvent evt) {

            if (before != null && after != null) {
                synchronized (this) {

                    Rectangle beforeBounds = before.getBounds(); // starting bounds of first pane
                    Rectangle afterBounds = after.getBounds();   // starting bounds of second pane
                    int x = evt.getX();
                    int y = evt.getY();

                    if (align == VERTICAL && y != anchory) {

                        int dy = y - anchory;

                        if (dy < 0) {
                            Dimension bmin = before.getMinimumSize();
                            if (beforeBounds.height + dy < bmin.height) {
                                dy = bmin.height - beforeBounds.height;
                            }
                        } else if (dy > 0) {
                            Dimension amin = after.getMinimumSize();
                            if (afterBounds.height - dy < amin.height) {
                                dy = afterBounds.height - amin.height;
                            }
                        }

                        if (dy != 0) {
                            before.setBounds(beforeBounds.x, beforeBounds.y, beforeBounds.width, beforeBounds.height + dy);
                            after.setBounds(afterBounds.x, afterBounds.y + dy, afterBounds.width, afterBounds.height - dy);
                            setLocation(beforeBounds.x, beforeBounds.y + beforeBounds.height + dy);
                            getParent().validate();
                        }

                    } else if (align == HORIZONTAL && x != anchorx) {

                        int dx = x - anchorx;

                        if (dx < 0) {
                            Dimension bmin = before.getMinimumSize();
                            if (beforeBounds.width + dx < bmin.width) {
                                dx = bmin.width - beforeBounds.width;
                            }
                        } else if (dx > 0) {
                            Dimension amin = after.getMinimumSize();
                            if (afterBounds.width - dx < amin.width) {
                                dx = afterBounds.width - amin.width;
                            }
                        }
                        if (dx != 0) {
                            before.setBounds(beforeBounds.x, beforeBounds.y, beforeBounds.width + dx, beforeBounds.height);
                            after.setBounds(afterBounds.x + dx, afterBounds.y, afterBounds.width - dx, afterBounds.height);
                            setLocation(beforeBounds.x + beforeBounds.width + dx, beforeBounds.y);
                            getParent().validate();
                        }
                    }
                }
            }
        }
    }


    /**
     * Paints the button and distributes an action event to all listeners.
     */
    public void processMouseEvent(MouseEvent e) {
        Graphics g;
        mousex = e.getX();
        mousey = e.getY();
        switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
		        mouseDown(e, mousex, mousey);
	            break;
            case MouseEvent.MOUSE_RELEASED:
		        mouseUp(e, mousex, mousey);
	            break;
            case MouseEvent.MOUSE_ENTERED:
		        mouseEnter(e, mousex, mousey);
	            break;
            case MouseEvent.MOUSE_EXITED:
		        mouseExit(e, mousex, mousey);
	            break;
        }
        super.processMouseEvent(e);
    }

    public void layoutContainer() {

        Rectangle b = before.bounds();
        Rectangle a = after.bounds();
	    Dimension limit = getParent().size();

        int bh;
        int bw;
	    Dimension targetsize;
        if (align == VERTICAL) {
            int totminht = minHeight;
            for (DividerBarComponent db = next; db != null; db = db.next) {
                totminht += minHeight;
            }
	        targetsize = new Dimension(limit.width - b.x, b.height + a.height);

    	    if (targetsize.height > limit.height - b.y - totminht || next == null) {
	            targetsize.height = limit.height - b.y - totminht;
            }
            bw = targetsize.width;
            bh = Math.min(b.height, targetsize.height);

            before.setBounds(b.x, b.y, bw, bh);
            after.setBounds(b.x, b.y + bh + minHeight, bw, targetsize.height - bh);
            setBounds(b.x, b.y + bh, bw, minHeight);

        } else {
            int totminwid = minWidth;
            for (DividerBarComponent db = next; db != null; db = db.next) {
                totminwid += minWidth;
            }
	        targetsize = new Dimension(b.width + a.width, limit.height - b.y);
    	    if (targetsize.width > limit.width - b.x - totminwid || next == null) {
	            targetsize.width = limit.width - b.x - totminwid;
            }
            bw = Math.min(b.width, targetsize.width);
            bh = targetsize.height;

            before.setBounds(b.x, b.y, bw, bh);
            after.setBounds(b.x + bw + minWidth, b.y, targetsize.width - bw, bh);
            setBounds(b.x + bw, b.y, minWidth, bh);
        }
        if (next != null) {
            next.layoutContainer();
        }
    }
}
