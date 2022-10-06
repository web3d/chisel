/*
 * @(#)PartitionMaker.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node.space;

import java.util.Hashtable;
import java.util.Enumeration;

/** The PartitionMaker is used to partition space.  This one is the simplest case
 *  where space is partitioned into even units.
 */
abstract public class PartitionMaker {
    protected float xMin;
    protected float yMin;
    protected float zMin;
    protected float xMax;
    protected float yMax;
    protected float zMax;
    protected int xSlice;
    protected int ySlice;
    protected int zSlice;
    Hashtable mashTable;

    public PartitionMaker( SpaceEntitySet spaceEntitySet, int xSlice, int ySlice, int zSlice ) {
        BoundingBox b = spaceEntitySet.getBoundingBox();
        this.xSlice = xSlice;
        this.ySlice = ySlice;
        this.zSlice = zSlice;
        xMin = (float)b.getMinX();
        yMin = (float)b.getMinY();
        zMin = (float)b.getMinZ();
        xMax = xMin + (float)b.getXdimension();
        yMax = yMin + (float)b.getYdimension();
        zMax = zMin + (float)b.getZdimension();
        mashTable = new Hashtable();
        createPartitions( spaceEntitySet );
        int numberEntities = spaceEntitySet.getNumberEntities();
        for ( int i = 0; i < numberEntities; i++ ) {
            add( spaceEntitySet.getEntity( i ));
        }
        Enumeration e = mashTable.elements();
        while ( e.hasMoreElements() ) {
            SpaceEntitySet s = (SpaceEntitySet)e.nextElement();
            s.setCenterLocation();
        }
    }

    abstract public void createPartitions( SpaceEntitySet spaceEntitySet );
    abstract public SpaceEntitySet getCube( SpacePrimitive sp );

    void add( SpacePrimitive sp ) {
        SpaceEntitySet s = getCube( sp );
        s.addEntity( sp );
    }
}
