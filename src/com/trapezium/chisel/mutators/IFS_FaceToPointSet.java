/*
 * @(#)IFS_FaceToPointSet.java
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
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import java.util.Vector;
import java.util.BitSet;
import java.io.PrintStream;

/**
 *  This chisel turns IndexedFaceSets into PointSets
 */
public class IFS_FaceToPointSet extends IFS_Converter {
    /** Constructor, convert IndexedFaceSet to IndexedLineSet */
	public IFS_FaceToPointSet() {
	    super( "PointSet" );
	}

    /** IndexedLineSet color controls similar to IndexedFaceSet color controls */
    public void convertColorInfo( Node n, TokenPrinter tp, int colorStart, VrmlElement color,
        int colorPerVertexStart, VrmlElement colorPerVertex,
        int colorIndexStart, VrmlElement colorIndex ) {
        if ( n.getBoolValue( "colorPerVertex" )) {
            if ( colorIndexStart == -1 ) {
        	    if ( colorStart != -1 ) {
        	        tp.printRange( colorStart, color.getLastTokenOffset(), false );
        	    }
        	} else {
        	    Field f = (Field)colorIndex;
        	    FieldValue fv = f.getFieldValue();
        	    if ( fv instanceof MFFieldValue ) {
        	        MFFieldValue mfv = (MFFieldValue)fv;
        	        int count = mfv.getRawValueCount();
        	        int scanner = colorIndexStart;
        	        dataSource.setState( scanner );
        	        scanner = dataSource.skipNonNumbers();
        	        tp.print( "color Color { color [" );
        	        for ( int i = 0; i < count; i++ ) {
        	            if ( scanner != -1 ) {
            	            int value = dataSource.getIntValue( scanner );
            	            printColor( tp, color, value );
            	            scanner = dataSource.getNextToken();
            	            scanner = dataSource.skipNonNumbers();
            	        } else {
            	            tp.print( 1 );
            	            tp.print( 1 );
            	            tp.print( 1 );
            	        }
            	    }
            	    tp.print( "] }" );
       	        }
       	    }
       	}
   	}

    void printColor( TokenPrinter tp, VrmlElement color, int offset ) {
        int state = dataSource.getState();
        dataSource.setState( color.getFirstTokenOffset() );
        int scanner = dataSource.getState();
        scanner = dataSource.skipNonNumbers();
        for ( int i = 0; i < offset; i++ ) {
            for ( int j = 0; j < 3; j++ ) {  // skip r,g,b
                scanner = dataSource.getNextToken();
                scanner = dataSource.skipNonNumbers();
            }
        }
        for ( int j = 0; j < 3; j++ ) {
            tp.print( dataSource, scanner );
            scanner = dataSource.getNextToken();
            scanner = dataSource.skipNonNumbers();
        }
    }


    void printCoordIndex( TokenPrinter tp, Field coordIndex ) {
	}
}


