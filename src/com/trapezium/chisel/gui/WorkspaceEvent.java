/*
 * WorkspaceEvent.java
 */

package com.trapezium.chisel.gui;

import java.awt.event.*;
import java.awt.Event;
import java.awt.Window;
import java.awt.Component;
import java.awt.Container;

/** WorkspaceEvent extends WindowEvent because that's what it really should be,
    except that WindowEvents only work for Windows which are heavyweight components
    we'd rather not have to use.  WorkspaceEvent works for any component.
*/
public class WorkspaceEvent extends WindowEvent {

    public static final int WINDOW_MAXIMIZED = WINDOW_LAST + 1;
    public static final int WINDOW_DEMAXIMIZED = WINDOW_LAST + 2;

    static Window dummyWindow = null;
    static Window getWindow(Component comp) {

        if (dummyWindow == null) {
            Container parent = comp.getParent();
            while (parent != null && !(parent instanceof Window)) {
                parent = parent.getParent();
            }
            dummyWindow = (Window) parent;
        }
        return dummyWindow;
    }

    public WorkspaceEvent(Component source, int id) {
        // construct with a dummy window source
        super(getWindow(source), id);
        // now set the source
        this.source = source;
    }
}
