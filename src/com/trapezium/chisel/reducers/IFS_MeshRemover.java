/*
 * @(#)IFS_MeshRemover.java
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
import java.io.PrintStream;

/**
 *  This is a polygon reduction tool, merges parallel edges into single edges.
 */
public class IFS_MeshRemover extends IFS_SpaceStructureLoader {
    int denominator;

	public IFS_MeshRemover() {
		super( "Parallel edge polygon reduction..." );
		denominator = 200;
	}

    public void replaceCoord( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        float threshold = (float)1/denominator;
//        System.out.println( "threshold is " + threshold );
        int result = ss.parallelEdgePolygonReduction( threshold );
//        System.out.println( "result is " + result );
        if ( result > 0 ) {
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

    public void replaceCoordIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        tp.printRange( startTokenOffset, endTokenOffset, false );
    }

    public void replaceTexCoordIndex( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        tp.printRange( startTokenOffset, endTokenOffset, false );
    }

    /** */
    public int getNumberOptions() {
        return( 1 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        return( Integer.TYPE );
    }

    public String getOptionLabel( int offset ) {
        return( "1/n, threshold for detecting as parallel" );
    }

    public Object getOptionValue( int offset ) {
        return( intToOptionValue(denominator) );
    }

    public void setOptionValue( int offset, Object value ) {
        denominator = optionValueToInt(value);
    }

    public Object getOptionConstraints( int offset ) {
        return( new IntegerConstraints(25, 10000, 25 ));
    }
}


