/*
 * @(#)EV_to_IFS.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.parse.TokenTypes;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.fields.ExposedField;
import com.trapezium.vrml.fields.SFNodeValue;

public class EV_to_IFS extends Optimizer {

    /** The main class for performing an ElevationGrid split operation */
    class EVinfo {
        Node elevationGridNode;
        float[] heights;

        public EVinfo( Node n ) {
            elevationGridNode = n;
        }

        void convertToIFS( TokenPrinter tp ) {
            tp.print( "IndexedFaceSet {" );
		    int xDimension = elevationGridNode.getIntValue( "xDimension" );
    		int zDimension = elevationGridNode.getIntValue( "zDimension" );
    		float xSpacing = elevationGridNode.getFloatValue( dataSource, "xSpacing" );
    		float zSpacing = elevationGridNode.getFloatValue( dataSource, "zSpacing" );
    		int numberPoints = xDimension*zDimension;
    		if ( numberPoints > 0 ) {
        		heights = elevationGridNode.getFloatArray( "height" );
        		if ( heights != null ) {
        		    // coordinates
            		tp.flush();
            		tp.print( "coord Coordinate { point [" );
            		tp.flush();
            		int heightScanner = 0;
        		    for ( int i = 0; i < zDimension; i++ ) {
        		        float zValue = zSpacing * i;
        		        for ( int j = 0; j < xDimension; j++ ) {
        		            float xValue = xSpacing * j;
        		            tp.print( xValue );
        		            tp.print( heights[ heightScanner ] );
        		            tp.print( zValue );
        		            heightScanner++;
        		        }
        		        tp.flush();
        		    }
            		tp.print( "] }" );
            		tp.flush();
            		
            		// coordIndex
            		tp.print( "coordIndex [" );
            		tp.flush();
            		for ( int i = 0; i < ( zDimension - 1 ); i++ ) {
            		    for ( int j = 0; j < ( xDimension - 1 ); j++ ) {
            		        int base = i * xDimension + j;
            		        // each egrid square turns into two triangles
            		        tp.print( base );
            		        tp.print( base + xDimension );
            		        tp.print( base + xDimension + 1 );
            		        tp.print( -1 );
            		        tp.print( base );
            		        tp.print( base + xDimension + 1 );
            		        tp.print( base + 1 );
            		        tp.print( -1 );
            		    }
            		    tp.flush();
            		}
            		tp.print( "]" );
            		tp.flush();
            	}
        	}
        	Field texCoord = elevationGridNode.getField( "texCoord" );
        	if ( texCoord != null ) {
        	    tp.flush();
        	    tp.printRange( texCoord.getFirstTokenOffset(), texCoord.getLastTokenOffset(), false );
        	    tp.flush();
        	}
            tp.print( "}" );
        }
    }

	public EV_to_IFS() {
		super( "ElevationGrid", "Convert ElevationGrid to IFS..." );
	}

	public void attemptOptimization( Node n ) {
        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), new EVinfo( n ));
	}


	// RangeReplacer calls this when it has a range of tokens to replace
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param instanceof EVinfo ) {
	        EVinfo ei = (EVinfo)param;
	        ei.convertToIFS( tp );
	    }
	}
}


