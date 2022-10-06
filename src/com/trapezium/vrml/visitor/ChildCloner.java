/*
 * @(#)ChildCloner.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.visitor;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.pattern.Visitor;

public class ChildCloner extends Visitor {
    VrmlElement parent;
    PROTOInstance protoInstance;

    public ChildCloner( VrmlElement parentClone, VrmlElement pi ) {
        super( null );
        parent = parentClone;
        protoInstance = (PROTOInstance)pi;
    }

    /**
     *  Visits each original non-clone child, and clones them.
     */
    public boolean visitObject( Object a ) {
        // level 1 is object we are cloning children for
        if ( visitLevel == 1 ) {
            return( true );
        }
        // level 2 is children, return false to end visiting
        if ( a instanceof VrmlElement ) {
            VrmlElement ve = (VrmlElement)a;
            VrmlElement childClone = (VrmlElement)ve.vrmlClone( protoInstance );
            parent.addChild( childClone );
        }
        return( false );
    }
}

