/*
 * @(#)MFFieldValue.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

import com.trapezium.parse.TokenEnumerator;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.visitor.ChildCounter;
import com.trapezium.vrml.LeftBracket;
import com.trapezium.vrml.RightBracket;
import com.trapezium.vrml.node.NULLNode;
import com.trapezium.vrml.grammar.NodeStatementRule;

/**
 *  MFFieldValue is the base class for all multiple value fields, which have the
 *  following syntax:
 *
 *  1. an optional "["
 *  2. zero or more SFFieldValues of the specified type
 *  3. an optional "]"
 *
 *  The individual fields are added as children of this element.  Individual fields are
 *  created by the "subclassFactory" template method.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
abstract public class MFFieldValue extends FieldValue {

	protected int optimizedValueCount = 0;
	boolean indexed = true;
	public void setIndexed( boolean ival ) {
	    indexed = ival;
	}
	public boolean isIndexed() {
	    return( indexed );
	}

	/** Get the number of values in this list of values */
	public int getRawValueCount() {
		return( optimizedValueCount );
	}

	/** Template method, is the sequence of values non-decreasing? */
	public boolean isNonDecreasing() {
		return( true );
	}

	/** Template method, is the sequence of values increasing? */
	public boolean isIncreasing() {
		return( true );
	}

	public MFFieldValue() {
	    super( -1 );
	}
	
	public MFFieldValue( int tokenOffset ) {
		super( tokenOffset );
	}

    public void init( int tokenOffset, TokenEnumerator v, Scene scene )  {
        init( null, tokenOffset, v, scene );
    }
    
	public void init( NodeStatementRule nodeStatementRule, int tokenOffset, TokenEnumerator v, Scene scene ) {
		Build( nodeStatementRule, tokenOffset, v, scene, this );
	}
	
	/** Build an MFFieldValue.  By the convention I've been following, this really should
	 *  be in the "grammar" package.  Its still here because the FieldValue objects don't
	 *  do much, and there doesn't seem to be a reason to break these tiny classes into two 
	 *  separate ones.
	 */	
	public void Build( NodeStatementRule nodeStatementRule, int tokenOffset, TokenEnumerator v, Scene scene, MFFieldValue parent ) {
		boolean hasLeftBracket = false;
		if ( v.isLeftBracket( tokenOffset )) {
			tokenOffset = v.getNextToken();
			hasLeftBracket = true;
		}
		while (( tokenOffset != -1 ) && !v.isRightBracket( tokenOffset ) && !v.isRightBrace( tokenOffset )) {
			// In some cases the "subclassFactory" itself adds the child to the parent, by
			// using the static "Build" method.  In these cases, the "subclassFactory" returns
			// null
			VrmlElement child = parent.subclassFactory( nodeStatementRule, tokenOffset, v, scene );
			if ( child != null ) {
				parent.addChild( child );
				try {
    				child.setLastTokenOffset( v.getCurrentTokenOffset() );
    			} catch ( Exception e ) {
    			}
			}

			// if there is no left bracket, there is only a single value
			if ( !hasLeftBracket ) {
				break;
			}
			tokenOffset = v.getNextToken();
			while (( tokenOffset != -1 ) && ( v.isContinuationString( tokenOffset ))) {
				tokenOffset = v.getNextToken();
			}
		}
		if ( hasLeftBracket ) {
		    if ( !RightBracket.isValid( tokenOffset, v )) {
    			parent.addChild( new RightBracket( tokenOffset, v ));
    		}
		}
		setLastTokenOffset( tokenOffset );
	}

	/** Get the number of values in this field? */
	public int numberValues() {
		ChildCounter cc = new ChildCounter( null );
		traverse( cc );
		return( cc.getChildCount() );
	}

	/** does a particular offset value exist? */
	public boolean hasValue( int offset ) {
		return( offset < numberValues() );
	}

	/** Get a Value at a particular offset */
	public FieldValue getFieldValueAt( int offset ) {
		int offsetCounter = -1;
		int nChildren = numberChildren();
		for ( int i = 0; i < nChildren; i++ ) {
			VrmlElement child = getChildAt( i );
			if ( child instanceof FieldValue ) {
				offsetCounter++;
				if ( offsetCounter == offset ) {
					FieldValue fv = (FieldValue)child;
					return( fv );
				}
			}
		}
		return( null );
	}

    /** Overridden by nodes which need to parse other data types */
	public VrmlElement subclassFactory( NodeStatementRule nodeStatementRule, int tokenOffset, TokenEnumerator v, Scene scene ) {
	    return( subclassFactory( tokenOffset, v, scene ));
	}
	
	/** Template method creates a single value element, specific MF types define this method */
	abstract public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene );
}

