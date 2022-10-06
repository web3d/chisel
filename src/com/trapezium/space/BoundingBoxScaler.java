package com.trapezium.space;

import com.trapezium.vrml.node.space.BoundingBox;

public class BoundingBoxScaler {
    float xToZero;
    float yToZero;
    float zToZero;
    float xFactor;
    float yFactor;
    float zFactor;
    float xShift;
    float yShift;
    float zShift;

    public BoundingBoxScaler( BoundingBox oldBoundingBox, BoundingBox newBoundingBox ) {
        xToZero = -1*oldBoundingBox.getMinX();
        yToZero = -1*oldBoundingBox.getMinY();
        zToZero = -1*oldBoundingBox.getMinZ();
        xFactor = newBoundingBox.getXdimension()/oldBoundingBox.getXdimension();
        yFactor = newBoundingBox.getYdimension()/oldBoundingBox.getYdimension();
        zFactor = newBoundingBox.getZdimension()/oldBoundingBox.getZdimension();
        xShift = newBoundingBox.getMinX();
        yShift = newBoundingBox.getMinY();
        zShift = newBoundingBox.getMinZ();
    }

    public void scaleTo( SpaceEntitySet set ) {
        set.adjustValues( xToZero, xFactor, xShift, yToZero, yFactor, yShift, zToZero, zFactor, zShift );
    }
}
