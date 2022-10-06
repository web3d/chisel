/*  Workspace
 *
 */

package com.trapezium.chisel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;


/** The interface for containers which can manage their contents */
public interface Workspace {

    /** get the selected component */
    Component getSelection();

    /** select a component */
    void setSelection(Component c);

    /** hide and remove a component */
    void close(Component c);

    /** make a component the front most one */
    void moveToFront(Component c);

    /** make a component the farthest back one */
    void moveToBack(Component c);
    
    /** paste from the system clipboard into the currently selected component */
    void paste();
    void copy();
    void cut();
    void undo();
    void redo();
    void nextError();
    void prevError();
}


