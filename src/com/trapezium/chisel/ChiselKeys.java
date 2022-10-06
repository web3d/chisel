/*
 * @(#)ChiselKeys.java
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
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class ChiselKeys {
	private static Hashtable keys = new Hashtable();
	static {
		setKeyAction("anchor-drop", Event.ALT_MASK, KeyEvent.VK_A);
		setKeyAction("anchor-goto-last", Event.ALT_MASK, KeyEvent.VK_J);
		setKeyAction("brace-match-backward", Event.ALT_MASK, 219); // '['
		setKeyAction("brace-match-forward", Event.ALT_MASK, 221); // ']'
		setKeyAction("buffer-paste", Event.CTRL_MASK, KeyEvent.VK_V);
		setKeyAction("character-delete-backward", 0, KeyEvent.VK_BACK_SPACE);
		setKeyAction("character-delete-forward", 0, KeyEvent.VK_DELETE);
		setKeyAction("cursor-backward", 0, KeyEvent.VK_LEFT);
		setKeyAction("cursor-document-begin", KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_UP);
		setKeyAction("cursor-document-end", KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_DOWN);
		setKeyAction("cursor-down", 0, KeyEvent.VK_DOWN);
		setKeyAction("cursor-forward", 0, KeyEvent.VK_RIGHT);
		setKeyAction("cursor-line-begin", 0, KeyEvent.VK_HOME);
		setKeyAction("cursor-line-end", 0, KeyEvent.VK_END);
		setKeyAction("cursor-select-backward", Event.SHIFT_MASK, KeyEvent.VK_LEFT);
		setKeyAction("cursor-select-document-begin", Event.SHIFT_MASK | KeyEvent.CTRL_MASK, KeyEvent.VK_HOME);
		setKeyAction("cursor-select-document-end", Event.SHIFT_MASK | KeyEvent.CTRL_MASK, KeyEvent.VK_END);
		setKeyAction("cursor-select-down", Event.SHIFT_MASK | Event.SHIFT_MASK, KeyEvent.VK_DOWN);
		setKeyAction("cursor-select-forward", Event.SHIFT_MASK, KeyEvent.VK_RIGHT);
		setKeyAction("cursor-select-line-begin", Event.SHIFT_MASK, KeyEvent.VK_HOME);
		setKeyAction("cursor-select-line-end", Event.SHIFT_MASK, KeyEvent.VK_END);
		setKeyAction("cursor-select-up", Event.SHIFT_MASK, KeyEvent.VK_UP);
		setKeyAction("cursor-select-word-backward", Event.SHIFT_MASK | Event.CTRL_MASK, KeyEvent.VK_LEFT);
		setKeyAction("cursor-select-word-forward", Event.SHIFT_MASK | Event.CTRL_MASK, KeyEvent.VK_RIGHT);
		setKeyAction("cursor-page-begin", KeyEvent.CTRL_MASK, KeyEvent.VK_HOME);
		setKeyAction("cursor-page-end", KeyEvent.CTRL_MASK, KeyEvent.VK_END);
		setKeyAction("cursor-up", 0, KeyEvent.VK_UP);
		setKeyAction("cursor-word-backward", Event.CTRL_MASK, KeyEvent.VK_LEFT);
		setKeyAction("cursor-word-forward", Event.CTRL_MASK, KeyEvent.VK_RIGHT);
		setKeyAction("find-dialog", Event.CTRL_MASK, KeyEvent.VK_F);
		setKeyAction("find-next-backward", Event.ALT_MASK, KeyEvent.VK_UP);
		setKeyAction("find-next-forward", Event.ALT_MASK, KeyEvent.VK_DOWN);
		setKeyAction("frame-close", Event.CTRL_MASK, KeyEvent.VK_W);
		setKeyAction("goto-dialog", Event.CTRL_MASK, KeyEvent.VK_G);
		setKeyAction("line-break", 0, KeyEvent.VK_ENTER);
		setKeyAction("line-clone", Event.ALT_MASK, KeyEvent.VK_O);
		setKeyAction("line-delete", Event.ALT_MASK, KeyEvent.VK_D);
		setKeyAction("line-swap", Event.ALT_MASK, KeyEvent.VK_S);
		setKeyAction("mode-autoindent-switch", Event.ALT_MASK, KeyEvent.VK_F3);
		setKeyAction("page-down", 0, KeyEvent.VK_PAGE_DOWN);
		setKeyAction("page-up", 0, KeyEvent.VK_PAGE_UP);
		setKeyAction("redo", Event.CTRL_MASK, KeyEvent.VK_Y);
		setKeyAction("replace-dialog", Event.CTRL_MASK, KeyEvent.VK_H);
		setKeyAction("selection-copy", Event.CTRL_MASK, KeyEvent.VK_C);
		setKeyAction("selection-cut", Event.CTRL_MASK, KeyEvent.VK_X);
		setKeyAction("selection-indent", Event.CTRL_MASK, KeyEvent.VK_TAB );
		setKeyAction("selection-unindent", Event.SHIFT_MASK, KeyEvent.VK_TAB );
		setKeyAction("undo", Event.CTRL_MASK, KeyEvent.VK_Z);
		setKeyAction("undo", Event.ALT_MASK, KeyEvent.VK_BACK_SPACE);
	}

	protected ChiselKeys() {
	}

	public static void setKeyAction(String action, int modifiers, int key) {
		keys.put(new KeyObject(modifiers, key), action);
	}

	public static String getKeyAction(int modifiers, int key) {
		return (String)keys.get(new KeyObject(modifiers, key));
	}
}

/* This is doubly a "key object".  It holds a keystroke and serves as
 * a hashtable key for the keystroke to action-string mapping
 */
class KeyObject {
	public int key;

	public KeyObject(int m, int k) {
		key = (k & 0xFFFF) | (m & 0x7FFF) << 16;
	}

	public int getMod() {
		return (key & 0x7FFF0000) >> 16;
	}

	public int getCode() {
		return (key & 0x0000FFFF);
	}

	public int hashCode() {
		return key;
	}

    public boolean equals(Object obj) {
		return obj instanceof KeyObject && ((KeyObject)obj).key == this.key;
    }
}
