/*
 * @(#)ROUTEMover.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.cleaners;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.ROUTE;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.chisel.*;
import java.io.PrintStream;

/**
 *  The ROUTEMover moves bad routes to the end of the file.  Some VRML generators
 *  include ROUTE statements within children nodes, this chisel is meant to correct
 *  that problem.
 */
public class ROUTEMover extends Optimizer {
    int movedROUTEs;

    public ROUTEMover() {
        super( null, "Move ROUTEs to end" );
        movedROUTEs = 0;
    }

    public boolean isROUTElistener() {
        return( true );
    }

	public void attemptOptimization( ROUTE route ) {
	    // don't move any ROUTEs that are contained in PROTOs
	    VrmlElement p = route.getParent();
	    if ( p != null ) {
	        p = p.getParent();
	        if ( p instanceof PROTO ) {
	            return;
	        }
	    }
		replaceRange( route.getFirstTokenOffset(), route.getLastTokenOffset(), null );
		eofTokens( route.getFirstTokenOffset(), route.getLastTokenOffset() );
	}

	// The first phase of the optimization just removes the route, the second phase
	// reprints the route at the end
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
		movedROUTEs++;
	}

	public void summarize( PrintStream ps ) {
		if ( movedROUTEs == 0 ) {
			ps.println( "ROUTEmover did nothing." );
		} else if ( movedROUTEs == 1 ) {
			ps.println( "ROUTEmover moved 1 ROUTE." );
		} else {
			ps.println( "ROUTEmover moved " + movedROUTEs + " ROUTEs." );
		}
	}
}

