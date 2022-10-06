/*
 * @(#)EntitySetScaler.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.space;

import com.trapezium.vrml.node.space.BoundingBox;

public class EntitySetScaler {
    SpaceEntitySet original;
    BoundingBox originalBoundingBox;

    public EntitySetScaler( SpaceEntitySet original ) {
        this.original = original;
        this.originalBoundingBox = original.getBoundingBox();
    }

    public void scaleTo( BoundingBox newBoundingBox ) {
        BoundingBoxScaler bbs = new BoundingBoxScaler( originalBoundingBox, newBoundingBox );
        bbs.scaleTo( original );
    }
}
