/*
 * @(#)AttractorSelector.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

/** The AttractorSelector or one of its subclasses is created by the
 *  CoordAttractor factory method "createAttractorSelector".  It is used
 *  to track the currently selected attractor as a floater is matched
 *  one by one to each attractor.  Subclasses handle marking of the
 *  preserved floaters in each attracted floater set.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class AttractorSelector {
    protected int selectedAttractor;
    protected float selectionDistance;

    /** Class constructor */
    public AttractorSelector() {
        reset();
    }

    /** Initialize the object, AttractorSelector reset each time matching
     *  for a new floater is started.
     */
    public void reset() {
        selectedAttractor = -1;
        selectionDistance = 0f;
    }

    /** Attempt the selection of a particular attractor.  Floaters are
     *  attracted to the nearest attractor.
     *
     *  @param attractorOffset the attractor to attempt to select
     *  @param distanceToFloater the distance for the "attractorOffset"
     *     attractor to the floater
     */
    public void attemptAttractorSelection( int attractorOffset, 
        float distanceToFloater ) {
        if (( selectedAttractor == -1 ) ||
            ( distanceToFloater < selectionDistance )) {
            selectedAttractor = attractorOffset;
            selectionDistance = distanceToFloater;
        }
    }

    /** Template, subclasses use this to mark preserved floaters in
     *  the array parameters passed from the CoordAttractor.
     */
    public void updatePreservedInfo( int[] floaterOffsets,
        float[] floaterDistances, int floaterOffset ) {
    }

    /** Get the selected attractor offset */
    public int getSelection() {
        return( selectedAttractor );
    }
}
