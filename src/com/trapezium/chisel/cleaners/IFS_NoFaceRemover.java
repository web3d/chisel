/*
 * @(#)IFS_NoFaceRemover.java
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
import com.trapezium.vrml.Value;
import com.trapezium.vrml.node.Node;
import com.trapezium.vrml.node.DEFUSENode;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.MFNodeValue;
import com.trapezium.vrml.fields.MFFieldValue;
import com.trapezium.vrml.fields.SFNodeValue;
import com.trapezium.vrml.visitor.ISLocator;
import com.trapezium.chisel.*;
import java.util.Vector;
import java.util.Hashtable;
import java.io.PrintStream;

/**
 *  This removes IndexedFaceSets that have no faces,
 *  unless the IFS has a DEF that is used.
 */
public class IFS_NoFaceRemover extends Optimizer {
    // number of nodes removed
    int removedNodeCount = 0;
    
    // list of useless nodes
    Vector uselessNodeList;
    
    // nodes that have been redefined
    Hashtable redeffedNodes;
    
    ISLocator isLocator;

	public IFS_NoFaceRemover() {
		super( "IndexedFaceSet", "Removing IndexedFaceSets with no faces..." );
		isLocator = new ISLocator();
		reset();
	}
	
	public void reset() {
		uselessNodeList = new Vector();
		redeffedNodes = new Hashtable();
	}

 	public void attemptOptimization( Node n ) {
 	    // do nothing if this has any IS fields
 	    isLocator.reset();
 	    n.traverse( isLocator );
 	    if ( isLocator.foundISField() ) {
 	        return;
 	    }
 	    // if any of the IFS fields used by a ROUTE, do nothing
 	    if ( n.isFieldUsedByROUTE( "coord" )) {
 	        return;
 	    }
 	    if ( n.isFieldUsedByROUTE( "color" )) {
 	        return;
 	    }
 	    if ( n.isFieldUsedByROUTE( "normal" )) {
 	        return;
 	    }
 	    if ( n.isFieldUsedByROUTE( "texCoord" )) {
 	        return;
 	    }
	    Field coordIndex = n.getField( "coordIndex" );
		MFFieldValue coordIndexValue = null;
		int coordIndexCount = 0;
		if ( coordIndex != null ) {
		    coordIndexValue =(MFFieldValue)coordIndex.getFieldValue();
		    coordIndexCount = coordIndexValue.getRawValueCount();
		}

        // check for Shape
        Node shape = null;
        Node originaln = n;
        VrmlElement p = n.getParent();
        if ( p != null ) {
            p = p.getParent();
        }
        if ( p != null ) {
            p = p.getParent();
            if ( p instanceof Node ) {
                Node pn = (Node)p;
                if ( pn.getBaseName().compareTo( "Shape" ) == 0 ) {
                    shape = pn;
                    p = pn.getParent();
                    if ( p instanceof DEFUSENode ) {
                        n = (Node)p;
                    } else {
   	                    n = pn;
   	                }
                }
            }
        }
 	    if ( coordIndexCount == 0 ) {
 	        // we are actually removing the Shape node, unless it is the only node
 	        // in the "children" field, in which case we remove the "children" field.
 	        uselessNodeList.addElement( n );
 	        VrmlElement nodeParent = n.getParent();
 	        VrmlElement mfnField = null;
 	        boolean didReplace = false;
 	        if ( nodeParent instanceof MFNodeValue ) {
 	            MFNodeValue mfnv = (MFNodeValue)nodeParent;
 	            mfnField = mfnv.getParent();
 	            if (( mfnField != null ) && ( mfnv.getRawValueCount() == 1 )) {
 	                replaceRange( mfnField.getFirstTokenOffset(), mfnField.getLastTokenOffset(), null );
 	                didReplace = true;
 	            }
 	        }
 	        if ( !didReplace ) {
     	        replaceRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), null );
     	    }
		} else {
		    // check if any of the IFS node fields are USEing a DEF in
		    // an IFS that is being removed
		    checkUSE( originaln.getField( "coord" ));
		    checkUSE( originaln.getField( "texCoord" ));
		    checkUSE( originaln.getField( "color" ));
		    checkUSE( originaln.getField( "normal" ));

		    // check if any of the Shape fields are USEing a DEF in a 
		    // Shape that is being removed
		    if ( shape != null ) {
		        checkUSE( shape.getField( "appearance" ));
		        Field appearance = shape.getField( "appearance" );
		        if ( appearance != null ) {
		            Node appearanceNode = appearance.getNodeValue();
		            if ( appearanceNode != null ) {
		                checkUSE( appearanceNode.getField( "material" ));
		            }
		        }
		    }
		}
	}

    /** Check if a Node field is a USE node, and is USEing a DEF being removed.
     *  If it is, then that USE node gets converted into the removed DEF.
     */
    void checkUSE( Field f ) {
        if ( f != null ) {
            FieldValue fv = f.getFieldValue();
            if ( fv instanceof SFNodeValue ) {
                Node n = ((SFNodeValue)fv).getNode();
                if ( n instanceof DEFUSENode ) {
                    DEFUSENode dun = (DEFUSENode)n;
                    if ( !dun.isDEF() ) {
                        // check if referenced DEF is within remove area
                        Node defNode = dun.getNode();
                        checkReplace( dun, defNode );
                    }
                }
            }
        }
    }
    
    /** Replace a USE node with the corresponding DEF on first USE encountered,
     *  but only do this if the USE node is within an area that is being
     *  removed.
     *
     *  @param dun the USE node to check
     *  @param n the definition of the node
     */
    void checkReplace( DEFUSENode dun, Node n ) {
        // if the definition of the node has already been reDEFed, do nothing
        if ( redeffedNodes.get( n ) != null ) {
            return;
        }
        int numberUselessNodes = uselessNodeList.size();
        int first = n.getFirstTokenOffset();
        int last = n.getLastTokenOffset();
        for ( int i = 0; i < numberUselessNodes; i++ ) {
            Node test = (Node)uselessNodeList.elementAt( i );
            if (( first >= test.getFirstTokenOffset() ) && ( last <= test.getLastTokenOffset() )) {
                replaceRange( dun.getFirstTokenOffset(), dun.getLastTokenOffset(), n );
                redeffedNodes.put( n, n );
                return;
            }
        }
    }
            

    /** Two possibilities here, if the param is null, we are removing the VrmlElement,
     *  so we just count the removal and do nothing.  If the param is a Node, it means
     *  that we have previously removed the DEF for a USE node, so the USE node is
     *  transformed into the original DEF.
     */
	public void optimize( TokenPrinter tp, Object param, int startTokenOffset, int endTokenOffset ) {
	    if ( param == null ) {
            removedNodeCount++;
        } else {
            tp.print( "DEF" );
            tp.print( dataSource, startTokenOffset+1 );
            Node n = (Node)param;
            tp.printRange( n.getFirstTokenOffset(), n.getLastTokenOffset(), false );
        }
	}

	public void summarize( PrintStream ps ) {
		if ( removedNodeCount == 0 ) {
			ps.println( "Removed no IFSes." );
		} else if ( removedNodeCount == 1 ) {
			ps.println( "Removed 1 IFS." );
		} else {
			ps.println( "Removed " + removedNodeCount + " IFSes." );
		}
	}
}
