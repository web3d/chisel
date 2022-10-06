/*
 * @(#)SFBoolValue.java
 *
 * Copyright (c) 1998 by Trapezium Development LLC.  All Rights Reserved.
 *
 * The information in this file is the property of Trapezium Development LLC
 * and may be used only in accordance with the terms of the license granted
 * by Trapezium.
 *
 */
package com.trapezium.vrml.fields;

import com.trapezium.vrml.Scene;
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.grammar.Spelling;
import com.trapezium.parse.TokenEnumerator;

/**
 *  Scene graph component for an SFBool field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 10 Dec 1997
 *
 *  @since           1.0
 */
public class SFBoolValue extends SFFieldValue {
	static final String TrueString = "TRUE";
	static final String FalseString = "FALSE";
	boolean bValue;

	public SFBoolValue() {
		super();
	}
	
	public SFBoolValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( v.sameAs( tokenOffset, TrueString )) {
		    bValue = true;
		} else if ( !v.sameAs( tokenOffset, FalseString )) {
    		VrmlElement value = new Value( tokenOffset );
	    	addChild( value );
		    String tokenString = v.toString( tokenOffset );
			int trueScore = Spelling.getMatchScore( TrueString, tokenString );
			int falseScore = Spelling.getMatchScore( FalseString, tokenString );
			if ( trueScore > falseScore ) {
				value.setError( "TRUE or FALSE expected here, possibly 'TRUE'" );
			} else if ( falseScore > trueScore ) {
				value.setError( "TRUE or FALSE expected here, possibly 'FALSE'" );
			} else {
				value.setError( "TRUE or FALSE expected here" );
			}
		}
	}

	public boolean getValue() {
		return( bValue );
	}
}
