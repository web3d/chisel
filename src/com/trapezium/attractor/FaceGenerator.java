/*
 * @(#)FaceGenerator.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.chisel.*;
import com.trapezium.space.*;

/** The FaceGenerator generates a set of triangles based on a face from
 *  the original attractor and its dual.  The triangles are two points from
 *  the attractor face edge, and the one dual point in the middle of the
 *  attractor face.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class FaceGenerator {
    SpaceEntitySet faces;
    CoordAttractor nearCoords;

    /** Class constructor.
     *
     *  @param faces needed to access the vertex offsets of each face
     *  @param nearCoords needed to convert an attractor offset into
     *     its corresponding preserved floater offset
     */
    public FaceGenerator( SpaceEntitySet faces, CoordAttractor nearCoords ) {
        this.faces = faces;
        this.nearCoords = nearCoords;
    }

    int numberFaces;
    int dualMatch;
    int[] face;

    /** Almost a class constructor, FaceGenEnumerator optimization is to
     *  set values rather than re-create a FaceGenerator.  This method
     *  accepts those values.
     */
    public void loadFaceSet( int[] faceInts, int dualMatch ) {
        numberFaces = faceInts.length;
        this.face = faceInts;
        this.dualMatch = dualMatch;
    }

    /** Get the number of faces this will generate */
    public int getNumberFaces() {
        return( numberFaces );
    }

    /** Get the floater offset values of a particular generated face.
     *
     * @param faceOffset which face to generate
     * @param faceIndex output parameter containing floater offset list of face
     *
     * @return true if faceIndex values are valid, false if any faceIndex
     *    value is -1
     */
    public boolean getFace( int faceOffset, int[] faceIndex ) {
        faceIndex[0] = nearCoords.getPreservedFloaterOffset( face[ faceOffset ] );
        faceOffset++;
        if ( faceOffset >= numberFaces ) {
            faceOffset = 0;
        }
        faceIndex[1] = nearCoords.getPreservedFloaterOffset( face[ faceOffset ] );
        faceIndex[2] = dualMatch;
        return(( faceIndex[0] != -1 ) && ( faceIndex[1] != -1 ) &&
               ( faceIndex[2] != -1 ));
    }
}
