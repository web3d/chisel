/*
 * @(#)DEFUSENode.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.node;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.fields.FieldValue;
import com.trapezium.vrml.fields.Field;
import com.trapezium.vrml.VrmlElementNotFoundException;
import com.trapezium.vrml.visitor.DEFrenameVisitor;

/**
 *  Scene graph component for both a DEF and a USE node.
 *
 *  Both are in one object to simplify changing one to the other when
 *  VRML is re-arranged.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 21 Jan 1998
 *
 *  @since           1.0
 */
public class DEFUSENode extends Node {

	/** is this a DEF node */
	boolean isDEFNode;

	/** the name */
	String defuseName;

	/** polygon count, optimization to avoid recounting on USE
	 *  (not done, need to set during DEF node traversal, need to
	 *  get bounds on traverse of DEF node, probably can do with visitLevel,
	 *  get count at DEF level, get count next time that level is encountered,
	 *  diff is polygon count for DEF node, but then problem is nested DEF,
	 *  , get on
	 *  USE node and return FALSE on USE node traversal in ComplexityVisitor
	 */
	int polygonCount;

	public void setPolygonCount( int n ) {
	    polygonCount = n;
	}

	public int getPolygonCount() {
	    return( polygonCount );
	}

	/** DEF nodes are always traversable, USE nodes are traversed only if they have errors */
	public boolean isTraversable() {
	    if ( !isDEFNode ) {
	        return( getError() != null );
	    } else {
	        return( true );
	    }
	}

    /** is this DEF node used? */
	boolean defIsUsed = false;

    /** mark this DEF as one that has a valid USE reference */
	public void markUsed() {
		defIsUsed = true;
		Node n = getNode();
		if ( n != null ) {
		    String e = n.getError();
		    if ( e != null ) {
    		    if ( e.indexOf( "unused" ) > 0 ) {
    		        n.setError( null );
    		    }
    		}
		}
	}

    /** is this DEF USEd somewhere else in the file? */
	public boolean isUsed() {
		return( defIsUsed );
	}

    /** is this DEF used by a ROUTE? */
    boolean defIsUsedByROUTE = false;

    /** De-register self from scene */
    public void deregisterSelf() {
        Scene s = (Scene)getScene();
        if ( s != null ) {
            s.deregisterDEF( this );
        }
    }

	/** is this DEF USed by a ROUTE? */
	public boolean isUsedByROUTE() {
	    return( defIsUsedByROUTE );
	}

	/** indicate this DEF is USEd by a ROUTE */
	public void markUsedByROUTE() {
	    defIsUsedByROUTE = true;
	}

	/** constant indicating node is DEF */
	static public final int DEF = 1;
	/** constant indicating node is USE */
	static public final int USE = 2;

	/** class constructor, create the DEF or USE node */
	public DEFUSENode( int tokenOffset, TokenEnumerator v, int type ) {
		super( tokenOffset );
		tokenOffset = v.getNextToken();
		defuseName = v.toString( tokenOffset );
		if ( type == DEF ) {
			isDEFNode = true;
		} else {
			isDEFNode = false;
		}
	}

    /** overrides Node version, just passes to Node if this is a DEF node */
    public Node addChildNode( Node sourceNode ) throws InvalidChildNodeException, NotAGroupingNodeException, InvalidFieldException {
        if ( isDEFNode ) {
            Node node = getNode();
            if ( node != null ) {
                return( node.addChildNode( sourceNode ));
            }
        }
        throw new NotAGroupingNodeException();
    }

    /** overrides Node version, delegates call to DEF */
    public void removeChildNode( Node nodeToRemove ) throws VrmlElementNotFoundException {
        if ( isDEFNode ) {
            Node node = getNode();
            if ( node != null ) {
                node.removeChildNode( nodeToRemove );
                return;
            }
        }
        throw new VrmlElementNotFoundException();
    }

    /** overrides Node version, just passes call to Node if this is a DEF node */
    public Field setField( TokenData td ) throws InvalidFieldException, InvalidNodeException {
        if ( isDEFNode ) {
            Node node = getNode();
            if ( node != null ) {
                return( node.setField( td ));
            }
        }
        throw new InvalidNodeException();
    }

    /** overrides Node version, just passes call to Node if this is a DEF node */
    public Field setField( Field f ) throws InvalidFieldException, InvalidNodeException {
        if ( isDEFNode ) {
            Node node = getNode();
            if ( node != null ) {
                return( node.setField( f ));
            }
        }
        throw new InvalidNodeException();
    }

	/** Is this a DEF node? */
	public boolean isDEF() {
		return( isDEFNode );
	}

	/** Is this a USE node? */
	public boolean isUSE() {
	    return( !isDEFNode );
	}

	/** Overrides Node template method */
	public boolean isDEForUSE() {
	    return( true );
	}

    /** Forwards request for a FieldValue to the referenced node. */
	public FieldValue getFieldValue( String fieldName ) {
		Node n = getNode();
		if ( n != null ) {
			return( n.getFieldValue( fieldName ));
		}
		return( null );
	}

    /** Forwards request for a Field to the referenced node. */
	public Field getField( String fieldName ) {
		Node n = getNode();
		if ( n != null ) {
			return( n.getField( fieldName ));
		}
		return( null );
	}

	/** Get string identifying DEFUSE */
	public String getId() {
	    return( defuseName );
	}

    /** Rename the DEF within the Scene */
    public void setId( String newDEFname ) {
        Scene myScene = (Scene)getScene();
        if ( myScene != null ) {
            TokenEnumerator sceneTokenEnumerator = myScene.getTokenEnumerator();
            if ( sceneTokenEnumerator != null ) {
                myScene.deregisterDEF( this );
                DEFrenameVisitor drv = new DEFrenameVisitor( sceneTokenEnumerator, defuseName, newDEFname );
                myScene.traverse( drv );
            }
            defuseName = newDEFname;
            myScene.registerDEF( this );
        }
    }

    /** Rename Id just within this DEFUSENode */
    public void resetId( String newDEFname ) {
        defuseName = newDEFname;
    }

    /** Get the name assigned to the node, or the name referenced by USE */
	public String getDEFName() {
		return( defuseName );
	}

	/** Set the DEF name, this does not adjust subsequent USE nodes */
	public void setDEFName( String newDEFName ) {
	    defuseName = newDEFName;
        VrmlElement root = getRoot();
        if ( root instanceof Scene ) {
            Scene sroot = (Scene)root;
            TokenEnumerator te = sroot.getTokenEnumerator();
            int offset = getFirstTokenOffset();
            offset = te.getNextToken( offset );
            te.replace( offset, newDEFName );
        }
    }


	/** Get the actual Node associated with the DEF or USE.
     *
     *  @return  the Node associated with the DEF or USE.  This
     *     may be null in the case where a USE refers to a String
     *     that has no corresponding DEf.
     */
	public Node getNode() {
	    int nChildren = numberChildren();
		for ( int i = 0; i < nChildren; i++ ) {
			Object a = getChildAt( i );
			if ( a instanceof Node ) {
				Node n = (Node)a;
				return( n );
			}
		}
		return( null );
	}

    /** Get the name of the node associated with this DEF/USE node */
    public String getNodeName() {
        Node n = getNode();
        if ( n != null ) {
            return( n.getNodeName() );
        } else {
            return( null );
        }
    }
}
