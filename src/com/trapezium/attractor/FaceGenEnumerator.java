/*
 * @(#)FaceGenEnumerator.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.chisel.*;
import com.trapezium.space.*;

import java.util.Enumeration;

/** The FaceGenEnumerator is an Enumeration which returns FaceGenerator
 *  objects.  Each FaceGenerator can be used to generate a subset of the
 *  coordIndex values for an IFS reduced by the AttractorReductionAlgorithm.
 *  This object is returned by AttractorReductionAlgorithm.enumerateFaces.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class FaceGenEnumerator implements Enumeration {
    SpaceEntitySet faces;
    int numberGenerators;
    int currentGenerator;
    CoordAttractor farCoords;
    FaceGenerator generator;

    /** Class constructor.
     *
     *  @param attractor a FaceGenerator generates a set of faces for
     *      each attractor face, this parameter is used to pass the 
     *      attractor faces to the FaceGenerator
     *  @param nearCoords the CoordMatcher which matched attractor coords
     *      to floater, preserving nearest floaters
     *  @param farCoords the CoordMatcher which matched the attractor-dual
     *      coords to floater, preserving farthest floaters.
     */
    public FaceGenEnumerator( SpaceStructure attractor,
        CoordAttractor nearCoords, CoordAttractor farCoords ) {
        faces = attractor.getFaces();
        numberGenerators = faces.getNumberEntities();
        currentGenerator = 0;
        this.farCoords = farCoords;
        generator = new FaceGenerator( faces, nearCoords );
    }


    /** Enumeration interface, are more generators available */
    public boolean hasMoreElements() {
        return( currentGenerator < numberGenerators );
    }

    /** Enumeration interface, get the next generator.  Optimization here
     *  is to just return the internal generator after setting it up to
     *  avoid re-creating FaceGenerator objects.  Assumes client is done
     *  with FaceGenerator by time this method is called.
     */
    public Object nextElement() {
        int[] faceInts = faces.getIlist( currentGenerator );
        generator.loadFaceSet( faceInts, 
            farCoords.getPreservedFloaterOffset( currentGenerator ));
        currentGenerator++;
        return( generator );
    }
}
