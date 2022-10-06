/*
 * @(#)ResolutionAdjuster.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.condensers;

import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.chisel.*;

/**
 *  This adjusts numeric resolution to a specific number of places
 *  beyond the decimal point.
 */
public class ResolutionAdjuster extends Optimizer {

	public ResolutionAdjuster( String s, String actionStr ) {
		super( s, actionStr );
	}

	/** Adjust resolution */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param instanceof Integer ) {
	        int resolution = ((Integer)param).intValue();
	        int resolutionFactor = 1;
	        for ( int i = 0; i < resolution; i++ ) {
	            resolutionFactor = resolutionFactor * 10;
	        }
	        int scanner = startTokenOffset;
	        dataSource.setState( scanner );
	        while ( scanner != -1 ) {
	            if ( dataSource.isNumber( scanner )) {
	                float fval = dataSource.getFloat( scanner );
	                fval = fval * resolutionFactor;
	                int ival = (int)fval;
	                fval = fval - (float)ival;
	                if ( fval <= -.5f ) {
	                    ival--;
                    } else if ( fval >= .5f ) {
                        ival++;
                    }
	                tp.printAtResolution( ival, resolution );
	            } else {
	                tp.print( dataSource, scanner );
	            }
	            if ( scanner >= endTokenOffset ) {
	                break;
	            }
	            scanner = dataSource.getNextToken();
	        }
        }
	}
}
