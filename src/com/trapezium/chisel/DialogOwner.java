/*
 * @(#)DialogOwner.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */

package com.trapezium.chisel;
import java.util.ResourceBundle;
import java.awt.Frame;

public interface DialogOwner {
		Frame getFrame();
		ResourceBundle getResourceBundle();
}


