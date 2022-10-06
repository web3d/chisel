/*
 * @(#)Table7.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.grammar;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;

/** 
 *  Table7 handles all the VRML97 base profile conformance limits, from
 *  Table 7 of the VRML97 specification.  All warnings resulting from
 *  this limit include the string "base profile", and can be found by 
 *  searching for this string.
 */
public class Table7 {
    static public final int ChildrenLimit = 500;
    static public final int KeyValueLimit = 1000;
    static public final int NameLimit = 50;
    static public final int UrlLimit = 10;
    static public final int InterfaceLimit = 30;
    static public final int ScriptInterfaceLimit = 25;
    static public final int PROTONestingLimit = 5;
    static public final int SFImageWidthLimit = 256;
    static public final int SFImageHeightLimit = 256;
    static public final int SFStringLengthLimit = 30000;
    static public final int MFColorLimit = 15000;
    static public final int MFFloatLimit = 1000;
    static public final int MFInt32Limit = 20000;
    static public final int MFNodeLimit = 500;
    static public final int MFRotationLimit = 1000;
    static public final int MFInterpolatorLimit = 1000;
    static public final int MFStringLimit = 10;
    static public final int MFVec2fLimit = 15000;
    static public final int MFVec3fLimit = 15000;
    static public final int MFTimeLimit = 1000;
    static public final int TextStringLimit = 100;
    static public final int ElevationGridHeightLimit = 16000;
    static public final int PointSetLimit = 5000;
    static public final int ExtrusionLimit = 2500;
    static public final int ILSIndexLimit = 15000;
    static public final int IFSMaxFaces = 5000;
    static public final int IFSMaxVerticesPerFace = 10;
    static public final int TextMaxStrings = 100;
    static public final int TextMaxCharPerString = 100;
}
