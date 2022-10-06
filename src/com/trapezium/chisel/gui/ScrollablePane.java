/*  ScrollablePane
 *
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

//import com.trapezium.chisel.*;

/** The base container for our own gui implementation.  This class includes
    commonly needed functionality such as double buffering, background texture
    and border.
 */
public class ScrollablePane extends ChiselAWTPane implements AdjustmentListener {

    // if the mode is DYNAMIC, the scroll bars disappear when not needed.
    public static final int STATIC = 0;
    public static final int DYNAMIC = 1;

    HolderPane holder;                      // owns the reference plane
    Component hScrollComponent;
    Component vScrollComponent;
    int mode = STATIC;

    private ComponentAdapter tracker;       // tracks position of children
    private Rectangle fullRect;             // the bounds of all children

    public ScrollablePane() {
        this(NO_BORDER);
    }

    public ScrollablePane(int border) {
        this(border, false);
    }

    public ScrollablePane(int border, boolean opaque) {
        super(border);

        tracker = new ComponentTracker(this);

        createScrollers(opaque);

        holder = new HolderPane();
        holder.setLayout(null);
        add(holder);

        fullRect = new Rectangle(0, 0, 0, 0);
        setScrollValues();
    }

    public void setScrollMode(int mode) {
        this.mode = mode;
    }

    private boolean hEnabled = true;
    private boolean vEnabled = true;

    public void enableScroll(boolean horiz, boolean vert) {
        hEnabled = horiz;
        hScrollComponent.setVisible(horiz);
        vEnabled = vert;
        vScrollComponent.setVisible(vert);
    }

    public void setErrorMarks( BitSet errorMarks, BitSet warningMarks, BitSet nonconformanceMarks ) {
        Slider s = (Slider)vScrollComponent;
        s.setErrorMarks( errorMarks, warningMarks, nonconformanceMarks );
    }

    public void setScrollIncrements(int hUnit, int vUnit, int hBlock, int vBlock) {
//        System.out.println(getClass().getName() + " scroll units " + hUnit + "," + vUnit + " blocks " + hBlock + "," + vBlock);

        Adjustable hScroller = (Adjustable) hScrollComponent;
        hScroller.setUnitIncrement(hUnit);
        hScroller.setBlockIncrement(hBlock);

        Adjustable vScroller = (Adjustable) vScrollComponent;
        vScroller.setUnitIncrement(vUnit);
        vScroller.setBlockIncrement(vBlock);
    }

    final public Component getHScrollComponent() {
        return hScrollComponent;
    }
    final public Component getVScrollComponent() {
        return vScrollComponent;
    }

    void createScrollers(boolean opaque) {
        hScrollComponent = new Slider(Adjustable.HORIZONTAL, 0, 100);
        ((Slider) hScrollComponent).setOpaque(opaque);
        add(hScrollComponent);

        vScrollComponent = new Slider(Adjustable.VERTICAL, 0, 100);
        ((Slider) vScrollComponent).setOpaque(opaque);
        add(vScrollComponent);
    }

    class ComponentTracker extends ComponentAdapter {

        ScrollablePane pane;

        public ComponentTracker(ScrollablePane pane) {
            super();
            this.pane = pane;
        }

        public void componentMoved(ComponentEvent evt) {
            setScrollValues(evt);
        }
        public void componentResized(ComponentEvent evt) {
            setScrollValues(evt);
        }
        public void componentHidden(ComponentEvent evt) {
            setScrollValues(evt);
        }
        public void componentShown(ComponentEvent evt) {
            setScrollValues(evt);
        }
        void setScrollValues(ComponentEvent evt) {
            pane.setScrollValues();
            /****
            Container granny = evt.getComponent().getParent().getParent();
            if (granny instanceof ScrollablePane) {
                ScrollablePane pane = (ScrollablePane) granny;
                pane.calibrateComponents();
                //System.out.println("Setting scroll values in response to " + evt.getComponent());
                pane.setScrollValues();
            } *****/
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent evt) {

        if (!isEnabled()) {
            return;
        }

        Adjustable hscroller = (Adjustable) hScrollComponent;
        int xnew = hscroller.getValue() - hscroller.getMinimum();
        Adjustable vscroller = (Adjustable) vScrollComponent;
        int ynew = vscroller.getValue() - vscroller.getMinimum();
        Point pt = holder.getLocation();

        //System.out.println("adjValChgd xnew,ynew " + xnew + "," + ynew + "  holder.getLocation " + pt);

        if (xnew != pt.x || ynew != pt.y) {
            Dimension size = getSize();
            Insets insets = getInsets();
            size.width -= insets.left + insets.right;
            size.height -= insets.top + insets.bottom;
            if (xnew + fullRect.width < size.width) {
                fullRect.width = size.width - xnew;
            }
            if (ynew + fullRect.height < size.height) {
                fullRect.height = size.height - ynew;
            }
            holder.setBounds(-xnew, -ynew, fullRect.width - fullRect.x, fullRect.height - fullRect.y);
            repaint();
        }
    }

