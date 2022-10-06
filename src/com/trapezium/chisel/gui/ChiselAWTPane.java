/*  ChiselAWTPane
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import com.trapezium.chisel.Workspace;

/** The base container for our own gui implementation.  This class includes
    commonly needed functionality such as background texture and border.
 */
public class ChiselAWTPane extends Container implements DisplayConstants, Workspace {

    private int border;
    private Insets borderInsets;
    private boolean opaque = true;

    /** selected component */
    private Component selection;

    public ChiselAWTPane() {
        this(NO_BORDER);
    }
    public ChiselAWTPane(int border) {
        super();
        borderInsets = new Insets(0,0,0,0);
        setBorder(border);
        selection = null;
    }

    /** get the selected subcomponent of this component */
    public Component getSelection() {
        return selection;
    }

    public void remove(Component c) {
        boolean setsel = false;
        if (selection == c) {
            selection = null;
            setsel = true;
        }
//        System.out.println( "Before super.remove, count " + getComponentCount() + ", comp 0 is " + getComponent(0) );
//        if ( getComponentCount() > 0 ) {
//            Component[] ccc = getComponents();
//            Component c0 = getComponent(0);
//            System.out.println( "ok, here1" );
//        }
        
        super.remove(c);
//        if ( getComponentCount() > 0 ) {
//            Component[] ccc = getComponents();
//            Component c0 = getComponent(0);
//            System.out.println( "ok, here2" );
//        }

//        System.out.println( "After super.remove, count " + getComponentCount() + ", comp 0 is " + getComponent(0) );
        if (setsel) {
            if (getComponentCount() > 0) {
                selection = getComponent(0);
            }
        }
    }

    protected void addImpl(Component c, Object constraints, int index) {
        super.addImpl(c, constraints, index);
        if (index == 0) {
            selection = c;
        }
    }

    public void paste() {
        if ( selection instanceof Workspace ) {
            ((Workspace)selection).paste();
        }
    }

    public void cut() {
        if ( selection instanceof Workspace ) {
            ((ChiselAWTViewer)selection).cut();
        }
    }

    public void copy() {
        if ( selection instanceof Workspace ) {
            ((ChiselAWTViewer)selection).copy();
        }
    }

    public void undo() {
        if ( selection instanceof Workspace ) {
            ((ChiselAWTViewer)selection).undo();
        }
    }

    public void redo() {
        if ( selection instanceof Workspace ) {
            ((ChiselAWTViewer)selection).redo();
        }
    }

    public void nextError() {
        if ( selection instanceof Workspace ) {
            ((ChiselAWTViewer)selection).nextError();
        }
    }

    public void prevError() {
        if ( selection instanceof Workspace ) {
            ((ChiselAWTViewer)selection).prevError();
        }
    }

    public void setSelection(Component c) {
        if (c != null) {
            moveToFront(c);
            selection = c;
            /***
            if (selection != c) {
                if ( selection != null && selection instanceof ChiselAWTViewer ) {
                    ((ChiselAWTViewer)selection).setActivated(false);
                }
                selection = c;
                if ( selection instanceof ChiselAWTViewer ) {
                    ((ChiselAWTViewer)selection).setActivated(true);
                }
            }***/
        }
    }

    public void close(Component c) {
        c.setVisible(false);
        remove(c);
        /****
        if (selection == c) {
            selection = getComponentCount() > 0 ? getComponent(0) : null;
            if ( selection != null && selection instanceof ChiselAWTViewer ) {
                ((ChiselAWTViewer)selection).setActivated(true);
            }
        } ****/
    }

    public void moveToFront(Component c) {
        if (c != getComponent(0)) {
            remove(c);
            add(c, 0);
            repaint();
        }
    }

    public void moveToBack(Component c) {
        remove(c);
        add(c);
        repaint();
    }


    public void paint(Graphics g) {

        Dimension size = size();

        paintBackground(g);
	    g.clipRect(borderInsets.left, borderInsets.top, size.width - borderInsets.right - borderInsets.left, size.height - borderInsets.bottom - borderInsets.top);
        super.paint(g);
    }

    public void paintBackground(Graphics g) {
        Dimension size = getSize();
        if (border == NO_BORDER) {
            g.setColor(getBackground());
            if (opaque) {
                g.fillRect(0, 0, size.width, size.height);
            }
        } else if (border == THIN_BORDER) {
            g.setColor(Color.black);
            g.drawRect(0, 0, size.width - 1, size.height - 1);
            g.setColor(getBackground());
            if (opaque) {
                g.fillRect(1, 1, size.width - 2, size.height - 2);
            }

        } else if (border == THICK_BORDER) {
            g.setColor(Color.darkGray);
            g.drawRect(0, 0, size.width - 1, size.height - 1);
            g.drawRect(2, 2, size.width - 5, size.height - 5);
            g.setColor(Color.black);
            g.drawRect(1, 1, size.width - 3, size.height - 3);
            g.setColor(getBackground());
            if (opaque) {
                g.fillRect(3, 3, size.width - 6, size.height - 6);
            }

        } else {
            g.setColor(getBackground());
            /***if (opaque) {
                g.fill3DRect(0, 0, size.width, size.height, (border != LOWERED_BORDER));
            } else {
                g.draw3DRect(0, 0, size.width - 1, size.height - 1, (border != LOWERED_BORDER));
            }***/
            g.draw3DRect(0, 0, size.width - 1, size.height - 1, (border != LOWERED_BORDER));
            if (opaque) {
                g.setColor(getBackground());
                g.fillRect(1, 1, size.width - 2, size.height - 2);
            }
            if (border == FRAMED_BORDER) {
                g.setColor(getBackground());
                g.draw3DRect(2, 2, size.width - 5, size.height - 5, false);
            }
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
    }

    public void setBorder(int border) {
        setBorder(border, 0);
    }

    public void setBorder(int border, int padding) {
        this.border = border;
        switch (border) {
            case THIN_BORDER:
            case RAISED_BORDER:
            case LOWERED_BORDER:
                borderInsets.left = 1;
                borderInsets.top = 1;
                borderInsets.right = 1;
                borderInsets.bottom = 1;
                break;

            case FRAMED_BORDER:
            case THICK_BORDER:
                borderInsets.left = 3;
                borderInsets.top = 3;
                borderInsets.right = 3;
                borderInsets.bottom = 3;
                break;

            case NO_BORDER:
            default:
                borderInsets.left = 0;
                borderInsets.top = 0;
                borderInsets.right = 0;
                borderInsets.bottom = 0;
                break;
        }
        borderInsets.left += padding;
        borderInsets.top += padding;
        borderInsets.right += padding;
        borderInsets.bottom += padding;
    }

    public int getBorder() {
        return border;
    }

    /** override getInsets to return the border insets */
    public Insets getInsets() {
        //Throwable t = new Throwable();
        //System.out.print("getInsets called as follows: ");
        //t.printStackTrace();
        return borderInsets;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public boolean isOpaque() {
        return opaque;
    }
}


