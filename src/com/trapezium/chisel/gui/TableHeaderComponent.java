/*  TableHeaderComponent
 *
 *  v.06
 *
 *  Actual object visualizing the ChiselFileTable header, a special version of
 *  TableRowComponent that looks more header-like.
 */

package com.trapezium.chisel.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

//import com.trapezium.chisel.*;

public class TableHeaderComponent extends TableRowComponent {
    public TableHeaderComponent() {
        this(DEFAULT_COLUMNS, DEFAULT_INSETS);
    }
    public TableHeaderComponent(int numcolumns) {
        this(numcolumns, DEFAULT_INSETS);
    }

    public TableHeaderComponent(int numcolumns, Insets cellInsets) {
        super(numcolumns, cellInsets);
        setBorder(LOWERED_BORDER);
    }
    public Color getBackground() {
        Color bg = super.getBackground().darker().darker();
        while (bg.getRed() + bg.getGreen() + bg.getBlue() >  255) {
            bg = bg.darker();
        }
        return bg;
    }
    public Color getForeground() {
        return Color.white;
    }
    public boolean isCollapsed() {
        return (expandButton != null && expandButton.getBooleanValue() == false);
    }
}

