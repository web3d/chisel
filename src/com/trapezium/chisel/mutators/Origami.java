package com.trapezium.chisel.mutators;

import com.trapezium.chisel.*;

import com.trapezium.vrml.node.space.SpaceEntitySet;
import com.trapezium.vrml.node.space.SpacePrimitive;

public class Origami extends IFS_SpacePartition {
    public void printCoords( TokenPrinter tp, SpaceEntitySet mc, SpacePrimitive sp ) {
        tp.print( (float)mc.getX() );
        tp.print( (float)mc.getY() );
        tp.print( (float)mc.getZ() );
    }
}
