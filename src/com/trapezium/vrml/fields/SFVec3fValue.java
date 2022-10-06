/*
 * @(#)SFVec3fValue.java
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
import com.trapezium.vrml.Value;
import com.trapezium.vrml.VrmlElement;

/**
 *  Scene graph component for an SFVec3f field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 8 Dec 1997
 *
 *  @since           1.0
 */
public class SFVec3fValue extends SFFieldValue implements ValueTypes {
	public SFVec3fValue() {
		super();
	}

	public SFVec3fValue( int tokenOffset, TokenEnumerator v ) {
		super( tokenOffset );
		init( tokenOffset, v, null );
	}

	int validChildCount = 0;
	public int numberValidChildren() {
		return( validChildCount );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
	    initByType( tokenOffset, v, AllValues );
	}
	
	public void initByType( int tokenOffset, TokenEnumerator v, int valueType ) {
	    int t1 = tokenOffset;
		int v1 = SFFloatValue.validateAndAdd( this, tokenOffset, v, valueType );
		if ( getChildAt( 0 ) == null ) {
			validChildCount++;
		} else if ( !v.isNumber( tokenOffset )) {
			return;
		}
		int state = v.getState();
		tokenOffset = v.getNextToken();
		int t2 = tokenOffset;
		int v2 = SFFloatValue.validateAndAdd( this, tokenOffset, v, valueType );
		VrmlElement vcheck = getChildAt( 1 - validChildCount );
		if ( vcheck == null ) {
			validChildCount++;
		} else if ( bracketError( vcheck, v ) || !v.isNumber( tokenOffset )) {
			v.setState( state );
			return;
		}
		state = v.getState();
		tokenOffset = v.getNextToken();
		int t3 = tokenOffset;
		int v3 = SFFloatValue.validateAndAdd( this, tokenOffset, v, valueType );
		vcheck = getChildAt( 2 - validChildCount );
		if ( vcheck == null ) {
			validChildCount++;
		} else if ( bracketError( vcheck, v ) || !v.isNumber( tokenOffset )) {
			v.setState( state );
		}
		if ( valueType == BboxSizeValues ) {
		    if (( v1 == NegativeOne ) || ( v2 == NegativeOne ) || ( v3 == NegativeOne )) {
		        if ( v1 != NegativeOne ) {
		            Value vv = new Value( t1 );
		            vv.setError( "all sizes must be -1, or positive" );
		            addChild( vv );
		        }
		        if ( v2 != NegativeOne ) {
		            Value vv = new Value( t2 );
		            vv.setError( "all sizes must be -1, or positive" );
		            addChild( vv );
		        }
		        if ( v3 != NegativeOne ) {
		            Value vv = new Value( t3 );
		            vv.setError( "all sizes must be -1, or positive" );
		            addChild( vv );
		        }
		    }
		}
    }

	static public boolean valid( int tokenOffset, TokenEnumerator v ) {
		if ( !SFFloatValue.valid( tokenOffset, v )) {
			return( false );
		}
		int state = v.getState();
		if ( !SFFloatValue.valid( v.getNextToken(), v )) {
			v.setState( state );
			return( false );
		}
		if ( !SFFloatValue.valid( v.getNextToken(), v )) {
			v.setState( state );
			return( false );
		}
		return( true );
	}
}