    /** move the holder without changing the apparent position of its children */
/***
    void calibrateComponents() {
        computeFullRect();

        int x = fullRect.x;
        int y = fullRect.y;

        System.out.println( "*** calibrate: Set holder x,y to " + x + "," + y );

        Point pt = holder.getLocation();
        int dx = x - pt.x;
        int dy = (y - pt.y); ///15;

        holder.setBounds(x, y, fullRect.width, fullRect.height);

        if (dx != 0 || dy != 0) {

            int count = holder.getComponentCount();
            for (int i = 0; i < count; i++) {
                Component c = holder.getComponent(i);
                Point cpt = c.getLocation();
                c.setLocation(cpt.x - dx, cpt.y - dy);
            }

            int xval = ((Adjustable) hScrollComponent).getValue();
            ((Adjustable) hScrollComponent).setValue(xval - dx);

            int yval = ((Adjustable) vScrollComponent).getValue();
            ((Adjustable) vScrollComponent).setValue(yval - dy);

        }
        repaint();
    }
****/


    void computeFullRect() {
        Rectangle rect = getBounds();
        Insets insets = getInsets();
        rect.width -= insets.left + insets.right;
        rect.height -= insets.top + insets.bottom;
        rect.x = 0;
        rect.y = 0;

        int count = holder.getComponentCount();
        for (int i = 0; i < count; i++) {
            Component c = holder.getComponent(i);
            Rectangle bounds = c.getBounds();
            //System.out.println("computeFullRect comp " + i + " has bounds " + bounds);
            if (bounds.x < rect.x) {
                rect.width += rect.x - bounds.x;
                rect.x = bounds.x;
            }
            if (bounds.y < rect.y) {
                rect.height += rect.y - bounds.y;
                rect.y = bounds.y;
            }
            if (bounds.x + bounds.width > rect.x + rect.width) {
                rect.width = bounds.x + bounds.width - rect.x;
            }
            if (bounds.y + bounds.height > rect.y + rect.height) {
                rect.height = bounds.y + bounds.height - rect.y;
            }
        }
        fullRect = rect;
    }


    /** set the horizontal and vertical scroll values.  The default is to
        set the min and max values equal to the furthest extents of all
        child components and the visibleAmount values to the ScrollablePane's
        bounds on the screen. */

    public void setScrollValues() {

        if (!isEnabled()) {
        //    System.out.println("ScrollablePane not enabled; setScrollValues bailing out");
            return;
        }
        //System.out.print("setScrollValues: ");
        //Throwable t = new Throwable();
        //t.printStackTrace();

        Dimension size = getSize();
        Insets insets = getInsets();
        size.width -= insets.left + insets.right;
        size.height -= insets.top + insets.bottom;

        computeFullRect();

        Adjustable hScroller = (Adjustable) hScrollComponent;
        int hval = hScroller.getValue();
        hScroller.setVisibleAmount(size.width);
        hScroller.setMinimum(fullRect.x);
        int hmax = fullRect.x + fullRect.width;
        hScroller.setMaximum(hmax);
        if (hval < fullRect.x) {
            hval = fullRect.x;
        } else if (hval >= hmax) {
            hval = hmax;
        }
        hScroller.setValue(hval);

        Adjustable vScroller = (Adjustable) vScrollComponent;
        int vval = vScroller.getValue();
        vScroller.setVisibleAmount(size.height);
        vScroller.setMinimum(fullRect.y);
        int vmax = fullRect.y + fullRect.height;
        vScroller.setMaximum(vmax);
        if (vval < fullRect.y) {
            vval = fullRect.y;
        } else if (vval >= vmax) {
            vval = vmax;
        }
        vScroller.setValue(vval);

        int x = fullRect.x - hval;
        int y = fullRect.y - vval;
        int width = fullRect.width - fullRect.x;
        int height = fullRect.height - fullRect.y;
        holder.setBounds( -hval, -vval, width, height );
        if (mode == DYNAMIC) {
            if (hval <= 0 && width <= size.width) {
                hScrollComponent.setVisible(false);
            } else if (hEnabled && !hScrollComponent.isVisible()) {
                hScrollComponent.setVisible(true);
            }
            if (vval <= 0 && height <= size.height) {
                vScrollComponent.setVisible(false);
            } else if (vEnabled && !vScrollComponent.isVisible()) {
                vScrollComponent.setVisible(true);
            }
        }
    }

    /** this lets an outside component own the scrollers */
    public void hookScrollersTo(Container c) {
        remove(hScrollComponent);
        c.add(hScrollComponent, 0);
        remove(vScrollComponent);
        c.add(vScrollComponent, 1);
    }


    /** the scrollers should always be in front */
    public void moveToFront(Component c) {
        if (c == hScrollComponent || c == vScrollComponent || c == holder) {
            return;
        } else if (c != null && c != holder.getComponent(0)) {
            holder.remove(c);
            holder.add(c, 0);
            holder.repaint();
        }
    }

