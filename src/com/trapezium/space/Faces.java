/*
 * @(#)Faces.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.space;
import java.util.Enumeration;
public class Faces extends SpaceEntitySet {
    /** Class constructor */
    Faces() {
        super( SpaceEntitySet.Int, -1, StandAlone );
    }
    
    /** Copy constructor */
    Faces( SpaceEntitySet src ) {
        super( src );
    }
    
    /** Create faces from vertices */
    Faces( SpaceEntitySet vertices, SpaceEntitySet faces ) {
        this();
        
        // create one face for each vertex
        int numberVertices = vertices.getNumberEntities();
        for ( int i = 0; i < numberVertices; i++ ) {
            // get all the faces that use a particular coordinate
            int[] faceOffset = faces.getIlistOffsets( i );
            
            // the coordinates for the new face are based on this
            int[] coordinates = new int[ faceOffset.length * 2 ];
            int coordIdx = 0;
            int[] coords = new int[2];
            
            // get all the coordinates connected to coordinate "i"
            for ( int j = 0; j < faceOffset.length; j++ ) {
                // in each face, get the coord before and after "i"
                faces.getConnectingIndex( faceOffset[j], i, coords );
                coordinates[ coordIdx++ ] = coords[0];
                coordinates[ coordIdx++ ] = coords[1];
            }
            
            // convert coordinates into an ordered set
            int[] orderedCoordinates = new int[ coordinates.length/2 ];
            int[] faceOrdering = new int[ coordinates.length/2 ];
            int orderedCoordIdx = 0;
            int coordValue = getCoordValue( coordinates, coordIdx );
            while ( coordValue != -1 ) {
                faceOrdering[ orderedCoordIdx ] = faceVal;
                orderedCoordinates[ orderedCoordIdx++ ] = coordValue;
                coordValue = getNextCoordValue( coordinates, coordIdx, coordValue );
            }
            
            // now add the face values in the proper order
            for ( int x = 0; x < faceOrdering.length; x++ ) {
                add( faceOffset[ faceOrdering[x] ] );
            }
            add( -1 );
        }
    }
    
    int faceVal;
    int getNextCoordValue( int[] coordinates, int coordIdx, int coordValue ) {
        for ( int i = 1; i < coordIdx; i+= 2 ) {
            if ( coordinates[i] == coordValue ) {
                faceVal = i/2;
                return( coordinates[i-1] );
            }
        }
        return( -1 );
    }
    
    int getCoordValue( int[] coordinates, int coordIdx ) {
        for ( int i = 0; i < coordIdx; i += 2 ) {
            if ( coordinates[i] != -1 ) {
                faceVal = i/2;
                return( coordinates[i] );
            }
        }
        return( -1 );
    }
}


