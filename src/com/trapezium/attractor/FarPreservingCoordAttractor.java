/*
 * @(#)FarPreservingCoordAttractor.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.attractor;

import com.trapezium.space.*;

/** The FarPreservingCoordAttractor is a CoordAttractor that marks the
 *  farthest floater in the attracted set.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 8 Oct 1998
 *
 *  @since           1.0
 */

public class FarPreservingCoordAttractor extends CoordAttractor {

    /** Class constructor */
    public FarPreservingCoordAttractor( SpaceEntitySet attractors ) {
        super( attractors );
    }

    /** AttractorSelector which marks farthest floater in attracted set */
    public AttractorSelector createAttractorSelector() {
        return( new FarPreservingAttractorSelector() );
    }
}
