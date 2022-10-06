package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.vrml.node.space.BoundingBox;
import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.node.space.SpacePrimitive;

public class Cubist extends IFS_SpacePartition {
    public void printCoords( TokenPrinter tp, SpaceEntitySet mc, SpacePrimitive sp ) {
        BoundingBox b = (BoundingBox)boxes.get( mc );
        if ( b == null ) {
            b = mc.getBoundingBox();
            boxes.put( mc, b );
        }
        tp.print( (float)b.getClosestX( sp.getX() ));
        tp.print( (float)b.getClosestY( sp.getY() ));
        tp.print( (float)b.getClosestZ( sp.getZ() ));
    }
}
