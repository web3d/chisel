/*
 * @(#)ChiselPluginDescriptor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel;

/** This is used for the external serialization of a ChiselSet
 *  description, used by plugins.
 */
public interface ChiselPluginDescriptor {

    public static final String extension = ".cpd";

    String getCommand();
    String getTip();
    boolean getGreenable();
    String getKey();
    boolean getInitialCollapsedValue();
    int getNumberChisels();
    String getClassName( int offset );
    String getPrompt( int offset );
    boolean getCheckVal( int offset );
    int getListener( int offset );
}
