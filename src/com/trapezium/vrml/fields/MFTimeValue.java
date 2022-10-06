/*
 * @(#)MFTimeValue.java
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
 *  Scene graph component for sequence of SFTime values.
 *
 *  Value objects are not added to scene graph, unless they contain errors.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.12, 25 Jan 1998, created, MFTime allowed in Script nodes
 *
 *  @since           1.12
 */
public class MFTimeValue extends MFFieldValue {
	public MFTimeValue( ) {
		super();
	}

	public VrmlElement subclassFactory( int tokenOffset, TokenEnumerator v, Scene scene ) {
		if ( !SFTimeValue.valid( tokenOffset, v )) {
			SFTimeValue sfvec = new SFTimeValue( tokenOffset, v );
			optimizedValueCount++;
			return( sfvec );
		} else {
			optimizedValueCount ++;
			if ( optimizedValueCount == ( Table7.MFTimeLimit + 1 )) {
			    v.setState( tokenOffset );
			    SFTimeValue sfvec = new SFTimeValue( tokenOffset, v );
			    sfvec.setError( "Nonconformance, base profile limit of " + Table7.MFTimeLimit + " exceeded here" );
			    return( sfvec );
			}
			return( null );
		}
	}
}
