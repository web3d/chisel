/*
 * @(#)SingleTokenElement.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;

import com.trapezium.parse.TokenEnumerator;

/**
 *  Base class for scene graph component made up of a single token.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 29 March 1998, added "adjust" method
 *  @version         1.11, 13 Jan 1998
 *
 *  @since           1.0
 */
public class SingleTokenElement extends VrmlElement {
    int firstTokenOffset;
    
	public SingleTokenElement( int tokenOffset ) {
	    super();
	    firstTokenOffset = tokenOffset;
	}

	public SingleTokenElement( TokenEnumerator v ) {
		this( v.getNextToken() );
	}

    public int getFirstTokenOffset() {
        return( firstTokenOffset );
    }
    
    public void setFirstTokenOffset( int firstTokenOffset ) {
        this.firstTokenOffset = firstTokenOffset;
    }
    
    public int getLastTokenOffset() {
        return( firstTokenOffset );
    }
    
    public void setLastTokenOffset( int lastTokenOffset ) throws FunctionCallException {
        throw new FunctionCallException();
    }
    
    /** Adjust the token offset if greater than or equal to boundary */
    public void adjust( int boundary, int amount ) {
        if ( firstTokenOffset >= boundary ) {
            firstTokenOffset += amount;
        }
    }

    /** Template method, subclasses may be traversible.  Used by Visitor subclasses
     *  to determine whether or not element can be traversed by Visitor.
     */
    public boolean isTraversable() {
        return( false );
    }
    
    /** Get the name of the element */
	public String getName() {
	    int tokenOffset = getFirstTokenOffset();
	    if ( tokenOffset == -1 ) {
	        return( null );
	    }
	    Scene s = (Scene)getScene();
	    TokenEnumerator v = s.getTokenEnumerator();
		return( v.toString( tokenOffset ));
	}
}

