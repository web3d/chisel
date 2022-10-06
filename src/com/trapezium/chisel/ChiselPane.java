/*  ChiselPane
 *
 */

package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

//import com.trapezium.factory.*;
import com.trapezium.chisel.gui.ComponentFactory;
import com.trapezium.chisel.gui.FontPool;


/** A top-level non-generic gui component of Chisel */
public class ChiselPane extends GuiAdapter implements ProcessedFileViewer {

    /** create a pane with a generic gui implementation */
    public ChiselPane() {
        this(null, false);
    }

    /** create a generic scrollable pane */
    public ChiselPane(boolean scrollable) {
        this(null, scrollable);
    }

    /** create a pane with a specific gui implementation */
    public ChiselPane(Component pane) {
        this(pane, false);
    }

    /** create a scrollable pane with a specific gui implementation */
    private ChiselPane(Component pane, boolean scrollable) {
        if (pane == null) {
            Container container;
            if (scrollable) {
                container = ComponentFactory.createScrollablePane();
                container.setLayout(null);
                setComponent(container);
            } else {
                container = ComponentFactory.createSimplePane();
                setContainer(container);
            }
            pane = container;
        } else {
            setComponent(pane);
        }
        pane.setBackground(DEFAULT_PANECOLOR);
        pane.setFont(FontPool.getFont(0));
    }

    /** update the display if the object belongs to this viewer or is null */
    public void fileUpdated(ProcessedFile data) {
    }

    /** load an object into the viewer */
    public void fileDone(ProcessedFile data) {
    }

    /** save the current document.  Prompts for filename if the document was not loaded
        from disk or previously saved. */
    public void save() {
    }

    /** remove the current document from the viewer and display an empty document */
    public void empty() {
    }

    /** get the ProcessedFile being viewed */
    public ProcessedFile getProcessedFile() {
        return null;
    }

    /** set the ProcessedFile being viewed */
    public void setProcessedFile(ProcessedFile data) {
    }

    /** dump the current document to the console */
    public void dump() {
    }
}

