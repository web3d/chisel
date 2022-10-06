/*  GuiAdapter
 *
 */

package com.trapezium.chisel;

import java.awt.*;
import java.util.*;

import com.trapezium.chisel.gui.DisplayConstants;

/** GuiAdapters separate the specific gui implementation from the gui logic.
    This is how we accomplish our own variation on pluggable look & feel,
    which is between JFC and AWT with our own custom l&f.

    An adapter class is necessary because the implementation classes are
    different for JFC vs. AWT.
  */
public class GuiAdapter implements LayoutManager, DisplayConstants {

    private Component component;

    /** if stretchLast is true, the final component is resized to occupy any
        remaining space */
    boolean stretchLast = false;


    public GuiAdapter() {
        component = null;
    }

    public Component getComponent() {
        return component;
    }

    public Container getContainer() {
        if (component != null && component instanceof Container) {
            return (Container) component;
        } else {
            return null;
        }
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public void setContainer(Container container) {
        this.component = container;
        if (container != null) {
            container.setLayout(this);
        }
    }

    public void setStretchLast(boolean stretchLast) {
        this.stretchLast = stretchLast;
    }

    void show() {
        if (component != null) {
            component.setVisible(true);
        }
    }

    /** ignored by default */
    public void addLayoutComponent(String name, Component comp) {
    }

    /** ignored by default */
    public void removeLayoutComponent(Component comp) {
    }

    /** determine the minimum size */
    public Dimension minimumLayoutSize(Container target) {
        Insets insets = target.getInsets();
        Dimension dim = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
        int numkids = target.getComponentCount();
        for (int i = 0; i < numkids; i++) {
            Component kid = target.getComponent(i);
            Dimension min = kid.getMinimumSize();
            dim.width += min.width + HGAP;
            int minht = min.height + insets.top + insets.bottom;
            if (dim.height < minht) {
                dim.height = minht;
            }
        }
        return dim;
    }

    /** determine the preferred size */
    public Dimension preferredLayoutSize(Container target) {
        Insets insets = target.getInsets();
        Dimension dim = new Dimension(insets.left + insets.right, insets.top + insets.bottom);
        int numkids = target.getComponentCount();
        for (int i = 0; i < numkids; i++) {
            Component kid = target.getComponent(i);
            Dimension pref = kid.getPreferredSize();
            dim.width += pref.width + HGAP;
            int prefht = pref.height + insets.top + insets.bottom;
            if (dim.height < prefht) {
                dim.height = prefht;
            }
        }
        return dim;
    }

    /** determine the maximum size.  By default this is infinite. */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /** lay out the component.  The default is similar to FlowLayout, but
        always top left aligned and with no wrap around. */
    public void layoutContainer(Container target) {
        Insets insets = target.getInsets();
        Dimension size = target.getSize();
        int top = insets.top;
        int bottom = size.height - insets.bottom;
        int left = insets.left;
        int right = size.width - insets.right;

        int x = left;
        int y = top;
        int numkids = target.getComponentCount();
        for (int i = 0; i < numkids; i++) {
            Component kid = target.getComponent(i);
            if ( kid == target ) {
                System.out.println( "Hey, what gives!" );
                continue;
            }
    	    if (kid.isVisible()) {
        		Dimension d = kid.getPreferredSize();
        		if (stretchLast && i == numkids - 1 && x < right) {
        		    d.width = right - x;
        		}
		        kid.setSize(d.width, d.height);
                kid.setLocation(x, y);
                x += kid.getSize().width + HGAP;
            }
        }
    }
}


