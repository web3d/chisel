/*
 * @(#)ActionNames.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

/** Predefined Action names.
 *
 *  @author          Michael St. Hippolyte
 *  @version         1.0, 27 Feb 1998
 *
 *  @since           1.0
 */
public interface ActionNames {

    // NOTE:  these actions have to be identical to the ones in ChiselResources.java
    // maybe one should ref the other
    public static final String openAction = "open";
    public static final String newAction  = "new";
    public static final String saveAction = "save";
    public static final String saveAsAction = "saveas";
    public static final String gzipSaveAction = "gzipsave";
    public static final String gzipSaveAsAction = "gzipsaveas";
    public static final String exitAction = "exit";
    public static final String pasteAction = "paste-from-clipboard";
    public static final String cutAction = "cut-to-clipboard";
    public static final String copyAction = "copy-to-clipboard";
    public static final String undoAction = "undo-last-command";
    public static final String redoAction = "redo-last-command";
}

