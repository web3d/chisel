/*
 * @(#)IFS_Masher.java
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
import com.trapezium.chisel.reducers.IFS_SpaceStructureLoader;
import com.trapezium.vrml.node.space.SpaceStructure;
import com.trapezium.vrml.node.space.SpacePrimitive;
import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.node.space.PartitionMaker;
import com.trapezium.vrml.node.space.BoundingBox;
import java.util.Hashtable;


/**
 *  This is an implementation of Michael's polygon reduction algorithm,
 *  rebuilds space structure by breaking into cubes, then reconnecting.
 */
abstract public class IFS_Masher extends IFS_SpaceStructureLoader {
    static final int DX = 0;
    static final int DY = 1;
    static final int DZ = 2;
    protected int dx;
    protected int dy;
    protected int dz;
    protected int numberVertices;
    Hashtable boxes;

	public IFS_Masher() {
		super( "Reconnecting vertices..." );
		dx = 5;
		dy = 5;
		dz = 5;
		boxes = new Hashtable();
	}

    public void replaceCoord( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset ) {
        replaceCoord( tp, ss, startTokenOffset, endTokenOffset, true, true );
    }
    
    public void replaceCoord( TokenPrinter tp, SpaceStructure ss, int startTokenOffset, int endTokenOffset, boolean printStart, boolean printEnd ) {
        int scanner = startTokenOffset;
        dataSource.setState( scanner );
        while ( scanner < endTokenOffset ) {
            if ( printStart ) {
                tp.print( dataSource, scanner );
            }
            if ( dataSource.isLeftBracket( scanner )) {
                break;
            }
            scanner = dataSource.getNextToken();
        }
        SpaceEntitySet ses = ss.getEntitySet( SpacePrimitive.Vertex );
        PartitionMaker masher = partitionSpace( ses, dx, dy, dz );
        numberVertices = ses.getNumberEntities();
        for ( int i = 0; i < numberVertices; i++ ) {
            SpacePrimitive sp = ses.getEntity( i );
            SpaceEntitySet mc = masher.getCube( sp );
            printCoords( tp, mc, sp );
        }
        tp.flush();
        while ( scanner < endTokenOffset ) {
            if ( dataSource.isRightBracket( scanner )) {
                break;
            }
            scanner = dataSource.getNextToken();
        }
        while ( scanner <= endTokenOffset ) {
            if ( printEnd ) {
                tp.print( dataSource, scanner );
            }
            scanner = dataSource.getNextToken();
        }
        tp.flush();
    }

   
    abstract public void printCoords( TokenPrinter tp, SpaceEntitySet mc, SpacePrimitive sp );
    abstract public PartitionMaker partitionSpace( SpaceEntitySet ses, int dx, int dy, int dz );
            
    /** xSlice, ySlice, zSlice (need to add a "preserve color boundaries" if this works) */
    public int getNumberOptions() {
        return( 3 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int offset ) {
        switch (offset) {
            case DX:
            case DY:
            case DZ:
                try {
                    return( Integer.TYPE );
                } catch (Exception e) {
                    break;
                }
        }
        return null;
    }

    public String getOptionLabel( int offset ) {
        switch (offset) {
            case DX:
                return( "X slices" );
            case DY:
                return( "Y slices" );
            case DZ:
                return( "Z slices" );
        }
        return null;
    }

    public Object getOptionValue( int offset ) {
        switch (offset) {
            case DX:
                return( intToOptionValue(dx) );
            case DY:
                return( intToOptionValue(dy) );
            case DZ:
                return( intToOptionValue(dz) );
        }
        return "";
    }

    public void setOptionValue( int offset, Object value ) {
        switch (offset) {
            case DX:
                dx = optionValueToInt(value);
                break;
            case DY:
                dy = optionValueToInt(value);
                break;
            case DZ:
                dz = optionValueToInt(value);
                break;
        }
    }

    public Object getOptionConstraints( int offset ) {
        switch (offset) {
            case DX:
            case DY:
            case DZ:
                return( new IntegerConstraints(2, 200, 1 ));
        }
        return "";
    }
}


