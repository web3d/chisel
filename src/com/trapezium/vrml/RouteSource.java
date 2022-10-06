/*
 * @(#)RouteSource.java
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
 *  Scene graph component representing a ROUTE source.
 *
 *  The ROUTE source is the "<defname>.<fieldname>" appearing before "TO".
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 17 Dec 1997
 *
 *  @since           1.0
 */
public class RouteSource extends RouteElement {
	public RouteSource( int tokenOffset, TokenEnumerator v, Scene scene ) {
		super( tokenOffset, v, scene );
	}

	public void noFieldError( String originalFieldName ) {
		setError( "no eventOut '" + originalFieldName + "'" );
	}
	
	public String getOptionalPrefix() {
	    return( null );
	}
	
	public String getOptionalSuffix() {
	    return( "_changed" );
	}
	
	public void setInterfaceError() {
	    setError( "source must be eventOut" );
	}

	public int getExpectedInterfaceType() {
	    return( VRML97.eventOut );
	}
}
