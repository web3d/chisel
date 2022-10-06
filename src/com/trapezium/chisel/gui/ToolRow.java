/* ToolRow
 *
 *  This is a component for use as a kind of tool bar embedded in a row in
 *  a table.
 */
package com.trapezium.chisel.gui;

import com.trapezium.chisel.GuiAdapter;
import com.trapezium.chisel.ChiselResources;
import com.trapezium.chisel.ActionConnector;

import java.awt.*;
import java.awt.event.ActionListener;

public class ToolRow extends TableRowComponent implements DisplayConstants {
    public ToolRow(ActionListener listener) {
        super(1, new Insets(1, 1, 1, 1));
        setColumn(0, new ToolCell(listener));
    }

    public Color getBackground() {
        return DEFAULT_TOOLBARCOLOR;
    }
}
