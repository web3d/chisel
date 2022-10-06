/*
 * @(#)IFS_SpacePartition.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.node.space.SpacePartitionMaker;
import com.trapezium.vrml.node.space.PartitionMaker;


/**
 *  Abstract base class for a chisel which requires space partitioning.
 */
abstract public class IFS_SpacePartition extends IFS_Masher {
    public PartitionMaker partitionSpace( SpaceEntitySet ses, int dx, int dy, int dz ) {
        return( new SpacePartitionMaker( ses, dx, dy, dz ));
    }
}


