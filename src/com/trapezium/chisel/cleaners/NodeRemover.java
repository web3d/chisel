/*
 * @(#)NodeRemover.java
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
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.node.PROTOInstance;
import com.trapezium.vrml.node.ScriptInstance;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.ISField;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.visitor.ISLocator;
import com.trapezium.chisel.*;

import java.util.Vector;


/**
 *  This removes useless nodes:
 *     Interpolators with zero or one key, IS assignments are unaffected
 *
 *  Any ROUTEs related to the removed nodes are also removed.
 *  If a DEF node field in a removed segment is USEd, then the
 *  first USE is converted into a DEF.
 */
public class NodeRemover extends Optimizer {
    // number of nodes removed
    int removedNodeCount = 0;

    // list of useless nodes
    Vector uselessNodeList;
    
    // TimeSensors untouched if they have IS fields
    ISLocator isLocator;

	public NodeRemover() {
		super( "Interpolator", "Removing useless interpolators and TimeSensors..." );
		addAdditionalNode( "TimeSensor" );
		reset();
		isLocator = new ISLocator();
	}
	
	public void reset() {
		uselessNodeList = new Vector();
	}

    public boolean isROUTElistener() {
        return( true );
    }
    
 	public void attemptOptimization( Node n ) {
   	    VrmlElement parent = n.getParent();
 	    if ( n.getBaseName().indexOf( "Interpolator" ) > 0 ) {
     	    if ( parent instanceof DEFUSENode ) {
     	        n = (Node)parent;
     	    }
    	    Field key = n.getField( "key" );
    	    if ( key == null ) {
    	        return;
    	    } else if ( key.isISfield() ) {
    	        return;
    	    }
    	    Field keyValue = n.getField( "keyValue" );
    	    if ( keyValue.isISfield() ) {
    	        return;
    	    }
    	    boolean replaceNode = true;
    	    if ( parent instanceof DEFUSENode ) {
    	        DEFUSENode dun = (DEFUSENode)parent;
    	        n = (Node)parent;
    	        replaceNode = !dun.isUsed();
    	        parent = n.getParent();
    	    }
    		MFFieldValue keyFieldValue = (MFFieldValue)key.getFieldValue();
     	    if ( replaceNode && ( keyFieldValue != null ) && ( keyFieldValue.getRawValueCount() < 2 )) {
     	        uselessNodeList.addElement( n );
     	        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), null );
    		}
    	} else {
    	    isLocator.reset();
    	    n.traverse( isLocator );
    	    boolean replaceNode = !isLocator.foundISField();
    	    if ( parent instanceof SFNodeValue ) {
    	        VrmlElement ve = parent.getParent();
    	        if ( ve != null ) {
          	        ve = ve.getParent();
          	        if ( ve != null ) {
          	            ve = ve.getParent();
          	            if ( ve instanceof ScriptInstance ) {
          	                replaceNode = false;
          	            }
          	        }
          	    }
    	    }
    	    if ( parent instanceof DEFUSENode ) {
    	        DEFUSENode dun = (DEFUSENode)parent;
    	        n = (Node)parent;
    	        replaceNode = !dun.isUsed();
    	        parent = n.getParent();
    	    }
    	    if ( parent instanceof MFFieldValue ) {
    	        MFFieldValue mfv = (MFFieldValue)parent;
    	        if ( mfv.getRawValueCount() == 1 ) {
    	            parent = mfv.getParent();
    	            if ( replaceNode && parent != null ) {
    	                replaceRange( parent.getFirstTokenOffset(), parent.getLastTokenOffset(), null );
    	                replaceNode = false;
    	            }
    	        }
    	    }
    	    if ( replaceNode ) {
    	        System.out.println( "replacing node" );
    	        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), null );
    	    }
    	}
	}
	
	public void attemptOptimization( ROUTE route ) {
        int numberUselessNodes = uselessNodeList.size();
        String sourceObject = route.getSourceDEFname();
        String destObject = route.getDestDEFname();
        for ( int i = 0; i < numberUselessNodes; i++ ) {
            Node test = (Node)uselessNodeList.elementAt( i );
            if ( test instanceof DEFUSENode ) {
                DEFUSENode dun = (DEFUSENode)test;
                String nodeName = dun.getDEFName();
                if ( sourceObject != null ) {
                    if ( sourceObject.compareTo( nodeName ) == 0 ) {
                        replaceRange( route.getFirstTokenOffset(), route.getLastTokenOffset(), null );
                        return;
                    }
                }
                if ( destObject != null ) {
                    if ( destObject.compareTo( nodeName ) == 0 ) {
                        replaceRange( route.getFirstTokenOffset(), route.getLastTokenOffset(), null );
                        return;
                    }
                }
            }
        }
    }

	// Do nothing, entire node or ROUTE is removed
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	}
}
