/*  ProcessedFileViewer
 *
 */

package com.trapezium.chisel;

import java.awt.Component;
import java.io.File;


/** interface for ProcessedFile viewers of any persuasion (Swing or AWT) */
public interface ProcessedFileViewer {

    /** update the display if the object belongs to this viewer or is null */
    void fileUpdated(ProcessedFile data);

    /** the file has been read */
    void fileDone(ProcessedFile data);

    /** save the current document.  Prompts for filename if the document was not loaded
        from disk or previously saved. */
    void save();

    /** remove the current document from the viewer and display an empty document */
    void empty();

    /** get the ProcessedFile being viewed */
    ProcessedFile getProcessedFile();

    /** set the ProcessedFile being viewed.  This should be done automatically
        by the done method; setProcessedFile should only be called when an object
        needs its ProcessedFile object before the done method is executed. */
    void setProcessedFile(ProcessedFile data);

    /** get the component embodying this viewer */
    Component getComponent();

    /** dump the current document to the console */
    void dump();
}
