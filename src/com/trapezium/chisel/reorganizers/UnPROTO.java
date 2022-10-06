/*
 * @(#)UnPROTO.java
 *
 * Copyright (c) 1999 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.chisel.reorganizers;

import com.trapezium.chisel.*;

//
//  The UnPROTO chisel converts PROTO instances into their original form
//

import java.io.PrintStream;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.PROTObase;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.Field;
import java.util.Vector;

public class UnPROTO extends Optimizer {
    class ProtoExpansionParams {
        PROTOInstance pi;
        PROTObase pb;
        
        ProtoExpansionParams( PROTOInstance pi, PROTObase pb ) {
            this.pi = pi;
            this.pb = pb;
        }
        
        public PROTOInstance getPROTOInstance() {
            return( pi );
        }
        
        public PROTObase getPROTObase() {
            return( pb );
        }
    }
    
    int protoRemoveCount;
    
	public UnPROTO() {
		super( "All", "Replacing PROTO instances with nodes..." );
		reset();
	}
	
	public void reset() {
	    protoRemoveCount = 0;
	}
	
    /** Called for each Inline encountered */
	public void attemptOptimization( Node n ) {
	    if ( n instanceof PROTOInstance ) {
	        PROTOInstance pi = (PROTOInstance)n;
	        PROTObase pb = pi.getPROTObase();
	        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), new ProtoExpansionParams( pi, pb ));
	    }
	}

    void expandProtoInstance( PROTOInstance pi, PROTObase pb, Scene protoBaseScene, TokenPrinter tp ) {
        // get a list of all IS fields in the PROTObase
        Vector isVector = pb.getISfields();
        
        // protoBaseScene's first node is the type
        //Node n = (Node)protoBaseScene.getChildAt( 0 );
        
        // need to find any PROTOInstance fields that correspond to IS fields
        int numberIsFields = ( isVector == null ) ? 0 : isVector.size();
        int[] isFieldOffsets = null;
        if ( numberIsFields > 0 ) {
            isFieldOffsets = new int[ numberIsFields ];
            for ( int i = 0; i < numberIsFields; i++ ) {
                ISField isField = (ISField)isVector.elementAt( i );
                isFieldOffsets[i] = isField.getFirstTokenOffset();
            }
        }
        
        for (int allChildren = 0; allChildren < protoBaseScene.numberChildren(); allChildren++ ) {
            Node n = null;
            Object qqq = protoBaseScene.getChildAt( allChildren );
            if ( qqq instanceof Node ) {
                n = (Node)qqq;
            } else {
                continue;
            }

        int last = n.getLastTokenOffset();
        for ( int i = n.getFirstTokenOffset(); i <= last; i++ ) {
            boolean foundIs = false;
            for ( int j = 0; j < numberIsFields; j++ ) {
                if ( i == isFieldOffsets[j] ) {
                    // if PROTO instance defined field, use that field
                    ISField isField = (ISField)isVector.elementAt( j );
                    String fieldId = isField.getFieldId();
                    System.out.println( "IS field is '" + isField.getFieldId() + "'" );
                    Field protoInstanceField = pi.getField( fieldId );
                    if ( protoInstanceField == null ) {
                        System.out.println( "Look for it in proto interface.." );
                        Field f = pb.getInterfaceDeclaration( isField.getFieldId() );
                        if ( f != null ) {
                            System.out.println("ok, it was found..." );
                            boolean foundit = false;
                            for ( int k = f.getFirstTokenOffset(); k <= f.getLastTokenOffset(); k++ ) {
                                String ff = dataSource.toString( k );
                                if ( ff.compareTo( fieldId ) == 0 ) {
                                    System.out.println("found fieldId" );
                                    foundit = true;
                                } else if ( foundit ) {
                                    System.out.println(".."+dataSource.toString(k));
                                    tp.print( dataSource.toString( k ));
                                }
                            }
                           // System.out.println("got to end of the fucker.."); // cough
                        }
                    } else {
                        System.out.println("OK, it is a proto instance field... whatever that is..");
                        for ( int k = protoInstanceField.getFirstTokenOffset() + 1; k <= protoInstanceField.getLastTokenOffset(); k++ ) {
                            tp.print( dataSource.toString( k ));
                        }
                    }
                    tp.flush();
                    foundIs = true;
                    break;
                }
            }
            if ( foundIs ) {
                i++;
                continue;
            }
            tp.print( dataSource, i );
        }
        }
    }
    
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    System.out.println( "replacing " + startTokenOffset + " to " + endTokenOffset + ", param is " + param );
	    if ( param instanceof ProtoExpansionParams ) {
	        ProtoExpansionParams pep = (ProtoExpansionParams)param;
	        PROTObase pb = pep.getPROTObase();
	        PROTOInstance pi = pep.getPROTOInstance();
	        Scene s = pb.getPROTObody();
	        if ( s != null ) {
	            System.out.println( "PROTO has " + s.numberChildren() + " children" );
	            if ( s.numberChildren() == 1 ) {
	                Node n = pb.getPROTONodeType();
	                if ( n != null ) {
	                    expandProtoInstance( pi, pb, s, tp );
	                }
	            } else {
	                System.out.println( "OK, so there are more children... what happens..." );
	                Node n = pb.getPROTONodeType();
	                if ( n != null ) {
	                    expandProtoInstance( pi, pb, s, tp );
	                }
	            }
	        }
	    }
	}
	
    /** summary used by command line version */
	public void summarize( PrintStream ps ) {
	    System.out.println( protoRemoveCount + " PROTOs removed" );
	}
}


