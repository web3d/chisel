/*
 * @(#)DensityPartitionMaker.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.space;

/** The DensityPartitionMaker is used to partition space.  This one is the simplest case
 *  where space is partitioned into even units.
 */
public class DensityPartitionMaker extends PartitionMaker {
    float dx;
    float dy;
    float dz;

    public DensityPartitionMaker( SpaceEntitySet spaceEntitySet, int xSlice, int ySlice, int zSlice ) {
        super( spaceEntitySet, xSlice, ySlice, zSlice );
    }

    public void createPartitions( SpaceEntitySet spaceEntitySet ) {
        BoundingBox b = spaceEntitySet.getBoundingBox();
        dx = (float)( (float)b.getXdimension()/(float)xSlice );
        dy = (float)( (float)b.getYdimension()/(float)ySlice );
        dz = (float)( (float)b.getZdimension()/(float)zSlice );
    }


    public SpaceEntitySet getCube( SpacePrimitive sp ) {
        int whichx = (int)(((float)sp.getX() - xMin)/dx);
        if ( whichx == xSlice ) {
            whichx--;
        }
        int whichy = (int)(((float)sp.getY() - yMin)/dy);
        if ( whichy == ySlice ) {
            whichy--;
        }
        int whichz = (int)(((float)sp.getZ() - zMin)/dz);
        if ( whichz == zSlice ) {
            whichz--;
        }
        String key = new String( whichx + "_" + whichy + "_" + whichz );
        SpaceEntitySet s = (SpaceEntitySet)mashTable.get( key );
        if ( s == null ) {
            s = new SpaceEntitySet( new Strategy( null, SpacePrimitive.Vertex ), null );
            mashTable.put( key, s );
        }
        return( s );
    }
}