    /**  move to back */
    public void moveToBack(Component c) {
        if (c == hScrollComponent || c == vScrollComponent || c == holder) {
            return;
        } else if (c != null) {
            holder.remove(c);
            holder.add(c);
            holder.repaint();
        }
    }

    protected void addImpl(Component c, Object constraints, int index) {
        if (c == hScrollComponent) {
            ((Adjustable)c).addAdjustmentListener(this);
            super.addImpl(c, constraints, index);
        } else if (c == vScrollComponent) {
            ((Adjustable)c).addAdjustmentListener(this);
            super.addImpl(c, constraints, index);
        } else if (c == holder) {
            super.addImpl(c, constraints, index);
        } else {
            c.addComponentListener(tracker);
            holder.add(c, constraints, index);
            computeFullRect();
        }
    }

    public void remove(Component comp) {
        if (comp != hScrollComponent && comp != vScrollComponent && comp != holder) {
            holder.remove(comp);
        } //else {
            super.remove(comp);
        //}
    }

    public int getComponentCount() {
        return holder.getComponentCount();
    }

    public Component getComponent(int n) {
        Component result = null;
        try {
        result = holder.getComponent(n);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.err.println(this.getClass().getName()+ ":getComponent() failed for n=" + n);
        }
        return result;
    }

    public void addContainerListener(ContainerListener listener) {
        holder.addContainerListener(listener);
    }

    /** determine the minimum size */
    public Dimension getMinimumSize() {
        Dimension min = hScrollComponent.getMinimumSize();
        Dimension minv = vScrollComponent.getMinimumSize();
        min.width += minv.width;
        min.height += minv.height;
        return min;
    }


    /** determine the preferred size */
    public Dimension getPreferredSize() {
        computeFullRect();
        Insets insets = getInsets();
        Dimension dim = new Dimension(fullRect.width + insets.left + insets.right, fullRect.height + insets.top + insets.bottom);
        return dim;
    }


    /** lay out the pane.  Only the scrollers are positioned; other
        components remain where they are. */
    public void doLayout() {

        Dimension size = getSize();
        Dimension minh = hScrollComponent.getMinimumSize();
        Dimension minv = vScrollComponent.getMinimumSize();
        Container owner = hScrollComponent.getParent();
        if (owner == this) {
            Insets insets = getInsets();
            int top = insets.top;
            int bottom = size.height - insets.bottom;
            int left = insets.left;
            int right = size.width - insets.right;

            hScrollComponent.setBounds(left, bottom - minh.height, right - left - minv.width, minh.height);
            vScrollComponent.setBounds(right - minv.width, top, minv.width, bottom - top - minh.height);
        } else {
            Point pt;
            // getLocationOnScreen() bombs when the frame hasn't been shown yet
            try {
                pt = getLocationOnScreen();
                Point ownerscreenpt = owner.getLocationOnScreen();
                pt.x -= ownerscreenpt.x;
                pt.y -= ownerscreenpt.y;
            } catch (Exception e) {
                pt = getLocation();
            }
            
            hScrollComponent.setBounds(pt.x, pt.y + size.height, size.width, minh.height);
            vScrollComponent.setBounds(pt.x + size.width, pt.y, minv.width, size.height);
        }

        setScrollValues();

      /****
        computeFullRect();
        Adjustable hscroller = (Adjustable) hScrollComponent;
        Adjustable vscroller = (Adjustable) vScrollComponent;

        int dx = 0;
        int dy = 0;
        if (hScrollComponent.isVisible() && ((Slider)hScrollComponent).isOpaque()) {
            dy = minh.height;
        }
        if (vScrollComponent.isVisible() && ((Slider)vScrollComponent).isOpaque()) {
            dx = minv.width;
        }
        holder.setBounds(-hscroller.getValue(), -vscroller.getValue(), fullRect.width, fullRect.height);
        ****/
    }

    /** overrides paint in order to paint the holder separately, otherwise
        it might be excessively clipped */
    public void paint(Graphics g) {

        Dimension size = getSize();
        Insets insets = getInsets();

        paintBackground(g);

        // for the holder, translate the graphics and expand the clip rect to compensate
        Rectangle hrect = holder.getBounds();
        Rectangle clip = g.getClipBounds();
        g.translate(hrect.x, hrect.y);
        holder.paint(g);
        g.translate(-hrect.x, -hrect.y);

        Graphics gcomp;
        Rectangle r;
		if (hScrollComponent.isVisible()) {
		    r = hScrollComponent.getBounds();
			gcomp = g.create(r.x, r.y, r.width, r.height);
			try {
			    hScrollComponent.paint(gcomp);
			} finally {
			    gcomp.dispose();
			}
		}
		if (vScrollComponent.isVisible()) {
		    r = vScrollComponent.getBounds();
			gcomp = g.create(r.x, r.y, r.width, r.height);
			try {
			    vScrollComponent.paint(gcomp);
			} finally {
			    gcomp.dispose();
			}
		}
    }
}

