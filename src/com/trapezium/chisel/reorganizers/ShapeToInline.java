/*
 * @(#)ShapeToInline.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

/** Class to convert Shape nodes into Inlines */
public class ShapeToInline extends InlineCreator {
    
    /** Class constructor, indicate Shape nodes are to be converted, InlineCreator
     *  base class does everything else.
     */
    public ShapeToInline() {
        super( "Shape" );
    }
}
