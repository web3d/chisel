/*
 * @(#)RouteDestination.java
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
 *  Scene graph component representing a ROUTE destination.
 *
 *  The ROUTE destination is the "<defname>.<fieldname>" appearing after "TO".
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 17 Dec 1997
 *
 *  @since           1.0
 */
public class RouteDestination extends RouteElement {
	public RouteDestination( int tokenOffset, TokenEnumerator v, Scene scene ) {
		super( tokenOffset, v, scene );
	}

	public boolean isDest() {
		return( true );
	}

	public void noFieldError( String originalFieldName ) {
		setError( "no eventIn '" + originalFieldName + "'" );
	}
	
	public String getOptionalPrefix() {
	    return( "set_" );
	}
	
	public String getOptionalSuffix() {
	    return( null );
	}
	
	public void setInterfaceError() {
	    setError( "destination must be eventIn" );
	}
	
	public int getExpectedInterfaceType() {
	    return( VRML97.eventIn );
	}
}
