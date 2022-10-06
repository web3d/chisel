/*
 * @(#)BadFieldId.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml;

/**
 *  Scene graph component representing an unknown field id.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.0, 15 Dec 1997
 *
 *  @since           1.0
 */
public class BadFieldId extends SingleTokenElement {
    /** Create object associated with a token
     *
     *  @param  tokenOffset  token associated with field
     */
	public BadFieldId( int tokenOffset ) {
		super( tokenOffset );
	}
	
	/** Create object associated with a token, and associated error.
	 *
	 *  @param  tokenOffset  token associated with field
	 *  @param  errorStr     description of error
	 */
	public BadFieldId( int tokenOffset, String errorStr ) {
	    super( tokenOffset );
	    setError( errorStr );
	}
}

