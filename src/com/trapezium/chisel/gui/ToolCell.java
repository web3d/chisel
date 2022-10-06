/* ToolCell
 *
 *  This is a component for use as a kind of tool bar embedded in a cell in
 *  a table.
 */
package com.trapezium.chisel.gui;

import com.trapezium.chisel.GuiAdapter;
import com.trapezium.chisel.ChiselResources;
import com.trapezium.chisel.ActionConnector;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class ToolCell extends ChiselAWTPane {
    final static int PADDING = 4;
    ActionListener listener;

    public ToolCell(ActionListener listener) {
        super();
        this.listener = listener;
        setBorder(NO_BORDER, PADDING);
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        setFont(font);
        GuiAdapter ga = new GuiAdapter();
        ga.setContainer(this);
        //ga.setStretchLast(true);

        add(createButton("open"));
        add(createButton("fetch"));
        add(createButton("save"));
        add(createButton("saveAs"));
        add(createButton("cut"));
        add(createButton("copy"));
        add(createButton("paste"));

        //add(createDirControl());
    }

    /**
     * Create a button suitable for a toolcell.
     */
    public static Component createButton(String key) {

        Image image = ChiselResources.getImage(key);
        if (image == null) {
            System.err.println("Image not available for " + key);
        }
        String astr = ChiselResources.getActionCommand(key);
        if (astr == null) {
            astr = key;
        }
        ActionListener a = ActionConnector.getAction(astr);

        LabelledImageButton button = new LabelledImageButton(null, image);
        if (a != null) {
            button.setActionCommand(astr);
            button.addActionListener(a);
        } else {
            button.setEnabled(false);
        }
        return button;
    }
    /**
     * Create a directory control.
     */
    public static Component createDirControl() {
        return new DirectoryControl( System.getProperty("user.dir", "") );
    }
}

class DirectoryControl extends TextLabel implements DisplayConstants {

    File dir;

    public DirectoryControl(String dirname) {
        super(dirname, FontPool.getFont(0), TEXT_ALIGN_LEFT | TEXT_ALIGN_VCENTER );
        dir = new File(dirname);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + dirname);
        }
    }

    public void paint(Graphics g) {
        Dimension size = getSize();
        g.setColor(getBackground());
        g.fill3DRect(0, 0, size.width, size.height, false);

        super.paint(g);
    }
}
