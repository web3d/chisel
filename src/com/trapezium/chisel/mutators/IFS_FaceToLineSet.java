/*
 * @(#)IFS_FaceToLineSet.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.Field;

/**
 *  This chisel turns IndexedFaceSets into IndexedLineSets.
 */
public class IFS_FaceToLineSet extends IFS_Converter {
    /** Constructor, convert IndexedFaceSet to IndexedLineSet */
	public IFS_FaceToLineSet() {
	    super( "IndexedLineSet" );
	}

    /** IndexedLineSet color controls similar to IndexedFaceSet color controls */
    public void convertColorInfo( Node n, TokenPrinter tp, 
        int colorStart, VrmlElement color,
        int colorPerVertexStart, VrmlElement colorPerVertex,
        int colorIndexStart, VrmlElement colorIndex ) {
	    if ( colorStart != -1 ) {
	        tp.printRange( colorStart, color.getLastTokenOffset(), false );
	    }
	    if ( colorPerVertexStart != -1 ) {
	        tp.printRange( colorPerVertexStart, colorPerVertex.getLastTokenOffset(), false );
	    }
	    if ( colorIndexStart != -1 ) {
	        tp.printRange( colorIndexStart, colorIndex.getLastTokenOffset(), false );
	    }
    }

    void printCoordIndex( TokenPrinter tp, Field coordIndex ) {
        int endTokenOffset = coordIndex.getLastTokenOffset();
        int scanner = coordIndex.getFirstTokenOffset();
        dataSource.setState( scanner );
        while ( true ) {
            scanner = tp.printNonNumbers( scanner, endTokenOffset );
            if ( scanner >= endTokenOffset ) {
                break;
            }
            scanner = convertFace( tp, scanner, endTokenOffset );
        }
    }
    
    int convertFace( TokenPrinter tp, int scanner, int endTokenOffset ) {
	    int firstValue = dataSource.getIntValue( scanner );
	    int value = firstValue;
	    while ( true ) {
	        if ( value == -1 ) {
	            tp.print( firstValue );
	            tp.print( value );
	            break;
	        }
	        tp.print( dataSource, scanner );
	        scanner = dataSource.getNextToken();
	        value = dataSource.getIntValue( scanner );
	    }
	    scanner = dataSource.getNextToken();
	    return( scanner );
	}
}


