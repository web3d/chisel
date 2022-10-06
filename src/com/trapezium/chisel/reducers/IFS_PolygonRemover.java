/*
 * @(#)IFS_PolygonRemover.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reducers;

import com.trapezium.chisel.*;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.space.SpaceStructure;
import com.trapezium.vrml.node.space.SpacePrimitive;
import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import java.util.Vector;
import java.io.FileOutputStream;

/**
 *  This is a polygon reduction tool, removes smallest triangles
 *  in an IndexedFaceSet.
 */
public class IFS_PolygonRemover extends IFS_SpaceStructureLoader {
    int minNumberFaces;
    int percentThreshold;
    boolean preserveColorBoundaries;
    static final int MINFACE = 0;
    static final int PERCENT = 1;
    static final int PRESERVE_COLOR_BOUNDARIES = 2;
    
	public IFS_PolygonRemover() {
		super( "Remove small triangles..." );
		minNumberFaces = 50;
		percentThreshold = 10;
		preserveColorBoundaries = false;
	}

    public void replaceCoord( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        if ( ss.smallTrianglePolygonReduction( minNumberFaces, percentThreshold, preserveColorBoundaries )) {
            tp.print( "Coordinate { point [" );
            SpaceEntitySet ses = ss.getEntitySet( SpacePrimitive.Vertex );
            int numberVertices = ses.getNumberEntities();
            for ( int i = 0; i < numberVertices; i++ ) {
                SpacePrimitive sp = ses.getEntity( i );
                tp.print( (float)sp.getX() );
                tp.print( (float)sp.getY() );
                tp.print( (float)sp.getZ() );
            }
            tp.print( "] }" );
        } else {
            tp.printRange( startTokenOffset, endTokenOffset, false );
        }
    }
    
    /** */
    public int getNumberOptions() {
        return( 3 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        switch (offset) {
            case MINFACE:
            case PERCENT:
                try {
                    return( Integer.TYPE );
                } catch (Exception e) {
                    break;
                }
            case PRESERVE_COLOR_BOUNDARIES:
                return( Boolean.TYPE );
        }
        return null;
    }

    public String getOptionLabel( int offset ) {
        switch (offset) {
            case MINFACE:
                return( "minimum face count" );
            case PERCENT:
                return( "% smallest faces to remove" );
            case PRESERVE_COLOR_BOUNDARIES:
                return( "preserve color boundaries" );
        }
        return null;
    }

    public Object getOptionValue( int offset ) {
        switch (offset) {
            case MINFACE:
                return( intToOptionValue(minNumberFaces) );
            case PERCENT:
                return( intToOptionValue(percentThreshold) );
            case PRESERVE_COLOR_BOUNDARIES:
                return( booleanToOptionValue(preserveColorBoundaries) );
        }
        return "";
    }

    public void setOptionValue( int offset, Object value ) {
        switch (offset) {
            case MINFACE:
                minNumberFaces = optionValueToInt(value);
                break;
            case PERCENT:
                percentThreshold = optionValueToInt(value);
                break;
            case PRESERVE_COLOR_BOUNDARIES:
                preserveColorBoundaries = optionValueToBoolean(value);
        }
    }

    public Object getOptionConstraints( int offset ) {
        switch (offset) {
            case MINFACE:
                return( new IntegerConstraints(10, 1000, 10 ));
            case PERCENT:
                return( new IntegerConstraints(5, 100, 5 ));
        }
        return "";
    }
}


