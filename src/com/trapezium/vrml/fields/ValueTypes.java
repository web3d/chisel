/*
 * @(#)ValueTypes.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

/**
 *  Constants to restrict float values to only positive vs. all values.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 8 Dec 1997
 *
 *  @since           1.0
 */
public interface ValueTypes {
    // types checked
    static public final int AllValues = 0;
    static public final int PositiveValues = 1;
    static public final int BboxSizeValues = 2;
    
    // types returned
    static public final int Unknown = 3;
    static public final int Positive = 4;
    static public final int NonNegative = 5;
    static public final int NegativeOne = 6;
    static public final int Negative = 7;
}
