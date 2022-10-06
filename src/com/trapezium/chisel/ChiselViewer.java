/*  ChiselViewer
 *
 */

package com.trapezium.chisel;

import java.awt.Component;

//import com.trapezium.factory.*;
import com.trapezium.chisel.gui.ComponentFactory;

/** viewer envelope for implementation of any persuasion (Swing or AWT) */
public class ChiselViewer extends ChiselPane {

    ProcessedFileViewer viewer;

    public ChiselViewer() {
        super();
        viewer = ComponentFactory.createViewer();
        setComponent((Component) viewer);
    }

    /** update the display if the object belongs to this viewer or is null */
    public void fileUpdated(ProcessedFile data) {
        viewer.fileUpdated(data);
    }

    /** load a new file into the viewer */
    public void fileDone(ProcessedFile data) {
        viewer.fileDone(data);
    }

    /** save the current document.  Prompts for filename if the document was not loaded
        from disk or previously saved. */
    public void save() {
        viewer.save();
    }

    /** remove the current document from the viewer and display an empty document */
    public void empty() {
        viewer.empty();
    }

    /** get the ProcessedFile being viewed */
    public ProcessedFile getProcessedFile() {
        return viewer.getProcessedFile();
    }

    /** set the ProcessedFile being viewed */
    public void setProcessedFile(ProcessedFile data) {
        viewer.setProcessedFile(data);
    }

    /** get the component embodying this viewer */
    public Component getComponent() {
        return viewer.getComponent();
    }

    /** dump the current document to the console */
    public void dump() {
        System.out.println("ChiselViewer holding real viewer:");
        viewer.dump();
    }
}
