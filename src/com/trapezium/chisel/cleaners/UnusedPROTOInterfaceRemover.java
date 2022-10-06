/*
 * @(#)UnusedPROTOInterfaceRemover.java
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
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.PROTO;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.chisel.*;

import java.io.PrintStream;
import java.util.Vector;
import java.util.Hashtable;

public class UnusedPROTOInterfaceRemover extends Optimizer {
	int numberFieldsRemoved = 0;
	Hashtable unusedPROTOfields;

	public UnusedPROTOInterfaceRemover() {
		super( "All", "Removing unused PROTO interface fields..." );
		unusedPROTOfields = new Hashtable();
	}

	public void registerReplacement( String fieldName, String protoName ) {
	    unusedPROTOfields.put( fieldName + "_" + protoName, this );
	    if ( unusedPROTOfields.get( fieldName ) == null ) {
	        unusedPROTOfields.put( fieldName, this );
	    }
	}
	
	/** This Chisel gets access to the interior of PROTOs */
	public boolean isPROTOlistener() {
	    return( true );
	}

	public void attemptOptimization( Node n ) {
		if ( n instanceof PROTO ) {
		    PROTO p = (PROTO)n;
		    Vector interfaceVector = p.getInterfaceVector();
		    if ( interfaceVector != null ) {
		        int count = interfaceVector.size();
		        for ( int i = 0; i < count; i++ ) {
		            Field f = (Field)interfaceVector.elementAt( i );
		            String err = f.getError();
		            if ( err != null ) {
		                if ( err.indexOf( "not referenced" ) > 0 ) {
        					replaceRange( f.getFirstTokenOffset(), f.getLastTokenOffset(), null );
        					registerReplacement( f.getFieldId(), p.getId() );
        				}
        			}
        		}
        	}
		} else if ( n instanceof PROTOInstance ) {
		    int numberChildren = n.numberChildren();
		    PROTOInstance p = (PROTOInstance)n;
		    String protoName = p.getPROTOname();
		    for ( int i = 0; i < numberChildren; i++ ) {
		        VrmlElement v = n.getChildAt( i );
		        if ( v instanceof Field ) {
		            Field f = (Field)v;
		            String fid = f.getFieldId();
		            if ( fid != null ) {
    		            if ( unusedPROTOfields.get( fid ) != null ) {
    		                String test = fid + "_" + protoName;
    		                if ( unusedPROTOfields.get( test ) != null ) {
    		                    replaceRange( f.getFirstTokenOffset(), f.getLastTokenOffset(), null );
    		                }
    		            }
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
			ps.println( "UnusedPROTOInterfaceRemover removed no fields." );
		} else if ( numberFieldsRemoved == 1 ) {
			ps.println( "UnusedPROTOInterfaceRemover removed 1 field." );
		} else {
			ps.println( "UnusedPROTOInterfaceRemover removed " + numberFieldsRemoved + " fields." );
		}
	}
}


