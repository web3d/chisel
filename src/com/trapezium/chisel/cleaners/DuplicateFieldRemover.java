package com.trapezium.chisel.cleaners;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.chisel.*;

import java.io.PrintStream;

public class DuplicateFieldRemover extends Optimizer {
	int numberFieldsRemoved = 0;

	public DuplicateFieldRemover() {
		super( "All", "Removing repeated fields..." );
	}

	public void attemptOptimization( Node n ) {
		VrmlElement parent = n.getParent();
		if ( parent instanceof DEFUSENode ) {
			DEFUSENode dun = (DEFUSENode)parent;
			if ( !dun.isDEF() ) {
				return;
			}
		}
		int numberFields = n.getNumberFields();
		for ( int i = 0; i < numberFields; i++ ) {
			Field f = n.getFieldAt( i );
			String ferr = f.getError();
			if ( ferr != null ) {
			    if ( ferr.indexOf( "duplicate field" ) > 0 ) {
    				if ( f.getFirstTokenOffset() != -1 ) {
	    				replaceRange( f.getFirstTokenOffset(), f.getLastTokenOffset(), null );
	    			}
				}
			}
		}
	}

	// Since we are removing the default field, optimize by just doing nothing
	public void optimize( TokenPrinter tp, Object param, int start, int end ) {
		numberFieldsRemoved++;
	}

	public void summarize( PrintStream ps ) {
		if ( numberFieldsRemoved == 0 ) {
			ps.println( "DuplicateFieldRemover removed no fields." );
		} else if ( numberFieldsRemoved == 1 ) {
			ps.println( "DuplicateFieldRemover removed 1 field." );
		} else {
			ps.println( "DuplicateFieldRemover removed " + numberFieldsRemoved + " fields." );
		}
	}
}


