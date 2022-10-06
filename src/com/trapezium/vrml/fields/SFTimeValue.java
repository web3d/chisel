/*
 * @(#)SFTimeValue.java
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
import com.trapezium.vrml.Value;
import com.trapezium.vrml.Scene;

/**
 *  Scene graph component for an SFTime field value.
 *
 *  @author          Johannes N. Johannsen
 *  @version         1.1, 31 Oct 1997
 *
 *  @since           1.0
 */
public class SFTimeValue extends SFFieldValue {
	public SFTimeValue() {
		super();
	}
	
	public SFTimeValue( int tokenOffset, TokenEnumerator v ) {
	    super( tokenOffset );
	}

	public void init( int tokenOffset, TokenEnumerator v, Scene scene ) {
//		float floatValue = Float.parseFloat( tok.toString() );
//		System.out.println( "*** longValue is " + longValue + " ***" );
		addChild( new Value( tokenOffset ));
//		Date d = new Date( longValue );
//		int year = d.getYear();
//		int month = d.getMonth();
//		int day = d.getDay();
//		int hours = d.getHours();
//		int minutes = d.getMinutes();
//		int seconds = d.getSeconds();
//		System.out.println( year + ", " + month + ", " + day + ", " + hours + ", " + minutes + ", " + seconds );
//		addChild( new Value( year ));
//		addChild( new Value( month ));
//		addChild( new Value( day ));
//		addChild( new Value( hours ));
//		addChild( new Value( minutes ));
//		addChild( new Value( seconds ));
	}
	
	/** currently no validation on times */
	static public boolean valid( int tokenOffset, TokenEnumerator v ) {
	    return( true );
	}
}


