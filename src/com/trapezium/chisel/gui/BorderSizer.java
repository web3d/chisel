/*  BorderSizer.java
 *
 */

package com.trapezium.chisel.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Dimension;

public class BorderSizer extends Sizer {
    public BorderSizer(Container container) {
        super(container);
    }
    public BorderSizer(Container container, boolean trackParent) {
        super(container, true);
    }
    protected int getSizeType(int x, int y) {
        Container container = (Container) getTarget();
        Insets insets = container.getInsets();
        Dimension size = container.getSize();
        if (x > insets.left && x < size.width - insets.right && y > insets.top && y < size.height - insets.bottom) {
            return NONE;
        } else {
            return super.getSizeType(x, y);
        }
    }
}

