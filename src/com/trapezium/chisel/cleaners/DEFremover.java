package com.trapezium.chisel.cleaners;

import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.chisel.*;
import java.io.PrintStream;

//
//  Removes unused DEFs from a file
//

public class DEFremover extends Optimizer {
	int removedDEFCount = 0;

    // listens for "DEFUSENode"
	public DEFremover() {
		super( "DEFUSENode", "Removing unused DEFs..." );
	}

	public boolean isDEFUSElistener() {
		return( true );
	}

    /** DEFremover has a single option controlling whether Viewpoint DEFs get removed */
    public int getNumberOptions() {
        return( 1 );
    }

    /** Get the class for an option */
    public Class getOptionClass( int optionOffset ) {
        Class c;
        try {
            c = Boolean.TYPE;
        } catch (Exception e) {
            c = null;
        }
        return c;
    }

    public String getOptionLabel( int offset ) {
        return( "Preserve Viewpoint DEFs" );
    }

    boolean preserveViewpointDEFs = true;
    public Object getOptionValue( int offset ) {
        return( booleanToOptionValue(preserveViewpointDEFs) );
    }

    public void setOptionValue( int offset, Object value ) {
        preserveViewpointDEFs = optionValueToBoolean(value);
    }

	public void attemptOptimization( Node n ) {
		if ( n instanceof DEFUSENode ) {
			DEFUSENode dun = (DEFUSENode)n;
			if ( dun.isDEF() && !dun.isUsed() ) {
				n = dun.getNode();
				if ( !preserveViewpointDEFs || ( n.getBaseName().compareTo( "Viewpoint" ) != 0 )) {
    				replaceRange( dun.getFirstTokenOffset(), n.getFirstTokenOffset() - 1, null );
    			}
			}
		}
	}

	// Since we are removing the shape, optimize by just doing nothing
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
//		tp.print( dataSource, endTokenOffset );
		removedDEFCount++;
	}

	public void summarize( PrintStream ps ) {
		if ( removedDEFCount == 0 ) {
			ps.println( "DEFremover removed no DEFs." );
		} else if ( removedDEFCount == 1 ) {
			ps.println( "DEFremover removed 1 DEF." );
		} else {
			ps.println( "DEFremover removed " + removedDEFCount + " DEFs." );
		}
	}
}


