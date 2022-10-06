/*
 * @(#)AttractorReductionAlgorithm.java
 *
 * Copyright (c) 1998-1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.space.*;
import com.trapezium.vrml.node.space.BoundingBox;
import java.util.BitSet;
import java.util.Enumeration;

/** The attractor based polygon reduction algorithm.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 7 Oct 1998
 *
 *  @since           1.0
 */

public class AttractorReductionAlgorithm {
    // attractor fields
    boolean attractorNeedsInit;
    boolean attractorNeedsRescale;

    SpaceStructure attractorSpaceStructure;
    SpaceEntitySet attractorVertices;
    int numberAttractorVertices;

    SpaceStructure dualSpaceStructure;
    SpaceEntitySet dualVertices;
    int numberDualVertices;

    // floater fields
    boolean floaterNeedsInit;

    SpaceStructure floaterSpaceStructure;
    SpaceEntitySet floaterVertices;
    int numberFloaterVertices;

    // bitmap of the floater coordinates that remain after the algorithm
    // has completed.
    BitSet preservedFloaterVertices;

    // generator fields
    CoordAttractor nearCoords;
    CoordAttractor farCoords;

    // the magical reduced result
    SpaceEntitySet magicCoords;
    CoordAttractor magicAttractor;  // may not be necessary...

    // mapping from the magic coords to their original coordinate offsets
    // offset = new coordinate offset, value = original coordinate offset
    int[] newToOldMapping;
    
    /** Class constructor */
    public AttractorReductionAlgorithm() {
    }
    
    /** Get the new coordinate results */
    public SpaceEntitySet getNewCoords() {
        return( magicCoords );
    }

    /** Get mapping between magic coords and original offsets */
    public int[] getNewToOldMapping() {
        return( newToOldMapping );
    }

    /** Get the BitSet indicating which original coordinates are preserved */
    public BitSet getPreservedFloaters() {
        return( preservedFloaterVertices );
    }

    /** The attractor reduction algorithm requires one SpaceStructure
     *  to function as the attractor, and one SpaceStructure to be reduced
     *  (called the floater).  This method sets the attractor.
     *
     *  @param attractor the SpaceStructure to use as the attractor
     */
    public void setAttractor( SpaceStructure attractor ) {
        if ( attractorSpaceStructure != attractor ) {
            attractorSpaceStructure = attractor;
            attractorNeedsInit = true;
            attractorNeedsRescale = true;
        }
    }

    /** The attractor reduction algorithm requires one SpaceStructure
     *  to function as the attractor, and one SpaceStructure to be reduced
     *  (called the floater).  This method sets the floater.
     *
     *  @param floater the SpaceStructure to use as the floater
     */
    public void setOriginal( SpaceStructure floater ) {
        if ( floaterSpaceStructure != floater ) {
            floaterSpaceStructure = floater;
            floaterNeedsInit = true;
            attractorNeedsRescale = true;
        }
    }

    /** Get the original SpaceStructure */
    public SpaceStructure getOriginal() {
        return( floaterSpaceStructure );
    }

    /** The main algorithm, requires the attractor and floater already
     *  be set by setAttractor and setOriginal.  If these are not set
     *  the algorithm does nothing.
     */
    public void reduce() {
        if (( attractorSpaceStructure != null ) &&
            ( floaterSpaceStructure != null )) {
            initAttractorFields();
            initFloaterFields();
            rescaleAttractor();
            createMagicCoords();
        }
    }

    /** Enumerate structures which can be used to generate coordIndex of
     *  the reduced face set.
     */
    public Enumeration enumerateFaces() {
        return( new FaceGenEnumerator( attractorSpaceStructure, nearCoords, farCoords ));
    }

    /** Get the reduced vertices, which is really the original floater set.
     *  We rely on the cleaners to detect the duplicates and unused ones.
     */
    public SpaceEntitySet getFloaterVertices() {
        return( floaterVertices );
    }

    /** Utility methods for the algorithm */

    /** Extracts and generates all the attractor based fields required by
     *  the algorithm.  These fields are just the attractor and dual 
     *  vertices.
     */
    void initAttractorFields() {
        if ( attractorNeedsInit ) {
            attractorNeedsInit = false;
            dualSpaceStructure = new SpaceStructure( attractorSpaceStructure );
            dualSpaceStructure.r2();
            attractorVertices = attractorSpaceStructure.getVertices();
            numberAttractorVertices = attractorVertices.getNumberEntities();
            dualVertices = dualSpaceStructure.getVertices();
            numberDualVertices = dualVertices.getNumberEntities();
        }
    }

    /** initialize all the fields associated with the floater */
    void initFloaterFields() {
        if ( floaterNeedsInit ) {
            floaterNeedsInit = false;
            floaterVertices = floaterSpaceStructure.getVertices();
            numberFloaterVertices = floaterVertices.getNumberEntities();
            preservedFloaterVertices = new BitSet( numberFloaterVertices );
        }
    }

    /** rescale the attractor so its bounding box is same as floater */
    void rescaleAttractor() {
        if ( attractorNeedsRescale ) {
            attractorNeedsRescale = false;
            EntitySetScaler attractorScaler = new EntitySetScaler( 
                attractorVertices );
            EntitySetScaler dualScaler = new EntitySetScaler( dualVertices );
            BoundingBox floaterBounds = floaterSpaceStructure.getBoundingBox();
            attractorScaler.scaleTo( floaterBounds );
            dualScaler.scaleTo( floaterBounds );
        }
    }

    /** Create the set of coordinates in the floater that all other coordinates
     *  map to during the reduction.
     */
    void createMagicCoords() {
        // Create the attractor objects for attracting nearest and farthest
        // coordinates in each subset of coordinates, perform the attraction
        // operation, keeping result in "preservedFloaterVertices" BitSet
        nearCoords = new NearPreservingCoordAttractor( attractorVertices );
        farCoords = new FarPreservingCoordAttractor( dualVertices );
        nearCoords.attract( floaterVertices, preservedFloaterVertices );
        farCoords.attract( floaterVertices, preservedFloaterVertices );

        // create the set of coords that get preserved, then attract to them
        // so that the location of the attracted set can be set to their
        // corresponding preserved location.
        magicCoords = new Vertices();
        newToOldMapping = new int[ preservedFloaterVertices.size() ];
        float[] preservedCoord = new float[3];
        int newOffset = 0;
        for ( int i = 0; i < numberFloaterVertices; i++ ) {
            if ( preservedFloaterVertices.get( i )) {
                floaterVertices.getLocation( i, preservedCoord );
                newToOldMapping[ newOffset++ ] = i;
                magicCoords.add3f( preservedCoord );
            }
        }

        magicAttractor = new CoordAttractor( magicCoords );
        magicAttractor.attract( floaterVertices );

// To be removed, only for reference until functionality is moved elsewhere
        // set the location of each floater vertex to be identical to the
        // magic coordinate it was attracted to
  /*      for ( int i = 0; i < numberFloaterVertices; i++ ) {
            int nearest = magicAttractor.getAttractorOffset( i );
            SpaceEntity magic = magicCoords.getEntity( nearest );
            SpaceEntity floater = floaterVertices.getEntity( i );
            floater.setLocation( magic.x, magic.y, magic.z );*/
            // set the texture, color, normal as well...
            // requires these to be stored in SpacePrimitive
 //       }
    }
}

