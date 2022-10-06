/*
 * WorkspaceListener.java
 */

package com.trapezium.chisel.gui;

import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

/** WorkspaceListener adds window maximization to WindowListener
 */
public interface WorkspaceListener extends WindowListener {
    void windowMaximized(WindowEvent evt);
    void windowDemaximized(WindowEvent evt);
}
