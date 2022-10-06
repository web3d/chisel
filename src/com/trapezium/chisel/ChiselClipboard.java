/*
 * @(#)ChiselClipboard.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.*;

// TextMenu is a fairly large class which creates and first handles
// events from all menus.  This may all change when I add toobars.

public class ChiselClipboard implements ClipboardOwner {

	private static Clipboard globalClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	private static ClipboardOwner globalOwner = new ChiselClipboard();
	
	protected ChiselClipboard() {
	}

	public static Clipboard getClipboard() {
		return globalClipboard;
	}
	
	public static ClipboardOwner getOwner() {
		return globalOwner;
	}
	
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
	
}
