/*
 * @(#)Vertices.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.space;

import java.util.Enumeration;

public class Vertices extends SpaceEntitySet {
    /** Class constructor */
    public Vertices() {
        super( SpaceEntitySet.Float, 3, StandAlone );
    }
    
    /** Copy constructor */
    public Vertices( SpaceEntitySet src ) {
        super( src );
    }
    
    /** Conversion constructor, convert faces into vertices */
    public Vertices( Vertices vertices, Faces faces ) {
        this();
        float[] faceLocation = new float[3];
        Enumeration faceEnumerator = faces.getEnumeration();
        while ( faceEnumerator.hasMoreElements() ) {
            Integer result = (Integer)faceEnumerator.nextElement();
            faces.getLocation( result.intValue(), vertices, faceLocation );
            add3f( faceLocation );
        }
    }
}

