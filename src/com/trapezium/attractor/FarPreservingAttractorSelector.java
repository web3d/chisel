/*
 * @(#)FarPreservingAttractorSelector.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.vrml.node.space.*;

/** The FarPreservingAttractorSelector marks for preservation the farthest
 *  floater in the attracted floater set.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class FarPreservingAttractorSelector extends AttractorSelector {

    /** Class constructor */
    public FarPreservingAttractorSelector() {
        super();
    }

    /** Update the floater-to-be-preserved information, the farthest floater
     *  in the attracted set is preserved.
     *
     *  @param floaterOffsets the offset of each floater to be preserved,
     *     the attractor offset is the index into this array
     *  @param floaterDistances the distance between the attractor and
     *     the current to-be-preserved floater, the attractor offset is
     *     the index into this array.
     *  @param floaterOffset the floater that may override previous
     *     to-be-preserved floater.
     */
    public void updatePreservedInfo( int[] floaterOffsets,
        float[] floaterDistances, int floaterOffset ) {
        if (( floaterOffsets[ selectedAttractor ] == -1 ) ||
            ( selectionDistance > floaterDistances[ selectedAttractor ] )) {
            floaterOffsets[ selectedAttractor ] = floaterOffset;
            floaterDistances[ selectedAttractor ] = selectionDistance;
        }
    }
}
