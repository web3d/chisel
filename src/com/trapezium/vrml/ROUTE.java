/*
 * @(#)ROUTE.java
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
import com.trapezium.vrml.grammar.VRML97;

/**
 *  Scene graph component representing a ROUTE.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 17 Dec 1997
 *
 *  @since           1.0
 */
public class ROUTE extends MultipleTokenElement {
    /** ROUTE constructor */
	public ROUTE( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		v.breakLineAt( tokenOffset );
	}

    /** Get the ROUTE source object field name */
	public String getSourceFieldName() {
	    return( getChildFieldName( 0 ));
	}

    /** Get the ROUTE destination object field name */
	public String getDestFieldName() {
	    return( getChildFieldName( 2 ));
	}

    /** Get the ROUTE source object DEF name */
	public String getSourceDEFname() {
	    return( getChildNodeName( 0 ));
	}

    /** Set the ROUTE source object DEF name */
	public void setSourceDEFname( String newName ) {
	    setChildNodeName( 0, newName );
	}

    /** Get the ROUTE destination object DEF name */
	public String getDestDEFname() {
	    return( getChildNodeName( 2 ));
	}
	
	/** Set the ROUTE dest object DEF name */
	public void setDestDEFname( String newName ) {
	    setChildNodeName( 2, newName );
	}

    /** Get the DEF name of a ROUTE node */
	String getChildNodeName( int offset ) {
	    RouteElement re = (RouteElement)getChildAt( offset );
	    if ( re != null ) {
	        return( re.getNodeName() );
	    } else {
	        return( null );
	    }
	}

    /** Set the DEF name of a particular ROUTE child */
	void setChildNodeName( int offset, String newName ) {
	    RouteElement re = (RouteElement)getChildAt( offset );
	    if ( re != null ) {
	        re.setNodeName( newName );
	    }
	}

	/** Get the name of a ROUTE field */
	String getChildFieldName( int offset ) {
	    RouteElement re = (RouteElement)getChildAt( offset );
	    if ( re != null ) {
	        return( re.getFieldName() );
	    } else {
	        return( null );
	    }
	}

    /** Get a particular RouteElement */
    public RouteElement getRouteElement( int offset ) {
        int counter = 0;
        for ( int i = 0; i < numberChildren(); i++ ) {
            VrmlElement e = getChildAt( i );
            if ( e instanceof RouteElement ) {
                if ( counter == offset ) {
                    return( (RouteElement)e );
                } else {
                    counter++;
                }
            }
        }
        return( null );
    }
    
	public RouteDestination getRouteDestination() {
		for ( int i = 0; i < numberChildren(); i++ ) {
			VrmlElement e = getChildAt( i );
			if ( e instanceof RouteDestination ) {
				RouteDestination rd = (RouteDestination)e;
				return( rd );
			}
		}
		return( null );
	}

    /** Verify that the data types of the ROUTE source and destination match */
	public void checkTypes() {
		RouteElement source = (RouteElement)getChildAt( 0 );
		RouteElement dest = (RouteElement)getChildAt( 2 );
		if (( source != null ) && ( dest != null )) {
			String sourceType = source.getFieldType();
			String destType = dest.getFieldType();
			sourceType = VRML97.convertToVRML97( sourceType );
			destType = VRML97.convertToVRML97( destType );
			if (( sourceType != null ) && ( destType != null )) {
				if ( sourceType.compareTo( destType ) != 0 ) {
					setError( "type mismatch, routing " + sourceType + " to " + destType );
				}
			}
		}
	}
}
