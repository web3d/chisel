/*
 * @(#)DEFToInline.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

import com.trapezium.chisel.*;

import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.Scene;

/** Chisel to convert top level DEF nodes into Inlines */
public class DEFToInline extends InlineCreator {
    
    /** Class constructor, indicate DEF nodes are to be converted */
    public DEFToInline() {
        super( "DEF" );
    }

    /** Override Optimizer base class template method to indicate this is to be
     *  called for any DEF node.
     */
    public boolean isDEFlistener() {
        return( true );
    }
    
    /** Only convert top level Nodes in a Scene */
	public void attemptOptimization( Node n ) {
        if (( n.getParent() instanceof Scene ) && ( n.getParent() == n.getRoot() )) {
            super.attemptOptimization( n );
        }
    }
}
