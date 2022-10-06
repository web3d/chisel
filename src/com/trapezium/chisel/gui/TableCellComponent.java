/* TableCellComponent
 *
 *  This is the abstract base class for the object contained in each cell, and
 *  is controlled by each TableRowComponent.
 */
package com.trapezium.chisel.gui;

import java.awt.*;

public class TableCellComponent extends Container {

    public Insets getInsets() {
        Container parent = getParent();
        if (parent == null) {
            //System.out.println("getInsets called before cell has a parent");
            return new Insets(0, 0, 0, 0);
        }
        return getParent().getInsets();
    }

}
