/*
 * @(#)MFVec2fValue.java
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
import com.trapezium.vrml.VrmlElement;
import com.trapezium.vrml.Scene;
import com.trapezium.vrml.grammar.Table7;

/**
 *  Scene graph component for sequence of Vec2f values.
 *
 *  Value objects are not added to scene graph, unless they contain errors.
 *  
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 25 Feb 1998, added base profile warning
 *  @version         1.1, 15 Jan 1998
 *
 *  @since           1.0
 */
public class MFVec2fValue extends MFFieldValue {
	public MFVec2fValue() {
		super();
	}

	public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
	    if ( !SFVec2fValue.valid( tokenOffset, v )) {
	        SFVec2fValue sfvec = new SFVec2fValue( tokenOffset, v );
	        optimizedValueCount += sfvec.numberValidChildren();
	        return( sfvec );
	    } else {
	        optimizedValueCount += 2;
	        if (( optimizedValueCount/2 ) == ( Table7.MFVec2fLimit + 1 )) {
	            v.setState( tokenOffset );
	            SFVec2fValue sfvec = new SFVec2fValue( tokenOffset, v );
	            sfvec.setError( "Nonconformance, base profile limit of " + Table7.MFVec2fLimit + " exceeded here" );
	            return( sfvec );
	        }
	        return( null );
	    }
	}
}


